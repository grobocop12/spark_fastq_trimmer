package pl.polsl.fastq.trimmer

import org.scalatest.flatspec.AnyFlatSpec
import pl.polsl.fastq.data.FastqRecord

class HeadCropTrimmerTest extends AnyFlatSpec {
  behavior of "HeadCropTrimmer"

  it should "trim first 5 quals" in {
    val trimmer = new HeadCropTrimmer(5)
    val record = FastqRecord("READ", "ATCGATCGATCG", "!!((!!((!!((")

    val result = trimmer.processSingle(record)

    assert(result === FastqRecord("READ", "TCGATCG", "!((!!(("))
  }

  it should "return empty RDD" in {
    val trimmer = new HeadCropTrimmer(5)
    val record = FastqRecord("READ", "ATCGA", "!!((!")

    val result = trimmer.processSingle(record)

    assert(result === null)
  }
}
