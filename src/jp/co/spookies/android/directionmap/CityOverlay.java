package jp.co.spookies.android.directionmap;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class CityOverlay extends Overlay {
	private String cityname;
	private Bitmap marker;
	private Bitmap passMarker;
	private GeoPoint geoPoint;
	private Paint paint;
	private boolean throughFlag;

	public CityOverlay(Resources res, GeoPoint geoPoint, String cityname) {
		// 都市名
		this.cityname = cityname;
		// 目的地の画像
		this.marker = BitmapFactory.decodeResource(res, R.drawable.point);
		// 通過後の画像
		this.passMarker = BitmapFactory.decodeResource(res,
				R.drawable.point_pass);
		// 座標
		this.geoPoint = geoPoint;

		paint = new Paint();
		paint.setAntiAlias(true);

		throughFlag = false;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		// marker描画
		Point point = mapView.getProjection().toPixels(geoPoint, null);
		canvas.drawBitmap(marker, point.x - marker.getWidth() / 2, point.y
				- marker.getHeight(), paint);
		super.draw(canvas, mapView, shadow);
	}

	public GeoPoint getGeoPoint() {
		return geoPoint;
	}

	public boolean isThrough() {
		return throughFlag;
	}

	public void setThrough() {
		// 通過したら画像切り替え
		throughFlag = true;
		marker = passMarker;
	}

	public String getCityname() {
		return cityname;
	}
}
