package lol.primitive.primitivemobile;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ProgressBar;

import com.pixplicity.sharp.Sharp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import primitivemobile.Primitivemobile;

public class FinishedPreviewActivity extends AppCompatActivity {

    private static final String DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/primitive";

    private int count = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {


        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("updateImage"));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finished_preview);
        final Intent intent = getIntent();

        final ProgressBar imageProgress = (ProgressBar) findViewById(R.id.progressBar);
        imageProgress.setMax(100);
        imageProgress.setProgress(0);

        int color = 0xFF00FFFF;
        imageProgress.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        imageProgress.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        imageProgress.bringToFront();
        final int totalNumShapes = intent.getExtras().getInt("count");

        final ImageView imageView = findViewById(R.id.finished_image_preview);


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

        Button saveBtn = (Button) findViewById(R.id.saveFinishedBtn);
        saveBtn.setClickable(false);
        saveBtn.setEnabled(false);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Saves Image as New Image with New Number (similar to hashing)
                try {
                    File file = File.createTempFile("Primitive-",".jpg",new File(DIR));
                    OutputStream stream = new FileOutputStream(file);

                    PictureDrawable pictureDrawable = (PictureDrawable)imageView.getDrawable();
                    Bitmap bitmap = Bitmap.createBitmap(pictureDrawable.getIntrinsicWidth(), pictureDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawPicture(pictureDrawable.getPicture());
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);

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
        Intent intent2 = new Intent(this, PrimitiveService.class);
        intent2.putExtra("inputSize",intent.getExtras().getInt("inputSize"));
        intent2.putExtra("outputSize",intent.getExtras().getInt("outputSize"));
        intent2.putExtra("count",intent.getExtras().getInt("count"));
        intent2.putExtra("mode",intent.getExtras().getInt("mode"));
        intent2.putExtra("background", intent.getExtras().getString("background"));
        intent2.putExtra("alpha", intent.getExtras().getInt("alpha"));
        intent2.putExtra("repeat", intent.getExtras().getInt("repeat"));
        intent2.putExtra("uri",(Uri) intent.getExtras().get("uri"));
        intent2.putExtra("path",intent.getExtras().getString("path"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent2);
        }
        else{
            startService(intent2);
        }




    }

    @Override
    public void onResume() {
        super.onResume();

        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("updateImage"));
    }

    // handler for received Intents for the "my-event" event
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent intent2 = getIntent();
            // Extract data included in the Intent
            String data = intent.getStringExtra("data");
            Log.d("receiver", "Got data: " + data);
            final ImageView imageView = findViewById(R.id.finished_image_preview);
            final ProgressBar imageProgress = (ProgressBar) findViewById(R.id.progressBar);
            final int totalNumShapes = intent2.getExtras().getInt("count");
                Sharp.loadString(data).into(imageView);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Button saveBtn = (Button) findViewById(R.id.saveFinishedBtn);
                        if(!saveBtn.isEnabled()) {
                            saveBtn.setClickable(true);
                            saveBtn.setEnabled(true);
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            imageProgress.setProgress((int) ((++count / (double) totalNumShapes) * 100), true);
                        } else {
                            imageProgress.setProgress((int) ((++count / (double) totalNumShapes) * 100));
                        }
                    }
                });
            }
    };

    /*
    @Override
    protected void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }
    */
}
