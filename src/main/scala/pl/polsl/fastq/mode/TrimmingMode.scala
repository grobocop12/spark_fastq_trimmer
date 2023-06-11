package pl.polsl.fastq.mode

import java.io.{BufferedReader, BufferedWriter, FileReader, FileWriter}
import java.nio.file.{FileSystems, Files}
import java.util.regex.Pattern
import scala.collection.convert.ImplicitConversions.`iterator asScala`

trait TrimmingMode extends Mode {
  def concatenateFiles(tempOutput: String, output: String): Unit = {
    val pattern = "part-\\d+"
    val dir = FileSystems.getDefault.getPath(tempOutput)
    val sw = new BufferedWriter(new FileWriter(output))
    Files.list(dir)
      .iterator()
      .filter(p => p.getFileName.toString.matches(pattern))
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
