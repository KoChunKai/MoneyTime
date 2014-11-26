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
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class GetReceiptDetial extends AsyncTask<String, Integer, Object[]> {
	
	private String cp,cn;
	private Context context;
	private HttpClient ReceipthttpClient;
	private HttpResponse responsePOST;
	private ProgressDialog dialog = null;
	private Custom CData;
	private ArrayList<Custom> AData;
	private String[] data = null;
	
	public GetReceiptDetial(
			Context context,
			String[] data,
			String cp,
			String cn,
			HttpClient ReceipthttpClient,
			HttpResponse responsePOST){
		this.data = data;
		this.cp = cp;
		this.cn = cn;
		this.context = context;
		this.ReceipthttpClient = ReceipthttpClient;
		this.responsePOST = responsePOST;
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
		dialog.setMessage("資料讀取中，請稍候....");
		dialog.show();
        super.onPreExecute();  
    }
	
	protected Object[] doInBackground(String... urls) {
		Object[] ReceiptList = null;
		try {
			ReceiptList = GetReceiptDetial(cp,cn,data);
			//return publicCarrierId;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ReceiptList;
    }
	
	@Override
	protected void onPostExecute(final Object[] result) {
		//for(int i = 0 ;i<result.length;i++)
		//TagNode testnode = (TagNode) result[0];
		dialog.dismiss();
		AlertDialog.Builder ReceiptW = new AlertDialog.Builder(context);
		LayoutInflater inflater;
		View V;
		TextView invNum,invDate;
		ListView Detail;
		ArrayAdapter<String> adapter;
		inflater = LayoutInflater.from(context);
        V = inflater.inflate(R.layout.receipt_layout,null);
        invNum = (TextView)V.findViewById(R.id.receipt_number);
        invDate = (TextView)V.findViewById(R.id.receipt_date);
        Detail = (ListView)V.findViewById(R.id.receipt_detail);
        adapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1);
        adapter.clear();
        invNum.setText(data[0]);
		invDate.setText(data[1]);
		int ObjectLength = 0;
		
		for(Object size : result){
			ObjectLength++;
		}	
		for(int i=0;i<ObjectLength;i++){
			TagNode name = (TagNode) result[i];
			TagNode aumont = (TagNode) result[++i];
			++i;
			TagNode money = (TagNode) result[++i];
			adapter.add(htmlUnicodeToJavaUnicode(name.getText().toString()) + "\t*" + aumont.getText().toString() + "\t" +(int)Double.parseDouble((money.getText().toString()))+"元");
		}
		adapter.add("合計：\t"+ data[2]);
		Detail.setAdapter(adapter);
		ReceiptW.setPositiveButton("儲存",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						int Objectsize = 0;
						for(Object size : result){
							Objectsize++;
						}	
						for(int size = 0;size < Objectsize;size++){
							TagNode name = (TagNode) result[size];
							++size;++size;
							TagNode money = (TagNode) result[++size];
							add(
								Integer.parseInt(data[1].substring(0, 3)) + 1911
								,Integer.parseInt(data[1].substring(4,6))
								,Integer.parseInt(data[1].substring(7,9))
								,"",""
								,htmlUnicodeToJavaUnicode(name.getText().toString()).toString()
								,0
								,(int)Double.parseDouble((money.getText().toString()))
								,data[0]
								);
						}
						Toast.makeText(context, "儲存成功!", Toast.LENGTH_LONG).show();
					}
				});
        ReceiptW.setNeutralButton("返回",null);
		V.setBackgroundColor(Color.WHITE);
		ReceiptW.setView(V);
		ReceiptW.show();
    }
	
	private Object[] GetReceiptDetial(String cp,String cn,String[] date) throws IOException{
		String mUrl = "https://www.einvoice.nat.gov.tw/APMEMBERVAN/GeneralCarrier/QueryInv!queryInvDetail";
		StringBuilder sb = new StringBuilder();
		HttpPost post = new HttpPost(mUrl);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		String ADdate = (Integer.parseInt(date[1].substring(0, 3)) + 1911) + date[1].substring(3,6);
		params.add(new BasicNameValuePair("checkCode",""));
		params.add(new BasicNameValuePair("mobile",cp));
		params.add(new BasicNameValuePair("publicCarrierId","450023"));
		params.add(new BasicNameValuePair("verifyCode",cn));
		params.add(new BasicNameValuePair("invNum",date[0]));
		params.add(new BasicNameValuePair("invDate",date[1]));
		params.add(new BasicNameValuePair("totalAmount",date[2]));
		params.add(new BasicNameValuePair("sellerName",date[3]));
		params.add(new BasicNameValuePair("sellerPersonInCharge",""));
		params.add(new BasicNameValuePair("isDonate","0"));
		params.add(new BasicNameValuePair("donateName",""));
		params.add(new BasicNameValuePair("isHeart",""));
		params.add(new BasicNameValuePair("heartBan",""));
		params.add(new BasicNameValuePair("mainRemark",""));
		params.add(new BasicNameValuePair("cardCodeForSelect",date[7]));
		params.add(new BasicNameValuePair("carrierId2ForSelect",date[8]));
		params.add(new BasicNameValuePair("queryInvDate",ADdate));
		params.add(new BasicNameValuePair("queryDate",""));
		params.add(new BasicNameValuePair("carrier","all"));
		params.add(new BasicNameValuePair("invStatus","1"));
		params.add(new BasicNameValuePair("queryBuyerBan",""));
		params.add(new BasicNameValuePair("banOrPC","輸入統編或愛心碼"));
		params.add(new BasicNameValuePair("ddlDonateUnit",""));
		params.add(new BasicNameValuePair("CSRT","1263953591699300171"));
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
            instream.close();
            String WebData = sb.toString();
            HtmlCleaner cleaner = new HtmlCleaner();
            CleanerProperties props = cleaner.getProperties();
            props.setAllowHtmlInsideAttributes(true);
            props.setAllowMultiWordAttributes(true);
            props.setRecognizeUnicodeChars(true);
            props.setOmitComments(true);
			TagNode tagNode = cleaner.clean(WebData);
			props.setAllowHtmlInsideAttributes(false);
			TagNode[] ErrorMsg = tagNode.getElementsByAttValue("class",
							"lpTb tablesorter", true, true);
			StringBuffer ReceiptTitle = null;
			Object[] test = null;
			try {
				test = tagNode.evaluateXPath("//table[@id='invoiceDetailTable']//*//tr//td");
				TagNode testnode = (TagNode) test[0];
				ReceiptTitle = htmlUnicodeToJavaUnicode(testnode.getText().toString());
				//return ReceiptTitle.toString();
				return test;
			} catch (XPatherException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		DBHelper DH = new DBHelper(context, null, null, 0);
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
