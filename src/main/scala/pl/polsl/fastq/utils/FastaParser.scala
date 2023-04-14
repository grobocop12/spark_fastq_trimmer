package pl.polsl.fastq.utils

import org.apache.hadoop.shaded.org.jline.utils.InputStreamReader
import pl.polsl.fastq.data.FastaRecord

import java.io.{BufferedInputStream, BufferedReader, File, FileInputStream}

class FastaParser(val file: File) {
  val reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(file), 1_000_000)))
  var current: Option[FastaRecord] = None
  var currentLine: Option[String] = None

  def parse(): Unit = {

  }

  def hasNext: Boolean = ???

  def next: FastaRecord = ???

  def parseOne(): Unit = {
    current = None

    if (currentLine.isEmpty) currentLine = Option(reader.readLine)

    while (current.nonEmpty && !currentLine.get.startsWith(">")) currentLine = Option(reader.readLine)

    if (currentLine != null && currentLine.startsWith(">")) {
      val fullName = currentLine.substring(1).trim
      val tokens = fullName.split("[\\| ]")
      val name = tokens(0)
      val builder = new StringBuilder
      currentLine = reader.readLine
      while (currentLine != null && !currentLine.startsWith(">")) {
        if (!currentLine.startsWith(";")) builder.append(currentLine.trim)
        currentLine = reader.readLine
      }
      current = new FastaRecord(name, builder.toString.trim, fullName)
    }
  }

}
