
import mill._, scalalib._

object dbutils extends ScalaModule {
  def scalaVersion = "2.12.10"
  def ivyDeps = Agg(
    ivy"com.databricks:databricks-sdk-java:0.4.0",
    ivy"org.apache.hadoop:hadoop-client-api:3.3.4",
    ivy"com.google.code.findbugs:jsr305:2.0.1",
  )
}

object dbutilsE2ERunner extends ScalaModule {
  override def sources: T[Seq[PathRef]] = T.sources(os.pwd / "dbutils-e2e-runner")
  def scalaVersion = "2.12.10"
  def ivyDeps = Agg(
    ivy"com.databricks:databricks-sdk-java:0.4.0",
    ivy"com.lihaoyi::os-lib:0.9.1",
  )
}
