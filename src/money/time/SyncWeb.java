package money.time;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;

public class SyncWeb extends AsyncTask<String, Integer, String> {

	private Context context;
	private ProgressDialog dialog = null;
	
	public SyncWeb(Context context){
		this.context = context;
	}
	
	@Override  
    protected void onPreExecute() {
		dialog = new ProgressDialog(context);
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
				new DialogInterface.OnClickListener(){ 
				@Override
				public void onClick(DialogInterface dialog, int which) {
					cancel(true);
					dialog.dismiss();
				}         
			});
		dialog.setMessage("test¡A½Ðµy­Ô....");
		dialog.show();
        super.onPreExecute();  
    }
	
	@Override
	protected String doInBackground(String... arg0) {
		// TODO Auto-generated method stub
		try {
			return GetWebJson("http://140.130.1.114/moneytime/home.php/?mod=monAPI&act=getdate&username=admin&date=20131211");
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
		//return null;
	}
	
	@Override
	protected void onPostExecute(final String result) {
		dialog.dismiss();
		//Toast.makeText(context, result, Toast.LENGTH_LONG).show();
		try {
			JSONObject JsonData = new JSONObject(result);
			JSONArray DataArray = JsonData.getJSONArray("data");
			for(int size = 0;size < DataArray.length();size++){
				JSONObject Data = DataArray.getJSONObject(size);
				Toast.makeText(context, 
					Data.getString("_Date")+"\t" + 
					Data.getString("_Category")+"\t" + 
					Data.getString("_Note")+"\t" + 
					Data.getString("_InCome")+"\t" + 
					Data.getString("_OutGo")+"\t" + 
					Data.getString("_ReceiptNumber")+"\t",
						Toast.LENGTH_LONG).show();
			}
			//Toast.makeText(context, result, Toast.LENGTH_LONG).show();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    }
	
	private String GetWebJson(String mUrl) throws IOException {
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
            String newString = sb.toString();//.replaceAll("[\\[\\]]","");
            return "{" +"\"data\":" + newString + "}";
        }
        return null;
    }
	
	public StringBuffer htmlUnicodeToJavaUnicode(String inputs){
	    StringBuffer result=new StringBuffer("");
	    int position =inputs.indexOf("&#");
	    int position2=inputs.indexOf(";", position+2);
	    if ( position >=0 && position2 >=0 ){
	        String befores = inputs.substring(0, position);            
	        String afters = inputs.substring(position2+1,inputs.length());
	        String middles = inputs.substring(position+2, position2);
	        String hexString = Integer.toHexString(Integer.parseInt(middles));
	        if(hexString.length() %2 != 0) {
	            hexString ="0".concat(hexString);
	        }
			int hl = hexString.length()/2;
			byte[] p  = {-2,-1,0,0};
			hl=3;
			int initf = 0;
			for(int i=0 ;i<hexString.length();i+=2){
				p[hl-1] =(byte) Integer.parseInt(hexString.substring(i, i+2),16);
				initf++;
				hl++;
			}
			try{               
				result = result.append(befores).append(new String(p,"UTF-16")).append(htmlUnicodeToJavaUnicode(afters));
			}
			catch(Exception e ){
			}
		}else{
			result = result.append(inputs);                       
		}
		return result;
	} 

}
