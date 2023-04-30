package pl.polsl.fastq.illumina

import pl.polsl.fastq.data.FastqRecord
import pl.polsl.fastq.illumina.IlluminaClippingSeq.{calcSingleMask, packSeqExternal}

import scala.collection.mutable

class IlluminaShortClippingSeq(seq: String, mask: Long, pack: Array[Long], seedMaxMiss: Int, minSequenceOverlap: Int, minSequenceLikelihood: Int)
  extends IlluminaClippingSeq(seq, mask, pack, seedMaxMiss, minSequenceOverlap, minSequenceLikelihood) {

  override def readsSeqCompare(rec: FastqRecord): Integer = {
    val seedMax = seedMaxMiss * 2
    val recSequence = rec.sequence
    val clipSequence = seq
    val offsetSet = new mutable.TreeSet[Integer]
    val packRec = packSeqExternal(rec.sequence)
    val packRecMax = packRec.length - minSequenceOverlap
    val packClipMax = pack.length - minSequenceOverlap
    for (i <- 0 until packRecMax) {
      val comboMask = calcSingleMask(packRec.length - i) & mask
      for (j <- 0 until packClipMax) {
        val diff = java.lang.Long.bitCount((packRec(i) ^ pack(j)) & comboMask)
        if (diff <= seedMax) {
          val offset = i - j
          offsetSet.add(offset)
        }
      }
    }
    for (offset <- offsetSet) {
      val recCompLength = if (offset > 0) recSequence.length - offset
      else recSequence.length
      val clipCompLength = if (offset < 0) clipSequence.length + offset
      else clipSequence.length
      val compLength = Math.min(recCompLength, clipCompLength)
      if (compLength > minSequenceOverlap) {
        val seqLikelihood = calculateDifferenceQuality(rec, clipSequence, compLength, offset)
        if (seqLikelihood >= minSequenceLikelihood) return offset
      }
    }
    null
  }

}
