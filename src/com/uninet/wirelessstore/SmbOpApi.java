package com.uninet.wirelessstore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import jcifs.smb.SmbSession;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;

public class SmbOpApi{
	public static String TAG = "smbapp";
	
	public static final int TOAST_MSG_SHOW = 1000;
	public static final int PROGRESS_MSG_SHOW = 1001;

	public static String tmpdir = "";//Environment.getDownloadCacheDirectory().getPath() + mcontext.getPackageName() + "/tmp";// "/sdcard/Android/data/com.uninet.wirelessstore/tmp/";
	public static SmbFile CopyorCut_buffer = null;
	public static int CopyorCut_flag = -1;
	public static ProgressDialog  pdialog = null;
	
	
	//保存登录信息和ip到文件
	private static void saveAuth(Context context,String ip,String user_auth){
		SharedPreferences sp = context.getSharedPreferences("smburlinfo",0);
		Editor ed = sp.edit();
		ed.putString(ip, user_auth);
		ed.commit();
		tmpdir = Environment.getExternalStorageDirectory()
				+"/Android/"
				+ context.getPackageName()
				+ "/.cache/";
		Log.d(TAG,"tmpdir = " + tmpdir);
		Log.d(TAG,"Save Auth Success !");
	}
	
	public static String getAuthbyUrl(Context context, String ip){
		SharedPreferences sp = context.getSharedPreferences("smburlinfo", 0);
		return sp.getString(ip, "");
	}
	
	public static SmbFile getSmbFileByUrl(String smburl){
		try {
			return new SmbFile(smburl);
		} catch(MalformedURLException e){
			Log.d(SmbOpApi.TAG,"MalformedURLException Error! :" + e.getMessage());
		} catch(Exception e){
			Log.d(SmbOpApi.TAG,"normal Exception Error! :" + e.getMessage());
		}
		return null;
	}
	
	//登录
    public static void login(Context context, String ip,String user_auth) throws Exception {
        UniAddress dc = UniAddress.getByName(ip);
        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(user_auth);
        SmbSession.logon(dc,auth);
        Log.d(TAG,"Login " + ip+ " Success!");
        saveAuth(context, ip, user_auth);
    }
    
	public static String mkdir(SmbFile sbf){
		String result_msg;
		
		try {
			sbf.mkdir();
			result_msg = "Create Folder " + sbf.getName() + " Success!";
		} catch (SmbException e) {
			result_msg = "Create Folder " + sbf.getName() + " Failed!";
			Log.d(TAG,"mkdir Error!:" + e.getMessage());
		}
		
		return result_msg;
	}
	
	public static String delete(SmbFile sbf){
		String result_msg = "";

		try {
			sbf.delete();
			result_msg = "Delete " + sbf.getName() + " Success!";
		} catch (SmbException e) {
			result_msg = "Delete " + sbf.getName() + " Failed!";
			Log.d(TAG,"delete Error!: " + e.getMessage());
		}
		
		return result_msg;
	}
	
	public static String rename(SmbFile from, SmbFile to){
		String result_msg;
		
		try {
			Log.d(TAG,"to == " + to.toString());
			from.renameTo(to);
			result_msg = "Rename " + from.getName() + " to " + to.getName() + " Success!";
		} catch (SmbException e) {
			result_msg = "Rename " + from.getName() + " to " + to.getName() + " Failed!";
			Log.d(TAG,"rename Error!:" + e.getMessage());
		}
		return result_msg;
	}
	
	public static void paste(Context context, final Handler uihander,final int what,final SmbFile des){
		
		if(uihander == null || des == null){
			Log.d(TAG,"paste param error!");
			return;
		}
		
		if(CopyorCut_buffer == null){
			Message msg1 = uihander.obtainMessage(what);
			msg1.obj = "Please seleted a file or folder first.";
			msg1.sendToTarget();
			return;
		}
		
		pdialog = ProgressDialog.show(context, "粘贴", "正在粘贴...");
		
		 new Thread(new Runnable() {
			Message msg = uihander.obtainMessage(what);
			 
			@Override
			public void run() {
				try {
					CopyorCut_buffer.copyTo(des);
					if(CopyorCut_flag == 1){
						CopyorCut_buffer.delete();
					}
					msg.obj = CopyorCut_flag == 1 ? "Cut ":"Copy " + CopyorCut_buffer.getName() + " Success!";
				} catch (SmbException e) {
					msg.obj = CopyorCut_flag == 1 ? "Cut ":"Copy " + CopyorCut_buffer.getName() + " Failed!";
					Log.d(TAG,"paste Error !:" + e.getMessage());
				} catch(Exception e){
					msg.obj = e.getMessage();
					Log.d(TAG,"Error :" + e.getMessage());
				}
				//close progressDialog
				uihander.sendEmptyMessage(PROGRESS_MSG_SHOW);
				
				msg.sendToTarget();
				CopyorCut_buffer = null;
				CopyorCut_flag = -1;
			}
		 }).start();
	}
	
