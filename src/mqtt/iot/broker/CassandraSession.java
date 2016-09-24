package mqtt.iot.broker;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.datastax.driver.core.Session;

/**
 * Cassandra Session 管理 クラス
 * @author yoshikuni
 *
 */
public class CassandraSession {
	String cass_user;
	String cass_pass;
	String cashost;
	int casport;
	String keyspace;
	Session session;

	/**
	 * Cassandra Session 管理
	 * @param _cashost
	 * @param _keyspace
	 */
	public CassandraSession(String _cashost, int _casport, String _keyspace, String _cass_user, String _cass_pass) {
		cashost = _cashost;
		casport = _casport;
		keyspace = _keyspace;
		cass_user = _cass_user;
		cass_pass = _cass_pass;

		this.initialize();
	}

	/**
	 * Cassandra Session 初期化
	 */
	private void initialize() {

		Cluster cluster;

		try {
			// Connect to the cluster and keyspace
			AuthProvider auth = new PlainTextAuthProvider(cass_user, cass_pass);
			cluster = Cluster.builder().addContactPoint(cashost).withPort(casport).withAuthProvider(auth).build();
			session = cluster.connect(keyspace);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Cassandra Session 取得
	 * @return
	 */
	public Session getSession() {
		if (null == session) {
			this.initialize();
		}

		return session;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	public void finalize() {
		try {
			session.close();
			session = null;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			session = null;
		}

	}
}
