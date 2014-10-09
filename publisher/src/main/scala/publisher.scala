import org.eclipse.paho.client.mqttv3.{MqttClient, MqttClientPersistence, MqttException, MqttMessage, MqttTopic,MqttConnectOptions}
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence

/**
 * A simple Mqtt publisher for demonstration purposes, repeatedly publishes
 * Space separated String Message "hello mqtt demo for spark streaming"
 */
object MQTTPublisher {

  var client: MqttClient = _

  def main(args: Array[String]) {
    println("args length: " + args.length)
    if (args.length < 2) {
      System.err.println("Usage: MQTTPublisher <MqttBrokerUrl> <topic>")
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

    val msgtopic: MqttTopic = client.getTopic(topic)
    val msg: String = "hello mqtt demo for spark streaming"

    println("Starting message loop")
    while (true) {
      val message: MqttMessage = new MqttMessage(String.valueOf(msg).getBytes("utf-8"))
      msgtopic.publish(message)
      //println("Published data. topic: " + msgtopic.getName + " Message: " + message)
    }
   client.disconnect()
  }
}
