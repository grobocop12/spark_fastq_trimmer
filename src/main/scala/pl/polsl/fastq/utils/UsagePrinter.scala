package pl.polsl.fastq.utils

object UsagePrinter {
  def printUsage(): Unit = {
    val usage =
      """
        |Usage:
        | main [ARGS]... [TRIMMERS]...
        |
        |Examples:
        | main -m SE -i path/to/source -o path/to/output LEADING:30
        | main -m PE -i1 path/to/source1 -i2 path/to/source2 -o path/to/output LEADING:30
        |
        |ARGS:
        | -h,--help
        |   prints help
        | -phred33
        |   sets phred offset to 33
        | -phred64
        |   sets phred offset to 64
        | -m,--mode
        |   selects mode: SE or PE
        | -i,--in
        |   path to source in SE mode
        | -i1,--in1
        |   path to first source in PE mode
        | -i2,--in2
        |   path to second source in PE mode
        | -o,--out
        |   path to output directory
        |
        |
        |TRIMMERS:
        |
        |""".stripMargin
    println(usage)
  }
}
