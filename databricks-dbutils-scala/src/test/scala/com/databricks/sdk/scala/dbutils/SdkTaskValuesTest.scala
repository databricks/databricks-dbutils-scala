package com.databricks.sdk.scala.dbutils

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.{be, convertToAnyShouldWrapper}

class SdkTaskValuesTest extends AnyFlatSpec {
  private val taskValues = new SdkTaskValues

  "When outside of DBR, TaskValuesUtils.getContext and TaskValuesUtils.setContext" should "throw error" in {
    val context = CommandContext(
      rootRunId = None,
      currentRunId = None,
      jobGroup = None,
      tags = Map.empty,
      extraContext = Map("test" -> "test"))

    val setE = intercept[NotImplementedError] {
      taskValues.setContext(context)
    }

    setE.getMessage should be("setContext is not supported outside of DBR.")

    val getE = intercept[NotImplementedError] {
      taskValues.getContext()
    }

    getE.getMessage should be("getContext is not supported outside of DBR.")
  }

  "When outside of DBR, TaskValuesUtils.get and TaskValues.set" should "throw error" in {
    val setE = intercept[NotImplementedError] {
      taskValues.set("test", "test")
    }

    setE.getMessage should be("set is not supported in Scala.")

    val getE = intercept[NotImplementedError] {
      taskValues.get("test", "test", None, None)
    }

    getE.getMessage should be("get is not supported in Scala.")
  }

  "When outside of DBR, TaskValuesUtils.setJson" should "not throw error" in {
    taskValues.setJson("test", "test")
  }

  "When outside of DBR, TaskValuesUtils.getJson" should "return empty list" in {
    taskValues.getJson("test", "test") should be(Seq.empty)
  }
}
