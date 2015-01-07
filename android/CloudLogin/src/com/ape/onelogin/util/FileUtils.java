package com.ape.onelogin.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {
    /**
     * create new file
     * 
     * @param f
     * @throws Exception
     */
    public static void createNewFile(File f) throws Exception {
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }
        if (!f.exists()) {
            f.createNewFile();
        }
    }

    /**
     * 删除指定目录下的文件
     * 
     * @param dir
     */
    public static void deleteFile(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            File[] childs = dir.listFiles();
            for (File f : childs) {
                if (f.exists() && f.isDirectory()) {
                    continue;
                } else if (f.exists() && f.isFile()) {
                    f.delete();
                }
            }
            dir.delete();
        } else if (dir.exists() && dir.isFile()) {
            dir.delete();
        }
    }

    /**
     * 递归删除目录
     * 
     * @param dir
     */
    public static void deleteDir(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            File[] childs = dir.listFiles();
            for (File f : childs) {
                if (f.exists() && f.isDirectory()) {
                    deleteDir(f);
                } else if (f.exists()) {
                    f.delete();
                }
            }
            dir.delete();
        } else if (dir.exists()) {
            dir.delete();
        }
    }
    
    public static void copyFile(String source, String target) {
        File sourceFile = new File(source);
        File targetFile = new File(target);
        
        if (!sourceFile.exists()) {
            return;
        }
        
        if (targetFile.exists()) {
            return;
        }
        
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        int readLength = -1;
        byte[] buffer = new byte[1024 * 4];
        
        try {
            inputStream = new FileInputStream(sourceFile);
            outputStream = new FileOutputStream(targetFile);
            while (true) {
                readLength = inputStream.read(buffer);
                if (readLength < 0) {
                    break;
                }
                
                outputStream.write(buffer, 0, readLength);
            }
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
    }
}
