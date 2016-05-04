package cn.wydewy.wyplayer;

import java.io.File;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import cn.wydewy.wyplayer.adapter.MusicAdapter;
import cn.wydewy.wyplayer.adapter.SlidingAdapter;
import cn.wydewy.wyplayer.db.DBDao;
import cn.wydewy.wyplayer.dialog.DeleteDialog;
import cn.wydewy.wyplayer.dialog.InfoDialog;
import cn.wydewy.wyplayer.dialog.MenuDialog;
import cn.wydewy.wyplayer.dialog.ScanDialog;
import cn.wydewy.wyplayer.dialog.TVAnimDialog.OnTVAnimDialogDismissListener;
import cn.wydewy.wyplayer.entity.MusicInfo;
import cn.wydewy.wyplayer.list.CoverList;
import cn.wydewy.wyplayer.list.DownloadList;
import cn.wydewy.wyplayer.list.FavoriteList;
import cn.wydewy.wyplayer.list.FolderList;
import cn.wydewy.wyplayer.list.MusicList;
import cn.wydewy.wyplayer.list.OnlineMusicList;
import cn.wydewy.wyplayer.service.MediaBinder;
import cn.wydewy.wyplayer.service.MediaBinder.OnPlayCompleteListener;
import cn.wydewy.wyplayer.service.MediaBinder.OnPlayErrorListener;
import cn.wydewy.wyplayer.service.MediaBinder.OnPlayPauseListener;
import cn.wydewy.wyplayer.service.MediaBinder.OnPlayStartListener;
import cn.wydewy.wyplayer.service.MediaBinder.OnPlayingListener;
import cn.wydewy.wyplayer.service.MediaService;
import cn.wydewy.wyplayer.slidingmenu.SlidingListActivity;
import cn.wydewy.wyplayer.slidingmenu.SlidingMenu;
import cn.wydewy.wyplayer.util.Constant;
import cn.wydewy.wyplayer.util.FormatUtil;
import cn.wydewy.wyplayer.util.ScanUtil;
import cn.wydewy.wyplayer.view.FloatDialog;
import cn.wydewy.wyplayer.view.WydewyListView.SideClickListener;

/**
 * <br>
 * <b>歌曲列表页面</b></br>
 * <p/>
 * <p/>
 * 项目流程：LOGO界面->主界面->SlidingMenu->数据集->扫描界面->数据库->列表显示->MP3标签扫描<br>
 * ->主界面不同模式数据显示->音乐播放及数据交互->移植并实现均衡器效果VisualizerView->自定义歌词LyricView<br>
 * ->自定义动画效果PushView->实现音乐的播放控制及Service相对应的更改->各Dialog的对应显示<br>
 * ->实现主界面更为复杂和详尽的数据显示->实现仿Path菜单->实现其余动画特效->完成设置界面->优化和测试<br>
 * <br>
 * <p/>
 * 由于Android2.3以下不支持均衡器类，所以直接最低安装要求2.3以上，这样也省了我去适配2.3以前的小屏机。<br>
 * <p/>
 * 还存在一些问题，如对文件不存在的相关操作的逻辑自己都搞的混乱，扫描与数据库相关操作有可能会出错，
 * 播放可能产生错误，耳机线控几乎是个鸡肋，还有一些测试未发现的问题。不想写了，留给各位感兴趣的开发者了<br>
 * <br>
 * <p/>
 * 展示歌曲列表，SlidingMenu支撑隐藏列表 <br>
 *
 * @version 2014.05.10 v0.1.0 完成侧滑菜单模块的功能（已删除）<br>
 *          2014.06.03 v0.2.0 替换更新侧滑模块，完全替换版本v1.0<br>
 *          2014.06.24 v0.2.1 实现播放<br>
 *          2014.07.03 v0.2.2 修改BindService的时机，通过ServiceConnection更新UI<br>
 *          2014.07.29 v0.2.3 实现播放按钮的相关操作及程序的退出<br>
 *          2014.08.04 v0.2.4 实现各种Dialog对应的操作<br>
 *          2014.08.19 v0.2.5 实现快退、快进播放<br>
 *          2014.08.22 v0.2.6 新增一些广播消息的处理<br>
 *          2014.08.24 v0.2.7 实现更换背景图<br>
 *          2014.08.31 v0.3.0 修复真机上跳转后立即返回UI更新停滞的严重问题<br>
 *          2014.09.01 v0.3.1 实现对文件不存在的相关操作</br>
 *          <p/>
 *          2015.09.02 v1.1.2 修复对异常情况列表清空的处理
 */
