import mill._, scalalib._, publish._

trait MyModule extends PublishModule {
  def publishVersion = "0.0.1"

  def pomSettings = PomSettings(
    description =
      "DBUtils for Scala simplifies interacting with various components of Databricks, such " +
      "as the Databricks File   System (DBFS), Secret Scopes, Widgets, and other utilities.",
    organization = "com.databricks",
    url = "https://github.com/databricks/databricks-dbutils-scala",
    licenses = Seq(License.MIT),
    versionControl = VersionControl.github("databricks", "databricks-dbutils-scala"),
    developers = Seq(Developer("miles", "Miles Yucht", "https://github.com/mgyucht"))
  )
}

trait MyScalaModule extends MyModule with CrossSbtModule {
  def ivyDeps = Agg(ivy"com.lihaoyi::scalatags:0.12.0")
  object test extends SbtModuleTests {
    def ivyDeps = Agg(
      ivy"org.scalactic::scalactic:3.2.16",
      ivy"org.scalatest::scalatest:3.2.16",
      ivy"org.slf4j:slf4j-reload4j:2.0.7",
      ivy"org.mockito:mockito-core:4.11.0",
    )
    def testFramework = "org.scalatest.tools.Framework"
  }
}

val scalaVersions = Seq("2.13.8", "2.12.18")

object `databricks-dbutils-scala` extends Cross[DbutilsModule](scalaVersions)
trait DbutilsModule extends MyScalaModule {
  def ivyDeps = Agg(
    ivy"com.google.code.findbugs:jsr305:3.0.2",
    ivy"com.databricks:databricks-sdk-java:0.7.0",
  )
}

object examples extends Cross[ExampleModule](scalaVersions)
trait ExampleModule extends MyScalaModule {
  def moduleDeps = Seq(`databricks-dbutils-scala`())
}

object release extends Cross[ReleaseModule](scalaVersions)
trait ReleaseModule extends MyScalaModule {
}


