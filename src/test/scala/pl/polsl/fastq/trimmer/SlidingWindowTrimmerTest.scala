package pl.polsl.fastq.trimmer

import org.scalatest.flatspec.AnyFlatSpec
import pl.polsl.fastq.data.FastqRecord

class SlidingWindowTrimmerTest extends AnyFlatSpec {
  behavior of "SlidingWindowTrimmer"

  it should "keep first 10 quals" in {
    val trimmer = new SlidingWindowTrimmer(5, 10)
    val record = FastqRecord(
      "READ",
      "GATATTGGCCTGCAGAAGTTCTTCCTGAAAGATGAT",
      "+",
      "++++++++++**************************",
      33)

    val result = trimmer.processSingle(record)

    assert(result === FastqRecord("READ",
      "GATATTGGCC",
      "+",
      "++++++++++",
      33))
  }

  it should "drop too short record" in {
    val trimmer = new SlidingWindowTrimmer(10, 10)
    val record = FastqRecord(
      "READ",
      "GATATTGG",
      "+",
      "++++++++",
      33)

    val result = trimmer.processSingle(record)

    assert(result === null)
  }

  it should "trim entire record" in {
    val trimmer = new SlidingWindowTrimmer(5, 10)
    val record = FastqRecord(
      "READ",
      "GATATTGGCCTGCAGAAGTTCTTCCTGAAAGATGAT",
      "+",
      "!+++++++++**************************",
      33)

    val result = trimmer.processSingle(record)

    assert(result === null)
  }
}
