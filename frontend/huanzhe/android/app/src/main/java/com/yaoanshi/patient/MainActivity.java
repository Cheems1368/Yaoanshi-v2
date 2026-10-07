package com.yaoanshi.patient;

import com.getcapacitor.BridgeActivity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(SecureMedicalCachePlugin.class);
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager manager = getSystemService(NotificationManager.class);
        for (int sound = 0; sound <= 1; sound++) {
            for (int vibration = 0; vibration <= 1; vibration++) {
                NotificationChannel channel = new NotificationChannel("medication-" + sound + "-" + vibration,
                        "服药提醒（" + (sound == 1 ? "有声" : "静音") + "、" + (vibration == 1 ? "震动" : "不震动") + "）",
                        NotificationManager.IMPORTANCE_HIGH);
                channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
                channel.setSound(sound == 1 ? Uri.parse("android.resource://" + getPackageName() + "/raw/medication_reminder") : null,
                        new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build());
                channel.enableVibration(vibration == 1);
                if (vibration == 1) channel.setVibrationPattern(new long[]{0, 300, 150, 300});
                manager.createNotificationChannel(channel);
            }
        }
    }
}
