package money.time;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

public class SettingFragment extends Fragment {
	
	private Button GasRemind,WriteRemind,GoogleDriveSync,Export,Ubike,Im_port;
	private PendingIntent pendingIntent;
	private static Calendar calendar;
	private static AlarmManager WriteAlarm,GasAlarm;
	private DBHelper DH = null;
	
	//---Drive Defines Variable---//
	static final int REQUEST_ACCOUNT_PICKER = 1;
	static final int REQUEST_AUTHORIZATION = 2;
	static final int CAPTURE_IMAGE = 3;
	private static Drive service;
	private static GoogleAccountCredential credential;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return inflater.inflate(R.layout.setting_layout, container, false);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		GasRemind = (Button)getView().findViewById(R.id.remindgas);
		WriteRemind = (Button)getView().findViewById(R.id.remindwrite);
		GoogleDriveSync = (Button)getView().findViewById(R.id.googlesync);
		Export = (Button)getView().findViewById(R.id.export);
		Ubike = (Button)getView().findViewById(R.id.ubike);
		Im_port = (Button)getView().findViewById(R.id.im_port);
		GasRemind.setOnClickListener(Lisenter);
		WriteRemind.setOnClickListener(Lisenter);
		GoogleDriveSync.setOnClickListener(Lisenter);
		Export.setOnClickListener(Lisenter);
		Im_port.setOnClickListener(Lisenter);
		Ubike.setOnClickListener(Lisenter);
		
	}
	
	private Button.OnClickListener Lisenter = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.remindgas:
				boolean AlarmGasUp = (PendingIntent.getBroadcast(getActivity(), 0, 
				        new Intent("money.time.MY_ACTION"), 
				        PendingIntent.FLAG_NO_CREATE) != null);
				if(AlarmGasUp){
					CancelFunction("money.time.MY_ACTION");
				}
				else{
					SettingGasFunction();
				}
				break;
			case R.id.remindwrite:
				boolean AlarmWriteUp = (PendingIntent.getBroadcast(getActivity(), 0, 
				        new Intent("money.time.MY_WRITEREMIND"), 
				        PendingIntent.FLAG_NO_CREATE) != null);
				if(AlarmWriteUp){
					CancelFunction("money.time.MY_WRITEREMIND");
				}
				else{
					SettingWriteFunction();
				}
				break;
			case R.id.googlesync:
				BackUpToGoogleDrive();
				break;
			case R.id.export:
				ExportDB();
				break;
			case R.id.im_port:
				ChooseCVS();
				break;
			case R.id.ubike:
				Intent mIntent = new Intent(getActivity(),uBikeMapActivity.class); //Same as above two lines
			    mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			    getActivity().startActivity(mIntent);
				break;
			
			}
		}
	};
	
	private void BackUpToGoogleDrive() {
		// TODO Auto-generated method stub
		credential = GoogleAccountCredential.usingOAuth2(getActivity(), DriveScopes.DRIVE);
		startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
	}
		
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
		case REQUEST_ACCOUNT_PICKER:
			String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
			credential.setSelectedAccountName(accountName);
			service = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential).build();
			File CheckPath = Environment.getExternalStorageDirectory(); // 路徑 /sdcard/
			File CheckFile = new File(CheckPath,"/MoneyTime.csv");
			if(!CheckFile.exists()){
				ExportDB();
			}
			new GoogleAsyncTask().execute("");
			break;
		case 5:
			if(data != null){
				Uri uri = data.getData();
				ImportCSV(data.getData());
				//Toast.makeText(getActivity(), uri.getPath()+"太棒了！匯入成功...", Toast.LENGTH_LONG).show();
			}
			else{
				Toast.makeText(getActivity(), "你沒選擇資料", Toast.LENGTH_LONG).show();
			}
			break;
		}
		
	}
	
	public class GoogleAsyncTask extends AsyncTask<String, Integer, String> {
		private ProgressDialog dialog = new ProgressDialog(getActivity());
		@Override
		protected void onPreExecute() {
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
					new DialogInterface.OnClickListener(){ 
					@Override
					public void onClick(DialogInterface dialog, int which) {
						cancel(true);
						dialog.dismiss();
					}         
				});
			dialog.setMessage("備份中，請稍候....");
			dialog.show();
			super.onPreExecute();
		}

		protected String doInBackground(String... urls) {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						String filename = "MoneyTime.csv";
						String path = Environment.getExternalStorageDirectory().toString()  
				                +File.separator  
				                +"MoneyTime"  
				                +File.separator  
				                +filename; 
						java.io.File fileContent = new java.io.File(path);
						FileContent mediaContent = new FileContent("", fileContent);
						com.google.api.services.drive.model.File body = new com.google.api.services.drive.model.File();
						body.setTitle(fileContent.getName());
						service.files().insert(body, mediaContent).execute();
					} catch (UserRecoverableAuthIOException e) {
						startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			});
			t.start();
			while(t.isAlive()){}
			return "";
		}

		@Override
		// 主UI執行緒將調用這個方法來在畫面上顯示背景任務的進展情況，例如通過一個進度條進行顯示。
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
		}

		@Override
		protected void onPostExecute(String result) {
			dialog.dismiss();
			super.onPostExecute(result);
		}
	}
	
	private void SettingGasFunction() {
		// TODO Auto-generated method stub
		calendar = Calendar.getInstance();   
		Dialog dialog = null;
		dialog = new TimePickerDialog(getActivity(),GasDialogLisenter
				,calendar.get(Calendar.HOUR_OF_DAY)
				,calendar.get(Calendar.MINUTE),  
                true);
		dialog.setTitle("設定每週日提醒時間");
		dialog.show();
	}
	
	private void SettingWriteFunction() {
		// TODO Auto-generated method stub
		calendar = Calendar.getInstance();   
		Dialog dialog = null;
		dialog = new TimePickerDialog(getActivity(),WriteDialogLisenter
				,calendar.get(Calendar.HOUR_OF_DAY)
				,calendar.get(Calendar.MINUTE),  
                true);
		dialog.setTitle("設定每日提醒時間");
		dialog.show();
	}
	
	private void CancelFunction(final String Str) {
		AlertDialog.Builder Cancel = new AlertDialog.Builder(getActivity());
		Cancel.setNeutralButton("取消提醒",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {							
						Intent WriteIntent = new Intent(); 
						WriteAlarm = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
						WriteIntent.setAction(Str);
						pendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 0, WriteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
						WriteAlarm.cancel(pendingIntent);	
						pendingIntent.cancel();
					}
				});
		Cancel.setNegativeButton("返回",null);
		Cancel.show();
	}
	
	private OnTimeSetListener GasDialogLisenter = new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker arg0, int Hour, int Minute) {
			// TODO Auto-generated method stub
			calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.set(Calendar.HOUR_OF_DAY, Hour);
			calendar.set(Calendar.MINUTE, Minute);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			new GasAsyncTask().execute("http://gas.goodlife.tw/");
		}
	};
	
	private OnTimeSetListener WriteDialogLisenter = new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker arg0, int Hour, int Minute) {
			// TODO Auto-generated method stub
			calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.set(Calendar.HOUR_OF_DAY, Hour);
			calendar.set(Calendar.MINUTE, Minute);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			
			Intent WriteIntent = new Intent(); 
			WriteAlarm = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
			WriteIntent.setAction("money.time.MY_WRITEREMIND");
			WriteIntent.putExtra("message", "WriteRemind");
			pendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 0, WriteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			WriteAlarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),1000*24*60*60, pendingIntent);
		}
	};
	
	public class GasAsyncTask extends AsyncTask<String, Integer, String> {
		private ProgressDialog dialog = null;
		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(getActivity(), null, "提醒設定中，請稍候....");
			super.onPreExecute();
		}

		protected String doInBackground(String... urls) {
			try {
				return GetGasInfo(urls[0]);
			} catch (IOException e) {
				e.printStackTrace();
				return "error";
			}
		}

		@Override
		// 主UI執行緒將調用這個方法來在畫面上顯示背景任務的進展情況，例如通過一個進度條進行顯示。
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
		}

		@Override
		protected void onPostExecute(String result) {
			// doInBackground全部執行完後觸發
			// 這裡的result就是上面doInBackground執行後的返回值，所以這裡是urls[0]的值
			dialog.dismiss();
			super.onPostExecute(result);
			Intent GasIntent = new Intent(); 
			GasAlarm = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
			GasIntent.setAction("money.time.MY_ACTION");
			GasIntent.putExtra("message", "GasRemind");
			GasIntent.putExtra("NextGasMoney", result);
			pendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 0, GasIntent, 0);
			GasAlarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
		}
	}

	private String GetGasInfo(String mUrl) throws IOException {
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
			TagNode tagNode;
			String Html = sb.toString();
			tagNode = new HtmlCleaner().clean(Html);
			TagNode[] nodePriceNow = tagNode.getElementsByAttValue("id",
					"gas-price", true, true);
			String NextWeakPrice = nodePriceNow[0].getText().toString()
					.replace(" ", "").replace("-", "降").replace("+", "升");			
			return NextWeakPrice;
		}
		return null;
	}
	
	private void ExportDB() {
		// TODO Auto-generated method stub
		File CSVFile = Environment.getExternalStorageDirectory(); // 路徑 /sdcard/
		//File SaveCSVFile = new File(CSVFile,"/MoneyTime/MoneyTime.csv");
		File SaveCSVFile = new File(CSVFile,"/MoneyTime.csv");
		FileWriter fw;
		BufferedWriter bfw;
		DBHelper DH = new DBHelper(getActivity(), null, null, 0);
		SQLiteDatabase db = DH.getReadableDatabase();
		Cursor ExportCSV = db.rawQuery("SELECT * FROM MTDB ", null);
		try {
			fw = new FileWriter(SaveCSVFile);
			bfw = new BufferedWriter(fw);
			if (ExportCSV.getCount() > 0){               
				ExportCSV.moveToFirst();
				bfw.write("比數,");bfw.write("年,");bfw.write("月,");
				bfw.write("日,");bfw.write("類別,");bfw.write("子類別,");
				bfw.write("備註,");bfw.write("收入,");bfw.write("支出");
				bfw.newLine();	
				do {
					bfw.write(ExportCSV.getString(0) + ',');
					bfw.write(ExportCSV.getString(1) + ',');
					bfw.write(ExportCSV.getString(2) + ',');
					bfw.write(ExportCSV.getString(3) + ',');
					bfw.write(ExportCSV.getString(4) + ',');
					bfw.write(ExportCSV.getString(5) + ',');
					bfw.write(ExportCSV.getString(6) + ',');
					bfw.write(ExportCSV.getString(7) + ',');
					bfw.write(ExportCSV.getString(8));
					bfw.newLine();
				} while (ExportCSV.moveToNext());
				bfw.flush();
				bfw.close();
				ExportCSV.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Toast.makeText(getActivity(), "匯出成功!檔案在" + CSVFile.getPath() + "/MoneyTime", Toast.LENGTH_LONG).show();
	}
	
	private void ChooseCVS() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
	    intent.setType("file/*");
	    startActivityForResult(intent, 5);
	}
	
	private void ImportCSV(Uri uri) {
		if(uri.getPath().contains("MoneyTime.csv")){
			FileReader ImportFile;
			BufferedReader FileReader;
			DH = new DBHelper(getActivity(), null, null, 0);
			try {
				ImportFile = new FileReader(uri.getPath());
				FileReader = new BufferedReader(ImportFile);
				String line = "";
				String[] Data;
				FileReader.readLine();
				while ((line = FileReader.readLine()) != null) {
					Data = line.split(",");	
					add(Integer.parseInt(Data[1]),
						Integer.parseInt(Data[2]),
						Integer.parseInt(Data[3]),
						Data[4],
						Data[5],
						Data[6],
						Integer.parseInt(Data[7]),	
						Integer.parseInt(Data[8]),
						"");
				}
				FileReader.close();
				Toast.makeText(getActivity(), "太棒了, 帳務匯入成功!", Toast.LENGTH_LONG).show();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			Toast.makeText(getActivity(), "失敗了...請確認是否有選擇MoenyTime.csv", Toast.LENGTH_LONG).show();
		}
	}
	
	private void add(
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
	
	
	
}
