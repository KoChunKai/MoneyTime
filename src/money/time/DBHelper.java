package money.time;

import java.io.IOException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	private final static int DBVersion = 1; //<-- 版本
	private final static String DBName = "MoneyTime.db"; 
	private final static String TableName_1 = "MTDB";
	private final static String TableName_2 = "SYNCDB";
	private final static String MTDBSQL = "CREATE TABLE IF NOT EXISTS " + TableName_1 + "( " +
			"_Id INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"_SerId INTEGER," +
			"_DateYear INT," +
			"_DateMonth INT," +
			"_DateDay INT," +
			"_Category VARCHAR," +
			"_ChildCategory VARCHAR," +
			"_Note VARCHAR," +
			"_InCome INT," +
			"_OutGo INT," +
			"_ReceiptNumber VARCHAR" +
			");";
	private final static String SYNCDBSQL = "CREATE TABLE IF NOT EXISTS " + TableName_2 + "( " +
			"_Id INTEGER PRIMARY KEY, " +
			"_SerId INT, " +
			"_Type INT" +
			");";
			
	public DBHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, DBName, factory, DBVersion);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(MTDBSQL);
		db.execSQL(SYNCDBSQL);
	}
	
	@Override    
	public void onOpen(SQLiteDatabase db) {     
		super.onOpen(db);       
		// TODO 每次成功打開數據庫後首先被執行     
	} 

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE " + TableName_1 + ";");
		db.execSQL("DROP TABLE " + TableName_2 + ";");
		onCreate(db);
	}
	@Override
    public synchronized void close() {
        super.close();
    }
	
    
	
}