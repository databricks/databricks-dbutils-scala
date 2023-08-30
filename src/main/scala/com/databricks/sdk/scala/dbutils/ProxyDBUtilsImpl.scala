package com.databricks.sdk.scala
package dbutils

import Implicits._

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
  def this() = this(DBUtilsWrapper.getDbUtils)

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

private object DBUtilsWrapper {
  private def getDbUtils: AnyRef = {
    val dbutilsHolderClass = Class.forName("com.databricks.dbutils_v1.DBUtilsHolder$")
    val dbutilsHolder = dbutilsHolderClass.getDeclaredField("MODULE$").get(null)
    val threadLocal = dbutilsHolder.getField[InheritableThreadLocal[AnyRef]]("dbutils0")
    threadLocal.get()
  }
}

class ProxyDBUtilsImpl() extends DBUtils {
  private val dbutils = new DBUtilsWrapper()

  override def help(): Unit = dbutils.help()

  override def help(moduleOrMethod: String): Unit = dbutils.help(moduleOrMethod)

  override val widgets: WidgetsUtils = new ProxyWidgetUtils(dbutils.forField("widgets"))
  override val meta: MetaUtils = new ProxyMetaUtils(dbutils.forField("meta"))
  override val fs: DbfsUtils = new ProxyDbfsUtils(dbutils.forField("fs"))
  override val notebook: NotebookUtils = new ProxyNotebookUtils(dbutils.forField("notebook"))
  override val secrets: SecretUtils = new ProxySecretUtils(dbutils.forField("secrets"))
  override val library: LibraryUtils = new ProxyLibraryUtils(dbutils.forField("library"))
  override val credentials: DatabricksCredentialUtils = new ProxyDatabricksCredentialUtils(
    dbutils.forField("credentials"))
  override val jobs: JobsUtils = new ProxyJobsUtils(dbutils.forField("jobs"))
  override val data: DataUtils = new ProxyDataUtils(dbutils.forField("data"))
}

private class ProxyWidgetUtils(widgets: DBUtilsWrapper) extends WidgetsUtils {

  override def help(): Unit = widgets.help()

  override def help(moduleOrMethod: String): Unit = widgets.help(moduleOrMethod)

  override def get(argName: String): String = widgets.invoke("get", Seq(argName))

  override def text(argName: String, defaultValue: String, label: String): Unit =
    widgets.invoke("text", Seq(argName, defaultValue, label))

  override def dropdown(argName: String, defaultValue: String, choices: Seq[String], label: String): Unit =
    widgets.invoke("dropdown", Seq(argName, defaultValue, choices, label))

  override def combobox(argName: String, defaultValue: String, choices: Seq[String], label: String): Unit =
    widgets.invoke("combobox", Seq(argName, defaultValue, choices, label))

  override def multiselect(argName: String, defaultValue: String, choices: Seq[String], label: String): Unit =
    widgets.invoke("multiselect", Seq(argName, defaultValue, choices, label))

  override def remove(argName: String): Unit = widgets.invoke("remove", Seq(argName))

  override def removeAll(): Unit = widgets.invoke("removeAll", Seq.empty)
}

private class ProxyMetaUtils(meta: DBUtilsWrapper) extends MetaUtils {

  override def help(): Unit = meta.help()

  override def help(moduleOrMethod: String): Unit = meta.help(moduleOrMethod)

  override def define(packageName: String, code: String): Boolean =
    meta.invoke("define", Seq(packageName, code))
}

private class ProxyDbfsUtils(fs: DBUtilsWrapper) extends DbfsUtils {

  override def help(): Unit = fs.help()

  override def help(moduleOrMethod: String): Unit = fs.help(moduleOrMethod)

  override def ls(dir: String): Seq[FileInfo] = fs.invoke(
    "ls",
    Seq(dir),
    p =>
      p.asInstanceOf[Seq[AnyRef]].map { p =>
        FileInfo(p.getField("path"), p.getField("name"), p.getField("size"), p.getField("modificationTime"))
      })

  override def rm(dir: String, recurse: Boolean): Boolean = fs.invoke("rm", Seq(dir, recurse))

  override def mkdirs(dir: String): Boolean = fs.invoke("mkdirs", Seq(dir))

  override def cp(from: String, to: String, recurse: Boolean): Boolean =
    fs.invoke("cp", Seq(from, to, recurse))

  override def mv(from: String, to: String, recurse: Boolean): Boolean =
    fs.invoke("mv", Seq(from, to, recurse))

  override def head(file: String, maxBytes: Int): String = fs.invoke("head", Seq(file, maxBytes))

  override def put(file: String, contents: String, overwrite: Boolean): Boolean =
    fs.invoke("put", Seq(file, contents, overwrite))

//  override def cacheTable(tableName: String): Boolean = dbutils.invoke("cacheTable", Seq(tableName))
//
//  override def uncacheTable(tableName: String): Boolean = dbutils.invoke("uncacheTable", Seq(tableName))
//
//  override def cacheFiles(files: String*): Boolean = dbutils.invoke("cacheFiles", Seq(files: _*))
//
//  override def uncacheFiles(files: String*): Boolean = dbutils.invoke("uncacheFiles", Seq(files: _*))

