package jp.co.spookies.android.directionmap;

import java.util.Iterator;
import java.util.List;

import com.google.android.maps.MapActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

public class OrienteeringActivity extends MapActivity implements Runnable {
	private boolean runFlag;
	private RunnerView mapView;
	private ProgressDialog dialog;
	private Handler handler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// MapViewの生成時にAPIキーを渡す
		mapView = new RunnerView(this, getString(R.string.api_key));
		setContentView(mapView);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	public void onResume() {
		super.onResume();

		mapView.init();
		handler = new Handler();

		// 目的地のダイアログ表示
		dialog = new ProgressDialog(this);
		dialog.setTitle("Loading...");

		// 目的地名
		List<CityOverlay> cities = mapView.getCities();
		if (cities != null && cities.size() > 0) {
			Iterator<CityOverlay> ite = cities.iterator();
			StringBuffer sbuf = new StringBuffer();
			while (ite.hasNext()) {
				CityOverlay city = ite.next();
				if (sbuf.length() != 0) {
					sbuf.append("と");
				}
				sbuf.append(city.getCityname());
			}
			dialog.setMessage(sbuf.toString() + "へ向かえ");
		}
		dialog.setIndeterminate(false);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setCancelable(false);
		dialog.show();

		// Mapの更新を別スレッドで処理
		runFlag = true;
		new Thread(this).start();
	}

	@Override
	public void onPause() {
		super.onPause();
		runFlag = false;
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(5000);
			handler.post(new Runnable() {
				@Override
				public void run() {
					dialog.setTitle("Ready");
				}
			});
			// ダイアログの消去
			Thread.sleep(1000);
			dialog.dismiss();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		while (runFlag) {
			// 更新処理
			mapView.update();
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}