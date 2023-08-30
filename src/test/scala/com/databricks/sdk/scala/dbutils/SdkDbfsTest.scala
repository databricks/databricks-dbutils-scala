package com.databricks.sdk.scala
package dbutils

class SdkDbfsTest extends DBUtilsTestBase {
  "An SDKDBFS" should "not be able to upload outside of DBFS" in {
    val testFilePath = s"/tmp/upload_and_download.txt"
    val testFile = "Hello, world!"

    val e = intercept[IllegalArgumentException] {
      dbutils.fs.put(testFilePath, testFile)
    }

    e.getMessage should be(
      "requirement failed: Cannot upload to paths outside of /Volumes outside of DBR: /tmp/upload_and_download.txt")
  }

  // More tests to verify that we can't copy/move outside or across UC interface or delete outside of DBFS
}
