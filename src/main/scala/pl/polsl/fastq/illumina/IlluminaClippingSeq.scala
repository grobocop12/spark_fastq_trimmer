package pl.polsl.fastq.illumina

import pl.polsl.fastq.data.FastqRecord
import pl.polsl.fastq.trimmer.IlluminaClippingTrimmer.LOG10_4

import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters._

abstract class IlluminaClippingSeq(seq: String, mask: Long, pack: Array[Long], seedMaxMiss: Int, minSequenceOverlap: Int, minSequenceLikelihood: Int) extends Serializable {

  def readsSeqCompare(rec: FastqRecord): Integer

  def calculateDifferenceQuality(rec: FastqRecord, clipSeq: String, overlap: Int, recOffset: Int): Float = {
    val seq = rec.sequence
    val quals = rec.qualityAsInteger()
    var recPos = Math.max(recOffset, 0)
    var clipPos = if (recOffset < 0) -recOffset
    else 0
    val likelihood = new Array[Float](overlap)
    for (i <- 0 until overlap) {
      val ch1 = seq.charAt(recPos)
      val ch2 = clipSeq.charAt(clipPos)
      if ((ch1 == 'N') || (ch2 == 'N')) likelihood(i) = 0
      else if (ch1 != ch2) likelihood(i) = -quals(recPos) / 10.0f
      else likelihood(i) = LOG10_4
      recPos += 1
      clipPos += 1
    }
    calculateMaximumRange(likelihood)
  }

  private def calculateMaximumRange(vals: Array[Float]): Float = {
    val merges = ArrayBuffer[Float]().asJava
    var total: Float = 0
    for (values <- vals) {
      if ((total > 0 && values < 0) || (total < 0 && values > 0)) {
        merges.add(total)
        total = values
      }
      else total += values
    }
    merges.add(total)
    var scanAgain = true
    while (merges.size > 0 && scanAgain) {
      val mergeIter = merges.listIterator()
      scanAgain = false
      while (mergeIter.hasNext) {
        val values = mergeIter.next
        if (values < 0 && mergeIter.hasPrevious && mergeIter.hasNext) {
          val prev = mergeIter.previous
          mergeIter.next
          val next = mergeIter.next
          if ((prev > -values) && (next > -values)) {
            mergeIter.remove()
            mergeIter.previous
            mergeIter.remove()
            mergeIter.previous
            mergeIter.set(prev + values + next)
            scanAgain = true
          }
          else mergeIter.previous
        }
      }
    }

    Math.max(merges.asScala.max, 0)
  }
}
