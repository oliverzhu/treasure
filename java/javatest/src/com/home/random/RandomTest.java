package com.home.random;

import java.util.Random;

public class RandomTest {
	private static Random rand = new Random();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(rand.nextInt(2));
	}

}
