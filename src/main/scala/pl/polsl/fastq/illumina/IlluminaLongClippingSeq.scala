package pl.polsl.fastq.illumina

import pl.polsl.fastq.data.FastqRecord
import pl.polsl.fastq.trimmer.IlluminaClippingTrimmer.INTERLEAVE
import pl.polsl.fastq.trimmer.IlluminaClippingTrimmer2.packSeqInternal

class IlluminaLongClippingSeq(seq: String, mask: Long, pack: Array[Long], seedMaxMiss: Int, minSequenceOverlap: Int, minSequenceLikelihood: Int)
  extends IlluminaClippingSeq(seq, mask, pack, seedMaxMiss, minSequenceOverlap, minSequenceLikelihood) {
  val fullPack: Array[Long] = packSeqInternal(seq, false)
  override pack = new Array[Long]((fullPack.length + INTERLEAVE - 1) / INTERLEAVE)
  var i = 0
  while (i < fullPack.length) {
    this.pack(i / INTERLEAVE) = fullPack(i)
    i += INTERLEAVE
  }

  override def readsSeqCompare(rec: FastqRecord): Integer = {
    val seedMax = seedMaxMiss * 2
    val recSequence = rec.sequence
    val clipSequence = seq
    val offsetSet = new mutable.TreeSet[Int]()
    val packRec = IlluminaClippingTrimmer2.packSeqExternal(rec.sequence)
    val packClip = getPack
    val packRecMax = packRec.length - minSequenceOverlap
    val packClipMax = packClip.length
    for (i <- 0 until packRecMax) {
      val comboMask = IlluminaClippingTrimmer2.calcSingleMask(packRec.length - i)
      for (j <- 0 until packClipMax) {
        val diff = java.lang.Long.bitCount((packRec(i) ^ packClip(j)) & comboMask)
        if (diff <= seedMax) {
          val offset = i - j * IlluminaClippingTrimmer2.INTERLEAVE
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
