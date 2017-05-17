package com.lightbend.modelServer

import java.io.ByteArrayInputStream
import java.nio.file.{Files, Paths}

import com.lightbend.kafka.DataProvider
import com.lightbend.model.winerecord.WineRecord
import com.lightbend.modelServer.model.PMMLModel


/**
  * Created by boris on 5/12/17.
  */
object ModelTester {

  val datafile = "data/winequality_red.csv"
  val modelfile = "data/winequalityDecisionTreeClassification.pmml"

  def main(args: Array[String]) {
    val byteArray = Files.readAllBytes(Paths.get(modelfile))
    val records = DataProvider.getListOfRecords(datafile)
    val model = new PMMLModel(new ByteArrayInputStream(byteArray))
    records.foreach(r => {
      val result = model.score(r.asInstanceOf[AnyVal]).asInstanceOf[Double]
      println(result)
    })
  }
}
