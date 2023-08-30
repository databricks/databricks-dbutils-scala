package com.databricks.sdk.scala
package dbutils

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ProxyDbfsTest extends AnyFlatSpec with Matchers {
  val dbutils = DBUtils.getDBUtils()
  val fs = dbutils.fs

  "A ProxyDBFS" should "be able to upload inside of DBFS" in {
    val testFilePath = s"/tmp/upload_and_download.txt"
    val testFile = "Hello, world!"
    fs.put(testFilePath, testFile)
    val result = fs.head(testFilePath)
    result should be(testFile)
  }
}
