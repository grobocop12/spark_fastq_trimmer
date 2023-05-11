package pl.polsl.fastq.mode

import org.apache.log4j.LogManager
import org.apache.spark.mllib.rdd.RDDFunctions.fromRDD
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import pl.polsl.fastq.data.FastqRecord
import pl.polsl.fastq.trimmer.Trimmer
import pl.polsl.fastq.trimmer.TrimmerFactory.createTrimmers
import pl.polsl.fastq.utils.{PairValidator, PhredDetector}

import scala.annotation.tailrec

class PairedEndMode extends Mode {
  private val PHRED_SAMPLE_SIZE = 100

  override def run(argsMap: Map[String, Any]): Unit = {
    val output = argsMap("output").asInstanceOf[String]
    val unpairedOutput1 = s"$output/unpaired_out_1"
    val unpairedOutput2 = s"$output/unpaired_out_2"
    val pairedOutput1 = s"$output/paired_out_1"
    val pairedOutput2 = s"$output/paired_out_2"
    val trimmers = createTrimmers(argsMap("trimmers").asInstanceOf[List[String]])
    val session = SparkSession
      .builder
      .appName(argsMap.getOrElse("appName", "FastTrimmerSE").asInstanceOf[String])
      .master(argsMap.getOrElse("master", "local[*]").asInstanceOf[String])
      .getOrCreate()
    val sc = session.sparkContext
    sc.setLogLevel("INFO")
    val logger = LogManager.getRootLogger

    val input1 = sc.textFile(argsMap("input_1").asInstanceOf[String])
      .sliding(4, 4)
    val input2 = sc.textFile(argsMap("input_2").asInstanceOf[String])
      .sliding(4, 4)


    val sample = input1
      .take(PHRED_SAMPLE_SIZE)
      .map(x => FastqRecord(x(0), x(1), x(2), x(3)))

    val phredOffset: Int = argsMap.getOrElse("phredOffset", PhredDetector(sample))
      .asInstanceOf[Int]

    val records1 = input1.map(x => FastqRecord(x(0), x(1), x(2), x(3), phredOffset))
    val records2 = input2.map(x => FastqRecord(x(0), x(1), x(2), x(3), phredOffset))
    val zipped = records1.zip(records2)
    PairValidator.validatePairs(zipped)

    val trimmed = applyTrimmer(zipped, trimmers)
      .filter {
        case (null, null) => false
        case _ => true
      }
      .cache()
    trimmed.filter {
      case (a: FastqRecord, null) => true
      case _ => false
    }.saveAsTextFile(unpairedOutput1)
    trimmed.filter {
      case (null, b: FastqRecord) => true
      case _ => false
    }.saveAsTextFile(unpairedOutput2)
    val paired = trimmed.filter {
      case (a: FastqRecord, b: FastqRecord) => true
      case _ => false
    }.cache()
    paired.map(f => f._1).saveAsTextFile(pairedOutput1)
    paired.map(f => f._2).saveAsTextFile(pairedOutput2)

    session.close
  }

  @tailrec
  private def applyTrimmer(records: RDD[(FastqRecord, FastqRecord)], trimmers: List[Trimmer]): RDD[(FastqRecord, FastqRecord)] = {
    if (trimmers.isEmpty)
      records
    else {
      applyTrimmer(records.map(trimmers.head.processPair(_)), trimmers.tail)
    }
  }
}
