package mqtt.iot.model;

public class sensData {

	private String s_id;
	private String s_date;
	private String s_time;
	private String s_val;

	/**
	 * @param s_id
	 *            センサID
	 * @param s_dtime
	 *            タイムスタンプ(yyyy/MM/dd HH:mm:ss)
	 * @param s_val
	 *            値
	 */
	public sensData(String s_id, String s_date, String s_time, String s_val) {
		super();
		this.s_id = s_id;
		this.s_date = s_date;
		this.s_time = s_time;
		this.s_val = s_val;
	}

	public sensData() {
		super();
	}

	public String getS_id() {
		return s_id;
	}

	public void setS_id(String s_id) {
		this.s_id = s_id;
	}

	public String getS_date() {
		return s_date;
	}

	public void setS_date(String s_date) {
		this.s_date = s_date;
	}

	public String getS_time() {
		return s_time;
	}

	public void setS_time(String s_time) {
		this.s_time = s_time;
	}

	public String getS_val() {
		return s_val;
	}

	public void setS_val(String s_val) {
		this.s_val = s_val;
	}

	public String toString() {
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		// String dtime = sdf.format(s_dtime.getTime());

		return s_id + ", " + s_date + ", " + s_time + ", " + s_val;
	}

	public String toJSON() {
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		// String dtime = sdf.format(s_dtime.getTime());

		return "{\"s_id\":\"" + s_id + "\", \"s_date\":\"" + s_date + "\", \"s_time\":\"" + s_time + "\" \"s_val\":" + s_val + "}";
	}

}
