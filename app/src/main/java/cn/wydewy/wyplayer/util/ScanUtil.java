package cn.wydewy.wyplayer.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import cn.wydewy.wyplayer.db.DBDao;
import cn.wydewy.wyplayer.entity.LyricInfo;
import cn.wydewy.wyplayer.entity.MusicInfo;
import cn.wydewy.wyplayer.entity.ScanInfo;
import cn.wydewy.wyplayer.list.DownloadList;
import cn.wydewy.wyplayer.list.LyricList;
import cn.wydewy.wyplayer.list.MusicList;
import cn.wydewy.wyplayer.list.OnlineMusicList;

/**
 * By wydewy 2013 Open Source Project
 * 
 * <br>
 * <b>扫描管理器，负责扫描数据库或者SD卡的歌曲文件(耗时操作，请使用异步线程执行)，
 * 由于在一个循环中同时判断出歌曲对应的歌词难度太大，只能新建数据库表来分别存储</b></br>
 * 
 * 
 * @author wydewy
 * @version 2013.05.18 v1.0 实现扫描SD卡，数据库增加及查询操作<br>
 *          2013.06.15 v2.0 实现获取详细mp3标签，修改数据库记录信息<br>
 *          2013.06.16 v2.1 修正数据库查询不存在bug<br>
 *          2013.06.23 v2.2 新增对歌词的扫描<br>
 *          2013.08.05 v2.3 新增对专辑名称的扫描，修复扫描出现的几个错误<br>
 *          2013.08.05 v2.4 修正扫描文件夹歌曲列表的一个错误<br>
 *          2013.08.29 v2.5 修正真机上扫描报错的问题，原因是路径被全被格成小写导致空指针<br>
 *          2013.08.30 v2.6 修正多次扫描后文件夹列表重复的问题</br>
 */
public class ScanUtil {

	private Context context;
	private DBDao db;

	public ScanUtil(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
	}

	/**
	 * 查询音乐媒体库所有目录，缺点是影响一点效率，没有找到直接提供媒体库目录的方法
	 */
	public List<ScanInfo> searchAllDirectory() {
		List<ScanInfo> list = new ArrayList<ScanInfo>();
		StringBuffer sb = new StringBuffer();
		String[] projection = { MediaStore.Audio.Media.DISPLAY_NAME,
				MediaStore.Audio.Media.DATA };
		Cursor cr = context.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null,
				null, MediaStore.Audio.Media.DISPLAY_NAME);
		String displayName = null;
		String data = null;
		if (cr != null) {
			while (cr.moveToNext()) {
				displayName = cr.getString(cr
						.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
				data = cr.getString(cr
						.getColumnIndex(MediaStore.Audio.Media.DATA));
				data = data.replace(displayName, "");// 替换文件名留下它的上一级目录
				if (!sb.toString().contains(data)) {
					list.add(new ScanInfo(data, true));
					sb.append(data);
				}
			}
		}
		cr.close();
		if (!list.contains(new ScanInfo(Constant.WEB_FOLDER, true))) {
			list.add(new ScanInfo(Constant.WEB_FOLDER, true));
		}
		return list;
	}

	/**
	 * 扫描指定路径音乐，录入数据库并加入歌曲列表，缺点是假如系统媒体库没有更新媒体库目录则扫描不到
	 * 
	 * @param scanList
	 *            音乐媒体库所有目录
	 */
	public void scanMusicFromPath(String folder) {
		db = new DBDao(context);
		db.deleteLyric();// 不做歌词是否已经存在判断，全部删除后重新扫描
		File file[] = new File(folder).listFiles();

		if (file != null && file.length > 0) {
			Log.i("TAG", "file.length:" + file.length);
			for (File temp : file) {
				// 是文件才保存，里面还有文件夹的，那就算了吧...
				if (temp.isFile()) {
					String fileName = temp.getName();
					final String path = temp.getPath();
					final String end = fileName.substring(
							fileName.lastIndexOf(".") + 1, fileName.length());
					fileName = fileName.substring(0, fileName.lastIndexOf("."));
					// 记录歌曲信息
					// String musicFile = UUID.randomUUID().toString();
					MusicInfo musicInfo = scanMusicTag(fileName, path);// 防止扫描歌名出错而命名为uuid
					if (end.equalsIgnoreCase("mp3")) {// 不区分大小写
						// 查询不存在则记录
						if (!db.queryExist(fileName, folder)) {
						}

						db.add(fileName, musicInfo.getName(), path, folder,
								false, musicInfo.getTime(),
								musicInfo.getSize(), musicInfo.getArtist(),
								musicInfo.getFormat(), musicInfo.getAlbum(),
								musicInfo.getYears(), musicInfo.getChannels(),
								musicInfo.getGenre(), musicInfo.getKbps(),
								musicInfo.getHz(), musicInfo.getUrl());
						// 加入所有歌曲列表
						MusicList.list.add(musicInfo);
						// 加入文件夹临时列表和更新文件夹
						MusicList.addMusic(folder, musicInfo);
						if (Constant.WEB_FOLDER.equals(folder)) {
							OnlineMusicList.list.add(musicInfo);
						}

					}
					// 记录歌词信息(只识别LRC歌词)
					if (end.equalsIgnoreCase("lrc")) {// 不区分大小写
						db.addLyric(fileName, path, "lrc");
						LyricInfo lyricInfo = new LyricInfo(fileName, path,
								"lrc");
						LyricList.map.put(fileName, lyricInfo);
					}

				}
			}
		}
		db.close();
	}

