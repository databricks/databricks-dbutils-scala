# Databricks Utilities (DBUtils) for Scala

**Stability**: [Experimental](https://docs.databricks.com/release-notes/release-types.html)

The Databricks Utilities for Scala includes functionality to accelerate development with Scala for the Databricks Lakehouse.

The Databricks Utilities for Scala library is implemented mostly using the core of the [SDK for Java](https://github.com/databricks/databricks-sdk-java). Consult that repository's README for information on authentication, logging, and how to make requests directly to the Databricks REST API.

## Contents

- [Getting started](#getting-started)
- [Migrating to DBUtils](#migrating-to-dbutils)
- [Limitations when running outside of Databricks Runtime](#limitations-when-running-outside-of-databricks-runtime)
- [Interface stability](#interface-stability)
- [Contributing](#contributing)
- [Disclaimer](#disclaimer)


## Getting started

You can install Databricks Utilities for Scala by adding the following to your `pom.xml`:

```pom.xml
<dependency>
  <groupId>com.databricks</groupId>
  <artifactId>databricks-sdk-dbutils</artifactId>
  <version>0.1.4</version>
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

## Migrating to DBUtils

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

During the [Experimental](https://docs.databricks.com/release-notes/release-types.html) period, Databricks is actively working on stabilizing the Databricks Utilities for Scala's interfaces. You are highly encouraged to pin the exact dependency version and read the [changelog](https://github.com/databricks/databricks-sdk-java/blob/main/CHANGELOG.md) where Databricks documents the changes. Databricks may have minor [documented](https://github.com/databricks/databricks-sdk-java/blob/main/CHANGELOG.md) backward-incompatible changes, such as renaming the methods or some type names to bring more consistency.

## Contributing
This section contains the information regarding adding a change in the repository.
1. Create a PR with the change.
2. Make sure the changes are unit tested.
3. Make sure the changes have been tested end to end. Please see the section below for end to end manual testing.

### Manually testing the changes end to end
Testing the change end to end is not straight forward since we don't have a dedicated infrastructure for the repository yet. Please look at the steps below for manually testing a change end to end.
1. Build and upload the local jar to Databricks Volumes. This will be used later on to install the library on the cluster.
   1. Make sure the changes are in the local branch you would be building the jar from.
   2. From repository root, run: `$ mvn package`
   3. The jars would be build under the following directory from root: `databricks-dbutils-scala/target`
   4. Upload the jar to test to UC Volumes. This would be `databricks-dbutils-scala_2.12-0.1.4.jar` in most cases.
2. Upload the jar in Volumes
   1. Open the databricks console.
   2. Go to the volumes and click: `Upload to this volume`
   3. Select the jar mentioned above in step 1.4.
3. Add an instance profile if needed. For example in case of interacting with S3.
   1. On Databricks console, go to User Settings -> Security -> Manage
   2. Click on Add instance profile
   3. Add the instance profile you need.
4. Create a cluster with the library:
   1. On Databricks console, go to Compute -> Create Compute
   2. Attach the instance profile (step - 3.3).
   3. Install the library from UC Volumes (step - 2.3)
5. Create a notebook with the code to test the end to end flow
   1.On Databricks console, create a notebook i.e. New -> Notebook
   2. Write the code to test the end to end flow, example: 
   ```scala
      import com.databricks.sdk.scala.dbutils.DBUtils
      DBUtils.getDBUtils().fs.mount("s3a://bucket-name", "/mnt/mount-point")
   ```
   3. Connect the cluster (step - 4.3) to the notebook and run

## Disclaimer
- The product is in preview and not intended to be used in production;
- The product may change or may never be released;
- While we will not charge separately for this product right now, we may charge for it in the future. You will still incur charges for DBUs.
- There's no formal support or SLAs for the preview - so please reach out to your account or other contact with any questions or feedback; and
- We may terminate the preview or your access at any time.

