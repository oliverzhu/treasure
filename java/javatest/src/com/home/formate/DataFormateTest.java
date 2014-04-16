package com.home.formate;

import java.util.Locale;

public class DataFormateTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(formatStation(8750));
	}
	
	/**
     * according station to get frequency string
     * @param station for 100KZ, range 875-1080, for 50khz 8750,1080
     * @return string like 87.5 or 87.50
     */
    public static String formatStation(int station) {
        float frequency = (float)station / 100;
        String result = String.format(Locale.ENGLISH, "%.1f", Float.valueOf(frequency));
        return result;
    }

}
