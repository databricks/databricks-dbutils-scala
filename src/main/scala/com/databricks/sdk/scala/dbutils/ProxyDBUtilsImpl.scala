package com.databricks.sdk.scala
package dbutils

import Implicits._
import com.databricks.sdk.scala.dbutils.ProxyDBUtilsImpl.getProxyInstance

import java.lang.reflect.{Method, Proxy}
import scala.reflect.{classTag, ClassTag}

private object Implicits {
  implicit class ReflectiveLookup(o: AnyRef) {

    /**
     * Get a field from an object using reflection. This restores the the isAccessible state of the field after the
     * field is accessed. This is very type-unsafe, so use with caution.
     *
     * @param field
     *   the name of the field
     * @tparam T
     *   the type of the field
     * @return
     *   the value of the field
     */
    def getField[T](field: String): T = {
      val f = o.getClass.getDeclaredField(field)
      val accessible = f.isAccessible
      if (!accessible) f.setAccessible(true)
      try {
        f.get(o).asInstanceOf[T]
      } finally {
        if (!accessible) f.setAccessible(false)
      }
    }
  }
}

/**
 * A wrapper around DBUtils or any of its fields.
 * @param baseObj
 *   the object to wrap
 */
private class DBUtilsWrapper(baseObj: AnyRef) {
  def forField(field: String): DBUtilsWrapper = {
    val fieldObj = baseObj.getField[AnyRef](field)
    new DBUtilsWrapper(fieldObj)
  }

  def invoke[T](methodName: String, args: Seq[Any], convert: AnyRef => T = (x: AnyRef) => x.asInstanceOf[T]): T = {
    val method = baseObj.getClass.getDeclaredMethod(methodName, args.map(_.getClass): _*)
    convert(method.invoke(baseObj, args.map(_.asInstanceOf[Object]): _*))
  }

  def help(): Unit = invoke("help", Seq.empty)

  def help(moduleOrMethod: String): Unit = invoke("help", Seq(moduleOrMethod))
}

private class MethodCallAdapter(
    val handleArgs: Seq[AnyRef] => Seq[AnyRef] = identity,
    val convertResult: AnyRef => AnyRef = identity)
private object MethodCallAdapter {
  val IDENTITY = new MethodCallAdapter(identity, identity)
}

private object ProxyDBUtilsImpl {
  private def getDbUtils: AnyRef = {
    val dbutilsHolderClass = Class.forName("com.databricks.dbutils_v1.DBUtilsHolder$")
    val dbutilsHolder = dbutilsHolderClass.getDeclaredField("MODULE$").get(null)
    val threadLocal = dbutilsHolder.getField[InheritableThreadLocal[AnyRef]]("dbutils0")
    threadLocal.get()
  }

  def getProxyInstance[T: ClassTag](
      backendInstance: AnyRef,
      converters: Map[String, MethodCallAdapter] = Map.empty): T = {
    Proxy
      .newProxyInstance(
        getClass.getClassLoader,
        Array(classTag[T].runtimeClass),
        (proxy: scala.Any, method: Method, args: Array[AnyRef]) => {
          val args0 = if (args == null) Seq.empty else args.toSeq
          val converter = converters.getOrElse(method.getName, MethodCallAdapter.IDENTITY)
          val convertedArgs = converters(method.getName).handleArgs(args0)
          val backendMethod = backendInstance.getClass.getMethod(method.getName, args.map(_.getClass): _*)
          val result = backendMethod.invoke(backendInstance, convertedArgs: _*)
          converter.convertResult(result)
        })
      .asInstanceOf[T]
  }

  def fromInternalCommandContext(c: AnyRef): CommandContext = {
    val internalRootRunIdOpt: Option[AnyRef] = c.getField("rootRunId")
    val internalRootRunId = internalRootRunIdOpt.map(id => id.getField[Long]("id"))
    val internalCurrentRunIdOpt = c.getField[Option[AnyRef]]("currentRunId")
    val internalCurrentRunId = internalCurrentRunIdOpt.map(id => id.getField[Long]("id"))
    CommandContext(
      rootRunId = internalRootRunId.map(RunId),
      currentRunId = internalCurrentRunId.map(RunId),
      jobGroup = c.getField("jobGroup"),
      tags = c.getField("tags"),
      extraContext = c.getField("extraContext"))
  }

