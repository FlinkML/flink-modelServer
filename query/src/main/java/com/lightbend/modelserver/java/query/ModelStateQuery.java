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

        JobID jobId = JobID.fromHexString("48aa41f575e99995ca74132de7331ad0");
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
                        client.getKvState(jobId, "currentModelState", key, keyType, descriptor);
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