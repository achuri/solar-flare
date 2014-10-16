import org.eclipse.paho.client.mqttv3.{MqttClient, MqttClientPersistence, MqttException, MqttMessage, MqttTopic,MqttConnectOptions}
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence
import org.joda.time.{DateTime, DateTimeZone}
import scala.util.Random

/**
 * A simple Mqtt publisher for demonstration purposes, repeatedly publishes
 * json messages serialized into utf-8 strings
 */
object publisher {

  val rng = new Random()
  val meterAssetIDs = (1 to 25).map("Meter"+_.toString)

  var client: MqttClient = _

  def runPublisher(msgtopic: MqttTopic): Unit = {
    println("Starting message loop")
    while (true) {
      val randIndex = rng.nextInt(meterAssetIDs.length/2)
      val meters = rng.nextDouble() match {
        case d if d <= 0.5 => meterAssetIDs.drop(randIndex)
        case d if d >= 0.5 => meterAssetIDs.dropRight(randIndex)
      }
      for (m <- meters) {
        val ts = new DateTime(DateTimeZone.UTC).toString()
        val kw   = rng.nextDouble*6000
        val pf   = rng.nextDouble*1
        val temp = rng.nextDouble*100
        val rx   = rng.nextDouble*600
        val msg = s"""
        {
           "Messages":[
              {
                 "Body":{
                    "Status":{
                       "DeviceType":"PowerBlaster",
                       "Measured":"$ts"
                    },
                    "Values":{
                       "RealPower": $kw,
                       "PowerFactor": $pf,
                       "Temperature": $temp,
                       "ReactivePower": $rx
                    }
                 },
                 "Envelope":{ "Topic":"/device/measurement/realtime/post/$m" }
              }
           ]
        }"""
        val message: MqttMessage = new MqttMessage(String.valueOf(msg).getBytes("utf-8"))
        msgtopic.publish(message)
      }
      Thread.sleep(math.abs(rng.nextLong % 2000))
    }
  }

  def main(args: Array[String]) {
    println("args length: " + args.length)

    if (args.length < 2) {
      System.err.println("Usage: publisher <MqttBrokerUrl> <topic>")
      System.exit(1)
    }

    val Seq(brokerUrl, topic) = args.toSeq
    val options: MqttConnectOptions = new MqttConnectOptions()

    try {
      println("Trying to connect")
      val persistence: MqttClientPersistence = new MqttDefaultFilePersistence("/tmp")
      client = new MqttClient(brokerUrl, MqttClient.generateClientId(), persistence)

      println("Setting username and password")
      options.setUserName("admin")
      options.setPassword("solarguard".toCharArray)
    } catch {
      case e: MqttException => println("Exception Caught: " + e)
    }

    client.connect(options)
    runPublisher(client.getTopic(topic))
  }
}
