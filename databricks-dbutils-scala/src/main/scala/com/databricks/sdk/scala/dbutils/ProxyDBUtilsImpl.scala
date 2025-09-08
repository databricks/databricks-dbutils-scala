package com.databricks.sdk.scala
package dbutils

import Implicits._
import com.databricks.sdk.scala.dbutils.ProxyDBUtilsImpl.getProxyInstance

import java.lang.reflect.{Method, Proxy}
import scala.reflect.{classTag, ClassTag}

private object Implicits {
  implicit class ReflectiveLookup(o: AnyRef) {

    /**
     * Get a field from an object using reflection. This restores the isAccessible state of the field after the
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

    /**
     * Get a field via an accessor method from an object using reflection.
     * This restores the isAccessible state of the field after the field is accessed.
     * This is very type-unsafe, so use with caution.
     *
     * This method is used to access lazy fields.
     *
     * @param field
     *   the name of the field
     * @tparam T
     *   the type of the field
     * @return
     *   the value of the field
     */
    def getFieldUsingGetter[T](field: String): T = {
      val getterMethod = o.getClass.getDeclaredMethod(field)
      val accessible = getterMethod.isAccessible
      if (!accessible) getterMethod.setAccessible(true)
      try {
        getterMethod.invoke(o).asInstanceOf[T]
      } finally {
        if (!accessible) getterMethod.setAccessible(false)
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

  def isParameterTypeCompatible(paramType: Class[_], arg: Any): Boolean = {
    // Argument shouldn't be null and primitive
    if (arg == null && paramType.isPrimitive) {
      throw new NoSuchMethodException("Unexpected null argument for primitive type")
    }

    // The following cases are valid:
    // 1. Argument is null but non-primitive.
    // 2. Argument's class is the same as paramType, or argument's class is a subtype of paramType.
    // 3. Argument's class is a boxed type and paramType is the corresponding primitive type.
    (arg == null && !paramType.isPrimitive) || (paramType.isAssignableFrom(
      arg.getClass)) || (paramType.isPrimitive && paramType.isAssignableFrom(toPrimitiveClass(arg.getClass)))
  }

  def toPrimitiveClass(clazz: Class[_]): Class[_] = clazz match {
    case c if c == classOf[java.lang.Boolean]   => java.lang.Boolean.TYPE
    case c if c == classOf[java.lang.Byte]      => java.lang.Byte.TYPE
    case c if c == classOf[java.lang.Character] => java.lang.Character.TYPE
    case c if c == classOf[java.lang.Double]    => java.lang.Double.TYPE
    case c if c == classOf[java.lang.Float]     => java.lang.Float.TYPE
    case c if c == classOf[java.lang.Integer]   => java.lang.Integer.TYPE
    case c if c == classOf[java.lang.Long]      => java.lang.Long.TYPE
    case c if c == classOf[java.lang.Short]     => java.lang.Short.TYPE
    case _                                      => clazz
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
          // getMethod must be called with classOf[Option[T]] rather than classOf[Some[T]], or with primitive classes
          // rather than boxed classes. For example:
          // def put(path: String, contents: String, overwrite: Boolean = false): Unit
          // has a method signature of put(String, String, boolean), but the last argument will be passed as a
          // boxed java.lang.Boolean at runtime.
          // So, we need to find the method that matches the name and number of arguments, and whose arguments are
          // either the same type or a subtype as the arguments passed in, or whose arguments are boxed types and whose
          // corresponding parameters are the corresponding primitive types.
          val backendMethod = backendInstance.getClass.getMethods
            .find { m =>
              m.getName == method.getName && m.getParameterTypes.length == convertedArgs.length &&
              m.getParameterTypes.zip(convertedArgs).forall { case (paramType, arg) =>
                isParameterTypeCompatible(paramType, arg)
              }
            }
            .getOrElse {
              throw new NoSuchMethodException(
                s"Method ${method.getName} with arguments ${convertedArgs.mkString(", ")} not found in " +
                  s"backend instance ${backendInstance.getClass.getName}")
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
    // This class is defined in DBR.
    val runIdClass = Class.forName("com.databricks.backend.common.storage.elasticspark.RunId")
    val rootRunIdOpt = commandContext.rootRunId.map { id =>
      runIdClass.getConstructor(classOf[Long]).newInstance(new java.lang.Long(id.id))
    }
    val currentRunIdOpt = commandContext.currentRunId.map { id =>
      runIdClass.getConstructor(classOf[Long]).newInstance(new java.lang.Long(id.id))
    }
    // This class is defined in DBR.
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
            // Note: modificationTime is not in DBR < 10.4 LTS.
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
    getProxyInstance[DatabricksCredentialUtils](baseObj.getFieldUsingGetter("credentials"))
  override val jobs: JobsUtils = getProxyInstance[JobsUtils](
    baseObj.getField("jobs"),
    Map("taskValues" -> new MethodCallAdapter(convertResult = { taskValues =>
      getProxyInstance[TaskValuesUtils](
        taskValues,
        Map(
          "getContext" -> new MethodCallAdapter(convertResult = ProxyDBUtilsImpl.fromInternalCommandContext),
          "setContext" -> new MethodCallAdapter(handleArgs = args =>
            Seq(ProxyDBUtilsImpl.toInternalCommandContext(args.head)))))
    })))
  // Not in DBR 7.3
  override val data: DataUtils = getProxyInstance[DataUtils](baseObj.getField("data"))
}
