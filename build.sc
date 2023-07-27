
import mill._, scalalib._

object sdk extends RootModule with ScalaModule {
  def scalaVersion = "2.12.10"
  def ivyDeps = Agg(
    ivy"com.databricks::dbutils-api:0.0.6",
    ivy"com.databricks:databricks-sdk-java:0.2.0",
    ivy"org.apache.hadoop:hadoop-client-api:3.3.4",
  )

}
