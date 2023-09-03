ThisBuild / version := "0.0.1-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.12"

packageBin / mainClass := Some("pl.polsl.fastq.Main")

lazy val root = (project in file("."))
  .settings(
    name := "spark-fastq-trimmer",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.15" % "test",
    libraryDependencies += "org.apache.spark" % "spark-core_2.12" % "3.0.1",
    libraryDependencies += "org.apache.spark" % "spark-sql_2.12" % "3.0.1",
    libraryDependencies += "org.apache.spark" % "spark-streaming_2.12" % "3.0.1",
    libraryDependencies += "org.apache.spark" % "spark-mllib_2.12" % "3.0.1",
    libraryDependencies += "org.jmockit" % "jmockit" % "1.34" % "test"
  )