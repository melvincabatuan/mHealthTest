package ph.edu.dlsu.mhealthtest;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by cobalt on 1/4/16.
 */
public class MenuActivity extends ListActivity {

    public final static String TAG = "tag.MenuActivity";

    private static TopLevel[] mMenuItems;

    private static String [] mTitles;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        initializeList();
    }


    public void initializeList() {



        /**************         ADD NEW ACTIVITIES HERE      **************/

        // Instantiate the list of top level items.
        mMenuItems = new TopLevel[]{
                new TopLevel(R.string.title_camera_preview, CameraPreviewActivity.class),
                new TopLevel(R.string.title_face_detection, FaceDetectionActivity.class),
                new TopLevel(R.string.title_image_filter, ImageFilterActivity.class),
                new TopLevel(R.string.title_image_matching, ImageMatchingActivity.class),
        };

        /******************************************************************/


        // Extract titles from the TopLevel activities
        mTitles = new String[mMenuItems.length];
        for(int position = 0; position < mMenuItems.length; ++position) {
            mTitles[position] = mMenuItems[position].title.toString();
            // Log.d(TAG, "mTitles[position] = " + mTitles[position]);
        }

        // Display list: Adapter + ListView
        setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mTitles));
    }



    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        startActivity(new Intent(this, mMenuItems[position].activityClass));
    }



    /**
     * This class describes individual top level items (the sample title, and the activity class)
     */
    private class TopLevel {
        private CharSequence title;
        private Class<? extends AppCompatActivity> activityClass;

        public TopLevel(int titleResId, Class<? extends AppCompatActivity> activityClass) {
            this.activityClass = activityClass;
            this.title = getResources().getString(titleResId);
        }

        @Override
        public String toString() {
            return title.toString();
        }
    }
}
