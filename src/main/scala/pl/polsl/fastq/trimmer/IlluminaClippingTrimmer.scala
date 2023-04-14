package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import pl.polsl.fastq.data.{FastaRecord, FastqRecord}
import pl.polsl.fastq.illumina.{IlluminaClippingSeq, IlluminaPrefixPair}
import pl.polsl.fastq.trimmer.IlluminaClippingTrimmer.{LOG10_4, PREFIX, SUFFIX_F, SUFFIX_R}
import pl.polsl.fastq.utils.FastaParser

import java.io.File
import java.util
import scala.collection.mutable

class IlluminaClippingTrimmer(val seedMaxMiss: Int,
                              val minPalindromeLikelihood: Int,
                              val minSequenceLikelihood: Int,
                              val minPrefix: Int,
                              val palindromeKeepBooth: Boolean) extends Trimmer {
  val minSequenceOverlap = calculateMinSequenceOverlap()
  val prefixPairs = new mutable.ListBuffer[IlluminaPrefixPair]()
  val commonSeqs = new mutable.HashSet[IlluminaClippingSeq]()
  val forwardSeqs = new mutable.HashSet[IlluminaClippingSeq]()
  val reverseSeqs = new mutable.HashSet[IlluminaClippingSeq]()

  override def apply(in: RDD[FastqRecord]): RDD[FastqRecord] = ???

  private def calculateMinSequenceOverlap(): Int = {
    val result = (minSequenceLikelihood / LOG10_4).toInt
    if (result > 15) 15 else result
  }

  private def loadSequences(sequencePath: File): Unit = {
    val parser = new FastaParser(sequencePath)
    val forwardSeqMap = new mutable.HashMap[String, FastaRecord]
    val reverseSeqMap = new mutable.HashMap[String, FastaRecord]
    val commonSeqMap = new mutable.HashMap[String, FastaRecord]
    val forwardPrefix = new mutable.HashSet[String]
    val reversePrefix = new mutable.HashSet[String]
    parser.parse()
    while (parser.hasNext) {
      val rec = parser.next
      if (rec.name.endsWith(SUFFIX_F)) {
        forwardSeqMap.put(rec.name, rec)
        if (rec.name.startsWith(PREFIX)) forwardPrefix.add(rec.name.substring(0, rec.name.length - SUFFIX_F.length))
      } else if (rec.name.endsWith(SUFFIX_R)) {
        reverseSeqMap.put(rec.name, rec)
        if (rec.name.startsWith(PREFIX)) reversePrefix.add(rec.name.substring(0, rec.name.length - SUFFIX_R.length))
      } else {
        commonSeqMap.addOne((rec.name, rec))
      }
    }
    new mutable.HashSet[String].addAll(forwardPrefix)
      .intersect(reversePrefix)
//      .map()
    ???
  }
}

object IlluminaClippingTrimmer {
  private final val LOG10_4 = 0.60206f
  private final val SUFFIX_F = "/1"
  private final val SUFFIX_R = "/2"
  private final val PREFIX = "Prefix"
  private final val INTERLEAVE = 4

  def createTrimmer(args: Array[String]): IlluminaClippingTrimmer = {
    val file = new File(args(0))
    val seedMaxMiss = args(1).toInt
    val minPalindromeLikelihood = args(2).toInt
    val minSequenceLikelihood = args(3).toInt
    val minPrefix = if (args.length > 4) args(4).toInt else 1
    val palindromeKeepBoth = if (args.length > 5) args(5).toBoolean else false
    val trimmer = new IlluminaClippingTrimmer(seedMaxMiss, minPalindromeLikelihood, minSequenceLikelihood, minPrefix, palindromeKeepBoth)
    trimmer.loadSequences(file)
    trimmer
  }
}