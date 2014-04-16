package com.home.thread;

public class EvenGenerator extends IntGenerator {
	private int currentEvenValue;

	@Override
	public int next() {
		++currentEvenValue;
		++currentEvenValue;
		return currentEvenValue;
	}
	
	public static void main(String[] args) {
		EvenChecker.test(new EvenGenerator());
	}

}
