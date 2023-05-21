package pl.polsl.fastq.utils

import org.apache.spark.rdd.RDD
import pl.polsl.fastq.data.FastqRecord

object PairValidator extends Serializable {
  def validatePairs(pairs: RDD[(FastqRecord, FastqRecord)]): Unit = pairs.foreach(a => validatePair((a._1.name, a._2.name)))

  private def validatePair(pair: (String, String)): Unit = {
    val tokens1 = pair._1.toCharArray
    val tokens2 = pair._2.toCharArray
    val valid = tokens1.zip(tokens2)
      .forall(c => c._1 == c._2 || (c._1 == '1' && c._2 == '2'))
    if (!valid) println(s"Not valid pair: $pair")
  }
}
