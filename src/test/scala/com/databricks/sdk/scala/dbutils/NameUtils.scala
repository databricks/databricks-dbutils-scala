package com.databricks.sdk.scala.dbutils

import java.security.SecureRandom

object NameUtils {

  def uniqueName(prefix: String): String = prefix + "-" + generateRandomBase16String(16)

  def generateRandomBase16String(length: Int): String = {
    val bytes = new Array[Byte](length / 2)
    new SecureRandom().nextBytes(bytes)
    val hexBuilder = new StringBuilder(length)
    for (b <- bytes) {
      hexBuilder.append(String.format("%02x", b))
    }
    hexBuilder.toString
  }
}
