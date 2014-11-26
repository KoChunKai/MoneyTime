package money.time;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MoneyTimeAccount extends AsyncTask<String, Integer, String> {

	Context context;
	String UserName,Password,Email;
	AlertDialog alert;
	Button LoginLogout;
	boolean mod;
    private static SharedPreferences UserData;
	
	public MoneyTimeAccount(Context context, String UserName, String Password, String Email, AlertDialog alert, Button LoginLogout, boolean mod){
		this.context = context;
		this.UserName = UserName;
		this.Password = Password;
		this.alert = alert;
		this.mod = mod;
		this.Email = Email;
		this.LoginLogout = LoginLogout;
	}
	protected void onPreExecute() {
		if(mod){
			Toast.makeText(context, "登入中，請稍候....", Toast.LENGTH_LONG).show();
		}
		else{
			Toast.makeText(context, "註冊中，請稍候....", Toast.LENGTH_LONG).show();
		}
        super.onPreExecute();  
    }
	@Override
	protected String doInBackground(String... arg0) {
		try {
			return Post(arg0[0],UserName,Password,Email);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "error";
		}
		
	}
	@Override
	protected void onPostExecute(final String result) {
		if(result.contains("null")){
			Toast.makeText(context, "帳號或密碼錯誤喔!!第一次使用請先註冊喔!!", Toast.LENGTH_LONG).show();
		}
		else{
			//Toast.makeText(context, result, Toast.LENGTH_LONG).show();
			UserData = context.getSharedPreferences("UserData", 0);
			try {
				JSONObject JsonData = new JSONObject(result);
				SharedPreferences.Editor editor = UserData.edit();
				editor.putString("UserName", JsonData.getString("username"));
				editor.putString("Email", JsonData.getString("email"));
				editor.putString("UUID", android.provider.Settings.System.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID));
				editor.commit();
				alert.dismiss();
				LoginLogout.setText(JsonData.getString("username") + "登出");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
	
	private String Post(String mod,String username,String password,String email) throws IOException{
		String mUrl = "http://140.130.1.114/moneytime/home.php?mod=UserMobile&act=" + mod;
		StringBuilder sb = new StringBuilder();
		HttpPost post = new HttpPost(mUrl);
		HttpResponse responsePOST = null;
		HttpClient ReceipthttpClient = MySSLSocketFactory.createMyHttpClient();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("username", username));
		params.add(new BasicNameValuePair("password", password));
		params.add(new BasicNameValuePair("email", email));
		params.add(new BasicNameValuePair("UUID", android.provider.Settings.System.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID)));
		UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
		post.setEntity(ent);
		responsePOST = ReceipthttpClient.execute(post);
		HttpEntity httpEntity = responsePOST.getEntity();
		if (httpEntity != null) {
            InputStream instream = httpEntity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    instream));
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            instream.close();
            return sb.toString();
        }
        return null;
		
	}

}
