package jp.co.spookies.android.directionmap;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

/**
 * Mapの上に表示する画像を扱うクラス
 * 
 */
public class RunnerOverlay extends Overlay {
	private Paint paint;
	private double theta;
	private double speed;
	private int zoom;

	private RectF rectPlane;
	private RectF rectIn;
	private RectF rectOut;
	private RectF rectMap;
	private RectF rectResult;
	private int mapType;

	private Bitmap planeImage;
	private Bitmap[] mapImages;
	private Bitmap zoomInImage;
	private Bitmap zoomOutImage;
	private Bitmap resultImage;

	private final static int WIDTH = 800;
	private final static int HEIGHT = 480;
	private int width, height;
	private boolean resultFlag;
	private long resultTime;
	private long curTime;

	public RunnerOverlay(Resources res) {
		// 飛行機の画像
		planeImage = BitmapFactory.decodeResource(res, R.drawable.circle);
		// mapボタンの画像
		mapImages = new Bitmap[] {
				BitmapFactory.decodeResource(res, R.drawable.map),
				BitmapFactory.decodeResource(res, R.drawable.photo) };

		// zoomボタンの画像
		zoomInImage = BitmapFactory.decodeResource(res, R.drawable.zoom_in);
		zoomOutImage = BitmapFactory.decodeResource(res, R.drawable.zoom_out);
		// 結果画面の画像
		resultImage = BitmapFactory.decodeResource(res, R.drawable.result);
		// paint初期化
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.BLACK);

		// mapの初期値
		speed = 4.0;
		zoom = 12;
		mapType = 0;
		resultFlag = false;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);

		// 初期化がまだなら各値を設定
		if (rectIn == null) {
			width = mapView.getWidth();
			height = mapView.getHeight();
			rectPlane = new RectF(0, 0, height, height);
			rectPlane.offsetTo((width - rectPlane.width()) / 2, 0);
			rectOut = new RectF(0, 0, 161 * width / WIDTH, 64 * height / HEIGHT);
			rectIn = new RectF(639 * width / WIDTH, 0, width, 64 * height
					/ HEIGHT);
			rectMap = new RectF(639 * width / WIDTH, 416 * height / 480, width,
					height);
			rectResult = new RectF(0, 0, width, height);
			paint.setTextSize(35 * height / HEIGHT);
		}

		canvas.save();

		// 飛行機描画
		canvas.rotate((float) (theta * 180 / Math.PI), mapView.getWidth() / 2,
				mapView.getHeight() / 2);
		canvas.drawBitmap(planeImage, null, rectPlane, paint);
		canvas.restore();

		// ボタン描画
		canvas.drawBitmap(zoomInImage, null, rectIn, paint);
		canvas.drawBitmap(zoomOutImage, null, rectOut, paint);
		canvas.drawBitmap(mapImages[mapType], null, rectMap, paint);

		if (resultFlag) {
			// クリア画面描画
			canvas.drawBitmap(resultImage, null, rectResult, paint);
			canvas.drawText(String.format("%.2f 秒", resultTime / 1000.0f), 210
					* width / WIDTH, 285 * height / HEIGHT, paint);
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		if (resultFlag && System.currentTimeMillis() - curTime > 2000) {
			resultFlag = false;
			return true;
		}

		// 画面の中心からの距離を計算
		float dx = event.getX() - mapView.getWidth() / 2;
		float dy = event.getY() - mapView.getHeight() / 2;

		if (rectIn.contains(event.getX(), event.getY())) {
			// zoomIn
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (zoom < 17) { // zoomレベルの上限
					zoom++;
				}
			}
		} else if (rectOut.contains(event.getX(), event.getY())) {
			// zoomOut
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (zoom > 12) { // zoomレベルの下限
					zoom--;
				}
			}
		} else if (rectMap.contains(event.getX(), event.getY())) {
			// mapの表示切り替え
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				mapType = 1 - mapType;
			}
		} else {
			// 飛行機の向きとスピードを設定
			theta = Math.atan2(dy, dx);
			speed = Math.sqrt(dx * dx + dy * dy) * 3500.0 / mapView.getHeight();
		}

		return true;
	}

	// 回転を取得
	public double getRotation() {
		return theta;
	}

	// スピードを取得
	public double getSpeed() {
		return speed;
	}

	// zoomレベルを取得
	public int getZoom() {
		return zoom;
	}

	// 衛星写真かどうか
	public boolean isSatellite() {
		return mapType != 0;
	}

	// 結果を設定
	public void setResult(long time) {
		resultTime = time;
		curTime = System.currentTimeMillis();
		resultFlag = true;
	}
}
