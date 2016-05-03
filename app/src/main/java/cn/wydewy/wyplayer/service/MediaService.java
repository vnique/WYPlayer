package cn.wydewy.wyplayer.service;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import cn.wydewy.wyplayer.MainActivity;
import cn.wydewy.wyplayer.Mp3Application;
import cn.wydewy.wyplayer.PlayerActivity;
import cn.wydewy.wyplayer.ScanActivity;
import cn.wydewy.wyplayer.SettingActivity;
import cn.wydewy.wyplayer.entity.LyricInfo;
import cn.wydewy.wyplayer.entity.MusicInfo;
import cn.wydewy.wyplayer.list.CoverList;
import cn.wydewy.wyplayer.list.FavoriteList;
import cn.wydewy.wyplayer.list.FolderList;
import cn.wydewy.wyplayer.list.LyricList;
import cn.wydewy.wyplayer.list.MusicList;
import cn.wydewy.wyplayer.list.OnlineMusicList;
import cn.wydewy.wyplayer.lyric.LyricItem;
import cn.wydewy.wyplayer.lyric.LyricParser;
import cn.wydewy.wyplayer.lyric.LyricView;
import cn.wydewy.wyplayer.service.MediaBinder.OnServiceBinderListener;
import cn.wydewy.wyplayer.util.AlbumUtil;
import cn.wydewy.wyplayer.util.Constant;

/**
 * By wydewy 2013 Open Source Project
 * 
 * <br>
 * <b>控制播放服务</b></br>
 * 
 * @author wydewy
 * @version 2013.06.23 v1.0 实现基础播放服务，通过接口与外界通信，引入歌词的解析处理 <br>
 *          2013.07.03 v1.1 处理BindService等bug的衔接的问题<br>
 *          2013.07.24 v1.2 实现对音乐的控制播放(播放、暂停、上下首切换等)<br>
 *          2013.08.06 v1.3 支持对文件夹列表歌曲的播放<br>
 *          2013.08.19 v1.4 支持快退、快进播放<br>
 *          2013.08.20 v1.5 实现Notification跳转能够回到原来的界面，加上launchMode="singleTask"<br>
 *          2013.08.27 v1.6 修复暂停后重新播放歌词消失的问题<br>
 *          2013.08.29 v1.7 实现耳机线控及来电监听</br>
 */
public class MediaService extends Service {

	public static final int CONTROL_COMMAND_PLAY = 0;// 控制命令：播放或者暂停
	public static final int CONTROL_COMMAND_PREVIOUS = 1;// 控制命令：上一首
	public static final int CONTROL_COMMAND_NEXT = 2;// 控制命令：下一首
	public static final int CONTROL_COMMAND_MODE = 3;// 控制命令：播放模式切换
	public static final int CONTROL_COMMAND_REWIND = 4;// 控制命令：快退
	public static final int CONTROL_COMMAND_FORWARD = 5;// 控制命令：快进
	public static final int CONTROL_COMMAND_REPLAY = 6;// 控制命令：用于快退、快进后的继续播放

	public static final int ACTIVITY_SCAN = 0x101;// 扫描界面
	public static final int ACTIVITY_MAIN = 0x102;// 主界面
	public static final int ACTIVITY_PLAYER = 0x103;// 播放界面
	public static final int ACTIVITY_SETTING = 0x104;// 设置界面

	public static final String INTENT_ACTIVITY = "activity";// 区分来自哪个界面
	public static final String INTENT_LIST_PAGE = "list_page";// 列表页面
	public static final String INTENT_LIST_POSITION = "list_position";// 列表当前项
	public static final String INTENT_FOLDER_POSITION = "folder_position";// 文件夹列表当前项
	public static final String BROADCAST_ACTION_SERVICE = "cn.wydewy.wyplayer.action.service";// 广播标志

