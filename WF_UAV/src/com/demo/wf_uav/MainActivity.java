package com.demo.wf_uav;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;

public class MainActivity extends Activity implements OnClickListener {

	ImageButton start;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		findViewById(R.id.Sys_setting).setOnClickListener(this);// ÉèÖÃSys_settingµÄ¼àÌýÆ÷
		findViewById(R.id.start).setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.Sys_setting:
			Intent intent = new Intent(MainActivity.this, UAV_Setting.class);
			startActivity(intent);
			break;
		case R.id.start:
			Intent intent1 = new Intent(MainActivity.this, Video.class);
			startActivity(intent1);
			finish();  
            System.exit(0); 
			break;
		default:
			break;
		}

	}

}
