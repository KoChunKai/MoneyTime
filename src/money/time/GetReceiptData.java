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
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.ListAdapter;

public class GetReceiptData extends AsyncTask<String, Integer, String> {
	private String cp,cn,date;
	private Context context;
	private HttpClient ReceipthttpClient;
	private HttpResponse responsePOST;
	private ProgressDialog dialog = null;
	private Custom CData;
	private ArrayList<Custom> AData;
	private String[] data = null;
	
	public GetReceiptData(
			String cp,
			String cn,
			String date,
			Context context,
			HttpClient ReceipthttpClient,
			HttpResponse responsePOST){
		this.cp = cp;
		this.cn = cn;
		this.date = date;
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
	
	protected String doInBackground(String... urls) {
		String ReceiptList = null;
		try {
			ReceiptList = GetReceiptList(cp,cn,date);
			//return publicCarrierId;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ReceiptList;
    }
	
	@Override
	protected void onPostExecute(final String result) {
		dialog.dismiss();
		AlertDialog.Builder rrrr = new AlertDialog.Builder(context);
		if(result.contains("沒有消費紀錄")){
			rrrr.setMessage("沒有消費紀錄!!");
		}else{
			data = result.split(",");
			rrrr.setAdapter(SetDate(data[1],data[0],data[2]),SearchReceipt);
		}
		rrrr.show();
    }
	
	public ListAdapter SetDate(String d1,String d2,String d3){
		AData = new ArrayList<Custom>();
		CData = new Custom("消費日期","發票號碼","金額");
		AData.add(CData);
		CData = new Custom(d1,d2,d3);
		AData.add(CData);
		ReceiptListAdapter adapter = new ReceiptListAdapter(context,R.layout.recepit_list_layout,AData);
		return adapter;
	}
	
	private DialogInterface.OnClickListener SearchReceipt = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			GetReceiptDetial GRD;
			GRD = new GetReceiptDetial(context,data,cp,cn,ReceipthttpClient,responsePOST);
			GRD.execute("");
			//Toast.makeText(context, "系統開發中:" + AData.get(which).getcustomBig() + AData.get(which).getcustomSmall() + AData.get(which).getdateString(), Toast.LENGTH_LONG).show();
		}
	};
	
	private String GetReceiptList(String cp,String cn,String date) throws IOException{
		String mUrl = "https://www.einvoice.nat.gov.tw/APMEMBERVAN/GeneralCarrier/QueryInv!queryInvoiceList";
		StringBuilder sb = new StringBuilder();
		HttpPost post = new HttpPost(mUrl);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("checkCode",	""));
		params.add(new BasicNameValuePair("mobile",	cp));
		params.add(new BasicNameValuePair("publicCarrierId","450023"));
		params.add(new BasicNameValuePair("verifyCode",cn));
		params.add(new BasicNameValuePair("invNum",""));
		params.add(new BasicNameValuePair("invDate",""));
		params.add(new BasicNameValuePair("totalAmount",""));
		params.add(new BasicNameValuePair("sellerName",""));
		params.add(new BasicNameValuePair("sellerPersonInCharge",""));
		params.add(new BasicNameValuePair("isDonate",""));
		params.add(new BasicNameValuePair("donateName",""));
		params.add(new BasicNameValuePair("isHeart",""));
		params.add(new BasicNameValuePair("heartBan",""));
		params.add(new BasicNameValuePair("mainRemark",""));
		params.add(new BasicNameValuePair("cardCodeForSelect",""));
		params.add(new BasicNameValuePair("carrierId2ForSelect",""));
		params.add(new BasicNameValuePair("queryInvDate",date));
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
			if(!ErrorMsg[0].getText().toString().contains("無符合條件資料")){
				String ReceiptTitle = null;
				Object[] test = null;
				try {
					test = tagNode.evaluateXPath("//table[@id='invoiceTable']//a[@href]");
					TagNode testnode = (TagNode) test[0];
					ReceiptTitle = testnode.getAttributeByName("href").toString();
					return ReceiptTitle.replaceAll("javascript:queryDetail", "").replaceAll(";", "").replaceAll("'", "").replaceAll("[()]","");
				} catch (XPatherException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else{
				return "沒有消費紀錄!!";
			}
        }
        return null;
	}
}