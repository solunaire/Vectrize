package lol.primitive.primitivemobile;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
                }
            };
            observer.startWatching();

            new Thread(new Runnable() {
                public void run() {
                    String path = intent.getExtras().getString("path");
                    if(path == null) {
                        Uri temp = (Uri) intent.getExtras().get("uri");
                        System.out.println(temp.toString());
//                        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + temp.getPath();
                    }

                    if(path == null) {
                        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Photo.png";
                    }

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
}
