package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import pl.polsl.fastq.data.FastqRecord
import pl.polsl.fastq.illumina.{IlluminaClippingSeq, IlluminaPrefixPair}
import pl.polsl.fastq.trimmer.IlluminaClippingTrimmer.LOG10_4

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
}

object IlluminaClippingTrimmer {
  private val LOG10_4 = 0.60206f

  def createTrimmer(args: Array[String]): IlluminaClippingTrimmer = {
    val file = new File(args(0))
    val seedMaxMiss = args(1).toInt
    val minPalindromeLikelihood = args(2).toInt
    val minSequenceLikelihood = args(3).toInt
    val minPrefix = if (args.length > 4) args(4).toInt else 1
    val palindromeKeepBoth = if (args.length > 5) args(5).toBoolean else false
    val trimmer = new IlluminaClippingTrimmer(seedMaxMiss, minPalindromeLikelihood, minSequenceLikelihood, minPrefix, palindromeKeepBoth)
    ???
  }
}