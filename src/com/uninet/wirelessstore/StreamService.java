package com.uninet.wirelessstore;

import org.cybergarage.http.HTTPServerList;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class StreamService extends Service{
	
	private FileServer fileServer = null;

	@Override
	public IBinder onBind(Intent intent){
		return null;
	} 
	
	@Override
	public void onCreate(){
		super.onCreate(); 
		android.util.Log.d("abc","StreamService start ....");
		fileServer = new FileServer();
		fileServer.start();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		
		HTTPServerList httpServerList = fileServer.getHttpServerList();
		httpServerList.stop(); 
		httpServerList.close(); 
		httpServerList.clear(); 
		fileServer.interrupt(); 
	}

}
