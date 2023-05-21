package pl.polsl.fastq.mode

import org.apache.spark.mllib.rdd.RDDFunctions.fromRDD
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import pl.polsl.fastq.data.FastqRecord
import pl.polsl.fastq.trimmer.Trimmer
import pl.polsl.fastq.trimmer.TrimmerFactory.createTrimmers
import pl.polsl.fastq.utils.PhredDetector

import java.io._
import java.nio.file.{FileSystems, Files}
import scala.annotation.tailrec
import scala.jdk.CollectionConverters.IteratorHasAsScala
import scala.reflect.io.Directory
import scala.util.matching.Regex

class SingleEndMode extends Mode {
  private val PHRED_SAMPLE_SIZE = 100

  override def run(argsMap: Map[String, Any]): Unit = {
    val output = argsMap("output").asInstanceOf[String]
    val tempDirectory = s"$output-temp"
    val trimmers = createTrimmers(argsMap("trimmers").asInstanceOf[List[String]])
    val session = SparkSession
      .builder
      .appName(argsMap.getOrElse("appName", "FastTrimmerSE").asInstanceOf[String])
      .master(argsMap.getOrElse("master", "local[*]").asInstanceOf[String])
      .getOrCreate()
    val sc = session.sparkContext
    sc.setLogLevel("INFO")

    val fastqLines = sc.textFile(argsMap("input").asInstanceOf[String])
      .sliding(4, 4)

    val sample = fastqLines
      .take(PHRED_SAMPLE_SIZE)
      .map(x => FastqRecord(x(0), x(1), x(2), x(3)))

    val phredOffset: Int = argsMap.getOrElse("phredOffset", PhredDetector(sample))
      .asInstanceOf[Int]

    val records = fastqLines.map(x => FastqRecord(x(0), x(1), x(2), x(3), phredOffset))

    applyTrimmer(records, trimmers)
      .saveAsTextFile(tempDirectory)
    concatenateFiles(tempDirectory, output)
    new Directory(new File(tempDirectory)).deleteRecursively()
    session.close
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


  private def concatenateFiles(tempOutput: String, output: String): Unit = {
    val pattern: Regex = "part-\\d+".r
    val dir = FileSystems.getDefault.getPath(tempOutput)
    val sw = new BufferedWriter(new FileWriter(output))
    Files.list(dir)
      .iterator()
      .asScala
      .filter(p => pattern.matches(p.getFileName.toString))
      .foreach(p => {
        val reader = new BufferedReader(new FileReader(p.toString))
        var line: String = reader.readLine()
        while (line != null) {
          sw.write(line)
          sw.newLine()
          line = reader.readLine()
        }
        reader.close()
      })
    sw.close()
  }
}
