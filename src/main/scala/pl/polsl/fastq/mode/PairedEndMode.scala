package pl.polsl.fastq.mode

import org.apache.spark.mllib.rdd.RDDFunctions.fromRDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.storage.StorageLevel
import pl.polsl.fastq.data.FastqRecord
import pl.polsl.fastq.trimmer.TrimmerFactory.createTrimmers
import pl.polsl.fastq.utils.{PairValidator, PhredDetector}

class PairedEndMode extends TrimmingMode {
  private val PHRED_SAMPLE_SIZE = 100

  override def run(argsMap: Map[String, Any]): Unit = {
    val outputs = createOutputFileNames(argsMap("output").asInstanceOf[String])
    val session = SparkSession
      .builder
      .appName(argsMap.getOrElse("appName", "FastqTrimmerPE").asInstanceOf[String])
      .getOrCreate()
    val sc = session.sparkContext
    sc.setLogLevel("INFO")
    val trimmers = createTrimmers(sc, argsMap("trimmers").asInstanceOf[List[String]])

    val input1 = sc.textFile(argsMap("input_1").asInstanceOf[String])
      .sliding(4, 4)
      .zipWithIndex()
    val input2 = sc.textFile(argsMap("input_2").asInstanceOf[String], input1.getNumPartitions)
      .sliding(4, 4)
      .zipWithIndex()
    val sample = input1
      .take(PHRED_SAMPLE_SIZE)
      .map(x => FastqRecord(x._1(0), x._1(1), x._1(3)))
    val phredOffset = argsMap.getOrElse("phredOffset", PhredDetector(sample))
      .asInstanceOf[Int]
    val records1 = input1.map(x => (x._2, FastqRecord(x._1(0), x._1(1), x._1(3), phredOffset)))
    val records2 = input2.map(x => (x._2, FastqRecord(x._1(0), x._1(1), x._1(3), phredOffset)))
    val joined = records1.join(records2)

    val trimmed = joined.map(t => {
      var recs = t._2
      PairValidator.validatePair(recs._1.name, recs._2.name)
      for (trimmer <- trimmers) {
        recs = trimmer.processPair(recs)
      }
      (t._1, recs)
    })
      .filter {
        case (_: Long, (null, null)) => false
        case _ => true
      }
      .sortByKey()
      .map(_._2)
      .persist(StorageLevel.MEMORY_AND_DISK)

    trimmed.filter {
      case (_: FastqRecord, null) => true
      case _ => false
    }
      .map(_._1)
      .coalesce(1)
      .saveAsTextFile(outputs(0))

    trimmed.filter {
      case (null, _: FastqRecord) => true
      case _ => false
    }
      .map(_._2)
      .coalesce(1)
      .saveAsTextFile(outputs(1))

    val paired = trimmed.filter {
      case (_: FastqRecord, _: FastqRecord) => true
      case _ => false
    }
      .persist(StorageLevel.MEMORY_AND_DISK)

    paired
      .map(f => f._1)
      .coalesce(1)
      .saveAsTextFile(outputs(2))

    paired
      .map(f => f._2)
      .coalesce(1)
      .saveAsTextFile(outputs(3))

    session.close
  }

  private def createOutputFileNames(outputDir: String): Array[String] = {
    val unpairedOutput1 = s"$outputDir/unpaired_out_1.fastq"
    val unpairedOutput2 = s"$outputDir/unpaired_out_2.fastq"
    val pairedOutput1 = s"$outputDir/paired_out_1.fastq"
    val pairedOutput2 = s"$outputDir/paired_out_2.fastq"
    Array(unpairedOutput1, unpairedOutput2, pairedOutput1, pairedOutput2)
  }

//  private def getTemporaryDirPath(path: String): String = s"$path-temp"
}