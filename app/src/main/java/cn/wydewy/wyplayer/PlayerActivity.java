package cn.wydewy.wyplayer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import cn.wydewy.wyplayer.custom.PushView;
import cn.wydewy.wyplayer.custom.VisualizerView;
import cn.wydewy.wyplayer.dialog.AboutDialog;
import cn.wydewy.wyplayer.dialog.InfoDialog;
import cn.wydewy.wyplayer.entity.MusicInfo;
import cn.wydewy.wyplayer.list.CoverList;
import cn.wydewy.wyplayer.lyric.LyricView;
import cn.wydewy.wyplayer.service.MediaBinder;
import cn.wydewy.wyplayer.service.MediaBinder.OnModeChangeListener;
import cn.wydewy.wyplayer.service.MediaBinder.OnPlayCompleteListener;
import cn.wydewy.wyplayer.service.MediaBinder.OnPlayErrorListener;
import cn.wydewy.wyplayer.service.MediaBinder.OnPlayPauseListener;
import cn.wydewy.wyplayer.service.MediaBinder.OnPlayStartListener;
import cn.wydewy.wyplayer.service.MediaBinder.OnPlayingListener;
import cn.wydewy.wyplayer.service.MediaService;
import cn.wydewy.wyplayer.util.FormatUtil;
import cn.wydewy.wyplayer.util.ImageUtil;

/**
 * By wydewy 2013 Open Source Project
 * 
 * <br>
 * <b>播放详情页面</b></br>
 * 
 * 
 * @version 2013.06.30 v1.0 实现基本播放界面和可视化效果<br>
 *          2013.07.03 v1.1 实现按钮功能<br>
 *          2013.07.30 v1.2 实现对SeekBar监听，修复之前几处BUG<br>
 *          2013.08.13 v1.3 实现仿Path菜单<br>
 *          2013.08.16 v1.4 实现我的最爱按钮动画特效<br>
 *          2013.08.19 v1.5 实现快退、快进播放<br>
 *          2013.08.22 v1.6 将退出程序改为广播通知MainActivity，取缔原先的onActivityResult<br>
 *          2013.08.22 v1.6.1 修正仿Path菜单可以多次重复按的问题<br>
 *          2013.08.24 v1.7 实现更换背景图和歌词高亮色<br>
 *          2013.08.27 v2.0 实现横屏模式，修改横竖屏切换相关逻辑<br>
 *          2013.08.28 v2.1 实现专辑封面3D翻转动画<br>
 *          2013.08.30 v2.2 实现对系统媒体音量的设置</br>
 */
