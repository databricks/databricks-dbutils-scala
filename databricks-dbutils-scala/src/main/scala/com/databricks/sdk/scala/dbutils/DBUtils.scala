package com.databricks.sdk.scala
package dbutils

import com.databricks.sdk.core.DatabricksConfig

import javax.annotation.Nullable

/**
 * DBUtils make it easy to perform powerful combinations of tasks. You can use the utilities to work with object storage
 * efficiently, to chain and parameterize notebooks, and to work with secrets.
 */
object DBUtils {
  private var INSTANCE: DBUtils = _

  /**
   * Returns a DBUtils instance. When run in DBR, the returned instance of DBUtils delegates all calls to the underlying
   * DBUtils implementation. When run outside of DBR, the returned instance of DBUtils implements the DBUtils interface
   * using the Databricks REST API.
   *
   * <p>This method is thread-safe.
   * @param databricksConfig
   *   the DatabricksConfig to use when running outside of DBR. Note that this parameter is ignored when running inside
   *   of DBR.
   * @return
   *   an instance of [[DBUtils]].
   */
  def getDBUtils(databricksConfig: DatabricksConfig = new DatabricksConfig()): DBUtils = {
    if (INSTANCE == null) {
      DBUtils.synchronized {
        if (INSTANCE == null) {
          val dbutils =
            try {
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

/**
 * DBUtils make it easy to perform powerful combinations of tasks. You can use the utilities to work with object storage
 * efficiently, to chain and parameterize notebooks, and to work with secrets.
 */
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

/**
 * [[WidgetsUtils]] provides utilities for working with notebook widgets. You can create different types of widgets and
 * get their bound value.
 */
trait WidgetsUtils extends Serializable with WithHelpMethods {

  /**
   * Retrieves current value of an input widget. The widget is identified by its unique name. If a widget with given
   * name does not exist an error is generated.
   *
   * Example: dbutils.widgets.get("product")
   *
   * @param argName
   *   unique name identifying the widget
   */
  def get(argName: String): String

  // This has been marked as deprecated for so long.
//  @deprecated(
//    "Use dbutils.widgets.text() or dbutils.widgets.dropdown() to create a widget and " +
//      "dbutils.widgets.get() to get its bound value."
//  )
//  def getArgument(argName: String, defaultValue: String): String

  /**
   * Creates a text input widget with a given name and default value. Optionally, you can provide a label for the text
   * widget that will be rendered in place of the name. If widget with a given name already exists, its properties will
   * be overwritten.
   *
   * Example: dbutils.widgets.text("product", "Camera", label = "Product Name")
   *
   * @param argName
   *   unique name identifying the widget
   * @param defaultValue
   *   value with which widget is populated by default
   * @param label
   *   optional widget label
   */
  def text(argName: String, defaultValue: String, @Nullable label: String = null): Unit

  /**
   * Creates a dropdown input widget a with given name, default value and choices. Optionally, you can provide a label
   * for the dropdown widget that will be rendered in place of the name. If a widget with a given name already exists,
   * its properties will be overwritten. The default value must be one of choices.
   *
   * Example: dbutils.widgets.dropdown("product", "Camera", Seq("Camera", "GPS", "Smartphone"))
   *
   * @param argName
   *   unique name identifying the widget
   * @param defaultValue
   *   value value which widget is populated by default. Must be one of choices
   * @param choices
   *   possible choices for the dropdown menu
   * @param label
   *   optional widget label
   */
  def dropdown(argName: String, defaultValue: String, choices: Seq[String], @Nullable label: String = null): Unit

  /**
   * Creates a combobox input widget with a given name, default value and choices. Optionally, you can provide a label
   * for the combobox widget that will be rendered in place of the name. If a widget with a given name already exists,
   * its properties will be overwritten. The default value does not have to be one choices.
   *
   * Example: dbutils.widgets.combobox("product", "Other", Seq("Camera", "GPS", "Smartphone"))
   *
   * @param argName
   *   unique name identifying the widget
   * @param defaultValue
   *   value value which widget is populated by default
   * @param choices
   *   possible choices for the dropdown menu
   * @param label
   *   optional widget label
   */
  def combobox(argName: String, defaultValue: String, choices: Seq[String], @Nullable label: String = null): Unit

  /**
   * Creates a multiselect input widget with a given name, default value and choices. Optionally, you can provide a
   * label for the dropdown widget that will be rendered in place of the name. If a widget with a given name already
   * exists, its properties will be overwritten. The default value must be one of choices. When using
   * dbutils.widgets.get() with a multiselect widget, you get a string of comma delimited items that are selected by
   * user.
   *
   * Example: dbutils.widgets.multiselect("product", "Camera", Seq("Camera", "GPS", "Smartphone"))
   *
   * @param argName
   *   unique name identifying the widget
   * @param defaultValue
   *   value value which widget is populated by default. Must be one of choices
   * @param choices
   *   possible choices for the dropdown menu
   * @param label
   *   optional widget label
   */
  def multiselect(argName: String, defaultValue: String, choices: Seq[String], @Nullable label: String = null): Unit

  /**
   * Removes an input widget from the notebook. The widget is identified by its unique name.
   *
   * Example: dbutils.widgets.remove("product")
   *
   * @param argName
   *   unique name of the widget to be removed
   */
  def remove(argName: String): Unit

  /**
   * Removes all widgets in the notebook.
   *
   * Example: dbutils.widgets.removeAll()
   */
  def removeAll(): Unit
}

/**
 * [[MetaUtils]] provides utilities for working with source and class files directly.
 */
trait MetaUtils extends Serializable with WithHelpMethods {

  /**
   * Compiles a class or object within the given package. Multiple class/object definitions may appear within the same
   * code block, though one-per-method call is recommended.
   *
   * Example:
   * {{{
   *  define("org.apache.spark",
   *    """
   * |import java.io.File
   *      |case class MyDataClass(num: Int, location: File)
   *    """.stripMargin)
   *  val data = sc.parallelize(0 until 10).map { i =>
   *    org.apache.spark.MyDataClass(i, new java.io.File("file" + i))
   *  }.collect()
   *  data.map(_._location).foreach(println)
   * }}}
   *
   * It is not legal to redefine a class or object after using it. An error will not be immediately thrown, but the
   * class is no longer valid for use. The behavior of redefining a class before using it is undefined. It is legal to
   * redefine a class if it was never compiled successfully.
   *
   * <p>Classes defined by this method are available on a per-cluster basis, meaning that they will be accessible by any
   * notebook running on this cluster. Additionally, the pitfalls regarding redefinition also apply on a per-cluster
   * basis.
   *
   * <p>Two convenience features are provided to help using this method:
   *   1. Calling this method with the exact same package/code Strings will not cause the compiler to be invoked twice.
   *      The result of the original compilation will be returned instead.
   *   1. If the provided code already includes the expected package declaration, it will be stripped out. If the
   *      package declaration does not correspond exactly to the given packageName, an exception will be thrown instead
   *      to prevent accidental nesting.
   *
   * @param packageName
   *   Package in which to compile the code.
   * @param code
   *   String of text to be compiled, similar to what would run in a notebook.
   * @return
   *   True if the code was compiled successfully.
   */
  def define(packageName: String, code: String): Boolean
}

/**
 * The notebook module.
 */
trait NotebookUtils extends Serializable with WithHelpMethods {

  /**
   * This method lets you exit a notebook with a value.
   *
   * @param value
   *   the value to return when exiting
   */
  def exit(value: String): Unit

  /**
   * This method runs a notebook and returns its exit value. The notebook will run in the current cluster by default.
   *
   * @param path
   *   relative path to the notebook, e.g. ../path/to/notebook
   * @param timeoutSeconds
   *   timeout in seconds for the called notebook. If the run failed to finish within this time, this method will throw
   *   an exception. Note that currently, if the Databricks web application is down for more than 10 minutes, the run
   *   will fail regardless of this parameter.
   * @param arguments
   *   string map of arguments to pass to the notebook
   * @param __databricksInternalClusterSpec
   * @return
   *   the string returned by dbutils.notebook.exit() or null
   * @throws WorkflowException
   *   if the notebook run did not complete successfully
   */
  def run(
      path: String,
      timeoutSeconds: Int,
      arguments: scala.collection.Map[String, String] = Map.empty,
      __databricksInternalClusterSpec: String = null): String

  def getContext(): CommandContext
  def setContext(ctx: CommandContext): Unit
}

/**
 * [[SecretUtils]] provides utilities for working with secrets.
 */
trait SecretUtils extends Serializable with WithHelpMethods {

  /**
   * Gets the string representation of a secret value with scope and key. This API assumes the secret is encoded as
   * UTF-8 bytes. This will always be the case if you use the `string_value` write API.
   *
   * Example:
   * {{{
   * dbutils.secrets.get("scope1", "key1")
   * }}}
   *
   * @param scope
   *   Scope in which the secret was created
   * @param key
   *   Key with which the secret was created
   */
  def get(scope: String, key: String): String

  /**
   * Gets the bytes representation of a secret value with scope and key.
   *
   * Example:
   * {{{
   * dbutils.secrets.getBytes("scope1", "key1")
   * }}}
   *
   * @param scope
   *   Scope in which the secret was created
   * @param key
   *   Key with which the secret was created
   */
  def getBytes(scope: String, key: String): Array[Byte]

  /**
   * Lists secret metadata for secrets within a scope.
   *
   * Example:
   * {{{
   * dbutils.secrets.list("scope2")
   * }}}
   *
   * @param scope
   *   Scope in which secrets were created
   * @return
   *   A list of secrets in the given scope.
   */
  def list(scope: String): Seq[SecretMetadata]

  /**
   * Lists all secret scopes.
   *
   * Example:
   * {{{
   *   dbutils.secrets.listScopes()
   * }}}
   * @return
   */
  def listScopes(): Seq[SecretScope]
}

/** The key of the secret within the secret scope. */
case class SecretMetadata(key: String)

/** The name of the secret scope. */
case class SecretScope(name: String) {

  /** Get the name of the secret scope. */
  def getName(): String = name
}

/**
 * [[LibraryUtils]] is a collection of utilities for managing libraries in a notebook.
 */
trait LibraryUtils extends Serializable with WithHelpMethods {

  /**
   * Restart python process for the current notebook session. This is useful for some of the whl, PyPI libraries which
   * requires a restart to reload the virtualenv. This could also be used to override some databricks pre-installed
   * library with your own version. This could only be called in a python notebook or with %python.
   *
   * Example:
   * {{{
   * dbutils.library.restartPython()
   * }}}
   */
  def restartPython(): Unit
}

/**
 * Provides utilities for interacting with credentials within notebooks. Only usable on clusters with credential
 * passthrough enabled. IAM credential passthrough is a legacy data governance model. Databricks recommends that you
 * upgrade to Unity Catalog. Unity Catalog simplifies security and governance of your data by providing a central place
 * to administer and audit data access across multiple workspaces in your account. For more information, please consult
 * the Databricks documentation.
 */
trait DatabricksCredentialUtils extends Serializable with WithHelpMethods {

  /**
   * Sets the role ARN to assume when looking for credentials to authenticate with S3. See what roles are available with
   * dbutils.credentials.showRoles(). If you try to assume a role that is not available to you nothing will happen. Only
   * usable on clusters with credential passthrough enabled.
   *
   * Example:
   * {{{
   * dbutils.credentials.assumeRole("arn:aws:iam::123456789012:group/Developers")
   * }}}
   *
   * @param role
   *   The role to assume
   */
  def assumeRole(role: String): Boolean

  /**
   * Shows the currently set role. Only usable on clusters with credential passthrough enabled.
   *
   * Example:
   * {{{
   * dbutils.credentials.showCurrentRole()
   * }}}
   */
  def showCurrentRole(): java.util.List[String]

  /**
   * Shows the set of possibly assumed roles. Only usable on clusters with credential passthrough enabled.
   *
   * Example:
   * {{{
   * dbutils.credentials.showRoles()
   * }}}
   */
  def showRoles(): java.util.List[String]
}

/**
 * [[JobsUtils]] provides utilities for working with jobs.
 */
trait JobsUtils extends Serializable with WithHelpMethods {
  def taskValues: TaskValuesUtils
}

/**
 * [[TaskValuesUtils]] provides utilities for working with task values.
 */
trait TaskValuesUtils extends Serializable with WithHelpMethods {

  /**
   * Sets a task value on the current task run. This method is a no-op if used outside of the job context.
   *
   * @param key
   *   the task value's key
   * @param value
   *   the value to be stored (must be JSON-serializable)
   */
  def set(key: String, value: Any): Unit

  /**
   * Returns the latest task value that belongs to the current job run.
   *
   * @param taskKey
   *   the task key of the task value
   * @param key
   *   the key of the task value
   * @param default
   *   the value to return when called inside of a job context if the task value does not exist (must not be None)
   * @param debugValue
   *   the value to return when called outside of a job context (must not be None)
   *
   * @return
   *   the task value (if it exists) when called inside of a job context
   */
  def get(taskKey: String, key: String, default: Option[Any], debugValue: Option[Any]): Any

  def setJson(key: String, value: String): Unit

  def getJson(taskKey: String, key: String): Seq[String]

  def getContext(): CommandContext

  def setContext(context: CommandContext): Unit
}

/**
 * [[DataUtils]] provides utilities for understanding and interpreting datasets. This module is currently in
 * <b>preview</b> and may be unstable.
 */
trait DataUtils extends Serializable with WithHelpMethods {

  /**
   * Summarize a Spark DataFrame and visualize the statistics to get quick insights.
   *
   * Example:
   * {{{
   * dbutils.data.summarize(df, precise=false)
   * }}}
   *
   * @param df
   *   The dataframe to summarize. Streaming dataframes are not supported.
   * @param precise
   *   If false, percentiles, distinct item counts, and frequent item counts will be computed approximately to reduce
   *   the run time. If true, distinct item counts and frequent item counts will be computed exactly, and percentiles
   *   will be computed with high precision.
   * @return
   *   visualization of the computed summmary statistics. Summarize a DataFrame and visualize the statistics to get
   *   quick insights.
   */
  def summarize(df: Any, precise: Boolean = false): Unit
}

case class RunId private[dbutils] (id: Long)
case class CommandContext private[dbutils] (
    /** The run ID of the root run in a workflow. */
    rootRunId: Option[RunId],

    /** The run ID of the current run in a workflow. */
    currentRunId: Option[RunId],

    /** Unique command identifier that is injected by the driver. */
    jobGroup: Option[String],

    /** Attribution tags injected by the webapp. */
    tags: Map[String, String],

    /**
     * Other fields that are propagated opaquely through the Jobs daemon and driver. We represent this as a string map
     * to ensure that fields are propagated correctly through even old versions of Jobs daemon and driver packages.
     */
    extraContext: Map[String, String])
