package cn.wydewy.wyplayer.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import cn.wydewy.wyplayer.R;

/**
 * By wydewy 2013 Open Source Project
 * 
 * <br>
 * <b>首次进入程序提示扫描对话框</b></br>
 * 
 * <br>
 * 带电视机开关效果的首次进入提示扫描</br>
 * 
 * @author wydewy
 * @version 2013.07.31 v1.0 显示对话框界面
 */
public class ScanDialog extends TVAnimDialog {

	private Button button;

	public ScanDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public ScanDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}

	protected ScanDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_scan);

		button = (Button) findViewById(R.id.dialog_scan_btn_ok);
		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dismiss();
			}
		});

	}

}
