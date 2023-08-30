package com.databricks.sdk.scala
package dbutils

import utest._

object ProxyDbfsTest extends TestSuite {
  val tests = Tests {
    val dbutils = DBUtils.getDBUtils()
    val fs = dbutils.fs
    test("can upload inside of DBFS") {
      val testFilePath = s"/tmp/upload_and_download.txt"
      val testFile = "Hello, world!"
      fs.put(testFilePath, testFile)
      val result = fs.head(testFilePath)
      assert(result == testFile)
    }
  }
}
