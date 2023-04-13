package pl.polsl.fastq.illumina

class IlluminaPrefixPair(val prefix1: String, val prefix2: String) {

}

object IlluminaPrefixPair {
  def apply(prefix1: String, prefix2: String): IlluminaPrefixPair = {
    if (prefix1.length == prefix2.length) {
      new IlluminaPrefixPair(prefix1, prefix2)
    } else {
      val minLength = if (prefix1.length < prefix2.length) prefix1.length else prefix2.length

      new IlluminaPrefixPair(prefix1.substring(prefix1.length - minLength), prefix2.substring(prefix2.length - minLength))
    }
  }
}