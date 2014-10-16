import com.datastax.spark.connector.SomeColumns
import com.datastax.spark.connector.streaming._
import com.solarcity.flare.MQTTUtilsCustom
import org.apache.log4j.{Level, Logger}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.SparkConf
import org.joda.time.DateTime
import org.json4s.{JValue, DefaultFormats}
import org.json4s.jackson.JsonMethods._

object receiver {

  case class Measurement(timeStamp:Long, values:Array[(String, Double)])

  def toKeyedMeasurement(js: JValue): (String,Measurement) = {
    implicit val formats = DefaultFormats
    val s = new DateTime( ((js\"Messages")(0)\"Body"\"Status"\"Measured").extract[String] ).getMillis
    val v = ((js\"Messages")(0)\"Body"\"Values").values.asInstanceOf[Map[String,Double]].toArray
    val k = ((js\"Messages")(0)\"Envelope"\"Topic").extract[String].split("/").last
    (k, Measurement(s,v))
  }

  def main(args: Array[String]) {

    Logger.getRootLogger.setLevel(Level.ERROR)

    if (args.length < 2) {
      System.err.println(
        "Usage: receiver <MqttBrokerUrl> <topic>")
      System.exit(1)
    }

    val sparkConf = new SparkConf(true).set("spark.cassandra.connection.host", "localhost")
                                       .setJars(Array("target/scala-2.10/receiver-assembly-1.0.jar"))
                                       .setAppName("receiver")
                                       .setMaster("spark://172.31.1.42:7077")

    println("Creating MQTT input stream")
    val Seq(brokerUrl, topic) = args.toSeq
    val ssc = new StreamingContext(sparkConf, Seconds(2))
    val stream = MQTTUtilsCustom.createStream(ssc, brokerUrl, topic)

    println("Parsing messages in stream")
    stream.count().print()
    stream.map(parse(_)).map(toKeyedMeasurement).print()
    // TODO: interpolate andThen saveToCassandra

    ssc.start()
    ssc.awaitTermination()
  }
}