  override def mount(
      source: String,
      mountPoint: String,
      encryptionType: String = "",
      owner: String = null,
      extraConfigs: Map[String, String] = Map.empty): Boolean =
    fs.invoke("mount", Seq(source, mountPoint, encryptionType, owner, extraConfigs))

  override def updateMount(
      source: String,
      mountPoint: String,
      encryptionType: String = "",
      owner: String = null,
      extraConfigs: Map[String, String]): Boolean =
    fs.invoke("updateMount", Seq(source, mountPoint, encryptionType, owner, extraConfigs))

  override def refreshMounts(): Boolean = fs.invoke("refreshMounts", Seq())

  override def mounts(): Seq[MountInfo] = fs.invoke(
    "mounts",
    Seq.empty,
    m =>
      m.asInstanceOf[Seq[AnyRef]].map { m =>
        MountInfo(m.getField("mountPoint"), m.getField("source"), m.getField("encryptionType"))
      })

  override def unmount(mountPoint: String): Boolean = fs.invoke("unmount", Seq(mountPoint))
}

private class ProxyNotebookUtils(notebook: DBUtilsWrapper) extends NotebookUtils {
  override def help(): Unit = notebook.help()

  override def help(moduleOrMethod: String): Unit = notebook.help(moduleOrMethod)

  override def exit(value: String): Unit = notebook.invoke("exit", Seq(value))

  override def run(
      path: String,
      timeoutSeconds: Int,
      arguments: collection.Map[String, String],
      __databricksInternalClusterSpec: String): String =
    notebook.invoke("run", Seq(path, timeoutSeconds, arguments, __databricksInternalClusterSpec))

  override def getContext(): CommandContext = ???

  override def setContext(ctx: CommandContext): Unit = ???
}

private class ProxySecretUtils(secrets: DBUtilsWrapper) extends SecretUtils {

  override def help(): Unit = secrets.help()

  override def help(moduleOrMethod: String): Unit = secrets.help(moduleOrMethod)

  override def get(scope: String, key: String): String = secrets.invoke("get", Seq(scope, key))

  override def getBytes(scope: String, key: String): Array[Byte] =
    secrets.invoke("getBytes", Seq(scope, key))

  override def list(scope: String): Seq[SecretMetadata] = secrets.invoke(
    "list",
    Seq(scope),
    metadatas =>
      metadatas.asInstanceOf[Seq[AnyRef]].map { m =>
        SecretMetadata(m.getField("key"))
      })

  override def listScopes(): Seq[SecretScope] = secrets.invoke(
    "listScopes",
    Seq(),
    scopes =>
      scopes.asInstanceOf[Seq[AnyRef]].map { s =>
        SecretScope(s.getField("name"))
      })
}

private class ProxyLibraryUtils(library: DBUtilsWrapper) extends LibraryUtils {

  override def help(): Unit = library.help()

  override def help(moduleOrMethod: String): Unit = library.help(moduleOrMethod)

  override def restartPython(): Unit = library.invoke("restartPython", Seq())
}

private class ProxyDatabricksCredentialUtils(credentials: DBUtilsWrapper) extends DatabricksCredentialUtils {
  override def help(): Unit = credentials.help()

  override def help(moduleOrMethod: String): Unit = credentials.help(moduleOrMethod)

  override def assumeRole(role: String): Boolean = credentials.invoke("assumeRole", Seq(role))

  override def showCurrentRole(): java.util.List[String] = credentials.invoke("showCurrentRole", Seq.empty)

  override def showRoles(): java.util.List[String] = credentials.invoke("showRoles", Seq.empty)
}

private class ProxyJobsUtils(jobs: DBUtilsWrapper) extends JobsUtils {
  override def help(): Unit = jobs.help()

  override def help(moduleOrMethod: String): Unit = jobs.help(moduleOrMethod)

  override def taskValues: TaskValuesUtils = new ProxyTaskValuesUtils(jobs.forField("taskValues"))
}

private class ProxyTaskValuesUtils(taskValues: DBUtilsWrapper) extends TaskValuesUtils {
  override def help(): Unit = taskValues.help()

  override def help(moduleOrMethod: String): Unit = taskValues.help(moduleOrMethod)

  override def set(key: String, value: Any): Unit = taskValues.invoke("set", Seq(key, value))

  override def get(taskKey: String, key: String, default: Option[Any], debugValue: Option[Any]): Any =
    taskValues.invoke("get", Seq(taskKey, key, default, debugValue))

  override def setJson(key: String, value: String): Unit =
    taskValues.invoke("setJson", Seq(key, value))

  override def getJson(taskKey: String, key: String): Seq[String] =
    taskValues.invoke("getJson", Seq(taskKey, key))

  override def getContext(): CommandContext = taskValues.invoke(
    "getContext",
    Seq.empty,
    c => {
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
    })

  override def setContext(ctx: CommandContext): Unit = taskValues.invoke("setContext", Seq(ctx))
}

private class ProxyDataUtils(data: DBUtilsWrapper) extends DataUtils {
  override def help(): Unit = data.help()

  override def help(moduleOrMethod: String): Unit = data.help(moduleOrMethod)

  override def summarize(df: Any, precise: Boolean): Unit = data.invoke("summarize", Seq(df, precise))
}
