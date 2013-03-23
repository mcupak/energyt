package edu.toronto.cs.energyt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
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
	private boolean isPlaying;
	public StreamProxy.StreamToMediaPlayerTask clientSocketThread;
	public DownloadTask ytDownloaderThread;
	
	

	private class DownloadTask extends AsyncTask<Void, Void, Void> {
		private static final int BUFFER_SIZE = 1024;
		private static final int PREFETCH_SIZE = 100000;
		private boolean isRunning;
		
		public DownloadTask() { isRunning = false; };
		
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
			URL url = new URL(((EditText) findViewById(R.id.edtURL)).getText().toString());
			String debugMess = "User's Link: " + url.toString();
			Log.d(TAG, debugMess);
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
			Log.d(TAG, "AsyncTask: in doInBackground for YT.");
			this.isRunning = true;
			URL url = null;
			InputStream is = null;
			BufferedInputStream bis = null;
			FileOutputStream fos = null;
			BufferedOutputStream bos = null;
			
			try {
				
				

				
				
				
				
				Log.d(TAG, "AsyncTask: doInBackground: In try.");
				url = new URL(actualYTLink());
				/* Establish HTTP request to http source code of Youtube */
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				/* Define InputStreams to read from the URLConnection. */
				is = connection.getInputStream();
				bis = new BufferedInputStream(is);
				fos = new FileOutputStream(new File(OTHER_FILE));
				bos = new BufferedOutputStream(fos);
				//socket = new Soc

				// prefetch a few bytes before starting playing
				byte data[] = new byte[1024];
				int j;
				int count = 0;
				boolean notActivate = true;
				Log.d(TAG, "Prefetching started.");
				//Below while loop downloads the entire video file
				while ((j = bis.read(data)) != -1) {
					
					//TODO
					//if(connection is low)
					//	connection.
					
					
					if (isCancelled()) { //the thread has been cancelled!
						Log.d(TAG, "Download cancelled.");
						break;
					}
					fos.write(data, 0, j);
					count+=j;
					
					//If prefetch is done, startup mediaplayer via publishProgress(null)
					if(notActivate && count > 0 && count > PREFETCH_SIZE)
					{
						Log.d(TAG, "Playback called.");
						publishProgress(null);
						count = -1;
						notActivate = false;
					}
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
			Log.d(TAG, "AsyncTask: Task started.");
		}

		@Override
		protected void onPostExecute(Void unused) {
			Log.d(TAG, "Task finished.");
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			playVideo();
			//Connect to Server and play
		}

	}

	private class StreamProxy extends AsyncTask<Void,Void,Void>  {

	    private static final int SERVER_PORT=8893;
	    //private Thread thread;
	    private boolean isRunning;
	    private ServerSocket socket;
	    private int port;
	    
	    @Override
		protected void onPreExecute() {
			Log.d(TAG, "StreamProxy: Task started.");
			try {
				socket = new ServerSocket(SERVER_PORT, 0, InetAddress.getByAddress(new byte[] {127,0,0,1}));
				socket.setSoTimeout(5000);
				port = socket.getLocalPort();
				isRunning = false;
			} catch (UnknownHostException e) { // impossible
			} catch (IOException e) {
				Log.e(TAG, "IOException initializing server", e);
			}
		}

		@Override
		protected void onPostExecute(Void unused) {
			Log.d(TAG, "StreamProxy: Task finished.");
			isRunning = false;
		}
	    
		@Override
		protected Void doInBackground(Void... unused)
		{
			Looper.prepare();
			isRunning = true;
			while (isRunning) {
				if (isCancelled()) {
					this.isRunning = false;
					break;
				}
				try {
					Log.d(TAG, "StreamProxy: Listening.");
					Socket client = socket.accept();
					if (client == null) {
						continue;
					}
					Log.d(TAG, "StreamProxy: client connected");

					//No need to check for any protocol or anything like this...
					clientSocketThread = new StreamToMediaPlayerTask(client, 0);
					clientSocketThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
//					if (task.processRequest()) {
//						task.execute();
//						task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (String[])null);
//					}
				} catch (SocketTimeoutException e) {
					// Do nothing
				} catch (IOException e) {
					Log.e(TAG, "StreamProxy: Error connecting to client", e);
				}
			}
			Log.d(TAG, "StreamProxy: Proxy interrupted. Shutting down.");

			return null;
		}




	    private class StreamToMediaPlayerTask extends AsyncTask<Void, Void, Void> {

	        Socket client;
	        int cbSkip;
	        private Boolean isRunning;

	        public StreamToMediaPlayerTask(Socket client, int cbSkip) {
	            this.client = client;
	            this.cbSkip = cbSkip;
	            this.isRunning = false;
	        }

	        @Override
	        protected Void doInBackground(Void... unused) {
	        	
	        	isRunning = true;
	        	long fileSize = Long.MAX_VALUE;
//
//	            // Create HTTP header
//	            String headers = "HTTP/1.0 200 OK\r\n";
//	            headers += "Content-Type: " + "video/mp4" + "\r\n";
//	            //headers += "Content-Length: " + fileSize  + "\r\n";
//	            headers += "Connection: close\r\n";
//	            headers += "\r\n";

	            // Begin with HTTP header
	            //int fc = 0;
	            //long cbToSend = fileSize - cbSkip;
	        	long cbToSend = fileSize;
	            OutputStream output = null;
	            byte[] buff = new byte[64 * 1024];
	            try {
	                output = new BufferedOutputStream(client.getOutputStream(), 32*1024);                           

	                // Loop as long as there's stuff to send
	                while (isRunning && cbToSend>0 && !client.isClosed()) {

	                    // See if there's more to send
	                    File file = new File(OTHER_FILE);
	                    //fc++;
	                    int cbSentThisBatch = 0;
	                    if (file.exists()) {
	                        FileInputStream input = new FileInputStream(file);
	                        input.skip(cbSkip);
	                        int cbToSendThisBatch = input.available();
	                        
	                        
	                        while (cbToSendThisBatch > 0) {
	                        	if (isCancelled()) {
	                        		Log.d(TAG, "Closing socket connection - tread cancelled.");
	                        		break;
	                        	}
	                        	//how mcuh to send for this exact next packet to mediaplayer (client)
	                            int cbToRead = Math.min(cbToSendThisBatch, buff.length);
	                            //Now read the above amount into the buffer
	                            int cbRead = input.read(buff, 0, cbToRead);
	                            if (cbRead == -1) {
	                                break;
	                            }
	                            cbToSendThisBatch -= cbRead;
	                            cbToSend -= cbRead;
	                            output.write(buff, 0, cbRead);
	                            output.flush();
	                            cbSkip += cbRead;
	                            cbSentThisBatch += cbRead;
	                        }
	                        
	                        
	                        input.close();
	                        if (isCancelled()) {
	                        	Log.d(TAG, "Closing socket connection - tread cancelled.");
	                        	break;
	                        }
	                    }
	                    if (isCancelled()) {
	                    	Log.d(TAG, "Closing socket connection - tread cancelled.");
	                    	break;
	                    }
	                    // If we did nothing this batch, block for a second
	                    if (cbSentThisBatch == 0) {
	                        Log.d(TAG, "Blocking until more data appears");
	                        Thread.sleep(1000);
	                    }
	                }
	            }
	            catch (SocketException socketException) {
	                Log.e(TAG, "SocketException() thrown, proxy client has probably closed. This can exit harmlessly");
	            }
	            catch (Exception e) {
	                Log.e(TAG, "Exception thrown from streaming task:");
	                Log.e(TAG, e.getClass().getName() + " : " + e.getLocalizedMessage());
	                e.printStackTrace();                
	            }

	            // Cleanup
	            try {
	                if (output != null) {
	                    output.close();
	                }
	                client.close();
	            }
	            catch (IOException e) {
	                Log.e(TAG, "IOException while cleaning up streaming task:");                
	                Log.e(TAG, e.getClass().getName() + " : " + e.getLocalizedMessage());
	                e.printStackTrace();                
	            }

	            return null;
	        }

	    }
	}
	
	
	
	
	
	
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		this.current = null;
		this.isPlaying = false;
		this.clientSocketThread = null;
		this.ytDownloaderThread = null;
		
		boolean isEnabled = Settings.System.getInt(
				this.getApplicationContext().getContentResolver(), 
			      Settings.System.AIRPLANE_MODE_ON, 0) == 1;
		
		
		Log.d(TAG, "Instantiating server");
		//new StreamProxy().execute(null, null, null);
		new StreamProxy().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
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

		mPlay.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				Log.d(TAG,"In Play listener");
				if (!isPlaying && clientSocketThread == null) {
					Log.d(TAG,"Calling Download Task");
					ytDownloaderThread = new DownloadTask();
					ytDownloaderThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
				}
				else {
					video.start();
				}
			}
		});
		
		mPause.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (video != null) {
					Log.d(TAG,"In Pause listener");
					isPlaying = false;
					video.pause();
				}
			}
		});
		
		mReset.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (clientSocketThread != null) {
					clientSocketThread.cancel(true);
					clientSocketThread = null;
				}
				if (ytDownloaderThread != null) {
					ytDownloaderThread.cancel(true);
					ytDownloaderThread = null;
				}
				if (video != null) {
					isPlaying = false;
					video.seekTo(0);
				}
			}
		});
		
		mStop.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (clientSocketThread != null) {
					clientSocketThread.cancel(true);
					clientSocketThread = null;
				}
				if (ytDownloaderThread != null) {
					ytDownloaderThread.cancel(true);
					ytDownloaderThread = null;
				}
				if (video != null) {
					isPlaying = false;
					video.stopPlayback();
				}
			}
		});
	}
	
	private void playVideo() {
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
