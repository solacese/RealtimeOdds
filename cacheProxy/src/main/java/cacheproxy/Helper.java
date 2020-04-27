package cacheproxy;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Duration;

public class Helper {
	static public void pressEnter(String prompt) {
		printBanner(prompt);
		try {
			System.in.read();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	static public void printBanner(String banner) {
		int len = banner.length();
		banner = banner.replace(' ', '^');
		String b1 = banner.substring(0, len/2);
		String b2 = banner.substring(len/2, len);
		System.out.println(String.format("%40s%-40s", b1, b2).replace(' ', '-').replace('^', ' '));
	}
	
	static public String readableSize(double size) {
		// https://www.wikiwand.com/en/Kilobyte
		DecimalFormat df = new DecimalFormat("###,###.#");
		long K = 1000;
		long M = K*K;
		if (size >= M) {
			return df.format(size/M)+"MB";
		} else if (size >= K) {
			return df.format(size/K)+"kB";
		} else {
			return String.format("%dB", (int)size);
		}
	}

	static public String readableRate(double size, Duration elapsed) {
		// https://www.wikiwand.com/en/Kilobyte

		DecimalFormat df = new DecimalFormat("###,###.##"); 
		String rate = "*";
		String unit = "kB/s"; 
		double	kilo= 1000; // 1000 bytes
		long ms = elapsed.toMillis();
		if (0 == ms) {
			return rate;
		}
		
		double r_s = (size/kilo)*1000/ms; // k per second
		if(r_s > kilo) {
			r_s = r_s / kilo;
			unit = "MB/s"; // 1008*1000
		}
		rate = df.format(r_s) + unit;
				
		return rate;
	}

	static public String prettyDuration(long millis) {
  		long ONE_SECOND = 1000;
  		long ONE_MINUTE = ONE_SECOND * 60;
  		long ONE_HOUR = ONE_MINUTE * 60;
		long h=0,m=0,s=0;
		String dur = "";
		
		if (millis == 0){
			return "0ms";
		}

		if(millis<0){
			millis = Math.abs(millis);
			dur = "-";
		}

		if (millis > ONE_HOUR){
			h = millis / ONE_HOUR;
			millis = millis % ONE_HOUR;
			dur = dur+h+"h";
		}
		if (millis > ONE_MINUTE){
			m = millis / ONE_MINUTE;
			millis = millis % ONE_MINUTE;
			dur = dur+m+"m";
		}
		if (millis > ONE_SECOND){
			s = millis / ONE_SECOND;
			millis = millis % ONE_SECOND;
			dur = dur+s+"s";
		}
		if (millis > 0){
			dur = dur+millis+"ms";
		}

		return dur;
	}
}
