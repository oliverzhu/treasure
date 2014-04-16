package com.home.reflect;

public final class MediaStore {
	 public static final class Audio {
		 public interface AudioColumns extends AudioExtensionColumns{
			 
		 }
		 public static final class Media implements AudioColumns {
			 
		 }
	 }
	 
	 public interface AudioExtensionColumns {
        /**
         * Indicates the DURATION is accurate or not.
         * <P>Type: INTEGER (boolean)</P>
         */
        public static final String IS_ACCURATE_DURATION = "isaccurateduration";

        /**
         * A pinyin key calculated from the TITLE, used for
         * sorting and grouping
         * <P>Type: TEXT</P>
         */
        public static final String TITLE_PINYIN_KEY = "title_pinyin_key";
	 }

}
