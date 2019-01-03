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

import com.lightbend.model.Model;
import com.lightbend.model.Modeldescriptor;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.dmg.pmml.Visitor;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.evaluator.TargetField;
import org.jpmml.evaluator.visitors.*;
import org.jpmml.model.PMMLUtil;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

// Base PMML processing
public abstract class PMMLModel implements Model {

    // List of optimizerss
    private static List<? extends Visitor> optimizers = Arrays.asList(new ExpressionOptimizer(), new FieldOptimizer(), new PredicateOptimizer(), new GeneralRegressionModelOptimizer(), new NaiveBayesModelOptimizer(), new RegressionModelOptimizer());

    // PMML model
    protected PMML pmml;
    // PMML Evaluator
    protected Evaluator evaluator;
    // Result field name
    protected FieldName tname;
    // Input fields names
    protected List<InputField> inputFields;
    // Byte array
    protected byte[] bytes;

    public PMMLModel(byte[] input) throws Throwable{
        // Save bytes
        bytes = input;
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
    public void cleanup() {
        // Do nothing

    }

    @Override
    public byte[] getBytes() { return bytes; }

    @Override
    public long getType() {
        return (long) Modeldescriptor.ModelDescriptor.ModelType.PMML.getNumber();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PMMLModel) {
            return Arrays.equals(((PMMLModel)obj).getBytes(), bytes);
        }
        return false;
    }
}