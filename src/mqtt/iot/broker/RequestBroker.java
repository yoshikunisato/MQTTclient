package mqtt.iot.broker;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;

import mqtt.iot.model.sensData;
import net.arnx.jsonic.JSON;

public class RequestBroker implements MqttCallback {
	// 日付変換
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	// Cassandra ホスト
	//private final String cashost = "ec2-52-193-198-108.ap-northeast-1.compute.amazonaws.com";
	private final String cashost = "cassandra.japanwest.cloudapp.azure.com";
	// Cassandra キースペース
	private final String keyspace = "iot";
	// Cassandra ユーザ
	private final String cass_user = "iotapp";
	// Cassandra パスワード
	private final String cass_pass = "pwdiotapp";
	// Cassandra セッション
	private Session session;
	// Cassandra CQLステートメント
	private BoundStatement boundStatement;

	// MQTT brokerホスト
	//private final String broker = "tcp://ec2-52-192-251-45.ap-northeast-1.compute.amazonaws.com:1883";
	private final String broker = "tcp://mqtt.japanwest.cloudapp.azure.com:1883";
	// MQTT トピック
	private final String topic = "sens";
	// QoSレベル
	private final int qos = 2;
	// クライアントの識別子
	private String clientId;
	
	// MQTTへのサブスクライブ ユーティリティクラス
	private static Subscriber subscriber = new Subscriber();

	public static void main(String[] args) {
		new RequestBroker();
	}

	public RequestBroker() {
		// クライアントの識別子
		clientId = this.getClass().getName() + "." + UUID.randomUUID().toString();

		try {
			// Cassandra 接続
			initializeCassandraSession();
			// MQTTにSubscribe
			subscriber.subscribe(this, broker, clientId, topic, qos);
		} catch (MqttException me) {
			// Display full details of any exception that occurs
			log("reason " + me.getReasonCode());
			log("msg " + me.getMessage());
			log("loc " + me.getLocalizedMessage());
			log("cause " + me.getCause());
			log("excep " + me);
			me.printStackTrace();
		}

	}

	/**
	 * Cassandra Sessionの準備
	 */
	private void initializeCassandraSession() {
		// Session準備
		CassandraSession cs = new CassandraSession(cashost, keyspace, cass_user, cass_pass);
		session = cs.getSession();
		// PreparedStatement準備(インジェクション防止)
		PreparedStatement pstmt = session.prepare("insert into sens_by_day(s_id, s_date, s_time, s_val)  values(?, ?, ?, ?);");
		boundStatement = new BoundStatement(pstmt);
	}

	/**
	 * メッセージを受信したときに呼ばれるCallback。SkyOnDemandではスクリプトにメッセージを渡す処理を行う。
	 */
	@Override
	public void messageArrived(String topic, MqttMessage message) throws MqttException {
		String time = new Timestamp(System.currentTimeMillis()).toString();
		String payload = new String(message.getPayload());
		log(time + "\tMessage: " + payload);

		// 受信したJSONをオブジェクトにデコード
		sensData sensdata = JSON.decode(payload, sensData.class);
		// データを登録
		this.insertToCassandra(sensdata);
	}

	/**
	 * Cassandraにデータを登録する
	 * 
	 * @param _sensdata
	 */
	private void insertToCassandra(sensData _sensdata) {
		try {
			String s_id = _sensdata.getS_id();
			String s_date = _sensdata.getS_date();
			String s_time = _sensdata.getS_time();
			String s_val = _sensdata.getS_val();
			// Date型変換
			// 参考：https://docs.datastax.com/en/latest-java-driver/java-driver/reference/javaClass2Cql3Datatypes.html?local=true&nav=toc
			Date formatDate = sdf.parse(s_time);

			// Bind
			boundStatement.bind(s_id, s_date, formatDate, s_val);

			// Insert one record into the users table
			session.execute(boundStatement);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ブローカーとの接続が失われた時に呼ばれるCallback
	 * (なぜかAzureは)結構切れるので再接続処理を入れる
	 */
	@Override
	public void connectionLost(Throwable cause) {
		log("ERROR: Connection lost: " + cause.toString());
		try {
			// 現在のSubscribeをfinalize
			subscriber.finalze();
			subscriber = null;

			// Subscriberを再度生成
			log("INFO: Try to Subscribe to MQTT again.");
			subscriber = new Subscriber();
			// 再度Subscribe
			subscriber.subscribe(this, broker, clientId, topic, qos);
			log("INFO: done.");
		} catch (MqttException me) {
			log("Re-Subscribe fail. stop execution.");
			// Display full details of any exception that occurs
			log("reason " + me.getReasonCode());
			log("msg " + me.getMessage());
			log("loc " + me.getLocalizedMessage());
			log("cause " + me.getCause());
			log("excep " + me);
			me.printStackTrace();
			// 再Subscribeが失敗したらあきらめる
			System.exit(1);
		}
	}

	/**
	 * メッセージの送信が完了したときに呼ばれるCallback。
	 */
	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// will not be called in this demo
		log("Delivery complete");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	public void finalize() throws Throwable {
		super.finalize();
	}

	private static void log(String message) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
		System.out.println(sdf.format(new GregorianCalendar().getTime()) + " " + message);
	}
}
