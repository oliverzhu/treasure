package com.home.reflect;

import java.lang.reflect.Field;

import com.home.reflect.MediaStore.Audio.Media;

public class MediaStoreFieldTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String sort =  MediaStore.Audio.Media.TITLE_PINYIN_KEY;
		try {
			Class clz = Media.class;
			Field[] fiels = clz.getFields();
			for(Field f : fiels)
			{
				String fName = f.getName();
				System.out.println(fName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
