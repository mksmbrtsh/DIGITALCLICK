package maximsblog.blogspot.com.digitalclick;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ItemsAdapter extends BaseAdapter {

	private int[] mPrices;
	private int[] mAmounts;
	private int mSum;
	
	private Context mContext;
	
	public ItemsAdapter(Context context, int[] prices, int[] amounts){
		mContext = context;
		mPrices = prices;
		mAmounts = amounts;
		mSum = 0;
		for(int i : amounts)
			mSum+=i;
	}
	
	public int[] getAmounts(){
		return mAmounts;
	}
	
	@Override
	public int getCount() {
		return mAmounts.length;
	}

	@Override
	public Object getItem(int position) {
		return mAmounts[position];
	}

	@Override
	public long getItemId(int position) {
		return mAmounts[position];
	}
	
	public void setNewAmount(int position, int newValue){
		mSum-=mAmounts[position];
		mAmounts[position] = newValue;
		mSum+=newValue;
	}
	public int getSummaryAmount(){
		return mSum;
	}
	public void resetSum(){
		mSum = 0;
		for(int i1=0;i1<mAmounts.length;i1++)
			mAmounts[i1]=0;
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView text = new TextView(mContext);
		StringBuilder sb = new StringBuilder();
		sb.append(mContext.getString(R.string.part1));
		sb.append(position);
		sb.append(' ');
		sb.append(mContext.getString(R.string.part2));
		sb.append(mPrices[position]);
		sb.append(' ');
		sb.append(mContext.getString(R.string.part3));
		sb.append(mAmounts[position]);
		sb.append(' ');
		sb.append(mContext.getString(R.string.part4));
		text.setText(sb.toString());
		return text;
	}

}
