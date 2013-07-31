package com.example.bdmap;

import android.R.string;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
//import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.VersionInfo;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class LocationOverlayDemo extends Activity {

	int abc = 0;
	LocationClient mLocClient;

	LocationData locData = null;
	public MyLocationListenner myListener = new MyLocationListenner();

	locationOverlay myLocationOverlay = null;

	private PopupOverlay pop = null;
	private TextView popupText = null;
	private View viewCache = null;

	MyLocationMapView mMapView = null;
	private MapController mMapController = null;

	OnCheckedChangeListener radioButtonListener = null;
	Button requestLocButton = null;
	boolean isRequest = false;
	boolean isFirstLoc = true;
	boolean isLocationClientStop = false;
	Button xinjian = null;
	private EditText wenben = null;
	private String str = "";
	private MySQLiteOpenHelper myOpenHelper;// 创建一个继承SQLiteOpenHelper类实例
	private SQLiteDatabase mysql;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_locationoverlay);
		CharSequence titleLable = "百度地图^-^MM";
		setTitle(titleLable);
		requestLocButton = (Button) findViewById(R.id.button1);
		xinjian = (Button) findViewById(R.id.xinjian);
		String TABLE_NAME = "himi";
		String ID = "id";
		String TEXT = "text";
		String str_sql2 = "CREATE TABLE " + TABLE_NAME + "(" + ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + TEXT + " text );";
		try {
			mysql = myOpenHelper
					.getWritableDatabase(); // 实例数据库
		mysql.execSQL(str_sql2);}
		catch (Exception e) {
			System.out.println("操作失败！");
		}
		xinjian.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				LayoutInflater inflater = getLayoutInflater();
				final View layout = inflater.inflate(R.layout.xj,
						(ViewGroup) findViewById(R.id.xj));
				new AlertDialog.Builder(LocationOverlayDemo.this)
						.setTitle("新建纸条")
						.setView(layout)
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {

										wenben = (EditText) layout
												.findViewById(R.id.wenben);
										str = wenben.getText().toString();
										System.out.println("文本内容：" + str);
										ContentValues cv = new ContentValues();
										cv.put(MySQLiteOpenHelper.TEXT, str);
										abc++;
										pop.hidePop();
										popupText.setBackgroundResource(R.drawable.icon_geo);
										popupText.setText(" " + abc + " ");// + locData.latitude + ","
											//	+ locData.longitude);
										popupText.setTextColor(android.graphics.Color.WHITE);
										pop.showPopup(BMapUtil.getBitmapFromView(popupText),
												new GeoPoint((int) (locData.latitude * 1e6),
														(int) (locData.longitude * 1e6)), 8);
									}

								}).setNegativeButton("取消", null).show();
				// 新建一个Intent对象
				// Intent intent = new Intent();
				// 指定intent要指定的类
				// intent.putExtra("buttom", "1");
				// intent.setClass(LocationOverlayDemo.this, Exitbuttom.class);

				// 启动一个Activity
				// startActivity(intent);
			}
		});

		OnClickListener btnClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {

				requestLocClick();
			}
		};
		requestLocButton.setOnClickListener(btnClickListener);

		mMapView = (MyLocationMapView) findViewById(R.id.bmapView);
		mMapController = mMapView.getController();
		mMapView.getController().setZoom(17);
		mMapView.getController().enableClick(true);
		mMapView.setBuiltInZoomControls(true);
		createPaopao();

		mLocClient = new LocationClient(this);
		locData = new LocationData();
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setCoorType("bd09ll");
		option.setScanSpan(5000);
		mLocClient.setLocOption(option);
		mLocClient.start();

		myLocationOverlay = new locationOverlay(mMapView);

		myLocationOverlay.setData(locData);

		mMapView.getOverlays().add(myLocationOverlay);
		myLocationOverlay.enableCompass();

		mMapView.refresh();

	}

	public void requestLocClick() {
		isRequest = true;
		mLocClient.requestLocation();
		Toast.makeText(LocationOverlayDemo.this, "正在定位", Toast.LENGTH_SHORT)
				.show();
		pop.hidePop();

		popupText.setBackgroundResource(R.drawable.icon_geo);
		popupText.setText("" + abc + " ");// + locData.latitude + ","
		// + locData.longitude);
		popupText.setTextColor(this.getResources().getColor(R.color.white));
		pop.showPopup(BMapUtil.getBitmapFromView(popupText),
				new GeoPoint((int) (locData.latitude * 1e6),
						(int) (locData.longitude * 1e6)), 8);
	}

	public void modifyLocationOverlayIcon(Drawable marker) {

		myLocationOverlay.setMarker(marker);

		mMapView.refresh();
	}

	public void createPaopao() {
		viewCache = getLayoutInflater()
				.inflate(R.layout.custom_text_view, null);
		popupText = (TextView) viewCache.findViewById(R.id.textcache);

		PopupClickListener popListener = new PopupClickListener() {
			@Override
			public void onClickedPopup(int index) {
				Log.v("click", "clickapoapo");
			}
		};
		pop = new PopupOverlay(mMapView, popListener);
		MyLocationMapView.pop = pop;
	}

	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null || isLocationClientStop)
				return;

			locData.latitude = location.getLatitude();
			locData.longitude = location.getLongitude();

			locData.accuracy = 500;
			locData.direction = location.getDerect();

			myLocationOverlay.setData(locData);

			mMapView.refresh();

			if (isRequest || isFirstLoc) {

				mMapController.animateTo(new GeoPoint(
						(int) (locData.latitude * 1e6),
						(int) (locData.longitude * 1e6)));
				isRequest = false;
				pop.hidePop();
				popupText.setBackgroundResource(R.drawable.icon_geo);
				popupText.setText(" " + abc + " ");// + locData.latitude + ","
				// + locData.longitude);
				popupText.setTextColor(android.graphics.Color.WHITE);
				pop.showPopup(BMapUtil.getBitmapFromView(popupText),
						new GeoPoint((int) (locData.latitude * 1e6),
								(int) (locData.longitude * 1e6)), 8);
			}

			isFirstLoc = false;
		}

		public void onReceivePoi(BDLocation poiLocation) {
			if (poiLocation == null) {
				return;
			}
		}
	}

	public class locationOverlay extends MyLocationOverlay {

		public locationOverlay(MapView mapView) {
			super(mapView);
			// TODO Auto-generated constructor stub
		}

	}

	@Override
	protected void onPause() {
		isLocationClientStop = true;
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		isLocationClientStop = false;
		mMapView.onResume();
		// DemoApplication app = (DemoApplication)this.getApplication();

		super.onResume();

	}

	@Override
	protected void onDestroy() {

		if (mLocClient != null)
			mLocClient.stop();
		isLocationClientStop = true;
		mMapView.destroy();
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mMapView.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}

class MyLocationMapView extends MapView {
	static PopupOverlay pop = null;

	public MyLocationMapView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public MyLocationMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyLocationMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!super.onTouchEvent(event)) {

			if (pop != null && event.getAction() == MotionEvent.ACTION_UP)
				pop.hidePop();
		}
		return true;
	}
}
