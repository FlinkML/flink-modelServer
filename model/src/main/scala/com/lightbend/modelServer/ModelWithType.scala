package com.lightbend.modelServer

import com.lightbend.modelServer.model.Model

// Used for checkpointing
class ModelWithType(current : Boolean, dType: String, m: Option[Model]) {

  val isCurrent = current
  val dataType = dType
  val model = m

  override def equals(obj: Any): Boolean = {
    obj match {
      case modelWithType: ModelWithType =>
        modelWithType.isCurrent == isCurrent &&
          modelWithType.dataType == dataType &&
          (modelWithType.model match {
            case Some(m) =>
              model match {
                case Some(n) => m == n
                case _ => false
              }
            case _ => model.isEmpty
          })
      case _ => false
    }
  }
}
