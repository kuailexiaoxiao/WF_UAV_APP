package com.demo.wf_uav;

import com.iflytek.cloud.SpeechConstant;

import android.content.Context;
import android.os.Handler;

//
public class Constant {

	public static final String PREF_CONTROLIP_URL = "pref_controlIP_url";
	public static final String PREF_CAMERAIP_URL = "pref_cameraIP_url";
	public static final String PREF_SPEECH_SET = "pref_speech_settings";

	public static  String DEFAULT_CONTROLIP_Value = "192.168.1.1:2001";// 默认的控制IP：地址
	public static  String DEFAULT_CAMERAIP_Value  = "http://192.168.1.1:8080/?action=stream";// 默认的视频流地址
	public static  String DEFAULT_SPEECH_Value    = "Local";// 默认的语音引擎
	
	public static Context context;
	public static Handler handler=null;
	

}
