package pl.polsl.fastq.trimmer

import org.scalatest.flatspec.AnyFlatSpec
import pl.polsl.fastq.data.FastqRecord

class AvgQualTrimmerTest extends AnyFlatSpec {
  behavior of "AvgQualTrimmer"

  it should "drop sequence" in {
    val trimmer = new AvgQualTrimmer(20)
    val record = FastqRecord("READ",
      "GATATTGGCCTGCAGAAGTTCTTCCTGAAAGATGAT",
      "++++++++++**********$KKK************", 33)

    val result = trimmer.processSingle(record)

    assert(result === null)
  }

  it should "keep sequence" in {
    val trimmer = new AvgQualTrimmer(20)
    val record = FastqRecord("READ",
      "GATATTGGCCTGCAGAAGTTCTTCCTGAAAGATGAT",
      "!!!!!!CCCCCCCCCCCCCCCCCCCCCCCCK!!!!!", 33)

    val result = trimmer.processSingle(record)

    assert(result !== null)
  }
}
