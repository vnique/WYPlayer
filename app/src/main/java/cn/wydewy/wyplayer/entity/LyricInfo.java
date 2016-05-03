package cn.wydewy.wyplayer.entity;

import android.util.Log;

public class LyricInfo {
	private String file;// 文件名(通过文件名来作为唯一判断并且获得歌词)
	private String path;// 路径
	private String lyrtype;// 歌词类型 //wydewy:2015-08-29,添加歌词类型

	public LyricInfo(String file){
		this.file = file;
	}
	public LyricInfo(String file,String path){
		this.file = file;
		this.path = path;
	}
	
	public LyricInfo(String file,String path,String lyrtype){
		this.file = file;
		this.path = path;
		this.lyrtype = lyrtype;
	}
	public String getLyrtype() {
		return lyrtype;
	}

	public void setLyrtype(String lyrType) {
		Log.i("TAG", "MusicInfo.setLyrtype---->lyrType:" + lyrType);
		this.lyrtype = lyrType;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
