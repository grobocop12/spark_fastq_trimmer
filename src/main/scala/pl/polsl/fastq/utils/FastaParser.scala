package pl.polsl.fastq.utils

import org.apache.hadoop.shaded.org.jline.utils.InputStreamReader
import pl.polsl.fastq.data.FastaRecord

import java.io.{BufferedInputStream, BufferedReader, File, FileInputStream}

class FastaParser(val file: Array[String]) {
  //  val reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(file, 1000000)))
  //  val reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(getClass.getResourceAsStream(file.getName), 1000000)))
  var lines = file
  var current: Option[FastaRecord] = None
  var currentLine: Option[String] = None

  def hasNext: Boolean = current.nonEmpty

  def next: FastaRecord = {
    val current = this.current.get
    parseOne()
    current
  }

  def parseOne(): Unit = {
    current = None

    if (currentLine.isEmpty && lines.nonEmpty) {
      currentLine = lines.headOption
      lines = lines.tail
    }

    while (current.nonEmpty && !currentLine.get.startsWith(">")) {
      currentLine = lines.headOption
      lines = lines.tail
    }

    if (currentLine.nonEmpty && currentLine.get.startsWith(">")) {
      val fullName = currentLine.get.substring(1).trim
      val tokens = fullName.split("[\\| ]")
      val name = tokens(0)
      val builder = new StringBuilder
      currentLine = lines.headOption
      lines = lines.tail
      while (currentLine.nonEmpty && !currentLine.get.startsWith(">")) {
        if (!currentLine.get.startsWith(";")) builder.append(currentLine.get.trim)
        currentLine = lines.headOption
        lines = if (lines.isEmpty) Array() else lines.tail
      }
      current = Option(new FastaRecord(name, builder.toString.trim, fullName))
    }
  }

}
