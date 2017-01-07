package com.demo.wf_uav;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.demo.speech.ApkInstaller;
import com.demo.speech.FucUtil;
import com.demo.speech.JsonParser;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.sunflower.FlowerCollector;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

public class Video extends Activity implements OnClickListener {
	public static String CameraIp_const; // 视频流地址
	public static String CtrlIP_const; // 控制地址
	public static String EngineType_const;// 语音引擎值
	public String mEngineType;// 语音引擎
	public static String CameraIp;
	public static String CtrlIP;
	public static int CtrlPort = 0;
	private Drawable ForWardon;
	private Drawable ForWardoff;
	private Drawable BackWardon;
	private Drawable BackWardoff;
	private Drawable TurnLefton;
	private Drawable TurnLeftoff;
	private Drawable TurnRighton;
	private Drawable TurnRightoff;
	private Drawable buttonLenon;
	private Drawable buttonLenoff;
	private EditText chat_info; // 文本编辑框
	private byte[] Cmd_ForWard = { (byte) 0xff, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0xff };
	private byte[] Cmd_BackWard = { (byte) 0xff, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0xff };
	private byte[] Cmd_TurnLeft = { (byte) 0xff, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0xff };
	private byte[] Cmd_TurnRight = { (byte) 0xff, (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0xff };
	private byte[] Cmd_Land = { (byte) 0xff, (byte) 0x00, (byte) 0x05, (byte) 0x00, (byte) 0xff };
	private byte[] Cmd_Stop = { (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xff };
	private byte[] Cmd_Launch = { (byte) 0xff, (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0xff };
	private static final String TAG = Video.class.getSimpleName();
	// 语音听写对象
	private SpeechRecognizer mIat;
	// 语音听写UI
	private RecognizerDialog mIatDialog;
	// 用HashMap存储听写结果
	private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
	// 引擎类型
	// public String mEngineType = SpeechConstant.TYPE_CLOUD;
	// // 语记安装助手类
	ApkInstaller mInstaller;
	private Socket socket;// 初始化了一个socket流
	OutputStream socketWriter;// 初始化一个IO输出流
	MySurfaceView sfv;
	// temp用来只让结果显示一次
	public boolean temp = false;
	int ret = 0;// 调用函数返回值

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置窗体全屏
		getSettingValue();// 初始化设置参数
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new // 严苛模式
			StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		setContentView(R.layout.video);
		InitSpeech(); // 初始化语音，加载AppId
		InitLayout(); // 按键的初始化
		init_IP(); // IP地址初始化
		// 使用SpeechRecognizer对象，可根据回调消息自定义界面；
		// 之前这里一直有错误，是因为下面那一句没有声明
		mIat = SpeechRecognizer.createRecognizer(Video.this, mInitListener);
		mIatDialog = new RecognizerDialog(Video.this, mInitListener);
		chat_info = (EditText) findViewById(R.id.chat_info_1);// 用来显示识别的语音文本框
		mInstaller = new ApkInstaller(Video.this); // 用来安装讯飞语记APK

	}

	private void init_IP() { // 初始化IP地址和端口号
		int index = CtrlIP_const.indexOf(":"); // 获取控制IP地址中的":"位置的地址
		CtrlIP = CtrlIP_const.substring(0, index);
		String ctrlport = CtrlIP_const.substring(index + 1, CtrlIP_const.length());
		CtrlPort = Integer.parseInt(ctrlport);// String型转int整形
		CameraIp = CameraIp_const;
		MySurfaceView.GetCameraIP(CameraIp);// 把视频流地址传递给SurfaceView
		InitSocket();
	}

	public void getSettingValue() {// 初始化设置参数
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		CtrlIP_const = settings.getString(Constant.PREF_CONTROLIP_URL, Constant.DEFAULT_CONTROLIP_Value);// 加载控制地址
		CameraIp_const = settings.getString(Constant.PREF_CAMERAIP_URL, Constant.DEFAULT_CONTROLIP_Value);// 加载视频流地址
		EngineType_const = settings.getString(Constant.PREF_SPEECH_SET, Constant.DEFAULT_SPEECH_Value);// 加载语音引擎
		switch (EngineType_const) {
		case "Cloud":
			mEngineType = SpeechConstant.TYPE_CLOUD;
			break;
		case "Local":
			mEngineType = SpeechConstant.TYPE_LOCAL;
		case "Mix":
			mEngineType = SpeechConstant.TYPE_MIX;
		default:
			break;
		}
	}

	private void InitSocket() {

		try {
			socket = new Socket(CtrlIP, CtrlPort);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			socketWriter = socket.getOutputStream();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void onDestroy() {
		super.onDestroy();

	}

	private void InitLayout() {
		findViewById(R.id.mySurfaceViewVideo).setOnClickListener(this);
		findViewById(R.id.picture).setOnClickListener(this);// 拍照监听器
		findViewById(R.id.ButtonTakePic).setOnClickListener(this);// 设置Sys_setting的监听器
		findViewById(R.id.speech_recognize).setOnClickListener(this);// 设置语言识别的按钮
		findViewById(R.id.btnForward).setOnClickListener(this);
		findViewById(R.id.btnLeft).setOnClickListener(this);
		findViewById(R.id.btnRight).setOnClickListener(this);
		findViewById(R.id.btnBack).setOnClickListener(this);
		findViewById(R.id.btnStop).setOnClickListener(this);
		findViewById(R.id.land).setOnClickListener(this);
		findViewById(R.id.launch).setOnClickListener(this);
		ForWardon = getResources().getDrawable(R.drawable.sym_forward_1);
		ForWardoff = getResources().getDrawable(R.drawable.sym_forward);

		TurnLefton = getResources().getDrawable(R.drawable.sym_left_1);
		TurnLeftoff = getResources().getDrawable(R.drawable.sym_left);

		TurnRighton = getResources().getDrawable(R.drawable.sym_right_1);
		TurnRightoff = getResources().getDrawable(R.drawable.sym_right);

		BackWardon = getResources().getDrawable(R.drawable.sym_backward_1);
		BackWardoff = getResources().getDrawable(R.drawable.sym_backward);

		buttonLenon = getResources().getDrawable(R.drawable.sym_light);
		buttonLenoff = getResources().getDrawable(R.drawable.sym_light_off);
		// 下面的代码是默认混和式听写即有网络或者没有网络都可以的
		// mEngineType = SpeechConstant.TYPE_MIX;

	}

	@Override
	public void onClick(View v) {// 用来监听此活动下的按键
		switch (v.getId()) {
		case R.id.ButtonTakePic:
			new Thread(new Runnable() {// 这里拍照开启一个新的线程,这里要学会这种开启新的线程的方法
				@Override
				public void run() {
					if (null != Constant.handler) {
						Message message = new Message();
						message.what = 1;
						Constant.handler.sendMessage(message);
					}
				}
			}).start();
			break;
		case R.id.speech_recognize:
			mEngineType_Choice();
			startSpeech();
			break;
		case R.id.picture:
			showTip("查看已拍摄的照片");
			Intent intent = new Intent(Video.this, photograph.class);
			startActivity(intent);
			break;
		case R.id.btnForward:
			showTip("向前飞");
			SendCmd(Cmd_ForWard);
			break;
		case R.id.btnBack:
			showTip("向后飞");
			SendCmd(Cmd_BackWard);
			break;
		case R.id.btnLeft:
			showTip("向左飞");
			SendCmd(Cmd_TurnLeft);
			break;
		case R.id.btnRight:
			showTip("向右飞");
			SendCmd(Cmd_TurnRight);
			break;
		case R.id.btnStop:
			showTip("悬停");
			SendCmd(Cmd_Stop);
			break;
		case R.id.launch:
			showTip("起飞");
			SendCmd(Cmd_Launch);
			break;

		case R.id.land:
			showTip("降落");
			SendCmd(Cmd_Land);
		default:
			break;
		}
	}

	private void SendCmd(byte[] data) {
		try {
			socketWriter.write(data);
		} catch (Exception e) {
			Log.i("Socket", e.getMessage() != null ? e.getMessage().toString() : "sendCommand error!");
		}
	}

	private void mEngineType_Choice() {

		if ((mEngineType == SpeechConstant.TYPE_MIX) || (mEngineType == SpeechConstant.TYPE_LOCAL)) { /**
																										 * 选择本地听写
																										 * 判断是否安装语记,未安装则跳转到提示安装页面
																										 */
			if (!SpeechUtility.getUtility().checkServiceInstalled()) {
				mInstaller.install();
			} else {
				String result = FucUtil.checkLocalResource();
				if (!TextUtils.isEmpty(result)) {
					showTip(result);
				}
			}

		}

	}

	private void InitSpeech() {
		// 将“12345678”替换成您申请的 APPID，申请地址： http://www.xfyun.cn
		// 请勿在 “ =”与 appid 之间添加任务空字符或者转义符
		SpeechUtility.createUtility(Video.this, "appid=57f70bd1");

	}

	private void startSpeech() {
		// 移动数据分析，收集开始听写事件
		FlowerCollector.onEvent(Video.this, "iat_recognize");
		chat_info.setText(null);// 清空显示内容
		// 设置参数
		setParam();
		mIatDialog.setListener(mRecognizerDialogListener);
		// mIatDialog.setListener(new MyRecognizerDialogListener());
		// 6.显示Dialog,接收语音输入
		mIatDialog.show();
	}

	/**
	 * 参数设置
	 */
	private void setParam() {
		// 清空参数
		mIat.setParameter(SpeechConstant.PARAMS, null);
		// 设置听写引擎
		mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
		// 设置返回结果格式
		mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
		// 2.设置accent \ language 等参数
		mIatDialog.setParameter(SpeechConstant.LANGUAGE, "zn_ch");
		mIatDialog.setParameter(SpeechConstant.ACCENT, "mandarin");
	}

	/**
	 * 用来显示可以消失的对话框
	 */
	public void showTip(String data) {

		Toast.makeText(Video.this, data, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 初始化监听器。
	 */
	private InitListener mInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			Log.d(TAG, "SpeechRecognizer init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				showTip("初始化失败，错误码：" + code);
			}
		}
	};

	private void printResult(RecognizerResult results) {
		String text = JsonParser.parseIatResult(results.getResultString());

		String sn = null;
		// 读取json结果中的sn字段
		try {
			JSONObject resultJson = new JSONObject(results.getResultString());
			sn = resultJson.optString("sn");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		mIatResults.put(sn, text);

		StringBuffer resultBuffer = new StringBuffer();
		for (String key : mIatResults.keySet()) {
			resultBuffer.append(mIatResults.get(key));
		}

		chat_info.setText(resultBuffer.toString());
		// Toast.makeText(Video.this, chat_info.getText(),
		// Toast.LENGTH_SHORT).show();
		String message_voice = chat_info.getText().toString();
		switch (message_voice) {
		case "向前。":
			SendCmd(Cmd_ForWard);
			showTip("向前飞");
			break;
		case "向后。":
			SendCmd(Cmd_BackWard);
			showTip("向后飞");
			break;
		case "向左。":
			SendCmd(Cmd_TurnLeft);
			showTip("向左飞");
			break;
		case "向右。":
			SendCmd(Cmd_TurnRight);
			showTip("向右飞");
			break;
		case "悬停。":
			SendCmd(Cmd_Stop);
			showTip("悬停");
			break;
		case "降落。":
			SendCmd(Cmd_Land);
			showTip("降落");
			break;
		case "起飞。":
			SendCmd(Cmd_Launch);
			showTip("起飞");
			break;
		default:
			showTip("指令错误：请您重新输入！");
			break;
		}

		// if(message_voice.equals("向前飞。")){
		// SendCmd(Cmd_ForWard);
		// }
		chat_info.setSelection(chat_info.length());
	}

	/**
	 * 听写UI监听器
	 */
	private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
		public void onResult(RecognizerResult results, boolean isLast) {
			printResult(results);
		}

		/**
		 * 识别回调错误.
		 */
		public void onError(SpeechError error) {
			showTip(error.getPlainDescription(true));
		}

	};
	// 以下代码是提示再按一次退出程序
	private long exitTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {

			if ((System.currentTimeMillis() - exitTime) > 2500) // System.currentTimeMillis()无论何时调用，肯定大于2500
			{
				Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();
			} else {
				finish();
				System.exit(0); // 这个会释放内存
			}

			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
