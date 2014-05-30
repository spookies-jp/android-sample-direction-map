package jp.co.spookies.android.directionmap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

public class DirectionMapActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			Intent intent = new Intent(DirectionMapActivity.this,
					OrienteeringActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onTouchEvent(event);
	}

}
