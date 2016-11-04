package mqtt.iot.broker;

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

/**
 * MQTTにSubscribeしデバイスからPublishされたJSONデータをCassandraにinsertする
 * @author yoshikuni
 *
 */
public class RequestBroker implements MqttCallback {
	// 日付変換
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	// Cassandra ホスト
	//private final String cashost = "ec2-52-193-198-108.ap-northeast-1.compute.amazonaws.com";
	//private final String cashost = "cassandra.japanwest.cloudapp.azure.com";
	private final String cashost = "133.162.209.156";
	// Cassandra ポート
	private final int casport = 9042;
	// Cassandra キースペース
	private final String keyspace = "iot";
	// Cassandra ユーザ
	private final String cass_user = "iotapp";
	// Cassandra パスワード
	private final String cass_pass = "pwdiotapp";
	// Cassandra セッション管理
	private CassandraSession csession;
	// Cassandra セッション
	private Session session;
	// Cassandra CQLステートメント
	private BoundStatement boundStatement;

	// MQTT brokerホスト
	//private final String broker = "tcp://ec2-52-192-251-45.ap-northeast-1.compute.amazonaws.com:1883";
	//private final String broker = "tcp://mqtt.japanwest.cloudapp.azure.com:1883";
	private final String broker = "tcp://133.162.209.156:1883";
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
		csession = new CassandraSession(cashost, casport, keyspace, cass_user, cass_pass);
		session = csession.getSession();
		// PreparedStatement準備(インジェクション防止)
		PreparedStatement pstmt = session.prepare("insert into sens_by_day(s_id, s_date, s_time, s_val)  values(?, ?, ?, ?);");
		boundStatement = new BoundStatement(pstmt);
	}

	/**
	 * メッセージを受信したときに呼ばれるCallback。SkyOnDemandではスクリプトにメッセージを渡す処理を行う。
	 */
	@Override
	public void messageArrived(String topic, MqttMessage message) throws MqttException {
		String payload = new String(message.getPayload());
		log("INFO: Message: " + payload.trim());

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
			log("INFO: insert to cassandra: complete.");
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ブローカーとの接続が失われた時に呼ばれるCallback
	 * Cassandraセッションが切れた場合など、何らかの例外発生時にもcallbackされるみたい
	 * (なぜかAzureは)結構例外が出るので再接続処理を入れる
	 * TODO: callback時はデータ欠損するので再初期化後に再登録する仕組みが必要
	 */
	@Override
	public void connectionLost(Throwable cause) {
		log("ERROR: Connection lost: " + cause.toString());
		
		// Cassandra セッションを初期化
		try {
			log("INFO: Finalize and connect to Cassandra again.");
			// 現在のCassandra sessionをfinalize
			session.close();
			session = null;
			csession.finalize();
			csession = null;
			// 再度Cassandra sessionを準備
			initializeCassandraSession();
			log("INFO: done.");
		} catch (Exception e) {
			log("ERROR: Re-initialize cassandra session fail. stop execution.");
			log("excep "+e.toString());
			e.printStackTrace();
			// 失敗したらあきらめる
			System.exit(1);
		}

		// MQTT Subscriberを初期化
		try {
			log("INFO: Finalize and Subscribe to MQTT again.");
			// 現在のSubscribeをfinalize
			subscriber.finalze();
			subscriber = null;
			// Subscriberを再度生成
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
			// 失敗したらあきらめる
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
