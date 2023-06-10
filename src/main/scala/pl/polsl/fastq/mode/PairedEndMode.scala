package pl.polsl.fastq.mode

import org.apache.spark.mllib.rdd.RDDFunctions.fromRDD
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{SparkSession, functions}
import org.apache.spark.sql.functions.{col, concat, element_at, lit, split}
import pl.polsl.fastq.data.{DatasetFastq, FastqRecord}
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

    //    session.read.textFile(argsMap("input_1").asInstanceOf[String], argsMap("input_2").asInstanceOf[String])
    import session.implicits._
    val i1 = session.createDataset(input1.map {
      case Array(id, seq, _, qual) => (id, seq, qual)
    }).toDF("name_1", "sequence_1", "quality_1")
    val i2 = session.createDataset(input2.map {
      case Array(id, seq, _, qual) => (id, seq, qual)
    }).toDF("name_2", "sequence_2", "quality_2")
    val id1 = i1.withColumn("id", element_at(split(col("name_1"), " "), 1))
    val id2 = i2.withColumn("id", element_at(split(col("name_2"), " "), 1))
    val ds = id1.join(id2, Seq("id"))
    val beggingCount = ds.count()


    val sample = input1
      .take(PHRED_SAMPLE_SIZE)
      .map(x => FastqRecord(x(0), x(1), x(3)))
    val phredOffset: Int = argsMap.getOrElse("phredOffset", PhredDetector(sample))
      .asInstanceOf[Int]
    val result = ds.where(col("name_1").isNotNull or col("name_2").isNotNull)
    val survivingCount = result.count()

    val paired = result
      .where(col("name_1").isNotNull and col("name_2").isNotNull)
      .sort(col("id"))
      .cache()

    val leftPaired = paired.select(concat($"name_1", lit("\n"), $"sequence_1", lit("\n+\n"), $"quality_1"))
    val rightPaired = paired.select(concat($"name_2", lit("\n"), $"sequence_2", lit("\n+\n"), $"quality_2"))
    leftPaired
      .write
      .format("text")
      .text("left_paired.fastq")

    rightPaired
      .write
      .format("text")
      .text("right_paired.fastq")

    val unpaired = ds.where(col("name_1").isNull or col("name_2").isNull)
    unpaired
      .where(col("name_1").isNotNull)
      .select(concat($"name_1", lit("\n"), $"sequence_1", lit("\n+\n"), $"quality_1"))
      .write
      .format("text")
      .text("left_unpaired.fastq")
    unpaired
      .where(col("name_2").isNotNull)
      .select(concat($"name_2", lit("\n"), $"sequence_2", lit("\n+\n"), $"quality_2"))
      .write
      .format("text")
      .text("right_unpaired.fastq")
    val percentageSurviving: Double = survivingCount / beggingCount * 100
    println("Survived percentage: " + percentageSurviving)
    //
    //
    //    val records1 = input1.map(x => FastqRecord(x(0), x(1), x(3), phredOffset))
    //    val records2 = input2.map(x => FastqRecord(x(0), x(1), x(3), phredOffset))
    //    val zipped = records1.repartition(records2.getNumPartitions).zip(records2)
    //    PairValidator.validatePairs(zipped)
    //
    //    val trimmed = applyTrimmer(zipped, trimmers)
    //      .cache()
    //    trimmed.filter {
    //      case (_: FastqRecord, null) => true
    //      case _ => false
    //    }.saveAsTextFile(getTemporaryDirPath(outputs(0)))
    //    trimmed.filter {
    //      case (null, _: FastqRecord) => true
    //      case _ => false
    //    }.saveAsTextFile(getTemporaryDirPath(outputs(1)))
    //    val paired = trimmed.filter {
    //      case (_: FastqRecord, _: FastqRecord) => true
    //      case _ => false
    //    }.cache()
    //    paired.map(f => f._1).saveAsTextFile(getTemporaryDirPath(outputs(2)))
    //    paired.map(f => f._2).saveAsTextFile(getTemporaryDirPath(outputs(3)))

    //    outputs.foreach(o => concatenateFiles(getTemporaryDirPath(o), o))
    //    outputs.foreach(o => new Directory(new File(getTemporaryDirPath(o))).deleteRecursively())

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
