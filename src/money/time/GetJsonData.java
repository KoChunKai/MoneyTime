package money.time;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

public class GetJsonData {
	
	private String result;
	
	/*public GetJsonData(String mUrl) throws IOException{
		StringBuilder sb = new StringBuilder();
		HttpClient httpClient = MySSLSocketFactory.createMyHttpClient();
        HttpGet httpGet = new HttpGet(mUrl);
        HttpResponse httpResponse = httpClient.execute(httpGet);
        HttpEntity httpEntity = httpResponse.getEntity();
        if (httpEntity != null) {
            InputStream instream = httpEntity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    instream));
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            instream.close();
            this.result = sb.toString();
        }
	}*/
	
	public GetJsonData(String mUrl) throws IOException{
	    InputStream iStream = null;
	    HttpURLConnection urlConnection = null;
	    try{
	        URL url = new URL(mUrl);
	        urlConnection = (HttpURLConnection) url.openConnection();
	        // Connecting to url
	        urlConnection.connect();
	        // Reading data from url
	        iStream = urlConnection.getInputStream();
	        BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
	        StringBuffer sb  = new StringBuffer();
	        String line = "";
	        while( ( line = br.readLine())  != null){
	            sb.append(line);
	        }
	        this.result = sb.toString();
	        br.close();
	    }catch(Exception e){
	    }finally{
	        iStream.close();
	        urlConnection.disconnect();
	    }
	}
	public String getResult() { return result; }
}
