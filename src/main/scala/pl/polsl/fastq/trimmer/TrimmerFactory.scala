package pl.polsl.fastq.trimmer

object TrimmerFactory {
  def createTrimmers(trimmerNames: List[String], phredOffset: Int): List[Trimmer] =
    trimmerNames.map(createTrimmer(_, phredOffset))

  private def createTrimmer(str: String, phredOffset: Int): Trimmer = {
    val idx = str.indexOf(":")
    val name = str.substring(0, idx)
    val args = if (idx > 0) str.substring(idx + 1, str.length) else ""
    name match {
      case "CROP" => new CropTrimmer(args.toInt)
      case "LEADING" => new LeadingTrimmer(args.toInt, phredOffset)
      case "MINLEN" => new MinLenTrimmer(args.toInt)
      case "MAXLEN" => new MaxLenTrimmer(args.toInt)
      case "TRAILING" => new TrailingTrimmer(args.toInt, phredOffset)
      case _ => throw new RuntimeException(s"Unknown trimmer: $name")
    }
  }
}