public class PlayerActivity extends Activity implements OnClickListener,
		OnLongClickListener, OnTouchListener, OnSeekBarChangeListener {

	private final String TIME_NORMAL = "00:00";
	private final int[] modeImage = { R.drawable.player_btn_mode_normal_style,
			R.drawable.player_btn_mode_repeat_one_style,
			R.drawable.player_btn_mode_repeat_all_style,
			R.drawable.player_btn_mode_random_style };

	private int skinId;// 背景图ID
	private int colorId;// 歌词高亮色值
	private int musicPosition;// 当前播放歌曲索引

	private boolean isFavorite = false;// 当前歌曲是否为最爱
	private boolean isPortraitActivity = true;// 当前是否为竖屏模式
	private boolean isFirstTransition3dAnimation = true;// 3D翻转动画只执行一次

	private ImageButton btnMode;// 播放模式按钮
	private ImageButton btnReturn;// 返回按钮
	private ImageButton btnPrevious;// 上一首按钮
	private ImageButton btnPlay;// 播放和暂停按钮
	private ImageButton btnNext;// 下一首按钮
	private ImageButton btnFavorite;// 我的最爱按钮
	private ImageButton menuButton;// 菜单(按钮)
	private ImageButton menuAbout;// 菜单(关于)
	private ImageButton menuInfo;// 菜单(歌曲详情)
	private ImageButton menuSetting;// 菜单(设置)
	private ImageButton menuExit;// 菜单(退出)

	private RelativeLayout skin;// 背景图
	private RelativeLayout menu;// 菜单

	private TextView currentTime;// 当前时间
	private TextView totalTime;// 总时间
	private SeekBar seekBar;// 进度条
	private SeekBar seekVolumeBar;// 音量进度条
	private PushView mp3Name;// 歌名
	private PushView mp3Info;// 歌曲信息集合
	private PushView mp3Artist;// 艺术家
	private ImageView mp3Cover;// 专辑图片
	private ImageView mp3Favorite;// 我的最爱动画图片
	private LyricView lyricView;// 歌词视图
	private PopupWindow popupVolume;
	private VisualizerView visualizer;// 均衡器视图

	private Intent playIntent;
	private MediaBinder binder;
	private MusicInfo musicInfo;
	private AudioManager audioManager;
	private SharedPreferences preferences;
	private ServiceConnection serviceConnection;

	private String mp3Duration = "00:00";// 总时间
	private PlayerBroadcastReceiver broadcastReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		init();// 初始化

		Intent intent = new Intent(MediaService.BROADCAST_ACTION_SERVICE);
		intent.putExtra(MediaService.INTENT_ACTIVITY,
				MediaService.ACTIVITY_PLAYER);
		sendBroadcast(intent);

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		int id1 = preferences.getInt(MainActivity.PREFERENCES_SKIN,
				R.drawable.skin_bg1);
		int id2 = preferences.getInt(MainActivity.PREFERENCES_LYRIC,
				Color.argb(250, 251, 248, 29));
		if (skinId != id1) {// 判断是否更换背景图
			skinId = id1;
			if (isPortraitActivity) {
				skin.setBackgroundResource(skinId);
			}
		}
		if (colorId != id2) {// 判断是否更改歌词高亮色
			colorId = id2;
			lyricView.setLyricHighlightColor(colorId);
		}

		broadcastReceiver = new PlayerBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(MainActivity.SET_MUSIC_DURATION);
		registerReceiver(broadcastReceiver, filter);

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		int id1 = preferences.getInt(MainActivity.PREFERENCES_SKIN,
				R.drawable.skin_bg1);
		int id2 = preferences.getInt(MainActivity.PREFERENCES_LYRIC,
				Color.argb(250, 251, 248, 29));
		if (skinId != id1) {// 判断是否更换背景图
			skinId = id1;
			if (isPortraitActivity) {
				skin.setBackgroundResource(skinId);
			}
		}
		if (colorId != id2) {// 判断是否更改歌词高亮色
			colorId = id2;
			lyricView.setLyricHighlightColor(colorId);
		}

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// 4.使用完后要记得释放
		if (broadcastReceiver != null) {
			unregisterReceiver(broadcastReceiver);
		}
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		if (serviceConnection != null) {
			unbindService(serviceConnection);// 一定要在finish之前解除绑定
			serviceConnection = null;
		}
		if (isPortraitActivity) {
			visualizer.releaseVisualizerFx();// 暂停更新音乐可视化界面动画
		}

		// 4.使用完后要记得释放
		if (broadcastReceiver != null) {
			unregisterReceiver(broadcastReceiver);
			broadcastReceiver = null;
		}
		
		
	}

	/*
	 * 在2.3.3的模拟器中会出现能切横屏但回不到竖屏的情况，4.0的模拟器正常。
	 * 
	 * 不知在2.3.3的手机中是否也会出现这种情况???
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);

		if (serviceConnection != null) {
			unbindService(serviceConnection);// 切换还是得解绑
			serviceConnection = null;
		}

		if (popupVolume.isShowing()) {
			popupVolume.dismiss();// 切换前消失音量框
		}

		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			isPortraitActivity = true;
			initPortraitActivity();// 重新初始化竖屏界面
		} else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			isPortraitActivity = false;
			if (visualizer != null) {
				visualizer.releaseVisualizerFx();// 释放音乐可视化界面
			}
			initLandscapeActivity();// 重新初始化横屏界面
		}
	}

	/**
	 * 初始化入口
	 */
	private void init() {
		musicPosition = getIntent().getIntExtra(
				MainActivity.BROADCAST_INTENT_POSITION, 0);
		preferences = getSharedPreferences(MainActivity.PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		skinId = preferences.getInt(MainActivity.PREFERENCES_SKIN,
				R.drawable.skin_bg1);
		colorId = preferences.getInt(MainActivity.PREFERENCES_LYRIC,
				Color.argb(250, 251, 248, 29));
		playIntent = new Intent(getApplicationContext(), MediaService.class);

		Configuration config = getResources().getConfiguration();
		if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
			isPortraitActivity = true;
			initPortraitActivity();// 初始化竖屏界面
		} else if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			isPortraitActivity = false;
			initLandscapeActivity();// 初始化横屏界面
		}
		initPopupVolume();// 初始化音量窗口
	}

	/**
	 * 初始化竖屏模式
	 */
	private void initPortraitActivity() {
		setContentView(R.layout.activity_player);

		// /////////////////////////// 初始化主界面 /////////////////////////// //
		skin = (RelativeLayout) findViewById(R.id.activity_player_skin);
		btnReturn = (ImageButton) findViewById(R.id.activity_player_ib_return);
		btnMode = (ImageButton) findViewById(R.id.activity_player_ib_mode);
		btnPrevious = (ImageButton) findViewById(R.id.activity_player_ib_previous);
		btnPlay = (ImageButton) findViewById(R.id.activity_player_ib_play);
		btnNext = (ImageButton) findViewById(R.id.activity_player_ib_next);
		btnFavorite = (ImageButton) findViewById(R.id.activity_player_ib_favorite);
		seekBar = (SeekBar) findViewById(R.id.activity_player_seek);
		currentTime = (TextView) findViewById(R.id.activity_player_tv_time_current);
		totalTime = (TextView) findViewById(R.id.activity_player_tv_time_total);
		mp3Name = (PushView) findViewById(R.id.activity_player_tv_name);
		mp3Info = (PushView) findViewById(R.id.activity_player_tv_info);
		mp3Artist = (PushView) findViewById(R.id.activity_player_tv_artist);
		mp3Cover = (ImageView) findViewById(R.id.activity_player_cover);
		mp3Favorite = (ImageView) findViewById(R.id.activity_player_iv_favorite);
		lyricView = (LyricView) findViewById(R.id.activity_player_lyric);
		visualizer = (VisualizerView) findViewById(R.id.activity_player_visualizer);

		currentTime.setText(TIME_NORMAL);
		totalTime.setText(TIME_NORMAL);
		btnReturn.setOnClickListener(this);
		btnMode.setOnClickListener(this);
		btnPrevious.setOnClickListener(this);
		btnPlay.setOnClickListener(this);
		btnNext.setOnClickListener(this);
		btnFavorite.setOnClickListener(this);
		mp3Cover.setOnClickListener(this);
		btnPrevious.setOnLongClickListener(this);
		btnNext.setOnLongClickListener(this);
		btnPrevious.setOnTouchListener(this);
		btnNext.setOnTouchListener(this);
		seekBar.setOnSeekBarChangeListener(this);

		btnMode.setImageResource(modeImage[preferences.getInt(
				MainActivity.PREFERENCES_MODE, 0)]);
		skin.setBackgroundResource(skinId);
		lyricView.setLyricHighlightColor(colorId);
		// /////////////////////////// 初始化主界面 /////////////////////////// //

		// 接收广播

		// ////////////////////////// 初始化服务绑定 ////////////////////////// //
		serviceConnection = new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName name) {
				// TODO Auto-generated method stub
				binder = null;
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				// TODO Auto-generated method stub
				binder = (MediaBinder) service;
				if (binder != null) {
					binder.setOnPlayStartListener(new OnPlayStartListener() {

						@Override
						public void onStart(MusicInfo info) {
							// TODO Auto-generated method stub
							mp3Name.setText(info.getName());
							currentTime.setText(TIME_NORMAL);
							totalTime.setText(mp3Duration);
							seekBar.setMax(info.getMp3Duration());
							// 启动更新音乐可视化界面动画
							visualizer.setupVisualizerFx(info
									.getAudioSessionId());
							ArrayList<String> list = new ArrayList<String>();
							list.add(info.getFormat());
							list.add("大小: " + info.getSize());
							list.add(info.getGenre());
							list.add(info.getAlbum());
							list.add(info.getYears());
							list.add(info.getChannels());
							list.add(info.getKbps());
							list.add(info.getHz());
							mp3Info.setTextList(list);
							mp3Artist.setText(info.getArtist());
							isFirstTransition3dAnimation = true;
							if (CoverList.cover == null) {
								startTransition3dAnimation(BitmapFactory
										.decodeResource(getResources(),
												R.drawable.player_cover_default));
							} else {
								startTransition3dAnimation(CoverList.cover);
							}
							btnPlay.setImageResource(R.drawable.player_btn_pause_style);
							isFavorite = info.isFavorite();
							btnFavorite
									.setImageResource(isFavorite ? R.drawable.player_btn_favorite_star_style
											: R.drawable.player_btn_favorite_nostar_style);
							musicInfo = info;
						}
					});
					binder.setOnPlayingListener(new OnPlayingListener() {

						@Override
						public void onPlay(int currentPosition) {
							// TODO Auto-generated method stub
							seekBar.setProgress(currentPosition);
							currentTime.setText(FormatUtil
									.formatTime(currentPosition));
						}
					});
					binder.setOnPlayPauseListener(new OnPlayPauseListener() {

						@Override
						public void onPause() {
							// TODO Auto-generated method stub
							btnPlay.setImageResource(R.drawable.player_btn_play_style);
						}
					});
					binder.setOnPlayCompletionListener(new OnPlayCompleteListener() {

						@Override
						public void onPlayComplete() {
							// TODO Auto-generated method stub

						}
					});
					binder.setOnPlayErrorListener(new OnPlayErrorListener() {

						@Override
						public void onPlayError() {
							// TODO Auto-generated method stub
							// 这里若文件不存在也应执行从列表移除操作，不想写了。可以发个广播过去！
						}
					});
					binder.setOnModeChangeListener(new OnModeChangeListener() {

						@Override
						public void onModeChange(int mode) {
							// TODO Auto-generated method stub
							btnMode.setImageResource(modeImage[mode]);
						}
					});
					binder.setLyricView(lyricView, true);// 设置歌词视图，是卡拉OK模式
				}
			}
		};
		bindService(playIntent, serviceConnection, Context.BIND_AUTO_CREATE);
		// ////////////////////////// 初始化服务绑定 ////////////////////////// //

		initPathMenu();// 初始化菜单
	}

	/**
	 * 初始化横屏模式，注意加上android:configChanges="keyboard|keyboardHidden|orientation"，
	 * 这样切换后就只会执行onConfigurationChanged方法。
	 * 
	 * 其实横竖屏模式id可以写成一样的，就无需重复绑定，不过我不想去改了。
	 */
	private void initLandscapeActivity() {
		setContentView(R.layout.activity_player_landscape);

		// /////////////////////////// 初始化主界面 /////////////////////////// //
		mp3Name = (PushView) findViewById(R.id.activity_player_landscape_tv_list);
		btnPrevious = (ImageButton) findViewById(R.id.activity_player_landscape_ib_previous);
		btnPlay = (ImageButton) findViewById(R.id.activity_player_landscape_ib_play);
		btnNext = (ImageButton) findViewById(R.id.activity_player_landscape_ib_next);
		mp3Cover = (ImageView) findViewById(R.id.activity_player_landscape_cover);
		lyricView = (LyricView) findViewById(R.id.activity_player_landscape_lyric);
		lyricView.setLyricHighlightColor(colorId);

		btnPrevious.setOnClickListener(this);
		btnPlay.setOnClickListener(this);
		btnNext.setOnClickListener(this);
		mp3Cover.setOnClickListener(this);
		btnPrevious.setOnLongClickListener(this);
		btnNext.setOnLongClickListener(this);
		btnPrevious.setOnTouchListener(this);
		btnNext.setOnTouchListener(this);
		// /////////////////////////// 初始化主界面 /////////////////////////// //

		// ////////////////////////// 初始化服务绑定 ////////////////////////// //
		serviceConnection = new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName name) {
				// TODO Auto-generated method stub
				binder = null;
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				// TODO Auto-generated method stub
				binder = (MediaBinder) service;
				if (binder != null) {
					binder.setOnPlayStartListener(new OnPlayStartListener() {

						@Override
						public void onStart(MusicInfo info) {
							// TODO Auto-generated method stub
							ArrayList<String> list = new ArrayList<String>();
							list.add(info.getName());
							list.add(info.getArtist());
							list.add(info.getFormat());
							list.add("大小: " + info.getSize());
							list.add(info.getGenre());
							list.add(info.getAlbum());
							list.add(info.getYears());
							list.add(info.getChannels());
							list.add(info.getKbps());
							list.add(info.getHz());
							mp3Name.setTextList(list);

							Bitmap bitmap = null;
							isFirstTransition3dAnimation = true;
							if (CoverList.cover == null) {
								bitmap = BitmapFactory
										.decodeResource(
												getResources(),
												R.drawable.player_landscape_cover_default);
								bitmap = ImageUtil
										.createReflectionBitmap(bitmap);
							} else {
								bitmap = ImageUtil
										.createReflectionBitmap(CoverList.cover);
							}
							startTransition3dAnimation(bitmap);
							btnPlay.setBackgroundResource(R.drawable.player_landscape_btn_pause_style);
						}
					});
					binder.setOnPlayPauseListener(new OnPlayPauseListener() {

						@Override
						public void onPause() {
							// TODO Auto-generated method stub
							btnPlay.setBackgroundResource(R.drawable.player_landscape_btn_play_style);
						}
					});
					binder.setOnPlayErrorListener(new OnPlayErrorListener() {

						@Override
						public void onPlayError() {
							// TODO Auto-generated method stub
							// 这里若文件不存在也应执行从列表移除操作，不想写了。可以发个广播过去！
						}
					});
					binder.setLyricView(lyricView, false);// 设置歌词视图，滚动模式
				}
			}
		};
		bindService(playIntent, serviceConnection, Context.BIND_AUTO_CREATE);
		// ////////////////////////// 初始化服务绑定 ////////////////////////// //
	}

	/**
	 * 初始化菜单相关
	 */
	private void initPathMenu() {
		menu = (RelativeLayout) findViewById(R.id.activity_player_menu);
		menuButton = (ImageButton) findViewById(R.id.activity_player_ib_menu);
		menuAbout = (ImageButton) findViewById(R.id.activity_player_ib_menu_about);
		menuInfo = (ImageButton) findViewById(R.id.activity_player_ib_menu_info);
		menuSetting = (ImageButton) findViewById(R.id.activity_player_ib_menu_setting);
		menuExit = (ImageButton) findViewById(R.id.activity_player_ib_menu_exit);

		menuButton.setOnClickListener(this);
		menuAbout.setOnClickListener(this);
		menuInfo.setOnClickListener(this);
		menuSetting.setOnClickListener(this);
		menuExit.setOnClickListener(this);
		menu.setOnTouchListener(this);
	}

	/**
	 * 初始化音量窗口相关
	 */
	@SuppressWarnings("deprecation")
	private void initPopupVolume() {
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		View view = LayoutInflater.from(this).inflate(R.layout.popup_volume,
				null);// 引入窗口配置文件
		popupVolume = new PopupWindow(view, LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT, false);// 创建PopupWindow对象
		seekVolumeBar = (SeekBar) view.findViewById(R.id.pupup_volume_seek);
		seekVolumeBar.setMax(audioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		seekVolumeBar.setOnSeekBarChangeListener(this);
		popupVolume.setBackgroundDrawable(new BitmapDrawable());// 点击外边可消失
		popupVolume.setOutsideTouchable(true);// 设置点击窗口外边窗口消失
	}

	/**
	 * 弹出关于对话框
	 */
	private void showAboutDialog() {
		AboutDialog aboutDialog = new AboutDialog(this);
		aboutDialog.show();
	}

	/**
	 * 弹出歌曲详情对话框
	 */
	private void showInfoDialog() {
		if (musicInfo != null) {
			InfoDialog infoDialog = new InfoDialog(this);
			infoDialog.show();
			infoDialog.setInfo(musicInfo);
		} else {
			Toast.makeText(getApplicationContext(), "尚无歌曲播放",
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 过滤按钮事件
	 * 
	 * @param enabled
	 *            true/false
	 */
	private void setMenuEnabled(boolean enabled) {
		menuButton.setEnabled(enabled);
		menuAbout.setEnabled(enabled);
		menuInfo.setEnabled(enabled);
		menuSetting.setEnabled(enabled);
		menuExit.setEnabled(enabled);
	}

	/**
	 * 回退事件
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			// 此处写处理的事件
			// startActivity(new Intent(this, MainActivity.class));
			this.finish();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.activity_player_ib_return:// 返回按钮监听
			// startActivity(new Intent(this, MainActivity.class));
			finish();
			break;

		case R.id.activity_player_ib_mode:// 播放模式切换按钮监听
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_MODE);
			}
			break;

		case R.id.activity_player_ib_previous:// 上一首按钮监听
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_PREVIOUS);
			}
			break;

		case R.id.activity_player_ib_play:// 播放按钮监听
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_PLAY);
			}
			break;

		case R.id.activity_player_ib_next:// 下一首按钮监听
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_NEXT);
			}
			break;

		case R.id.activity_player_ib_favorite:// 我的最爱按钮监听
			if (seekBar.getMax() > 0) {// 此判断表示播放过歌曲
				Intent intent = new Intent(
						MainActivity.BROADCAST_ACTION_FAVORITE);
				intent.putExtra(MainActivity.BROADCAST_INTENT_POSITION,
						musicPosition);// 只传Position过去
				sendBroadcast(intent);
				if (isFavorite) {// 因为执行更改我的最爱不一定成功，本应通过发送回执来更改这边的状态。
					btnFavorite
							.setImageResource(R.drawable.player_btn_favorite_nostar_style);
					isFavorite = false;
				} else {// 可能很复杂，不想写了...(频繁点击可能要出问题)
					btnFavorite
							.setImageResource(R.drawable.player_btn_favorite_star_style);
					startFavoriteImageAnimation();// 现通过动画来阻止频繁点击
					isFavorite = true;
				}
			}
			break;

		case R.id.activity_player_cover:// 弹出音量调节窗口
			seekVolumeBar.setProgress(audioManager
					.getStreamVolume(AudioManager.STREAM_MUSIC));
			popupVolume.showAsDropDown(mp3Name);
			break;

		case R.id.activity_player_ib_menu:// 关闭菜单
			startMenuRotateAnimationOut(R.id.activity_player_ib_menu);
			break;

		case R.id.activity_player_ib_menu_about:// 弹出关于本软件
			menuAbout.startAnimation(startAmplifyIconAnimation());
			menuInfo.startAnimation(startShrinkIconAnimation());
			menuSetting.startAnimation(startShrinkIconAnimation());
			menuExit.startAnimation(startShrinkIconAnimation());
			startMenuRotateAnimationOut(R.id.activity_player_ib_menu_about);
			break;

		case R.id.activity_player_ib_menu_info:// 弹出歌曲详情
			menuInfo.startAnimation(startAmplifyIconAnimation());
			menuAbout.startAnimation(startShrinkIconAnimation());
			menuSetting.startAnimation(startShrinkIconAnimation());
			menuExit.startAnimation(startShrinkIconAnimation());
			startMenuRotateAnimationOut(R.id.activity_player_ib_menu_info);
			break;

		case R.id.activity_player_ib_menu_setting:// 跳转设置界面
			menuSetting.startAnimation(startAmplifyIconAnimation());
			menuAbout.startAnimation(startShrinkIconAnimation());
			menuInfo.startAnimation(startShrinkIconAnimation());
			menuExit.startAnimation(startShrinkIconAnimation());
			startMenuRotateAnimationOut(R.id.activity_player_ib_menu_setting);
			break;

		case R.id.activity_player_ib_menu_exit:// 退出
			menuExit.startAnimation(startAmplifyIconAnimation());
			menuAbout.startAnimation(startShrinkIconAnimation());
			menuInfo.startAnimation(startShrinkIconAnimation());
			menuSetting.startAnimation(startShrinkIconAnimation());
			startMenuRotateAnimationOut(R.id.activity_player_ib_menu_exit);
			break;

		case R.id.activity_player_landscape_ib_previous:// 横屏模式上一首按钮监听
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_PREVIOUS);
			}
			break;

		case R.id.activity_player_landscape_ib_play:// 横屏模式播放按钮监听
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_PLAY);
			}
			break;

		case R.id.activity_player_landscape_ib_next:// 横屏模式下一首按钮监听
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_NEXT);
			}
			break;

		case R.id.activity_player_landscape_cover:// 弹出音量调节窗口
			seekVolumeBar.setProgress(audioManager
					.getStreamVolume(AudioManager.STREAM_MUSIC));
			popupVolume.showAsDropDown(mp3Name);
			break;
		}
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.activity_player_ib_previous:// 竖屏模式快退
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_REWIND);
			}
			break;

		case R.id.activity_player_ib_next:// 竖屏模式快进
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_FORWARD);
			}
			break;

		case R.id.activity_player_landscape_ib_previous:// 横屏模式快退
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_FORWARD);
			}
			break;

		case R.id.activity_player_landscape_ib_next:// 横屏模式快进
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_FORWARD);
			}
			break;
		}
		return true;// 返回true，不准再执行onClick
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.activity_player_menu:
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (menuButton.isShown()) {
					if (menuButton.isEnabled()) {
						startMenuRotateAnimationOut(R.id.activity_player_ib_menu);
					}
					return true;// 类似触摸Dialog以外的区域让其消失
				} else if (mp3Favorite.isShown()) {
					return true;// 动画执行屏蔽所有事件
				}
			}
			break;

		case R.id.activity_player_ib_previous:// 竖屏模式松手播放
			if (binder != null && event.getAction() == MotionEvent.ACTION_UP) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_REPLAY);
			}
			break;

		case R.id.activity_player_ib_next:// 竖屏模式松手播放
			if (binder != null && event.getAction() == MotionEvent.ACTION_UP) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_REPLAY);
			}
			break;

		case R.id.activity_player_landscape_ib_previous:// 横屏模式松手播放
			if (binder != null && event.getAction() == MotionEvent.ACTION_UP) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_REPLAY);
			}
			break;

		case R.id.activity_player_landscape_ib_next:// 横屏模式松手播放
			if (binder != null && event.getAction() == MotionEvent.ACTION_UP) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_REPLAY);
			}
			break;
		}
		return false;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		switch (seekBar.getId()) {
		case R.id.activity_player_seek:
			if (fromUser && seekBar.getMax() > 0) {
				currentTime.setText(FormatUtil.formatTime(progress));
			}
			break;

		case R.id.pupup_volume_seek:
			if (fromUser) {
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
						progress, 0);
			}
			break;
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		if (seekBar.getId() == R.id.activity_player_seek) {
			if (binder != null) {
				binder.seekBarStartTrackingTouch();
			}
		}
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		if (seekBar.getId() == R.id.activity_player_seek) {
			if (binder != null) {
				binder.seekBarStopTrackingTouch(seekBar.getProgress());
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (isPortraitActivity) {// 只有竖屏模式才执行
			if (keyCode == KeyEvent.KEYCODE_MENU) {
				if (popupVolume.isShowing()) {
					popupVolume.dismiss();// 检查音量框
				}
				if (menuButton.isShown()) {
					if (menuButton.isEnabled()) {
						startMenuRotateAnimationOut(R.id.activity_player_ib_menu);
					}
				} else {
					startMenuRotateAnimationIn();
				}
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_BACK) {
				if (menuButton.isShown()) {
					if (menuButton.isEnabled()) {
						startMenuRotateAnimationOut(R.id.activity_player_ib_menu);
					}
					return true;
				}
			}
		}
		if (popupVolume.isShowing()) {
			if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {// 跟随音量键调节
				seekVolumeBar.setProgress(seekVolumeBar.getProgress() + 1);
			} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
				seekVolumeBar.setProgress(seekVolumeBar.getProgress() - 1);
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 菜单展开动画
	 */
	private void startMenuRotateAnimationIn() {
		AnimationSet animationSet = new AnimationSet(true);
		RotateAnimation rotateAnimation = new RotateAnimation(0.0f, 360.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		Animation alphaAnimation = new AlphaAnimation(0.2f, 1.0f);

		animationSet.addAnimation(rotateAnimation);
		animationSet.addAnimation(alphaAnimation);
		animationSet.setDuration(500);
		animationSet.setFillAfter(true);
		animationSet.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				setMenuEnabled(true);
			}
		});

		menuButton.setVisibility(View.VISIBLE);
		menuButton.startAnimation(animationSet);
		startIconTranslateAnimationIn();
	}

	/**
	 * 菜单关闭动画
	 * 
	 * @param id
	 *            ButtonId
	 */
	private void startMenuRotateAnimationOut(final int id) {
		setMenuEnabled(false);
		AnimationSet animationSet = new AnimationSet(true);
		RotateAnimation rotateAnimation = new RotateAnimation(0.0f, -360.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		Animation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);

		animationSet.addAnimation(rotateAnimation);
		animationSet.addAnimation(alphaAnimation);
		animationSet.setDuration(500);
		animationSet.setFillAfter(true);

		animationSet.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				menuButton.setVisibility(View.GONE);
				menuButton.setClickable(false);
				menuButton.setFocusable(false);
				switch (id) {
				case R.id.activity_player_ib_menu_about:// 显示关于
					showAboutDialog();
					break;

				case R.id.activity_player_ib_menu_info:// 显示歌曲详情
					showInfoDialog();
					break;

				case R.id.activity_player_ib_menu_setting:// 跳转设置界面
					Intent intent = new Intent(getApplicationContext(),
							SettingActivity.class);
					startActivity(intent);
					break;

				case R.id.activity_player_ib_menu_exit:// 退出程序
					sendBroadcast(new Intent(MainActivity.BROADCAST_ACTION_EXIT));
					finish();
					break;
				}
			}
		});
		menuButton.startAnimation(animationSet);
		if (id == R.id.activity_player_ib_menu) {
			startIconTranslateAnimationOut();
		}
	}

	/**
	 * 图标的动画(入动画)
	 */
	private void startIconTranslateAnimationIn() {
		final int w = menuButton.getWidth() / 2;
		final int h = menuButton.getHeight() / 2;
		for (int i = 1; i < 5; i++) {// 从1开始目的是过滤掉menuButton
			ImageButton imageButton = (ImageButton) menu.getChildAt(i);
			imageButton.setVisibility(View.VISIBLE);
			MarginLayoutParams params = (MarginLayoutParams) imageButton
					.getLayoutParams();

			AnimationSet animationset = new AnimationSet(false);
			RotateAnimation rotateAnimation = new RotateAnimation(0.0f,
					-360.0f, Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			rotateAnimation.setInterpolator(new LinearInterpolator());
			Animation alphaAnimation = new AlphaAnimation(0.1f, 1.0f);
			TranslateAnimation translateAnimation = null;
			switch (i) {
			case 1:
				translateAnimation = new TranslateAnimation(params.rightMargin
						+ w, 0.0f, 0.0f, 0.0f);
				break;

			case 2:
				translateAnimation = new TranslateAnimation(0.0f, 0.0f,
						params.bottomMargin + h, 0.0f);
				break;

			case 3:
				translateAnimation = new TranslateAnimation(-params.leftMargin
						- w, 0.0f, 0.0f, 0.0f);
				break;

			case 4:
				translateAnimation = new TranslateAnimation(0.0f, 0.0f,
						-params.topMargin - h, 0.0f);
				break;
			}
			translateAnimation.setInterpolator(new OvershootInterpolator(2F));// 弹出再回来的动画的效果

			animationset.addAnimation(rotateAnimation);// 旋转动画必须比位移动画先执行
			animationset.addAnimation(alphaAnimation);
			animationset.addAnimation(translateAnimation);// 自己改改顺序就知道为什么
			animationset.setDuration(500);
			animationset.setFillAfter(true);

			imageButton.startAnimation(animationset);
		}
	}

	/**
	 * 图标的动画(出动画)
	 */
	private void startIconTranslateAnimationOut() {
		final int w = menuButton.getWidth() / 2;
		final int h = menuButton.getHeight() / 2;
		for (int i = 1; i < 5; i++) {// 从1开始目的是过滤掉menuButton
			final ImageButton imageButton = (ImageButton) menu.getChildAt(i);
			MarginLayoutParams params = (MarginLayoutParams) imageButton
					.getLayoutParams();

			AnimationSet animationset = new AnimationSet(false);
			RotateAnimation rotateAnimation = new RotateAnimation(360.0f, 0.0f,
					Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			rotateAnimation.setInterpolator(new LinearInterpolator());
			Animation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
			TranslateAnimation translateAnimation = null;

			switch (i) {
			case 1:
				translateAnimation = new TranslateAnimation(0.0f,
						params.rightMargin + w, 0.0f, 0.0f);
				break;

			case 2:
				translateAnimation = new TranslateAnimation(0.0f, 0.0f, 0.0f,
						params.bottomMargin + h);
				break;

			case 3:
				translateAnimation = new TranslateAnimation(0f,
						-params.leftMargin - w, 0.0f, 0.0f);
				break;

			case 4:
				translateAnimation = new TranslateAnimation(0.0f, 0.0f, 0.0f,
						-params.topMargin - h);
				break;
			}

			animationset.addAnimation(rotateAnimation);// 旋转动画必须比位移动画先执行
			animationset.addAnimation(alphaAnimation);
			animationset.addAnimation(translateAnimation);// 自己改改顺序就知道为什么
			animationset.setDuration(500);
			animationset.setFillAfter(true);

			animationset.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stub
					imageButton.setVisibility(View.GONE);
				}
			});
			imageButton.startAnimation(animationset);
		}
	}

	/**
	 * icon缩小消失的动画
	 */
	private Animation startShrinkIconAnimation() {
		ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f,
				0.0f, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		scaleAnimation.setDuration(300);
		scaleAnimation.setFillAfter(true);
		return scaleAnimation;
	}

	/**
	 * icon放大渐变消失的动画
	 */
	private Animation startAmplifyIconAnimation() {
		AnimationSet animationset = new AnimationSet(true);

		ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 4.0f, 1.0f,
				4.0f, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);

		animationset.addAnimation(scaleAnimation);
		animationset.addAnimation(alphaAnimation);
		animationset.setDuration(300);
		animationset.setFillAfter(true);

		return animationset;
	}

	/**
	 * 我的最爱图片动画
	 */
	private void startFavoriteImageAnimation() {
		AnimationSet animationset = new AnimationSet(false);

		ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f,
				1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		scaleAnimation.setInterpolator(new OvershootInterpolator(5F));// 弹出再回来的动画的效果
		scaleAnimation.setDuration(700);
		AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
		alphaAnimation.setDuration(500);
		alphaAnimation.setStartOffset(700);

		animationset.addAnimation(scaleAnimation);
		animationset.addAnimation(alphaAnimation);
		animationset.setFillAfter(true);

		animationset.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				mp3Favorite.setVisibility(View.GONE);
			}
		});
		mp3Favorite.setVisibility(View.VISIBLE);
		mp3Favorite.startAnimation(animationset);
	}

	/**
	 * 专辑封面翻转动画
	 * 
	 * @param bitmap
	 *            专辑封面图
	 */
	private void startTransition3dAnimation(final Bitmap bitmap) {
		final int w = mp3Cover.getWidth() / 2;
		final int h = mp3Cover.getHeight() / 2;
		final MarginLayoutParams params = (MarginLayoutParams) mp3Cover
				.getLayoutParams();

		final Rotate3dAnimation rotation1 = new Rotate3dAnimation(0.0f, 90.0f,
				params.leftMargin + w, params.topMargin + h, 300.0f, true);
		rotation1.setDuration(500);
		rotation1.setFillAfter(true);
		rotation1.setInterpolator(new AccelerateInterpolator());

		final Rotate3dAnimation rotation2 = new Rotate3dAnimation(270.0f,
				360.0f, params.leftMargin + w, params.topMargin + h, 300.0f,
				false);// 反转动画
		rotation2.setDuration(500);
		rotation2.setFillAfter(true);
		rotation2.setInterpolator(new AccelerateInterpolator());

		rotation1.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				if (isFirstTransition3dAnimation) {
					isFirstTransition3dAnimation = false;
					mp3Cover.setImageBitmap(bitmap);
					mp3Cover.startAnimation(rotation2);
				}
			}
		});
		mp3Cover.startAnimation(rotation1);
	}

	/**
	 * 移植于ApiDemos里的的Rotate3dAnimation
	 * 
	 * 这里有个bug，180度翻转图片会反过来，所以只能折中的旋转一半又反转回来，效果就差了，交给各位去完善了
	 */
	private class Rotate3dAnimation extends Animation {
		private final float mFromDegrees;
		private final float mToDegrees;
		private final float mCenterX;
		private final float mCenterY;
		private final float mDepthZ;
		private final boolean mReverse;
		private Camera mCamera;

		/**
		 * Creates a new 3D rotation on the Y axis. The rotation is defined by
		 * its start angle and its end angle. Both angles are in degrees. The
		 * rotation is performed around a center point on the 2D space, definied
		 * by a pair of X and Y coordinates, called centerX and centerY. When
		 * the animation starts, a translation on the Z axis (depth) is
		 * performed. The length of the translation can be specified, as well as
		 * whether the translation should be reversed in time.
		 * 
		 * @param fromDegrees
		 *            the start angle of the 3D rotation
		 * @param toDegrees
		 *            the end angle of the 3D rotation
		 * @param centerX
		 *            the X center of the 3D rotation
		 * @param centerY
		 *            the Y center of the 3D rotation
		 * @param reverse
		 *            true if the translation should be reversed, false
		 *            otherwise
		 */
		public Rotate3dAnimation(float fromDegrees, float toDegrees,
				float centerX, float centerY, float depthZ, boolean reverse) {
			mFromDegrees = fromDegrees;
			mToDegrees = toDegrees;
			mCenterX = centerX;
			mCenterY = centerY;
			mDepthZ = depthZ;
			mReverse = reverse;
		}

		@Override
		public void initialize(int width, int height, int parentWidth,
				int parentHeight) {
			super.initialize(width, height, parentWidth, parentHeight);
			mCamera = new Camera();
		}

		@Override
		protected void applyTransformation(float interpolatedTime,
				Transformation t) {
			final float fromDegrees = mFromDegrees;
			float degrees = fromDegrees
					+ ((mToDegrees - fromDegrees) * interpolatedTime);

			final float centerX = mCenterX;
			final float centerY = mCenterY;
			final Camera camera = mCamera;

			final Matrix matrix = t.getMatrix();

			camera.save();
			if (mReverse) {
				camera.translate(0.0f, 0.0f, mDepthZ * interpolatedTime);
			} else {
				camera.translate(0.0f, 0.0f, mDepthZ
						* (1.0f - interpolatedTime));
			}
			camera.rotateY(degrees);
			camera.getMatrix(matrix);
			camera.restore();

			matrix.preTranslate(-centerX, -centerY);
			matrix.postTranslate(centerX, centerY);
		}
	}

	/**
	 * 
	 * @author weiyideweiyi8
	 * 
	 */

	public class PlayerBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if (MainActivity.SET_MUSIC_DURATION.equals(action)) {
				mp3Duration = FormatUtil.formatTime(intent.getExtras().getInt(
						"mp3Duration"));
				totalTime.setText(mp3Duration);
			}

		}

	}

}
