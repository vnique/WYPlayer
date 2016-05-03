package cn.wydewy.wyplayer.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import cn.wydewy.wyplayer.MainActivity;
import cn.wydewy.wyplayer.R;

/**
 * By wydewy 2013 Open Source Project
 * 
 * <br>
 * <b>电视机开关效果的Dialog</b></br>
 * 
 * <br>
 * 取自优酷视频客户端退出时电视机关闭动画效果，返向推出打开效果，继承该类就带有该动画效果</br>
 * 
 * @author wydewy
 * @version 2013.07.31 v1.0 实现动画效果 <br>
 *          2013.08.01 v1.1 实现对话框关闭的监听 </br>
 */
public class TVAnimDialog extends Dialog {

	private int dialogId = MainActivity.DIALOG_DISMISS;
	private OnTVAnimDialogDismissListener listener;

	public TVAnimDialog(Context context) {
		super(context, R.style.TVAnimDialog);// 此处附上Dialog样式
		// TODO Auto-generated constructor stub
	}

	public TVAnimDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}

	protected TVAnimDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().setWindowAnimations(R.style.TVAnimDialogWindowAnim);// 此处附上Dialog动画
	}

	@Override
	public void dismiss() {
		// TODO Auto-generated method stub
		super.dismiss();
		if (listener != null) {
			listener.onDismiss(dialogId);
		}
	}

	/**
	 * 用于区分Dialog用途
	 * 
	 * @param dialogId
	 *            Dialog ID
	 */
	public void setDialogId(int dialogId) {
		this.dialogId = dialogId;
	}

	/**
	 * 设置监听器
	 * 
	 * @param listener
	 *            OnTVAnimDialogDismissListener
	 */
	public void setOnTVAnimDialogDismissListener(
			OnTVAnimDialogDismissListener listener) {
		this.listener = listener;
	}

	/**
	 * 用于监听对话框关闭的接口
	 */
	public interface OnTVAnimDialogDismissListener {
		/**
		 * 对话框关闭
		 * 
		 * @param dialogId
		 *            Dialog ID
		 */
		void onDismiss(int dialogId);
	}

}
