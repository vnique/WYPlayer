package cn.wydewy.wyplayer;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import cn.wydewy.wyplayer.util.ScanUtil;

/**
 * <br>
 * <b>程序启动首页面</b></br>
 * <p/>
 * <br>
 * 执行动画、扫描数据库、第一次进入创建桌面图标， </br>
 *
 * @version 2014.05.06 v1.0 完成动画及定时跳转 <br>
 *          2014.07.30 v1.1 新增对服务是否已运行的判断，已经运行无需执行扫描任务，直接进入主界面</br>
 */
public class LogoActivity extends Activity {

    private Handler mHandler;
    private ScanUtil manager;

    private ImageView gifView;// GIF动画控件
    private ImageView logoView;// LOGO动画控件

    private Builder mBuilder;
    private Mp3Application application;

    private Notification notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(),
                    MainActivity.class);
            startActivity(intent);
            LogoActivity.this.finish();
        } else {
            initActivity();
        }
        application = (Mp3Application) getApplication();
        initNotify();
    }

    /**
     * 初始化通知栏
     */
    @SuppressLint("InlinedApi")
    private void initNotify() {
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("唯唯动听")
                .setContentText("爱唯一，爱音乐")
                .setContentIntent(
                        getDefalutIntent(Notification.FLAG_AUTO_CANCEL))
                // .setNumber(number)//显示数量
                .setTicker("音乐来啦")// 通知首次出现在通知栏，带上升动画效果的
                .setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
                .setPriority(Notification.PRIORITY_DEFAULT)// 设置该通知优先级
                // .setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消
                // ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                // .setDefaults(Notification.DEFAULT_VIBRATE)//
                // 向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合：
                // Notification.DEFAULT_ALL Notification.DEFAULT_SOUND 添加声音 //
                // requires VIBRATE permission
                .setSmallIcon(R.drawable.ic_launcher);

        // 点击的意图ACTION是跳转到Intent
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setClass(this, LogoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContent(application.updateNotification(false, "", "", null));

        mBuilder.setContentIntent(pendingIntent);
        notification = mBuilder.build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        application.notification = notification;
    }

    private PendingIntent getDefalutIntent(int flags) {
        // TODO Auto-generated method stub
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1,
                new Intent(), flags);
        return pendingIntent;
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacks(scan);
            mHandler.removeCallbacks(runnable);
        }
    }

    private void initActivity() {
        setContentView(R.layout.activity_logo);

        gifView = (ImageView) findViewById(R.id.activity_logo_gif);
        logoView = (ImageView) findViewById(R.id.activity_logo_name);

        manager = new ScanUtil(getApplicationContext());
        final Animation logoAnim = AnimationUtils.loadAnimation(
                getApplicationContext(), R.anim.activity_logo);
        logoView.startAnimation(logoAnim);

        // 动画监听，结束时播放GIF动画
        logoAnim.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @SuppressWarnings("deprecation")
            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub

				/*
                 * 貌似AnimationDrawable有bug，某些帧会挤压变形，没办法删减了；
				 * 想用自定义View用Movie直接播放GIF图片，但是在模拟器里会花屏，解决办法就是
				 * PS中导入的帧“作为背景”合成，但是图片失真厉害，没有采用；
				 * 上面所说自定义View参考我上一版本的C_Me音乐RunGif。
				 */
                final AnimationDrawable anim = (AnimationDrawable) getResources()
                        .getDrawable(R.drawable.activity_musicman);
                gifView.setBackgroundDrawable(anim);
                gifView.getViewTreeObserver().addOnPreDrawListener(
                        new OnPreDrawListener() {

                            @Override
                            public boolean onPreDraw() {
                                // TODO Auto-generated method stub
                                anim.start();
                                return true;// 必须true才能正常显示动画效果
                            }
                        });
            }
        });

        mHandler = new Handler();
        mHandler.post(scan);// 执行扫描
        mHandler.postDelayed(runnable, 3000);// 延迟执行跳转
    }

    private Runnable runnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            Intent intent = new Intent(LogoActivity.this, MainActivity.class);
            startActivity(intent);
            LogoActivity.this.finish();
        }
    };

    private Runnable scan = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            manager.scanMusicFromDB();
        }
    };

    /**
     * 检查服务是否正在运行
     *
     * @return true/false
     */
    private boolean isServiceRunning() {

        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                .getRunningServices(Integer.MAX_VALUE);

        if (!(serviceList.size() > 0)) {
            return false;
        }

        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(
                    "cn.wydewy.wyplayer.service.MediaService")) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

}
