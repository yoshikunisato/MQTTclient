package mqtt.iot.sample;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import mqtt.iot.model.sensData;
import net.arnx.jsonic.JSON;

public class JSONsample {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"));
		SimpleDateFormat fday = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat ftime = new SimpleDateFormat("HH:mm:ss");
		String date = fday.format(cal.getTime());
		String time = ftime.format(cal.getTime());
		
		sensData data = new sensData("s001.home", date, time, "100.1");

//		System.out.println("Data: "+data.toString());
		System.out.println("Data: "+data.toJSON());
		
		String json = JSON.encode(data);
//		String json = data.toJSON();
		
		System.out.println("JSON: "+json);
		
		sensData newdata = JSON.decode(json, sensData.class);
		System.out.println("NewData: "+newdata.toString());

	}

}
