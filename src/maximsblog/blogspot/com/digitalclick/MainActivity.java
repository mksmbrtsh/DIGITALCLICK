package maximsblog.blogspot.com.digitalclick;

import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.text.InputType;

public class MainActivity extends Activity implements OnItemClickListener,
		OnClickListener, Callback {

	private static final int MAXAMOUNTITEMS = 5;

	private int[] mPrices = new int[] { 10, 234, 23, 54, 23, 54, 34, 21, 53,
			23, 345, 65, 4, 5, 345, 65, 45, 34, 65, 786, 4, 4, 78, 9, 678, 456,
			7, 64, 5, 87, 4, 3 };

	private int[] mAmounts;
	private Button mCancelBtn;
	private Button mBuyBtn;
	private ListView mList;

	final static String PICT_URL = "http://digitalclick.ru/oplacheno.jpg";

	private static final int LOADINGDIALOG = 3;

	private Bitmap mOplacheno;
	private Bitmap mBlank;

	private OplachenoThread mOplachenoThread;
	private CreatingThread mCreatingThread;
	private SavingThread mSavingThread;

	private enum STAT {
		OPLACHENOLOADING, CREATINGPIC, SAVINGDATA, NOTHING
	};

	private STAT mStat;

	private Handler mProgressHandler = new Handler(this);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// инициализация UI
		initUi();

		if (savedInstanceState != null) {
			mAmounts = savedInstanceState.getIntArray("amounts");
			mOplacheno = savedInstanceState.getParcelable("mOplacheno");
			mBlank = savedInstanceState.getParcelable("mBlank");
			mStat = STAT.values()[savedInstanceState.getInt("mStat")];

		} else {
			mAmounts = new int[mPrices.length];
			mStat = STAT.NOTHING;
		}
		ItemsAdapter itemsAdapter = new ItemsAdapter(this, mPrices, mAmounts);
		mList.setAdapter(itemsAdapter);
		mList.setOnItemClickListener(this);
		mBuyBtn.setOnClickListener(this);
		mCancelBtn.setOnClickListener(this);
		if (getLastNonConfigurationInstance() != null) {
			if (mStat == STAT.OPLACHENOLOADING) {
				mOplachenoThread = (OplachenoThread) getLastNonConfigurationInstance();
				mOplachenoThread.HandlerOfCaller = mProgressHandler;
				switch (mOplachenoThread.GetStatus()) {
				case OplachenoThread.STATE_RUNNING:
					break;
				case OplachenoThread.STATE_NOT_STARTED:
					break;
				case OplachenoThread.STATE_DONE:
					if(mOplachenoThread.getResult()!=null)
						oplachenoComplite();
					break;
				default:
					dismissDialog(LOADINGDIALOG);
					// Get rid of the sending thread
					mOplachenoThread.StopThread();
					mOplachenoThread = null;
					mStat = STAT.NOTHING;
					break;
				}
			} else if(mStat == STAT.CREATINGPIC){
				mCreatingThread = (CreatingThread) getLastNonConfigurationInstance();
				mCreatingThread.HandlerOfCaller = mProgressHandler;
				switch (mCreatingThread.GetStatus()) {
				case CreatingThread.STATE_RUNNING:
					break;
				case CreatingThread.STATE_NOT_STARTED:
					break;
				case CreatingThread.STATE_DONE:
					if(mCreatingThread.getResult()!=null)
						creatingPicComplite();
					break;
				default:
					dismissDialog(LOADINGDIALOG);
					mCreatingThread.StopThread();
					mCreatingThread = null;
					mStat = STAT.NOTHING;
					break;
				}
			} else if(mStat == STAT.SAVINGDATA){
				mSavingThread = (SavingThread) getLastNonConfigurationInstance();
				mSavingThread.HandlerOfCaller = mProgressHandler;
				switch (mSavingThread.GetStatus()) {
				case SavingThread.STATE_RUNNING:
					break;
				case SavingThread.STATE_NOT_STARTED:
					break;
				case SavingThread.STATE_DONE:
					if(mSavingThread.getResult()!=null){
					Intent intent = new Intent(this, ResultActivity.class);
					startActivity(intent);
					mStat = STAT.NOTHING;
					}
					break;
				default:
					dismissDialog(LOADINGDIALOG);
					mCreatingThread.StopThread();
					mCreatingThread = null;
					mStat = STAT.NOTHING;
					break;
				}
				
			}
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putIntArray("amounts", mAmounts);
		outState.putParcelable("mOplacheno", mOplacheno);
		outState.putParcelable("mBlank", mBlank);
		outState.putInt("mStat", mStat.ordinal());
		super.onSaveInstanceState(outState);
	};

	@Override
	public Object onRetainNonConfigurationInstance() {
		removeDialog(LOADINGDIALOG);
		if (mOplachenoThread != null) {
			mOplachenoThread.HandlerOfCaller = null;
			return mOplachenoThread;
		} else if (mCreatingThread != null) {
			mCreatingThread.HandlerOfCaller = null;
			return mCreatingThread;
		}
		return null;
	};

	private void initUi() {
		mCancelBtn = (Button) findViewById(R.id.cancel_btn);
		mBuyBtn = (Button) findViewById(R.id.buy_btn);
		mList = (ListView) findViewById(R.id.list);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View v, final int position,
			long arg3) {
		final ItemsAdapter itemsAdapter = (ItemsAdapter) mList.getAdapter();

		if (itemsAdapter.getSummaryAmount() <= MAXAMOUNTITEMS) {
			Builder b = new Builder(this).setMessage(R.string.inputmes);
			final EditText inputView = new EditText(this);
			inputView.setInputType(InputType.TYPE_CLASS_NUMBER);
			inputView.setRawInputType(Configuration.KEYBOARD_12KEY);
			b.setView(inputView);
			b.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							String inputString = inputView.getText().toString();
							if (inputString.length() > 0) {
								int newValue = 0;
								try {
									newValue = Integer.valueOf(inputString);
								} catch (NumberFormatException e) {
									Toast.makeText(MainActivity.this,
											R.string.errinput,
											Toast.LENGTH_LONG).show();
									return;
								}
								if (itemsAdapter.getSummaryAmount() + newValue > MAXAMOUNTITEMS) {
									Toast.makeText(MainActivity.this,
											R.string.maxamount,
											Toast.LENGTH_LONG).show();
								} else {
									itemsAdapter.setNewAmount(position,
											newValue);
									mList.invalidateViews();

								}
							}
						}
					});
			b.create().show();
		} else
			Toast.makeText(this, R.string.maxamount, Toast.LENGTH_LONG).show();

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buy_btn:

			if (((ItemsAdapter) mList.getAdapter()).getSummaryAmount() > 0) {
				mOplachenoThread = new OplachenoThread(mProgressHandler);
				mOplachenoThread.start();
				mStat = STAT.OPLACHENOLOADING;
				showDialog(LOADINGDIALOG);
			} else
				Toast.makeText(this, R.string.maxamount, Toast.LENGTH_LONG)
						.show();

			break;
		case R.id.cancel_btn:
			((ItemsAdapter) mList.getAdapter()).resetSum();
			mList.invalidateViews();
			break;
		default:
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		ProgressDialog dialog = new ProgressDialog(this);
		dialog.setIndeterminate(true);
		dialog.setCancelable(true);
		dialog.setMessage(getString(R.string.loading));
		dialog.setButton(getString(android.R.string.cancel),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						stop_work();
						removeDialog(LOADINGDIALOG);
					}

				});
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {

				stop_work();
				removeDialog(LOADINGDIALOG);
			}
		});

		return dialog;
	};

	private void stop_work() {
		if (mOplachenoThread != null) {
			mOplachenoThread.StopThread();
			mOplachenoThread = null;
		} else if (mCreatingThread != null) {
			mCreatingThread.StopThread();
			mCreatingThread = null;
		}

		else if (mSavingThread != null) {
			mSavingThread.StopThread();
			mSavingThread = null;

		}
		if (mOplacheno != null)
			mOplacheno.recycle();
		if (mBlank != null)
			mBlank.recycle();
		mOplacheno = null;
		mBlank = null;
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (mStat == STAT.OPLACHENOLOADING) {
			switch (msg.what) {
			case MESSAGE_COMPLETE:
				oplachenoComplite();
				break;
			case MESSAGE_ERROR:
				removeDialog(LOADINGDIALOG);
				Toast.makeText(this, R.string.errio, Toast.LENGTH_LONG).show();
				mStat = STAT.NOTHING;
				break;
			case MESSAGE_CANCEL:
				removeDialog(LOADINGDIALOG);
				mStat = STAT.NOTHING;
				break;
			}
		} else if (mStat == STAT.CREATINGPIC) {
			switch (msg.what) {
			case MESSAGE_COMPLETE:
				creatingPicComplite();
				break;
			case MESSAGE_ERROR:
				Toast.makeText(this, R.string.errio, Toast.LENGTH_LONG).show();
				removeDialog(LOADINGDIALOG);
				mStat = STAT.NOTHING;
				break;
			case MESSAGE_CANCEL:
				removeDialog(LOADINGDIALOG);
				mStat = STAT.NOTHING;
				break;
			}
		} else if (mStat == STAT.SAVINGDATA) {
			switch (msg.what) {
			case MESSAGE_COMPLETE:
				Intent intent = new Intent(this, ResultActivity.class);
				startActivity(intent);
				mStat = STAT.NOTHING;
				removeDialog(LOADINGDIALOG);
				break;
			case MESSAGE_ERROR:
				Toast.makeText(this, R.string.errio, Toast.LENGTH_LONG).show();
				removeDialog(LOADINGDIALOG);
				mStat = STAT.NOTHING;
				break;
			case MESSAGE_CANCEL:
				removeDialog(LOADINGDIALOG);
				mStat = STAT.NOTHING;
				break;
			}
		}
		return false;

	}

	private void creatingPicComplite() {
		mBlank = mCreatingThread.getResult();
		mSavingThread = new SavingThread(mProgressHandler, this, mBlank);
		mSavingThread.start();
		mStat = STAT.SAVINGDATA;
	}

	private void oplachenoComplite() {
		mOplacheno = mOplachenoThread.getResult();
		StringBuilder sb = new StringBuilder();
		sb.append(getString(R.string.yourp));
		for (int i1 = 0; i1 < mAmounts.length; i1++) {
			if (mAmounts[i1] != 0) {
				if (sb.length() > getString(R.string.yourp).length())
					sb.append(',');
				sb.append(' ');
				sb.append(getString(R.string.part1));
				sb.append(i1);
				sb.append('(');
				sb.append(mAmounts[i1]);
				sb.append(' ');
				sb.append(getString(R.string.с));
				sb.append(')');
			}
		}
		mCreatingThread = new CreatingThread(mProgressHandler, this,
				sb.toString(), mOplacheno);
		mCreatingThread.start();
		mStat = STAT.CREATINGPIC;
	}

	public final static int MESSAGE_COMPLETE = 0;
	public final static int MESSAGE_ERROR = 1;
	public final static int MESSAGE_PROGRESS = 2;
	public final static int MESSAGE_CANCEL = 3;
}