package cn.wydewy.wyplayer.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import cn.wydewy.wyplayer.R;

/**
 * 
 * 
 * <br>
 * <b>关于对话框</b></br>
 * 
 * <br>
 * 带电视机开关效果的弹出对话框</br>
 * 
 * @version 2013.08.05 v1.0 显示对话框界面
 */
public class AboutDialog extends TVAnimDialog {

	private Button button;

	public AboutDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public AboutDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}

	protected AboutDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_about);

		button = (Button) findViewById(R.id.dialog_about_btn_ok);
		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dismiss();
			}
		});

	}

}
