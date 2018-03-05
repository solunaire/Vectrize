package com.solunaire.vectrize;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {

    private String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/primitive";
    private Activity act = this;
    private final int PICK_IMAGE_REQUEST = 1;
    private final int TAKE_PICTURE_REQUEST = 115;
    private final int RETURN_ACTIVITY = 5;
    Uri imageUri;

    private final int CAMERA_PERMISSIONS_REQUEST = 1;
    private final int STORAGE_PERMISSIONS_REQUEST = 2;

    private String selectedImagePath;
    private String filemanagerstring;

    RecyclerView recyclerView;
    ArrayList<CreateList> galleryList;

    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (grantResults.length == 0 || grantResults[0] == PERMISSION_DENIED) {
                    break;
                }
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                File photoFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Photo.png");
                imageUri = FileProvider.getUriForFile(MainActivity.this,
                        BuildConfig.APPLICATION_ID + ".provider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, TAKE_PICTURE_REQUEST);
                break;

            case STORAGE_PERMISSIONS_REQUEST:
                if (grantResults.length == 0 || grantResults[0] == PERMISSION_DENIED) {
                    break;
                }
                //Image Gallery Initialization
                recyclerView = (RecyclerView) findViewById(R.id.imagegallery);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
                ArrayList<CreateList> createLists = prepareData();
                galleryList = createLists;
                MyAdapter adapter = new MyAdapter(getApplicationContext(), createLists);
                recyclerView.setAdapter(adapter);


                if (galleryList.size() == 0) {
                    findViewById(R.id.no_saved_imageview).setVisibility(View.VISIBLE);
                }
                else{
                    findViewById(R.id.no_saved_imageview).setVisibility(View.INVISIBLE);
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
                                startActivityForResult(intent, RETURN_ACTIVITY);
                            }
                        }));
                break;
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Intent Data when Starting from Other Activities (not new run)
        Intent intent1 = getIntent();
        String action = intent1.getAction();
        String type = intent1.getType();
        if (intent1.hasExtra("img_choose") && intent1.getExtras().getBoolean("img_choose")) {
            newImage();
        } else if (intent1.hasExtra("detail") && intent1.getExtras().getInt("detail") == 1) {
            openRecentImage();
            finish();
        }

        //Receiving Simple Data from Other Apps
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                Uri imageUri = (Uri) intent1.getParcelableExtra(Intent.EXTRA_STREAM);
                if (imageUri != null) {
                    Intent optionsIntent = new Intent(MainActivity.this, PreviewActivity.class);
                    String path = getFilePathFromURI(this.getApplicationContext(), imageUri);
                    optionsIntent.putExtra("key", path);
                    startActivity(optionsIntent);
                }
            }
        }

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
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSIONS_REQUEST);
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
                startActivityForResult(aboutIntent, RETURN_ACTIVITY);
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

        switch(requestCode) {
            case RETURN_ACTIVITY:
                ArrayList<CreateList> createLists = prepareData();
                galleryList = createLists;
                MyAdapter adapter = new MyAdapter(getApplicationContext(), createLists);
                recyclerView.setAdapter(adapter);
                if (galleryList.size() == 0) {
                    findViewById(R.id.no_saved_imageview).setVisibility(View.VISIBLE);
                }
                else{
                    findViewById(R.id.no_saved_imageview).setVisibility(View.INVISIBLE);
                }

                break;
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
                    MainActivity.this.startActivityForResult(myIntent1, RETURN_ACTIVITY);
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
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    if(inputStream != null) {
                        inputStream.close();
                    }
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

            MainActivity.this.startActivityForResult(myIntent1, RETURN_ACTIVITY);
        }
    }

    //Method to prepare gallery
    private ArrayList<CreateList> prepareData(){


        ArrayList<CreateList> theimage = new ArrayList<>();
        File f = new File(dir);
        f.mkdirs();
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
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public void newCameraImage() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSIONS_REQUEST);
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

    public void openRecentImage() {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        galleryList = prepareData();
        ArrayList<ImageModel> data = new ArrayList<>();
        //Add images & data into Arraylist of type ImageModel
        ImageModel im = new ImageModel();
        im.setUrl(dir + "/" + galleryList.get(0).getImage_file());
        data.add(im);

        //Send ArrayList of type ImageModel to show in Fullscreen Gallery
        intent.putParcelableArrayListExtra("data", data);
        intent.putExtra("pos", 0);
        startActivity(intent);
        finish();
    }

    public String getFilePathFromURI(Context context, Uri contentUri) {
        //copy file and send new file path
        String fileName = getFileName(contentUri);
        if (!TextUtils.isEmpty(fileName)) {
            File copyFile = new File(getCacheDir() + File.separator + fileName);
            copy(context, contentUri, copyFile);
            return copyFile.getAbsolutePath();
        }
        return null;
    }

    public String getFileName(Uri uri) {
        if (uri == null) return null;
        String fileName = null;
        String path = uri.getPath();
        int cut = path.lastIndexOf('/');
        if (cut != -1) {
            fileName = path.substring(cut + 1);
        }
        return fileName;
    }

    public void copy(Context context, Uri srcUri, File dstFile) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(srcUri);
            if (inputStream == null) return;
            OutputStream outputStream = new FileOutputStream(dstFile);
            IOUtils.copy(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}