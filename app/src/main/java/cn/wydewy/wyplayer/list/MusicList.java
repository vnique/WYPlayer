package cn.wydewy.wyplayer.list;

import java.util.ArrayList;
import java.util.List;

import cn.wydewy.wyplayer.entity.FolderInfo;
import cn.wydewy.wyplayer.entity.MusicInfo;
import cn.wydewy.wyplayer.util.Constant;

/**
 * By wydewy 2013 Open Source Project
 * 
 * <br>
 * <b>创建一个公用的所有歌曲列表</b></br>
 * 
 * @author wydewy
 * @version 2013.05.19 v1.0 创建集合
 */
public class MusicList {

	public static final List<MusicInfo> list = new ArrayList<MusicInfo>();

	/**
	 * 
	 * @param folder
	 *            音乐保存的文件夹
	 * @param musicInfo
	 *            要保存的音乐
	 */
	public static void addMusic(String folder, MusicInfo musicInfo) {
		FolderInfo folderInfo = new FolderInfo();
		if (musicInfo != null) {
			boolean exists = false;
			for (int j = 0; j < FolderList.list.size(); j++) {
				// 做对比，存在同名路径则判断有新增就合并，没有直接添加。此方法比较笨啊，肯定影响效率的...
				if (folder.equals(FolderList.list.get(j).getMusicFolder())) {
					// 有扫描到新增得歌曲就合并列表
					FolderList.list.get(j).getMusicList().add(musicInfo);
					exists = true;
					break;// 跳出循环
				}
			}
			if (!exists) {// 不存在同名路径才新增
				// 设置文件夹列表文件夹路径
				folderInfo.setMusicFolder(folder);
				// 设置文件夹列表歌曲信息
				List<MusicInfo> listInfo = new ArrayList<MusicInfo>();
				listInfo.add(musicInfo);
				folderInfo.setMusicList(listInfo);
				// 加入文件夹列表
				FolderList.list.add(folderInfo);
			}
		}
	}

	/**
	 * 
	 * @param url
	 *            音乐的统一资源定位器
	 * @param listInfo
	 *            音乐
	 */
	public static void addWebMusic(MusicInfo musicInfo) {
		MusicList.list.add(musicInfo);
		OnlineMusicList.list.add(musicInfo);
		if (musicInfo != null) {
			for (int j = 0; j < FolderList.list.size(); j++) {
				if (FolderList.list.get(j).getMusicFolder()
						.equals(Constant.WEB_FOLDER)) {
					FolderList.list.get(j).getMusicList().add(musicInfo);
					break;// 跳出循环
				}

			}
		}
	}

	public static void addDownloadMusic(MusicInfo musicInfo) {
		MusicList.list.add(musicInfo);
		DownloadList.list.add(musicInfo);
		if (musicInfo != null) {
			for (int j = 0; j < FolderList.list.size(); j++) {
				if (FolderList.list.get(j).getMusicFolder()
						.equals(Constant.WEB_FOLDER)) {
					FolderList.list.get(j).getMusicList().add(musicInfo);
					break;// 跳出循环
				}

			}
		}
	}

	public static boolean isExistMusicByname(String musicName) {
		if (musicName != null) {
			for (int j = 0; j < MusicList.list.size(); j++) {
				if (MusicList.list.get(j).getName().equals(musicName)) {
					return true;
				}
			}
		}
		return false;
	}

}
