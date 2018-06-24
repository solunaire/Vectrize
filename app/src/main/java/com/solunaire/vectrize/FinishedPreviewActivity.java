package com.solunaire.vectrize;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import primitivemobile.Primitivemobile;

public class FinishedPreviewActivity extends AppCompatActivity {

    private static final String DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/primitive";
    private static final String fileName = "details.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finished_preview);
        final Intent intent = getIntent();

        final int totalNumShapes = intent.getExtras().getInt("count");

        final ProgressBar imageProgress = (ProgressBar) findViewById(R.id.progressBar);
        imageProgress.setMax(totalNumShapes);
        imageProgress.setProgress(0);

        int color = 0xFF00FFFF;
        imageProgress.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        imageProgress.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        imageProgress.bringToFront();

        final ImageView imageView = findViewById(R.id.finished_image_preview);

        final File file;
        try {
            file = File.createTempFile("primitive-output", null, this.getCacheDir());

            FileObserver observer = new FileObserver(file.getAbsolutePath(),FileObserver.MODIFY) { // set up a file observer to watch this directory on sd card
                int count = 0;
                @Override
                public void onEvent(int event, String nullFile) {
                    ++count;
                    Sharp.loadFile(file).into(imageView);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Button saveBtn = (Button) findViewById(R.id.saveFinishedBtn);
                            if(!saveBtn.isEnabled()) {
                                saveBtn.setClickable(true);
                                saveBtn.setEnabled(true);
                            }

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                imageProgress.setProgress(count, true);
                            } else {
                                imageProgress.setProgress(count);
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
                        String uri = temp.toString();
                        if (uri.startsWith("file://")) {
                            File selectedImage = new File(temp.getPath());
                            path = selectedImage.getPath();
                        } else {
                            String uriPath = temp.getPath();
                            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                                    + uriPath.substring(uriPath.lastIndexOf("/"));
                        }
                    }

                    System.out.println(path);

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
        saveBtn.setClickable(false);
        saveBtn.setEnabled(false);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //Saves Image as New Image with New Number (similar to hashing)
                    File file = File.createTempFile("Primitive-",".jpg",new File(DIR));
                    OutputStream stream = new FileOutputStream(file);

                    PictureDrawable pictureDrawable = (PictureDrawable)imageView.getDrawable();
                    Bitmap bitmap = Bitmap.createBitmap(pictureDrawable.getIntrinsicWidth(), pictureDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawPicture(pictureDrawable.getPicture());
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);

                    stream.flush();
                    stream.close();

                    int cutDash = file.getPath().lastIndexOf('-');
                    int cutDot = file.getPath().lastIndexOf('.');
                    long ID = Long.parseLong(file.getPath().substring(cutDash + 1, cutDot));

                    Date date = new Date(System.currentTimeMillis());

                    //Records Data into JSON File
                    String emptyJSON = "[ ]";

                    //checks to see if details.json exists
                    String path = FinishedPreviewActivity.this.getFilesDir().getAbsolutePath() + "/" + fileName;
                    File jsonFile = new File(path);
                    boolean isFilePresent = jsonFile.exists();

                    if(isFilePresent) { //if details.json exists
                        String jsonString = read(FinishedPreviewActivity.this, fileName);
                        parseJSON(jsonString, ID, date.toString());
                    } else { //create details.json
                        //try to create file
                        boolean isFileCreated;
                        try {
                            FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);
                            fos.write(emptyJSON.getBytes());
                            fos.close();
                            isFileCreated = true;
                        } catch (IOException e) {
                            e.printStackTrace();
                            isFileCreated = false;
                        }

                        if(isFileCreated) { //if creating file successful
                            parseJSON(emptyJSON, ID, date.toString());
                        } else {
                            System.err.println("Unable to create JSON");
                            Toast.makeText(FinishedPreviewActivity.this, "Unable to create details file", Toast.LENGTH_SHORT).show();
                        }
                    }

                    Uri savedImageURI = Uri.parse(file.getAbsolutePath());
                    Toast.makeText(FinishedPreviewActivity.this, "Image saved in internal storage.\n" + savedImageURI, Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(FinishedPreviewActivity.this, MainActivity.class);
                    intent.putExtra("detail", 1);
                    startActivity(intent);
                    finish();

                }
                catch (IOException e){
                    e.printStackTrace();
                    Toast.makeText(FinishedPreviewActivity.this, "Unable to save image", Toast.LENGTH_SHORT).show();
                    finish(); //Return to MainActivity
                }
            }
        });

    }

    private void parseJSON(String JSON, long ID, String date) {
        System.out.println(JSON);

        //write to JSON File
        try {
            JSONArray jsonArray = new JSONArray(JSON);
            JSONObject current = new JSONObject();
            current.put("ID", ID);
            current.put("date", date);
            jsonArray.put(current);

            try {
                FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);
                fos.write(jsonArray.toString().getBytes());
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String read(Context context, String fileName) {
        try {
            FileInputStream fis = context.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while((line=br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
