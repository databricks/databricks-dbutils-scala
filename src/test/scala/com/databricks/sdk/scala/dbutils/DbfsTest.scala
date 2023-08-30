package com.databricks.sdk.scala
package dbutils

class DbfsTest extends DBUtilsTestBase {
  "DBFS" should "upload and download" in {
    val testFilePath = s"$testDir/upload_and_download.txt"
    val testFile = "Hello, world!"
    dbutils.fs.put(testFilePath, testFile)
    val result = dbutils.fs.head(testFilePath)
    result should be(testFile)
  }

  it should "cp non-recursive" in {
    val testFilePath = s"$testDir/cp.txt"
    val testFile = "Hello, world!"
    dbutils.fs.put(testFilePath, testFile)
    dbutils.fs.cp(testFilePath, s"$testDir/cp2.txt")
    val result = dbutils.fs.head(s"$testDir/cp2.txt")
    result should be(testFile)
    // Assert that the original file still exists
  }

  it should "mv non-recursive" in {
    val testFilePath = s"$testDir/mv.txt"
    val testFile = "Hello, world!"
    dbutils.fs.put(testFilePath, testFile)
    dbutils.fs.mv(testFilePath, s"$testDir/mv2.txt")
    val result = dbutils.fs.head(s"$testDir/mv2.txt")
    result should be(testFile)
    // Assert that the original file does not exist
  }

  it should "delete non-recursive" in {
    val testFilePath = s"$testDir/delete.txt"
    val testFile = "Hello, world!"
    dbutils.fs.put(testFilePath, testFile)
    dbutils.fs.rm(testFilePath)
    // Assert that the file does not exist
    assertThrows {
      dbutils.fs.head(testFilePath)
    }
  }
}
