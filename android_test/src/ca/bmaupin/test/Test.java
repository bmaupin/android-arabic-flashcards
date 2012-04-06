package ca.bmaupin.test;

import java.util.Arrays;

import com.googlecode.chartdroid.pie.ChartPanelActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Test extends Activity {
	private static final String TAG = "Test";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_with_button);

        Button buttonTest = (Button) findViewById(R.id.button_test);
        buttonTest.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
/*                
                Intent i = new Intent(Intent.ACTION_VIEW, 
                        MyProvider.CONTENT_URI);
//                i.putExtra(
//                        "com.googlecode.chartdroid.intent.extra.SERIES_LINE_THICKNESSES", 
//                        new float[] {5f});
                startActivity(i);
                
                
                /*
                 * ACTION_VIEW
                Intent intent = new Intent(ChooseStudySet.this, 
                        ChooseCardGroup.class);
                startActivityForResult(intent, REQUEST_CARD_SET_BROWSE);
                */
                
                final String[] demo_pie_labels = new String[] {
                    "known",
                    "iffy",
                    "unknown"
                };
                final int[] demo_pie_data = new int[] {12, 5, 3};
/*                int[] colors = new int[demo_pie_labels.length];
                for (int j=0; j<demo_pie_labels.length; j++)
                                colors[j] = Color.HSVToColor(new float[] {360 * j / (float) colors.length, 0.6f, 1});
                Log.d(TAG, "colors=" + Arrays.toString(colors));
*/              
                int[] colors = {
                        Color.parseColor("#66FF66"),
                        Color.parseColor("#CDFF66"),
                        Color.parseColor("#FF6666")
                };
                /*
                int[] colors = {-10027162, -3276954, -39322};
                Log.d(TAG, String.format("#%06X", (0xFFFFFF & -10027162)));
                Log.d(TAG, String.format("#%06X", (0xFFFFFF & -3276954)));
                Log.d(TAG, String.format("#%06X", (0xFFFFFF & -39322)));
                /*
                int[] colors = {
                        /*
                        Color.parseColor("#66ff65"),
                        //Color.parseColor("#ff99009a"),//-10027162,
                        Color.parseColor("#dff650"),
                        //-3276954,
                        -39322
                        *//*};
                // color-blind safe colors
/*                int[] colors = {
                        Color.parseColor("#1BA1E2"),
                        -3276954,
                        Color.parseColor("#674f00")
                        };
*/
                Intent i = new Intent(Test.this, ChartPanelActivity.class);
                i.putExtra(Intent.EXTRA_TITLE, "Summary");
                i.putExtra(ChartPanelActivity.EXTRA_LABELS, demo_pie_labels);
                i.putExtra(ChartPanelActivity.EXTRA_DATA, demo_pie_data);
                i.putExtra(ChartPanelActivity.EXTRA_COLORS, colors);
                startActivity(i);
            }
        });
        
    }
    
    /* Inflates the menu */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    /* Handles menu selections */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_exit:
            finish();
            return true;
        }
        return false;
    }
}