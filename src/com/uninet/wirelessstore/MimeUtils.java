package com.uninet.wirelessstore;

import java.io.IOException;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import jcifs.smb.SmbFile;
import jcifs.util.MimeMap;

public class MimeUtils{

	static MimeMap mp;
	
	public static String getmimetype(String extension){
		String ext="";
		try {
			mp = new MimeMap();
			ext = mp.getMimeType(extension);
		} catch (IOException e) {
			Log.d(SmbOpApi.TAG,"Extension:  constrator Error!:" + e.getMessage());
		}
		Log.d(SmbOpApi.TAG, "Extension: " + extension + " mimetype  = " + ext);
		return ext;
	}
	
	public static int getIconbyType(String type){
		int ico;
		
		if(type.contains("text/")){
			ico = R.drawable.text;
		}else if(type.contains("application/pdf")){
			ico = R.drawable.pdf;
		}else if(type.contains("application/zip")
				||type.contains("application/x-tar")
				||type.contains("application/x-gzip")
				||type.contains("application/x-gtar")){
			ico = R.drawable.compress;
		}else if(type.contains("video/")){
			ico = R.drawable.video;
		}else if(type.contains("image/")){
			ico = R.drawable.image;
		}else if(type.contains("application/msword")){
			ico = R.drawable.word;
		}else if(type.contains("application/mspowerpoint")){
			ico = R.drawable.ppt;
		}else if(type.contains("application/vnd.ms-excel")){
			ico = R.drawable.excel;
		}else if(type.contains("audio/")){
			ico = R.drawable.audio;
		}else{
			ico = R.drawable.icon_common; 
		}
		Log.d(SmbOpApi.TAG, "Extension: " + type + " ico  = " + ico);
		return ico;
	}
	
	public static String getextension(String filename){
		String ex ="";
		ex = filename.substring(filename.lastIndexOf(".")+1,filename.length());
		Log.d(SmbOpApi.TAG, "Extension: " + filename + " Extension  = " + ex);
		return ex;
	}
	
	public static void OpenFile(Context context, SmbFile sbf ){
//		Uri uri = Uri.parse("http://www.baidu.com");//"file:///192.168.0.103/weeklyreport/App/2014/pzhaoyang_testvv/ggg.txt");//sbf.toString());
//		String type = getmimetype(MimeUtils.getextension(sbf.getName()));
		Intent intent = new Intent(Intent.ACTION_VIEW);
//		intent.putExtra(Intent.EXTRA_STREAM, "file:///192.168.0.103/weeklyreport/App/2014/pzhaoyang_testvv/ggg.txt");
//		intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("smb://pengzhaoyang:pengzhaoyang@192.168.0.103/WeeklyReport/App/2014/pzhaoyang_testvv/ggg.txt"));
//		intent.setData(Uri.parse("http://192.168.0.103/WeeklyReport/App/2014/pzhaoyang_testvv/ggg.txt"));
		intent.setData(Uri.parse("http://127.0.0.1:59777/smb/192.168.0.146/106/VID_19800101_123201.3gp"));
		intent.setType("video/*");
//		intent.setType("text/plain");
		context.startActivity(intent);
	}
}