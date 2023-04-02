package pl.polsl.fastq.trimmer

object TrimmerFactory {
  def createTrimmers(trimmerNames: List[String], phredOffset: Int): List[Trimmer] =
    trimmerNames.map(createTrimmer(_, phredOffset))

  private def createTrimmer(str: String, phredOffset: Int): Trimmer = {
    val idx = str.indexOf(":")
    val name = if (idx > 0) str.substring(0, idx) else str
    val args = if (idx > 0) str.substring(idx + 1, str.length) else ""
    name match {
      case "CROP" => new CropTrimmer(args.toInt)
      case "HEADCROP" => new HeadCropTrimmer(args.toInt)
      case "LEADING" => new LeadingTrimmer(args.toInt, phredOffset)
      case "MINLEN" => new MinLenTrimmer(args.toInt)
      case "MAXLEN" => new MaxLenTrimmer(args.toInt)
      case "TRAILING" => new TrailingTrimmer(args.toInt, phredOffset)
      case "TOPHRED33" => new ToPhred33Trimmer(phredOffset)
      case "TOPHRED64" => new ToPhred64Trimmer(phredOffset)
      case _ => throw new RuntimeException(s"Unknown trimmer: $name")
    }
  }
}
