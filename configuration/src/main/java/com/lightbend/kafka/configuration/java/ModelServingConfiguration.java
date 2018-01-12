package com.lightbend.kafka.configuration.java;

public class ModelServingConfiguration {
    public static String LOCAL_ZOOKEEPER_HOST = "localhost:2181";
    public static String LOCAL_KAFKA_BROKER = "localhost:9092";

    public static String DATA_TOPIC = "mdata";
    public static String MODELS_TOPIC = "models";

    public static String DATA_GROUP = "wineRecordsGroup";
    public static String MODELS_GROUP = "modelRecordsGroup";

}
