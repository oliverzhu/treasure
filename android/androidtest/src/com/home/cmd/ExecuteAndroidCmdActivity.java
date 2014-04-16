package com.home.cmd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.home.R;

public class ExecuteAndroidCmdActivity extends Activity {
	private TextView content;
	private Process process;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_cmd);
		
		content = (TextView) findViewById(R.id.content);
		
//		String cmd = "ls -l -R ./system";
//		try {
//			process=java.lang.Runtime.getRuntime().exec(cmd);
//		} catch (IOException e) {
//			e.printStackTrace();
//		} 
//		BufferedInputStream br = new BufferedInputStream(process.getInputStream());
//		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(); 
		
		int ch;  
		try {
//			while ((ch = br.read()) != -1) {  
//				byteStream.write(ch);  
//			}  
//			byte[] result= byteStream.toByteArray();
//			byteStream.close();  
//
//			String out = new String(result,"UTF-8");
//			
//			HashMap<String, String> fileInfoMap = null;
//			
//			String[] dirs = null;
//			if(out != null && !"".equals(out.trim()))
//			{
//				dirs = out.split("\n\n");
//				
//				if(dirs != null && dirs.length != 0)
//				{
//					fileInfoMap = new HashMap<String, String>();
//					for(int i = 0;i < dirs.length;i++)
//					{
//						String[] files = dirs[i].split("\n");
//						String fileDir = files[0];
//					}
//				}
//			}
			
			
			File file = new File("/etc/list.txt");
			
			boolean isRoot = false;
			
			if(file.exists())
			{
				BufferedReader in = new BufferedReader(
						new FileReader(file));
				String s;
				StringBuilder sb = new StringBuilder();
				while((s = in.readLine()) != null)
				{
					sb.append(s + "\n");
				}
				in.close();
				
				String out = sb.toString();
				String[] dirs = null;
				if(out != null && !"".equals(out.trim()))
				{
					dirs = out.split("\n\n");
					
					if(dirs != null && dirs.length != 0)
					{
						for(int i = 0;i < dirs.length;i++)
						{
							String[] files = dirs[i].split("\n");
							if(files != null && file.length() > 2)
							{
								File destFile = null;
								for(int j = files.length - 1;j > 1;j--)
								{
									String fileInfo = files[j];
									//文件符号
									if(fileInfo.startsWith("-"))
									{
										String[] fileInfos = fileInfo.split(" ");
										String fileName = fileInfos[fileInfos.length - 1];
										String parentPath = files[0].replace(".", "/system").replace(":", "/");
										String filePath = parentPath + fileName;
										String fileSize = fileInfos[fileInfos.length - 5];
										
										destFile = new File(filePath);
										if(!destFile.exists() || destFile.length() != Long.valueOf(fileSize))
										{
											isRoot = true;
											break;
										}
										
									}
								}
								if(isRoot)
								{
									break;
								}
							}
						}
					}
				}
				
			}else
			{
				isRoot = true;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
}
