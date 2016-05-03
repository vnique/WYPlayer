package cn.wydewy.wyplayer.util;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;

/**
 * By wydewy 2013 Open Source Project
 * 
 * <br>
 * <b>进行一些转换工作</b></br>
 * 
 * @author wydewy
 * @version 2013.05.18 v1.0
 */
public class FormatUtil {

	/**
	 * 格式化时间
	 * 
	 * @param time
	 *            时间值
	 * @return 时间
	 */
	public static String formatTime(int time) {
		// TODO Auto-generated method stub
		if (time == 0) {
			return "00:00";
		}
		time = time / 1000;
		int m = time / 60 % 60;
		int s = time % 60;
		return (m > 9 ? m : "0" + m) + ":" + (s > 9 ? s : "0" + s);
	}

	/**
	 * 格式化文件大小
	 * 
	 * @param size
	 *            文件大小值
	 * @return 文件大小
	 */
	public static String formatSize(long size) {
		// TODO Auto-generated method stub
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSize = "0B";
		if (size < 1024) {
			fileSize = df.format((double) size) + "B";
		} else if (size < 1048576) {
			fileSize = df.format((double) size / 1024) + "KB";
		} else if (size < 1073741824) {
			fileSize = df.format((double) size / 1048576) + "MB";
		} else {
			fileSize = df.format((double) size / 1073741824) + "GB";
		}
		return fileSize;
	}

	/**
	 * 对乱码的处理
	 * 
	 * @param s
	 *            原字符串
	 * @return GBK处理后的数据
	 */
	public static String formatGBKStr(String s) {
		String str = null;
		try {
			str = new String(s.getBytes("ISO-8859-1"), "GB2312");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str;
	}

}
