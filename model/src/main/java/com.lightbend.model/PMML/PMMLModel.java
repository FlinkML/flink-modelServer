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

package com.lightbend.model.PMML;

import com.google.protobuf.Descriptors;
import com.lightbend.model.Model;
import com.lightbend.model.Modeldescriptor;
import com.lightbend.model.Winerecord;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.dmg.pmml.Visitor;
import org.jpmml.evaluator.*;
import org.jpmml.evaluator.visitors.*;
import org.jpmml.model.PMMLUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

/**
 * Created by boris on 5/18/17.
 */
public class PMMLModel implements Model {

    private static List<? extends Visitor> optimizers = Arrays.asList(new ExpressionOptimizer(), new FieldOptimizer(), new PredicateOptimizer(), new GeneralRegressionModelOptimizer(), new NaiveBayesModelOptimizer(), new RegressionModelOptimizer());

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

    private PMML pmml;
    private Evaluator evaluator;
    private FieldName tname;
    private List<InputField> inputFields;
    private Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();

    public PMMLModel(byte[] input) throws Throwable{
        // unmarshal PMML
        pmml = PMMLUtil.unmarshal(new ByteArrayInputStream(input));
        // Optimize model
        synchronized(this) {
            for (Visitor optimizer : optimizers) {
                try {
                    optimizer.applyTo(pmml);
                }catch (Throwable t){
                   // Swallow it
                }
            }
        }

        // Create and verify evaluator
        ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
        evaluator = modelEvaluatorFactory.newModelEvaluator(pmml);
        evaluator.verify();

        // Get input/target fields
        inputFields = evaluator.getInputFields();
        TargetField target = evaluator.getTargetFields().get(0);
        tname = target.getName();
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

    @Override
    public void cleanup() {
        // Do nothing

    }

    // Get variable value by  name
    private double getValueByName(Winerecord.WineRecord input, String name){
        Descriptors.FieldDescriptor descriptor =  input.getDescriptorForType().findFieldByName(names.get(name));
        return (double)input.getField(descriptor);
    }

    @Override
    public byte[] getBytes() {
        ByteArrayOutputStream ous = new ByteArrayOutputStream();
        try {
            PMMLUtil.marshal(pmml, ous);
        }
        catch(Throwable t){
            t.printStackTrace();
        }
        return ous.toByteArray();
    }

    @Override
    public long getType() {
        return (long) Modeldescriptor.ModelDescriptor.ModelType.PMML.getNumber();
    }
}