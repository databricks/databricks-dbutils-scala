# Databricks SDK for Scala

**Stability**: [Experimental](https://docs.databricks.com/release-notes/release-types.html)

The Databricks SDK for Scala includes functionality to accelerate development with Java for the Databricks Lakehouse. Currently, the SDK for Scala includes support for the DBUtils interface provided in Databricks Runtime.

The SDK for Scala is implemented mostly using the core of the [SDK for Java](https://github.com/databricks/databricks-sdk-java). Consult that repository's README for information on authentication, logging, and how to make requests directly to the Databricks REST API.

## Contents

- [Getting started](#getting-started)
- [Migrating to SDK for Scala DBUtils](#migrating to-sdk-for-scala-dbutils)
- [Limitations when running outside of Databricks Runtime](#limitations-when-running-outside-of-databricks-runtime)
- [Interface stability](#interface-stability)
- [Disclaimer](#disclaimer)

## Getting started

You can install Databricks SDK for Scala DBUtils by adding the following to your `pom.xml`:

```pom.xml
<dependency>
  <groupId>com.databricks</groupId>
  <artifactId>databricks-sdk-dbutils</artifactId>
  <version>0.0.1</version>
</dependency>
```

Get an instance of DBUtils by calling `DBUtils.getDBUtils()`.

```scala
import com.databricks.sdk.scala.dbutils.DBUtils

object App {
   final def main(args: Array[String]): Unit = {
      DBUtils dbutils = DBUtils.getDBUtils()
      dbutils.fs.head("/Volumes/mycatalog/myschema/myvolume/file.txt")
   }
}
```

This code is now portable and can be run both within Databricks Runtime and in applications outside of Databricks Runtime. When this code is run in Databricks Runtime, the returned DBUtils instance proxies all function calls to the DBUtils instance provided by Databricks Runtime. When this code is run outside of Databricks Runtime, DBUtils uses the REST API to emulate the behavior of DBUtils within Databricks Runtime, providing a consistent interface for users to build applications that can run within and outside of Databricks Runtime.

## Migrating to SDK for Scala DBUtils

### Migrating from a Databricks notebook

In Databricks notebooks, DBUtils is provided as a built-in and is automatically available for users. To make notebook code using DBUtils portable with this library, add the following code in your notebook:

```scala
import com.databricks.sdk.scala.dbutils.DBUtils
val dbutils = DBUtils.getDBUtils()
```

If you have imported any types from DBUtils, change the package of those types to `com.databricks.sdk.scala.dbutils`.

### Migrating from DBConnect version 1

In DBConnect version 1, the DBUtils interface was exposed as `com.databricks.service.DBUtils`. Add the following code to your application:

```scala
import com.databricks.sdk.scala.dbutils.DBUtils
val dbutils = DBUtils.getDBUtils()
```

and replace usages of `DBUtils` with `dbutils`.

Additionally, if you have imported any types from `com.databricks.service`, replace those imports with `com.databricks.sdk.scala.dbutils`.

## Limitations when running outside of Databricks Runtime

The DBUtils interface provides many convenient utilities for interacting with Databricks APIs, notebooks and Databricks Runtime. When run outside of Databricks Runtime, some of these utilities are less useful. The limitations of the version of DBUtils returned by `DBUtils.getDBUtils()` in this case are as follows:

* Only `fs` and `secrets` components of DBUtils are supported. Other fields will throw an exception if accessed.
* Within `fs`, the mounting methods (`mount`, `updateMount`, `refreshMounts`, `mounts`, and `unmount`) are not implemented and will throw an exception if called.
* Within `fs`, the caching methods (`cacheTable`, `cacheFiles`, `uncacheTable`, and `uncacheFiles`) are not implemented and will throw an exception if called.
* `help()` methods are not implemented.

## Interface stability

During the [Experimental](https://docs.databricks.com/release-notes/release-types.html) period, Databricks is actively working on stabilizing the Databricks SDK for Java's interfaces. API clients for all services are generated from specification files that are synchronized from the main platform. You are highly encouraged to pin the exact dependency version and read the [changelog](https://github.com/databricks/databricks-sdk-java/blob/main/CHANGELOG.md) where Databricks documents the changes. Databricks may have minor [documented](https://github.com/databricks/databricks-sdk-java/blob/main/CHANGELOG.md) backward-incompatible changes, such as renaming the methods or some type names to bring more consistency.

## Disclaimer
- The product is in preview and not intended to be used in production;
- The product may change or may never be released;
- While we will not charge separately for this product right now, we may charge for it in the future. You will still incur charges for DBUs.
- There's no formal support or SLAs for the preview - so please reach out to your account or other contact with any questions or feedback; and
- We may terminate the preview or your access at any time.

