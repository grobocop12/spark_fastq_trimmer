package pl.polsl.fastq.trimmer

import org.scalatest.flatspec.AnyFlatSpec
import pl.polsl.fastq.data.FastqRecord

class CropTrimmerTest extends AnyFlatSpec {
  behavior of "CropTrimmer"

  it should "trim sequenc to 4 quals" in {
    val trimmer = new CropTrimmer(4)
    val record = FastqRecord("READ", "ATCGATCGATCG", "!!((!!((!!((")

    val result = trimmer.processSingle(record)

    assert(result === FastqRecord("READ", "ATCG", "!!(("))
  }

  it should "not trim sequence" in {
    val trimmer = new CropTrimmer(12)
    val record = FastqRecord("READ", "ATCGATCGATCG", "!!((!!((!!((")

    val result = trimmer.processSingle(record)

    assert(result === FastqRecord("READ", "ATCGATCGATCG", "!!((!!((!!(("))
  }

  it should "not trim sequence shorter than threshold" in {
    val trimmer = new CropTrimmer(12)
    val record = FastqRecord("READ", "ATCGATCGAT", "!!((!!((!!")

    val result = trimmer.processSingle(record)

    assert(result === FastqRecord("READ", "ATCGATCGAT", "!!((!!((!!"))
  }
}
