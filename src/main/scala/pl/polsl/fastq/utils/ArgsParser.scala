package pl.polsl.fastq.utils

import scala.annotation.tailrec
import scala.collection.mutable
import scala.jdk.CollectionConverters._


object ArgsParser {
  def parse(args: Array[String]): Map[String, Any] = {
    if (args.isEmpty) throw new RuntimeException("Not enough arguments!")
    parseOne(args.head, args.tail, Map.newBuilder[String, Any])
  }

  @tailrec
  private def parseOne(currentArg: String,
                       args: Array[String],
                       builder: mutable.Builder[(String, Any), Map[String, Any]]): Map[String, Any] = {
    currentArg match {
      case "-m" | "--mode" =>
        builder += ("mode" -> args.head)
        parseOne(args.tail.head, args.tail.tail, builder)
      case "-phred33" =>
        builder += ("phredOffset" -> 33)
        parseOne(args.head, args.tail, builder)
      case "-phred64" =>
        builder += ("phredOffset" -> 64)
        parseOne(args.head, args.tail, builder)
      case "-i" | "--in" =>
        builder += ("input" -> args.head)
        parseOne(args.tail.head, args.tail.tail, builder)
      case "-i1" | "--in1" =>
        builder += ("input_1" -> args.head)
        parseOne(args.tail.head, args.tail.tail, builder)
      case "-i2" | "--in2" =>
        builder += ("input_2" -> args.head)
        parseOne(args.tail.head, args.tail.tail, builder)
      case "-o" | "--out" =>
        builder += ("out" -> args.head)
        parseOne(args.tail.head, args.tail.tail, builder)
      case "-h" | "--help" =>
        builder += ("mode" -> "help")
        builder.result()
      case _ => (builder += ("trimmers" -> (currentArg +: args).toList)).result()
    }
  }
}
