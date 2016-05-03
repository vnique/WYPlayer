package cn.wydewy.wyplayer.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.wydewy.wyplayer.entity.MusicInfo;

public class DownloadList {
	public static final List<MusicInfo> list = new ArrayList<MusicInfo>();

	/**
	 * 按字母排序
	 */
	public static void sort() {
		Collections.sort(list, new MusicInfo());
	}

	public static boolean isExistMusicByname(String musicName) {
		if (musicName != null) {
			for (int j = 0; j < MusicList.list.size(); j++) {
				if (list.size() > 0) {
					if (list.get(j).getName().equals(musicName)) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
