package edu.toronto.cs.energyt;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;

public class MainActivity extends Activity {
	private static final String DEFAULT_FILE = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/Download/small.mp4";
	private static final String DEFAULT_URL = "http://techslides.com/demos/sample-videos/small.mp4";
	private static final String TAG = "energyt";
	private VideoView video;
	private String current;

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
				try {
					final String path = ((EditText) findViewById(R.id.edtURL))
							.getText().toString();
					Log.v(TAG, "path: " + path);
					if (path == null || path.length() == 0) {
						Toast.makeText(getBaseContext(),
								"File URL/path is empty", Toast.LENGTH_LONG)
								.show();

					} else {
						// If the path has not changed, just start the media
						// player
						if (path.equals(current) && video != null) {
							video.start();
							video.requestFocus();
							return;
						}
						current = path;
						video.setVideoPath(DEFAULT_FILE);
						video.start();
						video.requestFocus();

					}
				} catch (Exception e) {
					Log.e(TAG, "error: " + e.getMessage(), e);
					if (video != null) {
						video.stopPlayback();
					}
				}
			}
		});
		mPause.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (video != null) {
					video.pause();
				}
			}
		});
		mReset.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (video != null) {
					video.seekTo(0);
				}
			}
		});
		mStop.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (video != null) {
					current = null;
					video.stopPlayback();
				}
			}
		});
	}
}
