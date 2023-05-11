package pl.polsl.fastq.trimmer

import org.apache.spark.rdd.RDD
import pl.polsl.fastq.data.FastqRecord

import scala.annotation.tailrec

class MaximumInformationTrimmer(parLength: Int, strictness: Float) extends SingleTrimmer {
  private val maxQual = 60
  private val (lengthScore, qualProb) = initialize()

  //  override def apply(in: Array[FastqRecord]): Array[FastqRecord] = in.map(trim).filter(_ != null)

  override protected def processRecord(rec:FastqRecord): FastqRecord = {
    val quals = rec.qualityAsInteger()
    val (maxScore, maxScorePosition) = calculateMaxScoreAndPosition(quals.zipWithIndex, 0L, Double.MinValue, 0)
    if (maxScorePosition < 1 || maxScore == 0.0) return null
    if (maxScorePosition < quals.length)
      return FastqRecord(rec.name,
        rec.sequence.substring(0, maxScorePosition),
        rec.comment,
        rec.quality.substring(0, maxScorePosition),
        rec.phredOffset)
    rec
  }

  @tailrec
  private def calculateMaxScoreAndPosition(quals: Array[(Int, Int)],
                                           accumQuality: Long,
                                           maxScore: Double,
                                           maxScorePosition: Int): (Double, Int) = {
    val q = if (quals.head._1 < 0) 0 else if (quals.head._1 > maxQual) maxQual else quals.head._1
    val newAccumQuality = accumQuality + qualProb(q)
    val score = lengthScore(quals.head._2) + newAccumQuality
    val (newMaxScore, newMaxScorePos): (Double, Int) = if (score >= maxScore) (score, quals.head._2 + 1) else (maxScore, maxScorePosition)
    if (quals.tail.isEmpty) {
      (newMaxScore, newMaxScorePos)
    } else {
      calculateMaxScoreAndPosition(quals.tail, newAccumQuality, newMaxScore, newMaxScorePos)
    }
  }

  private def initialize(): (Array[Long], Array[Long]) = {
    val longestRead = 1000
    val lengthScoreTmp = Range(0, longestRead)
      .map(initializeLengthScore)
      .toArray
    val qualProbTmp = Range.inclusive(0, maxQual)
      .map(i => Math.log(1 - Math.pow(0.1, (0.5 + i) / 10.0)) * strictness)
      .toArray
    val normRatio = Math.max(calcNormalization(lengthScoreTmp, longestRead * 2),
      calcNormalization(qualProbTmp, longestRead * 2))
    val lengthScore = lengthScoreTmp.map(i => (i * normRatio).asInstanceOf[Long])
    val qualProb = qualProbTmp.map(i => (i * normRatio).asInstanceOf[Long])
    (lengthScore, qualProb)
  }

  private def initializeLengthScore(i: Int): Double = {
    val unique = Math.log(1.0 / (1.0 + Math.exp(parLength - i - 1)))
    val coverage = Math.log(i + 1) * (1 - strictness)
    unique + coverage
  }

  private def calcNormalization(array: Array[Double], margin: Int): Double = {
    val max = Math.max(array.head, array.tail.map(i => Math.abs(i)).max)
    Long.MaxValue / (max * margin)
  }
}
