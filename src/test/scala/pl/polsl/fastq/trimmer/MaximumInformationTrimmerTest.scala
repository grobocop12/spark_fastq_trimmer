package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.scalatest.flatspec.AnyFlatSpec
import pl.polsl.fastq.data.FastqRecord

class MaximumInformationTrimmerTest extends AnyFlatSpec {
  behavior of "MaximumInformationTrimmer"

  private val session = SparkSession
    .builder
    .appName("FastqTrimmerTest")
    .master("local[*]")
    .getOrCreate()
  private val sc = session.sparkContext

  it should "trim sequence after quality drop" in {
    val trimmer = new MaximumInformationTrimmer(100, 2)
    val rdd: RDD[FastqRecord] = sc.parallelize(List(FastqRecord("@DRR000001.2 3060N:7:1:1114:186/1",
      "GATATTGGCCTGCAGAAGTTCTTCCTGAAAGATGAT",
      "+",
      "!!!!!!CCCCCCCCCCCCCCCCCCCCCCCCK!!!!!",
      33)))
    val result = trimmer(rdd)

    assert(result.first() === FastqRecord(
      "@DRR000001.2 3060N:7:1:1114:186/1",
      "GATATTGGCCTGCAGAAGTTCTTCCTGAAAG",
      "+",
      "!!!!!!CCCCCCCCCCCCCCCCCCCCCCCCK",
      33))
  }
}
