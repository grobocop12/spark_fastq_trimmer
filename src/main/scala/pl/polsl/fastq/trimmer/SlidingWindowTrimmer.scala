package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import pl.polsl.fastq.data.FastqRecord

import scala.annotation.tailrec
import scala.util.control.Breaks.break

class SlidingWindowTrimmer(windowLength: Int, requiredQuality: Float) extends Trimmer {
  val totalRequiredQuality: Float = windowLength * requiredQuality

  override def apply(in: RDD[FastqRecord]): RDD[FastqRecord] = in.map(trim_asdf).filter(_ != null)

  private def trim(in: FastqRecord): FastqRecord = {
    val quals = in.qualityAsInteger
    if (in.sequence.length < windowLength) return null
    var total = quals.take(windowLength).sum
    if (total < totalRequiredQuality) return null
    var lengthToKeep = quals.length
    for (i <- 0 to (quals.length - windowLength)) {
      total = total - quals(i) + quals(i + windowLength)
      if (total < totalRequiredQuality) {
        lengthToKeep = i + windowLength
        break
      }
    }
    var i = lengthToKeep
    var lastBaseQuality = quals(i - 1)
    while (lastBaseQuality < requiredQuality && i > 1) {
      i -= 1
      lastBaseQuality = quals(i - 1)
    }
    if (i < 1) null
    if (i < quals.length) FastqRecord(in.name, in.sequence.substring(0, i), in.comment, in.quality)
    in
  }

  private def trim_asdf(in: FastqRecord): FastqRecord = {
    @tailrec
    def calculateLength(qualsSums: Iterator[Int], lengthToKeep: Int): Int = {
      if (!qualsSums.hasNext || qualsSums.next() < totalRequiredQuality) {
        return lengthToKeep
      }
      calculateLength(qualsSums, lengthToKeep + 1)
    }

    if (in.sequence.length < windowLength) return null
    val qualitySums = in.qualityAsInteger.sliding(windowLength).map(_.sum)
    if (qualitySums.next() < totalRequiredQuality) {
      null
    } else {
      val lengthToKeep = calculateLength(qualitySums, windowLength)
      FastqRecord(in.name, in.sequence.substring(0, lengthToKeep), in.comment, in.quality.substring(0, lengthToKeep), in.phredOffset)
    }
  }
}
