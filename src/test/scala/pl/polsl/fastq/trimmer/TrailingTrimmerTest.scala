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
    val trimmer = new TrailingTrimmer(6, 33)
    val rdd: RDD[FastqRecord] = sc.parallelize(List(FastqRecord("READ", "ATCG", "+", "((!!")))

    val result = trimmer(rdd)

    assert(result.first() === FastqRecord("READ", "AT", "+", "(("))
  }

  it should "not trim any quals" in {
    val trimmer = new TrailingTrimmer(0, 33)
    val rdd: RDD[FastqRecord] = sc.parallelize(List(FastqRecord("READ", "ATCG", "+", "((!!")))

    val result = trimmer(rdd)

    assert(result.first() === FastqRecord("READ", "ATCG", "+", "((!!"))
  }

  it should "trim entire record" in {
    val trimmer = new TrailingTrimmer(8, 33)
    val rdd: RDD[FastqRecord] = sc.parallelize(List(FastqRecord("READ", "ATCG", "+", "((!!")))

    val result = trimmer(rdd)

    assert(result.isEmpty())
  }
}
