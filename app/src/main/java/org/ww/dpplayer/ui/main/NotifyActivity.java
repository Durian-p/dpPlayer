//package org.ww.dpplayer.ui.main;
//
//import android.app.Notification;
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.ServiceConnection;
//import android.content.pm.PackageInfo;
//import android.content.pm.PackageManager;
//import android.media.AudioManager;
//import android.media.MediaPlayer;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.IBinder;
//import android.support.v4.app.NotificationCompat;
//import android.view.View;
//import android.widget.Button;
//import android.widget.RemoteViews;
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.request.target.NotificationTarget;
//import com.xx.R;
//import xx.activity.BusinessActivity;
//import com.xx.media.MediaPlayerUtil;
//import com.xx.model.bo.AudioBo;
//
//import java.util.ArrayList;
//import java.util.List;
//
///*
// * 音频列表activity
// *  */
//public class NotifityActivity extends BusinessActivity implements ServiceConnection {
//    private static final int SDK_PAY_FLAG = 1;
//    private final static String TAG = "加载SoundPlayerActivity类";
//
//    private Button mbutton;
//    private MediaPlayer mPlayer;
//    // 这里如果不设置为-1 的话，默认是0，会造成进入列表第一条数据就显示为播放状态
//    private int thisPosition;
//    String channelId = "音频会话";//渠道id
//    private MediaPlayerUtil mediaPlayerUtil;
//    private Notification notification;
//    private RemoteViews contentView;
//    private NotificationTarget   notificationTarget;
//    private AudioManager mAudioManager;
//    //  检索了NotifityActivity的运行实例，以方便广播接收成功时处理activity相对应的逻辑处理(如：更新UI)
//    private static NotifityActivity notifityActivity;
//
//    public static NotifityActivity getInstace() {
//        return notifityActivity;
//    }
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        notifityActivity = this;
//        setContentView(R.layout.acs);
//        Intent intent = new Intent(this, MediaService.class);
//
//        //增加StartService，来增加后台播放功能
//        startService(intent);
//        bindService(intent, this, BIND_AUTO_CREATE);
//
//        // 这里只是用来做测试数据
//        List<AudioBo> audioBoList=new ArrayList<AudioBo>();
//        AudioBo audioBo=new AudioBo();
//        int a1 = 0;long b1 = (int)a1;
//        audioBo.setId(b1);
//        audioBo.setTitle("音频1");
//        audioBo.setUrl("音频路径地址");
//        audioBo.setImgUrl("网络图片地址");
//        audioBoList.add(audioBo);
//
//        AudioBo audioBo2=new AudioBo();
//        int a = 1;long b = (int)a;
//        audioBo2.setId(b);
//        audioBo2.setTitle("音频2");
//        audioBo2.setUrl("音频路径地址");
//        audioBo2.setImgUrl("网络图片地址");
//        audioBoList.add(audioBo2);
//        mediaPlayerUtil=MediaPlayerUtil.getInstance();
//        mediaPlayerUtil.setAudioBoList(audioBoList);
//
//        initView();
//
//    }
//
//    private void initView() {
//        // 我这里是在一个页面中的button事件唤起通知栏
//        mbutton=findViewById(R.id.notifyButton);
//        mbutton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mediaPlayerUtil=MediaPlayerUtil.getInstance();
//
//                // 调用通知栏加载方法
//                initNotificationBar();
//                // 1.上下文 2.图标控件的ResID 2.RemoteView  ,4 Notification 5 Notification_ID
//                notificationTarget = new NotificationTarget(mContext, R.id.bgmmMusicImageView, contentView, notification, R.string.app_name);
//                Glide.with(mContext).asBitmap().load(mediaPlayerUtil.getAudioBoList().get(0).getImgUrl())
//                        .placeholder(R.drawable.test).into(notificationTarget);
//            }
//        });
//
//        //1 初始化AudioManager对象
//        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        //2 申请焦点(关闭其它音频通道)
//
//        mAudioManager.requestAudioFocus(mAudioFocusChange, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
//
//        mediaPlayerUtil = MediaPlayerUtil.getInstance();
//        if (mediaPlayerUtil != null) {
//            thisPosition = mediaPlayerUtil.getIndex();
//        }
//    }
//
//
//
//
//
//    @Override
//    public void onServiceConnected(ComponentName name, IBinder service) {
//    }
//
//    @Override
//    public void onServiceDisconnected(ComponentName name) {
//    }
//
//    @Override
//    public void onPointerCaptureChanged(boolean hasCapture) {
//
//    }
//
//    /*
//     * 创建自定义远程视图RemoteView
//     * */
//    private void initRemoteView() {
//        contentView = new RemoteViews(getPackageName(), R.layout.audio_music_notifi);// 远程视图
//        mediaPlayerUtil = MediaPlayerUtil.getInstance();
//        contentView.setTextViewText(R.id.musicTitleTextView, mediaPlayerUtil.getAudioBoList().get(mediaPlayerUtil.getIndex()).getTitle());
//        contentView.setImageViewResource(R.id.audio_close_btn, R.drawable.player_icon_close);
//
//        contentView.setImageViewResource(R.id.lastImageView, R.drawable.player_icon_last);
//        contentView.setImageViewResource(R.id.nextImageView, R.drawable.player_icon_next);
//        contentView.setImageViewResource(R.id.stopImageView, R.drawable.player_icon_stop);
//
//        if (mediaPlayerUtil.getmPlayer()!=null && mediaPlayerUtil.isplay()) {
//            contentView.setImageViewResource(R.id.stopImageView, R.drawable.player_icon_plays);
//        } else {
//            contentView.setImageViewResource(R.id.stopImageView, R.drawable.player_icon_stop);
//        }
//        // 实现停止/播放
//        Intent intentStop = new Intent("stop");
//        PendingIntent pIntentStop = PendingIntent.getBroadcast(this, 0, intentStop, 0);
//        contentView.setOnClickPendingIntent(R.id.stopImageView, pIntentStop);
//        //下一首事件
//        Intent intentNext = new Intent("next");//发送播放下一曲的通知
//        PendingIntent pIntentNext = PendingIntent.getBroadcast(this, 0, intentNext, 0);
//        contentView.setOnClickPendingIntent(R.id.nextImageView, pIntentNext);
//        //上一首事件
//        Intent intentLast = new Intent("last");//发送播放上一曲的通知
//        PendingIntent pIntentLast = PendingIntent.getBroadcast(this, 0, intentLast, 0);
//        contentView.setOnClickPendingIntent(R.id.lastImageView, pIntentLast);
//        // 关闭通知栏
//        Intent intentCancelled = new Intent("notification_cancelled");
//        PendingIntent pIntentCancelled = PendingIntent.getBroadcast(this, 0, intentCancelled, 0);
//        contentView.setOnClickPendingIntent(R.id.audio_close_btn, pIntentCancelled);
//    }
//
//
//    /*
//     * 创建通知栏
//     * */
//    private void initNotificationBar() {
//
//        initRemoteView();
//        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        notification=new Notification();
//        Intent intent = new Intent(mContext, NotificationClickReceiver.class); // 创建一个跳转到活动页面的意图
//        PendingIntent pendingIntent = PendingIntent.getActivity(NotifityActivity.this, R.string.app_name, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//        Notification.Builder builder = null; // 创建一个通知消息的构造器
//
//        //  判断是否是android 8以上的版本，因为从8.0版本后唤起通知栏必须要填写渠道id和渠道
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            String description = "有趣,有用,有未来";
//            int importance = NotificationManager.IMPORTANCE_LOW;//重要性级别
//            // getPackageName() 获取APP渠道名称
//            NotificationChannel mChannel = new NotificationChannel(channelId, "音频", importance);
//            mChannel.setDescription(description);//渠道描述
//            manager.createNotificationChannel(mChannel);//创建通知渠道
//            mChannel.setSound(null,null);
//            builder = new Notification.Builder(this, channelId);
//            builder.setContentIntent(pendingIntent)
//                    .setCustomContentView(contentView)
//                    .setCustomBigContentView(contentView)
//                    .setShowWhen(false)
//                    .setSmallIcon(R.drawable.notification_icon) // 必填
//                    .build();
//            notification = builder.build();
//        }else{ // 这里要兼顾android 8以下的版本
//            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext,channelId);
//            notificationBuilder.setCustomBigContentView(contentView)
//                    .setCustomContentView(contentView)
//                    .setContentIntent(pendingIntent)
//                    .setSmallIcon(R.drawable.notification_icon)
//                    .build();
//            notification = notificationBuilder.build();
//        }
//        notification.flags = notification.FLAG_NO_CLEAR;//设置通知点击或滑动时不被清除*/
//        manager.notify(R.string.app_name, notification);
//
//    }
//
//    /**
//     * [获取应用程序版本名称信息]
//     *
//     * @param context
//     * @return 当前应用的版本名称
//     */
//    public static synchronized String getPackageName(Context context) {
//        try {
//            PackageManager packageManager = context.getPackageManager();
//            PackageInfo packageInfo = packageManager.getPackageInfo(
//                    context.getPackageName(), 0);
//            return packageInfo.packageName;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//
//    }
//
//    @Override
//    protected void onDestroy() {
//        unbindService(this);
//        super.onDestroy();
//    }
//
//    // 调用广播回调来更新activity中相关事件
//    public void updateUI() {
//        NotifityActivity.this.runOnUiThread(new Runnable() {
//            public void run() {
//                initView();
//                initNotificationBar();
//                mediaPlayerUtil = MediaPlayerUtil.getInstance();
//                boolean isdestory;
//                if (mActivity== null || mActivity.isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && mActivity.isDestroyed())) {
//                    isdestory=true;
//                } else {
//                    isdestory=false;
//                }
//                // 由于图片使用Glide异步加载图片activity已销毁时时发生的崩溃
//                if(isdestory==false){
//                    // 此处为Glide的notificationTarget回调,当网络图片加载成功更新通知栏相对应控件的图像
//                    notificationTarget = new NotificationTarget(mContext, R.id.bgmmMusicImageView, contentView, notification, R.string.app_name);
//                    // 加载网络图片
//                    Glide.with(mContext).asBitmap().load(mediaPlayerUtil.getAudioBoList().get(mediaPlayerUtil.getIndex()).getImgUrl()).placeholder(R.drawable.test).into(notificationTarget);
//
//                }
//                mAudioManager.abandonAudioFocus(mAudioFocusChange);
//            }
//        });
//    }
//
//    /*
//     * 关闭、清楚通知栏
//     * */
//    public void closeNotifi() {
//        NotifityActivity.this.runOnUiThread(new Runnable() {
//            public void run() {
//                mediaPlayerUtil = MediaPlayerUtil.getInstance();
//                mediaPlayerUtil.closeMedia();
//                mAudioManager.abandonAudioFocus(mAudioFocusChange);
//                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//                manager.cancel(R.string.app_name);
//            }
//        });
//    }
//
//
//    // 焦点监听
//    AudioManager.OnAudioFocusChangeListener mAudioFocusChange = new AudioManager.OnAudioFocusChangeListener() {
//        @Override
//        public void onAudioFocusChange(int focusChange) {
//            mediaPlayerUtil=MediaPlayerUtil.getInstance();
//
//            if(focusChange==AudioManager.AUDIOFOCUS_LOSS) {
//
//                //长时间丢失焦点,当其他应用如播放QQ音乐，网易云音乐播放时
//                if (mediaPlayerUtil.isplay()) {
//                    closeNotifi();
//                }
//                mAudioManager.abandonAudioFocus(mAudioFocusChange);
//            }else if(focusChange== AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
//                if (mediaPlayerUtil.isplay()) {
//                    mediaPlayerUtil.stop();
//                    updateUI();
//                }
//
//            }
//        }
//    };
//
//}
//
//
