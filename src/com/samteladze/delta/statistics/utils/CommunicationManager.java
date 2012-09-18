package com.samteladze.delta.statistics.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import android.content.Intent;
import android.net.Uri;

public class CommunicationManager
{
	private static final String SERVER_URL = "http://xdp-apps.org/stat-collector/collect";
	private static final String MAIL_TEXT = "Current statistics.";
	private static final String MAIL_SUBJECT = "Delta Statistics";
	private static final String MAIL_ADDRESS = "delta.statistics@gmail.com";
	
	public static Intent CreatEmailDataIntent()
	{
		ArrayList<Uri> filesUri = new ArrayList<Uri>();
		Uri appFileUri = Uri.fromFile(FileManager.GetFile(FileManager.USER_APP_STAT_FILE_PATH));
		Uri netFileUri = Uri.fromFile(FileManager.GetFile(FileManager.USER_NET_STAT_FILE_PATH));		
		filesUri.add(appFileUri);
		filesUri.add(netFileUri);
		
		Intent sendIntent = new Intent(Intent.ACTION_SEND);
		sendIntent.setType("text/plain");
		sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { MAIL_ADDRESS });
		sendIntent.putExtra(Intent.EXTRA_SUBJECT, MAIL_SUBJECT);
		sendIntent.putExtra(Intent.EXTRA_TEXT, MAIL_TEXT);
		sendIntent.putExtra(Intent.EXTRA_STREAM, filesUri);

		return sendIntent;
	}

	public static boolean SendAppStatisticsToServer()
	{
		File fileToSend = FileManager.GetFile(FileManager.APP_STAT_FILE_PATH);
		
		try
		{
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(SERVER_URL);

			InputStreamEntity reqEntity = new InputStreamEntity(new FileInputStream(fileToSend), -1);
			reqEntity.setContentType("binary/octet-stream");
			reqEntity.setChunked(true);
			
			httppost.setEntity(reqEntity);

			@SuppressWarnings("unused")
			HttpResponse response = httpclient.execute(httppost);
			
			LogManager.Log(CommunicationManager.class.getSimpleName(), "App statistics was sent to the server");
			
			return true;
		} 
		catch (Exception e)
		{			
			e.printStackTrace(System.err);
			
			LogManager.Log(CommunicationManager.class.getSimpleName(), "ERROR! Could not send app statistics to the server");
			LogManager.Log(CommunicationManager.class.getSimpleName(), e.toString());
		}
		
		return false;
	}
	
	public static boolean SendDataToServer(String deviceID)
	{
		File appFile = FileManager.GetFile(FileManager.APP_STAT_FILE_PATH);
		File netFile = FileManager.GetFile(FileManager.NET_STAT_FILE_PATH);
		File logFile = FileManager.GetFile(LogManager.LOG_FILE_PATH);
		
		try
		{
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(SERVER_URL);

			FileBody appBin= new FileBody(appFile);
			FileBody netBin= new FileBody(netFile);
			FileBody logBin= new FileBody(logFile);
			
			MultipartEntity reqEntity = new MultipartEntity();
			reqEntity.addPart("appFile", appBin);
			reqEntity.addPart("netFile", netBin);
			reqEntity.addPart("logFile", logBin);
			reqEntity.addPart("id", new StringBody(deviceID));
			
			httppost.setEntity(reqEntity);

			@SuppressWarnings("unused")
			HttpResponse response = httpclient.execute(httppost);
			
			LogManager.Log(CommunicationManager.class.getSimpleName(), "Statistics and log were sent to the server");
			
			return true;
		} 
		catch (Exception e)
		{			
			e.printStackTrace(System.err);
			
			LogManager.Log(CommunicationManager.class.getSimpleName(), "ERROR! Could not send statistics and log to the server");
			LogManager.Log(CommunicationManager.class.getSimpleName(), e.toString());
		}
		
		return false;
	}
}
