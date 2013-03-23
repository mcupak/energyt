package edu.toronto.cs.energyt;

import edu.toronto.cs.energyt.MainActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;

public class DownloadTask extends AsyncTask<Void, Void, Void> {
	private static final int BUFFER_SIZE = 1024;
	private static final int PREFETCH_SIZE = 100000;
	private MainActivity mainApp;
	private boolean isRunning;
	
	public DownloadTask(MainActivity mainApp) { 
		this.isRunning = false; 
		this.mainApp = mainApp;
	}
	
	private String parseHTMLSource() throws IOException, NullPointerException {
		String link = null;
		FileInputStream fis = new FileInputStream(MainActivity.HTMLSOURCE_FILE);
		DataInputStream dis = new DataInputStream(fis);
		BufferedReader br = new BufferedReader(new InputStreamReader(dis));
		String strLine;
		final String keyword1 = "fmt_stream_map"; //Looking first for this specific format
		final String keyword2 = "\\\"url\\\": \\\""; //Having found the above keyword we look for this one
		int index = -1;
		while((strLine=br.readLine())!=null)
		{
			if ((index = strLine.indexOf(keyword1)) != -1) {
				strLine = strLine.substring(index + keyword1.length());
				index = strLine.indexOf(keyword2);
				strLine = strLine.substring(index + keyword2.length());
				index = strLine.indexOf('\"');
				if (index != -1) {
					// Link is in the same line...
					link = strLine.substring(0, index - 1);
				} else {
					// Link extends also to the next line...
					String secondStrLine = br.readLine();
					index = secondStrLine.indexOf('\"');
					link = strLine.concat(secondStrLine.substring(0, index - 1));
				}
				break; // We have found the link for sure
			}
		}
		if (link == null) { //Release resources and throw exception...
			br.close();
			dis.close();
			fis.close();
			throw new NullPointerException("link is NULL. Cannot retrieve youtube link \n" +
					"from the corresponding html source file!");
		}
		//Transform link to readable format...
		link = link.replaceAll("\\\\u0026", "&");
		link = link.replaceAll("\\\\", "");
        
		br.close();
		dis.close();
		fis.close();
		
        String debugMess = "Link produced: " + link;
        Log.d(MainActivity.TAG, debugMess);
		return link;
	}
	
	private String actualYTLink() throws IOException, NullPointerException {
		String ytLink = null;
		URL url = new URL(((EditText) mainApp.findViewById(R.id.edtURL)).getText().toString());
		String debugMess = "User's Link: " + url.toString();
		Log.d(MainActivity.TAG, debugMess);
		Log.d(MainActivity.TAG, "Request youtube html source code for a video.");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.connect();
		BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
		FileOutputStream fos = new FileOutputStream(MainActivity.HTMLSOURCE_FILE);
		BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);
		byte[] data = new byte[BUFFER_SIZE];
		int j = 0;
		while ((j = bis.read(data, 0, 1024)) != -1) {
			bos.write(data, 0, j);
		}
		bis.close();
		bos.close();
		fos.close();
		Log.d(MainActivity.TAG, "End of request of html source code.");
		
		Log.d(MainActivity.TAG, "Parsing HTML Source code.");
		ytLink = parseHTMLSource();
		Log.d(MainActivity.TAG, "End of parsing html source code.");
		
		return ytLink;
	}
	
	@Override
	protected Void doInBackground(Void... unused) {
		Log.d(MainActivity.TAG, "AsyncTask: in doInBackground for YT.");
		this.isRunning = true;
		URL url = null;
		InputStream is = null;
		BufferedInputStream bis = null;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		
		try {
			
			

			
			
			
			
			Log.d(MainActivity.TAG, "AsyncTask: doInBackground: In try.");
			url = new URL(actualYTLink());
			/* Establish HTTP request to http source code of Youtube */
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			/* Define InputStreams to read from the URLConnection. */
			is = connection.getInputStream();
			bis = new BufferedInputStream(is);
			fos = new FileOutputStream(new File(MainActivity.OTHER_FILE));
			bos = new BufferedOutputStream(fos);
			//socket = new Soc

			// prefetch a few bytes before starting playing
			byte data[] = new byte[1024];
			int j;
			int count = 0;
			boolean notActivate = true;
			Log.d(MainActivity.TAG, "Prefetching started.");
			//Below while loop downloads the entire video file
			while ((j = bis.read(data)) != -1) {
				
				//TODO
				//if(connection is low)
				//	connection.
				
				
				if (isCancelled()) { //the thread has been cancelled!
					Log.d(MainActivity.TAG, "Download cancelled.");
					break;
				}
				fos.write(data, 0, j);
				count+=j;
				
				//If prefetch is done, startup mediaplayer via publishProgress(null)
				if(notActivate && count > 0 && count > PREFETCH_SIZE)
				{
					Log.d(MainActivity.TAG, "Playback called.");
					publishProgress();
					count = -1;
					notActivate = false;
				}
			}
			
			bis.close();
			bos.close();
			fos.close();
			
			Log.d(MainActivity.TAG, "Download finished.");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	protected void onPreExecute() {
		Log.d(MainActivity.TAG, "AsyncTask: Task started.");
	}

	@Override
	protected void onPostExecute(Void unused) {
		Log.d(MainActivity.TAG, "Task finished.");
	}

	@Override
	protected void onProgressUpdate(Void... values) {
		mainApp.playVideo();
		//Connect to Server and play
	}

}
