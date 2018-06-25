package com.solunaire.vectrize;

import android.content.Context;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        String uri = getIntent().getStringExtra("uri");
        DetailAdapter adapter = new DetailAdapter(this);
        String date = "", background = "", mode = "";
        int alpha=-1, repeat=-1, count=-1, shapes=-1;
        boolean foundObject = false;
        long fileID;

        int dashCut = uri.lastIndexOf('-');
        int dotCut = uri.lastIndexOf('.');
        fileID = Long.parseLong(uri.substring(dashCut+1, dotCut));

        try {
            String JSON = readJSON(this, "details.json");
            JSONArray jsonArray = new JSONArray(JSON);

            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                if(obj.getLong("ID") == fileID) {
                    date = obj.getString("date");
                    alpha = obj.getInt("alpha");
                    repeat = obj.getInt("repeat");
                    count = obj.getInt("count");
                    mode = obj.getString("mode");
                    shapes = obj.getInt("shapes");
                    //TODO: Fix background
                    //background = obj.getString("background");
                    foundObject = true;
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(!date.equals("")) {
            adapter.list.add(new SingleRow("Date",
                    date,
                    R.drawable.calendar));
        }
        adapter.list.add(new SingleRow("File Path",
                uri,
                R.drawable.image));

        if(foundObject) {
            adapter.list.add(new SingleRow("Shape Type",
                    mode,
                    R.drawable.shape));
            adapter.list.add(new SingleRow("Total Number of Shapes",
                    shapes+"",
                    R.drawable.numeric));
            //TODO: Fix background
//            adapter.list.add(new SingleRow("Background",
//                    background,
//                    R.drawable.color_lens));
            adapter.list.add(new SingleRow("Opacity",
                    alpha+"",
                    R.drawable.opacity));
        }

        adapter.list.add(new SingleRow("Unique ID",
                fileID+"",
                R.drawable.alert));

        ListView listView = (ListView) findViewById(R.id.details_listView);
        listView.setAdapter(adapter);
        listView.setDivider(null);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private String readJSON(Context context, String fileName) {
        try {
            FileInputStream fis = context.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while((line=br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

class SingleRow {
    String title, description;
    int imageID;

    SingleRow(String t, String d, int i) {
        title = t;
        description = d;
        imageID = i;
    }
}

class DetailAdapter extends BaseAdapter {

    ArrayList<SingleRow> list;
    Context context;

    DetailAdapter(Context c) {
        context = c;
        list = new ArrayList<SingleRow>();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = layoutInflater.inflate(R.layout.row_layout, parent, false);

        TextView title = (TextView) row.findViewById(R.id.textViewTitle);
        TextView description = (TextView) row.findViewById(R.id.textViewDescription);
        ImageView img = (ImageView) row.findViewById(R.id.row_icon);

        SingleRow current = list.get(position);
        title.setText(current.title);
        description.setText(current.description);
        img.setBackgroundResource(current.imageID);

        return row;
    }
}
