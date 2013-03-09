package edu.toronto.cs.energyt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

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
	private static final String DEFAULT_URL = "http://techslides.com/demos/sample-videos/small.mp4";
	private static final String YOUTUBE_URL = "http://r4---sn-tt17rn7e.c.youtube.com/videoplayback?upn=I6GoCUDtvg8&source=youtube&sparams=algorithm%2Cburst%2Ccp%2Cfactor%2Cgcr%2Cid%2Cip%2Cipbits%2Citag%2Csource%2Cupn%2Cexpire&cp=U0hVR1dLUV9KTkNONV9PRVVEOktzTFptWTFrS1N3&ms=au&key=yt1&factor=1.25&ip=128.100.27.16&expire=1362703354&itag=34&mt=1362681980&mv=m&gcr=ca&signature=451CDA55101F74018904AE51D0E5B25A2002BB30.B9CA458DBA7A277375BC0834BAAB9D694CBF8823&newshard=yes&algorithm=throttle-factor&burst=40&ipbits=8&fexp=920704%2C912806%2C902000%2C929901%2C913605%2C925006%2C906938%2C931202%2C931401%2C908529%2C920201%2C930101%2C930603%2C900816%2C906834%2C926403&id=b9867330fb26e9ce&sver=3";
	private static final String OTHER_URL = "http://web.law.duke.edu/cspd/contest/finalists/viewentry.php?file=army";
	private static final String TAG = "energyt";
	private VideoView video;
	private String current;
	private boolean isPlaying = false;

	private class DownloadTask extends AsyncTask<Void, Void, Void> {
		public static final int BUFFER_SIZE = 1024;
		public static final int PREFETCH_SIZE = 2000;

		@Override
		protected Void doInBackground(Void... unused) {
			URL url = null;
			try {
				url = new URL(OTHER_URL);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				URLConnection ucon = url.openConnection();

				/*
				 * Define InputStreams to read from the URLConnection.
				 */
				InputStream is = ucon.getInputStream();
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

				Log.d(TAG, "Download finished.");
			} catch (IOException e) {
				// TODO Auto-generated catch block
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

		((EditText) findViewById(R.id.edtURL)).setText(DEFAULT_URL);

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
