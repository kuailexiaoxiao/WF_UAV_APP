package com.demo.wf_uav;

import com.iflytek.cloud.SpeechConstant;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class UAV_Setting extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private EditTextPreference mEditTextPreConIpUrl;
	private EditTextPreference mEditTextPreCamIPUrl;
	private ListPreference mListPreference;
	String TAG = null;

	// String Cloud = "����ѡ��Cloud";
	// String Local = "����ѡ��Local";
	// String Mix = "����ѡ��Mix";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// ���е�ֵ�����Զ����浽��SharePreferences
		addPreferencesFromResource(R.xml.preference);
		initPreference();
	}

	private void initPreference() {
		mEditTextPreConIpUrl = (EditTextPreference) findPreference(Constant.PREF_CONTROLIP_URL);
		// Constant.PREF_CONTROLIP_URL��Ӧ���Ǹñ༭��ļ�ֵ ����Xml�ļ��п����ҵ�
		mEditTextPreCamIPUrl = (EditTextPreference) findPreference(Constant.PREF_CAMERAIP_URL);
		mListPreference = (ListPreference) findPreference(Constant.PREF_SPEECH_SET);
	}

	@Override
	// ��onResume�г�ʼ���ؼ���ֵ
	protected void onResume() {
		super.onResume();
		SharedPreferences mSharedPreferences = getPreferenceScreen().getSharedPreferences();
		mEditTextPreConIpUrl
				.setSummary(mSharedPreferences.
						getString(Constant.PREF_CONTROLIP_URL, Constant.DEFAULT_CONTROLIP_Value));
		mEditTextPreCamIPUrl
				.setSummary(mSharedPreferences.
						getString(Constant.PREF_CAMERAIP_URL, Constant.DEFAULT_CAMERAIP_Value));

		mListPreference.setSummary(mSharedPreferences.
				         getString(Constant.PREF_SPEECH_SET, Constant.DEFAULT_SPEECH_Value));

		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);// ע��

	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);// ע���¼�
	}

	@Override
	// �¼�������. �������ݵı仯,����ʾ����Ϊ���ı�
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Preference pref = findPreference(key);
		if (pref instanceof EditTextPreference) {
			EditTextPreference etp = (EditTextPreference) pref;
			pref.setSummary(etp.getText());
			String prefsValue = etp.toString();
		    showToast(prefsValue);
		} else if (pref instanceof ListPreference) {
			ListPreference etp = (ListPreference) pref;
			pref.setSummary(etp.getEntry());
			String prefsValue = etp.getValue();
			showToast(prefsValue);// ���������ʾ�ı��ֵ
		}

	}

	private void showToast(String arg) {

		Toast.makeText(this, arg, Toast.LENGTH_SHORT).show();

	}

}
