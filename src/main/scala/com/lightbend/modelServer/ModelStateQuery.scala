package com.lightbend.modelServer

import java.util.concurrent.TimeUnit

import org.apache.flink.api.common.{ExecutionConfig, JobID}
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.configuration.{ConfigConstants, Configuration}
import org.apache.flink.runtime.query.QueryableStateClient
import org.apache.flink.runtime.query.netty.message.KvStateRequestSerializer
import org.apache.flink.runtime.state.{VoidNamespace, VoidNamespaceSerializer}

import org.joda.time.DateTime

import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration

/**
  * Created by boris on 5/12/17.
  * see https://ci.apache.org/projects/flink/flink-docs-release-1.3/dev/stream/queryable_state.html
  * It uses default port 6123 to access Flink server
  */
object ModelStateQuery {

  val timeInterval = 1000 * 20        // 20 sec

  def main(args: Array[String]) {

//  val parameterTool = ParameterTool.fromArgs(args)
//  val jobId = JobID.fromHexString(parameterTool.get("job"))
    val jobId = JobID.fromHexString("b5f1a882591256f3a5d163b683fd5aef")
    val types = Array("wine")

    val config = new Configuration()
    config.setString(ConfigConstants.JOB_MANAGER_IPC_ADDRESS_KEY, "localhost")
    config.setInteger(ConfigConstants.JOB_MANAGER_IPC_PORT_KEY, 6124)

    val client = new QueryableStateClient(config)
    val execConfig = new ExecutionConfig
    val keySerializer = createTypeInformation[String].createSerializer(execConfig)
    val valueSerializer = createTypeInformation[ModelToServeStats].createSerializer(execConfig)

    println("                   Name                      |       Description       |       Since       | Used")
    while(true) {
      val stats = for (key <- types) yield {
        val serializedKey = KvStateRequestSerializer.serializeKeyAndNamespace(
          key,
          keySerializer,
          VoidNamespace.INSTANCE,
          VoidNamespaceSerializer.INSTANCE)

        // now wait for the result and return it
        try {
          val serializedResult = client.getKvState(jobId, "currentModel", key.hashCode(), serializedKey)
          val serializedValue = Await.result(serializedResult, FiniteDuration(2, TimeUnit.SECONDS))
          val value = KvStateRequestSerializer.deserializeValue(serializedValue, valueSerializer)
          List(value.name, value.description, value.since, value.usage)
        } catch {
          case e: Exception => {
            e.printStackTrace()
            List()
          }
        }
      }
      stats.toList.filter(_.nonEmpty).foreach(row =>
        println(s" ${row(0)} | ${row(1)} | ${new DateTime(row(2)).toString("yyyy/MM/dd HH:MM:SS")} | ${row(3)}")
      )
      Thread.sleep(timeInterval)
    }
  }
}