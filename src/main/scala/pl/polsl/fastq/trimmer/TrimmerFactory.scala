package pl.polsl.fastq.trimmer

import java.io.File

object TrimmerFactory {
  def createTrimmers(trimmerNames: List[String]): List[Trimmer] =
    trimmerNames.map(createTrimmer)

  private def createTrimmer(str: String): Trimmer = {
    val idx = str.indexOf(":")
    val name = if (idx > 0) str.substring(0, idx) else str
    val args = if (idx > 0) str.substring(idx + 1, str.length) else ""
    name match {
      case "AVGQUAL" => new AvgQualTrimmer(args.toInt)
      case "BASECOUNT" =>
        val arg = args.split(":").toSeq
        val bases = arg.head
        val minCount: Int = if (arg.length > 1) arg(1).toInt else 0
        val maxCount: Integer = if (arg.length > 2) arg(2).toInt else null
        new BaseCountTrimmer(bases, minCount, maxCount)
      case "CROP" => new CropTrimmer(args.toInt)
      case "HEADCROP" => new HeadCropTrimmer(args.toInt)
      case "ILLUMINACLIP" =>
        val arg: Array[String] = args.split(":")
        val seqs: File = new File(arg(0))
        val seedMaxMiss: Int = arg(1).toInt
        val minPalindromeLikelihood: Int = arg(2).toInt
        val minSequenceLikelihood: Int = arg(3).toInt
        val minPrefix: Int = if (arg.length > 4) arg(4).toInt else 1
        val palindromeKeepBoth: Boolean = if (arg.length > 5) arg(5).toLowerCase.toBoolean else false
        IlluminaClippingTrimmer(seqs, seedMaxMiss, minPalindromeLikelihood, minSequenceLikelihood, minPrefix, palindromeKeepBoth)
      case "LEADING" => new LeadingTrimmer(args.toInt)
      case "MINLEN" => new MinLenTrimmer(args.toInt)
      case "MAXINFO" =>
        val splittedArgs = args.split(":")
        new MaximumInformationTrimmer(splittedArgs(0).toInt, splittedArgs(1).toFloat)
      case "MAXLEN" => new MaxLenTrimmer(args.toInt)
      case "SLIDINGWINDOW" =>
        val splittedArgs = args.split(":")
        new SlidingWindowTrimmer(splittedArgs(0).toInt, splittedArgs(1).toFloat)
      case "TAILCROP" => new TailCropTrimmer(args.toInt)
      case "TRAILING" => new TrailingTrimmer(args.toInt)
      case "TOPHRED33" => new ToPhred33Trimmer()
      case "TOPHRED64" => new ToPhred64Trimmer()
      case _ => throw new RuntimeException(s"Unknown trimmer: $name")
    }
  }
}
