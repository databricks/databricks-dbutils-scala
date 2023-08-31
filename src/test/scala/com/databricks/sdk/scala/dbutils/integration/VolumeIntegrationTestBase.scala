package com.databricks.sdk.scala.dbutils.integration

import com.databricks.sdk.scala.dbutils.NameUtils
import com.databricks.sdk.service.catalog.{CreateSchema, CreateVolumeRequestContent, VolumeType}
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterAll
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class VolumeIntegrationTestBase extends IntegrationTestBase with Matchers with BeforeAndAfterAll {
  protected var testDir: String = _
  private val cleanup: mutable.Buffer[() => Unit] = ListBuffer()

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
        .setVolumeType(VolumeType.MANAGED))
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
