package lol.primitive.primitivemobile;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileObserver;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Button;

import com.pixplicity.sharp.Sharp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import primitivemobile.Primitivemobile;

public class PrimitiveService extends Service {
    private static final int NOTIFICATION_ID = 1;
    public PrimitiveService() {
    }

    private String readString(File file){
        int length = (int) file.length();

        byte[] bytes = new byte[length];

        try {
            FileInputStream in = new FileInputStream(file);
            in.read(bytes);
            in.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("Error!!");
        }

        String contents = new String(bytes);
        return contents;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy(){
        System.out.println("***********");
    }


    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        String CHANNEL_ID = "my_channel_01";
        Intent notificationIntent = new Intent(this, FinishedPreviewActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// The id of the channel.
            String id = "my_channel_01";
// The user-visible name of the channel.
            CharSequence name = "Channel";
// The user-visible description of the channel.
            String description = "Channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
// Configure the notification channel.
            mChannel.setDescription(description);
            mChannel.enableLights(true);
// Sets the notification light color for notifications posted to this
// channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mNotificationManager.createNotificationChannel(mChannel);
            Notification notification =
                    new Notification.Builder(this, CHANNEL_ID)
                            .setContentTitle("Test")
                            .setContentText("Test")
                            .setSmallIcon(R.drawable.alert)
                            .setContentIntent(pendingIntent)
                            .setTicker("Test")
                            .build();
            startForeground(NOTIFICATION_ID, notification);
        } else {
            Notification notification =
            new Notification.Builder(this)
                    .setContentTitle("Test")
                    .setContentText("Test")
                    .setSmallIcon(R.drawable.alert)
                    .setContentIntent(pendingIntent)
                    .setTicker("Test")
                    .build();
            startForeground(NOTIFICATION_ID, notification);
        }



        final File file;
        try {
            file = File.createTempFile("primitive-output", null, this.getCacheDir());

            FileObserver observer = new FileObserver(file.getAbsolutePath(),FileObserver.MODIFY) { // set up a file observer to watch this directory on sd card
                int count = 0;
                @Override
                public void onEvent(int event, String nullFile) {
                    sendMessage(readString(file));
                }
            };
            observer.startWatching();

            new Thread(new Runnable() {
                public void run() {
                    String path = intent.getExtras().getString("path");
                    if(path == null) {
                        Uri temp = (Uri) intent.getExtras().get("uri");
                        String uriPath = temp.getPath();
                        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                                + uriPath.substring(uriPath.lastIndexOf("/"));
                    }

                    System.out.println(path);

                    int inputSize = intent.getExtras().getInt("inputSize");
                    int outputSize = intent.getExtras().getInt("outputSize");
                    int count = intent.getExtras().getInt("count");
                    int mode = intent.getExtras().getInt("mode");
                    String background = intent.getExtras().getString("background");
                    int alpha = intent.getExtras().getInt("alpha");
                    int repeat = intent.getExtras().getInt("repeat");
                    System.out.println(count);
                    Primitivemobile.processImage(path,inputSize,outputSize,count,mode,background,alpha,repeat,file.getAbsolutePath());
                }
            }).start();
        } catch (IOException e) {
            System.out.println("Error!");
            e.printStackTrace();
        }
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    private void sendMessage(String data) {
        System.out.println(data);
        Intent intent = new Intent("updateImage");
        // add data
        intent.putExtra("data", data);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
