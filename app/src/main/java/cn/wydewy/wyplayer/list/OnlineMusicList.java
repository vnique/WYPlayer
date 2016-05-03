package cn.wydewy.wyplayer.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.wydewy.wyplayer.entity.MusicInfo;

/**
 * By wydewy 2015 Open Source Project
 * 
 * <br>
 * <b>创建一个公用的在线歌曲歌曲列表</b></br>
 * 
 * @author wydewy
 * 
 */
public class OnlineMusicList {
	
	public static final List<MusicInfo> list = new ArrayList<MusicInfo>();

	/**
	 * 按字母排序
	 */
	public static void sort() {
		Collections.sort(list, new MusicInfo());
	}

}
