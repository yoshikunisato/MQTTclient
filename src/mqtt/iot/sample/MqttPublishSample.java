package mqtt.iot.sample;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttPublishSample {

    public static void main(String[] args) {
        // トピックはPublisherとSubscriberで同一である必要があります。
        String topic        = "sens";
        // 送信するデータ
        String content      = "こんにちは、IoT";
        // QoSレベル(0〜2。2が一番確実な伝送を実現する)
        int qos             = 2;
        // ブローカー。デフォルトでポート1883を使う。
        //String broker       = "tcp://ec2-52-192-251-45.ap-northeast-1.compute.amazonaws.com:1883";
        String broker       = "tcp://133.162.209.156:1883";
        // クライアントの識別子
        String clientId     = "Java Publish Client";
         
        try {
            log("Initializing");            
            MqttClient publishClient = new MqttClient(broker, clientId, new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            // QoSに沿った耐障害性の高い配信を行うためには、falseにセット
            connOpts.setCleanSession(true);
             
            log("Connecting to broker: " + broker);
            publishClient.connect(connOpts);
            log("Connected");
             
            log("Publishing message: " + content);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            publishClient.publish(topic, message);
            log("Message published");
             
            publishClient.disconnect();
            log("Disconnected");
        } catch(MqttException me) {
            log("reason "+me.getReasonCode());
            log("msg "+me.getMessage());
            log("loc "+me.getLocalizedMessage());
            log("cause "+me.getCause());
            log("excep "+me);
            me.printStackTrace();
        }
    }
     
    private static void log(String message) {
        System.out.println(message);
    }

}
