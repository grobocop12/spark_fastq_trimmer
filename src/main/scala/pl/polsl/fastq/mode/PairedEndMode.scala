package pl.polsl.fastq.mode

import org.apache.spark.mllib.rdd.RDDFunctions.fromRDD
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkConf, SparkContext}
import pl.polsl.fastq.data.FastqRecord
import pl.polsl.fastq.trimmer.TrimmerFactory.createTrimmers
import pl.polsl.fastq.utils.{PairValidator, PhredDetector}

class PairedEndMode extends Mode {
  private val PHRED_SAMPLE_SIZE = 100

  override def run(argsMap: Map[String, Any]): Unit = {
    val input1 = argsMap("input_1").asInstanceOf[String]
    val input2 = argsMap("input_2").asInstanceOf[String]
    val outputs = createOutputFileNames(argsMap("output").asInstanceOf[String])
    val partitions = argsMap.getOrElse("partitions", "2").asInstanceOf[String]
      .toInt

    val conf = new SparkConf()
    conf.setAppName("FastqTrimmerPE")
    if (argsMap.contains("master")) {
      conf.setMaster(argsMap("master").asInstanceOf[String])
    }
    val sc = new SparkContext(conf)
    sc.setLogLevel("INFO")
    val trimmers = createTrimmers(sc, argsMap("trimmers").asInstanceOf[List[String]])

    val validatedPairs = argsMap.getOrElse("validate_pairs", false)
      .asInstanceOf[Boolean]

    val lines1 = sc.textFile(input1, partitions)
      .sliding(4, 4)
    val lines2 = sc.textFile(input2, partitions)
      .sliding(4, 4)

    val sample = lines1
      .take(PHRED_SAMPLE_SIZE)
      .map(x => FastqRecord(x(0), x(1), x(3)))
    val phredOffset = argsMap.getOrElse("phredOffset", PhredDetector(sample))
      .asInstanceOf[Int]

    val records1 = lines1.zipWithIndex()
      .map(x => (x._2, FastqRecord(x._1(0), x._1(1), x._1(3), phredOffset)))
    val records2 = lines2.zipWithIndex()
      .map(x => (x._2, FastqRecord(x._1(0), x._1(1), x._1(3), phredOffset)))
    val joined = records1.join(records2)

    val trimmed = joined.map(t => {
      var recs = t._2
      if (validatedPairs) PairValidator.validatePair(recs._1.name, recs._2.name)
      for (trimmer <- trimmers) {
        recs = trimmer.processPair(recs)
      }
      (t._1, recs)
    })
      .filter {
        case (_: Long, (null, null)) => false
        case _ => true
      }
      .persist(StorageLevel.MEMORY_AND_DISK)

    val unpaired = trimmed.filter {
      case (_: Long, (_: FastqRecord, null)) => true
      case (_: Long, (null, _: FastqRecord)) => true
      case _ => false
    }

    unpaired.filter {
      case (_: Long, (_: FastqRecord, null)) => true
      case _ => false
    }
      .sortByKey()
      .map(_._2._1)
      .saveAsTextFile(outputs(0))

    unpaired.filter {
      case (_: Long, (null, _: FastqRecord)) => true
      case _ => false
    }
      .sortByKey()
      .map(_._2._2)
      .saveAsTextFile(outputs(1))

    val paired = trimmed.filter {
      case (_: Long, (_: FastqRecord, _: FastqRecord)) => true
      case _ => false
    }
      .sortByKey()
      .map(_._2)
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