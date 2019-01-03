package com.lightbend.modelserver.java.typeschema;

import com.lightbend.model.*;
import org.apache.flink.api.common.typeutils.SerializerTestBase;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.junit.BeforeClass;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public class ModelWithTypeSerializerTest extends SerializerTestBase<ModelWithType>{

    private static String tfmodeloptimized = "model/TF/optimized/optimized_WineQuality.pb";
    private static String tfmodelsaved = "model/TF/saved/";
    private static String pmmlmodel = "model/PMML/winequalityDecisionTreeClassification.pmml";

    private static String dataType = "wine";

    private static byte[] defaultdata = new byte[0];

    @BeforeClass
    public static void oneTimeSetUp() {
        // Set resolver
        DataConverter.setResolver(new SimpleFactoryResolver());
    }

    @Override
    protected TypeSerializer<ModelWithType> createSerializer() { return new ModelWithTypeSerializer(); }

    @Override
    protected int getLength() { return -1; }

    @Override
    protected Class<ModelWithType> getTypeClass() { return ModelWithType.class; }

    @Override
    protected ModelWithType[] getTestData() {
        // Get PMML model from File
        byte[] model = getModel(pmmlmodel);
        // Create model from binary
        Model pmml = DataConverter.restore(Modeldescriptor.ModelDescriptor.ModelType.PMML.getNumber(), model);
        // Get TF Optimized model from file
        model = getModel(tfmodeloptimized);
        // Create model from binary
        Model tfoptimized = DataConverter.restore(Modeldescriptor.ModelDescriptor.ModelType.TENSORFLOW.getNumber(), model);
        // Get TF bundled model location
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(tfmodelsaved).getFile());
        String location = file.getPath();
        // Create model from location
        Model tfbundled = DataConverter.restore(Modeldescriptor.ModelDescriptor.ModelType.TENSORFLOWSAVED.getNumber(), location.getBytes());
        return new ModelWithType[]{new ModelWithType(false, dataType, Optional.of(pmml)),
                new ModelWithType(false, dataType, Optional.of(tfoptimized)),
                new ModelWithType(false, dataType, Optional.of(tfbundled))};
    }

    private byte[] getModel(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            File file = new File(classLoader.getResource(fileName).getFile());
            return Files.readAllBytes(Paths.get(file.getPath()));
        } catch (Throwable t) {
            return defaultdata;
        }
    }

}
