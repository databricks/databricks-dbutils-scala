package com.databricks.sdk.scala
package dbutils

import scala.collection.JavaConverters._
import com.databricks.dbutils_v1.DBUtilsHolder.dbutils
import com.databricks.backend.daemon.dbutils.{FileInfo => ProxyFileInfo, MountInfo => ProxyMountInfo}
import com.databricks.dbutils_v1.DBUtilsV1

class ProxyDBUtilsImpl(dbutils: DBUtilsV1) extends DBUtils {
  override val widgets: WidgetsUtils = new ProxyWidgetUtils(dbutils)
  override val meta: MetaUtils = new ProxyMetaUtils(dbutils)
  override val fs: DbfsUtils = new ProxyDbfsUtils(dbutils)
  override val notebook: NotebookUtils = new ProxyNotebookUtils(dbutils)
  override val secrets: SecretUtils = new ProxySecretUtils(dbutils)
  override val library: LibraryUtils = new ProxyLibraryUtils(dbutils)
  override val credentials: DatabricksCredentialUtils = new ProxyDatabricksCredentialUtils(dbutils)
  override val jobs: JobsUtils = new ProxyJobsUtils(dbutils)
}

class ProxyWidgetUtils(dbutils: DBUtilsV1) extends WidgetsUtils {
  override def get(argName: String): String = dbutils.widgets.get(argName)

  override def text(argName: String, defaultValue: String, label: String): Unit =
    dbutils.widgets.text(argName, defaultValue, label)

  override def dropdown(
      argName: String,
      defaultValue: String,
      choices: Seq[String],
      label: String): Unit = dbutils.widgets.dropdown(argName, defaultValue, choices, label)

  override def combobox(
      argName: String,
      defaultValue: String,
      choices: Seq[String],
      label: String): Unit = dbutils.widgets.combobox(argName, defaultValue, choices, label)

  override def multiselect(
      argName: String,
      defaultValue: String,
      choices: Seq[String],
      label: String): Unit = dbutils.widgets.multiselect(argName, defaultValue, choices, label)

  override def remove(argName: String): Unit = dbutils.widgets.remove(argName)

  override def removeAll(): Unit = dbutils.widgets.removeAll()
}

class ProxyMetaUtils(dbutils: DBUtilsV1) extends MetaUtils {
  override def define(packageName: String, code: String): Boolean =
    dbutils.meta.define(packageName, code)
}

class ProxyDbfsUtils(dbutils: DBUtilsV1) extends DbfsUtils {
  override def ls(dir: String): Seq[FileInfo] = dbutils.fs.ls(dir).map { p =>
    FileInfo(p.path, p.name, p.size, p.modificationTime)
  }

  override def rm(dir: String, recurse: Boolean): Boolean = dbutils.fs.rm(dir, recurse)

  override def mkdirs(dir: String): Boolean = dbutils.fs.mkdirs(dir)

  override def cp(from: String, to: String, recurse: Boolean): Boolean =
    dbutils.fs.cp(from, to, recurse)

  override def mv(from: String, to: String, recurse: Boolean): Boolean =
    dbutils.fs.mv(from, to, recurse)

  override def head(file: String, maxBytes: Int): String = dbutils.fs.head(file, maxBytes)

  override def put(file: String, contents: String, overwrite: Boolean): Boolean =
    dbutils.fs.put(file, contents, overwrite)

  override def cacheTable(tableName: String): Boolean = dbutils.fs.cacheTable(tableName)

  override def uncacheTable(tableName: String): Boolean = dbutils.fs.uncacheTable(tableName)

  override def cacheFiles(files: String*): Boolean = dbutils.fs.cacheFiles(files: _*)

  override def uncacheFiles(files: String*): Boolean = dbutils.fs.uncacheFiles(files: _*)

  override def mount(
      source: String,
      mountPoint: String,
      encryptionType: String = "",
      owner: String = null,
      extraConfigs: Map[String, String] = Map.empty): Boolean =
    dbutils.fs.mount(source, mountPoint, encryptionType, owner, extraConfigs)

  override def updateMount(
      source: String,
      mountPoint: String,
      encryptionType: String = "",
      owner: String = null,
      extraConfigs: Map[String, String]): Boolean =
    dbutils.fs.updateMount(source, mountPoint, encryptionType, owner, extraConfigs)

  override def refreshMounts(): Boolean = dbutils.fs.refreshMounts()

  override def mounts(): Seq[MountInfo] = dbutils.fs.mounts().map { m =>
    MountInfo(m.mountPoint, m.source, m.encryptionType)
  }

  override def unmount(mountPoint: String): Boolean = dbutils.fs.unmount(mountPoint)
}

class ProxyNotebookUtils(dbutils: DBUtilsV1) extends NotebookUtils {
  override def exit(value: String): Unit = dbutils.notebook.exit(value)

  override def run(
      path: String,
      timeoutSeconds: Int,
      arguments: collection.Map[String, String],
      __databricksInternalClusterSpec: String): String =
    dbutils.notebook.run(path, timeoutSeconds, arguments, __databricksInternalClusterSpec)
}

class ProxySecretUtils(dbutils: DBUtilsV1) extends SecretUtils {
  override def get(scope: String, key: String): String = dbutils.secrets.get(scope, key)

  override def getBytes(scope: String, key: String): Array[Byte] =
    dbutils.secrets.getBytes(scope, key)

  override def list(scope: String): Seq[SecretMetadata] = dbutils.secrets.list(scope).map {
    metadata =>
      SecretMetadata(metadata.key)
  }

  override def listScopes(): Seq[SecretScope] = dbutils.secrets.listScopes().map { scope =>
    SecretScope(scope.name)
  }
}

class ProxyLibraryUtils(dbutils: DBUtilsV1) extends LibraryUtils {
  override def restartPython(): Unit = dbutils.library.restartPython()
}

class ProxyDatabricksCredentialUtils(dbutils: DBUtilsV1) extends DatabricksCredentialUtils {
  override def assumeRole(role: String): Boolean = dbutils.credentials.assumeRole(role)

  override def showCurrentRole(): Seq[String] = dbutils.credentials.showCurrentRole().asScala

  override def showRoles(): Seq[String] = dbutils.credentials.showRoles().asScala
}

class ProxyJobsUtils(dbutils: DBUtilsV1) extends JobsUtils {
  override def taskValues: TaskValuesUtils = ProxyTaskValuesUtils
}

object ProxyTaskValuesUtils extends TaskValuesUtils {
  override def set(key: String, value: Any): Unit = dbutils.jobs.taskValues.set(key, value)

  override def get(
      taskKey: String,
      key: String,
      default: Option[Any],
      debugValue: Option[Any]): Any =
    dbutils.jobs.taskValues.get(taskKey, key, default, debugValue)

  override def setJson(key: String, value: String): Unit =
    dbutils.jobs.taskValues.setJson(key, value)

  override def getJson(taskKey: String, key: String): Seq[String] =
    dbutils.jobs.taskValues.getJson(taskKey, key).asScala
}
