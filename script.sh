for ((i=1; i<=10; i++)); do
  echo $i
#  time (java -jar trimmomatic.jar PE -threads 16 data/DRR000001_1.fastq data/DRR000001_2.fastq output/paired_out_1-t.fastq output/unpaired_out_1-t.fastq output/paired_out_2-t.fastq output/unpaired_out_2-t.fastq ILLUMINACLIP:adapters/TruSeq2-PE.fa:2:20:5 LEADING:3 TRAILING:3 SLIDINGWINDOW:4:15 MINLEN:36) >> $1
  time (java -jar target/scala-2.12/spark-fastq-trimmer_2.12-0.0.1-SNAPSHOT.jar -m PE -master local[*] -v -i1 C:\Users\robochlop\dna-scans\DRR000001\DRR000001_1.fastq -i2 C:\Users\robochlop\dna-scans\DRR000001\DRR000001_2.fastq -o out ILLUMINACLIP:adapters/TruSeq2-SE.fa:2:20:5 LEADING:3 TRAILING:3 SLIDINGWINDOW:4:15 MINLEN:36) >> $1
  rm -rf out
done
