# Flink Model server

This is a simple implementation of model serving using Flink
The basic idea behind this implementation is fairly straightforward - there are two streams:

-**Data Stream** - Kafka stream delivering data record as protobuf buffer (example, modeldescriptor.proto)

-**Model Stream** - Kafka stream delivering models as protobuf buffer (example, modeldescriptor.proto)

THe implementation is based on [Dynamically controlled Streams](https://www.data-artisans.com/blog/bettercloud-dynamic-alerting-apache-flink)

The project contains 2 implementations of model server:

-**ModelServingKeyedJob** - This little application is based on a RichCoProcessFunction which works on a keyed streams. It is applicable
   when a single applications serves multiple different models for different data types. Every model is keyed with
   the type of data what it is designed for. Same key should be present in the data, if it wants to use a specific model.
   Scaling of the application is based on the data type - for every key there is a separate instance of the
   RichCoProcessFunction dedicated to this type. All messages of the same type are processed by the same instance
   of RichCoProcessFunction

-**ModelServingFlatJob** - This little application is based on a RichCoFlatMapFunction which works on a non keyed streams. It is
   applicable when a single applications serves a single model(model set) for a single data type.
   Scaling of the application is based on the parallelism of input stream and RichCoFlatMapFunction.
   The model is broadcasted to all RichCoFlatMapFunction instances. The messages are processed by different
   instances of RichCoFlatMapFunction in a round-robin fashion.
   
The project also contains 3 additional supporting applications
   
-**Clientr** - Small application reading data from winequality_red.csv and continiously submitting it to a Kafka topic,
thus emulating continious data Stream and reading PMML mode definitions (files with .pmml extension) and continiously
publishing them to the model stream
   
-**ModelStateQuery** - Small application demonstrating external access to a Flink queryable data, containing
current model state. It works only for ModelServingKeyedJob (queryable state is supported only for keyed stream)

-**Wine Serving** - An application leveraging underlying machinery to build model serving application.
