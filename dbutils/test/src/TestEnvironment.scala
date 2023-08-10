package com.databricks.sdk.scala
package dbutils

object TestEnvironment {
  private val env = System.getenv()
  val CATALOG: String = env.get("DBUTILS_TEST_CATALOG")
  val SCHEMA: String = env.get("DBUTILS_TEST_SCHEMA")
  val VOLUME: String = env.get("DBUTILS_TEST_VOLUME")
  val RUN_ID: String = env.get("DBUTILS_TEST_RUN_ID")
  val RUNTIME_VERSION: String = env.get("DATABRICKS_RUNTIME_VERSION")
  val IS_IN_DATABRICKS: Boolean = RUNTIME_VERSION != null && RUNTIME_VERSION.nonEmpty
  def getVolumeDir: String = s"/Volumes/$CATALOG/$SCHEMA/$VOLUME"
  def getTestDir: String = s"$getVolumeDir/test/dbutils/$RUN_ID"
}
