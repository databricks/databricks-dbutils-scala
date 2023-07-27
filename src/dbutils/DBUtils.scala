package com.databricks.sdk.scala
package dbutils

import com.databricks.dbutils_v1.DBUtilsHolder

object DBUtils {
  lazy val INSTANCE = {
    try {
      new ProxyDBUtilsImpl(DBUtilsHolder.dbutils)
    } catch {
      case _: NotImplementedError => new SdkDBUtilsImpl()
    }
  }
}

trait DBUtils {
  val widgets: WidgetsUtils
  val meta: MetaUtils
  val fs: DbfsUtils
  val notebook: NotebookUtils
  val secrets: SecretUtils
  val library: LibraryUtils
  val credentials: DatabricksCredentialUtils
  // Is this necessary?
  // val data: DataUtils
  val jobs: JobsUtils
}

trait DbfsUtils {

  // Is this necessary?
  // def dbfs: FileSystem

  def ls(dir: String): Seq[FileInfo]

  def rm(dir: String, recurse: Boolean = false): Boolean

  def mkdirs(dir: String): Boolean

  def cp(from: String, to: String, recurse: Boolean = false): Boolean

  def mv(from: String, to: String, recurse: Boolean = false): Boolean

  def head(file: String, maxBytes: Int = 64 * 1024): String

  def put(file: String, contents: String, overwrite: Boolean = false): Boolean

  def cacheTable(tableName: String): Boolean

  def uncacheTable(tableName: String): Boolean

  def cacheFiles(files: String*): Boolean

  def uncacheFiles(files: String*): Boolean

  // Notes:
  // - extraConfigs is introduced with Runtime 4.0/Runtime 3.6 (if there is a Runtime 3.6).
  // - Clusters running on Runtime 3.1 and newer versions can use mount points specified
  //   with extraConfigs. However, in the public doc, we will say that users need Runtime 3.4
  //   and newer versions since Runtime 3.4 is the lowest Runtime versions supported by
  //   Azure Databricks.
  def mount(
      source: String,
      mountPoint: String,
      encryptionType: String = "",
      owner: String = null,
      extraConfigs: Map[String, String] = Map.empty[String, String]): Boolean

  def updateMount(
      source: String,
      mountPoint: String,
      encryptionType: String = "",
      owner: String = null,
      extraConfigs: Map[String, String] = Map.empty[String, String]): Boolean

  def refreshMounts(): Boolean

  def mounts(): Seq[MountInfo]

  def unmount(mountPoint: String): Boolean
}

case class FileInfo(path: String, name: String, size: Long, modificationTime: Long) {
  def isDir: Boolean = name.endsWith("/")
  def isFile: Boolean = !isDir
}

case class MountInfo(mountPoint: String, source: String, encryptionType: String)

trait WidgetsUtils {

  def get(argName: String): String

  def text(argName: String, defaultValue: String, label: String = null): Unit

  def dropdown(
      argName: String,
      defaultValue: String,
      choices: Seq[String],
      label: String = null): Unit

  def combobox(
      argName: String,
      defaultValue: String,
      choices: Seq[String],
      label: String = null): Unit

  def multiselect(
      argName: String,
      defaultValue: String,
      choices: Seq[String],
      label: String = null): Unit

  def remove(argName: String): Unit

  def removeAll(): Unit
}

trait MetaUtils {

  /**
   * Compiles a snippet of scala code under the given package name. If the compilation fails,
   * throws `com.databricks.backend.daemon.driver.dbutils_impl.ScalaDriverMetaUtils.PackageCompilationException`.
   * The compilation error is inside the exception's message.
   *
   * Note: in the old versions, it returns `false` instead of throwing `PackageCompilationException`.
   * The caller must maintain the compatibility. (SC-4889)
   *
   * @param packageName A valid path for a scala package.
   * @param code A valid content for a scala file.
   */
  def define(packageName: String, code: String): Boolean
}

trait NotebookUtils {
  def exit(value: String): Unit

  def run(
           path: String,
           timeoutSeconds: Int,
           arguments: scala.collection.Map[String, String] = Map.empty,
           __databricksInternalClusterSpec: String = null): String

  // Should these be exposed? They would only work in the context of a notebook.
  // def getContext(): CommandContext
  // def setContext(ctx: CommandContext): Unit
}

trait SecretUtils {
  def get(scope: String, key: String): String
  def getBytes(scope: String, key: String): Array[Byte]
  def list(scope: String): Seq[SecretMetadata]
  def listScopes(): Seq[SecretScope]
}
case class SecretMetadata(key: String)
case class SecretScope(name: String) {
  def getName(): String = name
}

trait LibraryUtils {

  /**
   * Restart the python process to make sure some pip installed libraries could take effect.
   */
  def restartPython(): Unit
}

trait DatabricksCredentialUtils {
  def assumeRole(role: String): Boolean
  // These methods return Java lists... we should expose them as Scala lists.
  def showCurrentRole(): Seq[String]
  def showRoles(): Seq[String]
}

trait JobsUtils {
  def taskValues: TaskValuesUtils
}

trait TaskValuesUtils {
  def set(key: String, value: Any): Unit
  // This is slightly different from the underlying DBUtils API, which returns Unit...
  def get(taskKey: String, key: String, default: Option[Any], debugValue: Option[Any]): Any
  def setJson(key: String, value: String): Unit
  def getJson(taskKey: String, key: String): Seq[String]

// Again, should we expose these?
//  def getContext(): CommandContext
//  def setContext(context: CommandContext): Unit
}
