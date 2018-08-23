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

package com.lightbend.modelServer.query

import com.lightbend.modelServer.ModelToServeStats
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.api.common.{ExecutionConfig, JobID}
import org.apache.flink.api.common.typeinfo.BasicTypeInfo
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.queryablestate.client.QueryableStateClient
import org.joda.time.DateTime

/**
  * Created by boris on 5/12/17.
  * see https://ci.apache.org/projects/flink/flink-docs-release-1.3/dev/stream/queryable_state.html
  */
object ModelStateQuery {

  val timeInterval = 1000 * 20        // 20 sec

  def main(args: Array[String]) {

    val jobId = JobID.fromHexString("7e73aa32facc36def70d1b81ac454fb0")
    val types = Array("wine")

    val client = new QueryableStateClient("127.0.0.1", 9069)

    // the state descriptor of the state to be fetched.
    val descriptor = new ValueStateDescriptor[ModelToServeStats](
      "currentModel",   // state name
      createTypeInformation[ModelToServeStats].createSerializer(new ExecutionConfig) // type serializer
    )
    val keyType = BasicTypeInfo.STRING_TYPE_INFO

    println("                   Name                      |       Description       |       Since       |       Average       |       Min       |       Max       |")
    while(true) {
      for (key <- types) {
        try {
          val future = client.getKvState(jobId, "currentModel", key, keyType, descriptor)
          val stats = future.join().value()
          println(s" ${stats.name} | ${stats.description} | ${new DateTime(stats.since).toString("yyyy/MM/dd HH:MM:SS")} | ${stats.duration/stats.usage} |" +
            s"  ${stats.min} | ${stats.max} |")
        }
        catch {case e: Exception => e.printStackTrace()}
      }
      Thread.sleep(timeInterval)
    }
  }
}