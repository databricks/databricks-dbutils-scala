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
    dbutils.fs.rm("/Volumes/main/default/scrap/file.txt")

    val scopes = dbutils.secrets.listScopes()
    val secrets = dbutils.secrets.list(scopes.head.name)
    val secret = dbutils.secrets.get(scopes.head.name, secrets.head.key)
    println(s"Got secret ${secrets.head.key} from scope ${scopes.head.name}")
  }
}
