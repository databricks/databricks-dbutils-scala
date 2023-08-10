package com.databricks.sdk.scala
package dbutils

import scala.collection.JavaConverters._
import com.databricks.sdk.WorkspaceClient
import com.databricks.sdk.core.{DatabricksConfig, DatabricksError, DatabricksException}
import com.databricks.sdk.service.files.{Delete, Put, ReadDbfsRequest, UploadFileRequest}
import org.apache.hadoop.fs.FileSystem

import java.io.{ByteArrayInputStream, InputStream}
import java.nio.charset.StandardCharsets

object SdkDbfsUtilsImpl {
  def unsupportedMethod(methodName: String): Nothing = {
    throw new UnsupportedOperationException(
      s"Method $methodName is not supported in the SDK version of DBUtils."
    )
  }

  def unsupportedField(methodName: String): Nothing = {
    throw new UnsupportedOperationException(
      s"Field $methodName is not supported in the SDK version of DBUtils."
    )
  }
}

/** Help is a no-op in the SDK version of DBUtils. */
trait NoHelp extends WithHelpMethods {
  override def help(): Unit = {}
  override def help(moduleOrMethod: String): Unit = {}
}

class SdkDBUtilsImpl(config: DatabricksConfig) extends DBUtils with NoHelp {
  private val client = new WorkspaceClient(config)
  def this() = this(new DatabricksConfig())

  override def widgets: WidgetsUtils = SdkDbfsUtilsImpl.unsupportedField("widgets")
  override def meta: MetaUtils = SdkDbfsUtilsImpl.unsupportedField("meta")
  override val fs: DbfsUtils = new SdkDbfsUtils(client)
  override def notebook: NotebookUtils = SdkDbfsUtilsImpl.unsupportedField("notebook")
  override def secrets: SecretUtils = SdkDbfsUtilsImpl.unsupportedField("secrets")
  override def library: LibraryUtils = SdkDbfsUtilsImpl.unsupportedField("library")
  override def credentials: DatabricksCredentialUtils = SdkDbfsUtilsImpl.unsupportedField("credentials")
  override def jobs: JobsUtils = SdkDbfsUtilsImpl.unsupportedField("jobs")
  override def data: DataUtils = SdkDbfsUtilsImpl.unsupportedField("data")
}

class SdkDbfsUtils(w: WorkspaceClient) extends DbfsUtils with NoHelp {
  override def ls(dir: String): Seq[FileInfo] = SdkDbfsUtilsImpl.unsupportedMethod("dbutils.fs.ls")

  override def rm(dir: String, recurse: Boolean): Boolean = {
    if (recurse) {
        throw new UnsupportedOperationException("Recursive delete is not yet supported in the SDK version of DBUtils.")
    }
    w.files().deleteFile(dir)
    // Should we list before and after? Swallow errors?
    true
  }

  override def mkdirs(dir: String): Boolean = SdkDbfsUtilsImpl.unsupportedMethod("dbutils.fs.mkdirs")

  override def cp(from: String, to: String, recurse: Boolean): Boolean = {
    if (recurse) {
      throw new UnsupportedOperationException("Recursive copy is not yet supported in the SDK version of DBUtils.")
    }
    mv(from, to, recurse, delete = false)
  }

  private def mv(from: String, to: String, recurse: Boolean, delete: Boolean): Boolean = {
    val inputStream = w.files().downloadFile(from)
    w.files().uploadFile(new UploadFileRequest().setFilePath(to).setBody(inputStream))
    if (delete) {
      w.files().deleteFile(from)
    }
    // What to return here?
    true
  }

  override def mv(from: String, to: String, recurse: Boolean): Boolean = {
    if (recurse) {
      throw new UnsupportedOperationException("Recursive move is not yet supported in the SDK version of DBUtils.")
    }
    mv(from, to, recurse, delete = true)
  }

  override def head(file: String, maxBytes: Int): String = {
    val fileStream = w.files().downloadFile(file)
    val byteArray = new Array[Byte](maxBytes)
    val numBytes = fileStream.read(byteArray)
    // Assuming the file is UTF-8-encoded
    new String(byteArray.slice(0, numBytes), StandardCharsets.UTF_8)
  }

  override def put(file: String, contents: String, overwrite: Boolean): Boolean = {
    try {
      w.files().uploadFile(
        new UploadFileRequest()
          .setFilePath(file)
          .setOverwrite(overwrite)
          .setBody(new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8)))
      )
    } catch {
      case e: DatabricksError if e.getMessage == "No matching namespace can be found" =>
        throw new IllegalArgumentException("requirement failed: Cannot upload to paths outside of /Volumes outside of DBR: " + file, e)
    }
    // What to return here?
    true
  }

  override def mount(
      source: String,
      mountPoint: String,
      encryptionType: String,
      owner: String,
      extraConfigs: Map[String, String]): Boolean = SdkDbfsUtilsImpl.unsupportedMethod("dbutils.fs.mount")

  override def updateMount(
      source: String,
      mountPoint: String,
      encryptionType: String,
      owner: String,
      extraConfigs: Map[String, String]): Boolean = SdkDbfsUtilsImpl.unsupportedMethod("dbutils.fs.updateMount")

  override def refreshMounts(): Boolean = SdkDbfsUtilsImpl.unsupportedMethod("dbutils.fs.refreshMounts")

  override def mounts(): Seq[MountInfo] = SdkDbfsUtilsImpl.unsupportedMethod("dbutils.fs.mounts")

  override def unmount(mountPoint: String): Boolean = SdkDbfsUtilsImpl.unsupportedMethod("dbutils.fs.unmount")

  override def dbfs: FileSystem = SdkDbfsUtilsImpl.unsupportedField("dbutils.fs.dbfs")
}
