name := "FlinkModelServer"

version := "1.0"

scalaVersion := "2.11.8"

lazy val flinkVersion = "1.2.0"
lazy val scalaPB = "0.6.0-pre3"
lazy val tensorflow = "1.1.0"
lazy val PMML = "1.3.5"
lazy val kafka = "0.10.0.1"

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)
libraryDependencies ++= Seq(
  "org.apache.flink" %% "flink-scala" % flinkVersion, // % "provided",
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion, // % "provided",
  "org.apache.flink" % "flink-connector-kafka-0.10_2.11" % flinkVersion,
  "org.apache.kafka" % "kafka_2.11" % kafka,
  "org.tensorflow" % "tensorflow" % tensorflow,
  "org.jpmml" % "pmml-evaluator" % PMML,
  "org.jpmml" % "pmml-evaluator-extension" % PMML
)