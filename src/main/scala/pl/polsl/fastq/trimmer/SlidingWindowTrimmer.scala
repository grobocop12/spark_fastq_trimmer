package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import pl.polsl.fastq.data.FastqRecord

import scala.annotation.tailrec
import scala.util.control.Breaks.break

class SlidingWindowTrimmer(windowLength: Int, requiredQuality: Float) extends Trimmer {
  private val totalRequiredQuality: Float = windowLength * requiredQuality

  override def apply(in: RDD[FastqRecord]): RDD[FastqRecord] = in.map(trim).filter(_ != null)

  private def trim(in: FastqRecord): FastqRecord = {

    if (in.sequence.length < windowLength) return null
    val qualitySums = in.qualityAsInteger.sliding(windowLength).map(_.sum)
    if (qualitySums.next() < totalRequiredQuality) {
      null
    } else {
      val lengthToKeep = calculateLength(qualitySums, windowLength)
      FastqRecord(in.name, in.sequence.substring(0, lengthToKeep), in.comment, in.quality.substring(0, lengthToKeep), in.phredOffset)
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
