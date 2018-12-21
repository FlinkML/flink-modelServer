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

package com.lightbend.wineserving.server

import org.apache.flink.api.common.functions.FlatMapFunction
import org.apache.flink.util.Collector

import scala.util.{Failure, Success, Try}

object BadDataHandler {
  def apply[T] = new BadDataHandler[T]
}

class BadDataHandler[T] extends FlatMapFunction[Try[T], T] {
  override def flatMap(t: Try[T], out: Collector[T]): Unit = {
    t match {
      case Success(t) => out.collect(t)
      case Failure(e) => println(s"BAD DATA: ${e.getMessage}")
    }
  }
}