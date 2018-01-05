/*
 * Copyright (C) 2017  Lightbend
 *
 * This file is part of flink-ModelServing
 *
 * flink-ModelServing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
