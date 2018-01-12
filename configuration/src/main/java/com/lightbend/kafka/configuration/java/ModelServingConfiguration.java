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

package com.lightbend.kafka.configuration.java;

public class ModelServingConfiguration {
    public static String LOCAL_ZOOKEEPER_HOST = "localhost:2181";
    public static String LOCAL_KAFKA_BROKER = "localhost:9092";

    public static String DATA_TOPIC = "mdata";
    public static String MODELS_TOPIC = "models";

    public static String DATA_GROUP = "wineRecordsGroup";
    public static String MODELS_GROUP = "modelRecordsGroup";

}
