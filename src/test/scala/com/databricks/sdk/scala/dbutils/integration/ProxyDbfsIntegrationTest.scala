package com.databricks.sdk.scala.dbutils.integration

class ProxyDbfsIntegrationTest extends DBUtilsTestBase {

  "When inside DBR, FSUtils" should "be able to upload outside of /Volumes" taggedAs Integration in {
    if (!isInDbr) {
      cancel("This test must only be run inside DBR")
    }
    val testFilePath = s"/tmp/upload_and_download.txt"
    val testFile = "Hello, world!"
    dbutils.fs.put(testFilePath, testFile)
    val result = dbutils.fs.head(testFilePath)
    result should be(testFile)
  }
}
