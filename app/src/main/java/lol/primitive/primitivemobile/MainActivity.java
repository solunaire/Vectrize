package lol.primitive.primitivemobile;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.io.File;
import java.io.FileOutputStream;
import java.security.Permission;
import java.util.ArrayList;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

public class MainActivity extends AppCompatActivity {

    private String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/primitive";
    private Activity act = this;
    private int PICK_IMAGE_REQUEST = 1;

    private String selectedImagePath;
    private String filemanagerstring;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*TEST CODE FOR IMAGE SAVING PLEASE IGNORE
        String fname = "Image-1.jpg";
        int n = 1;
        File file = new File(dir, fname);
        while(file.exists()) {
            n++;
            fname="Image-" + n + ".jpg";
            file = new File(dir, fname);
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            Drawable drawable = getResources().getDrawable(R.drawable.test_image);
            Bitmap finalBitmap = ((BitmapDrawable)drawable).getBitmap();
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        MediaScannerConnection.scanFile(this, new String[] {file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                }); */

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
        FabSpeedDial fab = (FabSpeedDial) findViewById(R.id.fab_speed);
        fab.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                switch(menuItem.toString()) {
                    case "New": Log.v("MenuListener", "0");

                        //Either Pick Image Using Camera or from Gallery
                        AlertDialog.Builder builder = new AlertDialog.Builder(act);
                        builder.setMessage("Pick Image from Gallery or Camera:")
                                .setNegativeButton("Camera", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //Load Image through Camera (Intent)
                                        Intent cameraIntent = new Intent(act, CameraActivity.class);
                                        startActivity(cameraIntent);
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
                        break;
                    case "Edit": Log.v("MenuListener", "1");
                        break;
                    case "Open": Log.v("MenuListener", "2");
                        break;
                    case "Save": Log.v("MenuListener", "3");
                        break;
                    case "Cancel": Log.v("MenuListener", "4");
                        break;
                    default:
                        Log.v("MenuListener", menuItem.toString());
                        Log.v("MenuListener", menuItem.getItemId()+" ");
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                Uri selectedImageUri = data.getData();

                //OI FILE Manager
                filemanagerstring = selectedImageUri.getPath();

                //MEDIA GALLERY
                selectedImagePath = getPath(selectedImageUri);

                //DEBUG PURPOSE - you can delete this if you want
                if(selectedImagePath!=null)
                    System.out.println(selectedImagePath);
                else System.out.println("selectedImagePath is null");
                if(filemanagerstring!=null)
                    System.out.println(filemanagerstring);
                else System.out.println("filemanagerstring is null");

                //NOW WE HAVE OUR WANTED STRING
                if(selectedImagePath!=null) {
                    System.out.println("selectedImagePath is the right one for you!");
                    Log.v("FilePath", selectedImagePath);
                } else {
                    System.out.println("filemanagerstring is the right one for you!");
                    Log.v("FilePath", filemanagerstring);
                }

                int flagval = 1;
                sendImage(flagval);
            }
        }
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if(cursor!=null)
        {
            //HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            //THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
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
}
