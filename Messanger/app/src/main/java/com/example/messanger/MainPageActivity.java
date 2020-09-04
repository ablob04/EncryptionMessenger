package com.example.messanger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainPageActivity extends AppCompatActivity {
    private List<UserObject> userObjects;
    private Button chatCreate;
    private RecyclerView rv;
    private RecyclerViewAdapter adapter;
    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OneSignal.startInit(this).init();
        setContentView(R.layout.activity_main_page);
        userObjects = new ArrayList<>();
        chatCreate = findViewById(R.id.createChat);
        rv = findViewById(R.id.List);
        rv.setNestedScrollingEnabled(true);
        rv.setHasFixedSize(true);
        adapter = new RecyclerViewAdapter( userObjects);
        rv.setAdapter(adapter);
        OneSignal.setSubscription(true);
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                FirebaseDatabase.getInstance().getReference().child("user").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("notificationKey").setValue(userId);
            }
        });
        OneSignal.setInFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification);
        rv.setLayoutManager(new LinearLayoutManager(this));
        
        chatCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateChat();
            }
        });

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user");
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot snapshot1: snapshot.getChildren()){
                        if(!(firebaseUser.getUid().equals(snapshot1.getKey()))){
                            String name = "none";
                            String email = "none";
                            if(snapshot1.child("email").exists())
                                email = snapshot1.child("email").getValue().toString();
                            String key = snapshot1.getKey();
                            if(snapshot1.child("Name").exists())
                                name = snapshot1.child("Name").getValue().toString();
                            UserObject userObject = new UserObject(email, key, name);
                            userObjects.add(userObject);
                            adapter  = new RecyclerViewAdapter(userObjects);
                        }
                    }
                    rv.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void CreateChat() {
        final String key = FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();// push method creates a key
        int usersChosen = 0;
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("chat").child(key);
        for(UserObject object: userObjects){
            if(object.getSelected()){
                FirebaseDatabase.getInstance().getReference().child("user").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("chat").child(key).setValue(true);
                FirebaseDatabase.getInstance().getReference().child("user").child(object.getUid()).child("chat").child(key).setValue(true);
            }
        }
        FirebaseDatabase.getInstance().getReference().child("chat").child(key).setValue(true);
        FirebaseDatabase.getInstance().getReference().child("chat").child(key).child("users").child(FirebaseAuth.getInstance().getUid());


        for (UserObject object: userObjects){
            if (object.getSelected()){
                FirebaseDatabase.getInstance().getReference().child("chat").child(key).child("users").child(object.getUid()).setValue(true);
            }
        }
        FirebaseDatabase.getInstance().getReference().child("chat").child(key).child("users").child(FirebaseAuth.getInstance().getUid()).setValue(true);
        for(UserObject object: userObjects){
            if(object.getSelected()){
                usersChosen += 1;
            }
        }
        if(usersChosen == 1){
            //will not make user enter the name
            String userID = "";

            for (UserObject object: userObjects){
                if(object.getSelected()){
                    if(!object.getUid().equals(FirebaseAuth.getInstance().getUid())){
                        userID = object.getUid();
                    }
                }
            }
            DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference().child("user").child(userID);
            databaseReference1.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = "";
                    if(snapshot.child("Name").exists()){
                        name = snapshot.child("Name").getValue().toString();
                    }
                    final DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference().child("chat").child(key);
                    final String finalName = name;
                    databaseReference2.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Map<String, Object> addName = new HashMap<>();
                            addName.put("Group Name", finalName);
                            databaseReference2.updateChildren(addName);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            startActivity(new Intent(MainPageActivity.this, ChatListActivity.class) ) ;
        }
        else if(usersChosen > 1){
            //will show pop up for the user to enter the name
            Context context = this;
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View popup = inflater.inflate(R.layout.pop_up_for_grpname, null);
            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
            int hieght = LinearLayout.LayoutParams.WRAP_CONTENT;
            boolean focusable = true;
            final PopupWindow popupWindow = new PopupWindow(popup, width, hieght, focusable);

            popupWindow.showAtLocation(popup, Gravity.CENTER, 0, 0);
            Button enterGrpName = popup.findViewById(R.id.enterName);
            final EditText grpName = popup.findViewById(R.id.GrpName);
            enterGrpName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference().child("chat").child(key);
                    databaseReference1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String groupName = grpName.getText().toString();
                            Map<String, Object> addGrpName = new HashMap<>();
                            addGrpName.put("Group Name", groupName);
                            databaseReference1.updateChildren(addGrpName);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    startActivity(new Intent(MainPageActivity.this, ChatListActivity.class));
                }
            });

            popup.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popupWindow.dismiss();
                    return true;
                }
            });

        }

    }
}
