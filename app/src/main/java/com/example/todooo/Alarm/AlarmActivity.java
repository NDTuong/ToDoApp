package com.example.todooo.Alarm;


import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.todo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AlarmActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer;
    Button btnClose;
    TextView tvTitle, tvDescriptions;
    String id;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.jump_start);
        mediaPlayer.start();

        tvTitle = findViewById(R.id.tvTitle);
        tvDescriptions = findViewById(R.id.tvDescriptions);

        if(getIntent().getExtras() != null) {
            tvTitle.setText(getIntent().getStringExtra("TITLE"));
            tvDescriptions.setText(getIntent().getStringExtra("DESC"));
//        id = getIntent().getStringExtra("ID");
        }

//        mAuth = FirebaseAuth.getInstance();
//        // Check if user is signed in (non-null) and update UI accordingly.
//        currentUser = mAuth.getCurrentUser();
//        String UID = "";
//        if(currentUser != null){
//            UID = currentUser.getUid();
//        }
//        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("todo_app/" + UID);

        btnClose = (Button) findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> {
//            mDatabase.child("task").child(id).child("reminder").removeValue();
            finish();
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
    }
}