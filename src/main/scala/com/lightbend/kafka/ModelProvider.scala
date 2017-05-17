package com.lightbend.kafka

import java.io.{ByteArrayOutputStream, File}
import java.nio.file.{Files, Paths}

import com.google.protobuf.ByteString
import com.lightbend.model.modeldescriptor.ModelDescriptor

/**
  * Created by boris on 5/10/17.
  *
  * Application publishing models from /data directory to Kafka
  */
object ModelProvider {

  val directory = "data/"
  val timeInterval = 1000 * 60 * 1        // 1 mins

  def main(args: Array[String]) {
    val sender = KafkaMessageSender(ModelServingConfiguration.LOCAL_KAFKA_BROKER, ModelServingConfiguration.LOCAL_ZOOKEEPER_HOST)
    sender.createTopic(ModelServingConfiguration.MODELS_TOPIC)
    val files = getListOfFiles(directory)
    val bos = new ByteArrayOutputStream()
    while (true) {
      files.foreach(f => {
        val byteArray = Files.readAllBytes(Paths.get(directory + f))
        val record = ModelDescriptor(name = f.dropRight(5),
          description = "generated from SparkML", modeltype = ModelDescriptor.ModelType.PMML,
          dataType = "wine").withData(ByteString.copyFrom(byteArray))
        bos.reset()
        record.writeTo(bos)
        sender.writeValue(ModelServingConfiguration.MODELS_TOPIC, bos.toByteArray)
        pause()
      })
      pause()
    }
  }

  private def pause() : Unit = {
    try{
      Thread.sleep(timeInterval)
    }
    catch {
      case _: Throwable => // Ignore
    }
  }

  private def getListOfFiles(dir: String):Seq[String] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(f => (f.isFile) && (f.getName.endsWith(".pmml"))).map(_.getName)
    } else {
      Seq.empty[String]
    }
  }
}