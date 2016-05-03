package cn.wydewy.wyplayer.entity;

/**
 * By wydewy 2013 Open Source Project
 * 
 * <br>
 * <b>扫描信息，SD卡路径和用户是否勾选</b></br>
 * 
 * @author wydewy
 * @version 2013.05.18 v1.0
 */
public class ScanInfo {

	private String folderPath;// 文件夹路径
	private boolean isChecked;// 用户是否勾选

	public ScanInfo(String folderPath, boolean isChecked) {
		// TODO Auto-generated constructor stub
		this.folderPath = folderPath;
		this.isChecked = isChecked;
	}

	public String getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

}
