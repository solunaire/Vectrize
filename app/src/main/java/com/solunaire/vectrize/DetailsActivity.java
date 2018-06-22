package com.solunaire.vectrize;

import android.content.Context;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        DetailAdapter adapter = new DetailAdapter(this);
        adapter.list.add(new SingleRow("Title", "Description", R.drawable.alert));
        adapter.list.add(new SingleRow("Title 2", "Description 2", R.drawable.alert));

        ListView listView = (ListView) findViewById(R.id.details_listView);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
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
