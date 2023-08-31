package com.databricks.sdk.scala.dbutils

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
    jobs: JobsUtils = null)

class ProxyDbfsTest extends AnyFlatSpec {
  it should "call the appropriate backend method" in {
    val proxyLibrary = mock(classOf[LibraryUtils])
    val proxyBackend = TestDBUtils(library = proxyLibrary)
    val proxyDbUtils = new ProxyDBUtilsImpl(proxyBackend)
    proxyDbUtils.library.restartPython()
    verify(proxyLibrary).restartPython()
  }
}
