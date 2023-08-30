package com.databricks.sdk.scala
package dbutils

import utest._

object SdkDbfsTest extends TestSuite {
  val tests = Tests {
    val dbutils = DBUtils.getDBUtils()
    val fs = dbutils.fs
    test("cannot upload outside of DBFS") {
      val testFilePath = s"/tmp/upload_and_download.txt"
      val testFile = "Hello, world!"
      val e = intercept[IllegalArgumentException](fs.put(testFilePath, testFile))
      assert(
        e.getMessage == "requirement failed: Cannot upload to paths outside of /Volumes outside of DBR: /tmp/upload_and_download.txt")
    }
    // More tests to verify that we can't copy/move outside or across UC interface or delete outside of DBFS
  }
}
