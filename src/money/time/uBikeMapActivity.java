package money.time;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;



public class uBikeMapActivity extends Activity implements LocationListener{
	GoogleMap googleMap;
	private double latitude,longitude;
	private Location location;
    private LocationManager locationManager;
    private LatLng Pos;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.ubikemap_layout);
	    setGoogleMap();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		googleMap.setOnInfoWindowClickListener(MarkerListener);
		googleMap.setInfoWindowAdapter(new InfoWindowAdapter() {

	        @Override
	        public View getInfoWindow(Marker arg0) {
	            return null;
	        }

	        @Override
	        public View getInfoContents(Marker marker) {
	            View v = getLayoutInflater().inflate(R.layout.ubikeinfosinppet_layout, null);
	            TextView ub = (TextView) v.findViewById(R.id.ubtextView1);
	            ImageView ubv = (ImageView) v.findViewById(R.id.ubimageView1);
	            ub.setText(marker.getSnippet());
	            ubv.getLayoutParams().height = ub.getLayoutParams().height;
	            ubv.getLayoutParams().width = ubv.getLayoutParams().height;
	            return v;
	        }
	    });
	}
	
	private OnInfoWindowClickListener MarkerListener = new OnInfoWindowClickListener(){

		@Override
		public void onInfoWindowClick(final Marker m) {
			// TODO Auto-generated method stub
			AlertDialog.Builder Dialog = new AlertDialog.Builder(uBikeMapActivity.this);
			Dialog.setTitle("是否路經規劃到" + "\"" + m.getTitle() + "\"");
			Dialog.setNeutralButton("是",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
					Toast.makeText(uBikeMapActivity.this, "路徑規劃中" , Toast.LENGTH_LONG).show();
					LatLng origin = Pos;
		            LatLng dest = m.getPosition();
		            String url = getDirectionsUrl(origin, dest);
		            new DownloadTask().execute(url);
			    }
			});
			Dialog.setNegativeButton("返回", null);
			Dialog.show();
		}
    };
	
	private void setGoogleMap() {
		// TODO Auto-generated method stub
		try {
			locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
			location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	        longitude = location.getLongitude();
			latitude = location.getLatitude();
	        Pos = new LatLng(latitude,longitude);
			//Pos = new LatLng(25.0492279,121.5138646);
	        googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
			googleMap.setMyLocationEnabled(true); 
	        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
	        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Pos, 12));
	        //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Pos, 14));
		} catch (Exception e) {
			e.printStackTrace();
		}
		new uBikeMapMark().execute("http://its.taipei.gov.tw/atis_index/data/youbike/youbike.json");
		/*MarkerOptions marker = new MarkerOptions()
        .position(
        		new LatLng(23.753296,120.430756))
        		.title("test")
        		.snippet("位置:" + '\n'
        				+"總停車空位:" + '\n'
        				);
        
        googleMap.addMarker(marker);*/
	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		longitude = location.getLongitude();
		latitude = location.getLatitude();
	    Pos = new LatLng(latitude,longitude);
	    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Pos, googleMap.getMaxZoomLevel()));
	}
	
	public class uBikeMapMark extends AsyncTask<String, Integer, String> {

		private ProgressDialog dialog = new ProgressDialog(uBikeMapActivity.this);
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
					new DialogInterface.OnClickListener(){ 
					@Override
					public void onClick(DialogInterface dialog, int which) {
						cancel(true);
						dialog.dismiss();
					}         
				});
			dialog.setMessage("YouBike資料更新中，請稍候....");
			dialog.show();
		}

		@Override
		protected String doInBackground(String... urls) {
			try {
				GetJsonData uBikeData = new GetJsonData(urls[0]);
				return uBikeData.getResult();
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
			super.onPostExecute(result);
			dialog.dismiss();
			try {
				JSONObject JsonData = new JSONObject(result);
				JSONArray retValArray = JsonData.getJSONArray("retVal");
				for(int size = 0;size < retValArray.length();size++){
					JSONObject retValData = retValArray.getJSONObject(size);
					latitude = Double.parseDouble(retValData.getString("lat"));	//取得經度
					longitude= Double.parseDouble(retValData.getString("lng"));
		            MarkerOptions marker = new MarkerOptions()
		            .position(
		            		new LatLng(latitude, longitude))
		            		.title(retValData.getString("sna"))
		            		.snippet("位置:" + retValData.getString("ar") + '\n'
		            				+"總停車空位:" + retValData.getString("tot")
		            				);
		            
			        googleMap.addMarker(marker).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_greenmark));
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private String getDirectionsUrl(LatLng origin,LatLng dest){
	    // Origin of route
	    String str_origin = "origin="+origin.latitude+","+origin.longitude;
	    // Destination of route
	    String str_dest = "destination="+dest.latitude+","+dest.longitude;
	    // Sensor enabled
	    String sensor = "sensor=false";
	    // Building the parameters to the web service
	    String parameters = str_origin+"&"+str_dest+"&"+sensor;
	    // Output format
	    String output = "json";
	    String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
	    return url;
	}
	
	private String downloadUrl(String strUrl) throws IOException{
	    String data = "";
	    InputStream iStream = null;
	    HttpURLConnection urlConnection = null;
	    try{
	        URL url = new URL(strUrl);
	        urlConnection = (HttpURLConnection) url.openConnection();
	        // Connecting to url
	        urlConnection.connect();
	        // Reading data from url
	        iStream = urlConnection.getInputStream();
	        BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
	        StringBuffer sb  = new StringBuffer();
	        String line = "";
	        while( ( line = br.readLine())  != null){
	            sb.append(line);
	        }
	        data = sb.toString();
	        br.close();
	    }catch(Exception e){
	    }finally{
	        iStream.close();
	        urlConnection.disconnect();
	    }
	    return data;
	}

	private class DownloadTask extends AsyncTask<String, Void, String>{

	    // Downloading data in non-ui thread
	    @Override
	    protected String doInBackground(String... url) {

	        // For storing data from web service
	        String data = "";

	        try{
	            // Fetching the data from web service
	            data = downloadUrl(url[0]);
	        }catch(Exception e){
	        }
	        return data;
	    }

	    // Executes in UI thread, after the execution of
	    // doInBackground()
	    @Override
	    protected void onPostExecute(String result) {
	        super.onPostExecute(result);

	        ParserTask parserTask = new ParserTask();
	        
		    AlertDialog.Builder Dialog = new AlertDialog.Builder(uBikeMapActivity.this);

	        // Invokes the thread for parsing the JSON data
	        parserTask.execute(result);
	    }
	}

	private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>> {
		
	    protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

	        JSONObject jObject;
	        List<List<HashMap<String, String>>> routes = null;
	        try{
	            jObject = new JSONObject(jsonData[0]);
	            DirectionsJSONParser parser = new DirectionsJSONParser();
	            routes = parser.parse(jObject);
	        }catch(Exception e){
	            e.printStackTrace();
	        }
	        return routes;
	    }

	    @Override
	    protected void onPostExecute(List<List<HashMap<String, String>>> result) {
	        ArrayList<LatLng> points = null;
	        PolylineOptions lineOptions = null;
	        MarkerOptions markerOptions = new MarkerOptions();

	        // Traversing through all the routes
	        for(int i=0;i<result.size();i++){
	            points = new ArrayList<LatLng>();
	            lineOptions = new PolylineOptions();

	            // Fetching i-th route
	            List<HashMap<String, String>> path = result.get(i);

	            // Fetching all the points in i-th route
	            for(int j=0;j<path.size();j++){
	                HashMap<String,String> point = path.get(j);

	                double lat = Double.parseDouble(point.get("lat"));
	                double lng = Double.parseDouble(point.get("lng"));
	                LatLng position = new LatLng(lat, lng);

	                points.add(position);
	            }

	            // Adding all the points in the route to LineOptions
	            lineOptions.addAll(points);
	            lineOptions.width(12);
	            lineOptions.color(Color.GREEN);
	        }

	        // Drawing polyline in the Google Map for the i-th route
	        googleMap.addPolyline(lineOptions);
	    }
	}
	
}