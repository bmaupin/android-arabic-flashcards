package us.bmaupin.flashcards.arabic;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.widget.TextView;

public class About extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		String versionName;
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		// get the version name of the app
		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			versionName = packageInfo.versionName; 

		} catch (android.content.pm.PackageManager.NameNotFoundException e) {
		    e.printStackTrace();
		    versionName = "Cannot load Version!";
		}
		
		TextView tv = (TextView)findViewById(R.id.about_content);
		
		tv.setText("Version: " + versionName + "\n\n" + getString(R.string.about_text));
	}
}
