package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.scalatest.flatspec.AnyFlatSpec
import pl.polsl.fastq.data.FastqRecord

class TailCropTrimmerTest extends AnyFlatSpec {

  behavior of "TailCropTrimmer"

  private val session = SparkSession
    .builder
    .appName("FastqTrimmerTest")
    .master("local[*]")
    .getOrCreate()
  private val sc = session.sparkContext

  it should "trim last 5 quals" in {
    val trimmer = new TailCropTrimmer(5)
    val rdd: RDD[FastqRecord] = sc.parallelize(List(FastqRecord("READ", "ATCGATCGATCG", "+", "!!((!!((!!((")))

    val result = trimmer(rdd)

    assert(result.first() === FastqRecord("READ", "ATCGATC", "+", "!!((!!("))
  }

  it should "return empty RDD" in {
    val trimmer = new TailCropTrimmer(5)
    val rdd: RDD[FastqRecord] = sc.parallelize(List(FastqRecord("READ", "ATCGA", "+", "!!((!")))

    val result = trimmer(rdd)

    assert(result.isEmpty())
  }
}
