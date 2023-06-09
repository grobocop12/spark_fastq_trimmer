package pl.polsl.fastq.mode

import org.apache.spark.mllib.rdd.RDDFunctions.fromRDD
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import pl.polsl.fastq.data.FastqRecord
import pl.polsl.fastq.trimmer.Trimmer
import pl.polsl.fastq.trimmer.TrimmerFactory.createTrimmers
import pl.polsl.fastq.utils.{PairValidator, PhredDetector}

import java.io.File
import scala.annotation.tailrec
import scala.reflect.io.Directory

class PairedEndMode extends TrimmingMode {
  private val PHRED_SAMPLE_SIZE = 100

  override def run(argsMap: Map[String, Any]): Unit = {
    val outputs = createOutputFileNames(argsMap("output").asInstanceOf[String])
    val session = SparkSession
      .builder
      .appName(argsMap.getOrElse("appName", "FastTrimmerSE").asInstanceOf[String])
      .master(argsMap.getOrElse("master", "local[*]").asInstanceOf[String])
      .getOrCreate()
    val sc = session.sparkContext
    sc.setLogLevel("INFO")
    val trimmers = createTrimmers(sc, argsMap("trimmers").asInstanceOf[List[String]])

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
      .cache()
    trimmed.filter {
      case (_: FastqRecord, null) => true
      case _ => false
    }.saveAsTextFile(getTemporaryDirPath(outputs(0)))
    trimmed.filter {
      case (null, _: FastqRecord) => true
      case _ => false
    }.saveAsTextFile(getTemporaryDirPath(outputs(1)))
    val paired = trimmed.filter {
      case (_: FastqRecord, _: FastqRecord) => true
      case _ => false
    }.cache()
    paired.map(f => f._1).saveAsTextFile(getTemporaryDirPath(outputs(2)))
    paired.map(f => f._2).saveAsTextFile(getTemporaryDirPath(outputs(3)))

    outputs.foreach(o => concatenateFiles(getTemporaryDirPath(o), o))
    outputs.foreach(o => new Directory(new File(getTemporaryDirPath(o))).deleteRecursively())

    session.close
  }

  private def createOutputFileNames(outputDir: String): Array[String] = {
    val unpairedOutput1 = s"$outputDir/unpaired_out_1.fastq"
    val unpairedOutput2 = s"$outputDir/unpaired_out_2.fastq"
    val pairedOutput1 = s"$outputDir/paired_out_1.fastq"
    val pairedOutput2 = s"$outputDir/paired_out_2.fastq"
    Array(unpairedOutput1, unpairedOutput2, pairedOutput1, pairedOutput2)
  }

  private def getTemporaryDirPath(path: String): String = s"$path-temp"

  @tailrec
  private def applyTrimmer(records: RDD[(FastqRecord, FastqRecord)],
                           trimmers: List[Trimmer]): RDD[(FastqRecord, FastqRecord)] = {
    if (trimmers.isEmpty)
      records
    else {
      applyTrimmer(records.map(trimmers.head.processPair(_))
        .filter {
          case (null, null) => false
          case _ => true
        }, trimmers.tail)
    }
  }
}