  def toInternalCommandContext(c: AnyRef): AnyRef = {
    if (!c.isInstanceOf[CommandContext]) {
      throw new IllegalArgumentException(s"Expected CommandContext, got ${c.getClass}")
    }
    val commandContext = c.asInstanceOf[CommandContext]
    val runIdClass = Class.forName("com.databricks.backend.common.storage.elasticspark.RunId")
    val rootRunIdOpt = commandContext.rootRunId.map { id =>
      runIdClass.getConstructor(classOf[Long]).newInstance(new java.lang.Long(id.id))
    }
    val currentRunIdOpt = commandContext.currentRunId.map { id =>
      runIdClass.getConstructor(classOf[Long]).newInstance(new java.lang.Long(id.id))
    }
    val commandContextClass = Class.forName("com.databricks.backend.common.rpc.CommandContext")
    commandContextClass
      .getConstructor(
        classOf[Option[AnyRef]],
        classOf[Option[AnyRef]],
        classOf[String],
        classOf[Map[String, String]],
        classOf[Map[String, String]])
      .newInstance(
        rootRunIdOpt,
        currentRunIdOpt,
        commandContext.jobGroup,
        commandContext.tags,
        commandContext.extraContext)
      .asInstanceOf[AnyRef]
  }
}

class ProxyDBUtilsImpl private[dbutils] (baseObj: AnyRef) extends DBUtils {
  def this() = this(ProxyDBUtilsImpl.getDbUtils)
  private val dbutils = new DBUtilsWrapper(baseObj)

  override def help(): Unit = dbutils.help()

  override def help(moduleOrMethod: String): Unit = dbutils.help(moduleOrMethod)

  override val widgets: WidgetsUtils = getProxyInstance[WidgetsUtils](dbutils.forField("widgets"))
  override val meta: MetaUtils = getProxyInstance[MetaUtils](dbutils.forField("meta"))
  override val fs: DbfsUtils = getProxyInstance[DbfsUtils](
    dbutils.forField("fs"),
    Map(
      "ls" ->
        new MethodCallAdapter(convertResult = { p =>
          p.asInstanceOf[Seq[AnyRef]].map { p =>
            FileInfo(p.getField("path"), p.getField("name"), p.getField("size"), p.getField("modificationTime"))
          }
        }),
      "mounts" ->
        new MethodCallAdapter(convertResult = { m =>
          m.asInstanceOf[Seq[AnyRef]].map { m =>
            MountInfo(m.getField("mountPoint"), m.getField("source"), m.getField("encryptionType"))
          }
        })))
  override val notebook: NotebookUtils = getProxyInstance[NotebookUtils](
    dbutils.forField("notebook"),
    Map(
      "getContext" -> new MethodCallAdapter(convertResult = ProxyDBUtilsImpl.fromInternalCommandContext),
      "setContext" -> new MethodCallAdapter(handleArgs = args =>
        Seq(ProxyDBUtilsImpl.toInternalCommandContext(args.head)))))
  override val secrets: SecretUtils = getProxyInstance[SecretUtils](
    dbutils.forField("secrets"),
    Map(
      "list" -> new MethodCallAdapter(convertResult = { metadatas =>
        metadatas.asInstanceOf[Seq[AnyRef]].map { m =>
          SecretMetadata(m.getField("key"))
        }
      }),
      "listScopes" -> new MethodCallAdapter(convertResult = { scopes =>
        scopes.asInstanceOf[Seq[AnyRef]].map { s =>
          SecretScope(s.getField("name"))
        }
      })))
  override val library: LibraryUtils = getProxyInstance[LibraryUtils](dbutils.forField("library"))
  override val credentials: DatabricksCredentialUtils =
    getProxyInstance[DatabricksCredentialUtils](dbutils.forField("credentials"))
  override val jobs: JobsUtils = new ProxyJobsUtils(dbutils.forField("jobs"))
  override val data: DataUtils = getProxyInstance[DataUtils](dbutils.forField("data"))
}

private class ProxyJobsUtils(jobs: DBUtilsWrapper) extends JobsUtils {
  override def help(): Unit = jobs.help()

  override def help(moduleOrMethod: String): Unit = jobs.help(moduleOrMethod)

  override def taskValues: TaskValuesUtils = getProxyInstance[TaskValuesUtils](
    jobs.forField("taskValues"),
    Map(
      "getContext" -> new MethodCallAdapter(convertResult = ProxyDBUtilsImpl.fromInternalCommandContext),
      "setContext" -> new MethodCallAdapter(handleArgs = args =>
        Seq(ProxyDBUtilsImpl.toInternalCommandContext(args.head)))))
}
