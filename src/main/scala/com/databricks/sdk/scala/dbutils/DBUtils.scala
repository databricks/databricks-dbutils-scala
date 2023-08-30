package com.databricks.sdk.scala
package dbutils

import com.databricks.sdk.core.DatabricksConfig

import javax.annotation.Nullable

object DBUtils {
  private var INSTANCE: DBUtils = _

  def getDBUtils: DBUtils = {
    getDBUtils(new DatabricksConfig())
  }

  /** This method */
  private[dbutils] def getDBUtils(databricksConfig: DatabricksConfig): DBUtils = {
    if (INSTANCE == null) {
      DBUtils.synchronized {
        if (INSTANCE == null) {
          val dbutils = try {
            new ProxyDBUtilsImpl()
          } catch {
            case _: ClassNotFoundException => new SdkDBUtilsImpl(databricksConfig)
          }
          INSTANCE = dbutils
        }
      }
    }
    INSTANCE
  }
}

trait DBUtils extends WithHelpMethods {
  def widgets: WidgetsUtils
  def meta: MetaUtils
  def fs: DbfsUtils
  def notebook: NotebookUtils
  def secrets: SecretUtils
  def library: LibraryUtils
  def credentials: DatabricksCredentialUtils
  def data: DataUtils
  def jobs: JobsUtils
}

trait WithHelpMethods {
  def help(): Unit

  def help(moduleOrMethod: String): Unit

  final def apply(): this.type = this
}

trait DbfsUtils extends Serializable with WithHelpMethods {

  // Is this necessary?
  // def dbfs: FileSystem

  def ls(dir: String): Seq[FileInfo]

  def rm(dir: String, recurse: Boolean = false): Boolean

  def mkdirs(dir: String): Boolean

  def cp(from: String, to: String, recurse: Boolean = false): Boolean

  def mv(from: String, to: String, recurse: Boolean = false): Boolean

  def head(file: String, maxBytes: Int = 64 * 1024): String

  def put(file: String, contents: String, overwrite: Boolean = false): Boolean

  // The *cache* methods are listed as no-op since Jan 1, 2017. Perhaps we should simply not include these.
//  def cacheTable(tableName: String): Boolean
//
//  def uncacheTable(tableName: String): Boolean
//
//  def cacheFiles(files: String*): Boolean
//
//  def uncacheFiles(files: String*): Boolean

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

trait WidgetsUtils extends Serializable with WithHelpMethods {

  def get(argName: String): String

  // This has been marked as deprecated for so long.
//  @deprecated(
//    "Use dbutils.widgets.text() or dbutils.widgets.dropdown() to create a widget and " +
//      "dbutils.widgets.get() to get its bound value."
//  )
//  def getArgument(argName: String, defaultValue: String): String

  def text(argName: String, defaultValue: String, @Nullable label: String = null): Unit

  def dropdown(argName: String, defaultValue: String, choices: Seq[String], @Nullable label: String = null): Unit

  def combobox(argName: String, defaultValue: String, choices: Seq[String], @Nullable label: String = null): Unit

  def multiselect(argName: String, defaultValue: String, choices: Seq[String], @Nullable label: String = null): Unit

  def remove(argName: String): Unit

  def removeAll(): Unit
}

trait MetaUtils extends Serializable with WithHelpMethods {

  /** Compiles a snippet of scala code under the given package name. If the compilation fails, throws
    * `com.databricks.backend.daemon.driver.dbutils_impl.ScalaDriverMetaUtils.PackageCompilationException`. The
    * compilation error is inside the exception's message.
    *
    * Note: in the old versions, it returns `false` instead of throwing `PackageCompilationException`. The caller must
    * maintain the compatibility. (SC-4889)
    *
    * @param packageName
    *   A valid path for a scala package.
    * @param code
    *   A valid content for a scala file.
    */
  def define(packageName: String, code: String): Boolean
}

trait NotebookUtils extends Serializable with WithHelpMethods {
  def exit(value: String): Unit

  def run(
    path: String,
    timeoutSeconds: Int,
    arguments: scala.collection.Map[String, String] = Map.empty,
    __databricksInternalClusterSpec: String = null): String

  // Apparently these are somewhat widely used.
  def getContext(): CommandContext
  def setContext(ctx: CommandContext): Unit
}

trait SecretUtils extends Serializable with WithHelpMethods {
  def get(scope: String, key: String): String
  def getBytes(scope: String, key: String): Array[Byte]
  def list(scope: String): Seq[SecretMetadata]
  def listScopes(): Seq[SecretScope]
}
case class SecretMetadata(key: String)
case class SecretScope(name: String) {
  def getName(): String = name
}

trait LibraryUtils extends Serializable with WithHelpMethods {

  /** Restart the python process to make sure some pip installed libraries could take effect.
    */
  // We are really exposing this in Scala?
  def restartPython(): Unit
}

trait DatabricksCredentialUtils extends Serializable with WithHelpMethods {
  def assumeRole(role: String): Boolean
  def showCurrentRole(): java.util.List[String]
  def showRoles(): java.util.List[String]
}

trait JobsUtils extends Serializable with WithHelpMethods {
  def taskValues: TaskValuesUtils
}

trait TaskValuesUtils extends Serializable with WithHelpMethods {
  def set(key: String, value: Any): Unit
  // This is slightly different from the underlying DBUtils API, which returns Unit...
  def get(taskKey: String, key: String, default: Option[Any], debugValue: Option[Any]): Any
  def setJson(key: String, value: String): Unit
  def getJson(taskKey: String, key: String): Seq[String]
  // These seem to duplicate corresponding methods in the NotebookUtils trait.
  def getContext(): CommandContext
  def setContext(context: CommandContext): Unit
}

trait DataUtils extends Serializable with WithHelpMethods {

  /** Summarize a DataFrame and visualize the statistics to get quick insights.
    */
  def summarize(df: Any, precise: Boolean = false): Unit
}

case class RunId private[dbutils] (id: Long)
case class CommandContext private[dbutils] (
  // Fields set by jobs to track workflows.
  rootRunId: Option[RunId],
  currentRunId: Option[RunId],
  // Unique command identifier that is injected by the driver.
  jobGroup: Option[String],
  // Attribution tags injected by the webapp.
  tags: Map[String, String],
  // Other fields that are propagated opaquely through the Jobs daemon and driver. We represent
  // this as a string map to ensure that fields are propagated correctly through even old
  // versions of Jobs daemon and driver packages.
  extraContext: Map[String, String])
