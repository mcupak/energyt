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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DownloadTask extends AsyncTask<Void, Void, Void> {
	private static final int BUFFER_SIZE = 1024;
	private static final int PREFETCH_SIZE = 1000;
	// private static final int TIMEOUT = 5000;
	private MainActivity mainApp;
	// Keeps track of the first execution of doInBackground function
	// This means that is updated in this function and declares whether
	// the first connection of youtube streaming server has established...
	// [if it has been established its value should be: false]
	private boolean reestablishConn;
	private boolean acquireHTMLSource; // Does this thread acquires HTML
										// Source...
	private URL actualURL;

	// Connectivity variables
	private HttpURLConnection connection;
	private InputStream is;
	private BufferedInputStream bis;
	private File file;
	private FileOutputStream fos;
	private BufferedOutputStream bos;

	// Energy algorithm related variables
	private int networkType;

	public DownloadTask(MainActivity mainApp) {
		this.reestablishConn = false;
		this.acquireHTMLSource = false;
		this.mainApp = mainApp;
		this.networkType = -1;
	}

	private String parseHTMLSource() throws IOException, NullPointerException {
		String link = null;
		FileInputStream fis = new FileInputStream(MainActivity.HTMLSOURCE_FILE);
		DataInputStream dis = new DataInputStream(fis);
		BufferedReader br = new BufferedReader(new InputStreamReader(dis));
		String strLine;
		final String keyword1 = "fmt_stream_map"; // Looking first for this
													// specific format
		final String keyword2 = "\\\"url\\\": \\\""; // Having found the above
														// keyword we look for
														// this one
		int index = -1;
		while ((strLine = br.readLine()) != null) {
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
					link = strLine
							.concat(secondStrLine.substring(0, index - 1));
				}
				break; // We have found the link for sure
			}
		}
		if (link == null) { // Release resources and throw exception...
			br.close();
			dis.close();
			fis.close();
			throw new NullPointerException(
					"link is NULL. Cannot retrieve youtube link \n"
							+ "from the corresponding html source file!");
		}
		// Transform link to readable format...
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
		URL url = new URL(mainApp.getLinkPlaying());
		String debugMess = "User's Link: " + url.toString();
		Log.d(MainActivity.TAG, debugMess);
		Log.d(MainActivity.TAG, "Request youtube html source code for a video.");
		if (this.establishConnection(url, MainActivity.HTMLSOURCE_FILE)) {
			// connection = (HttpURLConnection) url.openConnection();
			// connection.connect();
			// BufferedInputStream bis = new
			// BufferedInputStream(connection.getInputStream());
			// FileOutputStream fos = new
			// FileOutputStream(MainActivity.HTMLSOURCE_FILE);
			// BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);
			byte[] data = new byte[BUFFER_SIZE];
			int j = 0;
			while ((j = bis.read(data, 0, 1024)) != -1) {
				bos.write(data, 0, j);
			}
			this.closeConnection();
			// bis.close();
			// bos.close();
			// fos.close();
			// connection.disconnect();
			Log.d(MainActivity.TAG, "End of request of html source code.");
			Log.d(MainActivity.TAG, "Parsing HTML Source code.");
			ytLink = parseHTMLSource();
			Log.d(MainActivity.TAG, "End of parsing html source code.");
			this.acquireHTMLSource = true;
		} else {
			Log.d(MainActivity.TAG,
					"Not acquiring html source! Problem with the connection...");
		}
		return ytLink;
	}

	private boolean checkConnected() {
		ConnectivityManager cm = (ConnectivityManager) mainApp
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected())
			return true;
		else
			return false;
	}

	private boolean establishConnection(URL url, String outputFile) {
		try {
			if (this.checkConnected()) {
				connection = (HttpURLConnection) url.openConnection();
				this.networkType = mainApp.cellSignal.getNetworkType();
				file = new File(outputFile);
				if (this.reestablishConn) {
					connection.setRequestProperty("Range",
							"bytes=" + (file.length()) + "-");
				} else if (file.exists()) {
					// First time establishing connection with yt service
					file.delete();
					file.createNewFile();
				}
				/* Define InputStreams to read from the URLConnection. */
				connection.connect();
				is = connection.getInputStream();
				bis = new BufferedInputStream(is);

				// Either we have created a new file or we have an existing
				// append to the file output buffer...
				fos = new FileOutputStream(file, true);
				bos = new BufferedOutputStream(fos);
				return true;
			} else
				return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private void closeConnection() {
		try {
			if (bis != null)
				bis.close();
			if (bos != null)
				bos.close();
			if (fos != null)
				fos.close();
			if (is != null)
				is.close();
			if (connection != null)
				connection.disconnect();
			bis = null;
			bos = null;
			fos = null;
			is = null;
			connection = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean networkWeakened(int currNetworkType) {
		if (currNetworkType == TelephonyManager.NETWORK_TYPE_UNKNOWN
				|| this.networkType == TelephonyManager.NETWORK_TYPE_UNKNOWN)
			return false;
		switch (this.networkType) {
		case TelephonyManager.NETWORK_TYPE_GPRS:
			break;
		case TelephonyManager.NETWORK_TYPE_EDGE:
			break;
		case TelephonyManager.NETWORK_TYPE_IDEN:
			break;
		case TelephonyManager.NETWORK_TYPE_1xRTT:
		case TelephonyManager.NETWORK_TYPE_CDMA:
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
		case TelephonyManager.NETWORK_TYPE_EVDO_B:
		case TelephonyManager.NETWORK_TYPE_EHRPD:
		case TelephonyManager.NETWORK_TYPE_UMTS:
			break;
		case TelephonyManager.NETWORK_TYPE_HSPA:
		case TelephonyManager.NETWORK_TYPE_HSUPA:
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			break;
		case TelephonyManager.NETWORK_TYPE_HSPAP:
			break;
		case TelephonyManager.NETWORK_TYPE_LTE:
			break;
		}
		return true;
	}

	@Override
	protected void onPreExecute() {
		Log.d(MainActivity.TAG, "AsyncTask: Task started.");
		this.connection = null;
		this.is = null;
		this.bis = null;
		this.file = null;
		this.fos = null;
		this.bos = null;

		mainApp.setYtDownFinished(false);
	}

	private boolean downloadProcess() throws IOException {
		if (this.acquireHTMLSource
				&& this.establishConnection(this.actualURL,
						MainActivity.OTHER_FILE)) {
			// prefetch a few bytes before starting playing
			// Log.d(MainActivity.TAG, "Prefetching started.");
			// No need to prefetch - We have streams now!
			if (!this.reestablishConn)
				publishProgress();
			byte data[] = new byte[1024];
			int j;
			
			// prefetch
			int count = 0;
			while ((j = bis.read(data)) != -1 && count < PREFETCH_SIZE) {
				fos.write(data, 0, j);
				mainApp.setYTDownloadSize(mainApp.getYTDownloadSize() + j);
				count++;
			}
			Log.d(MainActivity.TAG, "prefetch done");
			
			// test
			closeConnection();
			Log.d(MainActivity.TAG, "Connection closed.");
			this.reestablishConn = true;
			establishConnection(this.actualURL, MainActivity.OTHER_FILE);
			Log.d(MainActivity.TAG, "Connection started.");
			
			// While loop streams until there is nothing left in the socket
			while ((j = bis.read(data)) != -1) {
				if (isCancelled()) { // the thread has been cancelled!
					Log.d(MainActivity.TAG, "Download cancelled.");
					break;
				}
				fos.write(data, 0, j);
				mainApp.setYTDownloadSize(mainApp.getYTDownloadSize() + j);
				
				
				if (!checkConnected()) {
					// After having read the whole stream we need
					// to restore again the connection
					this.reestablishConn = true;
				}
			}
			return true;
		} else {
			Log.d(MainActivity.TAG,
					"Not acquiring mp4 file! Problem with the connection...");
			return false;
		}
	}

	@Override
	protected Void doInBackground(Void... unused) {
		Log.d(MainActivity.TAG, "AsyncTask: in doInBackground for YT.");
		try {
			this.actualURL = new URL(actualYTLink());
			Log.d(MainActivity.TAG, "AsyncTask: doInBackground: In try.");
			/* Establish HTTP request to http source code of Youtube */

			// Attempt to connect for 5 times in a row
			// if this won't work then quit...
			int trials = 0;
			while (!this.reestablishConn && trials < 10) {
				if (this.downloadProcess()) {
					// We have established connection which means that
					// the counter should be zero...
					trials = 0;
				}
				if (!this.reestablishConn && checkConnected()) {
					// We are sure that the video has been downloaded
					// properly...
					break;
				} else if (this.reestablishConn || !checkConnected()) {
					// We might be disconnected without having acquired all the
					// video
					// so we need to check connectivity again in case we haven't
					// activated
					// our energy efficient algorithm...

					// We caught reestablish connection event
					// time to set variable to its initial/default value
					this.reestablishConn = false;
					Log.d(MainActivity.TAG,
							"Connection lost during download process");
					// clear previous opened streams...
					this.closeConnection();
					trials++;

					// Make this thread wait for a while till connection appears
					// again...
					try {
						this.wait(5000);
					} catch (InterruptedException e) {
						Log.d(MainActivity.TAG,
								"YTDownloader: Interrupt occured!");
						e.printStackTrace();
					}
				}

			}

			// count+=j;
			// //If prefetch is done, startup mediaplayer via
			// publishProgress(null)
			// if(notActivate && count > 0 && count > PREFETCH_SIZE) {
			// Log.d(MainActivity.TAG, "Playback called.");
			// publishProgress();
			// count = -1;
			// notActivate = false;
			// }
			// else {
			// /**
			// * ENERGY EFFICIENT ALGORITHM - we start applying it when prefetch
			// has ended.
			// */
			// //TODO
			// int currNetworkType = mainApp.cellSignal.getNetworkType();
			// if (mainApp.canDisconnect() && !mainApp.wifi.isWifiEnabled()
			// && this.networkWeakened(currNetworkType)) {
			// Log.d(MainActivity.TAG, "Disconnect case");
			// this.networkType = mainApp.cellSignal.getNetworkType();
			// }
			// /* END OF ENERGY EFFICIENT ALGORITHM */
			// }

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
	protected void onPostExecute(Void unused) {
		Log.d(MainActivity.TAG, "Task finished.");
		this.closeConnection();
		mainApp.setYtDownFinished(true);
	}

	@Override
	protected void onProgressUpdate(Void... values) {
		mainApp.playVideo();
		// Connect to Server and play
	}

}
