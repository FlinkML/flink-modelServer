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

import com.lightbend.model.PMML.PMMLModel;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.InputField;

import java.util.List;

// PMML implementation for testing
public class SimplePMMLModel extends PMMLModel {


    public SimplePMMLModel(byte[] input) throws Throwable{

        super(input);
    }

    // Scoring method
    @Override
    public Object score(Object input) {
        // Just for test
        return null;
    }

    // Getters for validation
    public PMML getPmml() {
        return pmml;
    }

    public Evaluator getEvaluator() {
        return evaluator;
    }

    public FieldName getTname() {
        return tname;
    }

    public List<InputField> getInputFields() {
        return inputFields;
    }
}