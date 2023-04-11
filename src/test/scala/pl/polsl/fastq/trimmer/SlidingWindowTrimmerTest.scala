package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.scalatest.flatspec.AnyFlatSpec
import pl.polsl.fastq.data.FastqRecord

class SlidingWindowTrimmerTest extends AnyFlatSpec {
  behavior of "SlidingWindowTrimmer"

  private val session = SparkSession
    .builder
    .appName("FastqTrimmerTest")
    .master("local[*]")
    .getOrCreate()
  private val sc = session.sparkContext

  it should "keep first 10 quals" in {
    val trimmer = new SlidingWindowTrimmer(5, 10)
    val rdd: RDD[FastqRecord] = sc.parallelize(List(FastqRecord(
      "READ",
      "GATATTGGCCTGCAGAAGTTCTTCCTGAAAGATGAT",
      "+",
      "++++++++++**************************",
      33)))

    val result = trimmer(rdd)

    assert(result.first() === FastqRecord("READ",
      "GATATTGGCC",
      "+",
      "++++++++++",
      33))
  }

  it should "drop too short record" in {
    val trimmer = new SlidingWindowTrimmer(10, 10)
    val rdd: RDD[FastqRecord] = sc.parallelize(List(FastqRecord(
      "READ",
      "GATATTGG",
      "+",
      "++++++++",
      33)))

    val result = trimmer(rdd)

    assert(result.isEmpty())
    //    val trimmer = new TrailingTrimmer(0, 33)
    //    val rdd: RDD[FastqRecord] = sc.parallelize(List(FastqRecord("READ", "ATCG", "+", "((!!")))
    //
    //    val result = trimmer(rdd)
    //
    //    assert(result.first() === FastqRecord("READ", "ATCG", "+", "((!!"))
  }

  it should "trim entire record" in {
    val trimmer = new SlidingWindowTrimmer(5, 10)
    val rdd: RDD[FastqRecord] = sc.parallelize(List(FastqRecord(
      "READ",
      "GATATTGGCCTGCAGAAGTTCTTCCTGAAAGATGAT",
      "+",
      "!+++++++++**************************",
      33)))

    val result = trimmer(rdd)

    assert(result.isEmpty())
  }
}
