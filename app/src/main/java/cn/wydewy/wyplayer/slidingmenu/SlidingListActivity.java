package cn.wydewy.wyplayer.slidingmenu;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;

/**
 * By wydewy 2013 Open Source Project
 * 
 * <br>
 * <b>拿来主义，第一次实现的较简单，也已完全完美移植过来，最后不得不放弃删除重新修改
 * 其源码来自：http://www.eoeandroid.com/forum.php?mod=viewthread&tid=271752
 * 
 * 由于动画效果不是很理想，并且找到了更好的例子， 效果更好
 * 源码来自：http://www.eoeandroid.com/thread-278959-1-1.html
 * 原生版源码：http://www.eoeandroid.com/forum.php?mod=viewthread
 * &tid=262666&reltid=262043&pre_thread_id=271752&pre_pos=2&ext=
 * 
 * 由于本程序只需要SlidingListActivity，其他的都被精简删除了</b></br>
 * 
 * @author wydewy && 应该是出自Kris
 * @version 2013.06.03 v1.0 修改去除部分代码
 */
public class SlidingListActivity extends ListActivity implements
		SlidingActivityBase {

	private SlidingActivityHelper mHelper;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHelper = new SlidingActivityHelper(this);
		mHelper.onCreate(savedInstanceState);
		ListView listView = new ListView(this);
		listView.setId(android.R.id.list);
		setContentView(listView);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPostCreate(android.os.Bundle)
	 */
	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mHelper.onPostCreate(savedInstanceState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#findViewById(int)
	 */
	@Override
	public View findViewById(int id) {
		View v = super.findViewById(id);
		if (v != null)
			return v;
		return mHelper.findViewById(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mHelper.onSaveInstanceState(outState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#setContentView(int)
	 */
	@Override
	public void setContentView(int id) {
		setContentView(getLayoutInflater().inflate(id, null));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#setContentView(android.view.View)
	 */
	@Override
	public void setContentView(View v) {
		setContentView(v, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#setContentView(android.view.View,
	 * android.view.ViewGroup.LayoutParams)
	 */
	@Override
	public void setContentView(View v, LayoutParams params) {
		super.setContentView(v, params);
		mHelper.registerAboveContentView(v, params);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.slidingmenu.lib.app.SlidingActivityBase#setBehindContentView(int)
	 */
	public void setBehindContentView(int id) {
		setBehindContentView(getLayoutInflater().inflate(id, null));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.slidingmenu.lib.app.SlidingActivityBase#setBehindContentView(android
	 * .view.View)
	 */
	public void setBehindContentView(View v) {
		setBehindContentView(v, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.slidingmenu.lib.app.SlidingActivityBase#setBehindContentView(android
	 * .view.View, android.view.ViewGroup.LayoutParams)
	 */
	public void setBehindContentView(View v, LayoutParams params) {
		mHelper.setBehindContentView(v, params);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.slidingmenu.lib.app.SlidingActivityBase#getSlidingMenu()
	 */
	public SlidingMenu getSlidingMenu() {
		return mHelper.getSlidingMenu();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.slidingmenu.lib.app.SlidingActivityBase#toggle()
	 */
	public void toggle() {
		mHelper.toggle();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.slidingmenu.lib.app.SlidingActivityBase#showAbove()
	 */
	public void showContent() {
		mHelper.showContent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.slidingmenu.lib.app.SlidingActivityBase#showBehind()
	 */
	public void showMenu() {
		mHelper.showMenu();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.slidingmenu.lib.app.SlidingActivityBase#showSecondaryMenu()
	 */
	public void showSecondaryMenu() {
		mHelper.showSecondaryMenu();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.slidingmenu.lib.app.SlidingActivityBase#setSlidingActionBarEnabled
	 * (boolean)
	 */
	public void setSlidingActionBarEnabled(boolean b) {
		mHelper.setSlidingActionBarEnabled(b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onKeyUp(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean b = mHelper.onKeyUp(keyCode, event);
		if (b)
			return b;
		return super.onKeyUp(keyCode, event);
	}

}
