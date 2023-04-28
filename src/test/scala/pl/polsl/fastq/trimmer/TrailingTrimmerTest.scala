package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.scalatest.flatspec.AnyFlatSpec
import pl.polsl.fastq.data.FastqRecord

class TrailingTrimmerTest extends AnyFlatSpec {
  behavior of "TrailingTrimmer"

  private val session = SparkSession
    .builder
    .appName("FastqTrimmerTest")
    .master("local[*]")
    .getOrCreate()
  private val sc = session.sparkContext

  it should "trim last two leading quals" in {
    val trimmer = new TrailingTrimmer(6)
    val rdd: RDD[FastqRecord] = sc.parallelize(List(FastqRecord("READ", "ATCG", "+", "((!!", 33)))

    val result = trimmer(rdd)

    assert(result.first() === FastqRecord("READ", "AT", "+", "((", 33))
  }

  it should "not trim any quals" in {
    val trimmer = new TrailingTrimmer(0)
    val rdd: RDD[FastqRecord] = sc.parallelize(List(FastqRecord("READ", "ATCG", "+", "((!!", 33)))

    val result = trimmer(rdd)

    assert(result.first() === FastqRecord("READ", "ATCG", "+", "((!!", 33))
  }

  it should "trim entire record" in {
    val trimmer = new TrailingTrimmer(8)
    val rdd: RDD[FastqRecord] = sc.parallelize(List(FastqRecord("READ", "ATCG", "+", "((!!", 33)))

    val result = trimmer(rdd)

    assert(result.isEmpty())
  }
}
