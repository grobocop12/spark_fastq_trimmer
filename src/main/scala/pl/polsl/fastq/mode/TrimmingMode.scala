package pl.polsl.fastq.mode

import java.io.{BufferedReader, BufferedWriter, FileReader, FileWriter}
import java.nio.file.{FileSystems, Files}
import scala.jdk.CollectionConverters.IteratorHasAsScala
import scala.util.matching.Regex

trait TrimmingMode extends Mode {
  def concatenateFiles(tempOutput: String, output: String): Unit = {
    val pattern: Regex = "part-\\d+".r
    val dir = FileSystems.getDefault.getPath(tempOutput)
    val sw = new BufferedWriter(new FileWriter(output))
    Files.list(dir)
      .iterator()
      .asScala
      .filter(p => pattern.matches(p.getFileName.toString))
      .foreach(p => {
        val reader = new BufferedReader(new FileReader(p.toString))
        var line: String = reader.readLine()
        while (line != null) {
          sw.write(line)
          sw.newLine()
          line = reader.readLine()
        }
        reader.close()
      })
    sw.close()
  }
}
