package com.demandmedia.livestrong.android.utilities;

import java.io.InputStream;
import java.io.OutputStream;

import com.demandmedia.livestrong.android.Constants;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;


public class Utils {
    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
              int count=is.read(bytes, 0, buffer_size);
              if(count==-1)
                  break;
              os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }
    
    public static void openPlayStore(Activity currentActivity) {
    	try {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("market://details?id=" + Constants.APPLICATION_PACKAGE_ID_LITE));
			currentActivity.startActivity(intent);
		} catch (ActivityNotFoundException ex) {
			// Thrown if market app is not installed
			Toast.makeText(currentActivity,
					"Cannot open Play Store. Please search for LiveStrong Lite in the Play Store.",
					Toast.LENGTH_LONG)
					.show();
		}
    }
}