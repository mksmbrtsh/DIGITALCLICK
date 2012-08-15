package maximsblog.blogspot.com.digitalclick;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.TypedValue;

public class CreatingThread extends Thread {

	public final static byte STATE_NOT_STARTED = 0;
	public final static byte STATE_RUNNING = 1;
	public final static byte STATE_DONE = 2;

	public final static int MESSAGE_COMPLETE = 0;
	public final static int MESSAGE_ERROR = 1;
	public final static int MESSAGE_PROGRESS = 2;
	public final static int MESSAGE_CANCEL = 3;

	private byte mbytStatus = STATE_NOT_STARTED;
	private boolean mblnStop;

	public Handler HandlerOfCaller;

	private Bitmap mResult;
	private Context mContext;
	private String mText;
	private Bitmap mOplacheno;

	public Bitmap getResult() {
		return mResult;
	}

	CreatingThread(Handler oHandler, Context context, String text,
			Bitmap oplacheno) {

		HandlerOfCaller = oHandler;
		mContext = context;
		mText = text;
		mOplacheno = oplacheno;
	}

	public void run() {
		mblnStop = false;
		mbytStatus = STATE_RUNNING;
		Bitmap bitmap = Bitmap.createBitmap(680, 480, Bitmap.Config.ARGB_4444);
		bitmap.eraseColor(Color.WHITE);
		Canvas c = new Canvas(bitmap);

		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		Typeface typeface = Typeface.createFromAsset(mContext.getAssets(),
				"fonts/arial.ttf");
		paint.setTypeface(typeface);
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		final float scaledPx = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources()
						.getDisplayMetrics());// 24 * densityMultiplier;
		paint.setTextSize(scaledPx);
		c.drawText(mText, 0, scaledPx, paint);
		if (mblnStop) {
			HandlerOfCaller.sendEmptyMessage(MESSAGE_CANCEL);
			mResult.recycle();
			mResult = null;
			return;
		}
		final float size = paint.measureText(mText);
		if (!mOplacheno.isRecycled()) {
			c.drawBitmap(mOplacheno, size, 0, null);
			this.mResult = bitmap;
			mbytStatus = STATE_DONE;
			if (mblnStop) {
				mResult.recycle();
				mResult = null;
				HandlerOfCaller.sendEmptyMessage(MESSAGE_CANCEL);
			} else
				HandlerOfCaller.sendEmptyMessage(MESSAGE_COMPLETE);
		} else
			HandlerOfCaller.sendEmptyMessage(MESSAGE_CANCEL);
		return;

	}

	public void StopThread() {
		mblnStop = true;
	}

	public byte GetStatus() {
		return mbytStatus;
	}
}
