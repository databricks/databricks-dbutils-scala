package com.databricks.sdk.scala
package dbutils

class ProxyDbfsTest extends DBUtilsTestBase {

  "A ProxyDBFS" should "be able to upload inside of DBFS" in {
    val testFilePath = s"/tmp/upload_and_download.txt"
    val testFile = "Hello, world!"
    dbutils.fs.put(testFilePath, testFile)
    val result = dbutils.fs.head(testFilePath)
    result should be(testFile)
  }
}