@SuppressLint("InlinedApi")
public class MainActivity extends SlidingListActivity implements
        OnClickListener, OnLongClickListener, OnTouchListener,
        OnTVAnimDialogDismissListener, OnItemLongClickListener,
        cn.wydewy.wyplayer.view.FloatDialog.IDialogOnclickInterface {

    // 按照Item的顺序排列的，方便更新
    public static final int SLIDING_MENU_SEARCH = 0;// 侧滑->搜索歌曲
    public static final int SLIDING_MENU_SCAN = 1;// 侧滑->扫描歌曲
    public static final int SLIDING_MENU_ALL = 2;// 侧滑->全部歌曲
    public static final int SLIDING_MENU_ONLINE = 3;// 侧滑->在线歌曲
    public static final int SLIDING_MENU_FAVORITE = 4;// 侧滑->我的最爱
    public static final int SLIDING_MENU_FOLDER = 5;// 侧滑->文件夹
    public static final int SLIDING_MENU_DOWNLOAD = 8;// 侧滑->在线歌曲
    public static final int SLIDING_MENU_EXIT = 6;// 侧滑->退出程序
    public static final int SLIDING_MENU_FOLDER_LIST = 7;// 侧滑->文件夹->文件夹列表

    public static final int DIALOG_DISMISS = 0;// 对话框消失
    public static final int DIALOG_SCAN = 1;// 扫描对话框
    public static final int DIALOG_MENU_REMOVE = 2;// 歌曲列表移除对话框
    public static final int DIALOG_MENU_DELETE = 3;// 歌曲列表提示删除对话框
    public static final int DIALOG_MENU_INFO = 4;// 歌曲详情对话框
    public static final int DIALOG_DELETE = 5;// 歌曲删除对话框

    public static final String PREFERENCES_NAME = "settings";// SharedPreferences名称
    public static final String PREFERENCES_MODE = "mode";// 存储播放模式
    public static final String PREFERENCES_SCAN = "scan";// 存储是否扫描过
    public static final String PREFERENCES_SKIN = "skin";// 存储背景图
    public static final String PREFERENCES_LYRIC = "lyric";// 存储歌词高亮颜色

    public static final String BROADCAST_ACTION_SCAN = "cn.wydewy.wyplayer.action.scan";// 扫描广播标志
    public static final String BROADCAST_ACTION_MENU = "cn.wydewy.wyplayer.action.menu";// 弹出菜单广播标志
    public static final String BROADCAST_ACTION_FAVORITE = "cn.wydewy.wyplayer.action.favorite";// 喜爱广播标志
    public static final String BROADCAST_ACTION_EXIT = "cn.wydewy.wyplayer.action.exit";// 退出程序广播标志
    public static final String BROADCAST_INTENT_PAGE = "cn.wydewy.wyplayer.intent.page";// 页面状态
    public static final String BROADCAST_INTENT_POSITION = "cn.wydewy.wyplayer.intent.position";// 歌曲索引
    public static final String BROADCAST_ACTION_PLAY_ONLINE = "cn.wydewy.wyplayer.action.playonline";
    public static final String SET_MUSIC_DURATION = "cn.wydewy.wyplayer.action.setDuration";
    public static final String SHOW_ANIM = "cn.wydewy.wyplayer.action.showanim";
    public static final String BROADCAST_ACTION_UPDATE = "cn.wydewy.wyplayer.action.download";

    private final String TITLE_ALL = "播放列表";
    private final String TITLE_ONLINE = "在线播放";
    private final String TITLE_DOWNLOAD = "我下载的";
    private final String TITLE_FAVORITE = "我的最爱";
    private final String TITLE_FOLDER = "文件夹";
    private final String TITLE_NORMAL = "无音乐播放";
    private final String TIME_NORMAL = "00:00";

    private int skinId;// 背景图ID
    private int slidingPage = SLIDING_MENU_ALL;// 页面状态
    private int playerPage;// 发送给PlayerActivity的页面状态
    private int musicPosition;// 当前播放歌曲索引
    private int folderPosition;// 文件夹列表索引
    private int dialogMenuPosition;// 记住弹出歌曲列表菜单的歌曲索引

    private boolean canSkip = true;// 防止用户频繁点击造成多次解除服务绑定，true：允许解绑
    private boolean bindState = false;// 服务绑定状态

    private String mp3Current;// 歌曲当前时长
    private String mp3Duration;// 歌曲总时长
    private String dialogMenuPath;// 记住弹出歌曲列表菜单的歌曲路径

    private TextView mainTitle;// 列表标题
    private TextView mainSize;// 歌曲数量
    private TextView mainArtist;// 艺术家
    private TextView mainName;// 歌曲名称
    private TextView mainTime;// 歌曲时间
    private ImageView mainAlbum;// 专辑图片

    private ImageButton btnMenu;// 侧滑菜单按钮
    private ImageButton btnPrevious;// 上一首按钮
    private ImageButton btnPlay;// 播放和暂停按钮
    private ImageButton btnNext;// 下一首按钮

    private LinearLayout skin;// 背景图
    private LinearLayout viewBack;// 返回上一级
    private LinearLayout viewControl;// 底部播放控制视图

    private Intent playIntent;
    private MediaBinder binder;
    private MainReceiver receiver;
    private SlidingMenu slidingMenu;
    private MusicAdapter musicAdapter;
    private SharedPreferences preferences;
    private ServiceConnection serviceConnection;

    private Mp3Application application;
    private IMusic music;

    private ImageView gifView;
    private boolean isOnline;

    private ListView list;
    // ----------------长按相关---------------------
    protected int longClickPosition;
    protected FloatDialog floatDialog;
    protected View currentItemView;
    protected SideClickListener sideClickListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        application = (Mp3Application) getApplication();
        music = new IMusic() {

            @Override
            public void prevSong() {
                // TODO Auto-generated method stub
                if (binder != null) {
                    binder.setControlCommand(MediaService.CONTROL_COMMAND_PREVIOUS);
                    if (isOnline) {
                        showWaitImageAnimation();
                    }
                }
            }

            @Override
            public void play() {
                // TODO Auto-generated method stub
                if (binder != null) {
                    binder.setControlCommand(MediaService.CONTROL_COMMAND_PLAY);
                    if (isOnline) {
                        showWaitImageAnimation();
                    }
                }
            }

            @Override
            public void nextSong() {
                // TODO Auto-generated method stub
                if (binder != null) {
                    binder.setControlCommand(MediaService.CONTROL_COMMAND_NEXT);
                    if (isOnline) {
                        showWaitImageAnimation();
                    }
                }
            }

            @Override
            public void cancel() {
                // TODO Auto-generated method stub
                if (application != null) {
                    application.notManager.cancel(Constant.NOTI_CTRL_ID);
                }
            }

            @Override
            public void cancelDownload() {
                // TODO Auto-generated method stub
                if (application != null) {
                    application.notManager.cancel(2);
                }
            }
        };
        application.music = music;

        init();// 初始化

    }

    /*
     * 这里的部分本来是写在onStart里的，但是我发现在真机上点击跳转后立即返回不会执行onStart，但会执行onResume，
     * 但跳转后空2秒以上再返回，就会执行onStart，这种问题如何解释。各位可以试试，也许我对生命周期理解的不透彻。
     */
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        int id = preferences.getInt(MainActivity.PREFERENCES_SKIN,
                R.drawable.skin_bg1);
        if (skinId != id) {// 判断是否更换背景图
            skinId = id;
            skin.setBackgroundResource(skinId);
        }
        if (!bindState) {
            Intent intent = new Intent(MediaService.BROADCAST_ACTION_SERVICE);
            intent.putExtra(MediaService.INTENT_ACTIVITY,
                    MediaService.ACTIVITY_MAIN);
            sendBroadcast(intent);

            bindState = bindService(playIntent, serviceConnection,
                    Context.BIND_AUTO_CREATE);
        }

        // 如果异常情况导致列表清空,wydewy,15.09.02
        if (bindState && MusicList.list.isEmpty()) {
            preferences = getSharedPreferences(PREFERENCES_NAME,
                    Context.MODE_PRIVATE);// 检查是否扫描过歌曲
            if (preferences.getBoolean(PREFERENCES_SCAN, false)) {
                Toast.makeText(getApplicationContext(), "爱唯一，爱音乐~",
                        Toast.LENGTH_SHORT).show();
                showWaitImageAnimation();
                Handler handler = new Handler();
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        DBDao db = new DBDao(MainActivity.this);
                        db.queryAll(new ScanUtil(MainActivity.this)
                                .searchAllDirectory());
                        if (musicAdapter != null) {
                            musicAdapter.update(SLIDING_MENU_ALL);
                        }

                    }
                });
            }
        }

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (serviceConnection != null) {
            if (bindState) {
                unbindService(serviceConnection);// 一定要解除绑定
            }
            serviceConnection = null;
        }
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    /*
     * 初始化所有相关工作
     */
    private void init() {
        initSlidingMenu();// 先初始化侧滑相关
        initActivity();// 再初始化主界面
        initEnvet();// 再初始化事件
        initServiceConnection();// 后初始化服务绑定

    }

    /*
     * 初始化侧滑相关
     *
     * <---设置SlidingMenu的几种手势模式--->
     *
     * TOUCHMODE_FULLSCREEN：全屏模式，在content页面中，滑动，可以打开SlidingMenu
     *
     * TOUCHMODE_MARGIN：边缘模式，在content页面中，如果想打开SlidingMenu，
     * 你需要在屏幕边缘滑动才可以打开SlidingMenu
     *
     * TOUCHMODE_NONE：自然是不能通过手势打开啦
     */
    private void initSlidingMenu() {
        setBehindContentView(R.layout.activity_main_sliding);

        slidingMenu = getSlidingMenu();
        slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        slidingMenu.setShadowWidth(20);

        ListView listView = (ListView) slidingMenu.getMenu().findViewById(
                R.id.activity_main_sliding_list);
        listView.setAdapter(new SlidingAdapter(getApplicationContext()));
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                if (viewBack.getVisibility() != View.GONE) {
                    viewBack.setVisibility(View.GONE);
                }
                switch (position) {
                    case SLIDING_MENU_SEARCH:// 搜索歌曲
                        intentSearchActivity();
                        break;
                    case SLIDING_MENU_SCAN:// 扫描歌曲
                        intentScanActivity();
                        break;

                    case SLIDING_MENU_ALL:// 全部歌曲
                        if (musicAdapter.getPage() != SLIDING_MENU_ALL) {
                            mainTitle.setText(TITLE_ALL);
                            musicAdapter.update(SLIDING_MENU_ALL);
                            mainSize.setText(musicAdapter.getCount() + "首歌曲");
                        }
                        break;

                    case SLIDING_MENU_ONLINE:// 在线歌曲
                        if (musicAdapter.getPage() != SLIDING_MENU_ONLINE) {
                            mainTitle.setText(TITLE_ONLINE);
                            musicAdapter.update(SLIDING_MENU_ONLINE);
                            mainSize.setText(musicAdapter.getCount() + "首歌曲");
                            isOnline = true;
                        }
                        break;
                    case SLIDING_MENU_DOWNLOAD:// 下载的歌曲
                        if (musicAdapter.getPage() != SLIDING_MENU_DOWNLOAD) {
                            mainTitle.setText(TITLE_DOWNLOAD);
                            musicAdapter.update(SLIDING_MENU_DOWNLOAD);
                            mainSize.setText(musicAdapter.getCount() + "首歌曲");
                            isOnline = true;
                        }
                        break;

                    case SLIDING_MENU_FAVORITE:// 我的最爱
                        if (musicAdapter.getPage() != SLIDING_MENU_FAVORITE) {
                            mainTitle.setText(TITLE_FAVORITE);
                            musicAdapter.update(SLIDING_MENU_FAVORITE);
                            mainSize.setText(musicAdapter.getCount() + "首歌曲");
                        }
                        break;

                    case SLIDING_MENU_FOLDER:// 文件夹
                        if (musicAdapter.getPage() != SLIDING_MENU_FOLDER) {
                            mainTitle.setText(TITLE_FOLDER);
                            musicAdapter.update(SLIDING_MENU_FOLDER);
                            mainSize.setText(musicAdapter.getCount() + "个文件夹");
                        }
                        break;

                    case SLIDING_MENU_EXIT:// 退出程序
                        exitProgram();
                        break;
                }
                toggle();// 关闭侧滑菜单
            }

        });
    }

    /*
     * 初始化主界面相关
     */
    private void initActivity() {
        skin = (LinearLayout) findViewById(R.id.activity_main_skin);
        btnMenu = (ImageButton) findViewById(R.id.activity_main_ib_menu);
        mainTitle = (TextView) findViewById(R.id.activity_main_tv_title);
        mainSize = (TextView) findViewById(R.id.activity_main_tv_count);
        mainArtist = (TextView) findViewById(R.id.activity_main_tv_artist);
        mainName = (TextView) findViewById(R.id.activity_main_tv_name);
        mainTime = (TextView) findViewById(R.id.activity_main_tv_time);
        mainAlbum = (ImageView) findViewById(R.id.activity_main_iv_album);
        viewBack = (LinearLayout) findViewById(R.id.activity_main_view_back);
        viewControl = (LinearLayout) findViewById(R.id.activity_main_view_bottom);
        btnPrevious = (ImageButton) findViewById(R.id.activity_main_ib_previous);
        btnPlay = (ImageButton) findViewById(R.id.activity_main_ib_play);
        btnNext = (ImageButton) findViewById(R.id.activity_main_ib_next);

        // 长按相关初始化
        floatDialog = new FloatDialog(this, R.style.floatDialogStyle, this);
        floatDialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface arg0) {
                // TODO Auto-generated method stub
            }
        });
        list = (ListView) findViewById(android.R.id.list);

        gifView = (ImageView) findViewById(R.id.activity_logo_gif);

        musicAdapter = new MusicAdapter(getApplicationContext(),
                SLIDING_MENU_ALL);
        setListAdapter(musicAdapter);
        mainSize.setText(musicAdapter.getCount() + "首歌曲");

        playIntent = new Intent(getApplicationContext(), MediaService.class);// 绑定服务
        receiver = new MainReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCAST_ACTION_SCAN);
        filter.addAction(BROADCAST_ACTION_PLAY_ONLINE);
        filter.addAction(BROADCAST_ACTION_UPDATE);// 下载需要更新界面了
        filter.addAction(SET_MUSIC_DURATION);
        filter.addAction(SHOW_ANIM);// 展示动画
        filter.addAction(BROADCAST_ACTION_MENU);
        filter.addAction(BROADCAST_ACTION_FAVORITE);
        filter.addAction(BROADCAST_ACTION_EXIT);
        registerReceiver(receiver, filter);

        preferences = getSharedPreferences(PREFERENCES_NAME,
                Context.MODE_PRIVATE);// 检查是否扫描过歌曲
        if (!preferences.getBoolean(PREFERENCES_SCAN, false)) {
            ScanDialog scanDialog = new ScanDialog(this);
            scanDialog.setDialogId(DIALOG_SCAN);
            scanDialog.setOnTVAnimDialogDismissListener(this);
            scanDialog.show();
        }
    }

    public void initEnvet() {
        mainTitle.setText(TITLE_ALL);
        mainName.setText(TITLE_NORMAL);
        mainTime.setText(TIME_NORMAL + " - " + TIME_NORMAL);
        viewBack.setOnClickListener(this);
        btnMenu.setOnClickListener(this);
        viewControl.setOnClickListener(this);
        btnPrevious.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnPrevious.setOnLongClickListener(this);
        btnNext.setOnLongClickListener(this);
        btnPrevious.setOnTouchListener(this);
        btnNext.setOnTouchListener(this);

        list.setOnItemLongClickListener(this);
    }

    /*
     * 初始化服务绑定
     */
    private void initServiceConnection() {
        serviceConnection = new ServiceConnection() {

            @Override
            public void onServiceDisconnected(ComponentName name) {
                // TODO Auto-generated method stub
                binder = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                // TODO Auto-generated method stub
                binder = (MediaBinder) service;
                if (binder != null) {
                    canSkip = true;// 重置
                    binder.setOnPlayStartListener(new OnPlayStartListener() {

                        @Override
                        public void onStart(MusicInfo info) {
                            // TODO Auto-generated method stub
                            playerPage = musicAdapter.getPage();
                            mainArtist.setText(info.getArtist());
                            mainName.setText(info.getName());
                            if (info.getTime() != null) {
                                mp3Duration = info.getTime();
                            }
                            if (mp3Duration != null
                                    && !"00:00".equals(mp3Duration)) {
                                info.setTime(mp3Duration);
                            }

                            if (mp3Current == null) {
                                mainTime.setText(TIME_NORMAL + " - "
                                        + mp3Duration);
                            } else {
                                mainTime.setText(mp3Current + " - "
                                        + mp3Duration);
                            }
                            if (CoverList.cover == null) {
                                mainAlbum.setImageResource(R.drawable.musicman);
                            } else {
                                mainAlbum.setImageBitmap(CoverList.cover);
                            }
                            btnPlay.setImageResource(R.drawable.main_btn_pause);
                        }
                    });
                    binder.setOnPlayingListener(new OnPlayingListener() {

                        @Override
                        public void onPlay(int currentPosition) {
                            // TODO Auto-generated method stub
                            mp3Current = FormatUtil.formatTime(currentPosition);

                            mainTime.setText(mp3Current + " - " + mp3Duration);
                        }
                    });
                    binder.setOnPlayPauseListener(new OnPlayPauseListener() {

                        @Override
                        public void onPause() {
                            // TODO Auto-generated method stub
                            btnPlay.setImageResource(R.drawable.main_btn_play);
                        }
                    });
                    binder.setOnPlayCompletionListener(new OnPlayCompleteListener() {

                        @Override
                        public void onPlayComplete() {
                            // TODO Auto-generated method stub
                            mp3Current = null;
                        }
                    });
                    binder.setOnPlayErrorListener(new OnPlayErrorListener() {

                        @Override
                        public void onPlayError() {
                            // TODO Auto-generated method stub
                            dialogMenuPosition = musicPosition;
                            removeList();// 文件已经不存在必须从列表移除
                        }
                    });
                    binder.setLyricView(null, true);// 无歌词视图
                }
            }
        };
    }

    /**
     * 带返回值跳转至扫描页面
     */
    private void intentScanActivity() {
        Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
        startActivity(intent);
    }

    /**
     * 带返回值跳转至搜索页面
     */
    private void intentSearchActivity() {
        // TODO Auto-generated method stub
        Intent intent = new Intent(getApplicationContext(),
                SearchActivity.class);
        startActivity(intent);
    }

    /**
     * 从当前歌曲列表中移除
     */
    private void removeList() {
        MusicInfo info = null;
        int size = 0;
        isOnline = false;
        slidingPage = musicAdapter.getPage();
        switch (slidingPage) {
            case MainActivity.SLIDING_MENU_ALL:
                size = MusicList.list.size();
                info = MusicList.list.get(dialogMenuPosition);
                break;

            case MainActivity.SLIDING_MENU_FAVORITE:
                size = FavoriteList.list.size();
                info = FavoriteList.list.get(dialogMenuPosition);
                break;
            case MainActivity.SLIDING_MENU_ONLINE:
                size = OnlineMusicList.list.size();
                info = OnlineMusicList.list.get(dialogMenuPosition);
                isOnline = true;
                break;
            case MainActivity.SLIDING_MENU_DOWNLOAD:
                size = DownloadList.list.size();
                info = DownloadList.list.get(dialogMenuPosition);
                break;

            case MainActivity.SLIDING_MENU_FOLDER_LIST:
                size = FolderList.list.get(folderPosition).getMusicList().size();
                info = FolderList.list.get(folderPosition).getMusicList()
                        .get(dialogMenuPosition);
                break;
        }
        if (dialogMenuPath == null) {
            dialogMenuPath = info.getPath();
        }
        MusicList.list.remove(info);
        FavoriteList.list.remove(info);
        OnlineMusicList.list.remove(info);
        DownloadList.list.remove(info);
        for (int i = 0; i < FolderList.list.size(); i++) {
            FolderList.list.get(i).getMusicList().remove(info);
        }
        if (slidingPage != SLIDING_MENU_FOLDER) {
            musicAdapter.update(slidingPage);
            mainSize.setText(musicAdapter.getCount() + "首歌曲");
        }
        DBDao db = new DBDao(getApplicationContext());
        if (binder != null && musicPosition == dialogMenuPosition) {
            if (musicPosition == (size - 1)) {
                // binder.setControlCommand(MediaService.CONTROL_COMMAND_PREVIOUS);
            } else {
                // playIntent.putExtra(MediaService.INTENT_LIST_PAGE,
                // slidingPage);
                // playIntent.putExtra(MediaService.INTENT_LIST_POSITION,
                // musicPosition);
                // startService(playIntent);// 从当前position处播放
            }
        }
        db.delete(dialogMenuPath);// 从数据库中删除
        db.close();// 必须关闭

    }

    public void removeMusicInAllList(MusicInfo info) {
        MusicList.list.remove(info);
        FavoriteList.list.remove(info);
        OnlineMusicList.list.remove(info);
        DownloadList.list.remove(info);
        for (int i = 0; i < FolderList.list.size(); i++) {
            FolderList.list.get(i).getMusicList().remove(info);
        }
        DBDao db = new DBDao(getApplicationContext());
        db.delete(info.getPath());// 从数据库中删除
        db.close();// 必须关闭
    }

    /**
     * 文件(夹)的删除
     */
    private void deleteFile() {
        if ("".equals(dialogMenuPath)) {// 文件夹下所有文件
            final List<MusicInfo> list = FolderList.list.get(longClickPosition)
                    .getMusicList();
            boolean ok = false;
            for (final MusicInfo info : list) {
                String path = info.getPath();
                File file = new File(path);

                boolean isDelete = file.delete();
                ok &= isDelete;
                if (isDelete) {

                    removeMusicInAllList(info);
                }
            }
            if (ok) {
                FolderList.list.remove(longClickPosition);
                Toast.makeText(getApplicationContext(), "都已被删除",
                        Toast.LENGTH_SHORT).show();
                musicAdapter.update(slidingPage);
            } else {
                Toast.makeText(getApplicationContext(), "小唯没有权限删除《*.*》~",
                        Toast.LENGTH_LONG).show();
            }

        } else {
            File file = new File(dialogMenuPath);
            if (file.delete()) {
                Toast.makeText(getApplicationContext(), "文件已被删除",
                        Toast.LENGTH_LONG).show();
                removeList();// 删除后还得更新列表
            } else {
                Toast.makeText(getApplicationContext(), "小唯没有权限删除《*.*》~",
                        Toast.LENGTH_LONG).show();
            }

        }
    }

    /**
     * 退出程序
     */
    private void exitProgram() {
        stopService(playIntent);
        finish();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.activity_main_view_back:// 返回上一级监听
                viewBack.setVisibility(View.GONE);
                mainTitle.setText(TITLE_FOLDER);
                musicAdapter.update(SLIDING_MENU_FOLDER);
                mainSize.setText(musicAdapter.getCount() + "个文件夹");
                break;

            case R.id.activity_main_ib_menu:// 侧滑按钮监听
                showMenu();
                break;

            case R.id.activity_main_view_bottom:// 底部播放控制视图监听
                if (serviceConnection != null && canSkip) {
                    canSkip = false;
                    unbindService(serviceConnection);// 一定要解除绑定
                    bindState = false;// 将状态更新为解除绑定
                }
                Intent intent = new Intent(getApplicationContext(),
                        PlayerActivity.class);
                intent.putExtra(BROADCAST_INTENT_POSITION, musicPosition);
                startActivity(intent);
                break;

            case R.id.activity_main_ib_previous:// 上一首按钮监听

                if (binder != null) {
                    binder.setControlCommand(MediaService.CONTROL_COMMAND_PREVIOUS);

                }
                break;

            case R.id.activity_main_ib_play:// 播放按钮监听

                if (binder != null) {
                    binder.setControlCommand(MediaService.CONTROL_COMMAND_PLAY);
                }
                break;

            case R.id.activity_main_ib_next:// 下一首按钮监听

                if (binder != null) {
                    binder.setControlCommand(MediaService.CONTROL_COMMAND_NEXT);
                }
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.activity_main_ib_previous:// 快退
                if (binder != null) {
                    binder.setControlCommand(MediaService.CONTROL_COMMAND_REWIND);
                }
                break;

            case R.id.activity_main_ib_next:// 快进
                if (binder != null) {
                    binder.setControlCommand(MediaService.CONTROL_COMMAND_FORWARD);
                }
                break;
        }
        return true;// 返回true，不准再执行onClick
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        if (binder != null && event.getAction() == MotionEvent.ACTION_UP) {
            binder.setControlCommand(MediaService.CONTROL_COMMAND_REPLAY);
        }
        return false;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);
        slidingPage = musicAdapter.getPage();
        playIntent.putExtra(MediaService.INTENT_LIST_PAGE, slidingPage);
        musicPosition = position;
        switch (slidingPage) {
            case SLIDING_MENU_FOLDER:// 文件夹
                folderPosition = position;
                viewBack.setVisibility(View.VISIBLE);
                mainTitle.setText(FolderList.list.get(folderPosition)
                        .getMusicFolder());
                musicAdapter.setFolderPosition(folderPosition);
                musicAdapter.update(SLIDING_MENU_FOLDER_LIST);
                mainSize.setText(musicAdapter.getCount() + "首歌曲");
                return;// 不执行播放

            case SLIDING_MENU_FOLDER_LIST:// 文件夹歌曲列表,准备播放//wydewy
                playIntent.putExtra(MediaService.INTENT_FOLDER_POSITION,
                        folderPosition);
                break;
            case SLIDING_MENU_ONLINE:// 网络歌曲列表,准备播放//wydewy
                playIntent.putExtra(MediaService.INTENT_FOLDER_POSITION,
                        folderPosition);
                showWaitImageAnimation();
                break;
            case SLIDING_MENU_DOWNLOAD:// 下载的歌曲列表,准备播放//wydewy
                playIntent.putExtra(MediaService.INTENT_FOLDER_POSITION,
                        folderPosition);
                break;
        }
        playIntent.putExtra(MediaService.INTENT_LIST_POSITION, musicPosition);
        startService(playIntent);
    }

    @Override
    public void onDismiss(int dialogId) {
        // TODO Auto-generated method stub
        switch (dialogId) {
            case DIALOG_SCAN:// 跳转至扫描页面
                intentScanActivity();
                break;

            case DIALOG_MENU_REMOVE:// 执行移除
                removeList();
                break;

            case DIALOG_MENU_DELETE:// 显示删除对话框
                DeleteDialog deleteDialog = new DeleteDialog(this);
                deleteDialog.setOnTVAnimDialogDismissListener(this);
                deleteDialog.show();
                break;

            case DIALOG_MENU_INFO:// 显示歌曲详情
                InfoDialog infoDialog = new InfoDialog(this);
                infoDialog.setOnTVAnimDialogDismissListener(this);
                infoDialog.show();
                switch (slidingPage) {// 必须在show后执行
                    case MainActivity.SLIDING_MENU_ALL:
                        infoDialog.setInfo(MusicList.list.get(dialogMenuPosition));
                        break;

                    case MainActivity.SLIDING_MENU_FAVORITE:
                        infoDialog.setInfo(FavoriteList.list.get(dialogMenuPosition));
                        break;

                    case MainActivity.SLIDING_MENU_ONLINE:
                        infoDialog
                                .setInfo(OnlineMusicList.list.get(dialogMenuPosition));
                        break;
                    case MainActivity.SLIDING_MENU_DOWNLOAD:
                        infoDialog.setInfo(DownloadList.list.get(dialogMenuPosition));
                        break;

                    case MainActivity.SLIDING_MENU_FOLDER_LIST:
                        infoDialog.setInfo(FolderList.list.get(folderPosition)
                                .getMusicList().get(dialogMenuPosition));
                        break;
                }
                break;

            case DIALOG_DELETE:// 执行删除
                deleteFile();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            toggle();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 播放缓冲动画
     */
    private void showWaitImageAnimation() {
        AnimationSet animationset = new AnimationSet(false);

        ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f,
                1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setInterpolator(new OvershootInterpolator(5F));// 弹出再回来的动画的效果
        scaleAnimation.setDuration(4000);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation.setDuration(1500);
        alphaAnimation.setStartOffset(2000);

        animationset.addAnimation(scaleAnimation);
        animationset.addAnimation(alphaAnimation);
        animationset.setFillAfter(true);

        animationset.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
                mainAlbum.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                gifView.setVisibility(View.GONE);
                mainAlbum.setVisibility(View.VISIBLE);
            }
        });
        gifView.setVisibility(View.VISIBLE);
        gifView.startAnimation(animationset);
    }

    /**
     * 用于接收歌曲列表菜单及将歌曲标记为最爱的广播
     */
    private class MainReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            // TODO Auto-generated method stub
            if (intent != null) {
                final String action = intent.getAction();

                if (action.equals(BROADCAST_ACTION_EXIT)) {
                    exitProgram();
                    return;
                } else if (action.equals(BROADCAST_ACTION_SCAN)) {
                    if (musicAdapter != null) {
                        // 从扫描页面返回的更新全部歌曲列表数据
                        musicAdapter.update(SLIDING_MENU_ALL);
                        mainSize.setText(musicAdapter.getCount() + "首歌曲");
                    }
                    return;
                } else if (action.equals(BROADCAST_ACTION_UPDATE)) {// wydewy
                    mainTitle.setText(TITLE_ALL);
                    mainSize.setText(musicAdapter.getCount() + "首歌曲");
                    musicAdapter.update(SLIDING_MENU_ALL);
                } else if (action.equals(BROADCAST_ACTION_PLAY_ONLINE)) {// wydewy
                    if (musicAdapter != null) {
                        // 从扫描页面返回的更新全部歌曲列表数据
                        mainTitle.setText(TITLE_ALL);
                        mainSize.setText(musicAdapter.getCount() + "首歌曲");
                        musicAdapter.update(SLIDING_MENU_ALL);
                        // 播放搜索到的歌曲
                        String name = intent.getStringExtra("name");
                        if ("1".equals(name)) {// 下载完歌曲跳转到这里
                            mainTitle.setText(TITLE_ALL);
                            mainSize.setText(musicAdapter.getCount() + "首歌曲");
                            musicAdapter.update(SLIDING_MENU_ALL);
                        } else {// 这种情况是播放网络歌曲
                            for (int i = 0; i < MusicList.list.size(); i++) {
                                if (MusicList.list.get(i).getName()
                                        .equals(name)) {
                                    playIntent.putExtra(
                                            MediaService.INTENT_LIST_PAGE,
                                            SLIDING_MENU_ALL);
                                    playIntent
                                            .putExtra(
                                                    MediaService.INTENT_FOLDER_POSITION,
                                                    i);
                                    playIntent.putExtra(
                                            MediaService.INTENT_LIST_POSITION,
                                            i);
                                    Handler handler = new Handler();
                                    showWaitImageAnimation();
                                    handler.postDelayed(new Runnable() {

                                        @Override
                                        public void run() {
                                            // TODO Auto-generated method stub
                                            startService(playIntent);
                                        }
                                    }, 1500);
                                    break;
                                }
                            }
                        }

                    }
                }

                // 设置时长
                if (action.equals(SET_MUSIC_DURATION)) {
                    mp3Duration = FormatUtil.formatTime(intent.getExtras()
                            .getInt("mp3Duration"));
                }
                // 展示动画
                if (action.equals(SHOW_ANIM)) {
                    showWaitImageAnimation();
                }

                // 没有传值的就是通过播放界面标记我的最爱的，所以默认赋值上次点击播放的页面，为0则默认为全部歌曲
                slidingPage = intent.getIntExtra(BROADCAST_INTENT_PAGE,
                        playerPage == 0 ? SLIDING_MENU_ALL : playerPage);
                dialogMenuPosition = intent.getIntExtra(
                        BROADCAST_INTENT_POSITION, 0);
                MusicInfo info = null;
                switch (slidingPage) {
                    case MainActivity.SLIDING_MENU_ALL:
                        if (MusicList.list.size() >= dialogMenuPosition
                                && MusicList.list.size() != 0) {
                            info = MusicList.list.get(dialogMenuPosition);
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "小唯发现一首歌都没有~去搜索一下吧", Toast.LENGTH_LONG).show();
                        }

                        break;

                    case MainActivity.SLIDING_MENU_FAVORITE:
                        if (FavoriteList.list.size() >= dialogMenuPosition) {
                            info = FavoriteList.list.get(dialogMenuPosition);
                        }
                        break;
                    case MainActivity.SLIDING_MENU_ONLINE:
                        if (OnlineMusicList.list.size() >= dialogMenuPosition) {
                            info = OnlineMusicList.list.get(dialogMenuPosition);
                        }
                        isOnline = true;
                        break;
                    case MainActivity.SLIDING_MENU_DOWNLOAD:
                        if (DownloadList.list.size() >= dialogMenuPosition) {
                            info = DownloadList.list.get(dialogMenuPosition);
                        }
                        break;
                    case MainActivity.SLIDING_MENU_FOLDER_LIST:
                        if (FolderList.list.get(folderPosition).getMusicList()
                                .size() >= dialogMenuPosition) {
                            info = FolderList.list.get(folderPosition)
                                    .getMusicList().get(dialogMenuPosition);
                        }

                        break;
                }

                if (info != null) {
                    if (action.equals(BROADCAST_ACTION_MENU)) {
                        MenuDialog menuDialog = new MenuDialog(
                                MainActivity.this);
                        menuDialog
                                .setOnTVAnimDialogDismissListener(MainActivity.this);
                        menuDialog.show();
                        menuDialog.setDialogTitle(info.getName());// 必须在show后执行
                    } else if (action.equals(BROADCAST_ACTION_FAVORITE)) {
                        // 因为源数据是静态的，所以赋值给info也指向了静态数据的那块内存，直接改info的数据就行
                        // 不知我的理解对否。而且这算不算内存泄露？？？
                        if (info.isFavorite()) {
                            info.setFavorite(false);// 删除标记
                            FavoriteList.list.remove(info);// 移除
                        } else {
                            info.setFavorite(true);// 标记为喜爱
                            FavoriteList.list.add(info);// 新增
                            FavoriteList.sort();// 重新排序
                        }
                        DBDao db = new DBDao(getApplicationContext());
                        db.update(info.getName(), info.isFavorite());// 更新数据库
                        db.close();// 必须关闭
                        musicAdapter.update(musicAdapter.getPage());
                        mainSize.setText(musicAdapter.getCount() + "首歌曲");
                    }
                    dialogMenuPath = info.getPath();
                }
            }
        }
    }

    /**
     * 长按处理
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                   int position, long id) {
        // TODO Auto-generated method stub
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        currentItemView = view;
        setLongClickPosition(position);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Display display = this.getWindowManager().getDefaultDisplay();
        display.getMetrics(displayMetrics);
        WindowManager.LayoutParams params = floatDialog.getWindow()
                .getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.y = display.getHeight() - location[1];
        floatDialog.getWindow().setAttributes(params);
        floatDialog.setCanceledOnTouchOutside(true);
        floatDialog.show();
        return true;
    }

    public int getLongClickPosition() {
        return longClickPosition;
    }

    private void setLongClickPosition(int longClickPosition) {
        this.longClickPosition = longClickPosition;
    }

    @Override
    public void leftOnclick() {// 移除
        // TODO Auto-generated method stub
        MusicInfo info = null;
        slidingPage = musicAdapter.getPage();
        switch (slidingPage) {
            case MainActivity.SLIDING_MENU_ALL:
                info = MusicList.list.get(longClickPosition);
                removeMusicInAllList(info);
                musicAdapter.update(slidingPage);
                break;

            case MainActivity.SLIDING_MENU_FAVORITE:
                info = FavoriteList.list.get(longClickPosition);
                removeMusicInAllList(info);
                musicAdapter.update(slidingPage);
                break;
            case MainActivity.SLIDING_MENU_ONLINE:
                info = OnlineMusicList.list.get(longClickPosition);
                removeMusicInAllList(info);
                musicAdapter.update(slidingPage);
                break;
            case MainActivity.SLIDING_MENU_DOWNLOAD:
                info = DownloadList.list.get(longClickPosition);
                removeMusicInAllList(info);
                musicAdapter.update(slidingPage);
                break;
            case MainActivity.SLIDING_MENU_FOLDER_LIST:
                info = FolderList.list.get(folderPosition).getMusicList()
                        .get(longClickPosition);
                removeMusicInAllList(info);
                musicAdapter.update(slidingPage);
                break;
            case MainActivity.SLIDING_MENU_FOLDER:
                List<MusicInfo> musiclist = FolderList.list.get(longClickPosition)
                        .getMusicList();
                FolderList.list.remove(longClickPosition);
                musicAdapter.update(slidingPage);
                for (MusicInfo musicInfo : musiclist) {
                    removeMusicInAllList(musicInfo);
                }
                Toast.makeText(getApplicationContext(), "文件夹下的均已移除！",
                        Toast.LENGTH_SHORT).show();
                break;
        }
        floatDialog.dismiss();
    }

    @Override
    public void rightOnclick() {// 删除歌曲或文件夹
        // TODO Auto-generated method stub
        MusicInfo info = null;
        slidingPage = musicAdapter.getPage();
        // 显示删除对话框
        DeleteDialog deleteDialog = new DeleteDialog(this);
        deleteDialog.setOnTVAnimDialogDismissListener(this);
        deleteDialog.show();
        switch (slidingPage) {
            case MainActivity.SLIDING_MENU_ALL:
                info = MusicList.list.get(longClickPosition);
                dialogMenuPath = info.getPath();
                break;

            case MainActivity.SLIDING_MENU_FAVORITE:
                info = FavoriteList.list.get(longClickPosition);
                dialogMenuPath = info.getPath();
                break;
            case MainActivity.SLIDING_MENU_ONLINE:
                info = OnlineMusicList.list.get(longClickPosition);
                dialogMenuPath = info.getPath();
                break;
            case MainActivity.SLIDING_MENU_DOWNLOAD:
                info = DownloadList.list.get(longClickPosition);
                dialogMenuPath = info.getPath();
                break;
            case MainActivity.SLIDING_MENU_FOLDER_LIST:
                info = FolderList.list.get(folderPosition).getMusicList()
                        .get(longClickPosition);
                dialogMenuPath = info.getPath();
                break;
            case MainActivity.SLIDING_MENU_FOLDER:
                dialogMenuPath = "";
                break;
        }
        floatDialog.dismiss();
    }

    // 长按处理结束

    public void deleteMusic(MusicInfo info) {
        removeMusicInAllList(info);
        File file = new File(info.getPath());
        if (file.delete()) {
            Toast.makeText(getApplicationContext(), "文件已被删除", Toast.LENGTH_LONG)
                    .show();
        }
        musicAdapter.update(slidingPage);
    }

}
