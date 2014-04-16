package com.home.symbol;

public class SymbolTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String aa = "te\nstte\nst";
		
		String bb = aa.replace("\n", "\r\n");
		System.out.print(bb);
	}

}
