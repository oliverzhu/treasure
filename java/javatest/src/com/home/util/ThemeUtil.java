package com.home.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class ThemeUtil {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File file = new File("X:\\s9121\\mediatek\\frameworks\\themes\\theme-res-gold\\settings\\res\\mipmap-xxhdpi");
		
		File[] subFile = file.listFiles();

		try {
			FileWriter fos = new FileWriter("D:\\setting_mxx.xml", true);
			BufferedWriter bw = new BufferedWriter(fos);
			for (int i = 0; i < subFile.length; i++) {
				String ss = "<item>" + subFile[i].getName() + "</item>" + "\n";
				bw.write(ss);
			}
			bw.close();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
