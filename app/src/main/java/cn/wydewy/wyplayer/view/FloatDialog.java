package cn.wydewy.wyplayer.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import cn.wydewy.wyplayer.R;

/**
 * @文件名称: FloatDialog.java
 * @功能描述: 自定义dialog
 * @版本信息: Copyright (c)2015
 * @开发人员: wydewy
 * @版本日志: 1.0
 * @创建时间: 2015年8月27日 下午1:45:38
 */
public class FloatDialog extends Dialog implements OnClickListener {
	private TextView leftTextView, rightTextView;
	private IDialogOnclickInterface dialogOnclickInterface;
	private Context context;

	public FloatDialog(Context context, int theme,
			IDialogOnclickInterface dialogOnclickInterface) {
		super(context, theme);
		this.context = context;
		this.dialogOnclickInterface = dialogOnclickInterface;

	}

	public String getLeftTextView() {
		return leftTextView.getText().toString();
	}

	public void setLeftTextView(String leftText) {
		if (this.leftTextView != null)
			this.leftTextView.setText(leftText);
	}

	public String getRightTextView() {
		return rightTextView.getText().toString();
	}

	public void setRightTextView(String rightText) {
		if (this.rightTextView != null)
		this.rightTextView.setText(rightText);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_dialog);
		leftTextView = (TextView) findViewById(R.id.textview_one);
		rightTextView = (TextView) findViewById(R.id.textview_two);
		leftTextView.setOnClickListener(this);
		rightTextView.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.textview_one:
			dialogOnclickInterface.leftOnclick();
			break;
		case R.id.textview_two:
			dialogOnclickInterface.rightOnclick();
			break;
		default:
			break;
		}
	}

	public interface IDialogOnclickInterface {
		void leftOnclick();

		void rightOnclick();
	}
}