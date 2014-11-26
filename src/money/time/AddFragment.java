package money.time;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class AddFragment extends Fragment {
	
	private TextView Date;
	private ImageButton DateAdd,DateSub,CategoryPlus,ChildCategoryPlus,NotePlus;
	private EditText CategoryEdit,ChildCategoryEdit,NoteEdit,MoneyEdit;
	private Button Save,QRcodeScanner,LoginLogout;
	private RadioGroup rdg;
	private Calendar c;
	private SimpleDateFormat df;
	private DBHelper DH = null;
	private Boolean WhichMethodVaule = false;
	private static String DateReg;
	private String invNumValue,invTermValue,invDateValue,UUIDValue,randomNumberValue,GetUrl;
	private static SharedPreferences UserData;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.add_layout, container, false);
		return view;
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		FindCompent();
		DateInitialization();
		CheckAccountViewButton();
		
		DateAdd.setOnClickListener(Lisenter);
		DateSub.setOnClickListener(Lisenter);
		CategoryPlus.setOnClickListener(Lisenter);
		ChildCategoryPlus.setOnClickListener(Lisenter);
		NotePlus.setOnClickListener(Lisenter);
		Save.setOnClickListener(Lisenter);
		QRcodeScanner.setOnClickListener(Lisenter);
		LoginLogout.setOnClickListener(Lisenter);
		
		rdg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
		    public void onCheckedChanged(RadioGroup group, final int checkedId) {
		        switch (checkedId) {
		            case R.id.rdo1:
		            	WhichMethodVaule = true;
		                break;
		            case R.id.rdo2:
		            	WhichMethodVaule = false;
		                break;
		            }
		    }});
	}
	
	private Button.OnClickListener Lisenter = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.dateadd:
				c.add(Calendar.DAY_OF_WEEK, 1);
				Date.setText(df.format(c.getTime()));
				DateReg = df.format(c.getTime());
				break;
			case R.id.datesub:
				c.add(Calendar.DAY_OF_WEEK, -1);
				Date.setText(df.format(c.getTime()));
				DateReg = df.format(c.getTime());
				break;
			case R.id.CategoryPlus:
				QuickSelect(R.id.CategoryPlus);
				break;
			case R.id.ImageButton01:
				QuickSelect(R.id.ImageButton01);
				break;
			case R.id.ImageButton02:
				QuickSelect(R.id.ImageButton02);
				break;
			case R.id.savebutton:
				AddData();
				break;
			case R.id.qrbutton:
				Intent intent = new Intent("com.google.zxing.client.android.SCAN");	//開啟條碼掃描器
				intent.putExtra("SCAN_MODE", "QR_CODE_MODE");	//設定QR Code參數
				startActivityForResult(intent, 1);
				break;
			case R.id.accountbuttonn:
				DoActionAccount();
				break;
			}
		}
	};

	private void FindCompent(){
		DateAdd = (ImageButton)getView().findViewById(R.id.dateadd);
		DateSub = (ImageButton)getView().findViewById(R.id.datesub);
		CategoryPlus = (ImageButton)getView().findViewById(R.id.CategoryPlus);
		ChildCategoryPlus = (ImageButton)getView().findViewById(R.id.ImageButton01);
		NotePlus = (ImageButton)getView().findViewById(R.id.ImageButton02);
		CategoryEdit = (EditText)getView().findViewById(R.id.editText1);
		ChildCategoryEdit = (EditText)getView().findViewById(R.id.editText2);
		NoteEdit = (EditText)getView().findViewById(R.id.editText3);
		MoneyEdit = (EditText)getView().findViewById(R.id.editText4);
		rdg=(RadioGroup)getView().findViewById(R.id.rdg1);
		Save = (Button)getView().findViewById(R.id.savebutton);
		QRcodeScanner = (Button)getView().findViewById(R.id.qrbutton);
		LoginLogout = (Button)getView().findViewById(R.id.accountbuttonn);
		UserData = getActivity().getSharedPreferences("UserData", 0);
		openDB();
	}
	
	@SuppressLint("SimpleDateFormat")
	private void DateInitialization(){
		c = Calendar.getInstance();
		df = new SimpleDateFormat("yyyy/MM/dd");
		Date = (TextView)getView().findViewById(R.id.dateview);
		Date.setText(df.format(c.getTime()));
		DateReg = df.format(c.getTime());
	}
	
	private void openDB() {
		// TODO Auto-generated method stub
    	DH = new DBHelper(getActivity(), null, null, 0);
	}
	
	private void QuickSelect(int condition){
		AlertDialog.Builder SelectDialog = new AlertDialog.Builder(getActivity());
		String SelectConditional = null;
		SQLiteDatabase db = DH.getReadableDatabase();
		ArrayAdapter<String> QSadapter;
		final ArrayList<String> QSLsitData = new ArrayList<String>();
		Cursor QSList;
		switch (condition){
		case R.id.CategoryPlus:
			SelectConditional = 
				"SELECT _Category FROM MTDB " +
				"GROUP BY _Category";
			QSList = db.rawQuery(SelectConditional, null);
			if (QSList.getCount() > 0){
				QSList.moveToFirst();
				do {
					if(QSList.getString(QSList.getColumnIndex("_Category")).matches("")) QSList.moveToNext();
					QSLsitData.add(QSList.getString(QSList.getColumnIndex("_Category")));
				}while (QSList.moveToNext());
				SelectDialog.setTitle(R.string.Category);
				QSadapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_item, QSLsitData);
				SelectDialog.setAdapter(QSadapter,
		        		new DialogInterface.OnClickListener() {
		        			public void onClick(DialogInterface dialog, int which) {
		        				CategoryEdit.setText(QSLsitData.get(which));
		        			}
		        		});
				
			}
			else{
				SelectDialog.setTitle(R.string.Category);
				SelectDialog.setMessage("目前還沒有快選資料！");
			}
			break;
		case R.id.ImageButton01:
			SelectConditional = 
				"SELECT _ChildCategory FROM MTDB " + "GROUP BY _ChildCategory";
			QSList = db.rawQuery(SelectConditional, null);
		if (QSList.getCount() > 0){
			QSList.moveToFirst();
			do {
				if(QSList.getString(QSList.getColumnIndex("_ChildCategory")).matches("")) QSList.moveToNext();
				QSLsitData.add(QSList.getString(QSList.getColumnIndex("_ChildCategory")));
			}while (QSList.moveToNext());
			SelectDialog.setTitle(R.string.ChildCategory);
			QSadapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_item, QSLsitData);
			SelectDialog.setAdapter(QSadapter,
	        		new DialogInterface.OnClickListener() {
	        			public void onClick(DialogInterface dialog, int which) {
	        				ChildCategoryEdit.setText(QSLsitData.get(which));
	        			}
	        		});
			
		}
		else{
			SelectDialog.setTitle(R.string.ChildCategory);
			SelectDialog.setMessage("目前還沒有快選資料！");
		}
			break;
		case R.id.ImageButton02:
			SelectConditional = 
					"SELECT _Note FROM MTDB " + "GROUP BY _Note";
				QSList = db.rawQuery(SelectConditional, null);
			if (QSList.getCount() > 0){
				QSList.moveToFirst();
				do {
					if(QSList.getString(QSList.getColumnIndex("_Note")).matches("")) QSList.moveToNext();
					QSLsitData.add(QSList.getString(QSList.getColumnIndex("_Note")));
				}while (QSList.moveToNext());
				SelectDialog.setTitle(R.string.Note);
				QSadapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_item, QSLsitData);
				SelectDialog.setAdapter(QSadapter,
		        		new DialogInterface.OnClickListener() {
		        			public void onClick(DialogInterface dialog, int which) {
		        				NoteEdit.setText(QSLsitData.get(which));
		        			}
		        		});
				
			}
			else{
				SelectDialog.setTitle(R.string.Note);
				SelectDialog.setMessage("目前還沒有快選資料！");
			}
			break;
		}
		SelectDialog.setNegativeButton("返回", null);
		SelectDialog.show();
		
	}
	
	private void AddData(){
		if(CategoryEdit.getText().toString().matches("")
				|| ChildCategoryEdit.getText().toString().matches("")
				|| MoneyEdit.getText().toString().matches("")){
				Toast.makeText(getActivity(), "資料尚未完整呢!", Toast.LENGTH_LONG).show();
			}
			else{
				if(WhichMethodVaule == false){
			        if(UserData.getString("UserName", "").isEmpty()){
			        	add(
			        		0
							,Integer.parseInt(DateReg.substring(0, 4).toString())
							,Integer.parseInt(DateReg.substring(5, 7).toString())
							,Integer.parseInt(DateReg.substring(8, 10).toString())
							,CategoryEdit.getText().toString()
							,ChildCategoryEdit.getText().toString()
							,NoteEdit.getText().toString()
							,0
							,Integer.parseInt(MoneyEdit.getText().toString())
							,null);
			        }
			        else{
			        	new ConnectServer(
								getActivity(),
								UserData.getString("UserName", ""), 
								DateReg.replaceAll("/", ""), 
								CategoryEdit.getText().toString(), 
								ChildCategoryEdit.getText().toString(), 
								NoteEdit.getText().toString(), 
								"0", 
								MoneyEdit.getText().toString(), 
								"").execute("add");
			        }
				}
				else{
					if(UserData.getString("UserName", "").isEmpty()){
			        	add(
			        		0
							,Integer.parseInt(DateReg.substring(0, 4).toString())
							,Integer.parseInt(DateReg.substring(5, 7).toString())
							,Integer.parseInt(DateReg.substring(8, 10).toString())
							,CategoryEdit.getText().toString()
							,ChildCategoryEdit.getText().toString()
							,NoteEdit.getText().toString()
							,Integer.parseInt(MoneyEdit.getText().toString())
							,0
							,null);
			        }
			        else{
			        	new ConnectServer(
								getActivity(),
								UserData.getString("UserName", ""), 
								DateReg.replaceAll("/", ""), 
								CategoryEdit.getText().toString(), 
								ChildCategoryEdit.getText().toString(), 
								NoteEdit.getText().toString(),  
								MoneyEdit.getText().toString(), 
								"0",
								"").execute("add");
			        }
				}
				
			}
		CategoryEdit.setText("");
		ChildCategoryEdit.setText("");
		NoteEdit.setText("");
		MoneyEdit.setText("");
	}
	
	private void add(
			int SerId,
			int DateYear,
			int DateMonth,
			int DateDay,
	    	String Category,
	    	String ChildCategory,
	    	String Note,
	    	int InCome,	
	    	int OutGo,
	    	String ReceiptNumber){
    	SQLiteDatabase db = DH.getWritableDatabase();
    	ContentValues values = new ContentValues();
    	values.put("_SerId", SerId);
    	values.put("_DateYear", DateYear);
    	values.put("_DateMonth", DateMonth);
    	values.put("_DateDay", DateDay);
    	values.put("_Category", Category);
    	values.put("_ChildCategory", ChildCategory);
    	values.put("_Note", Note);
    	values.put("_InCome", InCome);
    	values.put("_OutGo", OutGo);
    	values.put("_ReceiptNumber", ReceiptNumber);
    	db.insert("MTDB", null, values);
    }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		String contents = data.getStringExtra("SCAN_RESULT");	//取得QR Code內容
		invNumValue = (String) contents.subSequence(0, 10);
		
		if(contents.charAt(14)%2!=0){
			if(contents.charAt(14) == '9'){
				invTermValue = contents.subSequence(10, 13) + ""+10;
			}
			else{
				invTermValue = contents.subSequence(10, 14) +""+ ((char)(contents.charAt(14)+1));
			}
		}
		else{
			invTermValue = (String) contents.subSequence(10, 15);
		}
		invDateValue = ""
				+ (1911 + Integer.parseInt((String) contents.subSequence(10, 13)))
				+ "/"
				+ (String) contents.subSequence(13, 15)
				+ "/"
				+ (String) contents.subSequence(15, 17);
		UUIDValue = android.provider.Settings.System.getString(getActivity().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
		randomNumberValue = (String) contents.subSequence(17, 21);
		
		GetUrl = "https://www.einvoice.nat.gov.tw/PB2CAPIVAN/invapp/InvApp?version=0.2&type=Barcode"
				+ "&invNum="
				+ invNumValue
				+ "&action=qryInvDetail&generation=V2"
				+ "&invTerm="
				+ invTermValue
				+ "&invDate="
				+ invDateValue
				+ "&encrypt=&sellerID=&"
				+ "&UUID="
				+ UUIDValue
				+ "&randomNumber="
				+ randomNumberValue
				+ "&appID=EINV3201306053713";	
		new MyAsyncTask().execute(GetUrl);
	}
	
	public class MyAsyncTask extends AsyncTask<String, Integer, String> 
    {
		private ProgressDialog dialog = null;

		@Override  
        protected void onPreExecute() {
			dialog = new ProgressDialog(getActivity());
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
					new DialogInterface.OnClickListener(){ 
					@Override
					public void onClick(DialogInterface dialog, int which) {
						cancel(true);
						dialog.dismiss();
					}         
				});
			dialog.setMessage("資料讀取中，請稍候....");
			dialog.show();
			
            super.onPreExecute();  
        }  
		
		protected String doInBackground(String... urls) {
            try {
                return GetReceiptJson(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return "error";
            }
        }
		
		@Override
		protected void onPostExecute(final String result) {
			//doInBackground全部執行完後觸發  
	        //這裡的result就是上面doInBackground執行後的返回值，所以這裡是urls[0]的值 
			super.onPostExecute(result); 
			dialog.dismiss();
			AlertDialog.Builder ReceiptW = new AlertDialog.Builder(getActivity());
			LayoutInflater inflater;
			View V;
			TextView invNum,invDate;
			ListView Detail;
			int SumPrice = 0;
			ArrayAdapter<String> adapter;
			inflater = LayoutInflater.from(getActivity());
	        V = inflater.inflate(R.layout.receipt_layout,null);
	        invNum = (TextView)V.findViewById(R.id.receipt_number);
	        invDate = (TextView)V.findViewById(R.id.receipt_date);
	        Detail = (ListView)V.findViewById(R.id.receipt_detail);
	        adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1);
	        adapter.clear();
	        try {
				JSONObject JsonData = new JSONObject(result);
				JSONArray DetailArray = JsonData.getJSONArray("details");
				invNum.setText(JsonData.getString("invNum").toString());
				invDate.setText(JsonData.getString("invDate").toString());
				for(int size = 0;size < DetailArray.length();size++){
					JSONObject DetailData = DetailArray.getJSONObject(size);
					adapter.add(DetailData.getString("description")+"\t"+(int)Double.parseDouble((DetailData.getString("amount")))+"元");
					SumPrice = SumPrice + (int)Double.parseDouble((DetailData.getString("amount")));
				}
				adapter.add("合計：\t"+ SumPrice);
				Detail.setAdapter(adapter);
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
	        ReceiptW.setPositiveButton("儲存",
					new DialogInterface.OnClickListener() {
    					@Override
    					public void onClick(DialogInterface dialog, int which) {
    						try {
    							JSONObject JsonData = new JSONObject(result);
        						JSONArray DetailArray = JsonData.getJSONArray("details");
    							for(int size = 0;size < DetailArray.length();size++){
    								JSONObject DetailData = DetailArray.getJSONObject(size);
    								if(WhichMethodVaule == false){
    							        if(UserData.getString("UserName", "").isEmpty()){
    							        	add(
    							        		0
    		    								,Integer.parseInt(JsonData.getString("invDate").subSequence(0, 4).toString())
    		    								,Integer.parseInt(JsonData.getString("invDate").subSequence(4, 6).toString())
    		    								,Integer.parseInt(JsonData.getString("invDate").subSequence(6, 8).toString())
    		    								,"","",DetailData.getString("description")
    		    								,0
    		    								,(int)Double.parseDouble((DetailData.getString("amount")))
    		    								,JsonData.getString("invNum").toString()
    		    								);
    							        }
    							        else{
    							        	new ConnectServer(
    												getActivity(),
    												UserData.getString("UserName", ""), 
    												JsonData.getString("invDate"), 
    												"", 
    												"", 
    												DetailData.getString("description"), 
    												"0", 
    												""+(int)Double.parseDouble((DetailData.getString("amount"))), 
    												JsonData.getString("invNum").toString()).execute("add");
    							        }
    								}
    								/*add(
    									0
    									,Integer.parseInt(JsonData.getString("invDate").subSequence(0, 4).toString())
    									,Integer.parseInt(JsonData.getString("invDate").subSequence(4, 6).toString())
    									,Integer.parseInt(JsonData.getString("invDate").subSequence(6, 8).toString())
    									,"","",DetailData.getString("description"),0
    									,(int)Double.parseDouble((DetailData.getString("amount")))
    									,JsonData.getString("invNum").toString()
    									);*/
    							}
    						} catch (JSONException e) {
    							e.printStackTrace();
    						}
    						Toast.makeText(getActivity(), "儲存成功!", Toast.LENGTH_LONG).show();
    					}
    				});
	        ReceiptW.setNeutralButton("返回",null);
			V.setBackgroundColor(Color.WHITE);
			ReceiptW.setView(V);
			ReceiptW.show();
	    }
    }
	
	private String GetReceiptJson(String mUrl) throws IOException {
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
	
	private void CheckAccountViewButton(){
        if(UserData.getString("UserName", "").isEmpty()){
        	LoginLogout.setText("離線模式中，同步請登入");
        }
        else{
        	LoginLogout.setText(UserData.getString("UserName", "") + "登出");
        }
	}
	
	private void DoActionAccount(){
        if(LoginLogout.getText().toString().contains("登出")){
        	Toast.makeText(getActivity(), "登出囉....", Toast.LENGTH_LONG).show();
        	SharedPreferences.Editor editor = UserData.edit();
			editor.remove("UserName");
			editor.remove("Email");
			editor.remove("UUID");
			editor.commit();
			LoginLogout.setText("離線模式中，同步請登入");
        }
        else{
        	LoginRegistration();
        }
	}
	
    public void LoginRegistration(){
		final Button Login,Sign;
		final EditText  UserName, Passsword;
		TextView UserNameView,PassswordView;
		AlertDialog.Builder LoginW = new AlertDialog.Builder(getActivity()); 
		LayoutInflater inflater;
		final View V;
		
		final AlertDialog alert = LoginW.create();
		inflater = LayoutInflater.from(getActivity());
        V = inflater.inflate(R.layout.login_registration_layout,null);
        
        Login = (Button)V.findViewById(R.id.loginbutton);
        Sign = (Button)V.findViewById(R.id.signbutton);
        
        DisplayMetrics dm = new DisplayMetrics(); 
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm); //先取得螢幕解析度  
        final int screenWidth = dm.widthPixels;
        Login.setWidth(screenWidth/2);
        Sign.setWidth(screenWidth/2);
        
        UserName = (EditText)V.findViewById(R.id.cellphoneeditText);
        Passsword = (EditText)V.findViewById(R.id.checknumeditText);
        UserNameView = (TextView)V.findViewById(R.id.textView1);
        PassswordView = (TextView)V.findViewById(R.id.textView2);
        
        UserNameView.setText("帳號");
        PassswordView.setText("密碼");
        UserName.setInputType(InputType.TYPE_CLASS_TEXT);
        
        V.findViewById(R.id.imgnumeditText).setVisibility(View.GONE);
        V.findViewById(R.id.textView3).setVisibility(View.GONE);
        V.findViewById(R.id.imagecheck).setVisibility(View.GONE);
       
        
        Login.setOnClickListener(new Button.OnClickListener(){ 
			@Override
			public void onClick(View v) {
				//SharedPreferences.Editor editor = UserData.edit();
				MoneyTimeAccount LoginStatus = new MoneyTimeAccount(getActivity()
						,UserName.getText().toString()
						,Passsword.getText().toString(),"",alert,LoginLogout,true);
				LoginStatus.execute("doLog");
				if(!LoginStatus.getStatus().equals(AsyncTask.Status.FINISHED)){
					Toast.makeText(getActivity(), "登入成功....", Toast.LENGTH_LONG).show();
					new ConnectServer(getActivity()).execute("CheckServerData");
				}
			}
		});
        
        Sign.setOnClickListener(new Button.OnClickListener(){ 
			@Override
			public void onClick(View v) {
				EditText  Email;
				TextView EmailView;
				Email = (EditText)V.findViewById(R.id.imgnumeditText);
				EmailView = (TextView)V.findViewById(R.id.textView3);
				if(Login.getVisibility()==View.VISIBLE){
					V.findViewById(R.id.imgnumeditText).setVisibility(View.VISIBLE);
			        V.findViewById(R.id.textView3).setVisibility(View.VISIBLE);
					EmailView.setText("E-Mail");
					alert.setTitle("MoneyTime註冊");
					Login.setVisibility(View.GONE);
					Sign.setWidth(screenWidth);
				}
				else{
					new MoneyTimeAccount(getActivity()
							,UserName.getText().toString()
							,Passsword.getText().toString(),Email.getText().toString(),alert,LoginLogout,false).execute("doReg");
				}
			}
		});
        
        V.setBackgroundColor(Color.WHITE);
        alert.setTitle("MoneyTime登入");
		alert.setView(V);
		alert.show();
	}
}