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

import sbt._
import Versions._

object Dependencies {

  val flinkScala            = "org.apache.flink"    % "flink-scala_2.11"                    % flinkVersion
  val flinkStreaming        = "org.apache.flink"    % "flink-streaming-scala_2.11"          % flinkVersion
  val flinkKafka            = "org.apache.flink"    %% "flink-connector-kafka"              % flinkVersion
  val flinkQueryableRuntime = "org.apache.flink"    %% "flink-queryable-state-runtime"      % flinkVersion
  val flinkQueryableClient  = "org.apache.flink"    %% "flink-queryable-state-client-java"  % flinkVersion
  val flinktestutils        = "org.apache.flink"    %% "flink-test-utils"                   % flinkVersion      % Test

  val kafka                 = "org.apache.kafka"    %% "kafka"                              % kafkaVersion
  val tensorflow            = "org.tensorflow"      % "tensorflow"                          % tensorflowVersion
  val tensoeflowProto       = "org.tensorflow"      % "proto"                               % tensorflowVersion
  val PMMLEvaluator         = "org.jpmml"           % "pmml-evaluator"                      % PMMLVersion
  val PMMLExtensions        = "org.jpmml"           % "pmml-evaluator-extension"            % PMMLVersion
  val joda                  = "joda-time"           % "joda-time"                           % jodaVersion
  val curator               = "org.apache.curator"  % "curator-test"                        % curatorVersion                 // ApacheV2
  val slf4japi              = "org.slf4j"           % "slf4j-api"                           % slf4jVersion
  val slf4jsimple           = "org.slf4j"           % "slf4j-simple"                        % slf4jVersion
  val junit                 = "junit"               % "junit"                               % junitVersion      % Test
  
  val flinkDependencies = Seq(flinkScala, flinkStreaming, flinkKafka, flinkQueryableRuntime, flinkQueryableClient, slf4japi, slf4jsimple, junit, flinktestutils)
  val modelsDependencies = Seq(PMMLEvaluator, PMMLExtensions, tensorflow, tensoeflowProto, slf4japi, slf4jsimple, junit)
}