package cn.wydewy.wyplayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import cn.wydewy.wyplayer.db.DBDao;
import cn.wydewy.wyplayer.download.DownloadProgressListener;
import cn.wydewy.wyplayer.download.FileDownloader;
import cn.wydewy.wyplayer.entity.LyricInfo;
import cn.wydewy.wyplayer.entity.MusicInfo;
import cn.wydewy.wyplayer.list.DownloadList;
import cn.wydewy.wyplayer.list.LyricList;
import cn.wydewy.wyplayer.list.MusicList;
import cn.wydewy.wyplayer.util.Constant;
import cn.wydewy.wyplayer.util.ScanUtil;
import cn.wydewy.wyplayer.view.FloatDialog;
import cn.wydewy.wyplayer.view.FloatDialog.IDialogOnclickInterface;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayPostRequest;
import com.android.volley.toolbox.Volley;

public class SearchActivity extends Activity implements OnClickListener,
		OnItemClickListener, TextWatcher, OnItemLongClickListener,
		IDialogOnclickInterface {
	private EditText searchTxt;
	private ImageView searchImg;
	private ListView searchListView;

	private ArrayList<String> contentLists = new ArrayList<String>();
	private ArrayList<String> pathLists = new ArrayList<String>();
	private ArrayList<String> artistLists = new ArrayList<String>();
	private ArrayList<String> timeLists = new ArrayList<String>();
	private ArrayList<String> tagLists = new ArrayList<String>();
	private ArrayList<String> lyricLists = new ArrayList<String>();

	private ArrayAdapter<String> adapter;
	private RequestQueue mQueue;

	// 长按相关
	private int longClickPosition;
	private FloatDialog floatDialog;
	private View currentItemView;

	private boolean isDownloadCompleted = false;
	private ImageView back_img;

	private IMusic music;

	/** 初始化通知栏 */
	@SuppressLint("InlinedApi")
	private void initNotify() {
		Builder mBuilder = new NotificationCompat.Builder(this);
		mBuilder.setContentTitle("小唯下载").setContentText("歌曲准备中")
		// .setNumber(number)//显示数量
				.setTicker("歌曲来啦")// 通知首次出现在通知栏，带上升动画效果的
				.setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
				.setSmallIcon(R.drawable.ic_launcher);
		// 点击的意图ACTION是跳转到Intent
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setClass(this, LogoActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		mBuilder.setContent(application
				.updateDowmloadNotification(0, 100, "准备"));

		mBuilder.setContentIntent(pendingIntent);
		Notification notification = mBuilder.build();
		application.downloadNotification = notification;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		initDates();
		initView();
		initNotify();
		initEvent();

	}

	private void initDates() {
		// TODO Auto-generated method stub
		application = (Mp3Application) getApplication();
		music = application.music;

		mQueue = Volley.newRequestQueue(this);
	}

	private void initEvent() {
		// TODO Auto-generated method stub
		adapter = new ArrayAdapter<String>(this, R.layout.content_list_item,
				R.id.contentItemText, contentLists);
		searchListView.setAdapter(adapter);
		back_img.setOnClickListener(this);
		searchTxt.addTextChangedListener(this);
		searchImg.setOnClickListener(this);
		searchListView.setOnItemClickListener(this);
		searchListView.setOnItemLongClickListener(this);

		floatDialog = new FloatDialog(this, R.style.floatDialogStyle, this);

		floatDialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface arg0) {
				// TODO Auto-generated method stub
			}
		});
	}

	private void initView() {
		// TODO Auto-generated method stub
		back_img = (ImageView) findViewById(R.id.back_img);

		searchTxt = (EditText) findViewById(R.id.searchTxt);
		searchImg = (ImageView) findViewById(R.id.search_img);
		searchListView = (ListView) findViewById(R.id.searchList);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.search_img:
			// downloadMusic(urlStr);
			String content = searchTxt.getText().toString();
			Log.i("TAG", "content:" + content);
			refreshContents(content);
			Toast.makeText(getApplicationContext(), "长按进行下载~",
					Toast.LENGTH_SHORT).show();
			break;

		case R.id.back_img:
			sendUpdateBroadcast("1");
			SearchActivity.this.finish();
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int index, long arg3) {
		// TODO Auto-generated method stub
		playWebMusic(pathLists.get(index), contentLists.get(index),
				artistLists.get(index), timeLists.get(index),
				tagLists.get(index), lyricLists.get(index));

	}

	// 下载歌曲相关wydewy

	private static final int PROCESSING = 1;
	private static final int FAILURE = -1;
	private Handler handler = new UIHandler();
	private Mp3Application application;

	private final class UIHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case PROCESSING: // 更新进度
				float totalPro = msg.getData().getInt("total");
				float currentPro = msg.getData().getInt("size");
				Log.i("TAG", "totalPro:" + totalPro + "currentPro:"
						+ currentPro);

				application.updateDowmloadNotification(currentPro, totalPro,
						"一首歌曲正在");
				application.showDowmloadNotification();
				// 下载完成
				if (currentPro == totalPro) {
					Toast.makeText(getApplicationContext(), "下载完成！",
							Toast.LENGTH_LONG).show();
					application.notManager.cancel(2);
					isDownloadCompleted = true;
				}
				break;
			case FAILURE: // 下载失败
				Toast.makeText(getApplicationContext(), "下载失败了呢~",
						Toast.LENGTH_LONG).show();
				break;
			}
		}
	}

	/**
	 * 
	 * UI控件画面的重绘(更新)是由主线程负责处理的，如果在子线程中更新UI控件的值，更新后的值不会重绘到屏幕上
	 * 一定要在主线程里更新UI控件的值，这样才能在屏幕上显示出来，不能在子线程中更新UI控件的值
	 * 
	 */
	private final class DownloadTask implements Runnable {
		private String fileName;
		private String path;
		private File saveDir;
		private FileDownloader loader;

		public DownloadTask(String path, File saveDir, String fileName) {
			this.path = path;
			this.saveDir = saveDir;
			this.fileName = fileName;
		}

		/**
		 * 退出下载
		 */
		public void exit() {
			if (loader != null)
				loader.exit();
		}

		DownloadProgressListener downloadProgressListener = new DownloadProgressListener() {
			@Override
			public void onDownloadSize(int size) {
				Message msg = new Message();
				msg.what = PROCESSING;
				msg.getData().putInt("total", loader.getFileSize());
				msg.getData().putInt("size", size);
				handler.sendMessage(msg);
			}
		};

		public void run() {
			try {
				// 实例化一个文件下载器
				loader = new FileDownloader(getApplicationContext(), path,
						fileName, saveDir, 5);
				// 设置进度条最大值
				// progressBar.setMax(loader.getFileSize());
				loader.download(downloadProgressListener);
			} catch (Exception e) {
				e.printStackTrace();
				handler.sendMessage(handler.obtainMessage(FAILURE)); //
				// 发送一条空消息对象
			}
		}
	}

	private DownloadTask task;
	protected boolean isLyricOk = false;

	/**
	 * 下载网络歌曲
	 * 
	 * @param path
	 * @param name
	 * @param artist
	 * @param time
	 * @param tag
	 * 
	 */
	private void downloadWebMusic(final String path, final String name,
			final String artist, final String time, final String tag,
			final String lyric) {

		String url = null;
		if ("wydewy".equals(tag)) {
			url = Constant.HOST + path;
		} else {
			url = path;
		}

		if (!DownloadList.isExistMusicByname(name)) {
			File fileSave = new File(Constant.WEB_FOLDER);
			task = new DownloadTask(url, fileSave, name+".mp3");
			new Thread(task).start();// 开一个线程下载歌曲
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					downloadLyric(Constant.HOST + lyric, name);
				}
			}).start();// 开一个线程下载歌词

			Toast.makeText(getApplicationContext(), "开始下载歌曲！",
					Toast.LENGTH_LONG).show();
			new Thread(new Runnable() {// 另一个线程等待下载完成

						@Override
						public void run() {
							// TODO Auto-generated method stub
							while (!isDownloadCompleted)
								;// 条件(下载是否完成)等待
							new ScanUtil(getApplicationContext())
									.scanMusicFromPath(Constant.WEB_FOLDER);
							DBDao db = new DBDao(SearchActivity.this);
							// 记录歌词位置
							db.addLyric(name, Constant.LRC_FOLDER + name, "lrc");
							LyricInfo lyricInfo = new LyricInfo(name,
									Constant.LRC_FOLDER+".lrc" + name, "lrc");
							LyricList.map.put(name, lyricInfo);
							db.close();
							sendUpdateBroadcast("1");
							SearchActivity.this.finish();
						}
					}).start();
		} else {
			Toast.makeText(getApplicationContext(), "歌曲已经下载了~",
					Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * 播放网络歌曲
	 * 
	 * @param path
	 * @param name
	 * @param artist
	 * @param time
	 */
	private void playWebMusic(final String path, final String name,
			final String artist, final String time, final String tag,
			final String lyric) {
		// TODO Auto-generated method stub

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				downloadLyric(Constant.HOST + lyric, name);
			}
		}).start();// 开一个线程下载歌词
		Log.i("TAG", "lyric地址" + Constant.HOST + lyric);
		final DBDao db = new DBDao(this);

		if (!MusicList.isExistMusicByname(name)) {

			String url = null;
			if ("wydewy".equals(tag)) {
				url = Constant.HOST + path;
			} else {
				url = path;
			}
			db.add(name, name, "", Constant.WEB_FOLDER, false, time,
					"musicSize", artist, "musicFormat", "musicAlbum",
					"musicYears", "musicChannels", "musicGenre", "musicKbps",
					"musicHz", url);
			MusicInfo musicInfo = new MusicInfo();

			musicInfo.setUrl(url);
			musicInfo.setFile(name);
			musicInfo.setName(name);
			musicInfo.setArtist(artist);
			musicInfo.setTime(time);
			musicInfo.setPath("");
			// 加入在线歌曲列表
			MusicList.addWebMusic(musicInfo);
			// 记录歌词位置
			db.addLyric(name, Constant.LRC_FOLDER + name, "lrc");
			LyricInfo lyricInfo = new LyricInfo(name, Constant.LRC_FOLDER
					+ name + ".lrc", "lrc");
			LyricList.map.put(name, lyricInfo);
			sendUpdateBroadcast(name);// 发送广播
			SearchActivity.this.finish();
		}

		db.close();

	}

	@Override
	public void afterTextChanged(Editable arg0) {
		// TODO Auto-generated method stub
		// String content = searchTxt.getText().toString();
		// refreshContents(content);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		String content = searchTxt.getText().toString();
		refreshContents(content);
	}

	/**
	 * 刷新
	 * 
	 * @return
	 */
	public void refreshContents(final String requestContent) {
		String url = Constant.HOST + Constant.SEARCH;
		Map<String, String> map = new HashMap<String, String>();
		map.put("content", requestContent);

		JsonArrayPostRequest jsonArrayRequest = new JsonArrayPostRequest(url,
				new Listener<JSONArray>() {

					@Override
					public void onResponse(JSONArray response) {
						// TODO Auto-generated method stub
						contentLists.clear();// 记得清除
						pathLists.clear();
						artistLists.clear();
						timeLists.clear();
						lyricLists.clear();
						for (int i = 0; i < response.length(); i++) {
							try {
								String name = response.getJSONObject(i)
										.getString("name");
								String path = response.getJSONObject(i)
										.getString("path");
								String artist = response.getJSONObject(i)
										.getString("artist");
								String time = response.getJSONObject(i)
										.getString("time");
								String tag = response.getJSONObject(i)
										.getString("tag");
								String lyric = response.getJSONObject(i)
										.getString("lyric");
								Log.i("TAG", "response:" + response.toString());
								contentLists.add(name);
								pathLists.add(path);
								artistLists.add(artist);
								timeLists.add(time);
								tagLists.add(tag);
								lyricLists.add(lyric);
								adapter.notifyDataSetChanged();
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						// TODO Auto-generated method stub
						Log.i("TAG", "error:" + error.toString());
						Toast.makeText(getApplicationContext(), "小唯没有找到~",
								Toast.LENGTH_LONG).show();
					}
				}, map);
		mQueue.add(jsonArrayRequest);
	}

	/**
	 * 因为Activity的launchMode="SingleTask"时不会执行onActivityResult方法，改为发送广播
	 */
	public void sendUpdateBroadcast(String name) {
		Intent intent = new Intent(MainActivity.BROADCAST_ACTION_PLAY_ONLINE);
		intent.putExtra("name", name);
		sendBroadcast(intent);
	}

	/**
	 * 长按处理
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		// TODO Auto-generated method stub
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		currentItemView = view;
		setLongClickPosition(position);
		DisplayMetrics displayMetrics = new DisplayMetrics();
		Display display = this.getWindowManager().getDefaultDisplay();
		display.getMetrics(displayMetrics);
		WindowManager.LayoutParams params = floatDialog.getWindow()
				.getAttributes();
		params.gravity = Gravity.BOTTOM;
		params.y = display.getHeight() - location[1];
		floatDialog.getWindow().setAttributes(params);
		floatDialog.setCanceledOnTouchOutside(true);
		floatDialog.show();
		// 必须在show()后执行
		String leftText = "播放";
		floatDialog.setLeftTextView(leftText);
		String rightText = "下载";
		floatDialog.setRightTextView(rightText);
		return true;
	}

	public int getLongClickPosition() {
		return longClickPosition;
	}

	private void setLongClickPosition(int longClickPosition) {
		this.longClickPosition = longClickPosition;
	}

	@Override
	public void leftOnclick() {
		// TODO Auto-generated method stub
		playWebMusic(pathLists.get(longClickPosition),
				contentLists.get(longClickPosition),
				artistLists.get(longClickPosition),
				timeLists.get(longClickPosition),
				tagLists.get(longClickPosition),
				lyricLists.get(longClickPosition));
		floatDialog.dismiss();
	}

	@Override
	public void rightOnclick() {
		// TODO Auto-generated method stub
		downloadWebMusic(pathLists.get(longClickPosition),
				contentLists.get(longClickPosition),
				artistLists.get(longClickPosition),
				timeLists.get(longClickPosition),
				tagLists.get(longClickPosition),
				lyricLists.get(longClickPosition));
		floatDialog.dismiss();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (task != null) {
			task.exit();
		}
		application.notManager.cancel(2);
	}

	/**
	 * 
	 * @param lyricUrl
	 *            歌词统一资源定位器
	 * @param fileName
	 *            歌词名称
	 */
	public void downloadLyric(String lyricUrl, String fileName) {
		String urlStr = lyricUrl;
		InputStream input = null;
		try {
			/*
			 * 通过URL取得HttpURLConnection 要网络连接成功，需在AndroidMainfest.xml中进行权限配置
			 * <uses-permission android:name="android.permission.INTERNET" />
			 */
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			// 取得inputStream，并将流中的信息写入SDCard

			/*
			 * 写前准备 1.在AndroidMainfest.xml中进行权限配置 <uses-permission
			 * android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
			 * 取得写入SDCard的权限 2.取得SDCard的路径：
			 * Environment.getExternalStorageDirectory() 3.检查要保存的文件上是否已经存在
			 * 4.不存在，新建文件夹，新建文件 5.将input流中的信息写入SDCard 6.关闭流
			 */
			String pathName = Constant.LRC_FOLDER + fileName + ".lrc";// 文件存储路径
			File file = new File(pathName);
			input = conn.getInputStream();
			if (file.exists()) {
				System.out.println("exits");
				file.delete();
			}
			String dir = Constant.LRC_FOLDER;
			new File(dir).mkdir();// 新建文件夹
			file.createNewFile();// 新建文件
			// 读取文件
			BufferedReader in = new BufferedReader(new InputStreamReader(input));
			String line = null;
			StringBuffer sb = new StringBuffer();
			while ((line = in.readLine()) != null) {
				sb.append(line);
				sb.append("\r\n");
			}
			Log.i("TAG", "sb.toString():"+sb.toString());
			FileOutputStream output = new FileOutputStream(file);
			byte[] bytes = sb.toString().getBytes();
			output.write(bytes);
			output.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("success");
		}
	}

}
