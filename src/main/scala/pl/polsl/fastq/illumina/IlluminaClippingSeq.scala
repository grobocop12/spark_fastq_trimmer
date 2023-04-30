package pl.polsl.fastq.illumina

import pl.polsl.fastq.data.FastqRecord
import pl.polsl.fastq.trimmer.IlluminaClippingTrimmer.LOG10_4

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

abstract class IlluminaClippingSeq(seq: String, mask: Long, pack: Array[Long], seedMaxMiss: Int, minSequenceOverlap: Int, minSequenceLikelihood: Int) extends Serializable {

  def readsSeqCompare(rec: FastqRecord): Integer

  def calculateDifferenceQuality(rec: FastqRecord, clipSeq: String, overlap: Int, recOffset: Int): Float = {
    val seq = rec.sequence
    val quals = rec.qualityAsInteger(true)
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

object IlluminaClippingSeq {
  val BASE_A = 0x1
  val BASE_C = 0x4
  val BASE_G = 0x8
  val BASE_T = 0x2

  def calcSingleMask(length: Int): Long = {
    var mask = 0xFFFFFFFFFFFFFFFFL
    if (length < 16) mask <<= (16 - length) * 4L
    mask
  }

  def packSeqExternal(seq: String): Array[Long] = {
    var out: Array[Long] = null
    out = new Array[Long](seq.length)
    var pack = 0
    var offset = 0
    for (i <- 0 until 15) {
      var tmp = 0
      if (offset < seq.length) tmp = packCh(seq.charAt(offset), rev = false)
      pack = (pack << 4) | tmp
      offset += 1
    }
    for (i <- 0 until seq.length) {
      var tmp = 0
      if (offset < seq.length) tmp = packCh(seq.charAt(offset), false)
      pack = (pack << 4) | tmp
      out(i) = pack
      offset += 1
    }
    out
  }

  private def packCh(ch: Char, rev: Boolean): Int = {
    if (!rev) ch match {
      case 'A' =>
        return BASE_A
      case 'C' =>
        return BASE_C
      case 'G' =>
        return BASE_G
      case 'T' =>
        return BASE_T
    }
    else ch match {
      case 'A' =>
        return BASE_T
      case 'C' =>
        return BASE_G
      case 'G' =>
        return BASE_C
      case 'T' =>
        return BASE_A
    }
    0
  }
}
