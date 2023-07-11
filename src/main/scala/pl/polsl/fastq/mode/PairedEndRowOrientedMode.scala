package pl.polsl.fastq.mode

import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkConf, SparkContext}
import pl.polsl.fastq.data.FastqRecord
import pl.polsl.fastq.trimmer.TrimmerFactory.createTrimmers
import pl.polsl.fastq.utils.{PairValidator, PhredDetector}

class PairedEndRowOrientedMode extends Mode {
  private val PHRED_SAMPLE_SIZE = 100

  override def run(argsMap: Map[String, Any]): Unit = {
    val outputs = createOutputFileNames(argsMap("output").asInstanceOf[String])
    val conf = new SparkConf()
    conf.setAppName("FastqTrimmerPERO")
    if (argsMap.contains("master")) {
      conf.setMaster(argsMap("master").asInstanceOf[String])
    }
    val sc = new SparkContext(conf)
    sc.setLogLevel("INFO")
    val trimmers = createTrimmers(sc, argsMap("trimmers").asInstanceOf[List[String]])

    val validatedPairs = argsMap.getOrElse("validate_pairs", false)
      .asInstanceOf[Boolean]

    val input = sc.textFile(argsMap("input").asInstanceOf[String])
      .map(_.split("\\|"))

    val sample = input
      .take(PHRED_SAMPLE_SIZE)
      .map(row => FastqRecord(row(0), row(1), row(3)))
    val phredOffset = argsMap.getOrElse("phredOffset", PhredDetector(sample))
      .asInstanceOf[Int]

    val trimmed = input
      .map { row =>
        val rec1 = FastqRecord(row(0), row(1), row(3), phredOffset)
        val rec2 = FastqRecord(row(4), row(5), row(7), phredOffset)
        (rec1, rec2)
      }
      .map(t => {
        var recs = t
        if (validatedPairs) PairValidator.validatePair(recs._1.name, recs._2.name)
        for (trimmer <- trimmers) {
          recs = trimmer.processPair(recs)
        }
        recs
      })
      .filter {
        case (null, null) => false
        case _ => true
      }
      .persist(StorageLevel.MEMORY_AND_DISK)

    trimmed.filter {
      case (_: FastqRecord, null) => true
      case _ => false
    }
      .map(_._1)
      .saveAsTextFile(outputs(0))

    trimmed.filter {
      case (null, _: FastqRecord) => true
      case _ => false
    }
      .map(_._2)
      .saveAsTextFile(outputs(1))

    val paired = trimmed.filter {
      case (_: FastqRecord, _: FastqRecord) => true
      case _ => false
    }
      .persist(StorageLevel.MEMORY_AND_DISK)

    paired
      .map(f => f._1)
      .saveAsTextFile(outputs(2))

    paired
      .map(f => f._2)
      .saveAsTextFile(outputs(3))
  }

  private def createOutputFileNames(outputDir: String): Array[String] = {
    val unpairedOutput1 = s"$outputDir/unpaired_out_1"
    val unpairedOutput2 = s"$outputDir/unpaired_out_2"
    val pairedOutput1 = s"$outputDir/paired_out_1"
    val pairedOutput2 = s"$outputDir/paired_out_2"
    Array(unpairedOutput1, unpairedOutput2, pairedOutput1, pairedOutput2)
  }
}
