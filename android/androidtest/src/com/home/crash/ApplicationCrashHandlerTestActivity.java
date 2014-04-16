package com.home.crash;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.home.R;

public class ApplicationCrashHandlerTestActivity extends Activity {
	private Button btn;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crash_main);
        
        Thread.setDefaultUncaughtExceptionHandler(
        		new ApplicationCrashHandler(this));
        
        btn = (Button) findViewById(R.id.btn);
        
        btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ExecutorService es = Executors.newCachedThreadPool();
				es.execute(new Thread()
				{
					@Override
					public void run() {
							throw new RuntimeException();
					}
				});
				
			}
		});
        
    }
}