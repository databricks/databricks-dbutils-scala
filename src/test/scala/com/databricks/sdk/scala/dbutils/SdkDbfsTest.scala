package com.databricks.sdk.scala
package dbutils

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SdkDbfsTest extends AnyFlatSpec with Matchers {
  val dbutils = DBUtils.getDBUtils()
  val fs = dbutils.fs

  "An SDKDBFS" should "not be able to upload outside of DBFS" in {
    val testFilePath = s"/tmp/upload_and_download.txt"
    val testFile = "Hello, world!"

    val e = intercept[IllegalArgumentException] {
      fs.put(testFilePath, testFile)
    }

    e.getMessage should be ("requirement failed: Cannot upload to paths outside of /Volumes outside of DBR: /tmp/upload_and_download.txt")
  }

  // More tests to verify that we can't copy/move outside or across UC interface or delete outside of DBFS
}
