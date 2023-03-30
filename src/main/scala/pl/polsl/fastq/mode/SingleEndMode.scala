package pl.polsl.fastq.mode

import org.apache.spark.mllib.rdd.RDDFunctions.fromRDD
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import pl.polsl.fastq.data.FastqRecord
import pl.polsl.fastq.trimmer.Trimmer
import pl.polsl.fastq.trimmer.TrimmerFactory.createTrimmers
import pl.polsl.fastq.utils.PhredDetector

import scala.annotation.tailrec

class SingleEndMode extends Mode {
  private val PHRED_SAMPLE_SIZE = 100

  override def run(argsMap: Map[String, Any]): Unit = {
    val session = SparkSession
      .builder
      .appName(argsMap.getOrElse("appName", "FastTrimmerSE").asInstanceOf[String])
      .master(argsMap.getOrElse("master", "local[*]").asInstanceOf[String])
      .getOrCreate()
    val sc = session.sparkContext

    val records = sc.textFile(argsMap("input").asInstanceOf[String])
      .sliding(4, 4)
      .map(x => FastqRecord(x(0), x(1), x(2), x(3)))
      .cache

    val phredOffset: Int = argsMap.getOrElse("phredOffset", PhredDetector(records.takeSample(withReplacement = false,
      PHRED_SAMPLE_SIZE)))
      .asInstanceOf[Int]
    val trimmers = createTrimmers(argsMap("trimmers").asInstanceOf[List[String]], phredOffset)

    val trimmedRecords = applyTrimmer(records, trimmers)

    trimmedRecords.saveAsTextFile(argsMap("output").asInstanceOf[String])

    session.close
  }

  @tailrec
  private def applyTrimmer(records: RDD[FastqRecord], trimmers: List[Trimmer]): RDD[FastqRecord] =
    if (trimmers.isEmpty)
      records
    else
      applyTrimmer(trimmers.head(records), trimmers.tail)
}