	public static void copy(final Handler uihander,final int what,SmbFile slectedfile){
		if(uihander == null || slectedfile == null){
			Log.d(TAG,"copy param error!");
			return;
		}
		
		Message msg = uihander.obtainMessage(what);
		CopyorCut_flag = 0;
		CopyorCut_buffer = slectedfile;
		msg.obj = "Copy "+ CopyorCut_buffer.getName() + " Success!";
		msg.sendToTarget();
	}
	
	public static void cut(final Handler uihander,final int what,SmbFile slectedfile){
		if(uihander == null || slectedfile == null){
			Log.d(TAG,"copy param error!");
			return;
		}
		
		Message msg = uihander.obtainMessage(what);
		CopyorCut_flag = 1;
		CopyorCut_buffer = slectedfile;
		msg.obj = "Cut "+ CopyorCut_buffer.getName() + " Success!";
		msg.sendToTarget();
	}

	public static void download(Context context, final Handler uihander,final int what,final SmbFile seletcedfile, final String target_folder){
		
		if(uihander == null || seletcedfile == null || target_folder == null){
			Log.d(TAG,"paste param error!");
			return;
		}
		
		 Thread thread = new Thread(new Runnable() {
			Message msg = uihander.obtainMessage(what);
			 
			@Override
			public void run() {
				try {
			        SmbFileInputStream in = new SmbFileInputStream(seletcedfile);
			        FileOutputStream out = new FileOutputStream(target_folder + "/" + seletcedfile.getName());

			        byte[] b = new byte[8192];
			        int n,total = 0;
			        while(( n = in.read( b )) > 0 ) {
			            out.write( b, 0, n );
			            total += n;
			            setProgressValue(total,getsize(seletcedfile));
			            Log.d(TAG,"download total = " + total +", length = " + seletcedfile.length());
			        }
			        in.close();
			        out.close();
			        msg.obj = "Download " + seletcedfile.getName() + "Success!";
				} catch (SmbException e) {
					msg.obj = "Download File Error!:"+e.getMessage();
					Log.d(TAG,"Download File Error!:"+e.getMessage());
				} catch (FileNotFoundException e){
					msg.obj = "Save File Error!:"+e.getMessage();
					Log.d(TAG,"Save File Error!:"+e.getMessage());
				} catch (IOException e){
					msg.obj = "IOException Error!:"+e.getMessage();
					Log.d(TAG,"IOException Error!:"+e.getMessage());
					
				} catch(Exception e){
					msg.obj = "Download normal Error!:"+e.getMessage();
					Log.d(TAG,"Download normal Error!:"+e.getMessage());
				}
				//close progressDialog
				uihander.sendEmptyMessage(PROGRESS_MSG_SHOW);
				msg.sendToTarget();
				Log.d(TAG,"download Run End!");
			}
		 });
		 setProgressDialog(context,"下载","正在下载 " + seletcedfile.getName() + " :",thread);
	}


	public static void upload(Context context, final Handler uihander,final int what,final SmbFile savedfile, final String pickedfie){
		
		Log.d(TAG,"upload target path = " + savedfile);
		Thread thread = new Thread(new Runnable() {
			Message msg = uihander.obtainMessage(what);
			
			@Override
			public void run() {
				try {
			        FileInputStream in = new FileInputStream(pickedfie);
			        SmbFileOutputStream out = new SmbFileOutputStream(savedfile);
	
			        byte[] b = new byte[8192];
			        int n, tot = 0;
			        long filelen = (new File(pickedfie)).length();
			        while(( n = in.read( b )) > 0 ) {
			            out.write( b, 0, n );
			            tot += n;
			            setProgressValue(tot,filelen);
			            Log.d(TAG,"upload tot = " + tot +", length = " + filelen);
			        }
				    in.close();
				    out.close();
				    msg.obj = "Upload " + savedfile.getName() + "Success!";
				} catch (SmbException e) {
					msg.obj = "Upload File Error!:"+e.getMessage();
					Log.d(TAG,"Upload File Error!:"+e.getMessage());
				}catch(FileNotFoundException e) {
					msg.obj = "Picked File Error!:"+e.getMessage();
					Log.d(TAG,"Picked File Error!:"+e.getMessage());
				} catch(Exception e){
					msg.obj = "Upload normal Error!:"+e.getMessage();
					Log.d(TAG,"Upload normal Error!:"+e.getMessage());
				}
				//close progressDialog
				uihander.sendEmptyMessage(PROGRESS_MSG_SHOW);
				msg.sendToTarget();
			}
		 });
		
		setProgressDialog(context,"上传","正在上传 " + savedfile.getName() + " :",thread);
	}
	
