package maximsblog.blogspot.com.digitalclick;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

public class OplachenoThread extends Thread {

	private static final String URL = "http://digitalclick.ru/oplacheno.jpg";

	public final static byte STATE_NOT_STARTED = 0;
	public final static byte STATE_RUNNING = 1;
	public final static byte STATE_DONE = 2;

	private byte mbytStatus = STATE_NOT_STARTED;
	private boolean mblnStop;

	public Handler HandlerOfCaller;

	private Bitmap mResult;

	public Bitmap getResult() {
		return mResult;
	}

	OplachenoThread(Handler oHandler) {
		HandlerOfCaller = oHandler;
	}

	public void run() {
		mblnStop = false;
		mbytStatus = STATE_RUNNING;
		InputStream input;
		try {
			URL urlConn = new URL(URL);
			input = urlConn.openStream();
			if (mblnStop) {
				HandlerOfCaller.sendEmptyMessage(MainActivity.MESSAGE_CANCEL);

				return;
			}

			Bitmap b = BitmapFactory.decodeStream(input);
			input.close();
			mResult = b;

		} catch (MalformedURLException e) {
			HandlerOfCaller.sendEmptyMessage(MainActivity.MESSAGE_ERROR);
			e.printStackTrace();
			mResult.recycle();
			mResult = null;
			return;
		} catch (IOException e) {
			HandlerOfCaller.sendEmptyMessage(MainActivity.MESSAGE_ERROR);
			e.printStackTrace();
			
			return;
		}
		mbytStatus = STATE_DONE;
		if (mblnStop) {
			if(mResult!=null){
			mResult.recycle();
			mResult = null;
			}
			HandlerOfCaller.sendEmptyMessage(MainActivity.MESSAGE_CANCEL);
		} else
			HandlerOfCaller.sendEmptyMessage(MainActivity.MESSAGE_COMPLETE);

		return;

	}

	public void StopThread() {
		mblnStop = true;
	}

	public byte GetStatus() {
		return mbytStatus;
	}

}
