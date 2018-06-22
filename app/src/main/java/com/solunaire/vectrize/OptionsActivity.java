package com.solunaire.vectrize;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

public class OptionsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opts);

        //Load Image into ImageView to Preview before Running Primitive
        Intent intent1 = getIntent();
        final String picturePath = intent1.getExtras().getString("path");
        final Uri pictureUri = (Uri) intent1.getExtras().get("uri");
        ImageView imageView = (ImageView) findViewById(R.id.image_preview_options);
        if(picturePath != null) {
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
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

        //For Total Shapes Counter
        final EditText editText = (EditText) findViewById(R.id.num_shapes_edit);
        final DiscreteSeekBar perIter = (DiscreteSeekBar) findViewById(R.id.shapes_iteration_slider);
        final TextView numShapes = (TextView) findViewById(R.id.num_shapes);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().equals("")) {
                    numShapes.setText("0");
                } else {
                    numShapes.setText(Integer.parseInt(editText.getText().toString())*perIter.getProgress() + "");
                }
            }
        });

        perIter.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                if(editText.getText().toString().equals("")) {
                    numShapes.setText("0");
                } else {
                    numShapes.setText(Integer.parseInt(editText.getText().toString())*perIter.getProgress() + "");
                }
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) { }
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
                //Check that Shapes Filled First
                EditText numShapesView = (EditText)findViewById(R.id.num_shapes_edit);
                String shapesStr = numShapesView.getText().toString();
                if (TextUtils.isEmpty(shapesStr)) {
                    numShapesView.setError(getString(R.string.error_field_required));
                    numShapesView.requestFocus();;
                } else if (!isNotInteger(shapesStr)) {
                    numShapesView.setError(getString(R.string.number_field_required));
                    numShapesView.requestFocus();;
                } else {
                    int numberShapes = Integer.parseInt(numShapesView.getText().toString());
                    Intent finishIntent = new Intent(OptionsActivity.this, FinishedPreviewActivity.class);
                    if(picturePath != null ) {
                        finishIntent.putExtra("path", picturePath);
                    } else {
                        finishIntent.putExtra("uri", pictureUri);
                    }
                    finishIntent.putExtra("inputSize", 256);
                    finishIntent.putExtra("outputSize", 1024);
                    finishIntent.putExtra("count", numberShapes);
                    finishIntent.putExtra("mode", shapesSpinner.getSelectedItemPosition()+1);
                    if (((CheckBox) findViewById(R.id.backgroundCheckBox)).isChecked()) {
                        finishIntent.putExtra("background", ((ColorDrawable) colorPicker.getBackground()).getColor());
                    } else {
                        finishIntent.putExtra("background", "");
                    }
                    finishIntent.putExtra("alpha", ((DiscreteSeekBar) findViewById(R.id.shape_alpha_slider)).getProgress());
                    finishIntent.putExtra("repeat", ((DiscreteSeekBar) findViewById(R.id.shapes_iteration_slider)).getProgress() - 1);
                    startActivity(finishIntent);
                    finish();
                }
            }
        });

    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // parent.getItemAtPosition(pos)
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Do Something
    }

    private boolean isNotInteger(String str) {
        int size = str.length();

        if(size > 4) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }

        int num = Integer.parseInt(str);
        if(num < 1 || num >= 1000) {
            return false;
        }
        return true;
    }
}
