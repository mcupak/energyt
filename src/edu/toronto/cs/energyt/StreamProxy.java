package edu.toronto.cs.energyt;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;

public class StreamProxy extends AsyncTask<Void,Void,Void>  {

    private static final int SERVER_PORT=8893;
    private MainActivity mainApp;
    private boolean isRunning;
    private ServerSocket socket;
    private int port;
    
    public StreamProxy(MainActivity mainApp) {
    	this.mainApp = mainApp;
    	this.isRunning = false;
    	this.port = -1;
    	this.socket = null;
    }
    
    @Override
	protected void onPreExecute() {
		Log.d(MainActivity.TAG, "StreamProxy: Task started.");
		try {
			socket = new ServerSocket(SERVER_PORT, 0, InetAddress.getByAddress(new byte[] {127,0,0,1}));
			socket.setSoTimeout(5000);
			port = socket.getLocalPort();
			isRunning = false;
		} catch (UnknownHostException e) { // impossible
		} catch (IOException e) {
			Log.e(MainActivity.TAG, "IOException initializing server", e);
		}
	}

	@Override
	protected void onPostExecute(Void unused) {
		Log.d(MainActivity.TAG, "StreamProxy: Task finished.");
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
				Log.d(MainActivity.TAG, "StreamProxy: Listening.");
				Socket client = socket.accept();
				if (client == null) {
					continue;
				}
				Log.d(MainActivity.TAG, "StreamProxy: client connected");

				//No need to check for any protocol or anything like this...
				mainApp.clientSocketThread = new StreamToMediaPlayerTask(client, 0);
				mainApp.clientSocketThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
			} catch (SocketTimeoutException e) {
				// Do nothing
			} catch (IOException e) {
				Log.e(MainActivity.TAG, "StreamProxy: Error connecting to client", e);
			}
		}
		Log.d(MainActivity.TAG, "StreamProxy: Proxy interrupted. Shutting down.");

		return null;
	}




    public class StreamToMediaPlayerTask extends AsyncTask<Void, Void, Void> {

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
//            // Create HTTP header
//            String headers = "HTTP/1.0 200 OK\r\n";
//            headers += "Content-Type: " + "video/mp4" + "\r\n";
//            //headers += "Content-Length: " + fileSize  + "\r\n";
//            headers += "Connection: close\r\n";
//            headers += "\r\n";

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
                    File file = new File(MainActivity.OTHER_FILE);
                    //fc++;
                    int cbSentThisBatch = 0;
                    if (file.exists()) {
                        FileInputStream input = new FileInputStream(file);
                        input.skip(cbSkip);
                        int cbToSendThisBatch = input.available();
                        
                        
                        while (cbToSendThisBatch > 0) {
                        	if (isCancelled()) {
                        		Log.d(MainActivity.TAG, "Closing socket connection - tread cancelled.");
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
                        	Log.d(MainActivity.TAG, "Closing socket connection - tread cancelled.");
                        	break;
                        }
                    }
                    if (isCancelled()) {
                    	Log.d(MainActivity.TAG, "Closing socket connection - tread cancelled.");
                    	break;
                    }
                    // If we did nothing this batch, block for a second
                    if (cbSentThisBatch == 0) {
                        Log.d(MainActivity.TAG, "Blocking until more data appears");
                        Thread.sleep(1000);
                    }
                }
            }
            catch (SocketException socketException) {
                Log.e(MainActivity.TAG, "SocketException() thrown, proxy client has probably closed. This can exit harmlessly");
            }
            catch (Exception e) {
                Log.e(MainActivity.TAG, "Exception thrown from streaming task:");
                Log.e(MainActivity.TAG, e.getClass().getName() + " : " + e.getLocalizedMessage());
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
                Log.e(MainActivity.TAG, "IOException while cleaning up streaming task:");                
                Log.e(MainActivity.TAG, e.getClass().getName() + " : " + e.getLocalizedMessage());
                e.printStackTrace();                
            }

            return null;
        }

    }
}
