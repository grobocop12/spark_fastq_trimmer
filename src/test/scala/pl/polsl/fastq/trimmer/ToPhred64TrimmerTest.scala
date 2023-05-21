package pl.polsl.fastq.trimmer

import org.scalatest.flatspec.AnyFlatSpec
import pl.polsl.fastq.data.FastqRecord

class ToPhred64TrimmerTest extends AnyFlatSpec {
  behavior of "ToPhred64Trimmer"

  it should "convert quals to phred64" in {
    val trimmer = new ToPhred64Trimmer()
    val record = FastqRecord("READ", "ATCGA", "+", "!,7BK", 33)

    val result = trimmer.processSingle(record)

    assert(result === FastqRecord("READ", "ATCGA", "+", "@KVaj", 64))
  }

  it should "ignore convertion" in {
    val trimmer = new ToPhred64Trimmer()
    val record = FastqRecord("READ", "ATCGA", "+", "@KVaj", 64)

    val result = trimmer.processSingle(record)

    assert(result === FastqRecord("READ", "ATCGA", "+", "@KVaj", 64))
  }
}
