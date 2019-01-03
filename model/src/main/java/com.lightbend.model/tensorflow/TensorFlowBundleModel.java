package com.lightbend.model.tensorflow;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.lightbend.model.Model;
import com.lightbend.model.Modeldescriptor;
import org.tensorflow.Graph;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.framework.*;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

// This is experimental implementation showing how to deal with bundle. The implementation leverages
// local file system access in both constructor and get tag method. A real implementation will use
// some kind of shared storage, for example S3, Minio, GKS, etc.

abstract public class TensorFlowBundleModel implements Model {

    // Tensorflow graph
    protected Graph graph;
    // Tensorflow session
    protected Session session;
    // Signatures
    protected  Map<String,Signature> signatures;
    // Tags
    protected List<String> tags;
    // Path;
    private String path;

    public TensorFlowBundleModel(byte[] inputStream){

        // Convert input into file path
        path = new String(inputStream);
        // get tags. We assume here that the first tag is the one we use
        tags = getTags(path);
        if(tags.size() > 0) {
            // get saved model bundle
            SavedModelBundle bundle = SavedModelBundle.load(path, tags.get(0));
            // get grapth
            graph = bundle.graph();
            // get metatagraph and signature
            MetaGraphDef metaGraphDef = null;
            try {
                metaGraphDef = MetaGraphDef.parseFrom(bundle.metaGraphDef());
                Map<String, SignatureDef> signatureMap = metaGraphDef.getSignatureDefMap();
                //  parse signature, so that we can use definitions (if necessary) programmatically in score method

                signatures = parseSignatures(signatureMap);
            } catch (Throwable e) {
                System.out.println("Error parcing metagraph for " + path);
                signatures = null;
            }
            // Create tensorflow session
            session = bundle.session();
        }
        else{
            graph = null;
            session = null;
            signatures = null;
        }
    }

    @Override
    public void cleanup() {
        if(session != null)
            session.close();
        if(graph != null)
            graph.close();
    }

    @Override
    public byte[] getBytes() {
        return path.getBytes();
    }

    @Override
    public long getType() {
        return (long) Modeldescriptor.ModelDescriptor.ModelType.TENSORFLOWSAVED_VALUE;
    }

//    private

    private Map<String, Signature> parseSignatures(Map<String, SignatureDef> signaturedefs ) {

        Map<String, Signature> signatures = new HashMap<>();
        for(Map.Entry<String, SignatureDef> entry : signaturedefs.entrySet()){
            Map<String, Field> inputs = parseInputOutput(entry.getValue().getInputsMap());
            Map<String, Field> outputs = parseInputOutput(entry.getValue().getOutputsMap());
            signatures.put(entry.getKey(), new Signature(inputs, outputs));
        }
        return signatures;
    }

    private Map<String, Field> parseInputOutput(Map<String, TensorInfo> inputOutputs){

        Map<String, Field> fields = new HashMap<>();
        for(Map.Entry<String, TensorInfo> entry : inputOutputs.entrySet()){
            String name = "";
            Descriptors.EnumValueDescriptor type = null;
            List<Integer> shape = new LinkedList<>();
            Map<Descriptors.FieldDescriptor, Object> cf = entry.getValue().getAllFields();
            for(Map.Entry<Descriptors.FieldDescriptor, Object> fieldentry : cf.entrySet()){
                if(fieldentry.getKey().getName().contains("shape")){
                    List<TensorShapeProto.Dim> dimensions = ((TensorShapeProto)fieldentry.getValue()).getDimList();
                    for(TensorShapeProto.Dim d : dimensions)
                        shape.add((int)d.getSize());
                    continue;
                }
                if(fieldentry.getKey().getName().contains("name")){
                    name = fieldentry.getValue().toString().split(":")[0];
                    continue;
                }
                if(fieldentry.getKey().getName().contains("dtype")){
                    type = (Descriptors.EnumValueDescriptor)fieldentry.getValue();
                    continue;
                }
             }
            fields.put(entry.getKey(), new Field(name,type,shape));
        }
        return fields;
    }

    // Get tags method. If you want a known tag overwrite this method to return a list (of one) with the required tag
    protected List<String> getTags(String directory) {
        File d = new File(directory);
        List<String> tags = new LinkedList<>();
        List<File> pbfiles = new LinkedList<>();
        if(d.exists() && d.isDirectory()){
            File[] contained = d.listFiles();
            for (File file : contained)
                if(file.getName().endsWith("pb") || file.getName().endsWith("pbtxt")) pbfiles.add(file);
        }
        try {
            if (pbfiles.size() > 0) {
                byte[] data = Files.readAllBytes(pbfiles.get(0).toPath());
                List<MetaGraphDef> graphs = SavedModel.parseFrom(data).getMetaGraphsList();
                for (MetaGraphDef graph : graphs) {
                    List<ByteString> bstrings = graph.getMetaInfoDef().getTagsList().asByteStringList();
                    for (ByteString bs : bstrings)
                        tags.add(bs.toStringUtf8());
                }
            }
        }
        catch(Throwable e){
            System.out.println("Error getting tags " + e);
        }

        return tags;
    }

    public static class Field {

        // Field name
        private String name;
        // Field type
        private Descriptors.EnumValueDescriptor type;
        // Field shape
        private List<Integer> shape;

        public Field(String name, Descriptors.EnumValueDescriptor type, List<Integer> shape){
            this.name = name;
            this.type = type;
            this.shape = shape;
        }

        public String getName() {
            return name;
        }

        public Descriptors.EnumValueDescriptor getType() {
            return type;
        }

        public List<Integer> getShape() {
            return shape;
        }
    }

    public static class Signature {
        private Map<String, Field> inputs;
        private Map<String, Field> outputs;

        public Signature(Map<String, Field> inputs, Map<String, Field> outputs){
            this.inputs = inputs;
            this.outputs = outputs;
        }

        public Map<String, Field> getInputs() {
            return inputs;
        }

        public Map<String, Field> getOutputs() {
            return outputs;
        }
    }
}
