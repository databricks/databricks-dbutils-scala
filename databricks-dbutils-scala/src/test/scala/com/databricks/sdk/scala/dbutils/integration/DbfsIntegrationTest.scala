package com.databricks.sdk.scala.dbutils.integration

import com.databricks.sdk.core.DatabricksException

class DbfsIntegrationTest extends VolumeIntegrationTestBase {
  "DBFS" should "upload and download" taggedAs Integration in {
    val testFilePath = s"$testDir/upload_and_download.txt"
    val testFile = "Hello, world!"
    dbutils.fs.put(testFilePath, testFile)
    val result = dbutils.fs.head(testFilePath)
    result should be(testFile)
  }

  it should "cp non-recursive" taggedAs Integration in {
    val testFilePath = s"$testDir/cp.txt"
    val testFile = "Hello, world!"
    dbutils.fs.put(testFilePath, testFile)
    dbutils.fs.cp(testFilePath, s"$testDir/cp2.txt")
    val result = dbutils.fs.head(s"$testDir/cp2.txt")
    result should be(testFile)
    // Assert that the original file still exists
  }

  it should "mv non-recursive" taggedAs Integration in {
    val testFilePath = s"$testDir/mv.txt"
    val testFile = "Hello, world!"
    dbutils.fs.put(testFilePath, testFile)
    dbutils.fs.mv(testFilePath, s"$testDir/mv2.txt")
    val result = dbutils.fs.head(s"$testDir/mv2.txt")
    result should be(testFile)
    // Assert that the original file does not exist
  }

  it should "delete non-recursive" taggedAs Integration in {
    val testFilePath = s"$testDir/delete.txt"
    val testFile = "Hello, world!"
    dbutils.fs.put(testFilePath, testFile)
    dbutils.fs.rm(testFilePath)
    // Assert that the file does not exist
    assertThrows[DatabricksException] {
      dbutils.fs.head(testFilePath)
    }
  }
}
