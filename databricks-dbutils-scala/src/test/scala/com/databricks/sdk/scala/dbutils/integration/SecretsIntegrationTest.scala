package com.databricks.sdk.scala.dbutils.integration

import com.databricks.sdk.scala.dbutils.NameUtils
import com.databricks.sdk.service.workspace.{CreateScope, DeleteSecret, PutSecret}
import java.nio.charset.StandardCharsets

import scala.collection.JavaConverters._

class SecretsIntegrationTest extends IntegrationTestBase {
  def inScope(scopeName: String)(f: => Unit): Unit = {
    val createRequest = new CreateScope().setScope(scopeName)
    w.secrets().createScope(createRequest)
    try {
      f
    } finally {
      w.secrets().deleteScope(scopeName)
    }
  }

  "dbutils.secrets.listScopes()" should "list scopes in the workspace" taggedAs Integration in {
    val scopeName = NameUtils.uniqueName("dbutils")
    inScope(scopeName) {
      val scopes = dbutils.secrets.listScopes()
      assert(scopes.map(_.name).contains(scopeName))
    }
  }

  "dbutils.secrets.list()" should "list secrets in a scope" taggedAs Integration in {
    val scopeName = NameUtils.uniqueName("dbutils")
    inScope(scopeName) {
      val secretName = NameUtils.uniqueName("dbutils")
      val secretString = NameUtils.generateRandomBase16String(32)
      w.secrets().putSecret(new PutSecret().setScope(scopeName).setKey(secretName).setStringValue(secretString))
      try {
        val secrets = w.secrets().listSecrets(scopeName).asScala.toSeq
        assert(secrets.map(_.getKey).contains(secretName))
      } finally {
        val deleteRequest = new DeleteSecret().setScope(scopeName).setKey(secretName)
        w.secrets().deleteSecret(deleteRequest)
      }
    }
  }

  "dbutils.secrets.get()" should "get a secret in a scope" taggedAs (Integration, SecretsGet) in {
    val scopeName = NameUtils.uniqueName("dbutils")
    inScope(scopeName) {
      val secretName = NameUtils.uniqueName("dbutils")
      val secretString = NameUtils.generateRandomBase16String(32)
      w.secrets().putSecret(new PutSecret().setScope(scopeName).setKey(secretName).setStringValue(secretString))
      try {
        val secret = w.secrets().get(scopeName, secretName)
        assert(secret === secretString)
      } finally {
        val deleteRequest = new DeleteSecret().setScope(scopeName).setKey(secretName)
        w.secrets().deleteSecret(deleteRequest)
      }
    }
  }

  "dbutils.secrets.getBytes()" should "get a secret in a scope" taggedAs (Integration, SecretsGet) in {
    val scopeName = NameUtils.uniqueName("dbutils")
    inScope(scopeName) {
      val secretName = NameUtils.uniqueName("dbutils")
      val secretBytes = NameUtils.generateRandomBase16String(32).getBytes(StandardCharsets.UTF_8)
      val encodedBytes = java.util.Base64.getEncoder.encodeToString(secretBytes)
      w.secrets().putSecret(new PutSecret().setScope(scopeName).setKey(secretName).setBytesValue(encodedBytes))
      try {
        val secret = w.secrets().getBytes(scopeName, secretName)
        assert(secret === secretBytes)
      } finally {
        val deleteRequest = new DeleteSecret().setScope(scopeName).setKey(secretName)
        w.secrets().deleteSecret(deleteRequest)
      }
    }
  }
}
