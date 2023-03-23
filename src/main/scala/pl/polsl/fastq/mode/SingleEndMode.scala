package pl.polsl.fastq.mode

import org.apache.spark.SparkContext
import org.apache.spark.mllib.rdd.RDDFunctions.fromRDD
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import pl.polsl.fastq.data.FastqRecord
import pl.polsl.fastq.trimmer.TrimmerFactory.createTrimmers

class SingleEndMode extends Mode {
  override def run(argsMap: Map[String, Any]): Unit = {
    val trimmers = createTrimmers(argsMap("trimmers").asInstanceOf[List[String]])
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



    val first = records.first()

    records.saveAsTextFile(argsMap("output").asInstanceOf[String])

    session.close
  }

  private def applyTrimmer(): RDD[FastqRecord] = ???
}
