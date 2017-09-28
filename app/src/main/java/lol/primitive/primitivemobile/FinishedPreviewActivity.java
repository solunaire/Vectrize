package lol.primitive.primitivemobile;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.pixplicity.sharp.Sharp;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import static android.R.attr.bitmap;

import go.primitivemobile.Primitivemobile;



public class FinishedPreviewActivity extends AppCompatActivity {

    private static final String DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/primitive";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finished_preview);
        final Intent intent = getIntent();
        AVLoadingIndicatorView avi = (AVLoadingIndicatorView) findViewById(R.id.avi);
        avi.show();
        final ImageView imageView = (ImageView) findViewById(R.id.finished_image_preview);
        if(intent.hasExtra("svg_image")) {
            imageView.setImageBitmap((Bitmap) intent.getExtras().get("svg_image"));
        } else {
            imageView.setBackgroundResource(R.drawable.error);
        }
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
                Sharp.loadString(Primitivemobile.ProcessImage(path,inputSize,outputSize,count,mode,background,alpha,repeat)).into(imageView);
            }
        }).start();


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

        Button editBtn = (Button) findViewById(R.id.editFinishedBtn);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Sharp.loadString("<svg height='150' width='500'>\n" +
                        "<circle cx='200' cy='200' r='100' stroke='black' stroke-width='3' fill='red' />\n" +
                        "</svg>\n").into(imageView);
            }
        });
        //TODO: onClickListener for editBtn

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
                int n = (int)(Math.random()* Integer.MAX_VALUE);
                String fname = "Image-" + n + ".jpg";
                File file = new File(DIR, fname);
                while(file.exists()) {
                    n = (int) (Math.random() * Integer.MAX_VALUE);
                    fname = "Image-" + n + ".jpg";
                }

                //Process for Saving Image:
                try {
                    OutputStream stream = new FileOutputStream(file);
                    Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream); //TODO: change to SVG saving
                    stream.flush();
                    stream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Uri savedImageURI = Uri.parse(file.getAbsolutePath());
                Toast.makeText(FinishedPreviewActivity.this, "Image saved in internal storage.\n" + savedImageURI, Toast.LENGTH_SHORT).show();
                finish(); //Return to MainActivity
            }
        });

    }
}
