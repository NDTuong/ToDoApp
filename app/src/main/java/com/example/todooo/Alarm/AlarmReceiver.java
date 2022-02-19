package com.example.todooo.Alarm;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.todooo.MainActivity;
import com.example.todo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class AlarmReceiver extends BroadcastReceiver {
    private int typeAlarm;

    private final int ALARM = 1;
    private final int NOTIFICATIONS = 0;


    String title, description, id;
    int notificationID;

    @Override
    public void onReceive(Context context, Intent intent) {

//        // [CHECK ĐĂNG NHẬP CHƯA?]
//        FirebaseAuth mAuth = FirebaseAuth.getInstance();
//        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        String UID = "";
//        if(currentUser != null){
//            UID = currentUser.getUid();
//        }
//        // [KẾT THÚC CHECK ĐĂNG NHẬP CHƯA?]
//        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("todo_app/" + UID);

        title = intent.getStringExtra("TITLE");
        description = intent.getStringExtra("DESC");
        typeAlarm = intent.getIntExtra("TYPE", 0);
        notificationID = intent.getIntExtra("CODE", 0);
//        id  = intent.getStringExtra("ID");


        if(typeAlarm == NOTIFICATIONS){
            Intent i = new Intent(context, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context,notificationID,i,0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"todo_app_notifications")
                    .setSmallIcon(R.drawable.logo_app)
                    .setContentTitle(title)
                    .setContentText(description)
                    .setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setGroup("TODO")
                    .setContentIntent(pendingIntent);

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            notificationManagerCompat.notify(notificationID, builder.build());

        }
        if(typeAlarm == ALARM){
            Intent i = new Intent(context, AlarmActivity.class);
            i.putExtra("TITLE", title);
            i.putExtra("DESC", description);
            i.putExtra("ID", id);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
//        mDatabase.child("notificationsList").child(id).removeValue();
    }
    private int getNotificationsID(){
        return (int) new Date().getTime();
    }


}
