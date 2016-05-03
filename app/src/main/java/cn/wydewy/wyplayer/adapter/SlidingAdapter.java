package cn.wydewy.wyplayer.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import cn.wydewy.wyplayer.R;

/**
 * By wydewy 2013 Open Source Project
 * 
 * <br>
 * <b>侧滑菜单适配器</b></br>
 * 为什么要用TypedArray，参见http://www.eoeandroid.com/thread-233677-1-1.html
 * 和http://developer
 * .android.com/guide/topics/resources/more-resources.html#TypedArray
 * 
 * @author wydewy
 * @version 2013.05.12 v1.0 实现列表适配
 */
public class SlidingAdapter extends BaseAdapter {

	private TypedArray icons;
	private String[] texts;
	private int white;

	public SlidingAdapter(Context context) {
		// TODO Auto-generated constructor stub
		Resources res = context.getResources();
		icons = res.obtainTypedArray(R.array.sliding_list_icon);
		texts = res.getStringArray(R.array.sliding_list_text);
		white = res.getColor(R.color.white);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return icons.length();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		TextView textView;
		if (convertView == null) {
			textView = new TextView(parent.getContext());
			textView.setLayoutParams(new ListView.LayoutParams(
					ListView.LayoutParams.FILL_PARENT, 70));// 设置宽高
			textView.setPadding(20, 0, 0, 0);// 设置间距
			textView.setCompoundDrawablePadding(20);// 设置图片与文字的间距
			textView.setGravity(Gravity.CENTER_VERTICAL);// 垂直居中
			textView.setTextColor(white);// 白色
			textView.setTextSize(16);// 大小
		} else {
			textView = (TextView) convertView;
		}

		textView.setText(texts[position]);
		textView.setCompoundDrawablesWithIntrinsicBounds(
				icons.getDrawable(position), null, null, null);

		return textView;
	}

}
