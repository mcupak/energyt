package edu.toronto.cs.energyt;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.webkit.WebView;
import android.widget.FrameLayout;

public class FlashActivity extends Activity {

	private static final String TAG = "FLVplayer";
	private static WebView webView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flash);

		webView = (WebView) findViewById(R.id.webview);

		// WebView webview = new WebView(this);
		// setContentView(webview);

		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setAllowFileAccess(true);
		webView.getSettings().setPluginsEnabled(true);

		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Log.d(TAG, "No SDCARD");
		} else {
			File flvHtml = createVideoHtml();

			// load the content into the webview
			webView.loadUrl(Uri.fromFile(flvHtml).toString());
		}
	}

	private File createVideoHtml() {
		String htmlFile = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/Download/index.html";
		String player = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/Download/FLVPlayer.swf";
		String swf = "<script type=\"text/javascript\">var so = new SWFObject(\"http://www.flayr.net/serve/swf/flayr.swf?movie=http://www.flayr.net/media/games.flv&preview=&name=&controls=hide\", \"flayr_player_id\", \"620\", \"375\", \"8\");so.write(\"div_to_replace\");</script>";
		String video = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/Download/video.flv";
		String html = "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"utf-8\"></head><body style=\"margin:0; pading:0; background-color: black;\"><embed style=\"width:100%; height:100%\" src=\""
				+ player
				+ "?fullscreen=true&video="
				+ video
				+ "\" autoplay=\"true\" quality=\"high\" bgcolor=\"#000000\" name=\"VideoPlayer\" align=\"middle\" allowScriptAccess=\"*\" allowFullScreen=\"true\" type=\"application/x-shockwave-flash\" pluginspage=\"http://www.macromedia.com/go/getflashplayer\"></embed></body></html>";

		// generate html
		BufferedOutputStream out = null;
		File f = null;
		try {
			f = new File(htmlFile);
			out = new BufferedOutputStream(new FileOutputStream(f));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			out.write(html.getBytes());
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// // generate swf
		// out = null;
		// f = null;
		// try {
		// f = new File(player);
		// out = new BufferedOutputStream(new FileOutputStream(f));
		// } catch (FileNotFoundException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// try {
		// out.write(swf.getBytes());
		// out.close();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		return f;
	}

	@Override
	protected void onPause() {
		super.onPause();

		callHiddenWebViewMethod("onPause");

		webView.pauseTimers();
		if (isFinishing()) {
			webView.loadUrl("about:blank");
			setContentView(new FrameLayout(this));
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		callHiddenWebViewMethod("onResume");

		webView.resumeTimers();
	}

	private void callHiddenWebViewMethod(String name) {
		// credits:
		// http://stackoverflow.com/questions/3431351/how-do-i-pause-flash-
		// content-in-an-android-webview-when-my-activity-isnt-visible
		if (webView != null) {
			try {
				Method method = WebView.class.getMethod(name);
				method.invoke(webView);
			} catch (NoSuchMethodException e) {
				// Lo.g("No such method: " + name + e);
			} catch (IllegalAccessException e) {
				// Lo.g("Illegal Access: " + name + e);
			} catch (InvocationTargetException e) {
				// Lo.g("Invocation Target Exception: " + name + e);
			}
		}
	}

}
