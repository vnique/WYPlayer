package cn.wydewy.wyplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import cn.wydewy.wyplayer.service.MediaService;

/**
 * By CWD 2013 Open Source Project
 * 
 * <br>
 * <b>设置页面</b></br>
 * 
 * 图片资源不要放raw文件夹，可能出现很多问题，例如图片质量变低，@color透明色变成其他颜色
 * 
 * @author CWD
 * @version 2013.08.22 v1.0 实现对应各设置功能</br>
 */
public class SettingActivity extends SwipBack4AppCompatActivity {

	private int skinId;
	private int colorId;
	private int[] skins = { R.drawable.skin_bg1, R.drawable.skin_bg2,
			R.drawable.skin_bg3 }, // 背景图
			colors = { Color.argb(250, 251, 248, 29),// 默认色
					Color.argb(250, 255, 0, 0),// 红色
					Color.argb(250, 0, 255, 0),// 绿色
					Color.argb(250, 8, 255, 245),// 蓝色
					Color.argb(250, 185, 10, 245) };// 紫色

	private LinearLayout viewSkin;// 背景图区域
	private LinearLayout viewLyric;// 歌词颜色区域

	private ImageButton btnReturn;// 返回按钮

	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		viewSkin = (LinearLayout) findViewById(R.id.activity_setting_view_skin);
		viewLyric = (LinearLayout) findViewById(R.id.activity_setting_view_lyric);
		btnReturn = (ImageButton) findViewById(R.id.activity_setting_ib_return);

		preferences = getSharedPreferences(MainActivity.PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		skinId = preferences.getInt(MainActivity.PREFERENCES_SKIN,
				R.drawable.skin_bg1);
		colorId = preferences.getInt(MainActivity.PREFERENCES_LYRIC, colors[0]);

		final int size1 = viewSkin.getChildCount();
		for (int i = 0; i < size1; i++) {
			final ImageView imageView = (ImageView) viewSkin.getChildAt(i);
			final int resources = skins[i];
			if (resources == skinId) {
				imageView.setEnabled(false);
			}
			imageView.setId(i);
			imageView.setImageResource(resources);
			imageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					v.setEnabled(false);
					preferences.edit()
							.putInt(MainActivity.PREFERENCES_SKIN, resources)
							.commit();
					for (int j = 0; j < size1; j++) {
						if (v.getId() != j) {
							viewSkin.getChildAt(j).setEnabled(true);
						}
					}
				}
			});
		}

		final int size2 = viewLyric.getChildCount();
		for (int i = 0; i < size2; i++) {
			final LinearLayout layout = (LinearLayout) viewLyric.getChildAt(i);
			final ImageView imageView = (ImageView) layout.getChildAt(0);
			final int color = colors[i];
			if (color == colorId) {
				layout.setEnabled(false);
			}
			imageView.setImageDrawable(new ColorDrawable(color));
			layout.setId(i);
			layout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					v.setEnabled(false);
					preferences.edit()
							.putInt(MainActivity.PREFERENCES_LYRIC, color)
							.commit();
					for (int j = 0; j < size2; j++) {
						if (v.getId() != j) {
							viewLyric.getChildAt(j).setEnabled(true);
						}
					}
				}
			});
		}

		btnReturn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		Intent intent = new Intent(MediaService.BROADCAST_ACTION_SERVICE);
		intent.putExtra(MediaService.INTENT_ACTIVITY,
				MediaService.ACTIVITY_SETTING);
		sendBroadcast(intent);
	}

}
