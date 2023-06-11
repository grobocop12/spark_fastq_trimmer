package pl.polsl.fastq.utils

object PairValidator extends Serializable {
  def validatePair(pair: (String, String)): Unit = {
    val tokens1 = pair._1.toCharArray
    val tokens2 = pair._2.toCharArray
    val valid = tokens1.zip(tokens2)
      .forall(c => c._1 == c._2 || (c._1 == '1' && c._2 == '2'))
    if (!valid) throw new RuntimeException("Not valid pair: " + pair._1 + " " + pair._2)
  }
}
