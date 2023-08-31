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

case class TestJobsUtils(taskValues: TaskValuesUtils = null)

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
    val proxyJobs = new TestJobsUtils(proxyTaskValues)
    val proxyBackend = TestDBUtils(jobs = proxyJobs)
    val proxyDbUtils = new ProxyDBUtilsImpl(proxyBackend)
    val res = proxyDbUtils.jobs.taskValues.get("taskKey", "key", Some("value"), Some("value"))
    assert(res === "test")
    verify(proxyTaskValues).get("taskKey", "key", Some("value"), Some("value"))
  }
}
