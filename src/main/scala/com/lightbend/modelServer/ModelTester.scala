package com.lightbend.modelServer

import java.nio.file.{Files, Paths}

import com.lightbend.kafka.DataProvider
import com.lightbend.modelServer.model.{PMMLModel, TensorFlowModel}


/**
  * Created by boris on 5/12/17.
  */
object ModelTester {

  val datafile = "data/winequality_red.csv"
  val modelfilePMML = "data/winequalityDecisionTreeClassification.pmml"
  val modelfileTensor = "data/optimized_WineQuality.pb"

  def main(args: Array[String]) {
    val records = DataProvider.getListOfRecords(datafile)
    // PMML
    val PMMLbyteArray = Files.readAllBytes(Paths.get(modelfilePMML))
    val TensorflowbyteArray = Files.readAllBytes(Paths.get(modelfileTensor))
    val PMMLmodel = PMMLModel(PMMLbyteArray)
    val Tensormodel = TensorFlowModel(TensorflowbyteArray)
    println("PMML | Tensorflow")
    records.foreach(r => {
      val presult = PMMLmodel.get.score(r.asInstanceOf[AnyVal]).asInstanceOf[Double]
      val tresult = Tensormodel.get.score(r.asInstanceOf[AnyVal]).asInstanceOf[Double]
      println(s"$presult  | $tresult")
    })
  }
}