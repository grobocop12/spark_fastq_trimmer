package pl.polsl.fastq.trimmer

import org.scalatest.flatspec.AnyFlatSpec
import pl.polsl.fastq.data.FastqRecord

class TailCropTrimmerTest extends AnyFlatSpec {
  behavior of "TailCropTrimmer"

  it should "trim last 5 quals" in {
    val trimmer = new TailCropTrimmer(5)
    val record = FastqRecord("READ", "ATCGATCGATCG",  "!!((!!((!!((")

    val result = trimmer.processSingle(record)

    assert(result === FastqRecord("READ", "ATCGATC",  "!!((!!("))
  }

  it should "return empty RDD" in {
    val trimmer = new TailCropTrimmer(5)
    val record = FastqRecord("READ", "ATCGA", "!!((!")

    val result = trimmer.processSingle(record)

    assert(result === null)
  }
}
