package cn.wydewy.wyplayer;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

import cn.wydewy.wyplayer.util.Constant;

public class Mp3Application extends Application {
    public NotificationManager notManager;
    public Notification notification;
    public Notification downloadNotification;
    public IMusic music;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        notManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public RemoteViews updateNotification(boolean isPlay, String name,
                                          String singer, Bitmap icon) {
        RemoteViews contentView;
        contentView = new RemoteViews(getPackageName(),
                R.layout.notification_item);

        if (isPlay) {
            contentView.setImageViewResource(R.id.btn_custom_play,
                    R.drawable.btn_pause);
        } else {
            contentView.setImageViewResource(R.id.btn_custom_play,
                    R.drawable.btn_play);
        }

        if (icon == null) {
            contentView.setImageViewResource(R.id.custom_song_icon,
                    R.drawable.ic_launcher);
        } else {
            contentView.setImageViewBitmap(R.id.custom_song_icon, icon);
        }

        contentView.setTextViewText(R.id.tv_custom_song_name, name);
        contentView.setTextViewText(R.id.tv_custom_song_singer, singer);

        Intent intentPrevious = new Intent(Constant.ACTION_PREV);
        PendingIntent pIntentPrevious = PendingIntent.getBroadcast(
                getApplicationContext(), 0, intentPrevious,
                PendingIntent.FLAG_UPDATE_CURRENT);
        contentView.setOnClickPendingIntent(R.id.btn_custom_prev,
                pIntentPrevious);// 上一曲

        Intent intentPlay = new Intent(Constant.ACTION_PLAY);// 新建意图，并设置action标记为"play"，用于接收广播时过滤意图信息
        PendingIntent pIntentPlay = PendingIntent.getBroadcast(
                getApplicationContext(), 0, intentPlay,
                PendingIntent.FLAG_UPDATE_CURRENT);
        contentView.setOnClickPendingIntent(R.id.btn_custom_play, pIntentPlay);// 为play控件注册事件

        Intent intentNext = new Intent(Constant.ACTION_NEXT);
        PendingIntent pIntentNext = PendingIntent.getBroadcast(
                getApplicationContext(), 0, intentNext,
                PendingIntent.FLAG_UPDATE_CURRENT);
        contentView.setOnClickPendingIntent(R.id.btn_custom_next, pIntentNext);// 下一曲

        Intent intentCancel = new Intent(Constant.ACTION_CANCEL);
        PendingIntent pIntentCancel = PendingIntent.getBroadcast(
                getApplicationContext(), 0, intentCancel,
                PendingIntent.FLAG_UPDATE_CURRENT);
        contentView.setOnClickPendingIntent(R.id.btn_custom_cancel,
                pIntentCancel);

        if (notification != null) {
            notification.contentView = contentView;
        }

        return contentView;

    }

    public void showNotification() {
        if (this.notManager != null) {
            this.notManager.notify(Constant.NOTI_CTRL_ID, notification);
        }

    }

    public RemoteViews updateDowmloadNotification(float currentPro,
                                                  float totalPro, String name) {
        RemoteViews contentView;
        contentView = new RemoteViews(getPackageName(),
                R.layout.download_notification_item);

        contentView.setTextViewText(R.id.notificationTitle, name + "下载...");
        int pro = (int) (100 * (currentPro / totalPro));
        contentView.setTextViewText(R.id.notificationPercent, pro + "%");
        contentView.setProgressBar(R.id.progressBar, 100, pro, false);

        if (downloadNotification != null) {
            downloadNotification.contentView = contentView;
        }

        return contentView;

    }

    public void showDowmloadNotification() {
        this.notManager.notify(2, downloadNotification);
    }
}