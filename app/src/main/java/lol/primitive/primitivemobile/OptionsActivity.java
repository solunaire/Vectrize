package lol.primitive.primitivemobile;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

public class OptionsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opts);

        //Load Image into ImageView to Preview before Running Primitive
        Intent intent1 = getIntent();
        final String picturePath = intent1.getExtras().getString("path");
        ImageView imageView = (ImageView) findViewById(R.id.image_preview_options);
        if(picturePath != null) {
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
        } else {
            byte[] bytes = (byte[]) intent1.getExtras().get("img");
            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            imageView.setImageBitmap(bitmap);
        }

        //Shapes Spinner (Dropdown) Initialization
        final Spinner shapesSpinner = (Spinner) findViewById(R.id.shapes_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.shapes_spinner_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shapesSpinner.setAdapter(adapter);
        shapesSpinner.setOnItemSelectedListener(this);

        //Working Size Spinner Initialization
        Spinner sizesSpinner = (Spinner) findViewById(R.id.working_size_spinner);
        ArrayAdapter<CharSequence> sizesAdapter = ArrayAdapter.createFromResource(this,
                R.array.sizes_spinner_array, android.R.layout.simple_spinner_item);
        sizesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sizesSpinner.setAdapter(sizesAdapter);
        sizesSpinner.setOnItemSelectedListener(this);

        String background = "";
        //Color Picker Initialization
        final View colorPicker = findViewById(R.id.colorPickerBtn);
        colorPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ColorPickerDialogBuilder
                        .with(OptionsActivity.this)
                        .setTitle("Choose Background Color")
                        .initialColor(Color.rgb(255, 255, 255))
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int selectedColor) {
                                Toast.makeText(OptionsActivity.this, "onColorSelected: 0x" + Integer.toHexString(selectedColor), Toast.LENGTH_LONG);
                            }
                        })
                        .setPositiveButton("OK", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                colorPicker.setBackgroundColor(selectedColor);
                                CheckBox backgroundCB = (CheckBox)findViewById(R.id.backgroundCheckBox);
                                backgroundCB.setChecked(true);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        }).build().show();
            }
        });

        //Button Initializations
        Button otherBtn = (Button) findViewById(R.id.otherButton);
        otherBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent optionsIntent = new Intent(OptionsActivity.this, MainActivity.class);
                optionsIntent.putExtra("img_choose", true);
                startActivity(optionsIntent);
            }
        });

        Button cancelBtn = (Button) findViewById(R.id.cancelOptionsBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button runBtn = (Button) findViewById(R.id.runOptionsBtn);
        runBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Run Primitive Libraries
                Intent finishIntent = new Intent(OptionsActivity.this, FinishedPreviewActivity.class);
                finishIntent.putExtra("path",picturePath);
                finishIntent.putExtra("inputSize",256);
                finishIntent.putExtra("outputSize",1024);
                finishIntent.putExtra("count",30);
                finishIntent.putExtra("mode",shapesSpinner.getSelectedItemPosition());
                if (((CheckBox)findViewById(R.id.backgroundCheckBox)).isChecked()){
                    finishIntent.putExtra("background",((ColorDrawable)colorPicker.getBackground()).getColor());
                }
                else {
                    finishIntent.putExtra("background","");
                }
                finishIntent.putExtra("alpha",((DiscreteSeekBar) findViewById(R.id.shape_alpha_slider)).getProgress());
                finishIntent.putExtra("repeat", ((DiscreteSeekBar) findViewById(R.id.shapes_iteration_slider)).getProgress()-1);
                //TODO: Change to SVG Image
//                finishIntent.putExtra("svg_image", img);
                startActivity(finishIntent);
                finish();
            }
        });

    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // parent.getItemAtPosition(pos)
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Do Something
    }

    //Radio Button Selectors
    public void onRadioButtonClickedStopped(View v) {
        //Do nothing for now
    }

    public void onRadioButtonClickedShapes(View v) {
        //Do nothing for now
    }

    public void onRadioButtonClickedScore(View v) {
        //Do nothing for now
    }
}
