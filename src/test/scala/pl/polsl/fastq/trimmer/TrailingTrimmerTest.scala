package pl.polsl.fastq.trimmer

import org.scalatest.flatspec.AnyFlatSpec
import pl.polsl.fastq.data.FastqRecord

class TrailingTrimmerTest extends AnyFlatSpec {
  behavior of "TrailingTrimmer"

  it should "trim last two leading quals" in {
    val trimmer = new TrailingTrimmer(6)
    val record = FastqRecord("READ", "ATCG", "+", "((!!", 33)

    val result = trimmer.processSingle(record)

    assert(result === FastqRecord("READ", "AT", "+", "((", 33))
  }

  it should "not trim any quals" in {
    val trimmer = new TrailingTrimmer(0)
    val record = FastqRecord("READ", "ATCG", "+", "((!!", 33)

    val result = trimmer.processSingle(record)

    assert(result === FastqRecord("READ", "ATCG", "+", "((!!", 33))
  }

  it should "trim entire record" in {
    val trimmer = new TrailingTrimmer(8)
    val record = FastqRecord("READ", "ATCG", "+", "((!!", 33)

    val result = trimmer.processSingle(record)

    assert(result === null)
  }
}
