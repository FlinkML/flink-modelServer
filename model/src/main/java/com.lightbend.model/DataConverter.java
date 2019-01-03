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

import java.util.Optional;

// Data converter - collection of static methods for data transformation
public class DataConverter {

    // Model factories converter
    private static ModelFacroriesResolver resolver = null;

    private DataConverter(){}

    // Setting Model factories converter. Has to be invoked by the user at the beginning of his code
    public static void setResolver(ModelFacroriesResolver res){
        resolver = res;
    }

    // Validating that resolver is set
    private static boolean validateResolver() {
        if(resolver == null) {
            System.out.println("Model factories resolver is not set");
            return false;
        }
        return true;
    }

    // Convert byte array to ModelToServe
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
                return Optional.of(new ModelToServe(
                        model.getName(), model.getDescription(), model.getModeltype(),
                        null, model.getLocation(), model.getDataType()));
            }
        } catch (Throwable t) {
            // Oops
            System.out.println("Exception parsing input record" + new String(binary));
            t.printStackTrace();
            return Optional.empty();
        }
    }

    // Deep copy of model
    public static Model copy(Model model) {
        if(!validateResolver())
            return null;
        if (model == null)
            return null;
        else {
            ModelFactory factory = resolver.getFactory((int) model.getType());
            if(factory != null)
                return factory.restore(model.getBytes());
            return null;
        }
     }

    // Restore model from byte
    public static Model restore(int t, byte[] content){
        if(!validateResolver())
            return null;
        ModelFactory factory = resolver.getFactory(t);
        if(factory != null)
            return factory.restore(content);
        return null;
    }

    // Convert ModelToServe to Model
    public static Optional<Model> toModel(ModelToServe model){
        if(!validateResolver())
            return Optional.empty();
        ModelFactory factory = resolver.getFactory(model.getModelType().getNumber());
        if (factory != null)
            return factory.create(model);
        return Optional.empty();
    }
}