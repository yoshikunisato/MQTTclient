package mqtt.iot.broker;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class Subscriber {
	private MqttClient client;
	
	/**
	 * ブローカーに接続して指定トピックのメッセージを受信します。 ENTERキーを押すまで待機し続けます。
	 * 
	 * @param callback コールバックオブジェクト
	 * @param broker ブローカー
	 * @param clientId クライアントID
	 * @param サブスクライブするトピック（ワイルドカードも利用可）
	 * @param このサブスクリプションで利用するQOSの最大値（これを超えるものは下げて受信される）
	 * @throws MqttException
	 */
	public void subscribe(MqttCallback callback, String broker, String clientId, String topic, int qos) throws MqttException {
		log("Initializing");
		client = new MqttClient(broker, clientId, new MemoryPersistence());
		client.setCallback(callback);
		MqttConnectOptions connOpts = new MqttConnectOptions();
		// QoSに沿った耐障害性の高い配信を行うためには、falseにセット
		connOpts.setCleanSession(false);

		log("Connecting to broker: " + broker + ": clientId [" + clientId + "]");
		client.connect(connOpts);
		log("Connected");

		log("Subscribing to topic [" + topic + "]");
		client.subscribe(topic, qos);
	}

	public void finalze() {
		try {
			client.disconnect();
		} catch (MqttException e) {
			e.printStackTrace();
		}
		log("Disconnected");
	}
	
	private static void log(String message) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
		System.out.println(sdf.format(new GregorianCalendar().getTime()) + " " + message);
	}
}
