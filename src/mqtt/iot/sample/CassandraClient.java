package mqtt.iot.sample;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class CassandraClient {

	public static void main(String[] args) {
		new CassandraClient();
	}

	public CassandraClient() {
		String cashost = "ec2-52-193-198-108.ap-northeast-1.compute.amazonaws.com";
		String keyspace = "iot";

		Cluster cluster;
		Session session;

		// Connect to the cluster and keyspace "demo"
//		cluster = Cluster.builder().addContactPoint(cashost).build();
		AuthProvider auth = new PlainTextAuthProvider("iotapp", "pwdiotapp");
		cluster = Cluster.builder().addContactPoint(cashost).withAuthProvider(auth).build();
		session = cluster.connect(keyspace);

		// INSERT Test
		this.insert(session);

		// SELECT Test
		this.select(session);

		// // Update the same user with a new age
		// session.execute("update users set age = 36 where lastname =
		// 'Jones'");
		//
		// // Select and show the change
		// results = session.execute("select * from users where
		// lastname='Jones'");
		// for (Row row : results) {
		// System.out.format("%s %d\n", row.getString("firstname"),
		// row.getInt("age"));
		// }
		//
		// // Delete the user from the users table
		// session.execute("DELETE FROM users WHERE lastname = 'Jones'");
		//
		// // Show that the user is gone
		// results = session.execute("SELECT * FROM users");
		// for (Row row : results) {
		// System.out.format("%s %d %s %s %s\n", row.getString("lastname"),
		// row.getInt("age"), row.getString("city"),
		// row.getString("email"), row.getString("firstname"));
		// }

		// Clean up the connection by closing it
		cluster.close();
	}

	private void insert(Session session) {
		try {
			PreparedStatement pstmt = session.prepare("insert into sens_by_day(s_id, s_date, s_time, s_val)  values(?, ?, ?, ?);");
			BoundStatement boundStatement = new BoundStatement(pstmt);

			// Date型変換
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date formatDate = sdf.parse("2016-07-25 12:45:25");

			// Bind
			boundStatement.bind("s001.home", "2016-07-25", formatDate, "28.24");
			System.out.println("cql=" + pstmt.getQueryString());
			
			// Insert one record into the users table
			session.execute(boundStatement);
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

	private void select(Session session) {

		// Use select to get the user we just entered
		ResultSet results = session.execute("SELECT * FROM sens_by_day");
		for (Row row : results) {
			// System.out.format("%s %d\n", row.getString("s_id"),
			// row.getDecimal("s_val"));
			System.out.println(row);
		}

	}
}
