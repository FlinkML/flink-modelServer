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

package com.lightbend.model.tensorflow;

import com.lightbend.model.Model;
import com.lightbend.model.ModelToServe;
import com.lightbend.model.ModelFactory;

import java.util.Optional;

/**
 * Created by boris on 7/15/17.
 */
public class TensorflowModelFactory implements ModelFactory {

    private static TensorflowModelFactory instance = null;

    @Override
    public Optional<Model> create(ModelToServe descriptor) {

        try{
            return Optional.of(new TensorflowModel(descriptor.getModelData()));
        }
        catch (Throwable t){
            System.out.println("Exception creating TensorflowModel from " + descriptor);
            t.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public Model restore(byte[] bytes) {
        try{
            return new TensorflowModel(bytes);
        }
        catch (Throwable t){
            System.out.println("Exception restoring PMMLModel from ");
            t.printStackTrace();
            return null;
        }
    }

    public static ModelFactory getInstance(){
        if(instance == null)
            instance = new TensorflowModelFactory();
        return instance;
    }

}
