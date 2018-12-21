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

package com.lightbend.java.wineserving.model;

import com.google.protobuf.Descriptors;
import com.lightbend.model.PMML.PMMLModel;
import com.lightbend.model.Winerecord;
import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.Computable;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.InputField;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by boris on 5/18/17.
 */
public class SpecificPMMLModel extends PMMLModel {

    private static Map<String, String> names = createNamesMap();
    private static Map<String, String> createNamesMap() {
        Map<String, String> map = new HashMap<>();
        map.put("fixed acidity", "fixed_acidity");
        map.put("volatile acidity", "volatile_acidity");
        map.put("citric acid", "citric_acid");
        map.put("residual sugar", "residual_sugar");
        map.put("chlorides", "chlorides");
        map.put("free sulfur dioxide", "free_sulfur_dioxide");
        map.put("total sulfur dioxide", "total_sulfur_dioxide");
        map.put("density", "density");
        map.put("pH", "pH");
        map.put("sulphates", "sulphates");
        map.put("alcohol", "alcohol");
        return map;
    }

    private Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();

    public SpecificPMMLModel(byte[] input) throws Throwable{

        super(input);
    }

    @Override
    public Object score(Object input) {
        Winerecord.WineRecord inputs = (Winerecord.WineRecord) input;
        arguments.clear();
        for(InputField field : inputFields){
           arguments.put(field.getName(), field.prepare(getValueByName(inputs,field.getName().getValue())));
        }

        // Calculate Output// Calculate Output
        Map<FieldName, ?> result = evaluator.evaluate(arguments);

        // Prepare output
        double rv = 0;
        Object tresult = result.get(tname);
        if(tresult instanceof Computable){
            String value = ((Computable)tresult).getResult().toString();
            rv = Double.parseDouble(value);
        }
        else
            rv = (Double)tresult;
        return rv;
    }

    // Get variable value by  name
    private double getValueByName(Winerecord.WineRecord input, String name){
        Descriptors.FieldDescriptor descriptor =  input.getDescriptorForType().findFieldByName(names.get(name));
        return (double)input.getField(descriptor);
    }
}