package com.databricks.sdk.scala
package dbutils

import utest._

object DbfsTest extends TestSuite {
  val tests = Tests {
    val dbutils = DBUtils.getDBUtils()
    val fs = dbutils.fs
    test("upload and download") {
      val testFilePath = s"${TestEnvironment.getTestDir}/upload_and_download.txt"
      println(s"Test file: $testFilePath")
      val testFile = "Hello, world!"
      fs.put(testFilePath, testFile)
      val result = fs.head(testFilePath)
      assert(result == testFile)
    }
//    if (TestEnvironment.IS_IN_DATABRICKS) {
//      test("can upload inside of DBFS") {
//        val testFilePath = s"/tmp/upload_and_download.txt"
//        val testFile = "Hello, world!"
//        fs.put(testFilePath, testFile)
//        val result = fs.head(testFilePath)
//        assert(result == testFile)
//      }
//    } else {
//      test("cannot upload outside of DBFS") {
//        val testFilePath = s"/tmp/upload_and_download.txt"
//        val testFile = "Hello, world!"
//        val e = intercept[IllegalArgumentException](fs.put(testFilePath, testFile))
//        assert(e.getMessage == "requirement failed: Cannot upload to paths outside of /Volumes outside of DBR: /tmp/upload_and_download.txt")
//      }
//    }
  }
}
