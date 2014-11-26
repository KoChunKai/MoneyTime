package money.time;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ReceiptFragment extends Fragment {
	
	private TextView Code,Number;
	private ListView RecFunction;
	private ImageView BarCode;
	private HttpClient ReceipthttpClient;
	private HttpResponse responsePOST = null;
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
		return inflater.inflate(R.layout.receipt_layout, container, false);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		Code = (TextView)getView().findViewById(R.id.receipt_date);
		Number = (TextView)getView().findViewById(R.id.receipt_number);
		RecFunction = (ListView)getView().findViewById(R.id.receipt_detail);
		BarCode = (ImageView)getView().findViewById(R.id.barcodeimage);
		UserData = getActivity().getSharedPreferences("UserData", 0);
		CheckBarCode(BarCode);
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");	
		Number.setText(df.format(c.getTime()));
		Code.setText("���n�J");
		RecFunction.setAdapter(SetFunction());
		RecFunction.setOnItemClickListener(Lisenter);
	}


	private ListAdapter SetFunction() {
		// TODO Auto-generated method stub
		ArrayList<String> texttest = new ArrayList<String>();
		texttest.add("������X�n�J");
		texttest.add("�U���������X");
		texttest.add("�d�ߵo��");
		ArrayAdapter adapter = new ArrayAdapter(getActivity(),R.layout.list_center,R.id.fuctiontext,texttest);
		return adapter;
	}
	
	private void CheckBarCode(ImageView BarCodeView){
		File CheckPath = Environment.getExternalStorageDirectory(); // ���| /sdcard/
		File CheckFile = new File(CheckPath,"/CPBarCode.png");
		if(CheckFile.exists()){
			try {
				FileInputStream streamIn;
				streamIn = new FileInputStream(CheckFile);
				Bitmap bitmap = BitmapFactory.decodeStream(streamIn); //This gets the image	
				int width = bitmap.getWidth();
			    int height = bitmap.getHeight();
			    float scaleWidth = ((float) 700) / width;
			    float scaleHeight = ((float) 200) / height;
			    Matrix matrix = new Matrix();
			    matrix.postScale(scaleWidth, scaleHeight);
			    Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
				BarCodeView.setImageBitmap(resizedBitmap);
				streamIn.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private ListView.OnItemClickListener Lisenter = new ListView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			switch(position){
			case 0:
				ReceipthttpClient = MySSLSocketFactory.createMyHttpClient();
				CellPhoneLoginRegistration();
				break;
			case 1:
				TerminallyNumber();
				break;
			case 2:
				if(!Code.getText().toString().contains("���n�J")){
					ChooseDateSearchReceipt();
				}else{
					Toast.makeText(getActivity(), "�A�٨S�n�J�O!!!" , Toast.LENGTH_LONG).show();
				}
				break;
			}
		}
	};
	
	private void ChooseDateSearchReceipt(){
		AlertDialog.Builder ChooseDate = new AlertDialog.Builder(getActivity());  
		ChooseDate.setTitle("�п�ܵo�����O");
		ChooseDate.setAdapter(SetDate(),SearchReceipt);
		ChooseDate.show();
		/*AlertDialog.Builder rrrr = new AlertDialog.Builder(getActivity());
		rrrr.setMessage("OOPS....�t���٦b������");
		rrrr.show();*/
	}
	
	public ListAdapter SetDate(){
		ArrayList<String> texttest = new ArrayList<String>();
		Calendar c = Calendar.getInstance();
		SimpleDateFormat DateOFYear = new SimpleDateFormat("yyyy");
		SimpleDateFormat DateOFMonth = new SimpleDateFormat("MM");
		for(int step=0;step<=6;step++){
			int year = Integer.parseInt(DateOFYear.format(c.getTime())) - 1911;
			int month = Integer.parseInt(DateOFMonth.format(c.getTime()));
			texttest.add(year+"�~"+month+"��");
			c.add(Calendar.MONTH, -1);
		}
		ArrayAdapter adapter = new ArrayAdapter(getActivity(),R.layout.list_center,R.id.fuctiontext,texttest);
		return adapter;
	}
	
	private DialogInterface.OnClickListener SearchReceipt = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			Calendar c = Calendar.getInstance();
			SimpleDateFormat Date = new SimpleDateFormat("yyyy/MM");
			c.add(Calendar.MONTH, -which);
			GetReceiptData getdata = new GetReceiptData(
					UserData.getString("CellPhone", ""),
					UserData.getString("CheckNum", ""),
					Date.format(c.getTime()),getActivity(),ReceipthttpClient,responsePOST);
			getdata.execute("");
		}
	};
	
	public void CellPhoneLoginRegistration(){
		Button Login;
		final EditText  Cellphone, CheckNum, ImgNum;
		ImageView ImgView;
		AlertDialog.Builder LoginW = new AlertDialog.Builder(getActivity()); 
		LayoutInflater inflater;
		View V;
		
		final AlertDialog alert = LoginW.create();
		inflater = LayoutInflater.from(getActivity());
        V = inflater.inflate(R.layout.login_registration_layout,null);
        
        ImgView = (ImageView) V.findViewById(R.id.imagecheck);
        Login = (Button)V.findViewById(R.id.loginbutton);
        Cellphone = (EditText)V.findViewById(R.id.cellphoneeditText);
        CheckNum = (EditText)V.findViewById(R.id.checknumeditText);
        ImgNum = (EditText)V.findViewById(R.id.imgnumeditText);
        
        DisplayMetrics dm = new DisplayMetrics(); 
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm); //�����o�ù��ѪR��  
        int screenWidth = dm.widthPixels;
        Login.setWidth(screenWidth);
        V.findViewById(R.id.signbutton).setVisibility(View.GONE);
        Cellphone.setText(UserData.getString("CellPhone", ""));
        CheckNum.setText(UserData.getString("CheckNum", ""));
        
        new DownloadImageTask(ImgView).execute("https://www.einvoice.nat.gov.tw/APMEMBERVAN/PublicAudit/PublicAudit!generateImageCode");
        
        Login.setOnClickListener(new Button.OnClickListener(){ 
			@Override
			public void onClick(View v) {
				new LoginReceipt(Cellphone.getText().toString(),
						CheckNum.getText().toString(),
						ImgNum.getText().toString()).execute();
				alert.cancel();
			}
		});
        
        V.setBackgroundColor(Color.WHITE);
        alert.setTitle("������X�n�J");
		alert.setView(V);
		alert.show();
	}
	
	public class LoginReceipt extends AsyncTask<String, Integer, String> 
    {
		String cp,cn,in;
		private ProgressDialog dialog = null;
		public LoginReceipt(String cp,String cn, String in){
			this.cp = cp;
			this.cn = cn;
			this.in = in;
		}
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
			dialog.setMessage("�n�J���A�еy��....");
			dialog.show();
			
            super.onPreExecute();  
        }
		
		protected String doInBackground(String... urls) {
			String WebData;
			TagNode tagNode;
			 try {
				 WebData = PostLogin(cp,cn,in);
				 tagNode = new HtmlCleaner().clean(WebData);
				 TagNode[] ErrorMsg = tagNode.getElementsByAttValue("class",
							"ErrorMsg", true, true);
				 String Message = ErrorMsg[0].getText().toString();
				 if(Message.contains("�ϧΤW���K�X")){
					 return "�n�J���ѡA�ϧνX���~!!";
				 }
				 else if(Message.contains("��������ҽX���~")){
					 return "�n�J���ѡA��������ҽX���~!!";
				 }
				 else{
					 TagNode[] formdetail = tagNode.getElementsByAttValue("class",
								"formdetail", true, true);
					 String Deatil = formdetail[0].getText().toString().replaceAll(" ", "");
					 SharedPreferences.Editor editor = UserData.edit();
					 editor.putString("BarCode", Deatil.substring(21, Deatil.indexOf(",Email")));
					 editor.commit();
					 Bitmap BarCodeIMG = GetImg("http://www.barcodesinc.com/generator/image.php?code=" +
							 Deatil.substring(21, Deatil.indexOf(",Email")) +
					 		"&style=196&type=C128B&width=150&height=50&xres=1&font=1");
					 File CheckPath = Environment.getExternalStorageDirectory();
					 File CheckFile = new File(CheckPath,"/CPBarCode.png");
					 if(CheckFile.exists()){
						 CheckFile.delete();
					 }
					 try {
				           FileOutputStream out = new FileOutputStream(CheckFile);
				           BarCodeIMG.compress(Bitmap.CompressFormat.PNG, 0, out);
				           out.flush();
				           out.close();
				    } catch (Exception e) {
				           e.printStackTrace();
				    }
					 return "�n�J���\!!!!";
				 }
	         } catch (IOException e) {
	        	 e.printStackTrace();
	        	 return "error";
	         }
        }
		
		@Override
		protected void onPostExecute(final String result) {
			dialog.dismiss();
			SharedPreferences.Editor editor = UserData.edit();
			editor.putString("CellPhone", cp);
			editor.putString("CheckNum", cn);
			editor.commit();
			Toast.makeText(getActivity(), result , Toast.LENGTH_LONG).show();
			if(result.contains("���\")){
				Code.setText(UserData.getString("BarCode", "���n�J"));
				CheckBarCode(BarCode);
			}
	    }
    }
		
	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
	    ImageView bmImage;
	    
	    public DownloadImageTask(ImageView bmImage) {
	        this.bmImage = bmImage;
	    }

	    protected Bitmap doInBackground(String... urls) {
	    	try {
                return GetImg(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
	    }

	    protected void onPostExecute(Bitmap result) {
	        bmImage.setImageBitmap(result); 
	    }
	}
	
	private void TerminallyNumber() {
		AlertDialog.Builder ShowTerminally = new AlertDialog.Builder(getActivity());             
		ShowTerminally.setAdapter(GetTerminally(),SDListener);
		ShowTerminally.show();
	}
	
	public ListAdapter GetTerminally(){
		ArrayList<String> texttest = new ArrayList<String>();
		texttest.add("102�~07-08��");
		texttest.add("102�~05-06��");
		texttest.add("102�~03-04��");
		texttest.add("102�~01-02��");
		ArrayAdapter adapter = new ArrayAdapter(getActivity(),R.layout.list_center,R.id.fuctiontext,texttest);
		return adapter;
	}
	
	private DialogInterface.OnClickListener SDListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			String GetUrl,invTerm = null;
			String UUIDValue = android.provider.Settings.System.getString(getActivity().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
			switch(which){
			case 0:
				invTerm = "10208";
				break;
			case 1:
				invTerm = "10206";
				break;
			case 2:
				invTerm = "10204";
				break;
			case 3:
				invTerm = "10202";
				break;
			}
			GetUrl = "https://www.einvoice.nat.gov.tw" +
					"/PB2CAPIVAN/invapp/InvApp?version=0.2&action=QryWinningList" +
					"&invTerm=" +
					invTerm +
					"&appID=EINV3201306053713" +
					"&UUID=" +
					UUIDValue;
			new MyAsyncTask().execute(GetUrl);
		}
	};
	
	public class MyAsyncTask extends AsyncTask<String, Integer, String> 
    {
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
			dialog.setMessage("���Ū�����A�еy��....");
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
		
		@Override  // �DUI������N�եγo�Ӥ�k�Ӧb�e���W��ܭI�����Ȫ��i�i���p�A�Ҧp�q�L�@�Ӷi�ױ��i����ܡC
        protected void onProgressUpdate(final Integer... progress) {  
            super.onProgressUpdate(progress);  
        }  
		
		@Override
		protected void onPostExecute(final String result) {
			//doInBackground�������槹��Ĳ�o  
	        //�o�̪�result�N�O�W��doInBackground����᪺��^�ȡA�ҥH�o�̬Ourls[0]���� 
			dialog.dismiss();
	        super.onPostExecute(result);  
	        AlertDialog.Builder NumberW;
			LayoutInflater inflater;
			View V;
			TextView SupText,SpeText,FirstText,SixText;
			NumberW = new AlertDialog.Builder(getActivity());
			inflater = LayoutInflater.from(getActivity());
	        V = inflater.inflate(R.layout.terminalnumber_layout,null);
	        SupText = (TextView)V.findViewById(R.id.supertext);
	        SpeText = (TextView)V.findViewById(R.id.spetext);
	        FirstText = (TextView)V.findViewById(R.id.firsttext);
	        SixText = (TextView)V.findViewById(R.id.sixtext);
	        try {
				JSONObject JsonData = new JSONObject(result);
				SupText.setText(JsonData.getString("superPrizeNo").toString());
				SpeText.setText(JsonData.getString("spcPrizeNo").toString());
				FirstText.setText(JsonData.getString("firstPrizeNo1").toString()
						+ "\n" + JsonData.getString("firstPrizeNo2").toString()
						+ "\n" + JsonData.getString("firstPrizeNo3").toString());
				SixText.setText(JsonData.getString("sixthPrizeNo1").toString());
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
	        V.setBackgroundColor(Color.WHITE);
	        NumberW.setView(V);
	        NumberW.show();
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
	
	private Bitmap GetImg(String mUrl) throws IOException {
	    HttpGet httpGet = new HttpGet(mUrl);
	    HttpResponse httpResponse = ReceipthttpClient.execute(httpGet);
	    HttpEntity httpEntity = httpResponse.getEntity();
	    if (httpEntity != null) {
	        InputStream instream = httpEntity.getContent();
	        return BitmapFactory.decodeStream(instream);
	    }
	    return null;
	}
	
	private String PostLogin(String cp,String cn,String in) throws IOException{
		String mUrl = "https://www.einvoice.nat.gov.tw/APMEMBERVAN/GeneralCarrier/generalCarrier!maintain";
		StringBuilder sb = new StringBuilder();
		HttpPost post = new HttpPost(mUrl);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("mobile", cp));
		params.add(new BasicNameValuePair("verifyCode", cn));
		params.add(new BasicNameValuePair("imageCode", in));
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
