package assignment.sms.group.receivers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsMessage;
import android.util.Log;

import assignment.sms.group.MainActivity;
import assignment.sms.group.R;
import assignment.sms.group.utils.NotificationUtils;

import static android.content.Context.NOTIFICATION_SERVICE;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";

    private static final int NOTIFICATION_ID = 100;
    private static final String NOTIFICATION_CHANNEL_ID = "id_sms_receive";
    public static final String NEW_SMS = "new_sms";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG, "onReceive: new sms");

        if (intent.getAction() != null
                && intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {

            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[])bundle.get("pdus");

                final SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                }

                String address = messages[0].getOriginatingAddress();
                String body =  messages[0].getMessageBody();

                if (NotificationUtils.isAppIsInBackground(context)) {
                    showNotification(context, address, body);
                } else {
                    Intent pushNotification = new Intent(NEW_SMS);
                    pushNotification.putExtra("address", address);
                    pushNotification.putExtra("body", body);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(pushNotification);
                }
            }
        }
    }


    private void showNotification(Context context, String address, String body){

        createNotificationChannel(context);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

        Intent myIntent = new Intent(context.getApplicationContext(), MainActivity.class);
        myIntent.putExtra("notification_click", true);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                myIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = mBuilder
                .setTicker(address)
                .setContentTitle(address)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setStyle(new NotificationCompat.InboxStyle().addLine(body))
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null){
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    private void createNotificationChannel(Context context){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "General", importance);
            mChannel.setDescription("Random");
            mChannel.enableLights(true);
            mChannel.canShowBadge();
            mChannel.setLightColor(Color.RED);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(mChannel);
            }

        }
    }
}
