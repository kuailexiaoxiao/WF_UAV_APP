package com.demo.wf_uav;

import com.iflytek.cloud.SpeechConstant;

import android.content.Context;
import android.os.Handler;

//
public class Constant {

	public static final String PREF_CONTROLIP_URL = "pref_controlIP_url";
	public static final String PREF_CAMERAIP_URL = "pref_cameraIP_url";
	public static final String PREF_SPEECH_SET = "pref_speech_settings";

	public static  String DEFAULT_CONTROLIP_Value = "192.168.1.1:2001";// Ĭ�ϵĿ���IP����ַ
	public static  String DEFAULT_CAMERAIP_Value  = "http://192.168.1.1:8080/?action=stream";// Ĭ�ϵ���Ƶ����ַ
	public static  String DEFAULT_SPEECH_Value    = "Local";// Ĭ�ϵ���������
	
	public static Context context;
	public static Handler handler=null;
	

}
