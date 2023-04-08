package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.scalatest.flatspec.AnyFlatSpec
import pl.polsl.fastq.data.FastqRecord

class ToPhred33TrimmerTest extends AnyFlatSpec {
  behavior of "ToPhred64Trimmer"

  private val session = SparkSession
    .builder
    .appName("FastqTrimmerTest")
    .master("local[*]")
    .getOrCreate()
  private val sc = session.sparkContext

  it should "convert quals to phred33" in {
    val trimmer = new ToPhred33Trimmer()
    val rdd: RDD[FastqRecord] = sc.parallelize(List(FastqRecord("READ", "ATCGA", "+", "@KVaj", 64)))

    val result = trimmer(rdd)

    assert(result.first() === FastqRecord("READ", "ATCGA", "+", "!,7BK", 33))
  }

  it should "ignore convertion" in {
    val trimmer = new ToPhred33Trimmer()
    val rdd: RDD[FastqRecord] = sc.parallelize(List(FastqRecord("READ", "ATCGA", "+", "!,7BK", 33)))

    val result = trimmer(rdd)

    assert(result.first() === FastqRecord("READ", "ATCGA", "+", "!,7BK", 33))
  }
}
