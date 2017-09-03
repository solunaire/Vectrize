package lol.primitive.primitivemobile;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class PreviewActivity extends AppCompatActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        //Load Image into ImageView to Preview before Running Primitive
        Intent intent1 = getIntent();
        String picturePath = intent1.getExtras().getString("key");
        ImageView imageView = (ImageView) findViewById(R.id.image_preview);
        imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
    }


}