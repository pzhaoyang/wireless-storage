package com.uninet.wirelessstore;

public class GeneralUtil {
	public static String localip = "127.0.0.1";
	public static int port = 0;
	public static String auth = "guest:guest";
	
	public static String getFileType(String uri){
		String type = MimeUtils.getmimetype(MimeUtils.getextension(uri));
		android.util.Log.d(SmbOpApi.TAG,"File type = " + type );
		return type;
	}
	
	public static String getAuth(){
		return auth;
	}
	
	public static void SetAuth(String userpass){
		auth = userpass;
	}
}
