package money.time;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

/**
 * This demonstrates how you can implement switching between the tabs of a
 * TabHost through fragments.  It uses a trick (see the code below) to allow
 * the tabs to switch between fragments instead of simple views.
 */
public class StartActivity extends FragmentActivity {
    private TabHost mTabHost;
    private TabManager mTabManager;
    private static SharedPreferences UserData;
    private static ConnectivityManager CheckNetworkStatus;
    private static boolean check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UserData = getSharedPreferences("UserData", 0);
        CheckNetworkStatus = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo WifiStatus = CheckNetworkStatus.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo MobileStatus = CheckNetworkStatus.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if(WifiStatus.isConnected()){
        	check = true;
        }else if(MobileStatus.isConnected()){
        	check = true;
        }else{
        	check = false;
        	SharedPreferences.Editor editor = UserData.edit();
			editor.remove("UserName");
			editor.remove("Email");
			editor.remove("UUID");
			editor.commit();
        }
        if(UserData.getString("UserName", "").isEmpty()){
        	//CellPhoneLoginRegistration();
        	creatTab();
        }
        else{
        	creatTab();
        	new ConnectServer(this).execute("CheckServerData");
        }

    }
    
   /* private void setupTab(Class<?> ccls, String name, String label, Integer iconId) {
    	TabHost mtestTabHost;
        TabManager mtestTabManager;
        mtestTabHost = (TabHost)findViewById(android.R.id.tabhost);
        mtestTabHost.setup();
       // mtestTabManager = new TabManager(this, mtestTabHost, R.id.realtabcontent);
        
		Intent intent = new Intent().setClass(this, ccls);
        View tab = LayoutInflater.from(this).inflate(R.layout.custom_tabwidget_layout, null);
        ImageView image = (ImageView) tab.findViewById(R.id.customicon);
        TextView text = (TextView) tab.findViewById(R.id.customtext);
        if(iconId != null){
        	image.setImageResource(iconId);
        	}
        text.setText(label);
        TabSpec spec = mtestTabHost.newTabSpec(name).setIndicator(tab).setContent(intent);
        mtestTabHost.addTab(spec);
    }*/
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode != RESULT_CANCELED) {
    		//super.onActivityResult(requestCode, resultCode, data); 
    		//android.support.v4.app.Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.realtabcontent);
            //fragment.onActivityResult(requestCode, resultCode, data);
            super.onActivityResult(requestCode, resultCode, data); 
    	}

    }
    
    /*public void CellPhoneLoginRegistration(){
		final Button Login,Sign;
		final EditText  UserName, Passsword;
		TextView UserNameView,PassswordView;
		AlertDialog.Builder LoginW = new AlertDialog.Builder(this); 
		LayoutInflater inflater;
		final View V;
		
		final AlertDialog alert = LoginW.create();
		inflater = LayoutInflater.from(this);
        V = inflater.inflate(R.layout.login_registration_layout,null);
        
        Login = (Button)V.findViewById(R.id.loginbutton);
        Sign = (Button)V.findViewById(R.id.signbutton);
        
        DisplayMetrics dm = new DisplayMetrics(); 
        getWindowManager().getDefaultDisplay().getMetrics(dm); //先取得螢幕解析度  
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
				new MoneyTimeAccount(getApplicationContext()
						,UserName.getText().toString()
						,Passsword.getText().toString(),"",alert,true).execute("doLog");
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
					new MoneyTimeAccount(getApplicationContext()
							,UserName.getText().toString()
							,Passsword.getText().toString(),Email.getText().toString(),alert,false).execute("doReg");
				}
			}
		});
        
        V.setBackgroundColor(Color.WHITE);
        alert.setTitle("MoneyTime登入");
		alert.setView(V);
		alert.show();
	}*/
    
    private void creatTab(){
    	 setContentView(R.layout.activity_start);
         mTabHost = (TabHost)findViewById(android.R.id.tabhost);
         mTabHost.setup();
         
         mTabManager = new TabManager(this, mTabHost, R.id.realtabcontent);

 		
         mTabManager.addTab(mTabHost.newTabSpec("AddFragment")
         		.setIndicator("",this.getResources().getDrawable(R.drawable.ic_add))
         		,AddFragment.class, null);
         mTabManager.addTab(mTabHost.newTabSpec("ListFragment")
         		.setIndicator("",this.getResources().getDrawable(R.drawable.ic_list))
         		,ListFragment.class, null);
         mTabManager.addTab(mTabHost.newTabSpec("ReceiptFragment")
         		.setIndicator("",this.getResources().getDrawable(R.drawable.ic_qr))
         		,ReceiptFragment.class, null);
         mTabManager.addTab(mTabHost.newTabSpec("SettingFragment")
         		.setIndicator("",this.getResources().getDrawable(R.drawable.ic_setting))
         		,SettingFragment.class, null);
         //setupTab(AddFragment.class,"AddFragment","AddFragment",R.drawable.ic_add);
         //setupTab(ListFragment.class,"ListFragment","ListFragment",R.drawable.ic_list);
         //setupTab(ReceiptFragment.class,"ReceiptFragment","ReceiptFragment",R.drawable.ic_pictable);
        // setupTab(Fragment4.class,"test","test",R.drawable.ic_setting);
         
         
         
         DisplayMetrics dm = new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(dm);
         int screenWidth = dm.widthPixels;
         int count = mTabHost.getTabWidget().getChildCount(); 
         for (int i = 0; i < count; i++) {
         	mTabHost.getTabWidget().getChildAt(i).getLayoutParams().width = screenWidth/count ;
         }
            
         /*DisplayMetrics dm = new DisplayMetrics();   
         getWindowManager().getDefaultDisplay().getMetrics(dm); //先取得螢幕解析度  
         int screenWidth = dm.widthPixels;   //取得螢幕的寬
            
            
         TabWidget tabWidget = mTabHost.getTabWidget();   //取得tab的物件
         int count = tabWidget.getChildCount();   //取得tab的分頁有幾個
         if (count > 3) {   //如果超過三個就來處理滑動
             for (int i = 0; i < count; i++) {   
                 tabWidget.getChildTabViewAt(i).setMinimumWidth((screenWidth) / 3);//設定每一個分頁最小的寬度   
             }   
         }*/
    }
    
    
}