	/**
	 * 扫描SD卡音乐，录入数据库并加入歌曲列表，缺点是假如系统媒体库没有更新媒体库目录则扫描不到
	 * 
	 * @param scanList
	 *            音乐媒体库所有目录
	 */
	public void scanMusicFromSD(List<String> folderList, Handler handler) {
		int count = 0;// 统计新增数
		db = new DBDao(context);
		db.deleteLyric();// 不做歌词是否已经存在判断，全部删除后重新扫描
		final int size = folderList.size();
		for (int i = 0; i < size; i++) {
			final String folder = folderList.get(i);
			File file[] = new File(folder).listFiles();
			if (file == null) {
				continue;
			}
			for (File temp : file) {
				// 是文件才保存，里面还有文件夹的，那就算了吧...
				if (temp.isFile() && temp.getName().contains(".")) {
					String fileName = temp.getName();
					final String path = temp.getPath();
					final String end = fileName.substring(
							fileName.lastIndexOf(".") + 1, fileName.length());
					fileName = fileName.substring(0, fileName.lastIndexOf("."));
					// 记录歌曲信息
					// String musicFile = UUID.randomUUID().toString();
					MusicInfo musicInfo = scanMusicTag(fileName, path);// 防止扫描歌名出错而命名为uuid
					if (end.equalsIgnoreCase("mp3")) {// 不区分大小写
						// 查询不存在则记录
						if (!db.queryExist(fileName, folder)) {

							// // 第一次扫描最喜爱肯定为false
							db.add(fileName, musicInfo.getName(), path, folder,
									false, musicInfo.getTime(),
									musicInfo.getSize(), musicInfo.getArtist(),
									musicInfo.getFormat(),
									musicInfo.getAlbum(), musicInfo.getYears(),
									musicInfo.getChannels(),
									musicInfo.getGenre(), musicInfo.getKbps(),
									musicInfo.getHz(), musicInfo.getUrl());
							// 加入所有歌曲列表
							MusicList.list.add(musicInfo);
							// 加入文件夹临时列表和更新文件夹
							MusicList.addMusic(folder, musicInfo);
							if (Constant.WEB_FOLDER.equals(musicInfo.getUrl())) {
								DownloadList.list.add(musicInfo);
							}
							count++;
						}
						if (handler != null) {
							Message msg = handler.obtainMessage();
							msg.obj = fileName;
							// Message从handler获取，可以直接向该handler对象发送消息
							msg.sendToTarget();
						}
					}
					// 记录歌词信息(只识别LRC歌词)
					if (end.equalsIgnoreCase("lrc")) {// 不区分大小写
						db.addLyric(fileName, path, "lrc");
						LyricInfo lyricInfo = new LyricInfo(fileName, path,
								"lrc");
						LyricList.map.put(fileName, lyricInfo);
					}
				}
			}

		}
		if (handler != null) {
			Message msg = handler.obtainMessage();
			msg.obj = "扫描完成，新增歌曲" + count + "首";
			// Message从handler获取，可以直接向该handler对象发送消息
			msg.sendToTarget();
		}
		db.close();
	}

	/**
	 * 查新数据库记录的所有歌曲
	 */
	public void scanMusicFromDB() {
		db = new DBDao(context);
		db.queryAll(searchAllDirectory());
		db.close();
	}

