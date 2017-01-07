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
	public static String CameraIp_const; // ��Ƶ����ַ
	public static String CtrlIP_const; // ���Ƶ�ַ
	public static String EngineType_const;//��������ֵ
	public String mEngineType;//��������
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
	private EditText chat_info; // �ı��༭��
	private byte[] Cmd_ForWard = { (byte) 0xff, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0xff };
	private byte[] Cmd_BackWard = { (byte) 0xff, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0xff };
	private byte[] Cmd_TurnLeft = { (byte) 0xff, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0xff };
	private byte[] Cmd_TurnRight = { (byte) 0xff, (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0xff };
	private byte[] Cmd_Land = { (byte) 0xff, (byte) 0x00, (byte) 0x05, (byte) 0x00, (byte) 0xff };
	private byte[] Cmd_Stop = { (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xff };
	private byte[] Cmd_Launch = { (byte) 0xff, (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0xff };
	private static final String TAG = Video.class.getSimpleName();
	// ������д����
	private SpeechRecognizer mIat;
	// ������дUI
	private RecognizerDialog mIatDialog;
	// ��HashMap�洢��д���
	private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
	// ��������
	// public String mEngineType = SpeechConstant.TYPE_CLOUD;
	// // ��ǰ�װ������
	ApkInstaller mInstaller;

	private Socket socket;// ��ʼ����һ��socket��
	OutputStream socketWriter;// ��ʼ��һ��IO�����
	MySurfaceView sfv;
	// temp����ֻ�ý����ʾһ��
	public boolean temp = false;
	int ret = 0;// ���ú�������ֵ

	public void getSettingValue() {
		 SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this); 
		 CtrlIP_const   = settings.getString(Constant.PREF_CONTROLIP_URL, 
				               Constant.DEFAULT_CONTROLIP_Value);//���ؿ��Ƶ�ַ
		 CameraIp_const = settings.getString(Constant.PREF_CAMERAIP_URL, 
				               Constant.DEFAULT_CONTROLIP_Value);//������Ƶ����ַ
		  EngineType_const  = settings.getString(Constant.PREF_SPEECH_SET,
				               Constant.DEFAULT_SPEECH_Value);//������������
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// ���ô���ȫ��
		getSettingValue();
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new // �Ͽ�ģʽ
			StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		setContentView(R.layout.video);
		InitSpeech(); // ��ʼ������������AppId
		InitLayout(); // �����ĳ�ʼ��
		init_IP(); // IP��ַ��ʼ��
		// ʹ��SpeechRecognizer���󣬿ɸ��ݻص���Ϣ�Զ�����棻
		// ֮ǰ����һֱ�д�������Ϊ������һ��û������
		mIat = SpeechRecognizer.createRecognizer(Video.this, mInitListener);
		mIatDialog = new RecognizerDialog(Video.this, mInitListener);
		chat_info = (EditText) findViewById(R.id.chat_info_1);// ������ʾʶ��������ı���
		mInstaller = new ApkInstaller(Video.this); // ������װѶ�����APK

	}

	private void init_IP() { // ��ʼ��IP��ַ�Ͷ˿ں�
		int index = CtrlIP_const.indexOf(":"); // ��ȡ����IP��ַ�е�":"λ�õĵ�ַ
		CtrlIP = CtrlIP_const.substring(0, index);
		String ctrlport = CtrlIP_const.substring(index + 1, CtrlIP_const.length());
		CtrlPort = Integer.parseInt(ctrlport);// String��תint����
		CameraIp = CameraIp_const;
		MySurfaceView.GetCameraIP(CameraIp);// ����Ƶ����ַ���ݸ�SurfaceView
		InitSocket();
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
		findViewById(R.id.picture).setOnClickListener(this);// ���ռ�����
		findViewById(R.id.ButtonTakePic).setOnClickListener(this);// ����Sys_setting�ļ�����
		findViewById(R.id.speech_recognize).setOnClickListener(this);// ��������ʶ��İ�ť
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
		// ����Ĵ�����Ĭ�ϻ��ʽ��д�����������û�����綼���Ե�
		// mEngineType = SpeechConstant.TYPE_MIX;

	}

	@Override
	public void onClick(View v) {// ���������˻�µİ���
		switch (v.getId()) {
		case R.id.ButtonTakePic:
			new Thread(new Runnable() {// �������տ���һ���µ��߳�,����Ҫѧ�����ֿ����µ��̵߳ķ���
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
			showTip(CameraIp_const);
			Intent intent = new Intent(Video.this, photograph.class);
			break;
		case R.id.btnForward:
			showTip("��ǰ��");
			SendCmd(Cmd_ForWard);
			break;
		case R.id.btnBack:
			showTip("����");
			SendCmd(Cmd_BackWard);
			break;
		case R.id.btnLeft:
			showTip("�����");
			SendCmd(Cmd_TurnLeft);
			break;
		case R.id.btnRight:
			showTip("���ҷ�");
			SendCmd(Cmd_TurnRight);
			break;
		case R.id.btnStop:
			showTip("��ͣ");
			SendCmd(Cmd_Stop);
			break;
		case R.id.launch:
			showTip("���");
			SendCmd(Cmd_Launch);
			break;

		case R.id.land:
			showTip("����");
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
																									 * ѡ�񱾵���д
																									 * �ж��Ƿ�װ���,δ��װ����ת����ʾ��װҳ��
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
		// ����12345678���滻��������� APPID�������ַ�� http://www.xfyun.cn
		// ������ �� =���� appid ֮�����������ַ�����ת���
		SpeechUtility.createUtility(Video.this, "appid=57f70bd1");

	}

	private void startSpeech() {
		// �ƶ����ݷ������ռ���ʼ��д�¼�
		FlowerCollector.onEvent(Video.this, "iat_recognize");
		chat_info.setText(null);// �����ʾ����
		// ���ò���
		setParam();
		mIatDialog.setListener(mRecognizerDialogListener);
		// mIatDialog.setListener(new MyRecognizerDialogListener());
		// 6.��ʾDialog,������������
		mIatDialog.show();
	}

	/**
	 * ��������
	 */
	private void setParam() {
		// ��ղ���
		mIat.setParameter(SpeechConstant.PARAMS, null);
		// ������д����
		mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
		// ���÷��ؽ����ʽ
		mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
		// 2.����accent \ language �Ȳ���
		mIatDialog.setParameter(SpeechConstant.LANGUAGE, "zn_ch");
		mIatDialog.setParameter(SpeechConstant.ACCENT, "mandarin");
	}

	/**
	 * ������ʾ������ʧ�ĶԻ���
	 */
	public void showTip(String data) {

		Toast.makeText(Video.this, data, Toast.LENGTH_SHORT).show();
	}

	/**
	 * ��ʼ����������
	 */
	private InitListener mInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			Log.d(TAG, "SpeechRecognizer init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				showTip("��ʼ��ʧ�ܣ������룺" + code);
			}
		}
	};

	private void printResult(RecognizerResult results) {
		String text = JsonParser.parseIatResult(results.getResultString());

		String sn = null;
		// ��ȡjson����е�sn�ֶ�
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
		case "��ǰ��":
			SendCmd(Cmd_ForWard);
			showTip("��ǰ��");
			break;
		case "���":
			SendCmd(Cmd_BackWard);
			showTip("����");
			break;
		case "����":
			SendCmd(Cmd_TurnLeft);
			showTip("�����");
			break;
		case "���ҡ�":
			SendCmd(Cmd_TurnRight);
			showTip("���ҷ�");
			break;
		case "��ͣ��":
			SendCmd(Cmd_Stop);
			showTip("��ͣ");
			break;
		case "���䡣":
			SendCmd(Cmd_Land);
			showTip("����");
			break;
		case "��ɡ�":
			SendCmd(Cmd_Launch);
			showTip("���");
			break;
		default:
			showTip("ָ����������������룡");
			break;
		}

		// if(message_voice.equals("��ǰ�ɡ�")){
		// SendCmd(Cmd_ForWard);
		// }
		chat_info.setSelection(chat_info.length());
	}

	/**
	 * ��дUI������
	 */
	private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
		public void onResult(RecognizerResult results, boolean isLast) {
			printResult(results);
		}

		/**
		 * ʶ��ص�����.
		 */
		public void onError(SpeechError error) {
			showTip(error.getPlainDescription(true));
		}

	};
	// ���´�������ʾ�ٰ�һ���˳�����
	private long exitTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {

			if ((System.currentTimeMillis() - exitTime) > 2500) // System.currentTimeMillis()���ۺ�ʱ���ã��϶�����2500
			{
				Toast.makeText(getApplicationContext(), "�ٰ�һ���˳�����", Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();
			} else {
				finish();
				System.exit(0); // ������ͷ��ڴ�
			}

			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
