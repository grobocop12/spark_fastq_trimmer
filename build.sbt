ThisBuild / version := "0.0.1-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "spark-fastq-trimmer",
    libraryDependencies += "org.apache.spark" %% "spark-core" % "3.3.1",
    libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.3.1",
    libraryDependencies += "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.3.1",
    libraryDependencies += "org.postgresql" % "postgresql" % "42.5.2"
  )
