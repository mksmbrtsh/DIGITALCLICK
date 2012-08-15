package maximsblog.blogspot.com.digitalclick;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Handler;

public class SavingThread extends Thread {

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

	public Bitmap getResult() {
		return mResult;
	}

	SavingThread(Handler oHandler, Context context, Bitmap result) {

		HandlerOfCaller = oHandler;
		mContext = context;
		mResult = result;
	}

	public void run() {
		mblnStop = false;
		mbytStatus = STATE_RUNNING;
		try {
		File f = new File(mContext.getExternalFilesDir(null).getAbsolutePath(),
				"1.png");	
			f.createNewFile();
			FileOutputStream fstream = new FileOutputStream(f);
			mResult.compress(CompressFormat.PNG, 70, fstream);
			fstream.close();
		} catch (IOException e) {
			HandlerOfCaller.sendEmptyMessage(MESSAGE_ERROR);
			mResult.recycle();
			mResult = null;
			return;
		}
		mbytStatus = STATE_DONE;
		if (mblnStop) {
			mResult.recycle();
			mResult = null;
			HandlerOfCaller.sendEmptyMessage(MESSAGE_CANCEL);
		} else
			HandlerOfCaller.sendEmptyMessage(MESSAGE_COMPLETE);

		return;

	}

	public void StopThread() {
		mblnStop = true;
	}

	public byte GetStatus() {
		return mbytStatus;
	}
}
