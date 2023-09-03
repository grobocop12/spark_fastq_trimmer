ThisBuild / version := "0.0.1"

ThisBuild / scalaVersion := "2.12.12"

lazy val root = (project in file("."))
  .settings(
    assembly / assemblyJarName := "spark-fastq-trimmer.jar",
    assembly / mainClass := Some("pl.polsl.fastq.Main"),
    name := "spark-fastq-trimmer",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.15" % "test",
    libraryDependencies += "org.apache.spark" % "spark-core_2.12" % "3.0.1",
    libraryDependencies += "org.apache.spark" % "spark-sql_2.12" % "3.0.1",
    libraryDependencies += "org.apache.spark" % "spark-streaming_2.12" % "3.0.1",
    libraryDependencies += "org.apache.spark" % "spark-mllib_2.12" % "3.0.1",
    libraryDependencies += "org.jmockit" % "jmockit" % "1.34" % "test"
  )

ThisBuild / assemblyMergeStrategy := {
  case PathList("com", xs@_*) => MergeStrategy.first
  case PathList("jakarta", xs@_*) => MergeStrategy.first
  case PathList("javax", xs@_*) => MergeStrategy.first
  case PathList("org", xs@_*) => MergeStrategy.first
  case PathList(ps@_*) if ps.last endsWith ".html" => MergeStrategy.first
  case PathList(ps@_*) if ps.last endsWith "git.properties" => MergeStrategy.first
  case PathList(ps@_*) if ps.last endsWith "module-info.class" => MergeStrategy.first
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}
