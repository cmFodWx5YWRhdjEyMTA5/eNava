

package  com.enavamaratha.enavamaratha.fragment;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import  com.enavamaratha.enavamaratha.MainApplication;
import  com.enavamaratha.enavamaratha.R;
import com.enavamaratha.enavamaratha.activity.GeneralPrefsActivity;
import com.enavamaratha.enavamaratha.activity.HomeActivity;
import  com.enavamaratha.enavamaratha.service.RefreshService;
import  com.enavamaratha.enavamaratha.utils.PrefUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GeneralPrefsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.general_preferences);

        setRingtoneSummary();

        Preference preference = findPreference(PrefUtils.REFRESH_ENABLED);
        preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Activity activity = getActivity();
                if (activity != null) {
                    if (Boolean.TRUE.equals(newValue)) {
                        activity.startService(new Intent(activity, RefreshService.class));
                    } else {
                        PrefUtils.putLong(PrefUtils.LAST_SCHEDULED_REFRESH, 0);
                        activity.stopService(new Intent(activity, RefreshService.class));
                    }
                }
                return true;
            }
        });


        // Delete Saved Epapers
        PreferenceScreen prefer = (PreferenceScreen) findPreference("cacheclear");
        prefer.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Delete All ePapers");
                builder.setMessage("Do you Really Want To Delete All Saved ePapers...?");

                String positiveText = getString(android.R.string.ok);
                builder.setPositiveButton(positiveText,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {


                                // For Pdf Delete Logic
                                deleteEpapers();


                                // Delete Epaper Cache Logic
                             /*   // object fo ClearEaperCache class
                                ClearEpaperCache object = new ClearEpaperCache();

                                // delete is a method for delete
                                object.Del();
                                // positive button logic
*/
                            }
                        });

                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();

                    }
                });


                AlertDialog dialog = builder.create();
                // display dialog
                dialog.show();

                return true;
            }
        });


    }


    @Override
    public void onResume() {
        // The ringtone summary text should be updated using
        // OnSharedPreferenceChangeListener(), but I can't get it to work.
        // Updating in onResume is a very simple hack that seems to work, but is inefficient.
        setRingtoneSummary();

        super.onResume();

    }

    private void setRingtoneSummary() {
        Preference ringtone_preference = findPreference(PrefUtils.NOTIFICATIONS_RINGTONE);
        Uri ringtoneUri = Uri.parse(PrefUtils.getString(PrefUtils.NOTIFICATIONS_RINGTONE, ""));
        if (TextUtils.isEmpty(ringtoneUri.toString())) {
            ringtone_preference.setSummary(R.string.settings_notifications_ringtone_none);
        } else {
            Ringtone ringtone = RingtoneManager.getRingtone(MainApplication.getContext(), ringtoneUri);
            if (ringtone == null) {
                ringtone_preference.setSummary(R.string.settings_notifications_ringtone_none);
            } else {
                ringtone_preference.setSummary(ringtone.getTitle(MainApplication.getContext()));
            }
        }
    }

    // clear epaper image cache from memory

    public class ClearEpaperCache
    {
        Context context;
        SQLiteDatabase mDatabase;
        String mytable1 = "mytable1";
        final String DATABASE_NAME = "MyMb";

        public Context getContext() {
            return context;
        }

        private void Del()
       {
           mDatabase =  getActivity().openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);


           // check table is exist or not
           if ((isTableExists(mytable1)))

           {
              mDatabase.execSQL("delete from mytable1");

               // update cache table with current date
               final String ROWID = "id";
               SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
               String currentDateandTime = sdf.format(new Date());

               // update date and time in table
               ContentValues args = new ContentValues();
               args.put("time", currentDateandTime);
               mDatabase.update("cache", args, ROWID + "=" + 1, null);

               // call clear method for clear cache dirctory
               clear();

           }

           mDatabase.close();
       }






        private void clear()
        {
            // TODO Auto-generated method stub
            File cache = new File(getActivity().getFilesDir(), "/Epaper/");
            // if file is exist
            if (cache.exists() && cache.isDirectory())
            {
                Date lastModDate = new Date(cache.lastModified());
                // delete cache directory of epaper
                deleteDir(cache);
                //  DELDir(cache);
                // Toast.makeText(context, "Your All Cache is clear", Toast.LENGTH_SHORT).show();


            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Cache Clear");
            builder.setMessage("All Saved ePapers Deleted");
            AlertDialog dialog = builder.create();
            // display dialog
            dialog.show();
        }


        private boolean deleteDir(File dir) {
            // TODO Auto-generated method stub
            {
                if (dir.isDirectory())
                {
                    // last modified date

                    String[] children = dir.list();
                    for (int i = 0; i < children.length; i++) {
                        boolean success = deleteDir(new File(dir, children[i]));

                        if (!success)
                        {
                            return false;
                        }
                    }
                }
                // The directory is now empty so delete it
                return dir.delete();
            }


        }

        // boolean method for table is exist or not
        public boolean isTableExists(String tableName)
        {

            mDatabase =  getActivity().openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);

            Cursor cursor = mDatabase.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.close();
                    return true;
                }
                cursor.close();
            }
            return false;
        }
    }


    private void deleteEpapers() {
        String path = Environment.getExternalStorageDirectory() + "/" + "NavaMaratha/";

        File directory = new File(path);

        if (directory.exists()) {

            File[] files = directory.listFiles();

            for (int i = 0; i < files.length; i++) {
                files[i].delete();
            }


            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("ePaper Clear");
            builder.setMessage("All Saved ePapers Deleted");
            AlertDialog dialog = builder.create();
            // display dialog
            dialog.show();


        }
    }

}

