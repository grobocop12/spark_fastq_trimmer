package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.scalatest.flatspec.AnyFlatSpec
import pl.polsl.fastq.data.FastqRecord

class AvgQualTrimmerTest extends AnyFlatSpec {
  behavior of "AvgQualTrimmer"

  private val session = SparkSession
    .builder
    .appName("FastqTrimmerTest")
    .master("local[*]")
    .getOrCreate()
  private val sc = session.sparkContext

  it should "drop sequence" in {
    val trimmer = new AvgQualTrimmer(20)
    val rdd: RDD[FastqRecord] = sc.parallelize(List(FastqRecord("READ",
      "GATATTGGCCTGCAGAAGTTCTTCCTGAAAGATGAT",
      "+",
      "++++++++++**********$KKK************", 33)))

    val result = trimmer(rdd)

    assert(result.isEmpty())
  }

  it should "keep sequence" in {
    val trimmer = new AvgQualTrimmer(20)
    val rdd: RDD[FastqRecord] = sc.parallelize(List(FastqRecord("READ",
      "GATATTGGCCTGCAGAAGTTCTTCCTGAAAGATGAT",
      "+",
      "!!!!!!CCCCCCCCCCCCCCCCCCCCCCCCK!!!!!", 33)))

    val result = trimmer(rdd)

    assert(!result.isEmpty())
  }
}
