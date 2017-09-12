package lol.primitive.primitivemobile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

public class OptionsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        //Load Image into ImageView to Preview before Running Primitive
        Intent intent1 = getIntent();
        String picturePath = intent1.getExtras().getString("path");
        ImageView imageView = (ImageView) findViewById(R.id.image_preview_options);
        if(picturePath != null) {
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
        } else {
            byte[] bytes = (byte[]) intent1.getExtras().get("img");
            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            imageView.setImageBitmap(bitmap);
        }

        //Spinner (Dropdown) Initialization
        Spinner shapesSpinner = (Spinner) findViewById(R.id.shapes_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.shapes_spinner_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shapesSpinner.setAdapter(adapter);
        shapesSpinner.setOnItemSelectedListener(this);
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // parent.getItemAtPosition(pos)
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Do Something
    }
}
