package pl.polsl.fastq.trimmer

object TrimmerFactory {
  def createTrimmers(trimmerNames: List[String]): List[Trimmer] = trimmerNames.map(createTrimmer)

  private def createTrimmer(str: String): Trimmer = {
    val idx = str.indexOf(":")
    val name = str.substring(0, idx)
    val args = if (idx > 0) str.substring(idx + 1, str.length) else ""
    name match {
      case "MAXLEN" => new MaxLenTrimmer(args.toInt)
      case _ => throw new RuntimeException(s"Unknown trimmer: $name")
    }
  }
}
