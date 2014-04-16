package com.home.str;

public class SplitStrTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String str = 
			"application: label='Backup and Restore' icon='res/drawable-hdpi/ic_backuprestore_launcher.png' name=''";
		
		String newStr = str.substring(str.indexOf("label"));	
		
		System.out.println(newStr);
		
		String[] appInfos = newStr.split("' ");
		
		System.out.println(appInfos);
	}

}
