package com.databricks.sdk.scala.dbutils.integration
import com.databricks.sdk.core.DatabricksError

class SdkDbfsIntegrationTest extends VolumeIntegrationTestBase {
  "When outside of DBR, FSUtils" should "not be able to upload outside of /Volumes" taggedAs Integration in {
    if (isInDbr) {
      cancel("This test must only be run inside DBR")
    }
    val testFilePath = s"/tmp/upload_and_download.txt"
    val testFile = "Hello, world!"

    val e = intercept[DatabricksError] {
      dbutils.fs.put(testFilePath, testFile)
    }

    e.getMessage should be("Invalid path: unsupported first path component: tmp")
  }

  // More tests to verify that we can't copy/move outside or across UC interface or delete outside of DBFS
}
