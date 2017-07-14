package com.lightbend.kafka.configuration

/**
  * Created by boris on 5/10/17.
  */
object ModelServingConfiguration {

  val LOCAL_ZOOKEEPER_HOST = "localhost:2181"
  val LOCAL_KAFKA_BROKER = "localhost:9092"

  val DATA_TOPIC = "mdata"
  val MODELS_TOPIC = "models"

  val DATA_GROUP = "wineRecordsGroup"
  val MODELS_GROUP = "modelRecordsGroup"
}
