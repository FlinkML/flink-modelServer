package com.lightbend.modelserver.java.query;

import com.lightbend.model.ModelToServeStats;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.queryablestate.client.QueryableStateClient;
import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModelStateQuery{

    private static final int timeInterval = 1000 * 20;        // 20 sec

    public static void main(String[] args) throws Exception {

        JobID jobId = JobID.fromHexString("0621cf014973560b866e1f4dc8e58d53");
        List<String> types = Arrays.asList("wine");

        QueryableStateClient client = new QueryableStateClient("127.0.0.1", 9069);

        // the state descriptor of the state to be fetched.
        ValueStateDescriptor<ModelToServeStats> descriptor = new ValueStateDescriptor<>(
                "currentModel",   // state name
                TypeInformation.of(ModelToServeStats.class).createSerializer(new ExecutionConfig()) // type serializer
        );

        BasicTypeInfo keyType = BasicTypeInfo.STRING_TYPE_INFO;

        System.out.println("                   Name                      |       Description       |       Since       |       Average       |       Min       |       Max       |");

        while(true) {
            for (String key : types) {
                CompletableFuture<ValueState<ModelToServeStats>> future =
                        client.getKvState(jobId, "currentModel", key, keyType, descriptor);
                future.thenAccept(response -> {
                    try {
                        ModelToServeStats stats = response.value();
                        System.out.println("  " + stats.getName() + " | " + stats.getDescription() + " | " +
                                new DateTime(stats.getSince()).toString("yyyy/MM/dd HH:MM:SS") + " | " +
                                stats.getDuration() / stats.getInvocations() + " |  " + stats.getMin() + " | " + stats.getMax() + " |");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            Thread.sleep(timeInterval);
        }
    }
}