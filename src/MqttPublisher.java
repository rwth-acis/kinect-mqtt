import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttPublisher {
    MqttClient client = null;

    public void connect(String broker, String clientID) {
        try {
            client = new MqttClient(broker, clientID, null);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            client.connect(connOpts);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publish(String topic, MqttMessage message) {
        if (client != null) {
            try {
                client.publish(topic, message.getPayload(), 0, false);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
}
