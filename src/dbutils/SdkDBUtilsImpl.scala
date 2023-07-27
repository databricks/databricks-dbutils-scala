package com.databricks.sdk.scala
package dbutils

import scala.collection.JavaConverters._
import com.databricks.sdk.WorkspaceClient
import com.databricks.sdk.core.DatabricksConfig
import com.databricks.sdk.service.files.Delete

class SdkDBUtilsImpl(config: DatabricksConfig) extends DBUtils {
  private val client = new WorkspaceClient(config)
  def this() = this(new DatabricksConfig())

  override val widgets: WidgetsUtils = ???
  override val meta: MetaUtils = ???
  override val fs: DbfsUtils = new SdkDbfsUtils(client)
  override val notebook: NotebookUtils = ???
  override val secrets: SecretUtils = ???
  override val library: LibraryUtils = ???
  override val credentials: DatabricksCredentialUtils = ???
  override val jobs: JobsUtils = ???
}

class SdkDbfsUtils(w: WorkspaceClient) extends DbfsUtils {
  override def ls(dir: String): Seq[FileInfo] = w.dbfs().list(dir).asScala.toSeq.map { f =>
    val maybeSlash = if (f.getIsDir) "/" else ""
    val lastSegment = f.getPath.split("/").last
    FileInfo(f.getPath + maybeSlash, lastSegment + maybeSlash, f.getFileSize, f.getModificationTime)
  }

  override def rm(dir: String, recurse: Boolean): Boolean = {
    w.dbfs().delete(new Delete().setPath(dir).setRecursive(recurse))
    // Should we list before and after? Swallow errors?
    true
  }

  override def mkdirs(dir: String): Boolean = {
    w.dbfs().mkdirs(dir)
    // Should we list before and after? Swallow errors?
    true
  }

  override def cp(from: String, to: String, recurse: Boolean): Boolean = {
    w.dbfs()
  }

  override def mv(from: String, to: String, recurse: Boolean): Boolean = ???

  override def head(file: String, maxBytes: Int): String = ???

  override def put(file: String, contents: String, overwrite: Boolean): Boolean = ???

  override def cacheTable(tableName: String): Boolean = ???

  override def uncacheTable(tableName: String): Boolean = ???

  override def cacheFiles(files: String*): Boolean = ???

  override def uncacheFiles(files: String*): Boolean = ???

  override def mount(source: String, mountPoint: String, encryptionType: String, owner: String, extraConfigs: Map[String, String]): Boolean = ???

  override def updateMount(source: String, mountPoint: String, encryptionType: String, owner: String, extraConfigs: Map[String, String]): Boolean = ???

  override def refreshMounts(): Boolean = ???

  override def mounts(): Seq[MountInfo] = ???

  override def unmount(mountPoint: String): Boolean = ???
}
