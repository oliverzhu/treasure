package com.home.loader;

/*
 * 去掉Android文档中需要联网的javascript代码
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FormatDoc {
    public static int j=1;
    /**
     * @param args
     */
    public static void main(String[] args) {
        
        File file = new File("D:/workDev/android-sdk-windows/docs/");
        searchDirectory(file, 0);
        System.out.println("OVER");
    }

    public static void searchDirectory(File f, int depth) {
        if (!f.isDirectory()) {
            String fileName = f.getName();
            if (fileName.matches(".*.{1}html")) {
                String src= "<(link rel)[=]\"(stylesheet)\"\n(href)[=]\"(http)://(fonts.googleapis.com/css)[?](family)[=](Roboto)[:](regular,medium,thin,italic,mediumitalic,bold)\"( title)[=]\"roboto\">";
                String src1 = "<script src=\"http://www.google.com/jsapi\" type=\"text/javascript\"></script>";
                String dst = "";
                //如果是html文件则注释掉其中的特定javascript代码
                annotation(f, src, dst);
                annotation(f, src1, dst);
            }
        } else {
            File[] fs = f.listFiles();
            depth++;
            for (int i = 0; i < fs.length; ++i) {
                File file = fs[i];
                searchDirectory(file, depth);
            }
        }
    }

    /*
     * f 将要修改其中特定内容的文件 
     * src 将被替换的内容 
     * dst 将被替换层的内容
     */
    public static void annotation(File f, String src, String dst) {
        String content = FormatDoc.read(f);
        content = content.replaceFirst(src, dst);
        int ll=content.lastIndexOf(src);
        System.out.println(ll);
        FormatDoc.write(content, f);
        System.out.println(j++);
        return;

    }

    public static String read(File src) {
        StringBuffer res = new StringBuffer();
        String line = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(src));
            int i=0;
            while ((line = reader.readLine()) != null) {
                if (i!=0) {
                    res.append('\n');
                }
                res.append(line);
                i++;
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res.toString();
    }

    public static boolean write(String cont, File dist) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(dist));
            writer.write(cont);
            writer.flush();
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
