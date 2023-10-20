package com.databricks.sdk.scala.dbutils

import com.databricks.sdk.scala.dbutils.StubbingUtils.UseSingleArg
import org.scalatest.flatspec.AnyFlatSpec
import org.mockito.Mockito._

case class TestDBUtils(
    widgets: WidgetsUtils = null,
    meta: MetaUtils = null,
    fs: DbfsUtils = null,
    notebook: NotebookUtils = null,
    secrets: SecretUtils = null,
    library: LibraryUtils = null,
    credentials: DatabricksCredentialUtils = null,
    data: DataUtils = null,
    jobs: TestJobsUtils = null)

case class TestJobsUtils(taskValuesObj: TaskValuesUtils = null) {
  def taskValues: TaskValuesUtils = taskValuesObj
}

class ProxyDbfsTest extends AnyFlatSpec {
  "dbutils.library.restartPython()" should "call restartPython()" in {
    val proxyLibrary = mock(classOf[LibraryUtils])
    val proxyBackend = TestDBUtils(library = proxyLibrary)
    val proxyDbUtils = new ProxyDBUtilsImpl(proxyBackend)
    proxyDbUtils.library.restartPython()
    verify(proxyLibrary).restartPython()
  }

  "dbutils.fs.ls()" should "call ls() and convert the response" in {
    // We mock using the DbfsUtils type, but in DBR, the underlying type is defined
    // in DBR.
    val proxyFs = mock(classOf[DbfsUtils])
    when(proxyFs.ls("/")).thenReturn(Seq(FileInfo("/test", "test", 0, 0)))
    val proxyBackend = TestDBUtils(fs = proxyFs)
    val proxyDbUtils = new ProxyDBUtilsImpl(proxyBackend)
    val res = proxyDbUtils.fs.ls("/")
    assert(res === Seq(FileInfo("/test", "test", 0, 0)))
    verify(proxyFs).ls("/")
  }

  "dbutils.fs.put()" should "call put()" in {
    val proxyFs = mock(classOf[DbfsUtils])
    val proxyBackend = TestDBUtils(fs = proxyFs)
    val proxyDbUtils = new ProxyDBUtilsImpl(proxyBackend)
    proxyDbUtils.fs.put("/test", "test")
    verify(proxyFs).put("/test", "test")
  }

  "dbutils.fs.mounts()" should "call mounts() and convert the response" in {
    // We mock using the DbfsUtils type, but in DBR, the underlying type is defined
    // in DBR.
    val proxyFs = mock(classOf[DbfsUtils])
    when(proxyFs.mounts()).thenReturn(Seq(MountInfo("/test", "s3", "sse")))
    val proxyBackend = TestDBUtils(fs = proxyFs)
    val proxyDbUtils = new ProxyDBUtilsImpl(proxyBackend)
    val res = proxyDbUtils.fs.mounts()
    assert(res === Seq(MountInfo("/test", "s3", "sse")))
    verify(proxyFs).mounts()
  }

  "dbutils.jobs.taskValues.get()" should "call get()" in {
    val proxyTaskValues = mock(classOf[TaskValuesUtils])
    when(proxyTaskValues.get("taskKey", "key", Option("value"), Option("value")))
      .asInstanceOf[UseSingleArg]
      .thenReturn("test")
    val proxyJobs = TestJobsUtils(proxyTaskValues)
    val proxyBackend = TestDBUtils(jobs = proxyJobs)
    val proxyDbUtils = new ProxyDBUtilsImpl(proxyBackend)
    val res = proxyDbUtils.jobs.taskValues.get("taskKey", "key", Some("value"), Some("value"))
    assert(res === "test")
    verify(proxyTaskValues).get("taskKey", "key", Some("value"), Some("value"))
  }

  "dbutils.notebook.getContext()" should "call getContext() and convert the response" in {
    val proxyNotebook = mock(classOf[NotebookUtils])
    when(proxyNotebook.getContext())
      .thenReturn(CommandContext(Some(RunId(0)), Some(RunId(1)), Some("test"), Map("a" -> "b"), Map("c" -> "d")))
    val proxyBackend = TestDBUtils(notebook = proxyNotebook)
    val proxyDbUtils = new ProxyDBUtilsImpl(proxyBackend)
    val res = proxyDbUtils.notebook.getContext()
    assert(res === CommandContext(Some(RunId(0)), Some(RunId(1)), Some("test"), Map("a" -> "b"), Map("c" -> "d")))
    verify(proxyNotebook).getContext()
  }

  "dbutils.secrets.list()" should "call list() and convert the response" in {
    val proxySecrets = mock(classOf[SecretUtils])
    when(proxySecrets.list("test")).thenReturn(Seq(SecretMetadata("testKey")))
    val proxyBackend = TestDBUtils(secrets = proxySecrets)
    val proxyDbUtils = new ProxyDBUtilsImpl(proxyBackend)
    val res = proxyDbUtils.secrets.list("test")
    assert(res === Seq(SecretMetadata("testKey")))
    verify(proxySecrets).list("test")
  }

  "dbutils.secrets.listScopes()" should "call listScopes() and convert the response" in {
    val proxySecrets = mock(classOf[SecretUtils])
    when(proxySecrets.listScopes()).thenReturn(Seq(SecretScope("testScope")))
    val proxyBackend = TestDBUtils(secrets = proxySecrets)
    val proxyDbUtils = new ProxyDBUtilsImpl(proxyBackend)
    val res = proxyDbUtils.secrets.listScopes()
    assert(res === Seq(SecretScope("testScope")))
    verify(proxySecrets).listScopes()
  }
}
