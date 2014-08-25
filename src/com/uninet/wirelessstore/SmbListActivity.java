package com.uninet.wirelessstore;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;

import jcifs.smb.*;

import java.util.*;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SmbListActivity extends ListActivity{

	private String prefix = "smb://";
	private String ip = "";
	private String root = "/";
	private String CurrPath = "";
	private String NativeCurrPath = "";
	private IconifiedText SelectedItem = null;
	
	public static final int REQUEST_CODE_UPLOAD = 1;
	public static final int REQUEST_CODE_DOWNLOAD = 2;
	public static final int REQUEST_CODE_BAK_DIR_SETTING = 3;
	
	
	private Handler mUIUpdate;
	
	private static final int MENU_RENAME_ID = 3;
	private static final int MENU_DELETE_ID = 4;
	private static final int MENU_COPY_ID = 5;
	private static final int MENU_CUT_ID = 6;
	private static final int MENU_PAST_ID = 7;
	private static final int MENU_DOWNLOAD_ID = 8;
	private static final int MENU_UPLOAD_ID = 9;
	private static final int MENU_NEW_FOLDER_ID = 10;
	private static final int MENU_SETTING_ID = 11;
	
	private IconifiedTextListAdapter listItemAdapter = null;
	private IconifiedTextListAdapter dlistadapter = null;
	private SearchTask task = null;
	private ArrayList<IconifiedText> alist = new ArrayList<IconifiedText>();
	private ArrayList<IconifiedText> nativelist = new ArrayList<IconifiedText>();
	
	private String getSmbRoot(String ip){
		return prefix + SmbOpApi.getAuthbyUrl(SmbListActivity.this, ip) + "@" + ip + "/";
	}
	private int getIconbyFile(String filepath){
		int icon = R.drawable.ic_launcher;
		SmbFile file = SmbOpApi.getSmbFileByUrl(filepath);
		
		if(SmbOpApi.isDir(file)){
			icon = R.drawable.folder;
		}else{
			icon = MimeUtils.getIconbyType(MimeUtils.getmimetype(MimeUtils.getextension(file.getName())));
		}
		
		return icon;
	}
	
	private void setAdapter(){
		listItemAdapter = new IconifiedTextListAdapter(SmbListActivity.this);
		listItemAdapter.setListItems(alist);
        setListAdapter(listItemAdapter);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		setAdapter();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ip = getIntent().getStringExtra("url");
		CurrPath = root = getSmbRoot(ip);
		
		setContentView(R.layout.activity_list);
		
		Intent intent = new Intent(SmbListActivity.this, StreamService.class);
		startService(intent);
		GeneralUtil.SetAuth(SmbOpApi.getAuthbyUrl(SmbListActivity.this, ip));
		
		mUIUpdate = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case SmbOpApi.TOAST_MSG_SHOW:
//                	searchFile(CurrPath);
                    Toast.makeText(SmbListActivity.this, (String)msg.obj,
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmbOpApi.PROGRESS_MSG_SHOW:
                	if(SmbOpApi.pdialog != null){
                		SmbOpApi.pdialog.dismiss();
                		SmbOpApi.pdialog = null;
                		searchFile(CurrPath);
                	}
                    break;
                }
                super.handleMessage(msg);
            }
        };
       
        searchFile(root);
        
        getListView().setOnCreateContextMenuListener(this);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
		IconifiedTextListAdapter adapter = (IconifiedTextListAdapter) getListAdapter();
		SelectedItem = (IconifiedText) adapter.getItem(info.position);
		Log.d("abc","menu SelectedItem = " + SelectedItem.getText());
		if(SelectedItem.getText().equals("..")){
			return;
		}
		
		menu.setHeaderTitle("执行操作");
		menu.add(0, MENU_RENAME_ID, 0, "重命名");
		menu.add(0, MENU_DELETE_ID, 0, "删除");
		menu.add(0, MENU_COPY_ID, 0, "复制");
		menu.add(0, MENU_CUT_ID, 0, "剪切");
		menu.add(0, MENU_PAST_ID, 0, "粘贴");
		menu.add(0, MENU_DOWNLOAD_ID, 0, "下载");
		if(!SelectedItem.isFile()){
			menu.add(0, MENU_UPLOAD_ID, 0, "上传");
			menu.add(0, MENU_NEW_FOLDER_ID, 0, "新建目录");
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		showDialog(item.getItemId());
		return super.onContextItemSelected(item);
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
        case MENU_RENAME_ID:
        	EditText et = (EditText) dialog.findViewById(R.id.foldername);
            et.setText(SelectedItem.getText());
            break;
        case MENU_DELETE_ID:
            ((AlertDialog) dialog).setMessage("确认删除 "+ SelectedItem.getText() +" 吗?");
            break;
        case MENU_UPLOAD_ID:
        	browsNativeFile("/storage");
            break;
		}

	}
	@Override
	protected Dialog onCreateDialog(int id) {
		
		switch (id) {
			case MENU_RENAME_ID:
				Log.d("abc", "item rename = " + SelectedItem.getText());
				LayoutInflater inflater = LayoutInflater.from(this);
				View view = inflater.inflate(R.layout.dialog_new_folder, null);
				final EditText et2 = (EditText) view.findViewById(R.id.foldername);
		            return new AlertDialog.Builder(this)
		                    .setTitle("重命名")
		                    .setView(view)
		                    .setPositiveButton(android.R.string.ok,
		                                new OnClickListener() {
		                                    public void onClick(DialogInterface dialog,
		                                            int which) {
		                                    	Message msg = mUIUpdate.obtainMessage(SmbOpApi.TOAST_MSG_SHOW);
		                                    	msg.obj = SmbOpApi.rename(SmbOpApi.getSmbFileByUrl(SelectedItem.getPath()),
		                                    			SmbOpApi.getSmbFileByUrl(CurrPath+et2.getText().toString()));
		            							msg.sendToTarget();
		            							searchFile(CurrPath);
		            							SelectedItem = null;
		                                    }
		                                })
		                    .setNegativeButton(android.R.string.cancel,
		                            new OnClickListener() {
		                                public void onClick(DialogInterface dialog,
		                                        int which) {
		                                    // Cancel should not do anything.
		                                }
		                            }).create();
			case MENU_DELETE_ID:
		            return new AlertDialog.Builder(this)
		                    .setTitle("删除")
		                    .setMessage("确认删除 "+ SelectedItem.getText() +" 吗?")
		                    .setPositiveButton(android.R.string.ok,
		                                new OnClickListener() {
		                                    public void onClick(DialogInterface dialog,
		                                            int which) {
		                                    	
		                                    	Message msg = mUIUpdate.obtainMessage(SmbOpApi.TOAST_MSG_SHOW);
		                                    	msg.obj = SmbOpApi.delete(SmbOpApi.getSmbFileByUrl(SelectedItem.getPath()));
		            							msg.sendToTarget();
		            							searchFile(CurrPath);
		            							SelectedItem = null;
		                                    }
		                                })
		                    .setNegativeButton(android.R.string.cancel,
		                            new OnClickListener() {
		                                public void onClick(DialogInterface dialog,
		                                        int which) {
		                                    // Cancel should not do anything.
		                                }
		                            }).create();

			case MENU_COPY_ID:
				SmbOpApi.copy(mUIUpdate,
						SmbOpApi.TOAST_MSG_SHOW,
						SmbOpApi.getSmbFileByUrl(SelectedItem.getPath()));
				SelectedItem = null;
				break;
			case MENU_CUT_ID:
				SmbOpApi.cut(mUIUpdate,
						SmbOpApi.TOAST_MSG_SHOW,
						SmbOpApi.getSmbFileByUrl(SelectedItem.getPath()));
				SelectedItem = null;
				break;
			case MENU_PAST_ID:
				SmbOpApi.paste(SmbListActivity.this,mUIUpdate,
						SmbOpApi.TOAST_MSG_SHOW,
						SmbOpApi.getSmbFileByUrl(SelectedItem.getPath() + SmbOpApi.CopyorCut_buffer.getName()));
				SelectedItem = null;
				break;
			case MENU_DOWNLOAD_ID:
//                Intent intent = new Intent("org.openintents.action.PICK_DIRECTORY");
//                intent.putExtra("org.openintents.extra.TITLE", "选择目录");
//                startActivityIfNeeded(intent, REQUEST_CODE_DOWNLOAD);

			    view = SetDialogListAdapter(this);
			    
			    browsNativeFile("/storage");
				return new AlertDialog.Builder(this)
                .setTitle("选取下载目录")
                .setView(view)
                .setPositiveButton(android.R.string.ok,
                            new OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int which) {
                        			SmbOpApi.download(SmbListActivity.this,
                        					mUIUpdate,
                        					SmbOpApi.TOAST_MSG_SHOW,
                        					SmbOpApi.getSmbFileByUrl(SelectedItem.getPath()),
                        					NativeCurrPath);
                        			SelectedItem = null;
                                }
                            })
                .setNegativeButton(android.R.string.cancel,
                        new OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                            	NativeCurrPath = "";
                            }
                        }).create();
				
