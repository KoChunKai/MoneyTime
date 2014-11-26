package money.time;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import money.time.R;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import money.time.SettingFragment.GasAsyncTask;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

public class RemindAdapter extends BroadcastReceiver{

	private NotificationCompat.Builder NotifiBuilder;
	private Intent NotificationIntent;
	private Notification Notifi;
	private RemoteViews remoteView;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if(intent.getStringExtra("message").matches("GasRemind")){
			String OilMoney,NextDate;
			OilMoney = intent.getStringExtra("NextGasMoney").substring(0, 12)+ ", " + intent.getStringExtra("NextGasMoney").substring(30);
			NextDate = intent.getStringExtra("NextGasMoney").substring(12, 15) + intent.getStringExtra("NextGasMoney").substring(20, 27);
			NotifiBuilder = new NotificationCompat.Builder(context);
			NotificationIntent = new Intent(context, StartActivity.class);
			NotifiBuilder.setSmallIcon(R.drawable.ic_launcher)
			   .setOngoing(true)
			   .setAutoCancel(true)
			   .setTicker("MoneyTime油價小提醒");
			Notifi = NotifiBuilder.build();
			Notifi.flags = Notification.FLAG_INSISTENT;
			remoteView = new RemoteViews("money.time",R.layout.notifi);
			remoteView.setTextViewText(R.id.notifititle , "油價小提醒(" + NextDate + ")");
			remoteView.setTextViewText(R.id.notifitext1,OilMoney);
			Notifi.contentView = remoteView;
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
					NotificationIntent, 0);
			Notifi.contentIntent = contentIntent;
			NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			Notifi.flags |= Notification.FLAG_AUTO_CANCEL;
			nm.notify(0, Notifi);
			Intent mIntent = new Intent(context,RemindActivity.class); //Same as above two lines
		    mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    mIntent.putExtra("OilMoney", OilMoney);
		    mIntent.putExtra("NextDate", NextDate);
		    context.startActivity(mIntent);
		}
		if(intent.getStringExtra("message").matches("WriteRemind")){
			NotifiBuilder = new NotificationCompat.Builder(context);
			NotificationIntent = new Intent(context, StartActivity.class);
			NotifiBuilder.setSmallIcon(R.drawable.ic_launcher)
			   .setOngoing(true)
			   .setAutoCancel(true)
			   .setTicker("MoneyTime記帳小提醒");
			Notifi = NotifiBuilder.build();
			Notifi.flags = Notification.FLAG_INSISTENT;
			remoteView = new RemoteViews("money.time",R.layout.notifi);
			remoteView.setTextViewText(R.id.notifititle , "不要忘記記帳喔");
			remoteView.setTextViewText(R.id.notifitext1,"");
			remoteView.setImageViewResource(R.id.imageView1, R.drawable.ic_pen);
			Notifi.contentView = remoteView;
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
					NotificationIntent, 0);
			Notifi.contentIntent = contentIntent;
			NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			Notifi.flags |= Notification.FLAG_AUTO_CANCEL;
			nm.notify(0, Notifi);
			/*Intent mIntent = new Intent(context,RemindActivity.class); //Same as above two lines
		    mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    context.startActivity(mIntent);*/
		}

			
	}
	
}
