package lol.primitive.primitivemobile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import static android.content.Intent.getIntent;

public class PreviewActivity extends AppCompatActivity {

    String picturePath;
    byte[] bytes;
    boolean usesPicPath;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Log.v("Activity", "PreviewActivity Started");

        //Load Image into ImageView to Preview before Running Primitive
        Intent intent1 = getIntent();
        picturePath = intent1.getExtras().getString("key");
        ImageView imageView = (ImageView) findViewById(R.id.image_preview);
        if(picturePath != null) {
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            usesPicPath = true;
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
                } else {
                    optionsIntent.putExtra("img", bytes);
                }

                startActivity(optionsIntent);

                //Can't press back to load Preview Activity
                finish();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}