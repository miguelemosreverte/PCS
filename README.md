# Project structure
* /  
  *  documentation/
      *   ...
  * assets/  
      * CI/    
      * docker-compose/    
      * examples/    
      * k8s/   
      * scripts/  
  * common/  
      * src/  
      * test/  
  * pcs/  
      * src/  
      * test/  
  * project/  
      * build.properties  
      * plugins.sbt  
  * readside/  
      * src/  
      * test/  
  * build.sbt  
  
**documentation** stores any document that explains the project.
**assets** stores any document useful to the project.
**common** stores any useful abstraction shared between two projects.
**writeside or pcs** is where the event database is written.
It hears kafka topics and saves to the event database anything important.
**readside** is where the event database is read, and then some human readable view can be created on top. 
It hears raw events and transforms them into human readable presentations.

## Run test  
Sometimes the best way to understand a project is to run the tests-
```bash 
sbt test
 ```
 
# How to run
## bash
Using **bash** is useful when you want _performance_. You want the result _now_.
So you make all the effort of typing the path of the script.

#### A complete project demonstration
```bash 
sh assets/docker-compose/vm/start_all.sh
 ```
This demo is very resource demanding. It will start the following containers:
- 3 cassandra 
-  1 kafka|zookeper
-  1 logstash|kibana|elasticsearch.

#### A basic project demonstration
This will start Kafka and Cassandra
```bash 
sh it/src/main/resources/infrastructure.sh
 ```
This will hit Kafka with some topics like ObligacionesTri
```bash 
sh it/src/main/resources/examples.sh
 ```
 This will start the writeside
```bash 
sh it/src/main/resources/writeside.sh
 ```
 This will start the readside
```bash 
sh it/src/main/resources/readside.sh
 ```
This demo runs the fastest. Everything about it is made to run _at speed_ for your advantage as a developer. Use it for when you want to test changes on your application but don't want to bother documenting this piece of behaviour via tests -- example: _When you are proving serialization works as intended._

You can just run the _infrastructure_ script alonside with the _writeside_ script in order to do so.

## sbt  
Using **sbt** is useful when you want _to write nothing_.  And you have all the time in the world.
So you make no effort in typing the path of the script.

This will start Kafka and Cassandra
```bash 
 sbt infrastructure  
 ```
 
  This will create the tables needed by readside  inside the Cassandra
```bash 
 sbt createTables  
 ```
 
  This will start the readside
```bash 
 sbt readside
 ```

  This will start the writeside
```bash 
 sbt writeside
 ```

This will hit Kafka with some topics like ObligacionesTri
```bash 
 sbt kafkacat  
 ```
