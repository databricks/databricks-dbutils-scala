package com.databricks.sdk.scala.dbutils.integration

import com.databricks.sdk.scala.dbutils.CommandContext
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.matchers.should.Matchers.be

class SdkTaskValuesIntegrationTest extends IntegrationTestBase {
  "When outside of DBR, TaskValuesUtils.getContext" should "return default context initially" taggedAs Integration in {
    if (isInDbr) {
      cancel("This test must only be run outside DBR")
    }

    dbutils.jobs.taskValues.getContext should be(
      CommandContext(
        rootRunId = None,
        currentRunId = None,
        jobGroup = None,
        tags = Map.empty,
        extraContext = Map.empty))
  }

  "When outside of DBR, TaskValuesUtils.getContext" should "return context, which was set" taggedAs Integration in {
    if (isInDbr) {
      cancel("This test must only be run outside DBR")
    }

    val context = CommandContext(
      rootRunId = None,
      currentRunId = None,
      jobGroup = None,
      tags = Map.empty,
      extraContext = Map("test" -> "test"))

    dbutils.jobs.taskValues.setContext(context)

    dbutils.jobs.taskValues.getContext should be(context)
  }

  "When outside of DBR, TaskValuesUtils.get and TaskValues.set" should "throw error" taggedAs Integration in {
    if (isInDbr) {
      cancel("This test must only be run outside DBR")
    }

    val setE = intercept[NotImplementedError] {
      dbutils.jobs.taskValues.set("test", "test")
    }

    setE.getMessage should be("set is not supported in Scala.")

    val getE = intercept[NotImplementedError] {
      dbutils.jobs.taskValues.get("test", "test", None, None)
    }

    getE.getMessage should be("get is not supported in Scala.")
  }

  "When outside of DBR, TaskValuesUtils.setJson" should "throw error" taggedAs Integration in {
    if (isInDbr) {
      cancel("This test must only be run outside DBR")
    }

    val e = intercept[NotImplementedError] {
      dbutils.jobs.taskValues.setJson("test", "test")
    }

    e.getMessage should be("setJson is not supported for local development in Scala.")
  }

  "When outside of DBR, TaskValuesUtils.getJson" should "return empty list" taggedAs Integration in {
    if (isInDbr) {
      cancel("This test must only be run outside DBR")
    }

    dbutils.jobs.taskValues.getJson("test", "test") should be(Seq.empty)
  }
}
