package com.lighbend.modelServer

import java.util

import com.lightbend.modelServer.ModelToServe
import com.lightbend.modelServer.model.{Model, ModelFactory}
import com.lightbend.modelServer.model.PMML.PMMLModel
import org.dmg.pmml.{FieldName, PMML}
import org.jpmml.evaluator.{Evaluator, InputField}

class SimplePMMLModel (inputStream: Array[Byte]) extends PMMLModel(inputStream) {

  override def score(input: AnyVal): AnyVal = null.asInstanceOf[AnyVal]

  // Getters for validation
  def getPmml: PMML = pmml

  def getEvaluator: Evaluator = evaluator

  def getTname: FieldName = tname

  def getInputFields: util.List[InputField] = inputFields
}

object SimplePMMLModel extends ModelFactory {
  override def create(input: ModelToServe): Option[Model] =
    try {
      Some(new SimplePMMLModel(input.model))
    }catch{
      case t: Throwable => None
    }

  override def restore(bytes: Array[Byte]): Model = new SimplePMMLModel(bytes)
}