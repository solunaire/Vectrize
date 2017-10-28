package lol.primitive.primitivemobile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PreviewActivity extends AppCompatActivity {

    String picturePath;
    Uri pictureUri;
    byte[] bytes;
    boolean usesPicPath, usesUriPath;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Load Image into ImageView to Preview before Running Primitive
        Intent intent1 = getIntent();
        picturePath = intent1.getExtras().getString("key");
        pictureUri = (Uri) intent1.getExtras().get("uriKey");
        System.out.println(picturePath);
        ImageView imageView = (ImageView) findViewById(R.id.image_preview);
        if(picturePath != null) {
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            usesPicPath = true;
        } else if(pictureUri != null) {
            ParcelFileDescriptor parcelFD = null;
            try {
                parcelFD = getContentResolver().openFileDescriptor(pictureUri, "r");
                FileDescriptor imageSource = parcelFD.getFileDescriptor();

                // Decode image size
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(imageSource, null, o);

                // the new size we want to scale to
                final int REQUIRED_SIZE = 1024;

                // Find the correct scale value. It should be the power of 2.
                int width_tmp = o.outWidth, height_tmp = o.outHeight;
                int scale = 1;
                while (true) {
                    if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE) {
                        break;
                    }
                    width_tmp /= 2;
                    height_tmp /= 2;
                    scale *= 2;
                }

                // decode with inSampleSize
                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = scale;
                Bitmap bitmap = BitmapFactory.decodeFileDescriptor(imageSource, null, o2);

                imageView.setImageBitmap(bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (parcelFD != null)
                    try {
                        parcelFD.close();
                    } catch (IOException e) {
                        // ignored
                    }
            }

            usesUriPath = true;
        } else {
            bytes = (byte[]) intent1.getExtras().get("img");
            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            imageView.setImageBitmap(bitmap);
            usesPicPath = false;
        }

        //Choose Button Initialization
        FloatingActionButton btn = (FloatingActionButton) findViewById(R.id.usePicBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent optionsIntent = new Intent(PreviewActivity.this, OptionsActivity.class);
                if(usesPicPath) {
                    optionsIntent.putExtra("path", picturePath);
                } else if (usesUriPath) {
                    optionsIntent.putExtra("uri", pictureUri);
                } else {
                    optionsIntent.putExtra("img", bytes);
                }

                startActivity(optionsIntent);
                finish(); //Can't press back to load Preview Activity
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}