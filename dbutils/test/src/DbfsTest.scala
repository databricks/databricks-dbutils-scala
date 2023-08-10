package com.databricks.sdk.scala
package dbutils

import utest._

object DbfsTest extends TestSuite {
  val tests = Tests {
    val dbutils = DBUtils.getDBUtils()
    val fs = dbutils.fs
    test("upload and download") {
      val testFilePath = s"${TestEnvironment.getTestDir}/upload_and_download.txt"
      val testFile = "Hello, world!"
      fs.put(testFilePath, testFile)
      val result = fs.head(testFilePath)
      assert(result == testFile)
    }
    test("cp non-recursive") {
      val testFilePath = s"${TestEnvironment.getTestDir}/cp.txt"
      val testFile = "Hello, world!"
      fs.put(testFilePath, testFile)
      fs.cp(testFilePath, s"${TestEnvironment.getTestDir}/cp2.txt")
      val result = fs.head(s"${TestEnvironment.getTestDir}/cp2.txt")
      assert(result == testFile)
      // Assert that the original file still exists
    }
    test("mv") {
      val testFilePath = s"${TestEnvironment.getTestDir}/mv.txt"
      val testFile = "Hello, world!"
      fs.put(testFilePath, testFile)
      fs.mv(testFilePath, s"${TestEnvironment.getTestDir}/mv2.txt")
      val result = fs.head(s"${TestEnvironment.getTestDir}/mv2.txt")
      assert(result == testFile)
      // Assert that the original file does not exist
    }
  }
}
