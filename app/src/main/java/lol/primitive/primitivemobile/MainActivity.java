package lol.primitive.primitivemobile;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/primitive";
    private Activity act = this;
    private final int PICK_IMAGE_REQUEST = 1;
    private final int TAKE_PICTURE_REQUEST = 115;
    Uri imageUri;

    private String selectedImagePath;
    private String filemanagerstring;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.v("Activity", "MainActivity Started");

        //Intent Data when Starting from Other Activities (not new run)
        Intent intent1 = getIntent();
        if(intent1.hasExtra("img_choose") && intent1.getExtras().getBoolean("img_choose")) {
            newImage();
        }

        //Image Gallery Initialization
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.imagegallery);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        ArrayList<CreateList> createLists = prepareData();
        final ArrayList<CreateList> galleryList = createLists;
        MyAdapter adapter = new MyAdapter(getApplicationContext(), createLists);
        recyclerView.setAdapter(adapter);

        //On ImageClick Listener
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                        ArrayList<ImageModel> data = new ArrayList<>();
                        //Add images & data into Arraylist of type ImageModel
                        for (int i = 0; i < galleryList.size(); i++) {
                            ImageModel im = new ImageModel();
                            im.setName(galleryList.get(i).getImage_title());
                            im.setUrl(dir + "/" + galleryList.get(i).getImage_file());
                            data.add(im);
                        }

                        //Send ArrayList of type ImageModel to show in Fullscreen Gallery
                        intent.putParcelableArrayListExtra("data", data);
                        intent.putExtra("pos", position);
                        startActivity(intent);
                    }
                }));

        //Toolbar Initialization
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //FAB Initialization
        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.fab_plus);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                newImage();
            }
        });
    }

    //Kabob Menu Initialization
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //Kabob Menu Selection
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri selectedImageUri;
        InputStream inputStream;

        switch(requestCode) {
            case PICK_IMAGE_REQUEST:
                if (resultCode == RESULT_OK) {
                    selectedImageUri = data.getData();

                    //OI FILE Manager
                    filemanagerstring = selectedImageUri.getPath();

                    //MEDIA GALLERY
                    selectedImagePath = getPath(selectedImageUri);

                    if (selectedImagePath != null) {
                        System.out.println("selectedImagePath is the right one for you!");
                        Log.v("FilePath", selectedImagePath);
                    } else {
                        System.out.println("filemanagerstring is the right one for you!");
                        Log.v("FilePath", filemanagerstring);
                    }

                    int flagval = 1;
                    sendImage(flagval);
                }
                break;
            case TAKE_PICTURE_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    selectedImageUri = imageUri;

                    //TODO: Send URI Over instead of image
                    try {
                        inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, null);

                        //Scale Down Image Size
                        final float densityMultiplier = this.getResources().getDisplayMetrics().density;

                        int h= (int) (100*densityMultiplier);
                        int w= (int) (h * bitmap.getWidth()/((double) bitmap.getHeight()));

                        bitmap=Bitmap.createScaledBitmap(bitmap, w, h, true);

                        //Then Convert to Byte Array
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
                        byte[] byteArray = stream.toByteArray();

                        Intent myIntent1 = new Intent(MainActivity.this, PreviewActivity.class);
                        myIntent1.putExtra("img", byteArray);
                        MainActivity.this.startActivity(myIntent1);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
        //No Defaults
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if(cursor!=null)
        {
            //HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            //THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        else return null;
    }

    public void sendImage(int flag) {

        if (flag == 1) {
            Intent myIntent1 = new Intent(MainActivity.this, PreviewActivity.class);
            if (selectedImagePath != null) {
                myIntent1.putExtra("key", selectedImagePath);
            } else {
                myIntent1.putExtra("key", filemanagerstring);
            }

            MainActivity.this.startActivity(myIntent1);
        }
    }

    //Method to prepare gallery
    private ArrayList<CreateList> prepareData(){
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        ArrayList<CreateList> theimage = new ArrayList<>();
        File f = new File(dir);
        if(!f.exists()) {f.mkdirs(); }

        //Directory Tests
        Log.v("Files", f.exists()+"");
        Log.v("Files", f.isDirectory()+"");
        Log.v("Files", f.listFiles()+"");
        Log.v("Files", dir);

        File file[] = f.listFiles();
        //Add all files to ArrayList "theimage"
        for (int i=0; i < file.length; i++)
        {
            CreateList createList = new CreateList();
            createList.setImage_Location(file[i].getName());
            createList.setImage_title(file[i].getName());
            createList.setImage_ID(i);
            theimage.add(createList);
        }
        return theimage;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public void newImage() {
        //Either Pick Image Using Camera or from Gallery
        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        builder.setMessage("Pick Image from Gallery or Camera:")
                .setNegativeButton("Camera", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Load Image through Camera (Intent)
                        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                        File photoFile = new File(dir,  "Photo.png");
                        imageUri = FileProvider.getUriForFile(MainActivity.this,
                                BuildConfig.APPLICATION_ID + ".provider", photoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        startActivityForResult(intent, TAKE_PICTURE_REQUEST);
                    }
                })
                .setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Load Image through Gallery (Intent)
                        Intent intent = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, PICK_IMAGE_REQUEST);

                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
