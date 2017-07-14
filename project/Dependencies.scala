import sbt._
import Versions._

object Dependencies {

  val flinkScala = "org.apache.flink" %% "flink-scala" % flinkVersion
  val flinkStreaming = "org.apache.flink" %% "flink-streaming-scala" % flinkVersion
  val flinkKafka = "org.apache.flink" % "flink-connector-kafka-0.10_2.11" % flinkVersion
  val kafka = "org.apache.kafka" % "kafka_2.11" % kafkaVersion
  val tensorflow = "org.tensorflow" % "tensorflow" % tensorflowVersion
  val PMMLEvaluator = "org.jpmml" % "pmml-evaluator" % PMMLVersion
  val PMMLExtensions = "org.jpmml" % "pmml-evaluator-extension" % PMMLVersion
  val joda = "joda-time" % "joda-time" % jodaVersion
  val kryo = "com.esotericsoftware.kryo" % "kryo" % kryoVersion


  val flinkDependencies = Seq(flinkScala, flinkStreaming, flinkKafka)
  val modelsDependencies = Seq(PMMLEvaluator, PMMLExtensions, tensorflow)
}