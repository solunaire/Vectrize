package lol.primitive.primitivemobile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class LicensesAdapter extends RecyclerView.Adapter<LicensesAdapter.ViewHolder> {
    private ArrayList<String> libraries = new ArrayList<String>();
    private ArrayList<String> licenses = new ArrayList<String>();
    private ArrayList<String> urls = new ArrayList<String>();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public TextView mTextView2;
        public View layout;
        public ViewHolder(View v) {
            super(v);
            layout = v;
            mTextView = (TextView)v.findViewById(R.id.library);
            mTextView2 = (TextView)v.findViewById(R.id.license);
        }
    }

    public LicensesAdapter(String[] myDataset) {
        for(int i = 0; i < myDataset.length; i++) {
            libraries.add(myDataset[i]);
            licenses.add(myDataset[++i]);
            urls.add(myDataset[++i]);
        }
    }

    @Override
    public LicensesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.license_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mTextView.setText(libraries.get(position));
        holder.mTextView2.setText(licenses.get(position));
        final int pos = position;
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urls.get(pos)));
                view.getContext().startActivity(browserIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return libraries.size();
    }
}
