package pl.polsl.fastq.data

case class FastaRecord(name: String, sequence: String, var fullName: String = null, var barcodeLabel: String = null)

object FastaRecord {
  def complementaryChar(ch: Character): Character = ch match {
    case 'A' => 'T'
    case 'T' => 'A'
    case 'C' => 'G'
    case 'G' => 'C'
    case _ => 'N'
  }
}