package com.example.son.othellogame;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.example.son.othellogame.adapter.UserAdapter;
import com.example.son.othellogame.entities.Message;
import com.example.son.othellogame.entities.User;
import com.example.son.othellogame.firebase.FirebaseModel;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Random;

import static com.example.son.othellogame.entities.User.Status.OFFLINE;
import static com.example.son.othellogame.entities.User.Status.ONLINE;

public class MainActivity extends AppCompatActivity implements UserAdapter.InviteInterface {

    private Toolbar toolbar;
    private TextView userName;
    private RecyclerView recyclerViewUsers;
    private FirebaseModel firebaseModel = new FirebaseModel(this);

    @Override
    protected void onResume() {
        super.onResume();
        firebaseModel.updateCurrentUserStatus(ONLINE.getValue());
    }

    @Override
    protected void onPause() {
        super.onPause();
        firebaseModel.updateCurrentUserStatus(OFFLINE.getValue());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userName = (TextView) findViewById(R.id.userName);
        firebaseModel.updateCurrentUserName(userName);

        toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        recyclerViewUsers = (RecyclerView) findViewById(R.id.recyclerViewUsers);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));

        // Update list friends
        firebaseModel.getRealTimeOnlineFriends();

        // Receive messages from other users (invitation, opponent quits in the middle game, ...) by yourId as root of branch
        // E.g.
        //  yourId
        //      friendMessage1
        //      friendMessage2
        firebaseModel.receiveMessage(firebaseModel.getCurrentUserId());
    }

    public void updateNewListFriends(List<User> users) {
        UserAdapter userAdapter = new UserAdapter(this, this, users);
        recyclerViewUsers.setAdapter(userAdapter);
    }

    // Handle invitation from UserAdapter when click on invite button
    @Override
    public void inviteFriend(String friendId, String friendStatus) {
        // Send invitation to friend here
        if (friendStatus.equals(User.Status.ONLINE.getValue())) {
            firebaseModel.sendMessage(userName.getText().toString(), firebaseModel.getCurrentUserId(), friendId, Message.Type.INVITE.getValue(), null);
        } else {
            Toast.makeText(this, "This friend is offline. Please find someone online", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle received message from friend
     * This function will be executed in firebaseModel.receiveMessage
     *
     * @param message
     */
    public void handleMessage(final Message message) {
        AlertDialog alertDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // create random match number so 2 players can exchange their data within the branch title match number
        final int randomMatchNumber = new Random().nextInt(100000);

        if (message != null) {
            String messageType = message.getMessageType();
            if (messageType.equals(Message.Type.INVITE.getValue())) {
                // If you got an invitation
                builder.setMessage(message.getSenderName() + " invited you to play a game.")
                        .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Swap to reply message to sender
                                firebaseModel.sendMessage(userName.getText().toString(), firebaseModel.getCurrentUserId()
                                        , message.getSenderId(), Message.Type.ACCEPT.getValue(), randomMatchNumber);

                                Intent intent = new Intent(MainActivity.this, GamePlayActivity.class);
                                intent.putExtra("userName", userName.getText().toString());
                                intent.putExtra("invite", false);
                                intent.putExtra("friendId", message.getSenderId()); // this param to query only data from your friend
                                intent.putExtra("matchNumber", randomMatchNumber); // swap to send back to whom sent you an invitation
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                firebaseModel.sendMessage(userName.getText().toString(), firebaseModel.getCurrentUserId()
                                        , message.getSenderId(), Message.Type.DENY.getValue(), null);
                            }
                        });
            } else if (messageType.equals(Message.Type.QUIT.getValue())) {

            } else if (messageType.equals(Message.Type.DENY.getValue())) {
                // If you invited and your opponent denied your request
                builder.setMessage("Your opponent does not want to play with you.")
                        .setPositiveButton("OK", null);

            } else if (messageType.equals(Message.Type.ACCEPT.getValue())) {
                // If you invited and your opponent accepted
                Intent intent = new Intent(MainActivity.this, GamePlayActivity.class);
                intent.putExtra("userName", userName.getText().toString());
                intent.putExtra("invite", true);
                intent.putExtra("friendId", message.getSenderId()); // this param to query only data from your friend
                intent.putExtra("matchNumber", message.getMatchNumber());
                startActivity(intent);
            }

            alertDialog = builder.create();
            alertDialog.show();
        }
    }


}
