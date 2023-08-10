
import mill._, scalalib._

object dbutils extends ScalaModule {
  def scalaVersion = "2.12.10"
  def ivyDeps = Agg(
//    ivy"com.databricks:databricks-sdk-java:0.4.0",
    ivy"org.apache.hadoop:hadoop-client-api:3.3.4",
    ivy"com.google.code.findbugs:jsr305:2.0.1",

    // Extra deps needed for the short term
    ivy"org.slf4j:slf4j-api:2.0.7",
    ivy"com.fasterxml.jackson.core:jackson-databind:2.15.2",
    ivy"org.ini4j:ini4j:0.5.4",
    ivy"org.apache.httpcomponents:httpclient:4.5.14",
    ivy"commons-io:commons-io:2.13.0",
  )

  // Remove this when publishing
  def unmanagedClasspath = T {
    os.copy(os.home / ".m2" / "repository"/"com"/"databricks"/"databricks-sdk-java"/"0.4.0"/"databricks-sdk-java-0.4.0.jar", T.dest / "databricks-sdk-java-0.4.0.jar")
    Agg(PathRef(T.dest / "databricks-sdk-java-0.4.0.jar"))
  }

  object test extends ScalaTests {
    def ivyDeps = Agg(
      ivy"com.lihaoyi::utest:0.7.11",
    )

    // remove this when publishing
    def unmanagedClasspath = T {
      os.copy(os.home / ".m2" / "repository" / "com" / "databricks" / "databricks-sdk-java" / "0.4.0" / "databricks-sdk-java-0.4.0.jar", T.dest / "databricks-sdk-java-0.4.0.jar")
      Agg(PathRef(T.dest / "databricks-sdk-java-0.4.0.jar"))
    }

    def testFramework = "utest.runner.Framework"
  }

  object testDbr extends ScalaTests {
    def ivyDeps = Agg(
      ivy"com.lihaoyi::utest:0.7.11",
    )
    def sources = T.sources(os.pwd / "dbutils" / "test-dbr" / "src")

    // remove this when publishing
    def unmanagedClasspath = T {
      os.copy(os.home / ".m2" / "repository" / "com" / "databricks" / "databricks-sdk-java" / "0.4.0" / "databricks-sdk-java-0.4.0.jar", T.dest / "databricks-sdk-java-0.4.0.jar")
      Agg(PathRef(T.dest / "databricks-sdk-java-0.4.0.jar"))
    }

    def testFramework = "utest.runner.Framework"
  }

  object testSdk extends ScalaTests {
    def ivyDeps = Agg(
      ivy"com.lihaoyi::utest:0.7.11",
    )

    def sources = T.sources(os.pwd / "dbutils" / "test-sdk" / "src")

    // remove this when publishing
    def unmanagedClasspath = T {
      os.copy(os.home / ".m2" / "repository" / "com" / "databricks" / "databricks-sdk-java" / "0.4.0" / "databricks-sdk-java-0.4.0.jar", T.dest / "databricks-sdk-java-0.4.0.jar")
      Agg(PathRef(T.dest / "databricks-sdk-java-0.4.0.jar"))
    }

    def testFramework = "utest.runner.Framework"
  }
}
