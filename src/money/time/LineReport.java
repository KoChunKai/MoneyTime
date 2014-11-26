package money.time;

import java.util.ArrayList;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import android.os.Bundle;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class LineReport extends Activity {
	private LinearLayout li1;
	private DBHelper DH = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.line_layout);
		li1 = (LinearLayout) findViewById(R.id.lin);
		openDB();
		initView();
	}
	
	private void openDB() {
		// TODO Auto-generated method stub
    	DH = new DBHelper(this, null, null, 0);
	}

	private void initView() {
		String[] titles = new String[] { "收入", "支出" };
		SQLiteDatabase db = DH.getReadableDatabase();
		String MoneyConditional; 
		ArrayList<int[]> value = new ArrayList<int[]>();
		int[] d1 = new int[12];
		int[] d2 = new int[12];
		for(int month = 1;month<=12;month++){
			MoneyConditional = 
					"SELECT _DateYear,_DateMonth,SUM(_InCome),SUM(_OutGo) FROM MTDB WHERE _DateMonth = "
					+ month +
					" GROUP BY _DateMonth,_DateYear " +
					"ORDER BY _DateMonth ASC";
			Cursor Money = db.rawQuery(MoneyConditional, null);
			if (Money.getCount() > 0){
				Money.moveToNext();
				int p = month-1;
				d2[p] = Integer.parseInt(Money.getString(Money.getColumnIndex("SUM(_OutGo)")));
		    	d1[p] = Integer.parseInt(Money.getString(Money.getColumnIndex("SUM(_InCome)")));
			}
			else{
				int p = month-1;
				d2[p] = 0;
		    	d1[p] = 0;
			}
			Money.close();
		}
		value.add(d1);
		value.add(d2);
		int[] colors = { Color.GREEN, Color.RED };
		li1.addView(
				xychar(titles, value, colors, new int[] { 1, 2, 3, 4, 5, 6, 7,
						8, 9, 10, 11, 12 }), new RelativeLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	public GraphicalView xychar(String[] titles, ArrayList<int[]> value,
			int[] colors, int[] xLable) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		for (int i = 0; i < titles.length; i++) {
			XYSeries series = new XYSeries(titles[i]);
			int[] yLable = value.get(i);
			for (int j = 0; j < yLable.length; j++) {
				series.add(xLable[j], yLable[j]);
			}
			for (int j = 0; j < 13; j++)
				renderer.addXTextLabel(j, j + "月");
			dataset.addSeries(series);
			XYSeriesRenderer xyRenderer = new XYSeriesRenderer();
			xyRenderer.setColor(colors[i]);
			xyRenderer.setPointStyle(PointStyle.TRIANGLE);
			renderer.addSeriesRenderer(xyRenderer);
		}
		renderer.setXTitle("月份");
		renderer.setYTitle("(元)");
		renderer.setChartTitleTextSize(20);
		renderer.setAxisTitleTextSize(20);
		renderer.setXLabels(6);
		renderer.setYLabels(20);
		renderer.setAxesColor(Color.BLACK);
		renderer.setXLabelsAlign(Align.LEFT);
		renderer.setYLabelsAlign(Align.RIGHT);
		renderer.setShowGrid(true);
		renderer.setShowAxes(true);
		renderer.setBarSpacing(0.5);
		renderer.setInScroll(false);
		renderer.setPanEnabled(true, false);
		renderer.setClickEnabled(false);
		renderer.setZoomEnabled(false, false);
		int length = renderer.getSeriesRendererCount();
		renderer.setLabelsColor(Color.RED);
		renderer.setChartTitle("每個月的總計");
		renderer.setLegendTextSize(18);
		renderer.setPanLimits(new double[] { 0, 13, 0, 20000 });
		renderer.setRange(new double[] { 0d, 5d, 0d, 20000d });
		renderer.setApplyBackgroundColor(true);
		renderer.setBackgroundColor(Color.WHITE);
		renderer.setMarginsColor(Color.argb(0, 220, 228, 234));
		
		for (int i = 0; i < length; i++) { SimpleSeriesRenderer ssr =
		renderer.getSeriesRendererAt(i);
		ssr.setChartValuesTextAlign(Align.RIGHT);
		 ssr.setChartValuesTextSize(12); ssr.setDisplayChartValues(true); }
		
		GraphicalView mChartView = ChartFactory.getBarChartView(
				getApplicationContext(), dataset, renderer, Type.DEFAULT);

		return mChartView;

	}
	

}