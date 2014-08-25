package com.uninet.wirelessstore;

import java.net.UnknownHostException;

import jcifs.smb.SmbAuthException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.text.format.Formatter;
import android.util.Log;

public class MainActivity extends Activity {
	
	//const values
	private static String default_url = "192.168.0.103";
	private static final int UPDATE_TIP = 1;
    private static final int PROGRESS_MSG_SHOW = 2;
	
	private String user_passwd ="";
	public static ProgressDialog  pdialog;
	
	private void gotolist(){
        Intent intent = new Intent();
        intent.setClass(this, SmbListActivity.class);
        intent.putExtra("url",default_url);
		startActivity(intent);
		Log.d(SmbOpApi.TAG,"Entry ListView...");
	}
    
	//获取登录信息
    private String getInputs(){
    	String auth = "";
        auth = ((EditText)findViewById(R.id.ed_user)).getText().toString()
        		+":" + ((EditText)findViewById(R.id.ed_passwd)).getText().toString();
        Log.d(SmbOpApi.TAG,"Login info: " + auth);
        return auth;
    }
    
	Handler progresshandler =new Handler(){
		   @Override
		   //当有消息发送出来的时候就执行Handler的这个方法
		   public void handleMessage(Message msg){
		      super.handleMessage(msg);
		      
		      switch(msg.what){
		      	case UPDATE_TIP:
		      		final TextView tip = (TextView)findViewById(R.id.error_tip);
		      		tip.setVisibility(View.VISIBLE);
		      		tip.setText((String)msg.obj);
		      		progresshandler.sendEmptyMessage(PROGRESS_MSG_SHOW);
		    	  break;
		      	case PROGRESS_MSG_SHOW:
		      		if(pdialog != null){
				    	pdialog.dismiss();
				    }else{
				    	Log.d(SmbOpApi.TAG,"pdialog == null");
				    }
		    	  break;
		      default:
		    	  break;
		      }
		   }
		};
    
		
    @Override
	protected void onResume() {
//    	default_url = getGateWay();
        Button button1 = (Button)findViewById(R.id.bt_login);
        button1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d(SmbOpApi.TAG,"Onclick ....");
					user_passwd = getInputs();
					LoginThread();
			}
		});    	
		super.onResume();
	}

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        Log.d(SmbOpApi.TAG,"GateWay: " + getGateWay());
        
        if(!"".equals(SmbOpApi.getAuthbyUrl(MainActivity.this, default_url))){
        	user_passwd = SmbOpApi.getAuthbyUrl(MainActivity.this, default_url);
			LoginThread();
			Log.d(SmbOpApi.TAG,"Auto Login... !");
			return;
        }
    }
    
    private String getGateWay() {
    	WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
    	return  Formatter.formatIpAddress(wifiManager.getDhcpInfo().gateway);
    }
    
   private void setDialogText(float size, View v, String message) {

        if (v instanceof ViewGroup) {
                ViewGroup parent = (ViewGroup) v;
                int count = parent.getChildCount();
                for (int i = 0; i < count; i++) {
                        View child = parent.getChildAt(i);
                        setDialogText(size, child, message);
                }
        } else if (v instanceof TextView) {
        	if(((TextView) v).getText().equals(message)){
                ((TextView) v).setTextSize(size);
        	}
        }
    }
    
    private void LoginDialog(final Thread thread){
		pdialog = new ProgressDialog(MainActivity.this);
		pdialog.setTitle("登录");
		pdialog.setMessage("正在登录...");
		pdialog.setCanceledOnTouchOutside(false);
		pdialog.setCancelable(false);
		pdialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消",  
                new DialogInterface.OnClickListener() {
  
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    	pdialog.dismiss();
                    	thread.interrupt();
                    }
                });
		pdialog.show();
		setDialogText(25,pdialog.getWindow().getDecorView(), "正在登录...");
		thread.start();
    }
    
    private void LoginThread(){
		Thread thread = new Thread(new Runnable(){
			Message msg = progresshandler.obtainMessage();
			@Override
			public void run() {
				try{
					SmbOpApi.login(getApplicationContext(),default_url,user_passwd);
		            msg.what = PROGRESS_MSG_SHOW;
					gotolist();
				}catch(SmbAuthException e){
					msg.what = UPDATE_TIP;
					msg.obj = "用户名或密码错误";
					Log.d(SmbOpApi.TAG,"SmbAuthException Error! :" + e);
				}catch(UnknownHostException e){
					msg.what = UPDATE_TIP;
					msg.obj = "主机错误!";
					Log.d(SmbOpApi.TAG,"UnknownHostException");
				}catch(Exception e){
					msg.what = UPDATE_TIP;
					msg.obj = e.getMessage();
					Log.d(SmbOpApi.TAG,"normal Error!:" + e);
				}
				msg.sendToTarget();
			}
		});
		LoginDialog(thread);
    }
}
