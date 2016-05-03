package cn.wydewy.wyplayer.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import cn.wydewy.wyplayer.MainActivity;
import cn.wydewy.wyplayer.R;

/**
 * By wydewy 2013 Open Source Project
 * 
 * <br>
 * <b>歌曲列表菜单对话框</b></br>
 * 
 * <br>
 * 带电视机开关效果的弹出菜单</br>
 * 
 * @author wydewy
 * @version 2013.07.31 v1.0 显示对话框界面
 */
public class MenuDialog extends TVAnimDialog implements
		android.view.View.OnClickListener {

	private TextView title;
	private TextView remove;
	private TextView delete;
	private TextView info;
	private Button button;

	public MenuDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public MenuDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}

	protected MenuDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_menu);

		title = (TextView) findViewById(R.id.dialog_menu_tv_title);
		remove = (TextView) findViewById(R.id.dialog_menu_tv_remove);
		delete = (TextView) findViewById(R.id.dialog_menu_tv_delete);
		info = (TextView) findViewById(R.id.dialog_menu_tv_info);
		button = (Button) findViewById(R.id.dialog_menu_btn_return);
		remove.setOnClickListener(this);
		delete.setOnClickListener(this);
		info.setOnClickListener(this);
		button.setOnClickListener(this);
	}

	/**
	 * 设置对话框标题
	 * 
	 * @param text
	 *            内容
	 */
	public void setDialogTitle(String text) {
		title.setText(text);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.dialog_menu_tv_remove:
			setDialogId(MainActivity.DIALOG_MENU_REMOVE);
			break;

		case R.id.dialog_menu_tv_delete:
			setDialogId(MainActivity.DIALOG_MENU_DELETE);
			break;

		case R.id.dialog_menu_tv_info:
			setDialogId(MainActivity.DIALOG_MENU_INFO);
			break;

		default:
			setDialogId(MainActivity.DIALOG_DISMISS);
			break;
		}
		dismiss();
	}

}