	private static final int MEDIA_PLAY_ERROR = 0;
	private static final int MEDIA_PLAY_START = 1;
	private static final int MEDIA_PLAY_UPDATE = 2;
	private static final int MEDIA_PLAY_COMPLETE = 3;
	private static final int MEDIA_PLAY_UPDATE_LYRIC = 4;
	private static final int MEDIA_PLAY_REWIND = 5;
	private static final int MEDIA_PLAY_FORWARD = 6;
	private static final int MEDIA_BUTTON_ONE_CLICK = 7;
	private static final int MEDIA_BUTTON_DOUBLE_CLICK = 8;

	private final int MODE_NORMAL = 0;// 顺序播放，放到最后一首停止
	private final int MODE_REPEAT_ONE = 1;// 单曲循环
	private final int MODE_REPEAT_ALL = 2;// 全部循环
	private final int MODE_RANDOM = 3;// 随即播放
	private final int UPDATE_LYRIC_TIME = 150;// 歌词更新间隔0.15秒
	private final int UPDATE_UI_TIME = 1000;// UI更新间隔1秒

	private MusicInfo info;// 歌曲详情
	private List<LyricItem> lyricList;// 歌词列表
	private List<Integer> positionList;// 列表当前项集合，目的记住前面所播放的所有歌曲，类似天天动听

	private String mp3Path;// mp3文件路径
	private String lyricPath;// 歌词文件路径
	private LyricInfo lyricInfo;// 歌词文件路径

	private int mode = MODE_NORMAL;// 播放模式(默认顺序播放)
	private int page = MainActivity.SLIDING_MENU_ALL;// 列表页面(默认全部歌曲)
	private int lastPage = 0;// 记住上一次的列表页面
	private int position = 0;// 列表当前项
	private int folderPosition = 0;// 文件夹列表当前项
	private int mp3Current = 0;// 歌曲当前时间
	private int mp3Duration = 0;// 歌曲总时间
	private int buttonClickCounts = 0;

	private boolean hasLyric = false;// 是否有歌词
	private boolean isCommandPrevious = false;// 是否属于上一首操作命令

	private MediaPlayer mediaPlayer;
	private MediaBinder mBinder;
	private AlbumUtil albumUtil;
	private LyricView lyricView;

	private ServiceHandler mHandler;
	private ServiceReceiver receiver;
	/** NotificationCompat 构造器 */
	private SharedPreferences preferences;

	/** 通知栏按钮点击事件对应的ACTION */
	public final static String ACTION_BUTTON = "com.notifications.intent.action.ButtonClick";
	public final static String INTENT_BUTTONID_TAG = "ButtonId";

	private Mp3Application application;

	@SuppressLint("InlinedApi")
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		// 初始化音乐监听器
		application = (Mp3Application) getApplication();

		albumUtil = new AlbumUtil();
		lyricList = new ArrayList<LyricItem>();
		positionList = new ArrayList<Integer>();

