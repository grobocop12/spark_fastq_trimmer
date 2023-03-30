package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.scalatest.flatspec.AnyFlatSpec
import pl.polsl.fastq.data.FastqRecord

class CropTrimmerTest extends AnyFlatSpec {
  behavior of "CropTrimmer"

  private val session = SparkSession
    .builder
    .appName("FastqTrimmerTest")
    .master("local[*]")
    .getOrCreate()
  private val sc = session.sparkContext

  it should "trim sequenc to 4 quals" in {
    val trimmer = new CropTrimmer(4)
    val rdd: RDD[FastqRecord] = sc.parallelize(List(FastqRecord("READ", "ATCGATCGATCG", "+", "!!((!!((!!((")))

    val result = trimmer(rdd)

    assert(result.first() === FastqRecord("READ", "ATCG", "+", "!!(("))
  }

  it should "not trim sequence" in {
    val trimmer = new CropTrimmer(12)
    val rdd: RDD[FastqRecord] = sc.parallelize(List(FastqRecord("READ", "ATCGATCGATCG", "+", "!!((!!((!!((")))

    val result = trimmer(rdd)

    assert(result.first() === FastqRecord("READ", "ATCGATCGATCG", "+", "!!((!!((!!(("))
  }

  it should "not trim sequence shorter than threshold" in {
    val trimmer = new CropTrimmer(12)
    val rdd: RDD[FastqRecord] = sc.parallelize(List(FastqRecord("READ", "ATCGATCGAT", "+", "!!((!!((!!")))

    val result = trimmer(rdd)

    assert(result.first() === FastqRecord("READ", "ATCGATCGAT", "+", "!!((!!((!!"))
  }
}
