package com.home.io;

public class HexStringTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		byte[] b = new byte[1];
		b[0] = -1;
		String hex = Integer.toHexString(b[0] & 0xFF);
		System.out.println(hex);
	}

}
