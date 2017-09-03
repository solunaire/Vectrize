package lol.primitive.primitivemobile;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.PhotoView;

public class ChooseImageActivity extends AppCompatActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_image);

        //Built-In Gallery Intent to Choose File
        Intent intent1 = getIntent();
        String picturePath = intent1.getExtras().getString("key");
        PhotoView imageView = (PhotoView) findViewById(R.id.image_preview);
        imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));


    }


}