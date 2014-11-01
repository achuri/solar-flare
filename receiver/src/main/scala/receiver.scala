import breeze.linalg.DenseVector
import breeze.interpolation.LinearInterpolator
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

  case class Measurement(timeStamp:Long, values:Map[String, Double])

  def toKeyedMeasurement(js: JValue): (String,Measurement) = {
    implicit val formats = DefaultFormats
    val s = new DateTime( ((js\"Messages")(0)\"Body"\"Status"\"Measured").extract[String] ).getMillis
    val v = ((js\"Messages")(0)\"Body"\"Values").values.asInstanceOf[Map[String,Double]]
    val k = ((js\"Messages")(0)\"Envelope"\"Topic").extract[String].split("/").last
    (k, Measurement(s,v))
  }

  def interpolateStream(tup: (String, Seq[Measurement])) = {
    val measurements = tup._2
    if (measurements.length > 1) {
      val x = measurements.map(_.timeStamp).map(_.toDouble).toArray
      val y = measurements.map(_.values).flatMap(_.get("RealPower")).toArray
      val interpFunc = LinearInterpolator( DenseVector(x), DenseVector(y) )
      tup._1 + ": " + interpFunc(System.currentTimeMillis.toDouble)
    }
  }

  def main(args: Array[String]) {

    Logger.getRootLogger.setLevel(Level.ERROR)

    if (args.length < 3) {
      System.err.println("Usage: receiver <SparkMasterUrl> <MqttBrokerUrl> <topic>")
      System.exit(1)
    }
    
    val Seq(sparkMasterUrl, brokerUrl, topic) = args.toSeq
    val sparkConf = new SparkConf(true).set("spark.cassandra.connection.host", "localhost")
                                       .setJars(Array("target/scala-2.10/receiver-assembly-1.0.jar"))
                                       .setAppName("receiver")
                                       .setMaster(sparkMasterUrl)

    println("Creating MQTT input stream")
    val ssc = new StreamingContext(sparkConf, Seconds(2))
    val stream = MQTTUtilsCustom.createStream(ssc, brokerUrl, topic)

    println("Parsing out data from stream and interpolating")
    stream.count().print()
    //stream.map(parse(_)).map(toKeyedMeasurement).print()

    // TODO: interpolate andThen saveToCassandra
    stream.window(Seconds(10))
      .map(parse(_))
      .map(toKeyedMeasurement)
      .groupByKey()
      .map(interpolateStream)
      .print()

    ssc.start()
    ssc.awaitTermination()
  }
}
