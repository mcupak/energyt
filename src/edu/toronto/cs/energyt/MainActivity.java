package edu.toronto.cs.energyt;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.VideoView;

/**
 * Main player activity.
 * 
 * @author mcupak
 * 
 */
public class MainActivity extends Activity {
	
	public static final String OTHER_FILE = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/Download/other.mp4";
	public static final String HTMLSOURCE_FILE = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/Download/html_source.html";
	public static final String YOUTUBE_URL = "http://www.youtube.com/watch?v=Vjm3KPGab5o";
	public static final String TAG = "energyt";
	private VideoView video;
	private boolean isPlaying;
	public StreamProxy serverSocketThread;
	public StreamProxy.StreamToMediaPlayerTask clientSocketThread;
	public DownloadTask ytDownloaderThread;
	private PlayListener playListener;
	private StopListener stopListener;
	private ResetListener resetListener;
	
	/* ACTIVITY LISTENERS */
	
	private class PlayListener implements OnClickListener {

		private MainActivity mainApp;
		
		public PlayListener(MainActivity mainApp) {
			this.mainApp = mainApp;
		}
		
		@Override
		public void onClick(View v) {
			Log.d(MainActivity.TAG,"In Play listener");
			if (!mainApp.isPlaying && mainApp.clientSocketThread == null) {
				Log.d(MainActivity.TAG,"Calling Download Task");
				mainApp.ytDownloaderThread = new DownloadTask(mainApp);
				mainApp.ytDownloaderThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
			}
			else {
				mainApp.video.start();
			}
		}
		
	}
	
	private class StopListener implements OnClickListener {

		private MainActivity mainApp;
		
		public StopListener(MainActivity mainApp) {
			this.mainApp = mainApp;
		}
		
		@Override
		public void onClick(View v) {
			if (mainApp.clientSocketThread != null) {
				mainApp.clientSocketThread.cancel(true);
				mainApp.clientSocketThread = null;
			}
			if (mainApp.ytDownloaderThread != null) {
				mainApp.ytDownloaderThread.cancel(true);
				mainApp.ytDownloaderThread = null;
			}
			if (mainApp.video != null) {
				mainApp.isPlaying = false;
				mainApp.video.stopPlayback();
			}
		}
		
	}
	
	private class ResetListener implements OnClickListener {
		
		private MainActivity mainApp;
		
		public ResetListener(MainActivity mainApp) {
			this.mainApp = mainApp;
		}
		
		public void onClick(View v) {
			if (mainApp.clientSocketThread != null) {
				mainApp.clientSocketThread.cancel(true);
				mainApp.clientSocketThread = null;
			}
			if (mainApp.ytDownloaderThread != null) {
				mainApp.ytDownloaderThread.cancel(true);
				mainApp.ytDownloaderThread = null;
			}
			if (mainApp.video != null) {
				mainApp.isPlaying = false;
				mainApp.video.seekTo(0);
			}
		}
	}
	
	

	
	/* ACTIVITY CORE FUNCTIONS */
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.isPlaying = false;
		this.serverSocketThread = null;
		this.clientSocketThread = null;
		this.ytDownloaderThread = null;
		this.playListener = new PlayListener(this);
		this.stopListener = new StopListener(this);
		this.resetListener = new ResetListener(this);
		
		
		Log.d(TAG, "Instantiating server");
		this.serverSocketThread = new StreamProxy(this);
		this.serverSocketThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
		Log.d(TAG, "Server triggered");
		
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		setContentView(R.layout.activity_main);
		
		((EditText) findViewById(R.id.edtURL)).setText(YOUTUBE_URL);

		video = (VideoView) findViewById(R.id.vidView);

		// set up buttons to override default controls
		Button mPlay = (Button) findViewById(R.id.btnPlay);
		Button mPause = (Button) findViewById(R.id.btnPause);
		Button mReset = (Button) findViewById(R.id.btnReset);
		Button mStop = (Button) findViewById(R.id.btnStop);

		mPlay.setOnClickListener(this.playListener);
		
		mPause.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (video != null) {
					Log.d(TAG,"In Pause listener");
					isPlaying = false;
					video.pause();
				}
			}
		});
		
		mReset.setOnClickListener(this.resetListener);
		
		mStop.setOnClickListener(this.stopListener);
	}
	
	public void playVideo() {
		try {
			Log.d(TAG, "Playback started.");
			isPlaying = true;
			video.setVideoURI(Uri.parse("http://127.0.0.1:8893/xyz"));
			video.start();
			video.requestFocus();
		} catch (Exception e) {
			Log.e(TAG, "error: " + e.getMessage(), e);
			if (video != null) {
				video.stopPlayback();
			}
		}
	}
}
