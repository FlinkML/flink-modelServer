package com.lightbend.kafka

import java.io.{ByteArrayOutputStream, File}
import java.nio.file.{Files, Paths}

import com.google.protobuf.ByteString
import com.lightbend.model.modeldescriptor.ModelDescriptor
import com.lightbend.model.winerecord.WineRecord

import scala.io.Source

/**
  * Created by boris on 5/10/17.
  *
  * Application publishing models from /data directory to Kafka
  */
object DataProvider {

  val file = "data/winequality_red.csv"
  val timeInterval = 1000 * 1        // 1 sec

  def main(args: Array[String]) {
    val sender = KafkaMessageSender(ModelServingConfiguration.LOCAL_KAFKA_BROKER, ModelServingConfiguration.LOCAL_ZOOKEEPER_HOST)
    sender.createTopic(ModelServingConfiguration.DATA_TOPIC)
    val bos = new ByteArrayOutputStream()
    val records  = getListOfRecords(file)
    while (true) {
      var lineCounter = 0
      records.foreach(r => {
        bos.reset()
        r.writeTo(bos)
        lineCounter = lineCounter + 1
        if(lineCounter % 50 == 0)
          println(s"Processed $lineCounter record")
        sender.writeValue(ModelServingConfiguration.DATA_TOPIC, bos.toByteArray)
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

  def getListOfRecords(file: String): Seq[WineRecord] = {

    var result = Seq.empty[WineRecord]
    val bufferedSource = Source.fromFile(file)
    for (line <- bufferedSource.getLines) {
      val cols = line.split(";").map(_.trim)
      val record = new WineRecord(
        fixedAcidity = cols(0).toFloat,
        volatileAcidity = cols(1).toFloat,
        citricAcid = cols(2).toFloat,
        residualSugar = cols(3).toFloat,
        chlorides = cols(4).toFloat,
        freeSulfurDioxide = cols(5).toFloat,
        totalSulfurDioxide = cols(6).toFloat,
        density = cols(7).toFloat,
        pH = cols(8).toFloat,
        sulphates = cols(9).toFloat,
        alcohol = cols(10).toFloat,
        dataType = "wine"
      )
      result = record +: result
    }
    bufferedSource.close
    result
  }
}