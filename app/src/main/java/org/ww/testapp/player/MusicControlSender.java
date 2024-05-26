package org.ww.testapp.player;

import android.content.Context;
import android.content.Intent;

public class MusicControlSender {

    public static void sendPlayBroadcast(Context context) {Intent intent = new Intent(MusicService.MusicControlActions.ACTION_PLAY);
        context.sendBroadcast(intent);
    }

    public static void sendPauseBroadcast(Context context) {
        Intent intent = new Intent(MusicService.MusicControlActions.ACTION_PAUSE);
        context.sendBroadcast(intent);
    }

    public static void sendNextBroadcast(Context context) {
        Intent intent = new Intent(MusicService.MusicControlActions.ACTION_NEXT);
        context.sendBroadcast(intent);
    }

    public static void sendPreviousBroadcast(Context context) {
        Intent intent = new Intent(MusicService.MusicControlActions.ACTION_PREVIOUS);
        context.sendBroadcast(intent);
    }
}

