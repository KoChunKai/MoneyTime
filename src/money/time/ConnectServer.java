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
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

public class ConnectServer extends AsyncTask<String, Integer, String> {

	private Context context;
	private String username,date,_category,_childcategory,_note,_income,_outgo,receipt;
	private String[] serid;
	
	public ConnectServer(Context context){this.context = context;}
	
	public ConnectServer(
			Context context,
			String username,
			String date,
			String _category,
			String _childcategory,
			String _note,
			String _income,
			String _outgo,
			String receipt){
		this.context = context;
		this.username = username;
		this.date = date;
		this._category = _category;
		this._childcategory = _childcategory;
		this._note = _note;
		this._income = _income;
		this._outgo = _outgo;
		this.receipt = receipt;
	}
	
	public ConnectServer(
			Context context,
			String[] serid,
			String date,
			String _category,
			String _childcategory,
			String _note,
			String _income,
			String _outgo,
			String receipt) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.serid = serid;
		this.date = date;
		this._category = _category;
		this._childcategory = _childcategory;
		this._note = _note;
		this._income = _income;
		this._outgo = _outgo;
		this.receipt = receipt;
	}

	public ConnectServer(Context context,String[] serid) {
		this.context = context;
		this.serid = serid;
	}

	@Override
	protected String doInBackground(String... arg0) {
		// TODO Auto-generated method stub
		try {
			if(arg0[0].contains("add")){
				DBHelper DH = null;
				DH = new DBHelper(context, null, null, 0);
				SQLiteDatabase db = DH.getWritableDatabase();
		    	ContentValues values = new ContentValues();
		    	values.put("_SerId", GetVaule("http://140.130.1.114/moneytime/home.php?mod=monAPI&act=add"
						+"&username=" + username
						+"&date=" + date 
						+"&_category=" + _category
						+"&_childcategory=" + _childcategory
						+"&_note=" + _note
						+"&_income=" + _income
						+"&_outgo=" + _outgo
						+"&receipt=" + receipt));
		    	values.put("_DateYear", date.substring(0, 4));
		    	values.put("_DateMonth", date.substring(4, 6));
		    	values.put("_DateDay", date.substring(6, 8));
		    	values.put("_Category", _category);
		    	values.put("_ChildCategory", _childcategory);
		    	values.put("_Note", _note);
		    	values.put("_InCome", _income);
		    	values.put("_OutGo", _outgo);
		    	values.put("_ReceiptNumber", receipt);
		    	db.insert("MTDB", null, values);
				return "新增成功";
			}
			else if(arg0[0].contains("edit")){
				GetVaule("http://140.130.1.114/moneytime/home.php?mod=monAPI&act=edit"
						+"&id=" + serid[0]
						+"&date=" + date 
						+"&_category=" + _category
						+"&_childcategory=" + _childcategory
						+"&_note=" + _note
						+"&_income=" + _income
						+"&_outgo=" + _outgo
						+"&receipt=" + receipt);
				return "修改成功";
			}
			else if(arg0[0].contains("del")){
				GetVaule("http://140.130.1.114/moneytime/home.php?mod=monAPI&act=del&id="
						+ serid[0]
						+ "&UUID=");
				return "刪除成功";
			}
			else if(arg0[0].contains("CheckServerData")){
				SharedPreferences UserData = context.getSharedPreferences("UserData", 0);
				String test = "";
				//----------****----------//
				SyncTable();
				//----------****----------//
				try {
					JSONObject JsonData = new JSONObject(GetVaule("http://140.130.1.114/moneytime/home.php/?mod=monAPI&act=syn&username="+ UserData.getString("UserName", "")));
					JSONArray DataArray = JsonData.getJSONArray("data");
					for(int size = 0;size < DataArray.length();size++){
						JSONObject RegDataValue = DataArray.getJSONObject(size);
						//JSONArray RegAddArray = RegDataValue.getJSONArray(RegDataValue.names().toString().replaceAll("[\\[\\]]","").replaceAll("\"", ""));
						//JSONObject Value = RegAddArray.getJSONObject(0);
						if(RegDataValue.names().toString().contains("add")){
							JSONArray RegAddArray = RegDataValue.getJSONArray(RegDataValue.names().toString().replaceAll("[\\[\\]]","").replaceAll("\"", ""));
							JSONObject Value = RegAddArray.getJSONObject(0);
							Add(Value);
						}
						else if(RegDataValue.names().toString().contains("edit")){
							JSONArray RegAddArray = RegDataValue.getJSONArray(RegDataValue.names().toString().replaceAll("[\\[\\]]","").replaceAll("\"", ""));
							JSONObject Value = RegAddArray.getJSONObject(0);
							Edit(Value);
							test = test + "Edit("+size+")" + "\n";
						}
						else{
							Del(RegDataValue.getString("id"));
						}
					}
					GetVaule("http://140.130.1.114/moneytime/home.php?mod=monAPI&act=syn_finish&username="+ UserData.getString("UserName", ""));
					return ""+ test;
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return "error";
				}
			}
			else{
				return null;
			}
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
	}
	
	@Override
	protected void onPostExecute(final String result) {
		//Toast.makeText(context, result, Toast.LENGTH_LONG).show();
		
    }
	
	private String GetVaule(String mUrl) throws IOException {
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
            return sb.toString();
        }
        return null;
    }
	
	private void Add(JSONObject Value) throws JSONException{
		DBHelper DH = new DBHelper(context, null, null, 0);
		SQLiteDatabase db = DH.getWritableDatabase();
    	ContentValues values = new ContentValues();
    	values.put("_SerId", Value.getString("id"));
		values.put("_DateYear", Value.getString("_Date").substring(0, 4));
    	values.put("_DateMonth", Value.getString("_Date").substring(4, 6));
    	values.put("_DateDay", Value.getString("_Date").substring(6, 8));
    	values.put("_Category", Value.getString("_Category"));
    	values.put("_ChildCategory", Value.getString("_ChildCategory"));
    	values.put("_Note", Value.getString("_Note"));
    	values.put("_InCome", Value.getString("_InCome"));
    	values.put("_OutGo", Value.getString("_OutGo"));
    	values.put("_ReceiptNumber", Value.getString("_ReceiptNumber"));
    	db.insert("MTDB", null, values);
	}
	
	private void Edit(JSONObject Value) throws JSONException{
		DBHelper DH = new DBHelper(context, null, null, 0);
		SQLiteDatabase db = DH.getWritableDatabase();
		ContentValues upData = new ContentValues();
		String[] SerId = new String[] {Value.getString("id")};
		upData.put("_DateYear", Value.getString("_Date").substring(0, 4));
    	upData.put("_DateMonth", Value.getString("_Date").substring(4, 6));
    	upData.put("_DateDay", Value.getString("_Date").substring(6, 8));
    	upData.put("_Category", Value.getString("_Category"));
    	upData.put("_ChildCategory", Value.getString("_ChildCategory"));
    	upData.put("_Note", Value.getString("_Note"));
    	upData.put("_InCome", Value.getString("_InCome"));
    	upData.put("_OutGo", Value.getString("_OutGo"));
    	upData.put("_ReceiptNumber", Value.getString("_ReceiptNumber"));
		db.update("MTDB", upData, "_SerId=?" , SerId);
	}
	
	private void Del(String string) throws JSONException{
		DBHelper DH = new DBHelper(context, null, null, 0);
		SQLiteDatabase db = DH.getWritableDatabase();
		String[] SerId = new String[] {string};
		db.delete("MTDB", "_SerId=?", SerId);
	}
	
	private void SyncTable() throws IOException{
		DBHelper DH = new DBHelper(context, null, null, 0);
		SQLiteDatabase db = DH.getReadableDatabase();
		String Conditional;
		Cursor SyncData;
		Conditional = "SELECT * FROM MTDB WHERE _SerId = 0";
		SyncData = db.rawQuery(Conditional, null);
		if (SyncData.getCount() > 0){    
			SQLiteDatabase getSerId = DH.getWritableDatabase();
			SyncData.moveToFirst();
		    do {
		    	String dateval;
		    	if(SyncData.getString(SyncData.getColumnIndex("_DateMonth")).length() == 1){
		    		dateval = SyncData.getString(SyncData.getColumnIndex("_DateYear")) 
							+0
							+SyncData.getString(SyncData.getColumnIndex("_DateMonth")) 
							+SyncData.getString(SyncData.getColumnIndex("_DateDay"));
				}else{
					dateval = SyncData.getString(SyncData.getColumnIndex("_DateYear")) 
							+SyncData.getString(SyncData.getColumnIndex("_DateMonth")) 
							+SyncData.getString(SyncData.getColumnIndex("_DateDay"));
				}
		    	ContentValues UpData = new ContentValues();
		    	UpData.put("_SerId", GetVaule("http://140.130.1.114/moneytime/home.php?mod=monAPI&act=add"
						+"&username=" + username
						+"&date=" + dateval 
						+"&_category=" + SyncData.getString(SyncData.getColumnIndex("_Category"))
						+"&_childcategory=" + SyncData.getString(SyncData.getColumnIndex("_ChildCategory"))
						+"&_note=" + SyncData.getString(SyncData.getColumnIndex("_Note"))
						+"&_income=" + SyncData.getString(SyncData.getColumnIndex("_InCome"))
						+"&_outgo=" + SyncData.getString(SyncData.getColumnIndex("_OutGo"))
						+"&receipt=" + SyncData.getString(SyncData.getColumnIndex("_ReceiptNumber"))));
		    	getSerId.update("MTDB", UpData, "_Id=?" ,new String[]{SyncData.getString(SyncData.getColumnIndex("_Id"))});
		    } while (SyncData.moveToNext());
		    SyncData.close();
		}
		Conditional = "SELECT _SerId,_Type FROM SYNCDB";
		SyncData = db.rawQuery(Conditional, null);
		if (SyncData.getCount() > 0){    
			SyncData.moveToFirst();
		    do {
		    	if(SyncData.getString(SyncData.getColumnIndex("_Type")).contains("0")){//edit
		    		SQLiteDatabase MTDBREAD = DH.getReadableDatabase();
		    		String MTDBConditional = 
							"SELECT * FROM MTDB WHERE _SerId = " 
							+ SyncData.getString(SyncData.getColumnIndex("_SerId"));
		    		Cursor GetData = MTDBREAD.rawQuery(MTDBConditional, null);
					if (GetData.getCount() > 0){   
						GetData.moveToFirst();
						String dateval;
				    	if(GetData.getString(GetData.getColumnIndex("_DateMonth")).length() == 1){
				    		dateval = GetData.getString(GetData.getColumnIndex("_DateYear")) 
									+0
									+GetData.getString(GetData.getColumnIndex("_DateMonth")) 
									+GetData.getString(GetData.getColumnIndex("_DateDay"));
						}else{
							dateval = GetData.getString(GetData.getColumnIndex("_DateYear")) 
									+GetData.getString(GetData.getColumnIndex("_DateMonth")) 
									+GetData.getString(GetData.getColumnIndex("_DateDay"));
						}
				    	GetVaule("http://140.130.1.114/moneytime/home.php?mod=monAPI&act=edit"
								+"&id=" + GetData.getString(GetData.getColumnIndex("_SerId"))
								+"&date=" + dateval 
								+"&_category=" + GetData.getString(GetData.getColumnIndex("_Category")) 
								+"&_childcategory=" + GetData.getString(GetData.getColumnIndex("_ChildCategory")) 
								+"&_note=" + GetData.getString(GetData.getColumnIndex("_Note")) 
								+"&_income=" + GetData.getString(GetData.getColumnIndex("_InCome")) 
								+"&_outgo=" + GetData.getString(GetData.getColumnIndex("_OutGo")) 
								+"&receipt=" + GetData.getString(GetData.getColumnIndex("_ReceiptNumber")));
						GetData.close();
					}
		    	}else{//del
		    		GetVaule("http://140.130.1.114/moneytime/home.php?mod=monAPI&act=del&id="
							+ SyncData.getString(SyncData.getColumnIndex("_SerId"))
							+ "&UUID=");
		    	}
		    } while (SyncData.moveToNext());
		    SyncData.close();
		}
		SQLiteDatabase ClearSyncTable = DH.getWritableDatabase();
		ClearSyncTable.delete("SYNCDB", null, null);
	}

}
