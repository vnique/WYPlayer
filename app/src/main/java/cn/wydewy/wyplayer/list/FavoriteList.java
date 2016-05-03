package cn.wydewy.wyplayer.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.wydewy.wyplayer.entity.MusicInfo;

/**
 * By wydewy 2013 Open Source Project
 * 
 * <br>
 * <b>创建一个公用的最喜爱歌曲列表</b></br>
 * 
 * @author wydewy
 * @version 2013.05.19 v1.0 创建集合<br>
 *          2013.08.06 v1.1 新增按字母排序方法</br>
 */
public class FavoriteList {

	public static final List<MusicInfo> list = new ArrayList<MusicInfo>();

	/**
	 * 按字母排序
	 */
	public static void sort() {
		Collections.sort(list, new MusicInfo());
	}

}
