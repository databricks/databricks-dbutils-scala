package com.databricks.sdk.scala
package dbutils

import java.util.Objects

object TestEnvironment {
  private val env = System.getenv()
  val CATALOG: String = Objects.requireNonNull(env.get("DBUTILS_TEST_CATALOG"))
  val SCHEMA: String = Objects.requireNonNull(env.get("DBUTILS_TEST_SCHEMA"))
  val VOLUME: String = Objects.requireNonNull(env.get("DBUTILS_TEST_VOLUME"))
  val RUN_ID: String = Option(env.get("DBUTILS_TEST_RUN_ID")).getOrElse("manual-test")
  val IS_IN_DATABRICKS: Boolean = Option(env.get("DATABRICKS_RUNTIME_VERSION")).isDefined
  def getVolumeDir: String = s"/Volumes/$CATALOG/$SCHEMA/$VOLUME"
  def getTestDir: String = s"$getVolumeDir/test/dbutils/$RUN_ID"
}
