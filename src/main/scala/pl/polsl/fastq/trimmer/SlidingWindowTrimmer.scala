package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import pl.polsl.fastq.data.FastqRecord

import scala.annotation.tailrec

class SlidingWindowTrimmer(windowLength: Int, requiredQuality: Float) extends SingleTrimmer {
  private val totalRequiredQuality: Float = windowLength * requiredQuality

  //  override def apply(in: Array[FastqRecord]): Array[FastqRecord] = in.map(trim).filter(_ != null)

  override protected def processRecord(rec:FastqRecord): FastqRecord = {

    if (rec.sequence.length < windowLength) return null
    val qualitySums = rec.qualityAsInteger().sliding(windowLength).map(_.sum)
    if (qualitySums.next() < totalRequiredQuality) {
      null
    } else {
      val lengthToKeep = calculateLength(qualitySums, windowLength)
      FastqRecord(rec.name,
        rec.sequence.substring(0, lengthToKeep),
        rec.comment,
        rec.quality.substring(0, lengthToKeep),
        rec.phredOffset)
    }
  }

  @tailrec
  private def calculateLength(qualsSums: Iterator[Int], lengthToKeep: Int): Int = {
    if (!qualsSums.hasNext || qualsSums.next() < totalRequiredQuality) {
      return lengthToKeep
    }
    calculateLength(qualsSums, lengthToKeep + 1)
  }
}
