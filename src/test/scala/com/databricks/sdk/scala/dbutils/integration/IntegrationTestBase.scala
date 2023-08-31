package com.databricks.sdk.scala.dbutils.integration

import com.databricks.sdk.WorkspaceClient
import com.databricks.sdk.core.DatabricksConfig
import com.databricks.sdk.scala.dbutils.DBUtils
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import org.scalatest.Tag
import org.scalatest.flatspec.AnyFlatSpec
import org.slf4j.{Logger, LoggerFactory}

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.function.Supplier

class IntegrationTestBase extends AnyFlatSpec {
  protected val logger: Logger = LoggerFactory.getLogger(this.getClass)
  protected val config: DatabricksConfig = getDatabricksConfig
  protected val w = new WorkspaceClient(config)
  protected val dbutils: DBUtils = DBUtils.getDBUtils(config)

  protected def isInDbr: Boolean = {
    System.getenv("DATABRICKS_RUNTIME_VERSION") != null
  }

  class DebugEnv extends TypeReference[java.util.Map[String, java.util.Map[String, String]]]

  private def getDatabricksConfig: DatabricksConfig = {
    if (isInDbr) {
      // Use unified auth in DBR
      logger.info("Using unified auth in DBR")
      return new DatabricksConfig()
    }
    // try to get the workspace environment from ~/.databricks/debug-env.json
    val path = System.getProperty("user.home") + "/.databricks/debug-env.json"
    if (!Paths.get(path).toFile.exists()) {
      logger.info("debug-env.conf not found, using auth from environment")
      return new DatabricksConfig()
    }
    val mapper = getObjectMapper
    val file = Files.readAllBytes(Paths.get(path))
    val json = new String(file, StandardCharsets.UTF_8)
    val allEnv = mapper.readValue(json, new DebugEnv)
    val env = allEnv.get("ucws")
    if (env == null) {
      logger.info("workspace env not defined, using auth from environment")
      return new DatabricksConfig()
    }
    val config = new DatabricksConfig()
    val resolveMethod = classOf[DatabricksConfig].getDeclaredMethod("resolve", classOf[Supplier[Map[String, String]]])
    resolveMethod.setAccessible(true)
    resolveMethod.invoke(
      config,
      new Supplier[java.util.Map[String, String]] {
        override def get(): java.util.Map[String, String] = env
      })
    logger.info("loaded workspace env from debug-env.conf")
    config
  }

  private def getObjectMapper: ObjectMapper = {
    new ObjectMapper()
      .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
      .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
      .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
      .setSerializationInclusion(JsonInclude.Include.NON_NULL)
  }
}

object Integration extends Tag("com.databricks.sdk.scala.dbutils.Integration")
object SecretsGet extends Tag("com.databricks.sdk.scala.dbutils.SecretsGet")
