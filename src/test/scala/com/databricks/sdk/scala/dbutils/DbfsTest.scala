package com.databricks.sdk.scala
package dbutils

import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DbfsTest extends AnyFlatSpec with Matchers with BeforeAndAfterAll {
  val dbutils = DBUtils.getDBUtils()
  val fs = dbutils.fs

  "DBFS" should "upload and download" in {
    val testFilePath = s"${TestEnvironment.getTestDir}/upload_and_download.txt"
    val testFile = "Hello, world!"
    fs.put(testFilePath, testFile)
    val result = fs.head(testFilePath)
    result should be(testFile)
  }

  it should "cp non-recursive" in {
    val testFilePath = s"${TestEnvironment.getTestDir}/cp.txt"
    val testFile = "Hello, world!"
    fs.put(testFilePath, testFile)
    fs.cp(testFilePath, s"${TestEnvironment.getTestDir}/cp2.txt")
    val result = fs.head(s"${TestEnvironment.getTestDir}/cp2.txt")
    result should be(testFile)
    // Assert that the original file still exists
  }

  it should "mv non-recursive" in {
    val testFilePath = s"${TestEnvironment.getTestDir}/mv.txt"
    val testFile = "Hello, world!"
    fs.put(testFilePath, testFile)
    fs.mv(testFilePath, s"${TestEnvironment.getTestDir}/mv2.txt")
    val result = fs.head(s"${TestEnvironment.getTestDir}/mv2.txt")
    result should be(testFile)
    // Assert that the original file does not exist
  }

  it should "delete non-recursive" in {
    val testFilePath = s"${TestEnvironment.getTestDir}/delete.txt"
    val testFile = "Hello, world!"
    fs.put(testFilePath, testFile)
    fs.rm(testFilePath)
    // Assert that the file does not exist
  }
}
