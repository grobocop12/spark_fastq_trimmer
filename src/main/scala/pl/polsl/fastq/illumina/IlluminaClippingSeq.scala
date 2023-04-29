package pl.polsl.fastq.illumina

abstract class IlluminaClippingSeq {

  private[trim] val seq: String = null
  private[trim] val pack: Array[Long] = null


  def getSeq: String = {
    return seq
  }

  def getPack: Array[Long] = {
    return pack
  }

  private[trim] def readsSeqCompare(rec: FastqRecord): Integer


  private[trim] def calculateDifferenceQuality(rec: FastqRecord, clipSeq: String, overlap: Int, recOffset: Int): Float = {
    val seq: String = rec.getSequence
    val quals: Array[Int] = rec.getQualityAsInteger(true)
    var recPos: Int = if ((recOffset > 0)) {
      recOffset
    }
    else {
      0
    }
    var clipPos: Int = if ((recOffset < 0)) {
      -(recOffset)
    }
    else {
      0
    }
    val likelihood: Array[Float] = new Array[Float](overlap)
    for (i <- 0 until overlap) {
      val ch1: Char = seq.charAt(recPos)
      val ch2: Char = clipSeq.charAt(clipPos)
      if ((ch1 == 'N') || (ch2 == 'N')) {
        likelihood(i) = 0
      }
      else {
        if (ch1 != ch2) {
          likelihood(i) = -(quals(recPos)) / 10.0f
        }
        else {
          likelihood(i) = LOG10_4
        }
      }
      recPos += 1
      clipPos += 1
    }
    val l: Float = calculateMaximumRange(likelihood)
    return l
  }

  private def calculateMaximumRange(vals: Array[Float]): Float = {
    val merges: List[Float] = new ArrayList[Float]
    var total: Float = 0
    for (`val` <- vals) {
      if ((total > 0 && `val` < 0) || (total < 0 && `val` > 0)) {
        merges.add(total)
        total = `val`
      }
      else {
        total += `val`
      }
    }
    merges.add(total)
    var scanAgain: Boolean = true
    while (merges.size > 0 && scanAgain) {
      val mergeIter: ListIterator[Float] = merges.listIterator
      scanAgain = false
      while (mergeIter.hasNext) {
        val `val`: Float = mergeIter.next
        if (`val` < 0 && mergeIter.hasPrevious && mergeIter.hasNext) {
          val prev: Float = mergeIter.previous
          mergeIter.next
          val next: Float = mergeIter.next
          if ((prev > -(`val`)) && (next > -(`val`))) {
            mergeIter.remove()
            mergeIter.previous
            mergeIter.remove()
            mergeIter.previous
            mergeIter.set(prev + `val` + next)
            scanAgain = true
          }
          else {
            mergeIter.previous
          }
        }
      }
    }
    var max: Float = 0
    import scala.collection.JavaConversions._
    for (`val` <- merges) {
      if (`val` > max) {
        max = `val`
      }
    }
    return max
  }
}
