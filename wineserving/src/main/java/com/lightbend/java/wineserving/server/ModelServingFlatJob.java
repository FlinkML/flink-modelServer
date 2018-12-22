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

package com.lightbend.java.wineserving.server;

import com.lightbend.kafka.configuration.java.ModelServingConfiguration;
import com.lightbend.model.DataConverter;
import com.lightbend.model.DataToServe;
import com.lightbend.model.ModelToServe;
import com.lightbend.java.wineserving.model.WineFactoryResolver;
import com.lightbend.modelserver.java.partitioned.DataProcessorMap;
import com.lightbend.modelserver.java.typeschema.ByteArraySchema;
import org.apache.flink.api.common.typeinfo.PrimitiveArrayTypeInfo;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.JobManagerOptions;
import org.apache.flink.configuration.TaskManagerOptions;
import org.apache.flink.runtime.concurrent.Executors;
import org.apache.flink.runtime.highavailability.HighAvailabilityServicesUtils;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.minicluster.LocalFlinkMiniCluster;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

import java.util.Optional;
import java.util.Properties;

public class ModelServingFlatJob {

    public static void main(String[] args) {
//    executeLocal();
        executeServer();
    }

    // Execute on the local Flink server - to test queariable state
    private static void  executeServer() {

        // We use a mini cluster here for sake of simplicity, because I don't want
        // to require a Flink installation to run this demo. Everything should be
        // contained in this JAR.

        int port = 6124;
        int parallelism = 2;

        Configuration config = new Configuration();
        config.setInteger(JobManagerOptions.PORT, port);
        config.setString(JobManagerOptions.ADDRESS, "localhost");
        config.setInteger(TaskManagerOptions.NUM_TASK_SLOTS, parallelism);

        try {

            // Create a local Flink server
            LocalFlinkMiniCluster flinkCluster = new LocalFlinkMiniCluster(
                    config,
                    HighAvailabilityServicesUtils.createHighAvailabilityServices(
                            config,
                            Executors.directExecutor(),
                            HighAvailabilityServicesUtils.AddressResolution.TRY_ADDRESS_RESOLUTION),
                    false);
            // Start server and create environment
            flinkCluster.start(true);

            StreamExecutionEnvironment env = StreamExecutionEnvironment.createRemoteEnvironment("localhost", port);
            env.setParallelism(parallelism);
            // Build Graph
            buildGraph(env);
            JobGraph jobGraph = env.getStreamGraph().getJobGraph();
            // Submit to the server and wait for completion
            flinkCluster.submitJobAndWait(jobGraph, false);
        } catch (Throwable t){
            t.printStackTrace();
        }
    }

    // Execute localle in the environment
    private static void  executeLocal(){
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        buildGraph(env);
        System.out.println("[info] Job ID: " + env.getStreamGraph().getJobGraph().getJobID());
        try {
            env.execute();
        }
        catch (Throwable t){
            t.printStackTrace();
        }
    }

    // Build execution Graph
    private static void buildGraph(StreamExecutionEnvironment env) {
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.enableCheckpointing(5000);

        // configure Kafka consumer
        // Data
        Properties dataKafkaProps = new Properties();
        dataKafkaProps.setProperty("zookeeper.connect", ModelServingConfiguration.LOCAL_ZOOKEEPER_HOST);
        dataKafkaProps.setProperty("bootstrap.servers", ModelServingConfiguration.LOCAL_KAFKA_BROKER);
        dataKafkaProps.setProperty("group.id", ModelServingConfiguration.DATA_GROUP);
        // always read the Kafka topic from the current location
        dataKafkaProps.setProperty("auto.offset.reset", "earliest");

        // Model
        Properties modelKafkaProps = new Properties();
        modelKafkaProps.setProperty("zookeeper.connect", ModelServingConfiguration.LOCAL_ZOOKEEPER_HOST);
        modelKafkaProps.setProperty("bootstrap.servers", ModelServingConfiguration.LOCAL_KAFKA_BROKER);
        modelKafkaProps.setProperty("group.id", ModelServingConfiguration.MODELS_GROUP);
        // always read the Kafka topic from the beginning
        modelKafkaProps.setProperty("auto.offset.reset", "earliest");

        // create a Kafka consumers
        // Data
        FlinkKafkaConsumer<byte[]> dataConsumer = new FlinkKafkaConsumer<>(
                ModelServingConfiguration.DATA_TOPIC,
                new ByteArraySchema(),
                dataKafkaProps);

        // Model
        FlinkKafkaConsumer<byte[]> modelConsumer = new FlinkKafkaConsumer<>(
                ModelServingConfiguration.MODELS_TOPIC,
                new ByteArraySchema(),
                modelKafkaProps);

        // Create input data streams
        DataStream<byte[]> modelsStream = env.addSource(modelConsumer, PrimitiveArrayTypeInfo.BYTE_PRIMITIVE_ARRAY_TYPE_INFO);
        DataStream<byte[]> dataStream = env.addSource(dataConsumer, PrimitiveArrayTypeInfo.BYTE_PRIMITIVE_ARRAY_TYPE_INFO);

        // Set DataConverter
        DataConverter.setResolver(new WineFactoryResolver());

        // Read data from streams
        DataStream<ModelToServe> models = modelsStream
                .flatMap((byte[] value, Collector<ModelToServe> out) -> {
                    Optional<ModelToServe> model = DataConverter.convertModel(value);
                    if (model.isPresent())
                        out.collect(model.get());
                    else
                        System.out.println("Failed to convert model input");

                }).returns(ModelToServe.class)
                .broadcast();
        DataStream<DataToServe> data = dataStream
                .flatMap((byte[] value, Collector<DataToServe> out) -> {
                    Optional<DataRecord> record = DataRecord.convertData(value);
                    if (record.isPresent())
                        out.collect((DataToServe)record.get());
                    else
                        System.out.println("Failed to convert data input");
                }).returns(DataToServe.class)
                .keyBy(record -> record.getType());

        // Merge streams
        data
                .connect(models)
                .flatMap(new DataProcessorMap())
                .map(result -> {System.out.println(result); return result;});
    }
}
