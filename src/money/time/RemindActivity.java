package money.time;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import money.time.R;


public class RemindActivity extends Activity{
	
	private TextView Title,message;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notifi);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Title = (TextView)findViewById(R.id.notifititle);
		message = (TextView)findViewById(R.id.notifitext1);
		Intent intent = getIntent();
		message.setText(intent.getStringExtra("OilMoney"));
		Title.setText("ªo»ù¤p´£¿ô(" +  intent.getStringExtra("NextDate") + ")");
	}

}
