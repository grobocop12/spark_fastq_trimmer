# SparkFastqTrimmer
SparkFastqTrimmer is an NGS data cleansing tool in Apache Spark. Its functionality is modeled on that offered by Trimmomatic ([github](https://github.com/usadellab/Trimmomatic)).

# Building from Source
SparkFastqTrimmer uses a sbt build.

## Building from Command Line
``sbt assembly``

# Program arguments
* -h - prints help,
* -phred33 - sets phred offset value to 33,
* -phred64 - sets phred offset value to 64,
* -partitions - sets minimal number of partitions in RDDs,
* -m - sets the mode to select from SE, PE and PERO,
* -i - path to input file in SE and PERO modes,
* -i1 - path to first input file in PE mode,
* -i2 - path to second input file in PE mode,
* -o - path to output directory,
* -v - enables validation of pair names in PE and PERO modes.

# Trimmers
* AVGQUAL
* BASECOUNT
* CROP
* HEADCROP
* ILLUMINACLIP
* LEADING
* MINLEN
* MAXINFO
* MAXLEN
* SLIDINGWINDOW
* TAILCROP
* TRAILING
* TOPHRED33
* TOPHRED64

# Running SparkFastqTrimmer
Program can be run with the following command:

``java -jar spark-fastq-trimmer.jar [ARGS] [TRIMMERS]``

## SingleEndMode (SE)
SingleEndMode cleanses a single fastq file.
### Example
``java -jar spark-fastq-trimmer.jar -m SE -i path/to/file.fastq -o path/to/output LEADING:3 TRAILING:2``

## PairedEndMode (PE)
PairedEndMode cleanses paired fastq files.
### Example
``java -jar spark-fastq-trimmer.jar -m PE -i1 path/to/file1.fastq -i1 path/to/file2.fastq -o path/to/output LEADING:3 TRAILING:2``

## PairedEndRowOrienteMode (PERO)
PairedEndRowOrientedMode cleanses a single row oriented file.
### Example
``java -jar spark-fastq-trimmer.jar -m PERO -i path/to/file.ro -o path/to/output LEADING:3 TRAILING:2`` 