//				break;
			case MENU_UPLOAD_ID:
//                Intent intent = new Intent("org.openintents.action.PICK_FILE");
//                intent.putExtra("org.openintents.extra.TITLE", "选择文件");
//                startActivityIfNeeded(intent, REQUEST_CODE_UPLOAD);
			    view = SetDialogListAdapter(this);
			    
			    browsNativeFile("/storage");
				return new AlertDialog.Builder(this)
                .setTitle("选取上传文件")
                .setView(view)
                .setPositiveButton(android.R.string.ok,
                            new OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                	File f = new File(NativeCurrPath);
                                	SmbOpApi.upload(SmbListActivity.this,
                        					mUIUpdate,
                        					SmbOpApi.TOAST_MSG_SHOW,
                        					SmbOpApi.getSmbFileByUrl((SelectedItem == null ? CurrPath : SelectedItem.getPath()) + f.getName()),
                        					NativeCurrPath);
                        			SelectedItem = null;
                        			dlistadapter.setSelectItem(-1);
                                }
                            })
                .setNegativeButton(android.R.string.cancel,
                        new OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                            	NativeCurrPath = "";
                            	dlistadapter.setSelectItem(-1);
                            }
                        }).create();
//				break;
			case MENU_NEW_FOLDER_ID:
				
				inflater = LayoutInflater.from(this);
				view = inflater.inflate(R.layout.dialog_new_folder, null);
				final EditText et3 = (EditText) view.findViewById(R.id.foldername);
		            return new AlertDialog.Builder(this)
	                    .setTitle("新建文件夹")
	                    .setView(view)
	                    .setPositiveButton(android.R.string.ok,
	                                new OnClickListener() {
	                                    public void onClick(DialogInterface dialog,
	                                            int which) {
	                                    	SmbFile newfolder = SmbOpApi.getSmbFileByUrl(
	                                    			(SelectedItem == null ? CurrPath : SelectedItem.getPath()) + et3.getText().toString());
	                                    	Message msg = mUIUpdate.obtainMessage(SmbOpApi.TOAST_MSG_SHOW);
	                                    	Log.d("abc","newfolder = " + newfolder.toString());
	                                    	msg.obj = SmbOpApi.mkdir(newfolder);
	                                    	msg.sendToTarget();
	                                    	searchFile(CurrPath);
	                                    	SelectedItem = null;
	                                    }
	                                })
	                    .setNegativeButton(android.R.string.cancel,
	                            new OnClickListener() {
	                                public void onClick(DialogInterface dialog,
	                                        int which) {
	                                }
	                            }).create();
		}
		return null;
	}
	
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		switch(requestCode) {
//		case REQUEST_CODE_BAK_DIR_SETTING:
//			break;
//		case REQUEST_CODE_UPLOAD:
//			String encodedfile = URLDecoder.decode(
//					data.getDataString().substring(data.getDataString().indexOf("file:///")+7));
//			
//			String filename = encodedfile.substring(encodedfile.lastIndexOf("/")+1);
//			Log.d(SmbOpApi.TAG,"name = " + filename);
//			Log.d(SmbOpApi.TAG,"CurrPath = " + CurrPath+filename);
//			Log.d(SmbOpApi.TAG,"SelectedItem.getPath() = " + (SelectedItem == null ? "NULL":SelectedItem.getPath()));
//			SmbOpApi.upload(SmbListActivity.this,
//					mUIUpdate,
//					SmbOpApi.TOAST_MSG_SHOW,
//					SmbOpApi.getSmbFileByUrl((SelectedItem == null ? CurrPath : SelectedItem.getPath()) + filename),
//					encodedfile);
//			SelectedItem = null;
//			break;
//		case REQUEST_CODE_DOWNLOAD:
//			String download_folder = data.getDataString().substring(data.getDataString().indexOf("file:///")+7);
//			SmbOpApi.download(SmbListActivity.this,
//					mUIUpdate,
//					SmbOpApi.TOAST_MSG_SHOW,
//					SmbOpApi.getSmbFileByUrl(SelectedItem.getPath()),
//					download_folder);
//			SelectedItem = null;
//			break;
//		default:
//			break;
//		}
//	}
	
	@Override
	public void onBackPressed() {
		if(!CurrPath.equals(root)){
			searchFile(SmbOpApi.getSmbFileByUrl(CurrPath).getParent().toString());
		}else
			super.onBackPressed();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_SETTING_ID, 0, "备份目录设置");
		menu.add(Menu.NONE, MENU_NEW_FOLDER_ID, 0, "新建文件夹");
		menu.add(Menu.NONE, MENU_UPLOAD_ID, 0, "上传文件");
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	
		switch(item.getItemId()){
		case MENU_SETTING_ID:
	        Intent intent = new Intent();
	        intent.setClass(this, WirelessSettingActivity.class);
			startActivity(intent);
            break;
		case MENU_NEW_FOLDER_ID:
			LayoutInflater inflater = LayoutInflater.from(this);
			View view = inflater.inflate(R.layout.dialog_new_folder, null);
			final EditText et3 = (EditText) view.findViewById(R.id.foldername);
	            new AlertDialog.Builder(this)
	                    .setTitle("新建文件夹")
	                    .setView(view)
	                    .setPositiveButton(android.R.string.ok,
	                                new OnClickListener() {
	                                    public void onClick(DialogInterface dialog,
	                                            int which) {
	                                    	SmbFile newfolder = SmbOpApi.getSmbFileByUrl(
	                                    			(SelectedItem == null ? CurrPath : SelectedItem.getPath()) + et3.getText().toString());
	                                    	Message msg = mUIUpdate.obtainMessage(SmbOpApi.TOAST_MSG_SHOW);
	                                    	Log.d("abc","newfolder = " + newfolder.toString());
	                                    	msg.obj = SmbOpApi.mkdir(newfolder);
	                                    	msg.sendToTarget();
	                                    	searchFile(CurrPath);
	                                    	SelectedItem = null;
	                                    }
	                                })
	                    .setNegativeButton(android.R.string.cancel,
	                            new OnClickListener() {
	                                public void onClick(DialogInterface dialog,
	                                        int which) {
	                                }
	                            }).show();
			break;
		case MENU_UPLOAD_ID:
//            intent = new Intent("org.openintents.action.PICK_FILE");
//            intent.putExtra("org.openintents.extra.TITLE", "选择文件");
//            startActivityIfNeeded(intent, REQUEST_CODE_UPLOAD);
//			  break;
			showDialog(MENU_UPLOAD_ID);
			 break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	
	private void showtotal(SmbFile disk){
		String total  = SmbOpApi.getDiskFreeSpace(SmbListActivity.this, disk);
		TextView tv = (TextView)findViewById(R.id.volume_tip);
		tv.setText(total);
	}
	
	
	private void openfile(IconifiedText it){
		String httpReq = "http://" + GeneralUtil.localip + ":" + GeneralUtil.port + "/smb=";
		String path = it.getPath().substring(32);
		String mimetype = MimeUtils.getmimetype(MimeUtils.getextension(it.getText()));
		Uri uri;
		Log.d(SmbOpApi.TAG,"substring path = " + path + ",\nmimetype = " + mimetype);
		
		if( mimetype.contains("image")
			||mimetype.contains("text")
			||mimetype.contains("video")
			||mimetype.contains("audio")){
			try {
				path = URLEncoder.encode(path, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				Log.d(SmbOpApi.TAG,"UnsupportedEncodingException Error!: " + e);
			}
			
			String url = httpReq + path;
			Log.d(SmbOpApi.TAG,"http endoced url : "  + url);
			uri = Uri.parse(url);
			
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(uri, mimetype);
			startActivity(intent);
		}else{
//			SyncLock sycl = new SyncLock();
			File dir = new File(SmbOpApi.tmpdir);
			if(!dir.exists()){
				dir.mkdirs();
			}
			
			SmbOpApi.cachefile(SmbListActivity.this, mUIUpdate, SmbOpApi.TOAST_MSG_SHOW, SmbOpApi.getSmbFileByUrl(it.getPath()), mimetype/*, sycl*/);
//			sycl.waitForReady();
			
//			File file = new File(SmbOpApi.tmpdir + it.getText());
//			uri = Uri.fromFile(file);
//			Log.d(SmbOpApi.TAG," else url : "  + uri.toString());
		}
		

	}
	
	private void searchFile(String path){
		if (task == null || task.getStatus().equals(AsyncTask.Status.FINISHED)){
			new SearchTask().execute(path);
		}
	}
	
	class SearchTask extends AsyncTask<String, Void, Void>{
		ArrayList<IconifiedText> item = new ArrayList<IconifiedText>();
		private ProgressDialog dialog = null;
		
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			dialog = new ProgressDialog(SmbListActivity.this);
			dialog.setMessage("正在加载...");
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();
		}

		@Override
		protected Void doInBackground(String... params){
			try {
				Log.d(SmbOpApi.TAG,"SearchTask");
				Log.d(SmbOpApi.TAG,"SearchTask params[0]" + params[0]);
				SmbFile smbFile = new SmbFile(params[0]);
				ArrayList<SmbFile> dirList = new ArrayList<SmbFile>();
				ArrayList<SmbFile> fileList = new ArrayList<SmbFile>();
	
				setCurrPath(smbFile);
				
				SmbFile[] fs = smbFile.listFiles();
				
				for (SmbFile f : fs){
					if (f.isDirectory()){
						dirList.add(f);
					} else {
						fileList.add(f);
					}
				}
	
				dirList.addAll(fileList);
	
				for (SmbFile f : dirList){
					String filePath = f.getPath();
					String fileName = f.getName();
					boolean isFile = f.isFile();
					String fileSize = Formatter.formatFileSize(SmbListActivity.this, f.length());
					Drawable dab = getResources().getDrawable(
							getIconbyFile(filePath));
					/*IconifiedText(String filename, String filepath,boolean isfile, Drawable fileicon,);*/
					if(isFile){
						item.add(new IconifiedText(fileName, filePath, isFile, fileSize, dab));
					}else{
						item.add(new IconifiedText(fileName, filePath, isFile, dab));
					}
				}
	
			}catch (MalformedURLException e){
					Log.d(SmbOpApi.TAG, "MalformedURLException = " + e);
			}catch (SmbException e){
					Log.d(SmbOpApi.TAG, "SmbException = " + e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			alist.clear();
			if(!isRoot()){
				String up = SmbOpApi.getSmbFileByUrl(CurrPath).getParent().toString();
				alist.add(new IconifiedText("..", up, false, getResources().getDrawable(R.drawable.folder)));
			}
			
			for (IconifiedText i : item){
				alist.add(i);
			}

			dialog.cancel();
			showtotal(SmbOpApi.getSmbFileByUrl(CurrPath));
			listItemAdapter.notifyDataSetChanged();
		}

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Log.d("abc","aaaaaa");
		IconifiedText it = alist.get(position);
		
		String path = it.getPath();
		CurrPath = path;
		Log.d(SmbOpApi.TAG,"CurrPath = " + CurrPath);
		if(it.isFile()){
			openfile(it);
		}else{
			searchFile(path);
		}
	}

	private void setCurrPath(SmbFile sbf){
		if(!sbf.toString().equals(root)){
			CurrPath = sbf.toString();
		}else{
			CurrPath = root;
		}
	}
	
	private boolean isRoot(){
		return CurrPath.equals(root);
	}
	
	private View SetDialogListAdapter(Context context){
		LayoutInflater inflater = LayoutInflater.from(context);
	    View view = inflater.inflate(R.layout.dialog_list, null);
	    ListView lv  = (ListView)view.findViewById(R.id.dialog_list);
	    

	    lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				IconifiedText it = nativelist.get(arg2);
				
				if(it.isFile()){
					NativeCurrPath = it.getPath();
					dlistadapter.setSelectItem(arg2);
					dlistadapter.notifyDataSetInvalidated();  
				}else{
					browsNativeFile(it.getPath());
				}
			}
		});
	    
	    
	    
		dlistadapter = new IconifiedTextListAdapter(this);
		dlistadapter.setListItems(nativelist);
		lv.setAdapter(dlistadapter);
		return view;
	}
	
	private void browsNativeFile(String path){
		if (task == null || task.getStatus().equals(AsyncTask.Status.FINISHED)){
			new BrowsLocalFileTask().execute(path);
		}
	}
	
	class BrowsLocalFileTask extends AsyncTask<String, Void, Void>{
		ArrayList<IconifiedText> item = new ArrayList<IconifiedText>();
		private ProgressDialog dialog = null;
		
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			dialog = new ProgressDialog(SmbListActivity.this);
			dialog.setMessage("正在加载...");
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();
		}

		@Override
		protected Void doInBackground(String... params){
				Log.d(SmbOpApi.TAG,"BrowsLocalFileTask");
				Log.d(SmbOpApi.TAG,"BrowsLocalFileTask params[0]" + params[0]);
				
				File lf = new File(params[0]);
				ArrayList<File> dlist = new ArrayList<File>();
				ArrayList<File> flist = new ArrayList<File>();
				setNativeCurrPath(lf);

				File[] fs = lf.listFiles();
				
				for (File f : fs){
					if (f.isDirectory()){
						dlist.add(f);
					} else {
						flist.add(f);
					}
				}
	
				dlist.addAll(flist);
	
				for (File f : dlist){
					String filePath = f.getPath();
					String fileName = f.getName();
					boolean isFile = f.isFile();
					String fileSize = Formatter.formatFileSize(SmbListActivity.this, f.length());
					Drawable dab = getResources().getDrawable(
							getIconbyNativeFile(filePath));
					/*IconifiedText(String filename, String filepath,boolean isfile, Drawable fileicon,);*/
					if(isFile){
						item.add(new IconifiedText(fileName, filePath, isFile, fileSize, dab));
					}else{
						item.add(new IconifiedText(fileName, filePath, isFile, dab));
					}
				}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			nativelist.clear();
			if(!isNativeRoot()){
				String up = new File(NativeCurrPath).getParent();
				nativelist.add(new IconifiedText("..", up, false, getResources().getDrawable(R.drawable.folder)));
			}
			
			for (IconifiedText i : item){
				nativelist.add(i);
			}

			dialog.cancel();
			dlistadapter.notifyDataSetChanged();
		}

	}
	private boolean isNativeRoot(){
		return NativeCurrPath.equals("/storage");
	}
	

	private void setNativeCurrPath(File f){
		if(!f.toString().equals("/storage")){
			NativeCurrPath = f.toString();
		}else{
			NativeCurrPath = "/storage";
		}
	}
	
	private int getIconbyNativeFile(String filepath){
		int icon = R.drawable.ic_launcher;
		File f = new File(filepath);
		
		if(f.isDirectory()){
			icon = R.drawable.folder;
		}else{
			icon = MimeUtils.getIconbyType(MimeUtils.getmimetype(MimeUtils.getextension(f.getName())));
		}
		
		return icon;
	}
}