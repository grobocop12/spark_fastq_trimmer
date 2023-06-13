package pl.polsl.fastq.trimmer

import org.scalatest.flatspec.AnyFlatSpec
import pl.polsl.fastq.data.FastqRecord

class MaximumInformationTrimmerTest extends AnyFlatSpec {
  behavior of "MaximumInformationTrimmer"

  it should "trim sequence after quality drop" in {
    val trimmer = new MaximumInformationTrimmer(100, 2)
    val record = FastqRecord("@DRR000001.2 3060N:7:1:1114:186/1",
      "GATATTGGCCTGCAGAAGTTCTTCCTGAAAGATGAT",
      "!!!!!!CCCCCCCCCCCCCCCCCCCCCCCCK!!!!!",
      33)
    val result = trimmer.processSingle(record)

    assert(result === FastqRecord(
      "@DRR000001.2 3060N:7:1:1114:186/1",
      "GATATTGGCCTGCAGAAGTTCTTCCTGAAAG",
      "!!!!!!CCCCCCCCCCCCCCCCCCCCCCCCK",
      33))
  }
}
