//Gallery Adapter

package lol.primitive.primitivemobile;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private ArrayList<CreateList> galleryList;
    private Context context;

    public MyAdapter(Context context, ArrayList<CreateList> galleryList) {
        this.galleryList = galleryList;
        this.context = context;
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyAdapter.ViewHolder viewHolder, int i) {
        viewHolder.title.setText(galleryList.get(i).getImage_title());
        viewHolder.img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/primitive";
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;

        //Create Bitmap Image to place in ImageView (and detect whether SVG or other)
        String fileName = galleryList.get(i).getImage_file();
        if(getFileExtension(fileName).equals("svg")) { //If Image is SVG
            try {
                //TODO: SVG IMPLEMENTATION
                File file = new File(dir+"/"+fileName);
                FileInputStream fileInputStream = new FileInputStream(file);
//                SVG svg = SVGParser.getSVGFromInputStream(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else { //If image is not SVG
            final int THUMBSIZE = 300;
            Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(dir + "/" + fileName), THUMBSIZE, THUMBSIZE);
            viewHolder.img.setImageBitmap(ThumbImage);
        }

    }

    @Override
    public int getItemCount() {
        return galleryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView title;
        private ImageView img;
        public ViewHolder(View view) {
            super(view);

            title = (TextView)view.findViewById(R.id.title);
            img = (ImageView) view.findViewById(R.id.img);
        }
    }

    //Returns Thumbnail
    public static Bitmap getThumbnail(ContentResolver cr, String path) throws Exception {

        Cursor ca = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.MediaColumns._ID }, MediaStore.MediaColumns.DATA + "=?", new String[] {path}, null);
        if (ca != null && ca.moveToFirst()) {
            int id = ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID));
            ca.close();
            return MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MINI_KIND, null );
        }

        ca.close();
        return null;

    }

    //Returns File Extension
    public static String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }
}
