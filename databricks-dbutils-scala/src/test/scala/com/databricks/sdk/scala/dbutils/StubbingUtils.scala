package com.databricks.sdk.scala.dbutils

object StubbingUtils {
  type UseSingleArg = { def thenReturn[T](arg: Any): T }
}
