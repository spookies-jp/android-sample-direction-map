package jp.co.spookies.android.directionmap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.widget.Toast;

public class RunnerView extends MapView {
	private RunnerOverlay runner;
	private MapController controller;
	private long time;
	private int numTargets;
	private double speed;
	private List<CityOverlay> cities;

	public RunnerView(Context context, String apiKey) {
		super(context, apiKey);
		setEnabled(true);
	}

	public void init() {
		time = -1;
		speed = 0.0;
		runner = new RunnerOverlay(getResources());

		// MapViewのコントローラ取得
		controller = getController();

		// 飛行機のスタート地点の設定
		try {
			// <目的地名, 緯度経度>のMapを作成
			Map<String, GeoPoint> geoMap = new LinkedHashMap<String, GeoPoint>();

			InputStream input = getResources().getAssets()
					.open("landmarks.csv");
			BufferedReader buffer = new BufferedReader(new InputStreamReader(
					input));
			String line;
			String[] values;
			while ((line = buffer.readLine()) != null) {
				values = line.split(",");
				if (values.length == 3) {
					geoMap.put(
        				values[0],
        				new GeoPoint(
    						(int) (Double.parseDouble(values[1]) * 1000000),
    						(int) (Double.parseDouble(values[2]) * 1000000)));
				}
			}
			buffer.close();

			cities = new ArrayList<CityOverlay>();
			if (geoMap.size() >= 2) {
				int i = 0;
				for (Map.Entry<String, GeoPoint> map : geoMap.entrySet()) {
					if (i == 0) {
						// スタート地点
						controller.setCenter(map.getValue());
						i++;
						continue;
					}
					// 目的地
					String cityname = map.getKey();
					GeoPoint point = map.getValue();
					CityOverlay city = new CityOverlay(getResources(), point,
							cityname);
					cities.add(city);
					i++;
				}

				numTargets = cities.size();

				// Overlay表示するオブジェクトを指定
				getOverlays().clear();
				getOverlays().addAll(cities);
				getOverlays().add(runner);

			} else {
				// エラー処理
				Toast.makeText(getContext(), R.string.csv_error,
						Toast.LENGTH_LONG).show();
			}

			// zoom
			controller.setZoom(runner.getZoom());
			// 衛星画像表示
			setSatellite(runner.isSatellite());

		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// onTouchEventをRunnerOverlayに渡す
		return runner.onTouchEvent(event, this);
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		GeoPoint center = getMapCenter();

		// overlay全て取得してループ
		for (Overlay o : getOverlays()) {
			// CityOverlayにキャストできるものだけを扱う
			if (!(o instanceof CityOverlay)) {
				continue;
			}
			CityOverlay city = (CityOverlay) o;
			GeoPoint g = city.getGeoPoint();
			// 当たり判定
			if (!city.isThrough()
					&& Math.abs(center.getLatitudeE6() - g.getLatitudeE6())
						< 3000
					&& Math.abs(center.getLongitudeE6() - g.getLongitudeE6())
						< 3000) {
				city.setThrough();
				numTargets--;
				if (numTargets > 0) {
					// まだ残っているならToastで残りを表示
					Toast.makeText(getContext(), "残り　" + numTargets + "つ",
							Toast.LENGTH_LONG).show();
				}
				break;
			}
		}

		if (numTargets == 0) {
			// 全て通ったなら結果を設定
			numTargets--;
			;
			runner.setResult(System.currentTimeMillis() - time);
		}
	}

	public void update() {
		if (time < 0) {
			time = System.currentTimeMillis();
		}

		// zoom, speed, thetaの取得
		double zoom = runner.getZoom();
		speed = speed * 0.96 + runner.getSpeed() * 0.04;
		double theta = runner.getRotation();

		// 中心位置を取得
		GeoPoint g = getMapCenter();

		// 中心位置を更新
		controller.setCenter(new GeoPoint(g.getLatitudeE6()
				+ (int) (-speed * Math.sin(theta)), g.getLongitudeE6()
				+ (int) (speed * Math.cos(theta))));
		// 指定したzoomレベルになるまで繰り返し
		for (int i = 0; i < Math.abs(getZoomLevel() - zoom); i++) {
			if (zoom > getZoomLevel()) {
				controller.zoomIn();
			} else {
				controller.zoomOut();
			}
		}

		// 衛星画像の設定
		if (runner.isSatellite() && !isSatellite()) {
			setSatellite(true);
		} else if (!runner.isSatellite() && isSatellite()) {
			setSatellite(false);
		}
	}

	public List<CityOverlay> getCities() {
		return cities;
	}
}
