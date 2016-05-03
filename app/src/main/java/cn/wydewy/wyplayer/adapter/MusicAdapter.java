package cn.wydewy.wyplayer.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.wydewy.wyplayer.MainActivity;
import cn.wydewy.wyplayer.R;
import cn.wydewy.wyplayer.list.FavoriteList;
import cn.wydewy.wyplayer.list.FolderList;
import cn.wydewy.wyplayer.list.MusicList;
import cn.wydewy.wyplayer.list.OnlineMusicList;

/**
 * By wydewy 2013 Open Source Project
 * <p>
 * <br>
 * <b>扫描列表适配器</b></br>
 *
 * @author wydewy
 * @version 2013.05.12 v1.0 实现列表适配 <br>
 *          2013.06.23 v1.1 在同一适配器中实现三种模式数据显示<br>
 *          2013.08.06 v1.2 实现对列表里的菜单和我的最爱的监听<br>
 *          2013.08.07 v1.3 实现文件夹模式的歌曲列表数据显示</br>
 */
public class MusicAdapter extends BaseAdapter implements OnClickListener {

    private int page = MainActivity.SLIDING_MENU_ALL;// 默认值
    private int folderPosition;
    private Context mContext;
    private LayoutInflater mInflater;

    /**
     * 构造函数
     *
     * @param context 上下文
     * @param page    页面状态，指SLIDING_MENU_ALL、SLIDING_MENU_FAVORITE、
     *                SLIDING_MENU_FOLDER、SLIDING_MENU_FOLDER_LIST
     */
    public MusicAdapter(Context context, int page) {
        // TODO Auto-generated constructor stub
        this.mContext = context;
        this.page = page;
        this.mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        int count = 0;
        switch (page) {
            case MainActivity.SLIDING_MENU_ALL:
                count = MusicList.list.size();
                break;

            case MainActivity.SLIDING_MENU_FAVORITE:
                count = FavoriteList.list.size();
                break;

            case MainActivity.SLIDING_MENU_ONLINE:
                count = OnlineMusicList.list.size();
                break;

            case MainActivity.SLIDING_MENU_FOLDER:
                count = FolderList.list.size();
                break;

            case MainActivity.SLIDING_MENU_FOLDER_LIST:
                count = FolderList.list.get(folderPosition).getMusicList().size();
                break;
        }
        return count;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.adapter_music_item, null);
            holder.view = (LinearLayout) convertView
                    .findViewById(R.id.adapter_music_item_view);
            holder.favorite = (ImageButton) convertView
                    .findViewById(R.id.adapter_music_item_ib_favorite);
            holder.name = (TextView) convertView
                    .findViewById(R.id.adapter_music_item_tv_name);
            holder.artist = (TextView) convertView
                    .findViewById(R.id.adapter_music_item_tv_artist);
            holder.time = (TextView) convertView
                    .findViewById(R.id.adapter_music_item_tv_time);
            holder.menu = (ImageButton) convertView
                    .findViewById(R.id.adapter_music_item_ib_menu);
            holder.folder = (TextView) convertView
                    .findViewById(R.id.adapter_music_item_tv_folder);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        switch (page) {
            case MainActivity.SLIDING_MENU_ALL:
                if (holder.view.getVisibility() != View.VISIBLE) {
                    holder.view.setVisibility(View.VISIBLE);
                }
                holder.name.setText((position + 1) + ". "
                        + MusicList.list.get(position).getName());
                holder.artist.setText(MusicList.list.get(position).getArtist());
                holder.time.setText(MusicList.list.get(position).getTime());
                holder.favorite.setImageResource(MusicList.list.get(position)
                        .isFavorite() ? R.drawable.music_item_btn_favourite_pressed
                        : R.drawable.music_item_btn_favourite_normal);
                if (holder.folder.getVisibility() == View.VISIBLE) {
                    holder.folder.setVisibility(View.GONE);
                }

                holder.menu.setTag(position);
                holder.favorite.setTag(position);
                holder.menu.setOnClickListener(this);
                holder.favorite.setOnClickListener(this);
                break;

            case MainActivity.SLIDING_MENU_FAVORITE:
                if (holder.view.getVisibility() != View.VISIBLE) {
                    holder.view.setVisibility(View.VISIBLE);
                }
                holder.name.setText((position + 1) + ". "
                        + FavoriteList.list.get(position).getName());
                holder.artist.setText(FavoriteList.list.get(position).getArtist());
                holder.time.setText(FavoriteList.list.get(position).getTime());
                holder.favorite.setImageResource(FavoriteList.list.get(position)
                        .isFavorite() ? R.drawable.music_item_btn_favourite_pressed
                        : R.drawable.music_item_btn_favourite_normal);
                if (holder.folder.getVisibility() == View.VISIBLE) {
                    holder.folder.setVisibility(View.GONE);
                }

                holder.menu.setTag(position);
                holder.favorite.setTag(position);
                holder.menu.setOnClickListener(this);
                holder.favorite.setOnClickListener(this);
                break;

            case MainActivity.SLIDING_MENU_ONLINE:
                if (holder.view.getVisibility() != View.VISIBLE) {
                    holder.view.setVisibility(View.VISIBLE);
                }
                holder.name.setText((position + 1) + ". "
                        + OnlineMusicList.list.get(position).getName());
                holder.artist.setText(OnlineMusicList.list.get(position).getArtist());
                holder.time.setText(OnlineMusicList.list.get(position).getTime());
                if (holder.folder.getVisibility() == View.VISIBLE) {
                    holder.folder.setVisibility(View.GONE);
                }

                holder.menu.setTag(position);
                holder.favorite.setTag(position);
                holder.menu.setOnClickListener(this);
                holder.favorite.setOnClickListener(this);
                break;

            case MainActivity.SLIDING_MENU_FOLDER:
                if (holder.folder.getVisibility() != View.VISIBLE) {
                    holder.folder.setVisibility(View.VISIBLE);
                }
                if (holder.view.getVisibility() == View.VISIBLE) {
                    holder.view.setVisibility(View.GONE);
                }
                holder.folder.setText(FolderList.list.get(position)
                        .getMusicFolder());
                break;

            case MainActivity.SLIDING_MENU_FOLDER_LIST:
                if (holder.view.getVisibility() != View.VISIBLE) {
                    holder.view.setVisibility(View.VISIBLE);
                }
                holder.name.setText((position + 1)
                        + ". "
                        + FolderList.list.get(folderPosition).getMusicList()
                        .get(position).getName());
                holder.artist.setText(FolderList.list.get(folderPosition)
                        .getMusicList().get(position).getArtist());
                holder.time.setText(FolderList.list.get(folderPosition)
                        .getMusicList().get(position).getTime());
                holder.favorite.setImageResource(FolderList.list
                        .get(folderPosition).getMusicList().get(position)
                        .isFavorite() ? R.drawable.music_item_btn_favourite_pressed
                        : R.drawable.music_item_btn_favourite_normal);
                if (holder.folder.getVisibility() == View.VISIBLE) {
                    holder.folder.setVisibility(View.GONE);
                }

                holder.menu.setTag(position);
                holder.favorite.setTag(position);
                holder.menu.setOnClickListener(this);
                holder.favorite.setOnClickListener(this);
                break;
        }

