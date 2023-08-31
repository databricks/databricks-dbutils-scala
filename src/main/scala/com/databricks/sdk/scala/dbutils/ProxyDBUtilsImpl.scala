package com.databricks.sdk.scala
package dbutils

import Implicits._
import com.databricks.sdk.scala.dbutils.ProxyDBUtilsImpl.getProxyInstance

import java.lang.reflect.{Method, Proxy, UndeclaredThrowableException}
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
          val convertedArgs = converter.handleArgs(args0)
          // Some types in the SDK DBUtils implementation don't match the corresponding types in DBR (like
          // CommandContext, RunId, FileInfo, MountInfo, etc.). To look up the correct method in DBR, we need to use
          // the corresponding types in DBR. Conveniently, handleArgs maps these arguments to the corresponding types
          // in DBR. However, getMethod requires exact types and not subtypes, so for methods that take Option[T],
          // getMethod must be called with classOf[Option[T]] rather than classOf[Some[T]]. In this case, we fall back
          // to the original types defined in the SDK DBUtils interfaces. This does mean that it is impossible to
          // support methods with an Option[T] argument and a separate argument with a different type than defined in
          // DBR.
          val backendMethod =
            try {
              backendInstance.getClass.getMethod(method.getName, convertedArgs.map(_.getClass): _*)
            } catch {
              case _: NoSuchMethodException =>
                backendInstance.getClass.getMethod(method.getName, method.getParameterTypes: _*)
            }
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
  private val dbutils = getProxyInstance[DBUtils](baseObj)

  override def help(): Unit = dbutils.help()

  override def help(moduleOrMethod: String): Unit = dbutils.help(moduleOrMethod)

  override val widgets: WidgetsUtils = getProxyInstance[WidgetsUtils](baseObj.getField("widgets"))
  override val meta: MetaUtils = getProxyInstance[MetaUtils](baseObj.getField("meta"))
  override val fs: DbfsUtils = getProxyInstance[DbfsUtils](
    baseObj.getField("fs"),
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
    baseObj.getField("notebook"),
    Map(
      "getContext" -> new MethodCallAdapter(convertResult = ProxyDBUtilsImpl.fromInternalCommandContext),
      "setContext" -> new MethodCallAdapter(handleArgs = args =>
        Seq(ProxyDBUtilsImpl.toInternalCommandContext(args.head)))))
  override val secrets: SecretUtils = getProxyInstance[SecretUtils](
    baseObj.getField("secrets"),
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
  override val library: LibraryUtils = getProxyInstance[LibraryUtils](baseObj.getField("library"))
  override val credentials: DatabricksCredentialUtils =
    getProxyInstance[DatabricksCredentialUtils](baseObj.getField("credentials"))
  override val jobs: JobsUtils = new ProxyJobsUtils(baseObj.getField("jobs"))
  override val data: DataUtils = getProxyInstance[DataUtils](baseObj.getField("data"))
}

private class ProxyJobsUtils(jobs: AnyRef) extends JobsUtils {
  private val jobsProxy = getProxyInstance[JobsUtils](jobs)
  override def help(): Unit = jobsProxy.help()

  override def help(moduleOrMethod: String): Unit = jobsProxy.help(moduleOrMethod)

  override def taskValues: TaskValuesUtils = getProxyInstance[TaskValuesUtils](
    jobs.getField("taskValues"),
    Map(
      "getContext" -> new MethodCallAdapter(convertResult = ProxyDBUtilsImpl.fromInternalCommandContext),
      "setContext" -> new MethodCallAdapter(handleArgs = args =>
        Seq(ProxyDBUtilsImpl.toInternalCommandContext(args.head)))))
}
