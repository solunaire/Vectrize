package lol.primitive.primitivemobile;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

public class MainActivity extends AppCompatActivity {

    private String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/primitive";
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
        if (intent1.hasExtra("img_choose") && intent1.getExtras().getBoolean("img_choose")) {
            newImage();
        }

        //Image Gallery Initialization
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.imagegallery);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        ArrayList<CreateList> createLists = prepareData();
        final ArrayList<CreateList> galleryList = createLists;
        MyAdapter adapter = new MyAdapter(getApplicationContext(), createLists);
        recyclerView.setAdapter(adapter);

        if (galleryList.size() == 0) {
            findViewById(R.id.no_saved_imageview).setVisibility(View.VISIBLE);
        }

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
        FabSpeedDial fab = (FabSpeedDial) findViewById(R.id.fab_speed);
        fab.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                switch (menuItem.toString()) {
                    case "Camera":
                        Log.v("MenuListener", "0");
                        newCameraImage();
                        break;
                    case "Gallery":
                        Log.v("MenuListener", "1");
                        newGalleryImage();
                        break;
                    default:
                        Log.v("MenuListener", menuItem.toString());
                        Log.v("MenuListener", menuItem.getItemId() + " ");
                        break;
                }
                return false;
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
        switch (id) {
            case R.id.action_about:
                Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(aboutIntent);
                return true;
//            case R.id.action_help:
//                return true;
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

                        File tempFile = new File(selectedImagePath);
                        if(!tempFile.exists()) {
                            selectedImagePath = getImagePathFromInputStreamUri(selectedImageUri);
                        }
                    } else {
                        System.out.println("filemanagerstring is the right one for you!");
                        Log.v("FilePath", filemanagerstring);

                        File tempFile = new File(filemanagerstring);
                        if(!tempFile.exists()) {
                            filemanagerstring = getImagePathFromInputStreamUri(selectedImageUri);
                        }
                    }

                    int flagval = 1;
                    sendImage(flagval);
                }
                break;
            case TAKE_PICTURE_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    selectedImageUri = imageUri;

                    Intent myIntent1 = new Intent(MainActivity.this, PreviewActivity.class);
                    myIntent1.putExtra("uriKey", selectedImageUri);
                    MainActivity.this.startActivity(myIntent1);
                }

                break;
        }
        //No Defaults
    }

    public String getImagePathFromInputStreamUri(Uri uri) {
        InputStream inputStream = null;
        String filePath = null;

        if (uri.getAuthority() != null) {
            try {
                inputStream = getContentResolver().openInputStream(uri); // context needed
                File photoFile = createTemporalFileFrom(inputStream);

                filePath = photoFile.getPath();

            } catch (FileNotFoundException e) {
                // log
            } catch (IOException e) {
                // log
            }finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return filePath;
    }

    private File createTemporalFileFrom(InputStream inputStream) throws IOException {
        File targetFile = null;

        if (inputStream != null) {
            int read;
            byte[] buffer = new byte[8 * 1024];

            targetFile = createTemporalFile();
            OutputStream outputStream = new FileOutputStream(targetFile);

            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();

            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return targetFile;
    }

    private File createTemporalFile() {
        return new File(getExternalCacheDir(), "tempFile.jpg"); // context needed
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

        //Sort file[] based on Date Last Modified
        Arrays.sort(file, new Comparator()
        {
            public int compare(Object o1, Object o2) {
                if (((File)o1).lastModified() > ((File)o2).lastModified()) {
                    return -1;
                } else if (((File)o1).lastModified() < ((File)o2).lastModified()) {
                    return +1;
                } else {
                    return 0;
                }
            }
        });

        //Add all files to ArrayList "theimage"
        for (int i=0; i < file.length; i++)
        {
            CreateList createList = new CreateList();
            createList.setImage_Location(file[i].getName());
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

    public void newCameraImage() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File photoFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),  "Photo.png");
        imageUri = FileProvider.getUriForFile(MainActivity.this,
                BuildConfig.APPLICATION_ID + ".provider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PICTURE_REQUEST);
    }
    public void newGalleryImage() {
        //Load Image through Gallery (Intent)
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    public void newImage() {
        //Either Pick Image Using Camera or from Gallery
        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        builder.setMessage("Pick Image from Gallery or Camera:")
                .setNegativeButton("Camera", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        newCameraImage();
                    }
                })
                .setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        newGalleryImage();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}