        return convertView;
    }

    static class ViewHolder {
        LinearLayout view;
        ImageButton favorite;
        ImageButton menu;
        TextView name;
        TextView artist;
        TextView time;
        TextView folder;
    }

    /**
     * 刷新数据
     *
     * @param page 页面状态，指SLIDING_MENU_ALL、SLIDING_MENU_FAVORITE、
     *             SLIDING_MENU_FOLDER、SLIDING_MENU_FOLDER_LIST
     */
    public void update(int page) {
        this.page = page;
        notifyDataSetChanged();
    }

    public void setFolderPosition(int position) {
        this.folderPosition = position;
    }

    /**
     * 返回页面状态，指SLIDING_MENU_ALL、SLIDING_MENU_FAVORITE、SLIDING_MENU_FOLDER、
     * SLIDING_MENU_FOLDER_LIST
     *
     * @return
     */
    public int getPage() {
        return page;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        Intent intent = null;
        switch (v.getId()) {
            case R.id.adapter_music_item_ib_favorite:// 标记为我的最爱广播
                intent = new Intent(MainActivity.BROADCAST_ACTION_FAVORITE);
                break;

            case R.id.adapter_music_item_ib_menu:// 弹出菜单广播
                intent = new Intent(MainActivity.BROADCAST_ACTION_MENU);
                break;
        }
        if (intent != null) {
            intent.putExtra(MainActivity.BROADCAST_INTENT_PAGE, page);
            intent.putExtra(MainActivity.BROADCAST_INTENT_POSITION,
                    (Integer) v.getTag());
            mContext.sendBroadcast(intent);
        }
    }

}
