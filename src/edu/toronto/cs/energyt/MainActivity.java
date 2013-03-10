package edu.toronto.cs.energyt;

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
import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

/**
 * Main player activity.
 * 
 * @author mcupak
 * 
 */
public class MainActivity extends Activity {
	private static final String DEFAULT_FILE = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/Download/small.mp4";
	private static final String OTHER_FILE = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/Download/other.mp4";
	private static final String HTMLSOURCE_FILE = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/Download/html_source.html";
	private static final String DEFAULT_URL = "http://techslides.com/demos/sample-videos/small.mp4";
//	private static final String YOUTUBE_URL = "http://r4---sn-tt17rn7e.c.youtube.com/videoplayback?upn=I6GoCUDtvg8&source=youtube&sparams=algorithm%2Cburst%2Ccp%2Cfactor%2Cgcr%2Cid%2Cip%2Cipbits%2Citag%2Csource%2Cupn%2Cexpire&cp=U0hVR1dLUV9KTkNONV9PRVVEOktzTFptWTFrS1N3&ms=au&key=yt1&factor=1.25&ip=128.100.27.16&expire=1362703354&itag=34&mt=1362681980&mv=m&gcr=ca&signature=451CDA55101F74018904AE51D0E5B25A2002BB30.B9CA458DBA7A277375BC0834BAAB9D694CBF8823&newshard=yes&algorithm=throttle-factor&burst=40&ipbits=8&fexp=920704%2C912806%2C902000%2C929901%2C913605%2C925006%2C906938%2C931202%2C931401%2C908529%2C920201%2C930101%2C930603%2C900816%2C906834%2C926403&id=b9867330fb26e9ce&sver=3";
	private static final String YOUTUBE_URL = "http://www.youtube.com/watch?v=Vjm3KPGab5o";
	//private static final String YOUTUBE_URL = "http://r7---sn-tt17rn7s.c.youtube.com/videoplayback?key=yt1&cp=U0hVR1lLVV9LU0NONV9RRVlBOnZDcFh6LXhUNmdK&mv=m&signature=80962A2C0064350364CA43FE71F0C11DBF388122.74DB1C7CD0A92D8FDA0312840162A1972C350F3C&fexp=920704%2C912806%2C902000%2C919512%2C929901%2C913605%2C925006%2C906938%2C931202%2C931401%2C908529%2C930803%2C920201%2C930101%2C930603%2C900816%2C906834%2C926403&el=watch&mt=1362881593&itag=18&app=youtube_mobile&ip=199.7.156.148&ms=au&newshard=yes&source=youtube&id=5639b728f19a6f9a&sparams=cp%2Cid%2Cip%2Cipbits%2Citag%2Cratebypass%2Csource%2Cupn%2Cexpire&upn=rhj1vsGAoQk&expire=1362907069&ipbits=8&ratebypass=yes&yms=qFqVb-bQ8eE&dnc=1&sver=3";
	//private static final String YOUTUBE_URL = "http://www.youtube.com/watch?v=I6QLJUF2wqY";
	//private static final String YOUTUBE_URL = "http://r6---sn-tt17rn7l.c.youtube.com/videoplayback?signature=228C9EFCF77AE5B8ADE1D4A77ADC0866770D8022.80C9FEFA8CCDEF3D3AD662D24293D4845C884DA0&ratebypass=yes&sver=3&fexp=906918%2C903806%2C916625%2C902546%2C920704%2C912806%2C902000%2C919512%2C929901%2C913605%2C925006%2C906938%2C931202%2C931401%2C908529%2C930803%2C920201%2C930101%2C930603%2C906834&ipbits=8&newshard=yes&key=yt1&ip=199.7.156.148&ms=au&mv=m&mt=1362886873&id=23a40b254176c2a6&sparams=cp%2Cid%2Cip%2Cipbits%2Citag%2Cratebypass%2Csource%2Cupn%2Cexpire&upn=KlRNai9fgA4&cp=U0hVR1lMTl9OUUNONV9RRlJCOkZobzBMaDQ1NHNm&el=watch&itag=36&dnc=1&source=youtube&expire=1362910197&app=youtube_mobile&yms=Ckp6Nc575nc";
	//private static final String OTHER_URL = "http://web.law.duke.edu/cspd/contest/finalists/viewentry.php?file=army";
	private static final String TAG = "energyt";
	private VideoView video;
	private String current;
	private boolean isPlaying = false;

