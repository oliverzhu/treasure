package com.home.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String notificationReadTime = sdf.format(new Date());
		
		System.out.println(notificationReadTime);
		
		String readTimeStr = "1990-3-28 00:00:00";
		SimpleDateFormat sdfss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date readTime = sdfss.parse(readTimeStr);
			System.out.println(readTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		
	}

}
