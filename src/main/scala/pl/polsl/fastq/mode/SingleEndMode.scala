package pl.polsl.fastq.mode

import org.apache.spark.mllib.rdd.RDDFunctions.fromRDD
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import pl.polsl.fastq.data.FastqRecord
import pl.polsl.fastq.trimmer.Trimmer
import pl.polsl.fastq.trimmer.TrimmerFactory.createTrimmers
import pl.polsl.fastq.utils.PhredDetector

import scala.annotation.tailrec

class SingleEndMode extends Mode {
  private val PHRED_SAMPLE_SIZE = 100

  override def run(argsMap: Map[String, Any]): Unit = {
    val input = argsMap("input").asInstanceOf[String]
    val output = argsMap("output").asInstanceOf[String]
    val conf = new SparkConf()
    conf.setAppName("FastqTrimmerSE")
    if (argsMap.contains("master")) {
      conf.setMaster(argsMap("master").asInstanceOf[String])
    }

    val sc = new SparkContext(conf)
    sc.setLogLevel("INFO")

    val trimmers = createTrimmers(sc, argsMap("trimmers").asInstanceOf[List[String]])

    val fastqLines = sc.textFile(input)
      .sliding(4, 4)

    val sample = fastqLines
      .take(PHRED_SAMPLE_SIZE)
      .map(x => FastqRecord(x(0), x(1), x(3)))

    val phredOffset: Int = argsMap.getOrElse("phredOffset", PhredDetector(sample))
      .asInstanceOf[Int]

    fastqLines
      .map(x => FastqRecord(x(0), x(1), x(3), phredOffset))
      .map(r => {
        var rec = r
        for (trimmer <- trimmers) {
          rec = trimmer.processSingle(rec)
        }
        rec
      })
      .filter(_ != null)
      .saveAsTextFile(output)
  }

  @tailrec
  private def applyTrimmer(records: RDD[FastqRecord], trimmers: List[Trimmer]): RDD[FastqRecord] = {
    if (trimmers.isEmpty)
      records
    else {
      applyTrimmer(records.map(trimmers.head.processSingle(_))
        .filter(_ != null),
        trimmers.tail)
    }
  }
}
