import com.datastax.spark.connector.SomeColumns
import com.datastax.spark.connector.streaming._
import com.solarcity.flare.MQTTUtilsCustom
import org.apache.log4j.{Level, Logger}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.SparkConf

object MQTTWordCount {

  def main(args: Array[String]) {

    Logger.getRootLogger.setLevel(Level.ERROR)

    if (args.length < 2) {
      System.err.println(
        "Usage: MQTTWordCount <MqttBrokerUrl> <topic>")
      System.exit(1)
    }

    val sparkConf = new SparkConf(true).set("spark.cassandra.connection.host", "localhost")
                                       .setJars(Array("target/scala-2.10/receiver-assembly-1.0.jar"))
                                       .setAppName("MQTTWordCount")
                                       .setMaster("spark://172.31.1.42:7077")

    println("Creating MQTT input stream")
    val Seq(brokerUrl, topic) = args.toSeq
    val ssc = new StreamingContext(sparkConf, Seconds(2))
    val stream = MQTTUtilsCustom.createStream(ssc, brokerUrl, topic)

    println("Counting words in stream")
    val words = stream.flatMap(x => x.toString.split(" "))
    val wordCounts = words.map(x => (x, 1)).reduceByKey(_ + _)

    wordCounts.print()
    wordCounts.saveToCassandra("test", "wordcount", SomeColumns("word", "count"))

    ssc.start()
    ssc.awaitTermination()
  }
}
