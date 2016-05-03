package cn.wydewy.wyplayer.lyric;

import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.ParsingDetector;
import info.monitorenter.cpdetector.io.UnicodeDetector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;
import android.widget.Toast;

/**
 * By wydewy 2013 Open Source Project
 * 
 * <br>
 * <b>歌词解析器</b></br>
 * 
 * 文件的编码问题确实比较蛋疼，原先是通过EncodeUtil(已删除)来判断，但是问题也很多，比如ANSI的文件可能判断为Big5，
 * 也就是繁体中文的编码，貌似Android内部不支持Big5，所以依旧乱码。<br>
 * <br>
 * 
 * 
 * 网上找到了更为准确的jar包，主要是antlr-2.7.4、chardet-1.0、cpdetector_1.0.10三个包。
 * 具体使用方法参见http://blog.csdn.net/qqbitqqbit/article/details/9114469<br>
 * <br>
 * 
 * EncodeUtil源码来自http://www.eoeandroid.com/thread-244070-1-1.html
 * 
 * @author wydewy
 * @version 2013.06.23 v1.0 实现歌词的解析<br>
 *          2013.07.22 v1.1 更替判断文件编码的方法，实现较为准确的判断
 */
public class LyricParser {

	private String lyricPath = null;
	private String lyrType = null;

	private List<LyricItem> lyricList = null;

	/**
	 * 构造函数
	 * 
	 * @param lyricPath
	 *            歌词路径
	 */
	public LyricParser(String lyricPath, String lyrType) {// wydewy:2015-08-29,添加歌词类型
		// TODO Auto-generated constructor stub
		this.lyricPath = lyricPath;
		this.lyrType = lyrType;
		this.lyricList = new ArrayList<LyricItem>();
	}

	public List<LyricItem> parser() throws Exception {
		String encode = "UTF-8";// 默认编码
		File file = new File(lyricPath);
		Log.i("TAG", "lyricPath:"+lyricPath+"lyrType:"+lyrType);
		CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();
		detector.add(new ParsingDetector(false));
		detector.add(JChardetFacade.getInstance());// /用到antlr.jar、chardet.jar
		detector.add(ASCIIDetector.getInstance());
		detector.add(UnicodeDetector.getInstance());
		Charset set = null;
		try {
			set = detector.detectCodepage(file.toURI().toURL());// 检测文件编码
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (set != null) {
			encode = set.name();
		}
		InputStream inputStream = new FileInputStream(file);
		InputStreamReader inputReader = new InputStreamReader(inputStream,
				encode);
		BufferedReader bufferedReader = new BufferedReader(inputReader);

		String temp = null;
		// 行数
		int line = 0;
		// 每一行存在的时间点个数
		int count = 0;
		// 每一行存在的时间的文字长度
		int timeLength = 0;

		// 暂存时间点数据，因为有的一行有多个时间，需要先提取所有时间，最后提取歌词
		ArrayList<Integer> timeTemp = new ArrayList<Integer>();
		Pattern pattern = null;
		if ("lrc".equals(lyrType)) {
			// 创建一个正则表达式对象用来lrc匹配[00:00.00]/[00:00:00]/[00:00]
			pattern = Pattern
					.compile("\\[\\s*[0-9]{1,2}\\s*:\\s*[0-5][0-9]\\s*[\\.:]?\\s*[0-9]?[0-9]?\\s*\\]");
		} else if ("qrc".equals(lyrType)) {

			// Pattern qrcLinePattern = Pattern
			// .compile("\\[\\s*[0-9999]\\s*,\\s*[0-9999]\\s*\\]");
			// 创建一个正则表达式对象用来lrc匹配[00:00.00]/[00:00:00]/[00:00]
			pattern = Pattern
					.compile("\\(\\s*[0-9999]\\s*,\\s*[0-9999]\\s*\\)");
		}

		String msg = null;
		// 一行一行读取
		while ((temp = bufferedReader.readLine()) != null) {
			line++;
			// 计算前清零
			count = 0;
			// 清除暂存列表
			timeTemp.clear();
			// 对一行进行匹配
			Matcher m = pattern.matcher(temp);
			// 如果匹配到字段处理
			while (m.find()) {
				count++;
				// 获取匹配到的字段
				String timeStr = m.group();
				timeStr = timeStr.substring(1, timeStr.length() - 1);
				timeLength = timeStr.length() + 2;// 时间文字的长度，用于后面截取
				// 根据匹配到的字段计算出时间
				int timeMill = time2ms(timeStr);
				// 加入列表
				timeTemp.add(timeMill);
			}
			// 如果存在时间点数据
			if (count > 0) {
				// 按从小到大顺序插入时间点数据
				for (int j = 0; j < timeTemp.size(); j++) {
					LyricItem item = new LyricItem();
					// 如果列表为空直接添加
					if (lyricList.size() == 0) {
						// 时间点数据添加到列表
						item.setTime(timeTemp.get(j));
						// 截取歌词字符串，第一样多一个“]”需特殊处理
						if (line == 1) {
							msg = temp.substring(timeLength * count + 1);
						} else {
							msg = temp.substring(timeLength * count);
						}
						// 歌词添加到列表
						item.setLyric(msg);
						lyricList.add(item);
					}
					// 如果时间大于列表中最后一个时间直接添加到结尾
					else if (timeTemp.get(j) > lyricList.get(
							lyricList.size() - 1).getTime()) {
						item.setTime(timeTemp.get(j));
						if (line == 1) {
							msg = temp.substring(timeLength * count + 1);
						} else {
							msg = temp.substring(timeLength * count);
						}
						item.setLyric(msg);
						lyricList.add(item);
					}
					// 否则按大小顺序插入
					else {
						for (int index = 0; index < lyricList.size(); index++) {
							if (timeTemp.get(j) <= lyricList.get(index)
									.getTime()) {
								item.setTime(timeTemp.get(j));
								if (line == 1) {
									msg = temp
											.substring(timeLength * count + 1);
								} else {
									msg = temp.substring(timeLength * count);
								}
								item.setLyric(msg);
								lyricList.add(index, item);
								break;
							}
						}
					}
				}
			}
		}
		bufferedReader.close();
		inputReader.close();
		inputStream.close();
		return lyricList;
	}

	/**
	 * LRC文件支持三种不同的时间格式 mm:ss.ms/mm:ss:ms/mm:ss
	 * 
	 * @param timeStr
	 *            分钟或者秒
	 * @return 转换成毫秒
	 */
	public int time2ms(String timeStr) {
		
		int min =0;
		int sec = 0;
		int mill = 0;
		if("lrc".equals(lyrType))
		{
			String s[] = timeStr.split(":");
			min = Integer.parseInt(s[0]);
			// 如果格式为mm:ss:ms
			if (s.length > 2) {
				sec = Integer.parseInt(s[1]);
				mill = Integer.parseInt(s[2]);
			} else {
				String ss[] = s[1].split("\\.");
				// 如果格式为mm:ss.ms
				if (ss.length > 1) {
					sec = Integer.parseInt(ss[0]);
					mill = Integer.parseInt(ss[1]);
				}
				// 如果格式为mm:ss
				else {
					sec = Integer.parseInt(ss[0]);
					mill = 0;
				}
			}
		}else 	if("qrc".equals(lyrType)){
			String s[] = timeStr.split(",");
			return Integer.parseInt(s[0]);
		}
			
		
		return min * 60 * 1000 + sec * 1000 + mill * 10;
	}

}
