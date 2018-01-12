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

package com.lightbend.model;

import com.lightbend.model.PMML.PMMLModelFactory;
import com.lightbend.model.tensorflow.TensorflowModelFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by boris on 6/28/17.
 */
public class DataConverter {

    private static final Map<Integer, ModelFactory> factories = new HashMap<Integer, ModelFactory>() {
        {
            put(Modeldescriptor.ModelDescriptor.ModelType.TENSORFLOW.getNumber(), TensorflowModelFactory.getInstance());
            put(Modeldescriptor.ModelDescriptor.ModelType.PMML.getNumber(), PMMLModelFactory.getInstance());
        }
    };

    private DataConverter(){}

    public static Optional<Winerecord.WineRecord> convertData(byte[] binary){
        try {
            // Unmarshall record
            return Optional.of(Winerecord.WineRecord.parseFrom(binary));
        } catch (Throwable t) {
            // Oops
            System.out.println("Exception parsing input record" + new String(binary));
            t.printStackTrace();
            return Optional.empty();
        }
    }

    public static Optional<ModelToServe> convertModel(byte[] binary){
        try {
            // Unmarshall record
            Modeldescriptor.ModelDescriptor model = Modeldescriptor.ModelDescriptor.parseFrom(binary);
            // Return it
            if(model.getMessageContentCase().equals(Modeldescriptor.ModelDescriptor.MessageContentCase.DATA)){
               return Optional.of(new ModelToServe(
                        model.getName(), model.getDescription(), model.getModeltype(),
                        model.getData().toByteArray(), null, model.getDataType()));
            }
            else {
                System.out.println("Location based model is not yet supported");
                return Optional.empty();
            }
        } catch (Throwable t) {
            // Oops
            System.out.println("Exception parsing input record" + new String(binary));
            t.printStackTrace();
            return Optional.empty();
        }
    }

    public static Optional<Model> readModel(DataInputStream input) {

        try {
            int length = (int)input.readLong();
            if (length == 0)
                return Optional.empty();
            int type = (int) input.readLong();
            byte[] bytes = new byte[length];
            input.read(bytes);
            ModelFactory factory = factories.get(type);
            return Optional.of(factory.restore(bytes));
        } catch (Throwable t) {
            System.out.println("Error Deserializing model");
            t.printStackTrace();
            return Optional.empty();
        }
    }

    public static void writeModel(Model model, DataOutputStream output){
        try{
            if(model == null){
                output.writeLong(0);
                return;
            }
            byte[] bytes = model.getBytes();
            output.writeLong(bytes.length);
            output.writeLong(model.getType());
            output.write(bytes);
        }
        catch (Throwable t){
            System.out.println("Error Serializing model");
            t.printStackTrace();
        }
    }

    public static Model copy(Model model) {

        if (model == null)
            return null;
        else {
            ModelFactory factory = factories.get(model.getType());
            if(factory != null)
                return factory.restore(model.getBytes());
            return null;
        }
     }

    public static Model restore(int t, byte[] content){
        ModelFactory factory = factories.get(t);
        if(factory != null)
            return factory.restore(content);
        return null;
    }

    public static Optional<Model> toModel(ModelToServe model){
        ModelFactory factory = factories.get(model.getModelType().getNumber());
        if (factory != null)
            return factory.create(model);
        return Optional.empty();
    }
}