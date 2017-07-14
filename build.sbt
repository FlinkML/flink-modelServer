name := "FlinkModelServer"

version := "1.0"

scalaVersion := "2.11.11"

lazy val protobufs = (project in file("./protobufs"))
  .settings(
    PB.targets in Compile := Seq(
    scalapb.gen() -> (sourceManaged in Compile).value
  ))

lazy val client = (project in file("./client"))
  .settings(libraryDependencies ++= Seq(Dependencies.kafka))
  .dependsOn(protobufs, configuration)

lazy val model = (project in file("./model"))
  .settings(libraryDependencies ++= Dependencies.modelsDependencies ++ Seq(Dependencies.kryo))
  .dependsOn(protobufs)

lazy val query = (project in file("./query"))
  .settings(libraryDependencies ++= Dependencies.flinkDependencies ++ Seq(Dependencies.joda))
  .dependsOn(model)

lazy val server = (project in file("./server"))
  .settings(libraryDependencies ++= Dependencies.flinkDependencies ++ Dependencies.modelsDependencies)
  .dependsOn(model, configuration)

lazy val configuration = (project in file("./configuration"))

lazy val root = (project in file(".")).
  aggregate(protobufs, query, client, model, configuration, server)