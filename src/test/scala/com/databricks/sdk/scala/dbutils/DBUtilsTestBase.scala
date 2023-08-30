package com.databricks.sdk.scala.dbutils

import com.databricks.sdk.WorkspaceClient
import com.databricks.sdk.core.DatabricksConfig
import com.databricks.sdk.service.catalog.{CreateSchema, CreateVolumeRequestContent, VolumeType}
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.slf4j.{Logger, LoggerFactory}

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.function.Supplier
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class DBUtilsTestBase extends AnyFlatSpec with Matchers with BeforeAndAfterAll {
  protected val logger: Logger = LoggerFactory.getLogger(this.getClass)
  protected val config: DatabricksConfig = getDatabricksConfig
  protected val w = new WorkspaceClient(config)
  protected val dbutils: DBUtils = DBUtils.getDBUtils(config)
  protected var testDir: String = _
  private val cleanup: mutable.Buffer[() => Unit] = ListBuffer()

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
    resolveMethod.invoke(config, new Supplier[java.util.Map[String, String]] {
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


  override def beforeAll(): Unit = {
    super.beforeAll()
    val schemaName = NameUtils.uniqueName("dbutils")
    val newSchema = w.schemas.create(new CreateSchema().setName(schemaName).setCatalogName("main"))
    cleanup.prepend(() => w.schemas.delete(newSchema.getFullName))
    val volumeName = NameUtils.uniqueName("dbutils")
    val newVolume = w.volumes.create(
      new CreateVolumeRequestContent()
        .setCatalogName("main")
        .setSchemaName(schemaName)
        .setName(volumeName)
        .setVolumeType(VolumeType.MANAGED)
    )
    testDir = s"/Volumes/main/$schemaName/$volumeName"
    cleanup.prepend(() => w.volumes.delete(newVolume.getFullName))
  }

  override def afterAll(): Unit = {
    for (f <- cleanup) {
      f()
    }
    testDir = null
    super.afterAll()
  }
}
