package com.home.io;

/**
 * 
 * @author apache
 *
 */
public class Hex {
	private static final char[] DIGITS_LOWER = {
		'0','1','2','3','4','5','6','7',
		'8','9','a','b','c','d','e','f',
	};
	
	private static final char[] DIGITS_UPPDER = {
		'0','1','2','3','4','5','6','7',
		'8','9','A','B','C','D','E','F',
	};
	
	public static char[] encodeHex(byte[] data)
	{
		return encodeHex(data,true);
	}
	
	public static char[] encodeHex(byte[] data,boolean toLowerCase)
	{
		return encodeHex(data,
				toLowerCase ? DIGITS_LOWER : DIGITS_UPPDER);
	}
	
	/**
	 * 一个字节用两个字符表示，所有字符数组的大小是字节数组的两倍
	 * 0x0F:00001111
	 * java中是用补码来存储数据，参与位运算，而输出的结果(包括debug)则是计算机将反码转换成源码的结果
	 * data[i]要先转换成补码再做与运算
	 * data[i] >>> 4  将高4位转成字符
	 * @param data
	 * @param toDigits
	 * @return
	 */
	protected static char[] encodeHex(byte[] data,char[] toDigits)
	{
		int l = data.length;
		char[] out = new char[l << 1];
		
		for(int i = 0,j = 0;i < l;i++)
		{
			out[j++] = toDigits[0x0F & data[i] >>> 4];
			out[j++] = toDigits[0x0F & data[i]];
		}
		return out;
	}
	
	public static String encodeHexStr(byte[] data)
	{
		return encodeHexStr(data,true);
	}
	
	public static String encodeHexStr(byte[] data,boolean toLowerCase)
	{
		return encodeHexStr(data,
				toLowerCase ? DIGITS_LOWER : DIGITS_UPPDER);
	}
	
	protected static String encodeHexStr(byte[] data,char[] toDigits)
	{
		return new String(encodeHex(data, toDigits));
	}
	
	/**
	 * f & 0xFF = 229
	 * (byte) (f & 0xFF) = -27  (-27 = 229 - 256)
	 * @param data
	 * @return
	 */
	public static byte[] decodeHex(char[] data)
	{
		int len = data.length;
		
		if((len & 0x01) != 0)
		{
			throw new RuntimeException("Odd number of characters");
		}
		
		byte[] out = new byte[len >> 1];
		
		for (int i = 0, j = 0; j < len; i++) {
			int f = toDigit(data[j], j) << 4;
			j++;
			f = f | toDigit(data[j], j);
			j++;
			out[i] = (byte) (f & 0xFF);
		}
		
		return out;
	}
	
	protected static int toDigit(char ch, int index) {
		int digit = Character.digit(ch, 16);
		if (digit == -1) {
			throw new RuntimeException("Illegal hexadecimal character " 
					+ ch+ " at index " + index);
		}
		
		return digit;
	}
	
	public static void main(String[] args) {
		String srcStr = "待";
		String encodeStr = encodeHexStr(srcStr.getBytes());
		String decodeStr = new String(decodeHex(encodeStr.toCharArray()));
		System.out.println("转换前：" + srcStr);
		System.out.println("转换后：" + encodeStr);
		System.out.println("还原后：" + decodeStr);
	}
}