	private class DownloadTask extends AsyncTask<Void, Void, Void> {
		public static final int BUFFER_SIZE = 1024;
		public static final int PREFETCH_SIZE = 2000;
		
		private String parseHTMLSource() throws IOException, NullPointerException {
			String link = null;
			FileInputStream fis = new FileInputStream(HTMLSOURCE_FILE);
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
            Log.d(TAG, debugMess);
			return link;
		}
		
		private String actualYTLink() throws IOException, NullPointerException {
			String ytLink = null;
			URL url = new URL(YOUTUBE_URL);
			Log.d(TAG, "Request youtube html source code for a video.");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.connect();
			BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
			FileOutputStream fos = new FileOutputStream(HTMLSOURCE_FILE);
			BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);
			byte[] data = new byte[1024];
			int j = 0;
			while ((j = bis.read(data, 0, 1024)) != -1) {
				bos.write(data, 0, j);
			}
			bis.close();
			bos.close();
			fos.close();
			Log.d(TAG, "End of request of html source code.");
			
			Log.d(TAG, "Parsing HTML Source code.");
			ytLink = parseHTMLSource();
			Log.d(TAG, "End of parsing html source code.");
			
			return ytLink;
		}
		
		@Override
		protected Void doInBackground(Void... unused) {
			URL url = null;

			try {
				url = new URL(actualYTLink());
				//url = new URL(YOUTUBE_URL);
				/* Establish HTTP request to http source code of Youtube */
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				
				/*
				 * Define InputStreams to read from the URLConnection.
				 */
				
				InputStream is = connection.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);
				FileOutputStream fos = new FileOutputStream(
						new File(OTHER_FILE));
				BufferedOutputStream bos = new BufferedOutputStream(fos);

				// prefetch a few bytes before starting playing
				byte data[] = new byte[1024];
				int j;
				Log.d(TAG, "Prefetching started.");
				int count = 0;
				while (((count < PREFETCH_SIZE) && (j = bis.read(data)) != -1)) {
					fos.write(data, 0, j);
					count++;
				}
				Log.d(TAG, "Prefetching finished.");
				
				// download chunks and start playing
				Log.d(TAG, "Download started.");
				while ((j = bis.read(data)) != -1) {
					fos.write(data, 0, j);
					publishProgress(null);
				}

				bis.close();
				bos.close();
				fos.close();
				
				Log.d(TAG, "Download finished.");
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
			Log.d(TAG, "Task started.");
		}

		@Override
		protected void onPostExecute(Void unused) {
			Log.d(TAG, "Task finished.");
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			playVideo();
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		setContentView(R.layout.activity_main);

		((EditText) findViewById(R.id.edtURL)).setText(YOUTUBE_URL);

		video = (VideoView) findViewById(R.id.vidView);
		// set up buttons to override default controls
		Button mPlay = (Button) findViewById(R.id.btnPlay);
		Button mPause = (Button) findViewById(R.id.btnPause);
		Button mReset = (Button) findViewById(R.id.btnReset);
		Button mStop = (Button) findViewById(R.id.btnStop);

		mPlay.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				new DownloadTask().execute(null, null, null);
			}
		});
		mPause.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (video != null) {
					isPlaying = false;
					video.pause();
				}
			}
		});
		mReset.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (video != null) {
					isPlaying = false;
					video.seekTo(0);
				}
			}
		});
		mStop.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (video != null) {
					current = null;
					isPlaying = false;
					video.stopPlayback();
				}
			}
		});
	}

	private void playVideo() {
		try {
			final String path = ((EditText) findViewById(R.id.edtURL))
					.getText().toString();
			if (path == null || path.length() == 0) {
				Toast.makeText(getBaseContext(), "File URL/path is empty",
						Toast.LENGTH_LONG).show();

			} else {// If the path has not changed, just start the media
				// player
				if (path.equals(current) && video != null) {
					if (!isPlaying) {
						Log.d(TAG, "Playback started.");
						isPlaying = true;
						video.start();
						video.requestFocus();
					}
				} else {
					current = path;
					video.setVideoPath(OTHER_FILE);
					isPlaying = true;
					video.start();
					video.requestFocus();
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "error: " + e.getMessage(), e);
			if (video != null) {
				video.stopPlayback();
			}
		}
	}
}
