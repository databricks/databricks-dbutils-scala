package com.databricks.sdk.scala.dbutils.examples

import com.databricks.sdk.scala.dbutils.DBUtils

object Example {
  final def main(args: Array[String]): Unit = {
    val dbutils = DBUtils.getDBUtils()
    dbutils.fs.put("/Volumes/main/default/scrap/file.txt", "Hello World!")
    val res = dbutils.fs.head("/Volumes/main/default/scrap/file.txt")
    if (res == "Hello World!") {
      println("Success!")
    } else {
      println("Failure!")
    }
  }
}
