package lol.primitive.primitivemobile;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ShareActionProvider mShareActionProvider;

    //ArrayList containing ImageModels (implementing Parcelable) to store images & data
    public ArrayList<ImageModel> data = new ArrayList<>();
    int pos; //current index of image in RecyclerView (and therefore data)

    Toolbar toolbar;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Log.v("Activity", "DetailActivity Started");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Integer.decode("#FFFFFF")));

        data = getIntent().getParcelableArrayListExtra("data");
        pos = getIntent().getIntExtra("pos", 0);

        setTitle(data.get(pos).getName());

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), data);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setPageTransformer(true, new DepthPageTransformer());

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {}

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            public void onPageSelected(int position) {
                pos = position;
            }
        });

        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(pos);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                //noinspection ConstantConditions
                setTitle(data.get(position).getName());

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        ImageButton shareBtn = (ImageButton) findViewById(R.id.share);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareIntent();
            }
        });

        ImageButton delBtn = (ImageButton) findViewById(R.id.delete);
        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        MenuItem item = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        return(super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_share:
                shareIntent();
                return true;
            case R.id.action_details:
                //TODO: Details
                System.out.println("pos = " + pos );
                return true;
            case R.id.action_delete:
                delete();
                return true;
            default:
                Log.v("MenuItem", id+"");
        }

        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public ArrayList<ImageModel> data = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager fm, ArrayList<ImageModel> data) {
            super(fm);
            this.data = data;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position, data.get(position).getName(), data.get(position).getUrl());
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return data.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return data.get(position).getName();
        }
    }


    public static class PlaceholderFragment extends Fragment {
        String name, url;
        int pos;
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String ARG_IMG_TITLE = "image_title";
        private static final String ARG_IMG_URL = "image_url";

        @Override
        public void setArguments(Bundle args) {
            super.setArguments(args);
            this.pos = args.getInt(ARG_SECTION_NUMBER);
            this.name = args.getString(ARG_IMG_TITLE);
            this.url = args.getString(ARG_IMG_URL);
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber, String name, String url) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putString(ARG_IMG_TITLE, name);
            args.putString(ARG_IMG_URL, url);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public void onStart() {
            super.onStart();

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail2, container, false);

            final ImageView imageView = (ImageView) rootView.findViewById(R.id.detail_image);

            Glide.with(getActivity()).load(url).thumbnail(0.1f).into(imageView);

            return rootView;
        }

    }

    private void shareIntent() {
        System.out.println("SHARING INTENT!!!");
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/jpeg");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+data.get(pos).getUrl()));
        startActivity(Intent.createChooser(shareIntent, "Share image"));
    }

    private void delete() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this image?")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Do Nothing
                    }
                })
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Uri currUri = Uri.parse(data.get(pos).getUrl());
                        File fdelete = new File(currUri.getPath());
                        if (fdelete.exists()) {
                            if (fdelete.delete()) {
                                data.remove(pos);
                                mSectionsPagerAdapter.notifyDataSetChanged();
                                if(data.size() == 0) {
                                    finish();
                                } else if(pos+1 == data.size()) {
                                    mViewPager.setCurrentItem(--pos);
                                } else {
                                    mViewPager.setCurrentItem(pos);
                                }

                                Intent refreshIntent = new Intent(DetailActivity.this, DetailActivity.class);
                                refreshIntent.putParcelableArrayListExtra("data", data);
                                refreshIntent.putExtra("pos", pos);
                                startActivity(refreshIntent);
                                finish();

                                Toast.makeText(DetailActivity.this, "File Deleted", Toast.LENGTH_SHORT).show();
                            } else {
                                System.out.println("file not Deleted :" + currUri.getPath());
                                Toast.makeText(DetailActivity.this, "Unable to Delete File. Please Report Bug", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }
}