		preferences = getSharedPreferences(MainActivity.PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		mode = preferences.getInt(MainActivity.PREFERENCES_MODE, MODE_NORMAL);// 取出上次的播放模式

		mediaPlayer = new MediaPlayer();
		mHandler = new ServiceHandler(this);
		mBinder = new MediaBinder();
		mediaPlayer.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				// TODO Auto-generated method stub
				mp.start();
				mp3Current = 0;// 重置
				prepared();// 准备播放
			}
		});
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				removeAllMsg();// 移除所有消息
				mHandler.sendEmptyMessage(MEDIA_PLAY_COMPLETE);
			}
		});
		mediaPlayer.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				// TODO Auto-generated method stub
				removeAllMsg();// 移除所有消息
				mp.reset();
				page = MainActivity.SLIDING_MENU_ALL;
				MusicInfo info = MusicList.list.get(position);

				position = 0;
				File file = new File(mp3Path);
				if (file.exists()) {
					Toast.makeText(getApplicationContext(), "播放出错",
							Toast.LENGTH_SHORT).show();
				} else {
					if ("".equals(info.getUrl())) {
						Toast.makeText(getApplicationContext(), "网络不给力！",
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(getApplicationContext(), "文件已不存在",
								Toast.LENGTH_SHORT).show();
						mHandler.sendEmptyMessage(MEDIA_PLAY_ERROR);
					}

				}
				mp3Path = null;
				return true;
			}
		});
		mBinder.setOnServiceBinderListener(new OnServiceBinderListener() {

			@Override
			public void seekBarStartTrackingTouch() {
				// TODO Auto-generated method stub
				if (mediaPlayer.isPlaying()) {
					removeUpdateMsg();
				}
			}

			@Override
			public void seekBarStopTrackingTouch(int progress) {
				// TODO Auto-generated method stub
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.seekTo(progress);
					update();
				}
			}

			@Override
			public void lrc(LyricView lyricView, boolean isKLOK) {
				// TODO Auto-generated method stub
				MediaService.this.lyricView = lyricView;// 获得歌词视图
				if (MediaService.this.lyricView != null) {
					MediaService.this.lyricView.setKLOK(isKLOK);
				}
			}

			@Override
			public void control(int command) {
				// TODO Auto-generated method stub
				switch (command) {
				case CONTROL_COMMAND_PLAY:// 播放与暂停
					if (mediaPlayer.isPlaying()) {
						pause(1);
					} else {
						if (mp3Path != null && !"".equals(mp3Path)) {
							mediaPlayer.start();
							prepared();
							Bitmap icon = null;
							if (albumUtil != null) {
								icon = albumUtil.scanAlbumImage(info.getPath());
							}
							application.updateNotification(true,
									info.getName(), info.getArtist(), icon);
							application.showNotification();
						} else {// 无指定情况下播放全部歌曲列表的第一首

							startServiceCommand();
						}

					}

					break;

				case CONTROL_COMMAND_PREVIOUS:// 上一首
					if (mp3Path != null && !"".equals(mp3Path)) {
						previous();
					} else {// 无指定情况下播放全部歌曲列表的第一首

						startServiceCommand();
					}

					break;

				case CONTROL_COMMAND_NEXT:// 下一首
					if (mp3Path != null && !"".equals(mp3Path)) {
						next();
					} else {// 无指定情况下播放全部歌曲列表的第一首

						startServiceCommand();
					}

					break;

				case CONTROL_COMMAND_MODE:// 播放模式
					if (mode < MODE_RANDOM) {
						mode++;
					} else {
						mode = MODE_NORMAL;
					}
					switch (mode) {
					case MODE_NORMAL:
						Toast.makeText(getApplicationContext(), "顺序播放",
								Toast.LENGTH_SHORT).show();
						break;

					case MODE_REPEAT_ONE:
						Toast.makeText(getApplicationContext(), "单曲循环",
								Toast.LENGTH_SHORT).show();
						break;

					case MODE_REPEAT_ALL:
						Toast.makeText(getApplicationContext(), "全部循环",
								Toast.LENGTH_SHORT).show();
						break;

					case MODE_RANDOM:
						Toast.makeText(getApplicationContext(), "随机播放",
								Toast.LENGTH_SHORT).show();
						break;
					}
					mBinder.modeChange(mode);
					break;

				case CONTROL_COMMAND_REWIND:// 快退
					if (mediaPlayer.isPlaying()) {
						removeAllMsg();
						rewind();
					}
					break;

				case CONTROL_COMMAND_FORWARD:// 快进
					if (mediaPlayer.isPlaying()) {
						removeAllMsg();
						forward();
					}
					break;

				case CONTROL_COMMAND_REPLAY:// 用于快退、快进后的继续播放
					if (mediaPlayer.isPlaying()) {
						replay();
					}
					break;
				}
			}
		});

		receiver = new ServiceReceiver();// 注册广播
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_MEDIA_BUTTON);
		intentFilter.addAction(BROADCAST_ACTION_SERVICE);

		intentFilter.addAction(BROADCAST_ACTION_SERVICE);
		intentFilter.addAction(BROADCAST_ACTION_SERVICE);
		intentFilter.addAction(BROADCAST_ACTION_SERVICE);

		registerReceiver(receiver, intentFilter);

		TelephonyManager telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);// 获取电话通讯服务
		telephonyManager.listen(new ServicePhoneStateListener(),
				PhoneStateListener.LISTEN_CALL_STATE);// 创建一个监听对象，监听电话状态改变事件

	}

	// sdk2.0以上还是不使用onStart了吧...
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if (intent != null) {
			Bundle bundle = intent.getExtras();
			if (bundle != null && !bundle.isEmpty()) {
				// wydewy,从intent中获得歌曲相关信息
				page = bundle.getInt(INTENT_LIST_PAGE, 0);
				position = bundle.getInt(INTENT_LIST_POSITION, 0);
				// Toast.makeText(this, "position:" + position,
				// Toast.LENGTH_SHORT)
				// .show();
				folderPosition = bundle.getInt(INTENT_FOLDER_POSITION,
						folderPosition);
				play();
				// position可能改变，调用此方法可以更新歌曲信息，播放设为true
				updateNotification(page);
			}
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mediaPlayer != null) {
			stopForeground(true);
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.stop();
			}
			removeAllMsg();
			mediaPlayer.release();
			mediaPlayer = null;
		}
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
		preferences = getSharedPreferences(MainActivity.PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		preferences.edit().putInt(MainActivity.PREFERENCES_MODE, mode).commit();// 保存上次的播放模式
		application.notManager.cancelAll();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		lyricView = null;
		removeAllMsg();// 移除所有消息
		return true;// 一定返回true，允许执行onRebind
	}

	@Override
	public void onRebind(Intent intent) {
		// TODO Auto-generated method stub
		super.onRebind(intent);
		if (mediaPlayer.isPlaying()) {// 如果正在播放重新绑定服务的时候重新注册
			prepared();// 因为消息已经移除，所有需要重新开启更新操作
		} else {
			if (mp3Path != null && !"".equals(mp3Path)) {// 暂停原先播放重新开页面需要恢复原先的状态
				mp3Duration = mediaPlayer.getDuration();
				Intent mIntent = new Intent(MainActivity.SET_MUSIC_DURATION);
				Bundle bundle = new Bundle();
				bundle.putInt("mp3Duration", mp3Duration);
				mIntent.putExtras(bundle);
				sendBroadcast(mIntent);
				info.setMp3Duration(mp3Duration);
				CoverList.cover = albumUtil.scanAlbumImage(info.getPath());
				mBinder.playStart(info);
				mp3Current = mediaPlayer.getCurrentPosition();
				mBinder.playUpdate(mp3Current);
				mBinder.playPause();
			}
		}
		mBinder.modeChange(mode);
	}

	/**
	 * 播放操作
	 */
	private void play() {
		int size = 0;
		switch (page) {
		case MainActivity.SLIDING_MENU_ALL:
			size = MusicList.list.size();
			if (size > 0) {
				info = MusicList.list.get(position);
			}
			break;

		case MainActivity.SLIDING_MENU_FAVORITE:
			size = FavoriteList.list.size();
			if (size > 0) {
				info = FavoriteList.list.get(position);
			}
			break;
		case MainActivity.SLIDING_MENU_ONLINE:
			size = OnlineMusicList.list.size();
			if (size > 0) {
				info = OnlineMusicList.list.get(position);
			}
			break;

		case MainActivity.SLIDING_MENU_FOLDER_LIST:
			size = FolderList.list.get(folderPosition).getMusicList().size();
			if (size > 0) {
				info = FolderList.list.get(folderPosition).getMusicList()
						.get(position);
			}
			break;
		}
		// 真正执行播放wydewy
		if (size > 0) {
			mp3Path = info.getPath();
			if ("".equals(mp3Path)) {
				mp3Path = info.getUrl();
				Intent intent = new Intent(MainActivity.SHOW_ANIM);
				sendBroadcast(intent);
			}
			lyricInfo = LyricList.map.get(info.getFile());
			if (lyricInfo != null) {
				lyricPath = lyricInfo.getPath();
			}

			if (mp3Path != null && !"".equals(mp3Path)) {
				initMedia();// 先初始化音乐
				initLrc();// 再初始化歌词

			} else {
				Toast.makeText(getApplicationContext(), "出错了！",
						Toast.LENGTH_SHORT).show();
			}
			lastPage = page;
			if (!isCommandPrevious) {
				positionList.add(position);
			}
			isCommandPrevious = false;
		}
	}

	/**
	 * 自动播放操作
	 */
	private void autoPlay() {
		if (mode == MODE_NORMAL) {
			if (position != getSize() - 1) {
				next();
			} else {
				pause(1);
			}
		} else if (mode == MODE_REPEAT_ONE) {
			play();
		} else {
			next();
		}
	}

	/**
	 * 上一首操作
	 */
	private void previous() {
		int size = getSize();
		if (size > 0) {
			isCommandPrevious = true;
			if (mode == MODE_RANDOM) {
				if (lastPage == page) {
					if (positionList.size() > 1) {
						positionList.remove(positionList.size() - 1);
						position = positionList.get(positionList.size() - 1);
					} else {
						position = (int) (Math.random() * size);
					}
				} else {
					positionList.clear();
					position = (int) (Math.random() * size);
				}
			} else {
				if (position == 0) {
					position = size - 1;
				} else {
					position--;
				}
			}
			startServiceCommand();
		}
	}

	/**
	 * 更新Notification 一旦在OnStartCommand丰富中调用，isPlaying 99%要为true;
	 * 
	 * @param page
	 */
	public void updateNotification(int page) {
		startForeground(Constant.NOTI_CTRL_ID, application.notification);
		MusicInfo cInfo = null;
		// wydewy 跟句不停地页面查找不同的列表，然后更新通知栏
		switch (page) {
		case MainActivity.SLIDING_MENU_ALL:
			cInfo = MusicList.list.get(position);
			break;
		case MainActivity.SLIDING_MENU_FAVORITE:
			cInfo = FavoriteList.list.get(position);
			break;
		case MainActivity.SLIDING_MENU_ONLINE:
			cInfo = OnlineMusicList.list.get(position);
			break;
		case MainActivity.SLIDING_MENU_FOLDER_LIST:
			cInfo = FolderList.list.get(folderPosition).getMusicList()
					.get(position);
			break;
		}
		Bitmap icon = null;
		if (albumUtil != null) {
			icon = albumUtil.scanAlbumImage(info.getPath());
		}
		application.updateNotification(true, info.getName(), info.getArtist(),
				icon);
		application.showNotification();
	}

	/**
	 * 下一首操作
	 */
	private void next() {
		int size = getSize();
		if (size > 0) {
			if (mode == MODE_RANDOM) {
				position = (int) (Math.random() * size);
			} else {
				if (position == size - 1) {
					position = 0;
				} else {
					position++;
				}
			}
			startServiceCommand();
		}
	}

	/**
	 * 快退
	 */
	private void rewind() {
		int current = mp3Current - 1000;
		mp3Current = current > 0 ? current : 0;
		mBinder.playUpdate(mp3Current);
		mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_REWIND, 100);
	}

	/**
	 * 快进
	 */
	private void forward() {
		int current = mp3Current + 1000;
		mp3Current = current < mp3Duration ? current : mp3Duration;
		mBinder.playUpdate(mp3Current);
		mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_FORWARD, 100);
	}

	/**
	 * 用于快退、快进后的继续播放
	 */
	private void replay() {
		if (mHandler.hasMessages(MEDIA_PLAY_REWIND)) {
			mHandler.removeMessages(MEDIA_PLAY_REWIND);
		}
		if (mHandler.hasMessages(MEDIA_PLAY_FORWARD)) {
			mHandler.removeMessages(MEDIA_PLAY_FORWARD);
		}
		mediaPlayer.seekTo(mp3Current);
		mHandler.sendEmptyMessage(MEDIA_PLAY_UPDATE);
		if (lyricView != null && hasLyric) {
			lyricView.setSentenceEntities(lyricList);
			mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_UPDATE_LYRIC,
					UPDATE_LYRIC_TIME);// 通知刷新歌词
		}
	}

	/**
	 * 获得列表歌曲数量
	 * 
	 * @return 数量
	 */
	private int getSize() {
		int size = 0;
		switch (page) {
		case MainActivity.SLIDING_MENU_ALL:
			size = MusicList.list.size();
			break;

		case MainActivity.SLIDING_MENU_FAVORITE:
			size = FavoriteList.list.size();
			break;
		case MainActivity.SLIDING_MENU_ONLINE:
			size = OnlineMusicList.list.size();
			break;

		case MainActivity.SLIDING_MENU_FOLDER_LIST:
			size = FolderList.list.get(folderPosition).getMusicList().size();
			break;
		}
		return size;
	}

	/**
	 * 内部模拟生成启动服务的命令
	 */
	private void startServiceCommand() {
		Intent intent = new Intent(getApplicationContext(), MediaService.class);
		intent.putExtra(INTENT_LIST_PAGE, page);
		intent.putExtra(INTENT_LIST_POSITION, position);
		startService(intent);
	}

	/**
	 * 初始化媒体播放器
	 */
	private void initMedia() {
		try {

			removeAllMsg();// 对于重新播放需要移除所有消息
			if (mp3Path != null && !"".equals(mp3Path)) {
				mediaPlayer.reset();
				mediaPlayer.setDataSource(mp3Path);
				mediaPlayer.prepareAsync();
			} else {

			}
			stopForeground(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 初始化歌词
	 */
	private void initLrc() {
		hasLyric = false;
		if (lyricPath != null && !"".equals(lyricPath)) {
			try {
				String lyrType = lyricInfo.getLyrtype();
				LyricParser parser = new LyricParser(lyricPath, lyrType);// wydewy:2015-08-29,添加歌词类型
				Log.i("TAG", ".MediaService.initLrc()---->lyrType:" + lyrType);
				lyricList = parser.parser();
				Log.i("TAG","lyricList.size"+lyricList.size());
				hasLyric = true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			if (lyricView != null) {
				lyricView.clear();
			}
		}
	}

	/**
	 * 准备好开始播放工作
	 */
	private void prepared() {
		mHandler.sendEmptyMessage(MEDIA_PLAY_START);// 通知歌曲已播放
		if (lyricView != null) {
			// lyricView.clear(); 清空会把现有的lyricList也清空，内存共用了???
			if (hasLyric) {
				lyricView.setSentenceEntities(lyricList);
				mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_UPDATE_LYRIC,
						UPDATE_LYRIC_TIME);// 通知刷新歌词
			}
		}
	}

	/**
	 * 开始播放，获得总时间和AudioSessionId，并启动更新UI任务
	 */
	@SuppressLint("NewApi")
	private void start() {
		if (mp3Path != null && !"".equals(mp3Path)) {
			mp3Duration = mediaPlayer.getDuration();
			// Toast.makeText(getApplicationContext(),
			// "mp3Duration:" + mp3Duration, Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(MainActivity.SET_MUSIC_DURATION);
			Bundle bundle = new Bundle();
			bundle.putInt("mp3Duration", mp3Duration);
			intent.putExtras(bundle);
			sendBroadcast(intent);
			info.setMp3Duration(mp3Duration);
			info.setAudioSessionId(mediaPlayer.getAudioSessionId());

			// 获取歌曲封面
			CoverList.cover = albumUtil.scanAlbumImage(info.getPath());
			mBinder.playStart(info);
			mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_UPDATE, UPDATE_UI_TIME);
		}
	}

	/**
	 * 更新UI，发现MediaPlayer.getCurrentPosition()的bug很严重，感觉指的不是时间而是帧数，
	 * 而且Handler处理事务要话费时间，虽然间隔1秒的延时时间，但处理完成就不止1秒的时间，
	 * 所以换算后会出现跳秒的情况，机子配置越差的感觉越明显，本想通过自增来实现，但发现误差更大，暂无其他方法了
	 */
	private void update() {
		if (mp3Path != null && !"".equals(mp3Path)) {
			// TODO Auto-generated method stub
			mp3Current = mediaPlayer.getCurrentPosition();
			mBinder.playUpdate(mp3Current);
			mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_UPDATE, UPDATE_UI_TIME);
		}
	}

	/**
	 * 暂停音乐
	 * 
	 * @param b
	 */
	private void pause(int b) {
		removeAllMsg();// 移除所有消息
		if (mp3Path != null && !"".equals(mp3Path)) {
			mediaPlayer.pause();
			mBinder.playPause();
			mediaPlayer.pause();

		}
		Bitmap icon = null;
		if (albumUtil != null) {
			icon = albumUtil.scanAlbumImage(info.getPath());
		}
		application.updateNotification(false, info.getName(), info.getArtist(),
				icon);
		application.showNotification();
	}

	/**
	 * 移除更新UI的消息
	 */
	private void removeUpdateMsg() {
		if (mHandler != null && mHandler.hasMessages(MEDIA_PLAY_UPDATE)) {
			mHandler.removeMessages(MEDIA_PLAY_UPDATE);
		}
	}

	/**
	 * 播放完成
	 */
	private void complete() {
		// TODO Auto-generated method stub

		mBinder.playComplete();
		if (mp3Path != null && !"".equals(mp3Path)) {
			mBinder.playUpdate(mp3Duration);
		} else {

		}
		autoPlay();
	}

	/**
	 * 播放出错
	 */
	private void error() {

		mBinder.playError();
		mBinder.playPause();
		positionList.clear();
	}

	/**
	 * 刷新歌词
	 */
	private void updateLrcView() {
		if (lyricList.size() > 0 && lyricList != null) {
			lyricView.setIndex(getLrcIndex(mediaPlayer.getCurrentPosition(),
					mp3Duration));
			lyricView.invalidate();
			mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_UPDATE_LYRIC,
					UPDATE_LYRIC_TIME);
		}
	}

	/**
	 * 移除更新歌词的消息
	 */
	private void removeUpdateLrcViewMsg() {
		if (mHandler != null && mHandler.hasMessages(MEDIA_PLAY_UPDATE_LYRIC)) {
			mHandler.removeMessages(MEDIA_PLAY_UPDATE_LYRIC);
		}
	}

	/**
	 * 移除所有消息
	 */
	private void removeAllMsg() {
		removeUpdateMsg();
		removeUpdateLrcViewMsg();
	}

	/**
	 * 耳机线控-处理单击过渡事件
	 */
	private void buttonOneClick() {
		buttonClickCounts++;
		mHandler.sendEmptyMessageDelayed(MEDIA_BUTTON_DOUBLE_CLICK, 300);
	}

	/**
	 * 耳机线控-响应单击和双击事件
	 */
	private void buttonDoubleClick() {
		if (buttonClickCounts == 1) {
			mBinder.setControlCommand(CONTROL_COMMAND_PLAY);
		} else if (buttonClickCounts > 1) {
			mBinder.setControlCommand(CONTROL_COMMAND_NEXT);
		}
		buttonClickCounts = 0;
	}

	/**
	 * 歌词同步处理
	 */
	private int[] getLrcIndex(int currentTime, int duration) {
		int index = 0;
		int size = lyricList.size();
		if (currentTime < duration) {
			for (int i = 0; i < size; i++) {
				if (i < size - 1) {
					if (currentTime < lyricList.get(i).getTime() && i == 0) {
						index = i;
					}
					if (currentTime > lyricList.get(i).getTime()
							&& currentTime < lyricList.get(i + 1).getTime()) {
						index = i;
					}
				}
				if (i == size - 1 && currentTime > lyricList.get(i).getTime()) {
					index = i;
				}
			}
		}
		int temp1 = lyricList.get(index).getTime();
		int temp2 = (index == (size - 1)) ? 0 : lyricList.get(index + 1)
				.getTime() - temp1;
		return new int[] { index, currentTime, temp1, temp2 };
	}

	private class ServicePhoneStateListener extends PhoneStateListener {

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			// TODO Auto-generated method stub
			if (state == TelephonyManager.CALL_STATE_RINGING
					&& mediaPlayer != null && mediaPlayer.isPlaying()) { // 来电
				pause(1);
			}
		}
	}

	private class ServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent != null) {
				boolean isActionMediaButton = Intent.ACTION_MEDIA_BUTTON
						.equals(intent.getAction());
				if (isActionMediaButton) {// 由于广播优先级的限制，此功能未必能够很好的支持
					KeyEvent event = (KeyEvent) intent
							.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
					if (event == null) {
						return;
					}
					long eventTime = event.getEventTime() - event.getDownTime();// 按键按下到松开的时长
					if (eventTime > 1000) {
						mBinder.setControlCommand(CONTROL_COMMAND_PREVIOUS);
						abortBroadcast();// 终止广播(不让别的程序收到此广播，免受干扰)
					} else {
						if (event.getAction() == KeyEvent.ACTION_UP) {
							mHandler.sendEmptyMessage(MEDIA_BUTTON_ONE_CLICK);
							abortBroadcast();// 终止广播(不让别的程序收到此广播，免受干扰)
						}
					}

				} else {
					int i = intent.getIntExtra(INTENT_ACTIVITY, ACTIVITY_MAIN);
					switch (i) {
					case ACTIVITY_SCAN:
						intent = new Intent(getApplicationContext(),
								ScanActivity.class);
						break;

					case ACTIVITY_MAIN:
						intent = new Intent(getApplicationContext(),
								MainActivity.class);
						break;

					case ACTIVITY_PLAYER:
						intent = new Intent(getApplicationContext(),
								PlayerActivity.class);
						break;

					case ACTIVITY_SETTING:
						intent = new Intent(getApplicationContext(),
								SettingActivity.class);
						break;
					}
				}
			}

		}

	}

	private static class ServiceHandler extends Handler {

		private WeakReference<MediaService> reference;

		public ServiceHandler(MediaService service) {
			// TODO Auto-generated constructor stub
			reference = new WeakReference<MediaService>(service);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (reference.get() != null) {
				MediaService theService = reference.get();
				switch (msg.what) {
				case MEDIA_PLAY_START:
					theService.start();// 播放开始
					break;

				case MEDIA_PLAY_UPDATE:
					theService.update();// 更新UI
					break;

				case MEDIA_PLAY_COMPLETE:
					theService.complete();// 播放完成
					break;

				case MEDIA_PLAY_ERROR:
					theService.error();// 播放出错
					break;

				case MEDIA_PLAY_UPDATE_LYRIC:
					theService.updateLrcView();// 刷新歌词
					break;

				case MEDIA_PLAY_REWIND:
					theService.rewind();// 快退线程
					break;

				case MEDIA_PLAY_FORWARD:
					theService.forward();// 快进线程
					break;

				case MEDIA_BUTTON_ONE_CLICK:
					theService.buttonOneClick();// 线控单机事件
					break;

				case MEDIA_BUTTON_DOUBLE_CLICK:
					theService.buttonDoubleClick();// 线控双击事件
					break;
				}
			}
		}
	}

}
