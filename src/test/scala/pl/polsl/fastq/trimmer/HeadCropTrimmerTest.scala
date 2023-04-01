package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.scalatest.flatspec.AnyFlatSpec
import pl.polsl.fastq.data.FastqRecord

class HeadCropTrimmerTest extends AnyFlatSpec {

  behavior of "HeadCropTrimmer"

  private val session = SparkSession
    .builder
    .appName("FastqTrimmerTest")
    .master("local[*]")
    .getOrCreate()
  private val sc = session.sparkContext

  it should "trim first 5 quals" in {
    val trimmer = new HeadCropTrimmer(5)
    val rdd: RDD[FastqRecord] = sc.parallelize(List(FastqRecord("READ", "ATCGATCGATCG", "+", "!!((!!((!!((")))

    val result = trimmer(rdd)

    assert(result.first() === FastqRecord("READ", "TCGATCG", "+", "!((!!(("))
  }

  it should "return empty RDD" in {
    val trimmer = new HeadCropTrimmer(5)
    val rdd: RDD[FastqRecord] = sc.parallelize(List(FastqRecord("READ", "ATCGA", "+", "!!((!")))

    val result = trimmer(rdd)

    assert(result.isEmpty())
  }
}
