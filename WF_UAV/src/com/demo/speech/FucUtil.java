package com.demo.speech;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

import android.content.Context;

/**
 * �����Ժ�����չ��
 */
public class FucUtil {
	/**
	 * ��ȡassetĿ¼���ļ���
	 * @return content
	 */
	public static String readFile(Context mContext,String file,String code)
	{
		int len = 0;
		byte []buf = null;
		String result = "";
		try {
			InputStream in = mContext.getAssets().open(file);			
			len  = in.available();
			buf = new byte[len];
			in.read(buf, 0, len);
			
			result = new String(buf,code);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * ���ֽڻ��������չ̶���С���зָ������
	 * @param buffer ������
	 * @param length ��������С
	 * @param spsize �и���С
	 * @return
	 */
	public ArrayList<byte[]> splitBuffer(byte[] buffer,int length,int spsize)
	{
		ArrayList<byte[]> array = new ArrayList<byte[]>();
		if(spsize <= 0 || length <= 0 || buffer == null || buffer.length < length)
			return array;
		int size = 0;
		while(size < length)
		{
			int left = length - size;
			if(spsize < left)
			{
				byte[] sdata = new byte[spsize];
				System.arraycopy(buffer,size,sdata,0,spsize);
				array.add(sdata);
				size += spsize;
			}else
			{
				byte[] sdata = new byte[left];
				System.arraycopy(buffer,size,sdata,0,left);
				array.add(sdata);
				size += left;
			}
		}
		return array;
	}
	/**
	 * ��ȡ����Ƿ����������д��Դ����δ������ת����Դ����ҳ��
	 *1.PLUS_LOCAL_ALL: ����������Դ 
      2.PLUS_LOCAL_ASR: ����ʶ����Դ
      3.PLUS_LOCAL_TTS: ���غϳ���Դ
	 */
	public static String checkLocalResource(){
		String resource = SpeechUtility.getUtility().getParameter(SpeechConstant.PLUS_LOCAL_ASR);
		try {
			JSONObject result = new JSONObject(resource);
			int ret = result.getInt(SpeechUtility.TAG_RESOURCE_RET);
			switch (ret) {
			case ErrorCode.SUCCESS:
				JSONArray asrArray = result.getJSONObject("result").optJSONArray("asr");
				if (asrArray != null) {
					int i = 0;
					// ��ѯ�����������д��Դ
					for (; i < asrArray.length(); i++) {
						if("iat".equals(asrArray.getJSONObject(i).get(SpeechConstant.DOMAIN))){
							//asrArray�а������ԡ������ֶΣ�����������֧�ַ��Եı�����д��
							//�磺"accent": "mandarin","language": "zh_cn"
							break;
						}
					}
					if (i >= asrArray.length()) {
						
						SpeechUtility.getUtility().openEngineSettings(SpeechConstant.ENG_ASR);	
						return "û����д��Դ����ת����Դ����ҳ��";
					}
				}else {
					SpeechUtility.getUtility().openEngineSettings(SpeechConstant.ENG_ASR);
					return "û����д��Դ����ת����Դ����ҳ��";
				}
				break;
			case ErrorCode.ERROR_VERSION_LOWER:
				return "��ǰ汾���ͣ�����º�ʹ�ñ��ع���";
			case ErrorCode.ERROR_INVALID_RESULT:
				SpeechUtility.getUtility().openEngineSettings(SpeechConstant.ENG_ASR);
				return "��ȡ���������ת����Դ����ҳ��";
			case ErrorCode.ERROR_SYSTEM_PREINSTALL:
				//���Ϊ����Ԥ�ð汾��
			default:
				break;
			}
		} catch (Exception e) {
			SpeechUtility.getUtility().openEngineSettings(SpeechConstant.ENG_ASR);
			return "��ȡ���������ת����Դ����ҳ��";
		}
		return "";
	}
	
	/**
	 * ��ȡassetĿ¼����Ƶ�ļ���
	 * 
	 * @return �������ļ�����
	 */
	public static byte[] readAudioFile(Context context, String filename) {
		try {
			InputStream ins = context.getAssets().open(filename);
			byte[] data = new byte[ins.available()];
			
			ins.read(data);
			ins.close();
			
			return data;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	
}