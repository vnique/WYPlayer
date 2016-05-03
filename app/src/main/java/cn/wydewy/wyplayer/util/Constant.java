package cn.wydewy.wyplayer.util;

import android.os.Environment;

public class Constant {
	public static final String HOST = "http://www.wydewy.cn/Vplayer/admin/";
	public static final String SEARCH = "search.fun.php";
	public static final String WEB_FOLDER = Environment
			.getExternalStorageDirectory() + "/wyplayer/web/";//网络歌曲
	public static final String LRC_FOLDER = Environment
			.getExternalStorageDirectory() + "/wyplayer/lrc/";
	public static final int NOTI_CTRL_ID = 0;
	public final static String DOWNLOAD = "download";
	/** 上一首 按钮点 */
	public final static String ACTION_PREV = "previous";
	/** 播放/暂停 按钮 */
	public final static String ACTION_PLAY = "play";
	/** 下一首 按钮 */
	public final static String ACTION_NEXT = "next";
	/** 取   消 按钮 */
	public static final String ACTION_CANCEL = "cancel";
	public static final String ACTION_CANCEL_DOWNLOAD = "canceldownload";
}
