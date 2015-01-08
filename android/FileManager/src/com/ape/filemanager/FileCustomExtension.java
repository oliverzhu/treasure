package com.ape.filemanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.text.TextUtils;
import android.util.Xml;

public class FileCustomExtension
{
    private static final String XML_FILE_NAME = "/system/media/FileCustomExtension.xml";
    private static final String TAG_EXTENSION = "extension";
    private static final String ELEMENT_MIME = "mimeType";

    private static HashMap<String, String> extToMimeTypeMap = new HashMap<String, String>();
    private static HashMap<String, String> mimeTypeToExtMap = new HashMap<String, String>();

    private static final ArrayList<ExtMimeType> extMimeList = new ArrayList<ExtMimeType>();

    static {
        loadExtensionFile();
    }

    public static class ExtMimeType
    {
        String extName;
        String mimeType;

        public String getExtName()
        {
            return extName;
        }

        public void setExtName(String extName)
        {
            this.extName = extName;
        }

        public String getMimeType()
        {
            return mimeType;
        }

        public void setMimeType(String mimeType)
        {
            this.mimeType = mimeType;
        }
    }

    public static ArrayList<ExtMimeType> generateExtMimeList(InputStream xml)
            throws XmlPullParserException, IOException
    {
        ExtMimeType element = null;
        XmlPullParser pullParser = Xml.newPullParser();
        pullParser.setInput(xml, "UTF-8");
        int event = pullParser.getEventType();

        while (event != XmlPullParser.END_DOCUMENT)
        {
            switch (event)
            {
                case XmlPullParser.START_DOCUMENT:
                    break;

                case XmlPullParser.START_TAG:
                    if (TAG_EXTENSION.equals(pullParser.getName()))
                    {
                        String extName = pullParser.getAttributeValue(0);
                        element = new ExtMimeType();
                        element.setExtName(extName.toLowerCase());
                    }
                    else if (ELEMENT_MIME.equals(pullParser.getName()))
                    {
                        String mime = pullParser.nextText();
                        element.setMimeType(mime.toLowerCase());
                    }
                    break;

                case XmlPullParser.END_TAG:
                    if (TAG_EXTENSION.equals(pullParser.getName()))
                    {
                        extMimeList.add(element);
                        extToMimeTypeMap.put(element.getExtName(), element.getMimeType());
                        mimeTypeToExtMap.put(element.getMimeType(), element.getExtName());
                        element = null;
                    }
                    break;
            }
            event = pullParser.next();
        }
        return extMimeList;
    }
    
    public static void loadExtensionFile()
    {
        File xmlFile = new File(XML_FILE_NAME);
        FileInputStream fileIn = null;
        try
        {
            if (xmlFile.exists())
            {
                fileIn = new FileInputStream(xmlFile);
                generateExtMimeList(fileIn);
            }
        } catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            try
            {
                if (fileIn != null)
                    fileIn.close();
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    public static String getExtFromMime(String mimeType)
    {
        return mimeTypeToExtMap.get(mimeType.toLowerCase());
    }
    
    public static String getMimeFromExt(String ext)
    {
        return extToMimeTypeMap.get(ext.toLowerCase());
    }

    public static String getMimeFromPath(String filePath)
    {
        String ext = Util.getExtFromFilename(filePath);
        
        if (!TextUtils.isEmpty(ext))
        {
            return getMimeFromExt(ext);
        }
        
        return "";
    }
    
    public static int getIconIdFromExt(String ext)
    {
        String mimeType = getMimeFromExt(ext);
        
        return getIconIdFromMime(mimeType);
    }

    public static int getIconIdFromMime(String mimeType)
    {
        if (TextUtils.isEmpty(mimeType))
        {
            return R.drawable.file_icon_default;
        } else if (mimeType.startsWith("audio/"))
        {
            return R.drawable.file_icon_mp3;
        } else if (mimeType.startsWith("image/"))
        {
            return R.drawable.file_icon_picture;
        } else if (mimeType.startsWith("video/"))
        {
            return R.drawable.file_icon_video;
        } else
        {
            return R.drawable.file_icon_default;
        }
    }
    
    public static int getFileTypeFromMime(String mimeType)
    {
        if (TextUtils.isEmpty(mimeType))
        {
            return 0;
        } else if (mimeType.startsWith("audio/"))
        {
            return MediaFile.FILE_TYPE_MP3;
        } else if (mimeType.startsWith("image/"))
        {
            return MediaFile.FILE_TYPE_JPEG;
        } else if (mimeType.startsWith("video/"))
        {
            return MediaFile.FILE_TYPE_AVI;
        } else
        {
            return 0;
        }
    }
    
    public static int getFileTypeFromExt(String ext)
    {
        String mimeType = getMimeFromExt(ext);
        
        return getFileTypeFromMime(mimeType);
    }
}
