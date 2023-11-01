package com.databricks.sdk.scala.dbutils

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.{be, convertToAnyShouldWrapper}

class SdkTaskValuesTest extends AnyFlatSpec {
  private val taskValues = new SdkTaskValues
  "When outside of DBR, TaskValuesUtils.getContext" should "return default context initially" in {
    taskValues.getContext should be(
      CommandContext(
        rootRunId = None,
        currentRunId = None,
        jobGroup = None,
        tags = Map.empty,
        extraContext = Map.empty))
  }

  "When outside of DBR, TaskValuesUtils.getContext" should "return context, which was set" in {
    val context = CommandContext(
      rootRunId = None,
      currentRunId = None,
      jobGroup = None,
      tags = Map.empty,
      extraContext = Map("test" -> "test"))

    taskValues.setContext(context)

    taskValues.getContext should be(context)
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