	/**
	 * 关键一步，获取MP3详细信息，比如歌名、歌手、比特率之类的
	 * 
	 * @param path
	 *            文件路径
	 */
	private MusicInfo scanMusicTag(String fileName, String path) {
		File file = new File(path);
		MusicInfo info = new MusicInfo();
		if (path.startsWith(Constant.WEB_FOLDER)) {
			info.setUrl(Constant.WEB_FOLDER);
		} else {
			info.setUrl("");
		}
		if (file.exists()) {
			try {
				MP3File mp3File = (MP3File) AudioFileIO.read(file);
				MP3AudioHeader header = mp3File.getMP3AudioHeader();

				info.setFile(fileName);
				// 时长(此处可能与MediaPlayer获得长度不一致，但误差不大)
				info.setTime(FormatUtil.formatTime((int) (header
						.getTrackLength() * 1000)));
				info.setSize(FormatUtil.formatSize(file.length()));// 大小
				info.setPath(path);// 路径
				info.setFormat("格式: " + header.getEncodingType());// 格式(编码类型)
				final String channels = header.getChannels();
				if (channels.equals("Joint Stereo")) {
					info.setChannels("声道: 立体声");
				} else {
					info.setChannels("声道: " + header.getChannels());// 声道
				}
				info.setKbps("比特率: " + header.getBitRate() + "Kbps");// 比特率
				info.setHz("采样率: " + header.getSampleRate() + "Hz");// 采样率

				if (mp3File.hasID3v1Tag()) {
					Tag tag = mp3File.getTag();
					try {
						final String tempName = tag.getFirst(FieldKey.TITLE);
						if (tempName == null || tempName.equals("")) {
							info.setName(fileName);// 扫描不到存文件名
						} else {
							info.setName(tempName);// 歌名,直接传入即可，sb
						}
					} catch (KeyNotFoundException e) {
						// TODO Auto-generated catch block
						info.setName(fileName);// 扫描出错存文件名
					}

					try {
						final String tempArtist = tag.getFirst(FieldKey.ARTIST);
						if (tempArtist == null || tempArtist.equals("")) {
							info.setArtist("未知艺术家");
						} else {
							info.setArtist(tempArtist);// 艺术家
						}
					} catch (KeyNotFoundException e) {
						// TODO Auto-generated catch block
						info.setArtist("未知艺术家");
					}

					try {
						final String tempAlbum = tag.getFirst(FieldKey.ALBUM);
						if (tempAlbum == null || tempAlbum.equals("")) {
							info.setAlbum("专辑: 未知");
						} else {
							info.setAlbum("专辑: " + tempAlbum);// 专辑
						}
					} catch (KeyNotFoundException e) {
						// TODO Auto-generated catch block
						info.setAlbum("专辑: 未知");
					}

					try {
						final String tempYears = tag.getFirst(FieldKey.YEAR);
						if (tempYears == null || tempYears.equals("")) {
							info.setYears("年代: 未知");
						} else {
							info.setYears("年代: " + tempYears);// 年代
						}
					} catch (KeyNotFoundException e) {
						// TODO Auto-generated catch block
						info.setYears("年代: 未知");
					}

					try {
						final String tempGener = tag.getFirst(FieldKey.GENRE);
						if (tempGener == null || tempGener.equals("")) {
							info.setGenre("风格: 未知");
						} else {
							info.setGenre("风格: " + tempGener);// 风格
						}
					} catch (KeyNotFoundException e) {
						// TODO Auto-generated catch block
						info.setGenre("风格: 未知");
					}
				} else if (mp3File.hasID3v2Tag()) {// 如果上面的条件不成立，才执行下面的方法
					AbstractID3v2Tag v2Tag = mp3File.getID3v2Tag();
					try {
						final String tempName = v2Tag.getFirst(FieldKey.TITLE);
						if (tempName == null || tempName.equals("")) {
							info.setName(fileName);// 扫描不到存文件名
						} else {
							info.setName(tempName);// 歌名
						}
					} catch (KeyNotFoundException e) {
						// TODO Auto-generated catch block
						info.setName(fileName);// 扫描出错存文件名
					}

					try {
						final String tempArtist = v2Tag
								.getFirst(FieldKey.ARTIST);
						if (tempArtist == null || tempArtist.equals("")) {
							info.setArtist("未知艺术家");
						} else {
							info.setArtist(tempArtist);// 艺术家
						}
					} catch (KeyNotFoundException e) {
						// TODO Auto-generated catch block
						info.setArtist("未知艺术家");
					}

					try {
						final String tempAlbum = v2Tag.getFirst(FieldKey.ALBUM);
						if (tempAlbum == null || tempAlbum.equals("")) {
							info.setAlbum("专辑: 未知");
						} else {
							info.setAlbum("专辑: " + tempAlbum);// 专辑
						}
					} catch (KeyNotFoundException e) {
						// TODO Auto-generated catch block
						info.setAlbum("专辑: 未知");
					}

					try {
						final String tempYears = v2Tag.getFirst(FieldKey.YEAR);
						if (tempYears == null || tempYears.equals("")) {
							info.setYears("年代: 未知");
						} else {
							info.setYears("年代: " + tempYears);// 年代
						}
					} catch (KeyNotFoundException e) {
						// TODO Auto-generated catch block
						info.setYears("年代: 未知");
					}

					try {
						final String tempGener = v2Tag.getFirst(FieldKey.GENRE);
						if (tempGener == null || tempGener.equals("")) {
							info.setGenre("风格: 未知");
						} else {
							info.setGenre("风格: " + tempGener);// 风格
						}
					} catch (KeyNotFoundException e) {
						// TODO Auto-generated catch block
						info.setGenre("风格: 未知");
					}
				} else {
					info.setName(fileName);
					info.setArtist("未知艺术家");
					info.setAlbum("专辑: 未知");
					info.setYears("年代: 未知");
					info.setGenre("风格: 未知");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return info;
	}

}
