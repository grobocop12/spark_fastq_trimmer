package pl.polsl.fastq.trimmer

import org.scalatest.flatspec.AnyFlatSpec
import pl.polsl.fastq.data.FastqRecord

class LeadingTrimmerTest extends AnyFlatSpec {
  behavior of "LeadingTrimmer"

  it should "trim first two leading quals" in {
    val trimmer = new LeadingTrimmer(6)
    val record = FastqRecord("READ", "ATCG",  "!!((", 33)

    val result = trimmer.processSingle(record)

    assert(result === FastqRecord("READ", "CG", "((", 33))
  }

  it should "not trim any quals" in {
    val trimmer = new LeadingTrimmer(0)
    val record = FastqRecord("READ", "ATCG", "!!((", 33)

    val result = trimmer.processSingle(record)

    assert(result === FastqRecord("READ", "ATCG", "!!((", 33))
  }

  it should "trim entire record" in {
    val trimmer = new LeadingTrimmer(8)
    val record = FastqRecord("READ", "ATCG", "!!((", 33)

    val result = trimmer.processSingle(record)

    assert(result === null)
  }
}
