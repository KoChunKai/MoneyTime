package money.time;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class ListFragment extends Fragment {
	
	private ListView LV;
    private View V;
    private DBHelper DH = null;
    private ArrayList<Custom> ListDate = new ArrayList<Custom>();
    private SQLiteDatabase db;
    private String SelectConditional = null;
    private Button ShowReport;
    private static CustomAdapter adapter;
    private static SharedPreferences UserData;

	@Override
	public void onResume() {
		// 刷新ListView
		super.onResume();
		adapter.clear();
		LV.setAdapter(GetAdapter());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		UserData = getActivity().getSharedPreferences("UserData", 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		V = inflater.inflate(R.layout.lsit_layout, container, false);
		LV = (ListView)V.findViewById(R.id.listView);
		ShowReport = (Button)V.findViewById(R.id.showreportbtn);
		ShowReport.setOnClickListener(BtnLisenter);
		openDB();
		LV.setAdapter(GetAdapter());
        return V;
	}
	
	private Button.OnClickListener BtnLisenter = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent mIntent = new Intent(getActivity(),LineReport.class); //Same as above two lines
		    mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    getActivity().startActivity(mIntent);
			//Toast.makeText(getActivity(), "系統開發中", Toast.LENGTH_LONG).show();
		}
	};


	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		LV.setOnItemClickListener(Lisenter);
	}
	private void openDB() {
		// TODO Auto-generated method stub
    	DH = new DBHelper(getActivity(), null, null, 0);
	}
	
	public ListAdapter GetAdapter(){
		Custom Date;
		db = DH.getReadableDatabase();
		String DateString = null;
		String MoneyConditional = 
				"SELECT _DateYear,_DateMonth,_DateDay,SUM(_InCome),SUM(_OutGo) FROM MTDB " +
				"GROUP BY _DateDay,_DateMonth,_DateYear " +
				"ORDER BY _DateMonth ASC, _DateDay ASC";
		Cursor Money = db.rawQuery(MoneyConditional, null);
		if (Money.getCount() > 0){               
			Money.moveToLast();
		    do {
		    	DateString = Money.getString(Money.getColumnIndex("_DateMonth")) + "∕"
						+ Money.getString(Money.getColumnIndex("_DateDay"));
		    	Date = new Custom(
		    			 "支出: " + Money.getString(Money.getColumnIndex("SUM(_OutGo)")) + "元"
		    			,"收入: " + Money.getString(Money.getColumnIndex("SUM(_InCome)")) + "元"
		    			,DateString);
		    	ListDate.add(Date);
		    } while (Money.moveToPrevious());
		    Money.close();
		}
			
		adapter = new CustomAdapter(getActivity(),
                R.layout.list_view_setting,
                ListDate);
		return adapter;
	}
	
	
	private ListView.OnItemClickListener Lisenter = new ListView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			db = DH.getReadableDatabase();
			switch(ListDate.get(arg2).getdateString().replace("∕", "").length()){
			case 2:
				SelectConditional = "SELECT * FROM MTDB WHERE _DateMonth = "
						+ ListDate.get(arg2).getdateString().replace("∕", "").charAt(0)
						+ " AND _DateDay = "
						+ ListDate.get(arg2).getdateString().replace("∕", "").charAt(1);
				break;
			case 3:
				if(ListDate.get(arg2).getdateString().charAt(1) == '∕'){
					SelectConditional = "SELECT * FROM MTDB WHERE _DateMonth = "
							+ ListDate.get(arg2).getdateString().replace("∕", "").charAt(0)
							+ " AND _DateDay = "
							+ ListDate.get(arg2).getdateString().replace("∕", "").charAt(1)
							+ ListDate.get(arg2).getdateString().replace("∕", "").charAt(2);
					break;
				}
				else{
					SelectConditional = "SELECT * FROM MTDB WHERE _DateMonth = "
							+ ListDate.get(arg2).getdateString().replace("∕", "").charAt(0)
							+ ListDate.get(arg2).getdateString().replace("∕", "").charAt(1)
							+ " AND _DateDay = "
							+ ListDate.get(arg2).getdateString().replace("∕", "").charAt(2);
					break;
				}
			case 4:
				SelectConditional = "SELECT * FROM MTDB WHERE _DateMonth = "
						+ ListDate.get(arg2).getdateString().replace("∕", "").charAt(0)
						+ ListDate.get(arg2).getdateString().replace("∕", "").charAt(1)
						+ " AND _DateDay = "
						+ ListDate.get(arg2).getdateString().replace("∕", "").charAt(2)
						+ ListDate.get(arg2).getdateString().replace("∕", "").charAt(3);
				break;
			}
			Cursor SelectData = db.rawQuery(SelectConditional, null);
			AlertDialog.Builder ShowDetails = new AlertDialog.Builder(getActivity());             
			SelectData.moveToLast();
			ShowDetails.setAdapter(GetShowDetails(SelectConditional),SDListener);
			ShowDetails.setTitle(
					SelectData.getString(SelectData.getColumnIndex("_DateYear")) + "∕"
					+ SelectData.getString(SelectData.getColumnIndex("_DateMonth")) + "∕"
					+ SelectData.getString(SelectData.getColumnIndex("_DateDay")));
			ShowDetails.show();
		}
	};
	
	private DialogInterface.OnClickListener SDListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			db = DH.getWritableDatabase();
			final ContentValues UpData = new ContentValues();
			final Cursor SelectData = db.rawQuery(SelectConditional, null);
			SelectData.moveToPosition(which);
			final String[] Id = new String[] {String.valueOf(SelectData.getString(SelectData.getColumnIndex("_Id")))};
			final String[] SerId = new String[] {String.valueOf(SelectData.getString(SelectData.getColumnIndex("_SerId")))};
			//Toast.makeText(getActivity(), SerId[0], Toast.LENGTH_LONG).show();
			AlertDialog.Builder Choose = new AlertDialog.Builder(getActivity());
			Choose.setPositiveButton("修改", 
	        		new DialogInterface.OnClickListener() {
	        			@Override
	        			public void onClick(DialogInterface dialog, int which) {
	        				if(SelectData.getString(SelectData.getColumnIndex("_OutGo")).matches("0")){
	        					EditData(
	        							UpData
	        							,db
	        							,Id
	        							,SerId
	        							,Integer.parseInt(SelectData.getString(SelectData.getColumnIndex("_DateYear")))
	        							,Integer.parseInt(SelectData.getString(SelectData.getColumnIndex("_DateMonth")))
	        							,Integer.parseInt(SelectData.getString(SelectData.getColumnIndex("_DateDay")))
	        							,SelectData.getString(SelectData.getColumnIndex("_Category"))
	        							,SelectData.getString(SelectData.getColumnIndex("_ChildCategory"))
	        							,SelectData.getString(SelectData.getColumnIndex("_Note"))
	        							,SelectData.getString(SelectData.getColumnIndex("_InCome")));
	        				}
	        				else{
	        					EditData(
	        							UpData
	        							,db
	        							,Id
	        							,SerId
	        							,Integer.parseInt(SelectData.getString(SelectData.getColumnIndex("_DateYear")))
	        							,Integer.parseInt(SelectData.getString(SelectData.getColumnIndex("_DateMonth")))
	        							,Integer.parseInt(SelectData.getString(SelectData.getColumnIndex("_DateDay")))
	        							,SelectData.getString(SelectData.getColumnIndex("_Category"))
	        							,SelectData.getString(SelectData.getColumnIndex("_ChildCategory"))
	        							,SelectData.getString(SelectData.getColumnIndex("_Note"))
	        							,SelectData.getString(SelectData.getColumnIndex("_OutGo")));
	        				}
	        			}
	        		});
			Choose.setNeutralButton("刪除",
					new DialogInterface.OnClickListener() {
    					@Override
    					public void onClick(DialogInterface dialog, int which) {
    						db.delete("MTDB", "_Id=?", Id);
    						if(UserData.getString("UserName", "").isEmpty()){
	        					DBHelper SyncDB = new DBHelper(getActivity(), null, null, 0);
	        					SQLiteDatabase SyncTB = DH.getWritableDatabase();
	        			    	ContentValues values = new ContentValues();
	        			    	values.put("_SerId", SerId[0]);
	        			    	values.put("_Type", 1);
	        			    	db.insert("SYNCDB", null, values);
	        				}else{
	        					new ConnectServer(
		        						getActivity(),
		        						SerId).execute("del");
	        				}
    						onResume();
    					}
    				});
			Choose.setNegativeButton("返回",null);
			Choose.show();
		}
	};
	
	public ListAdapter GetShowDetails(String Conditional){
		Custom CData;
		ArrayList<Custom> AData = new ArrayList<Custom>();
		CustomAdapter ShowDetailsAdapter;
		db = DH.getReadableDatabase();
		Cursor Data = db.rawQuery(Conditional, null);
		if (Data.getCount() > 0) 
		{               
			Data.moveToFirst();
		    do {
		    	if(Data.getString(Data.getColumnIndex("_OutGo")).matches("0")){
		    		CData = new Custom(
			    			  Data.getString(Data.getColumnIndex("_Category")) + "\t"
							+ Data.getString(Data.getColumnIndex("_ChildCategory")) + "\t"
							+ Data.getString(Data.getColumnIndex("_Note")) + "\t"
			    			, "收入\t" 
							+ Data.getString(Data.getColumnIndex("_InCome")) + "\t"
			    			+ "元","");
		    	}
		    	else{
		    		CData = new Custom(
			    			  Data.getString(Data.getColumnIndex("_Category")) + "\t"
							+ Data.getString(Data.getColumnIndex("_ChildCategory")) + "\t"
							+ Data.getString(Data.getColumnIndex("_Note")) + "\t"
			    			, "支出\t" 
							+ Data.getString(Data.getColumnIndex("_OutGo")) + "\t"
			    			+ "元","");
		    	}
		    	AData.add(CData);
		    } while (Data.moveToNext());
		    Data.close();
		}
		
		ShowDetailsAdapter = new CustomAdapter(getActivity(),
                R.layout.details_layout,
                AData);
		return ShowDetailsAdapter;
	}
	
	@SuppressLint("SimpleDateFormat")
	public void EditData(final ContentValues upData
			, final SQLiteDatabase db2
			, final String[] Id
			, final String[] SerId
			, final int Year
			, final int Month
			, final int Day
			, final String Category
			, final String ChildCategory
			, final String Note
			, final String Money){
				
		ImageButton DateAdd,DateSub;
		final Button Save;
		final EditText  CategoryEdit, ChidCategoryEdit, NoteEdit, MoneyEdit;
		final RadioGroup rdg;
		final TextView Date;
		final Calendar c;
		final SimpleDateFormat df,date;
		final AlertDialog.Builder EditW;
		LayoutInflater inflater;
		View V;
		
		EditW = new AlertDialog.Builder(getActivity());
		final AlertDialog alert = EditW.create();
		inflater = LayoutInflater.from(getActivity());
        V = inflater.inflate(R.layout.add_layout,null);
        
        DateAdd = (ImageButton)V.findViewById(R.id.dateadd);
		DateSub = (ImageButton)V.findViewById(R.id.datesub);
		Save = (Button)V.findViewById(R.id.savebutton);
		CategoryEdit = (EditText)V.findViewById(R.id.editText1);
		ChidCategoryEdit = (EditText)V.findViewById(R.id.editText2);
		NoteEdit = (EditText)V.findViewById(R.id.editText3);
		MoneyEdit = (EditText)V.findViewById(R.id.editText4);
		rdg=(RadioGroup)V.findViewById(R.id.rdg1);
		
		CategoryEdit.setText(Category);
		ChidCategoryEdit.setText(ChildCategory);
		NoteEdit.setText(Note);
		MoneyEdit.setText(Money);
		
		V.findViewById(R.id.CategoryPlus).setVisibility(View.GONE);//隱藏+
		V.findViewById(R.id.ImageButton01).setVisibility(View.GONE);//隱藏+
		V.findViewById(R.id.ImageButton02).setVisibility(View.GONE);//隱藏+
		V.findViewById(R.id.qrbutton).setVisibility(View.GONE);//隱藏+
		V.findViewById(R.id.accountbuttonn).setVisibility(View.GONE);

        c = Calendar.getInstance();
        c.set(Year, Month-1, Day);
        
		df = new SimpleDateFormat("yyyy/MM/dd");
		date = new SimpleDateFormat("yyyyMMdd");
		Date = (TextView) V.findViewById(R.id.dateview);
		Date.setText(df.format(c.getTime()));
		
		
		DateAdd.setOnClickListener(new Button.OnClickListener(){ 
			@Override
			public void onClick(View v) {
				c.add(Calendar.DAY_OF_WEEK, 1);
				Date.setText(df.format(c.getTime()));
			}
		});
		DateSub.setOnClickListener(new Button.OnClickListener(){ 
			@Override
			public void onClick(View v) {
				c.add(Calendar.DAY_OF_WEEK, -1);
				Date.setText(df.format(c.getTime()));
			}
		});
		rdg.check(0);
		rdg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
		    public void onCheckedChanged(RadioGroup group, final int checkedId) {
		        switch (checkedId) {
		            case R.id.rdo1:
		            	Save.setOnClickListener(new Button.OnClickListener(){ 
		        			@Override
		        			public void onClick(View v) {
		        				upData.put("_DateYear", Integer.parseInt(df.format(c.getTime()).substring(0, 4).toString()));
    							upData.put("_DateMonth", Integer.parseInt(df.format(c.getTime()).substring(5, 7).toString()));
    							upData.put("_DateDay", Integer.parseInt(df.format(c.getTime()).substring(8, 10).toString()));
		        				upData.put("_Category",CategoryEdit.getText().toString()) ;
		        				upData.put("_ChildCategory", ChidCategoryEdit.getText().toString());
		        				upData.put("_Note", NoteEdit.getText().toString());
		        				upData.put("_InCome", MoneyEdit.getText().toString());
		        				upData.put("_OutGo", 0);
		        				db2.update("MTDB", upData, "_Id=?" ,Id);
		        				if(UserData.getString("UserName", "").isEmpty()){
		        					DBHelper SyncDB = new DBHelper(getActivity(), null, null, 0);
		        					SQLiteDatabase SyncTB = DH.getWritableDatabase();
		        			    	ContentValues values = new ContentValues();
		        			    	values.put("_SerId", SerId[0]);
		        			    	values.put("_Type", 0);
		        			    	db.insert("SYNCDB", null, values);
		        				}else{
			        				new ConnectServer(
			        						getActivity(),
			        						SerId, 
		        							date.format(c.getTime()), 
		        							CategoryEdit.getText().toString(), 
		        							ChidCategoryEdit.getText().toString(), 
		        							NoteEdit.getText().toString(), 
		        							MoneyEdit.getText().toString(), 
		        							"0",
		        							"").execute("edit");
		        				}
		        				onResume();
		        				alert.dismiss();
		        			}
		        		});
		                break;
		            case R.id.rdo2://支出
		            	Save.setOnClickListener(new Button.OnClickListener(){ 
		        			@Override
		        			public void onClick(View v) {
		        				upData.put("_DateYear", Integer.parseInt(df.format(c.getTime()).substring(0, 4).toString()));
    							upData.put("_DateMonth", Integer.parseInt(df.format(c.getTime()).substring(5, 7).toString()));
    							upData.put("_DateDay", Integer.parseInt(df.format(c.getTime()).substring(8, 10).toString()));
		        				upData.put("_Category",CategoryEdit.getText().toString()) ;
		        				upData.put("_ChildCategory", ChidCategoryEdit.getText().toString());
		        				upData.put("_Note", NoteEdit.getText().toString());
		        				upData.put("_InCome", 0);
		        				upData.put("_OutGo", MoneyEdit.getText().toString());
		        				db2.update("MTDB", upData, "_Id=?" ,Id);
		        				if(UserData.getString("UserName", "").isEmpty()){
		        					DBHelper SyncDB = new DBHelper(getActivity(), null, null, 0);
		        					SQLiteDatabase SyncTB = DH.getWritableDatabase();
		        			    	ContentValues values = new ContentValues();
		        			    	values.put("_SerId", SerId[0]);
		        			    	values.put("_Type", 0);
		        			    	db.insert("SYNCDB", null, values);
		        				}else{
		        					new ConnectServer(
			        						getActivity(),
			        						SerId, 
		        							date.format(c.getTime()), 
		        							CategoryEdit.getText().toString(), 
		        							ChidCategoryEdit.getText().toString(), 
		        							NoteEdit.getText().toString(), 
		        							"0", 
		        							MoneyEdit.getText().toString(), 
		        							"").execute("edit");
		        				}
		        				onResume();
		        				alert.dismiss();
		        			}
		        		});
		                break;
		            }
		    }});
		V.setBackgroundColor(Color.WHITE);
		alert.setView(V);
		alert.show();
        
	}
	
	
}
