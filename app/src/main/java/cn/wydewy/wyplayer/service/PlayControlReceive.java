package cn.wydewy.wyplayer.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import cn.wydewy.wyplayer.IMusic;
import cn.wydewy.wyplayer.Mp3Application;
import cn.wydewy.wyplayer.util.Constant;

public class PlayControlReceive extends BroadcastReceiver {
	private Mp3Application application;
	private IMusic music;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		application = (Mp3Application) context.getApplicationContext();
		music = application.music;
		String action = intent.getAction();
		// 播放控制栏相关：
		if (action != null) {
			// 控制上一曲
			if (action.equals(Constant.ACTION_PREV)) {
				music.prevSong();
			}
			// 控制播放&暂停
			if (action.equals(Constant.ACTION_PLAY)) {
				music.play();
			}

			// 控制下一曲
			if (action.equals(Constant.ACTION_NEXT)) {
				music.nextSong();

			}
			// 控制通知栏不显示
			if (action.equals(Constant.ACTION_CANCEL)) {
				music.cancel();
			}
			// 控制通知栏不显示
			if (action.equals(Constant.ACTION_CANCEL_DOWNLOAD)) {
				music.cancel();
			}
		}
	}

}
