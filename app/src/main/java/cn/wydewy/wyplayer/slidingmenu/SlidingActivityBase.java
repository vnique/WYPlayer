package cn.wydewy.wyplayer.slidingmenu;

import android.view.View;
import android.view.ViewGroup.LayoutParams;

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
public interface SlidingActivityBase {

	/**
	 * Set the behind view content to an explicit view. This view is placed
	 * directly into the behind view 's view hierarchy. It can itself be a
	 * complex view hierarchy.
	 * 
	 * @param view
	 *            The desired content to display.
	 * @param layoutParams
	 *            Layout parameters for the view.
	 */
	public void setBehindContentView(View view, LayoutParams layoutParams);

	/**
	 * Set the behind view content to an explicit view. This view is placed
	 * directly into the behind view 's view hierarchy. It can itself be a
	 * complex view hierarchy. When calling this method, the layout parameters
	 * of the specified view are ignored. Both the width and the height of the
	 * view are set by default to MATCH_PARENT. To use your own layout
	 * parameters, invoke setContentView(android.view.View,
	 * android.view.ViewGroup.LayoutParams) instead.
	 * 
	 * @param view
	 *            The desired content to display.
	 */
	public void setBehindContentView(View view);

	/**
	 * Set the behind view content from a layout resource. The resource will be
	 * inflated, adding all top-level views to the behind view.
	 * 
	 * @param layoutResID
	 *            Resource ID to be inflated.
	 */
	public void setBehindContentView(int layoutResID);

	/**
	 * Gets the SlidingMenu associated with this activity.
	 * 
	 * @return the SlidingMenu associated with this activity.
	 */
	public SlidingMenu getSlidingMenu();

	/**
	 * Toggle the SlidingMenu. If it is open, it will be closed, and vice versa.
	 */
	public void toggle();

	/**
	 * Close the SlidingMenu and show the content view.
	 */
	public void showContent();

	/**
	 * Open the SlidingMenu and show the menu view.
	 */
	public void showMenu();

	/**
	 * Open the SlidingMenu and show the secondary (right) menu view. Will
	 * default to the regular menu if there is only one.
	 */
	public void showSecondaryMenu();

	/**
	 * Controls whether the ActionBar slides along with the above view when the
	 * menu is opened, or if it stays in place.
	 * 
	 * @param slidingActionBarEnabled
	 *            True if you want the ActionBar to slide along with the
	 *            SlidingMenu, false if you want the ActionBar to stay in place
	 */
	public void setSlidingActionBarEnabled(boolean slidingActionBarEnabled);

}
