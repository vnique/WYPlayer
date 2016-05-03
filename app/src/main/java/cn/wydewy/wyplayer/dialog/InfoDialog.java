package cn.wydewy.wyplayer.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import cn.wydewy.wyplayer.R;
import cn.wydewy.wyplayer.entity.LyricInfo;
import cn.wydewy.wyplayer.entity.MusicInfo;

/**
 * By wydewy 2013 Open Source Project
 * 
 * <br>
 * <b>显示歌曲详细信息对话框</b></br>
 * 
 * <br>
 * 带电视机开关效果的弹出对话框</br>
 * 
 * @author wydewy
 * @version 2013.08.05 v1.0 显示对话框界面
 */
public class InfoDialog extends TVAnimDialog {

	private TextView name;
	private TextView artist;
	private TextView album;
	private TextView genre;
	private TextView time;
	private TextView format;
	private TextView kbps;
	private TextView size;
	private TextView years;
	private TextView hz;
	private TextView path;
	private Button button;
	private TextView lyrtype;

	public InfoDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public InfoDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}

	protected InfoDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_info);

		name = (TextView) findViewById(R.id.dialog_info_name);
		artist = (TextView) findViewById(R.id.dialog_info_artist);
		album = (TextView) findViewById(R.id.dialog_info_album);
		genre = (TextView) findViewById(R.id.dialog_info_genre);
		lyrtype = (TextView) findViewById(R.id.dialog_info_lyrtype);
		time = (TextView) findViewById(R.id.dialog_info_time);
		format = (TextView) findViewById(R.id.dialog_info_format);
		kbps = (TextView) findViewById(R.id.dialog_info_kbps);
		size = (TextView) findViewById(R.id.dialog_info_size);
		years = (TextView) findViewById(R.id.dialog_info_years);
		hz = (TextView) findViewById(R.id.dialog_info_hz);
		path = (TextView) findViewById(R.id.dialog_info_path);
		button = (Button) findViewById(R.id.dialog_info_btn_ok);

		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dismiss();
			}
		});

	}

	/**
	 * 设置歌曲信息集合并显示集合里的信息
	 * 
	 * @param info
	 *            歌曲信息集合
	 */
	public void setInfo(MusicInfo info) {
		name.setText("歌曲: " + info.getName());
		artist.setText("歌手: " + info.getArtist());
		album.setText(info.getAlbum());
		genre.setText(info.getGenre());
//		lyrtype.setText("歌词类型："+lyricInfo.getLyrtype());
		time.setText("时长: " + info.getTime());
		format.setText(info.getFormat());
		kbps.setText(info.getKbps());
		size.setText("大小: " + info.getSize());
		years.setText(info.getYears());
		hz.setText(info.getHz());
		path.setText("路径: " + info.getPath());
	}

}
