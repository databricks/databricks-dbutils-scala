package com.databricks.sdk.scala
package dbutils

import scala.collection.JavaConverters._
import com.databricks.sdk.WorkspaceClient
import com.databricks.sdk.core.DatabricksConfig
import com.databricks.sdk.service.files.{Delete, Put, ReadDbfsRequest}

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
    ???
  }

  override def mv(from: String, to: String, recurse: Boolean): Boolean = {
    // check if from is a directory, and fail if recurse is not set

    // if from is a directory, recursively list files and move them one by one
  }

  override def head(file: String, maxBytes: Int): String = {
    w.dbfs().read(new ReadDbfsRequest().setPath(file).setLength(maxBytes)).toString
  }

  override def put(file: String, contents: String, overwrite: Boolean): Boolean = {
    w.dbfs().put(new Put().setPath(file).setContents(contents).setOverwrite(overwrite))
    // What should we return?
    true
  }

  override def cacheTable(tableName: String): Boolean = throw new NotImplementedError("cacheTable")

  override def uncacheTable(tableName: String): Boolean = throw new NotImplementedError("uncacheTable")

  override def cacheFiles(files: String*): Boolean = throw new NotImplementedError("cacheFiles")

  override def uncacheFiles(files: String*): Boolean = throw new NotImplementedError("uncacheFiles")

  override def mount(source: String, mountPoint: String, encryptionType: String, owner: String, extraConfigs: Map[String, String]): Boolean = ???

  override def updateMount(source: String, mountPoint: String, encryptionType: String, owner: String, extraConfigs: Map[String, String]): Boolean = ???

  override def refreshMounts(): Boolean = ???

  override def mounts(): Seq[MountInfo] = ???

  override def unmount(mountPoint: String): Boolean = ???
}
