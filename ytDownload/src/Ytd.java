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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


public class Ytd {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		BufferedInputStream in = null;
        FileOutputStream fos = null;
        BufferedOutputStream bout=null;

        URL url = new URL("http://www.youtube.com/watch?v=uYZzMPsm6c4");
        String path = "/home/michael/Downloads/page.html";
        
        
        try {
        	int downloaded=0;
        	int size = 0;
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            /*if(ISSUE_DOWNLOAD_STATUS.intValue()==ECMConstant.ECM_DOWNLOADING){
                File file=new File(DESTINATION_PATH);
                if(file.exists()){
                    downloaded = (int) file.length();
                }
            }
            connection.setRequestProperty("Range", "bytes=" + downloaded + "-");*/
            connection.connect();
            size=connection.getContentLength();
            //Dialog.setMax(size);
             in = new BufferedInputStream(connection.getInputStream());
             fos = (downloaded==0)? new FileOutputStream(path): new FileOutputStream(path,true);
             bout = new BufferedOutputStream(fos, 1024);
            byte[] data = new byte[1024];
            int x = 0;
            while ((x = in.read(data, 0, 1024)) >= 0) {
                bout.write(data, 0, x);
                 downloaded += x;
                // System.out.println(downloaded);
                 //onProgressUpdate((int)(downloaded*100/size));
            }

    		FileInputStream fstream = new FileInputStream(path);
    		// Get the object of DataInputStream
    		DataInputStream in2 = new DataInputStream(fstream);
    		BufferedReader br = new BufferedReader(new InputStreamReader(in2));
    		String strLine;
    		String[] strLineArr = null;
    		while((strLine=br.readLine())!=null)
    		{	
    			strLineArr = strLine.split("yt.preload.start");
    			//System.out.println(strLineArr.length);
    			if(strLineArr.length>1)
    			{
    				System.out.println(strLineArr[3]);
    				break;
    			}
    			
    		}
            String link;
    		String parsingLink = strLineArr[3];
            //strLineArr[3] contains the unparsed link
            link = parsingLink.substring(2);
            link = link.substring(0, link.indexOf('"'));
           
            link = link.replaceAll("\\\\u0026", "&");
            
            link = link.replaceAll("\\\\", "");
            		
            link = link.replaceAll("generate_204", "videoplayback");
            System.out.println(link);
        	int downloaded2=0;
        	int size2 = 0;
            url = new URL(link);
            BufferedInputStream in3 = null;
            FileOutputStream fos3 = null;
            BufferedOutputStream bout3 = null;
            HttpURLConnection connection2 = (HttpURLConnection)url.openConnection();
            /*if(ISSUE_DOWNLOAD_STATUS.intValue()==ECMConstant.ECM_DOWNLOADING){
                File file=new File(DESTINATION_PATH);
                if(file.exists()){
                    downloaded = (int) file.length();
                }
            }
            connection.setRequestProperty("Range", "bytes=" + downloaded + "-");*/
            connection2.connect();
            size2=connection2.getContentLength();
            path = "/home/michael/Downloads/videoXXX.mp4";
            //Dialog.setMax(size);
            in3 = new BufferedInputStream(connection2.getInputStream());
            fos3 = (downloaded2==0)? new FileOutputStream(path): new FileOutputStream(path,true);
            bout3 = new BufferedOutputStream(fos3, 1024);
            byte[] data2 = new byte[1024];
            int y = 0;
            while ((y = in3.read(data2, 0, 1024)) >= 0) {
                bout3.write(data2, 0, y);
                downloaded2 += y;
                // System.out.println(downloaded);
                 //onProgressUpdate((int)(downloaded*100/size));
            }
            
            
            
            

            //success=true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                bout.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

	}

}
