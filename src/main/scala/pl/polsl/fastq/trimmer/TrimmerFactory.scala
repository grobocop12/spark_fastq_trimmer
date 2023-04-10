package pl.polsl.fastq.trimmer

object TrimmerFactory {
  def createTrimmers(trimmerNames: List[String]): List[Trimmer] =
    trimmerNames.map(createTrimmer)

  private def createTrimmer(str: String): Trimmer = {
    val idx = str.indexOf(":")
    val name = if (idx > 0) str.substring(0, idx) else str
    val args = if (idx > 0) str.substring(idx + 1, str.length) else ""
    name match {
      case "CROP" => new CropTrimmer(args.toInt)
      case "HEADCROP" => new HeadCropTrimmer(args.toInt)
      case "LEADING" => new LeadingTrimmer(args.toInt )
      case "MINLEN" => new MinLenTrimmer(args.toInt)
      case "MAXLEN" => new MaxLenTrimmer(args.toInt)
      case "SLIDINGWINDOW" =>
        val splittedArgs = args.split(":")
        new SlidingWindowTrimmer(splittedArgs(0).toInt, splittedArgs(1).toFloat)
      case "TRAILING" => new TrailingTrimmer(args.toInt )
      case "TOPHRED33" => new ToPhred33Trimmer()
      case "TOPHRED64" => new ToPhred64Trimmer()
      case _ => throw new RuntimeException(s"Unknown trimmer: $name")
    }
  }
}