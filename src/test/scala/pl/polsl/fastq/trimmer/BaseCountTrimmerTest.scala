package pl.polsl.fastq.trimmer

import org.scalatest.flatspec.AnyFlatSpec
import pl.polsl.fastq.data.FastqRecord

class BaseCountTrimmerTest extends AnyFlatSpec {
  behavior of "BaseCountTrimmer"

  it should "drop sequence" in {
    val trimmer = new BaseCountTrimmer("A", 0, 9)
    val record = FastqRecord("READ",
      "GATATTGGCCTGCAGAAGTTCTTCCTGAAAGATGAT", "+", "++++++++++**********$KKK************", 33)

    val result = trimmer.processSingle(record)

    assert(result === null)
  }

  it should "keep sequence" in {
    val trimmer = new BaseCountTrimmer("A", 0, 15)
    val record = FastqRecord("READ",
      "GATATTGGCCTGCAGAAGTTCTTCCTGAAAGATGAT",
      "+",
      "!!!!!!CCCCCCCCCCCCCCCCCCCCCCCCK!!!!!",
      33)

    val result = trimmer.processSingle(record)

    assert(result !== null)
  }
}
