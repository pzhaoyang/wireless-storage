package com.uninet.wirelessstore;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import org.cybergarage.http.HTTPRequest;
import org.cybergarage.http.HTTPResponse;
import org.cybergarage.http.HTTPServerList;
import org.cybergarage.http.HTTPStatus;
public class FileServer extends Thread implements org.cybergarage.http.HTTPRequestListener{
	public static final String CONTENT_EXPORT_URI = "/smb";
	private HTTPServerList httpServerList = new HTTPServerList();

	private int HTTPPort = 2222;
	private String bindIP = null;

	public String getBindIP(){
		return bindIP;
	}

	public void setBindIP(String bindIP){
		this.bindIP = bindIP;
	}

	public HTTPServerList getHttpServerList(){
		return httpServerList;
	}

	public void setHttpServerList(HTTPServerList httpServerList){
		this.httpServerList = httpServerList;
	}

	public int getHTTPPort(){
		return HTTPPort;
	}

	public void setHTTPPort(int hTTPPort){
		HTTPPort = hTTPPort;
	}

	@Override
	public void run(){
		super.run();
		int retryCnt = 0;
		int bindPort = getHTTPPort();
		HTTPServerList hsl = getHttpServerList();
		android.util.Log.d("abc","while befor =====");
		while (hsl.open(bindPort) == false){
			android.util.Log.d("abc","while in retryCnt ==" + retryCnt);	
			retryCnt++;
			if (100 < retryCnt){
				return;
			}
			setHTTPPort(bindPort + 1);
			bindPort = getHTTPPort();
		}
		android.util.Log.d("abc","while after =====");
		hsl.addRequestListener(this);
		hsl.start(); 
		
		GeneralUtil.localip = hsl.getHTTPServer(0).getBindAddress();
		GeneralUtil.port = hsl.getHTTPServer(0).getBindPort();
	}

	@Override
	public void httpRequestRecieved(HTTPRequest httpReq){
		String uri = httpReq.getURI();
		if (uri.startsWith(CONTENT_EXPORT_URI) == false){
			httpReq.returnBadRequest();
			return;
		}

		try{
			uri = URLDecoder.decode(uri, "UTF-8");
		}catch (UnsupportedEncodingException e1){ 
			e1.printStackTrace();
		}

		String filePaths = "smb://" + GeneralUtil.getAuth() + "@" + uri.substring(5);
		android.util.Log.d("abc","filePaths = " + filePaths);
		int indexOf = filePaths.indexOf("&");

		if (indexOf != -1){
			filePaths = filePaths.substring(0, indexOf);
		}

		try{
			SmbFile file = new SmbFile(filePaths);
			long contentLen = file.length();
			String contentType = GeneralUtil.getFileType(filePaths);
			InputStream contentIn = file.getInputStream();
			if (contentLen <= 0 || contentType.length() <= 0
					|| contentIn == null){
				httpReq.returnBadRequest();
				return;
			}

			HTTPResponse httpRes = new HTTPResponse();
			httpRes.setContentType(contentType);
			httpRes.setStatusCode(HTTPStatus.OK);
			httpRes.setContentLength(contentLen);
			httpRes.setContentInputStream(contentIn);
			httpReq.post(httpRes);

		    contentIn.close(); 
		}catch (MalformedURLException e){
			httpReq.returnBadRequest();
			return;
		}catch (SmbException e){
			httpReq.returnBadRequest();
			return;
		}catch (IOException e){
			httpReq.returnBadRequest();
			return;
		}
	}
}
