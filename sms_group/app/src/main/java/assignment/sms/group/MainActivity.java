package assignment.sms.group;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import assignment.sms.group.adapters.GroupedSmsAdapter;
import assignment.sms.group.models.MySms;
import assignment.sms.group.receivers.SmsReceiver;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String SMS_READ_PERMISSION = "android.permission.READ_SMS";
    private static final int RC_PERMISSION_READ_SMS = 11;

    private String[] permissions = new String[]{SMS_READ_PERMISSION};

    private List<MySms> mSmsList;
    private List<ListItem> mConsolidatedList;

    private RecyclerView mRecyclerViewGroupedSms;
    private GroupedSmsAdapter mGroupedSmsAdapter;

    private BroadcastReceiver mBroadcastReceiver;

    private boolean mIsNotificationClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerViewGroupedSms = findViewById(R.id.recycler_view_sms);

        mSmsList = new ArrayList<>();
        mConsolidatedList = new ArrayList<>();

        Intent intent = getIntent();
        if (intent != null){
            mIsNotificationClick = intent.getBooleanExtra("notification_click", false);
        }

        mGroupedSmsAdapter = new GroupedSmsAdapter(mConsolidatedList, mIsNotificationClick);
        mRecyclerViewGroupedSms.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        mRecyclerViewGroupedSms.setAdapter(mGroupedSmsAdapter);
        mRecyclerViewGroupedSms.setHasFixedSize(true);

        checkSmsReadPermission();

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction() != null && intent.getAction().equals(SmsReceiver.NEW_SMS)){

                    String address = intent.getStringExtra("address");
                    String body = intent.getStringExtra("body");

                    long currentTime = System.currentTimeMillis();

                    MySms mySms = new MySms(address, body, currentTime);
                    mSmsList.add(0, mySms);

                    groupSms(currentTime);

                    mGroupedSmsAdapter.updateNew(true);
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(SmsReceiver.NEW_SMS));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RC_PERMISSION_READ_SMS){

            if (grantResults[0] == -1){
                Toast.makeText(this, "Sms read permission required to proceed.", Toast.LENGTH_SHORT).show();
            } else {
                getSms();
            }
        }
    }

    private void checkSmsReadPermission(){
        if (ContextCompat.checkSelfPermission(this, SMS_READ_PERMISSION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, permissions, RC_PERMISSION_READ_SMS);
            return;
        }

        Log.i(TAG, "checkSmsReadPermission: granted");

        getSms();
    }

    private void getSms(){

        mSmsList.clear();

        ContentResolver contentResolver = getContentResolver();

        String[] projection = new String[]{
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE
        };

        @SuppressLint("Recycle")
        Cursor smsCursor = contentResolver.query(Telephony.Sms.CONTENT_URI, projection,
                null, null, Telephony.Sms.DEFAULT_SORT_ORDER);

        if (smsCursor != null){

            int indexAddress = smsCursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS);
            int indexBody = smsCursor.getColumnIndexOrThrow(Telephony.Sms.BODY);
            int indexDate = smsCursor.getColumnIndexOrThrow(Telephony.Sms.DATE);

            if (indexBody < 0 || !smsCursor.moveToFirst()) {
                return;
            }

            long currentTime = System.currentTimeMillis();
            long startTime = currentTime - 24 * 3600 * 1000;

            do {

                long currentSmsTimestamp = smsCursor.getLong(indexDate);

                if (currentSmsTimestamp >= startTime){

                    /*int id = smsCursor.getInt(indexId);*/
                    String address = smsCursor.getString(indexAddress);
                    String body = smsCursor.getString(indexBody);

                    MySms mySms = new MySms(address, body, currentSmsTimestamp);
                    mSmsList.add(mySms);
                }

            } while (smsCursor.moveToNext());

            groupSms(currentTime);

        }
    }

    private void groupSms(long currentTime){

        LinkedHashMap<String, List<MySms>> groupedHashmap = groupData(mSmsList, currentTime);

        mConsolidatedList.clear();

        for (String header : groupedHashmap.keySet()){

            HeaderItem headerItem = new HeaderItem();
            headerItem.setHeader(header);
            mConsolidatedList.add(headerItem);

            Log.i(TAG, "getSms: " + header);

            for (MySms mySms : groupedHashmap.get(header)){

                SmsItem smsItem = new SmsItem();
                smsItem.setMySms(mySms);
                mConsolidatedList.add(smsItem);

                Log.i(TAG, "getSms: " + mySms.getTimestamp());
            }
        }

        mGroupedSmsAdapter.notifyDataSetChanged();
    }

    private LinkedHashMap<String, List<MySms>> groupData(List<MySms> listSms, long currentTime){

        LinkedHashMap<String, List<MySms>> groupedHashmap = new LinkedHashMap<>();

        for (MySms mySms : listSms){

            long timestamp = mySms.getTimestamp();

            String hashmapKey = DateUtils.getRelativeTimeSpanString(timestamp, currentTime,
                    DateUtils.HOUR_IN_MILLIS).toString();

            if (groupedHashmap.containsKey(hashmapKey)){
                groupedHashmap.get(hashmapKey).add(mySms);
            } else {
                List<MySms> list = new ArrayList<>();
                list.add(mySms);
                groupedHashmap.put(hashmapKey, list);
            }
        }

        return groupedHashmap;
    }

    public abstract class ListItem {
        public static final int TYPE_HEADER = 1;
        public static final int TYPE_SMS = 2;

        abstract public int getType();
    }

    public class HeaderItem extends ListItem {

        private String header;

        public String getHeader() {
            return header;
        }

        public void setHeader(String header) {
            this.header = header;
        }

        @Override
        public int getType() {
            return TYPE_HEADER;
        }
    }

    public class SmsItem extends ListItem {

        private MySms mySms;

        public MySms getMySms() {
            return mySms;
        }

        public void setMySms(MySms mySms) {
            this.mySms = mySms;
        }

        @Override
        public int getType() {
            return TYPE_SMS;
        }
    }
}
