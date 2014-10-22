SolarFlare
============

MQTT -> Spark Streaming -> Cassandra. Prototype for Proof of Concept.
Base project is split into 2 subprojects: `publisher` and `receiver`

### Build

Requires SBT 0.13.x. Instructions below assume `sbt` sits in `~/sbt/bin/sbt`. 
To build publisher project:
```
$ cd solar-flare/publisher
$ sbt assembly
```

and to build receiver project
```
$ cd solar-flare/receiver
$ sbt assembly
```

### Usage

In the base `solar-flare` project directory, run publisher:
```
$ ./run-publisher
```

Run receiver:
```
$ ./run-receiver
```