	public static boolean isDir(SmbFile sbf){
		boolean isdir = false;
		
		if(sbf == null) return false;
		
		try{
			isdir = sbf.isDirectory();
		}catch(SmbException e){
			Log.d(SmbOpApi.TAG,"e = " + e.getMessage());
		}
		Log.d(SmbOpApi.TAG,"curnode " + sbf.getName() + " is dir = " + isdir);
		return isdir;
	}
	
	public static String[] getListArray(Handler uihander, SmbFile sbf){
		if(sbf != null){
			try{
				return sbf.list();
			}catch(SmbException e){
				Message msg = uihander.obtainMessage(TOAST_MSG_SHOW);
				msg.obj = "No Permission !";
				msg.sendToTarget();
				Log.d(SmbOpApi.TAG,"getListLength Error:" + e.getMessage());
			}
		}
		
		return new String[0];
	}
	
	private static long getsize(SmbFile sbf){
		long size = 0L;
		try {
			size = sbf.length();
		} catch (SmbException e) {
			Log.d(SmbOpApi.TAG,"getsize Error:" + e.getMessage());
		}
		return size;
	}
	
	public static String getFileSize(Context context, SmbFile sbf){
		long size = 0L;
		try {
			size = sbf.length();
		} catch (SmbException e) {
			Log.d(SmbOpApi.TAG,"getFileSize Error:" + e.getMessage());
		}
		return Formatter.formatFileSize(context,size);
	}
	
	private static void setProgressDialog(Context context, String title, String message,final Thread thread){
		pdialog = new ProgressDialog(context);
		pdialog.setTitle(title);
		pdialog.setMessage(message);
		pdialog.setCanceledOnTouchOutside(false);
		pdialog.setMax(100);
		pdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pdialog.setButton(DialogInterface.BUTTON_POSITIVE, "隐藏",
	                new DialogInterface.OnClickListener() {
	  
	                    @Override  
	                    public void onClick(DialogInterface dialog, int which) {
	                    	pdialog.hide();
	                    }
	                });
		pdialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消",  
	                new DialogInterface.OnClickListener() {
	  
	                    @Override
	                    public void onClick(DialogInterface dialog, int which) {
	                    	pdialog.dismiss();
	                    	thread.interrupt();
	                    }
	                });
		pdialog.show();
		thread.start();
	}
	
	private static void setProgressValue(int total,long filelength){
		 pdialog.setProgress((int)((long)total*100/filelength));
	}
	
	public static String getDiskFreeSpace(Context context, SmbFile dir){
		long size = 0L;
		long free = 0L;
		try {
			free = dir.getDiskFreeSpace();
			size = dir.length();
		} catch (SmbException e) {
			Log.d(TAG,"getDiskFreeSpace Error!: " + e.getMessage());
		}
	
		return "剩余:"+Formatter.formatFileSize(context,free);//+"/" +"总共:"+ Formatter.formatFileSize(context,size);
	}
	
	public static void cachefile(final Context context, final Handler uihander, final int what, final SmbFile seletcedfile, final String mimetype/*,final SyncLock sycl*/){
	
		 Thread thread = new Thread(new Runnable() {
			 Message msg = uihander.obtainMessage(what);
			@Override
			public void run() {
				try {
			        SmbFileInputStream in = new SmbFileInputStream(seletcedfile);
			        FileOutputStream out = new FileOutputStream(tmpdir + seletcedfile.getName());

			        byte[] b = new byte[8192];
			        int n,total = 0;
			        while(( n = in.read( b )) > 0 ) {
			            out.write( b, 0, n );
			            total += n;
			            setProgressValue(total,getsize(seletcedfile));
			        }
			        in.close();
			        out.close();
			        
				} catch (SmbException e) {
					Log.d(TAG,"Cache File Error!:"+e.getMessage());
				} catch (FileNotFoundException e){
					Log.d(TAG,"Save File Error!:"+e.getMessage());
				} catch (IOException e){
					Log.d(TAG,"IOException Error!:"+e.getMessage());
				} catch(Exception e){
					Log.d(TAG,"Cache normal Error!:"+e.getMessage());
				}
				//close progressDialog
				uihander.sendEmptyMessage(PROGRESS_MSG_SHOW);
//				uihander.sendEmptyMessage(OPEN_FILE);
//				sycl.setSync();
				File file = new File(SmbOpApi.tmpdir + seletcedfile.getName());
				
				Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
				Uri uri = Uri.fromFile(file);
				intent.setDataAndType(uri, mimetype);
				try {
					context.startActivity(intent);
				} catch (ActivityNotFoundException e) {
					msg.obj = "未知文件";
					msg.sendToTarget();
			    }
			}
		 });
		 setProgressDialog(context,"等待","正在加载... " + seletcedfile.getName() + " :",thread);
	}
}