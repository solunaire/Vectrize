package lol.primitive.primitivemobile;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ProgressBar;

import com.pixplicity.sharp.Sharp;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import primitivemobile.Primitivemobile;

import static android.R.attr.id;

public class FinishedPreviewActivity extends AppCompatActivity {

    private static final String DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/primitive";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finished_preview);
        final Intent intent = getIntent();

        final ProgressBar imageProgress = (ProgressBar) findViewById(R.id.progressBar);
        imageProgress.setMax(100);
        imageProgress.setProgress(0);
        final int totalNumShapes = intent.getExtras().getInt("count");

        final ImageView imageView = findViewById(R.id.finished_image_preview);

        final File file;
        try {
            file = File.createTempFile("primitive-output", null, this.getCacheDir());
            FileObserver observer = new FileObserver(file.getAbsolutePath(),FileObserver.MODIFY) { // set up a file observer to watch this directory on sd card
                int count = 0;
                @Override
                public void onEvent(int event, String nullFile) {
                    if(isAppOnForeground(FinishedPreviewActivity.this, FinishedPreviewActivity.this.getPackageName())) {
                        //App in Foreground: Load Shapes into ImageView
                        Sharp.loadFile(file).into(imageView);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    imageProgress.setProgress((int) ((++count / (double) totalNumShapes) * 100), true);
                                } else {
                                    imageProgress.setProgress((int) ((++count / (double) totalNumShapes) * 100));
                                }
                            }
                        });
                    } else {
                        //App in Background: Load Shapes in FileSVG
                        System.out.println("hi");
                        count++;
                    }

                    final NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(FinishedPreviewActivity.this);
                    mBuilder.setContentTitle("Primitive Generation")
                            .setContentText("Primitive In Progress")
                            .setSmallIcon(R.drawable.sync);

                    mBuilder.setProgress(100, (int) ((count / (double) totalNumShapes) * 100), false);
                    mNotifyManager.notify(id, mBuilder.build());

                    if (count / totalNumShapes == 1) {
                        mBuilder.setContentText("Primitive Operation Complete").setProgress(0, 0, false);
                        mNotifyManager.notify(id, mBuilder.build());
                    }
                }
            };
            observer.startWatching();

            new Thread(new Runnable() {
                public void run() {
                    String path = intent.getExtras().getString("path");
                    int inputSize = intent.getExtras().getInt("inputSize");
                    int outputSize = intent.getExtras().getInt("outputSize");
                    int count = intent.getExtras().getInt("count");
                    int mode = intent.getExtras().getInt("mode");
                    String background = intent.getExtras().getString("background");
                    int alpha = intent.getExtras().getInt("alpha");
                    int repeat = intent.getExtras().getInt("repeat");
                    Primitivemobile.processImage(path,inputSize,outputSize,count,mode,background,alpha,repeat,file.getAbsolutePath());
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }


        Button cancelBtn = (Button) findViewById(R.id.cancelFinishedBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(FinishedPreviewActivity.this);
                builder.setMessage("You haven't saved your image, do you want to discard changes?")
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //Do Nothing
                            }
                        })
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

//        Button temp = (Button) findViewById(R.id.tempBtn);
//        temp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//                final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(FinishedPreviewActivity.this);
//                mBuilder.setContentTitle("Primitive Running")
//                        .setContentText("Download in progress")
//                        .setSmallIcon(R.drawable.sync);
//                // Start a lengthy operation in a background thread
//                new Thread(
//                        new Runnable() {
//                            @Override
//                            public void run() {
//                                int incr;
//                                // Do the "lengthy" operation 20 times
//                                for (incr = 0; incr <= 100; incr+=5) {
//                                    // Sets the progress indicator to a max value, the
//                                    // current completion percentage, and "determinate"
//                                    // state
//                                    mBuilder.setProgress(100, incr, false);
//                                    // Displays the progress bar for the first time.
//                                    mNotifyManager.notify(id, mBuilder.build());
//                                    // Sleeps the thread, simulating an operation
//                                    // that takes time
//                                    try {
//                                        // Sleep for 5 seconds
//                                        Thread.sleep(5*1000);
//                                    } catch (InterruptedException e) {
//                                        Log.d("Notification Progress", "sleep failure");
//                                    }
//                                }
//                                // When the loop is finished, updates the notification
//                                mBuilder.setContentText("Download complete")
//                                        // Removes the progress bar
//                                        .setProgress(0,0,false);
//                                mNotifyManager.notify(id, mBuilder.build());
//                            }
//                        }
//                // Starts the thread by calling the run() method in its Runnable
//                ).start();
//            }
//        });


        Button saveBtn = (Button) findViewById(R.id.saveFinishedBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Use Below Code to Add Saving File Choice Name Option
//                final Dialog commentDialog = new Dialog(FinishedPreviewActivity.this);
//                commentDialog.setContentView(R.layout.modal_save);
//                Button okBtn = (Button) commentDialog.findViewById(R.id.ok);
//                boolean isFile = false;
//
//                okBtn.setOnClickListener(new View.OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//                        EditText nameText = (EditText) findViewById(R.id.save_body);
//                        String fileName = nameText.getText().toString();
//                        File tempFile = new File(DIR, fileName);
//
//                        if(tempFile.exists()) {
//                            Toast.makeText(FinishedPreviewActivity.this, "File Name Already Exists", Toast.LENGTH_SHORT).show();
//                        } else {
//
//                        }
//                        commentDialog.dismiss();
//                    }
//                });
//                Button defaultBtn = (Button) commentDialog.findViewById(R.id.use_default);
//                defaultBtn.setOnClickListener(new View.OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {

//                        }
//
//                        isFile = true;
//                        commentDialog.dismiss();
//                    }
//                });

                //Saves Image as New Image with New Number (similar to hashing)
                try {
                    File file = File.createTempFile("Primitive-",".jpg",new File(DIR));
                    OutputStream stream = new FileOutputStream(file);

                    PictureDrawable pictureDrawable = (PictureDrawable)imageView.getDrawable();
                    Bitmap bitmap = Bitmap.createBitmap(pictureDrawable.getIntrinsicWidth(), pictureDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawPicture(pictureDrawable.getPicture());
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream); //TODO: change to SVG saving

                    stream.flush();
                    stream.close();

                    Uri savedImageURI = Uri.parse(file.getAbsolutePath());
                    Toast.makeText(FinishedPreviewActivity.this, "Image saved in internal storage.\n" + savedImageURI, Toast.LENGTH_SHORT).show();
                    finish(); //Return to MainActivity

                }
                catch (IOException e){
                    e.printStackTrace();
                    Toast.makeText(FinishedPreviewActivity.this, "Unable to save image", Toast.LENGTH_SHORT).show();
                    finish(); //Return to MainActivity
                }


            }
        });

    }

    private boolean isAppOnForeground(Context context, String appPackageName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = appPackageName;
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }
}
