package com.databricks.sdk.scala
package dbutils

import com.databricks.sdk.WorkspaceClient
import com.databricks.sdk.core.{DatabricksConfig, DatabricksError}
import com.databricks.sdk.service.files.UploadRequest

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import scala.collection.JavaConverters._

private object SdkDBUtilsImpl {
  def unsupportedMethod(methodName: String): Nothing =
    throw new UnsupportedOperationException(s"Method $methodName is not supported in the SDK version of DBUtils.")

  def unsupportedField(fieldName: String): Nothing =
    throw new UnsupportedOperationException(s"Field $fieldName is not supported in the SDK version of DBUtils.")
}

/** Help is a no-op in the SDK version of DBUtils. */
trait NoHelp extends WithHelpMethods {
  override def help(): Unit = {}
  override def help(moduleOrMethod: String): Unit = {}
}

class SdkDBUtilsImpl(config: DatabricksConfig) extends DBUtils with NoHelp {
  private val client = new WorkspaceClient(config)
  def this() = this(new DatabricksConfig())

  override def widgets: WidgetsUtils = SdkDBUtilsImpl.unsupportedField("widgets")
  override def meta: MetaUtils = SdkDBUtilsImpl.unsupportedField("meta")
  override val fs: DbfsUtils = new SdkDbfsUtils(client)
  override def notebook: NotebookUtils = SdkDBUtilsImpl.unsupportedField("notebook")
  override def secrets: SecretUtils = new SdkSecretsUtils(client)
  override def library: LibraryUtils = SdkDBUtilsImpl.unsupportedField("library")
  override def credentials: DatabricksCredentialUtils = SdkDBUtilsImpl.unsupportedField("credentials")
  override val jobs: JobsUtils = new SdkJobsUtils
  override def data: DataUtils = SdkDBUtilsImpl.unsupportedField("data")
}

private class SdkDbfsUtils(w: WorkspaceClient) extends DbfsUtils with NoHelp {
  override def ls(dir: String): Seq[FileInfo] = SdkDBUtilsImpl.unsupportedMethod("dbutils.fs.ls")

  override def rm(file: String, recurse: Boolean): Boolean = {
    if (recurse) {
      throw new UnsupportedOperationException("Recursive delete is not yet supported in the SDK version of DBUtils.")
    }
    w.files().delete(file)
    // Should we list before and after? Swallow errors?
    true
  }

  override def mkdirs(dir: String): Boolean = SdkDBUtilsImpl.unsupportedMethod("dbutils.fs.mkdirs")

  override def cp(from: String, to: String, recurse: Boolean): Boolean = {
    if (recurse) {
      throw new UnsupportedOperationException("Recursive copy is not yet supported in the SDK version of DBUtils.")
    }
    mv(from, to, recurse, delete = false)
  }

  private def mv(from: String, to: String, recurse: Boolean, delete: Boolean): Boolean = {
    if (from == to) {
      return true
    }

    val inputStream = w.files().download(from).getContents
    try {
      w.files().upload(new UploadRequest().setFilePath(to).setContents(inputStream))
      if (delete) {
        w.files().delete(from)
      }
    } finally {
      inputStream.close()
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
    val fileStream = w.files().download(file).getContents
    try {
      val byteArray = new Array[Byte](maxBytes)
      val numBytes = fileStream.read(byteArray)
      // Assuming the file is UTF-8-encoded
      new String(byteArray.slice(0, numBytes), StandardCharsets.UTF_8)
    } finally {
      fileStream.close()
    }
  }

  override def put(file: String, contents: String, overwrite: Boolean): Boolean = {
    try {
      w.files()
        .upload(
          new UploadRequest()
            .setFilePath(file)
            .setOverwrite(overwrite)
            .setContents(new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8))))
    } catch {
      case e: DatabricksError if e.getMessage == "No matching namespace can be found" =>
        throw new IllegalArgumentException(
          "requirement failed: Cannot upload to paths outside of /Volumes outside of DBR: " + file,
          e)
    }
    // What to return here?
    true
  }

  override def mount(
      source: String,
      mountPoint: String,
      encryptionType: String,
      owner: String,
      extraConfigs: Map[String, String]): Boolean = SdkDBUtilsImpl.unsupportedMethod("dbutils.fs.mount")

  override def updateMount(
      source: String,
      mountPoint: String,
      encryptionType: String,
      owner: String,
      extraConfigs: Map[String, String]): Boolean = SdkDBUtilsImpl.unsupportedMethod("dbutils.fs.updateMount")

  override def refreshMounts(): Boolean = SdkDBUtilsImpl.unsupportedMethod("dbutils.fs.refreshMounts")

  override def mounts(): Seq[MountInfo] = SdkDBUtilsImpl.unsupportedMethod("dbutils.fs.mounts")

  override def unmount(mountPoint: String): Boolean = SdkDBUtilsImpl.unsupportedMethod("dbutils.fs.unmount")
}

private class SdkSecretsUtils(client: WorkspaceClient) extends SecretUtils with NoHelp {
  override def get(scope: String, key: String): String =
    client.secrets().get(scope, key)

  override def getBytes(scope: String, key: String): Array[Byte] =
    client.secrets().getBytes(scope, key)

  override def list(scope: String): Seq[SecretMetadata] =
    client.secrets().listSecrets(scope).asScala.toSeq.map { secret =>
      SecretMetadata(secret.getKey)
    }

  override def listScopes(): Seq[SecretScope] =
    client.secrets().listScopes().asScala.toSeq.map { scope =>
      SecretScope(scope.getName)
    }
}

private class SdkJobsUtils extends JobsUtils with NoHelp {
  override val taskValues: TaskValuesUtils = new SdkTaskValues
}

private class SdkTaskValues extends TaskValuesUtils with NoHelp {
  private var commandContext =
    CommandContext()
  /**
   * Sets a task value on the current task run. This method is a no-op if used outside of the job context.
   *
   * @param key
   * the task value's key
   * @param value
   * the value to be stored (must be JSON-serializable)
   */
  override def set(key: String, value: Any): Unit = throw new NotImplementedError("set is not supported in Scala.")

  /**
   * Returns the latest task value that belongs to the current job run.
   *
   * @param taskKey
   * the task key of the task value
   * @param key
   * the key of the task value
   * @param default
   * the value to return when called inside of a job context if the task value does not exist (must not be None)
   * @param debugValue
   * the value to return when called outside of a job context (must not be None)
   * @return
   * the task value (if it exists) when called inside of a job context
   */
  override def get(taskKey: String, key: String, default: Option[Any], debugValue: Option[Any]): Any =
    throw new NotImplementedError("get is not supported in Scala.")

  override def setJson(key: String, value: String): Unit =
    throw new NotImplementedError("setJson is not supported for local development in Scala.")

  override def getJson(taskKey: String, key: String): Seq[String] = Seq.empty

  override def getContext(): CommandContext = {
    commandContext
  }

  override def setContext(context: CommandContext): Unit = {
    commandContext = context
    println(context)
    println(commandContext)
  }
}
