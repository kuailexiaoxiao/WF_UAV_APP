package com.demo.speech;

import com.iflytek.cloud.SpeechUtility;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;

public class ApkInstaller {
private Activity mActivity ;
	
	public ApkInstaller(Activity activity) {
		mActivity = activity;
	}

	public void install(){
		AlertDialog.Builder builder = new Builder(mActivity);
		builder.setMessage("��⵽��δ��װ��ǣ�\n�Ƿ�ǰ��������ǣ�");
		builder.setTitle("������ʾ");
		builder.setPositiveButton("ȷ��ǰ��", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				String url = SpeechUtility.getUtility().getComponentUrl();
				String assetsApk="SpeechService.apk";
				processInstall(mActivity, url,assetsApk);
			}
		});
		builder.setNegativeButton("���ܾ̾�", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
		return;
	}
	/**
	 * ����������û�а�װ�����������������ҳ�棬�������غ�װ��
	 */
	private boolean processInstall(Context context ,String url,String assetsApk){
		//ֱ�����ط�ʽ
		Uri uri = Uri.parse(url);
		Intent it = new Intent(Intent.ACTION_VIEW, uri);
		context.startActivity(it);
		return true;		
	}
}
