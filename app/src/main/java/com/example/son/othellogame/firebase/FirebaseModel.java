package com.example.son.othellogame.firebase;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.son.othellogame.adapter.ChessBroadAdapter;
import com.example.son.othellogame.entities.Message;
import com.example.son.othellogame.entities.PlayingPiece;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.son.othellogame.entities.Player.Status.OFFLINE;

public class FirebaseModel {

    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;
    private final String TAG;
    private boolean registerStatus = false;
    private boolean signIn = false;
    private List<Message> messages; // messages when player quits, send invitation, ect.
    private Context context;

    public FirebaseModel(Context context) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseAuth = FirebaseAuth.getInstance();
        TAG = FirebaseModel.class.getSimpleName();
        this.context = context;
    }

    public FirebaseUser getCurrentUser() {
        return currentUser;
    }

    public String getUserId() {
        return currentUser.getUid();
    }

    /**
     * Add new account to firebase DB with 3 parameters:
     *
     * @param userName
     * @param email: must have true email form
     * @param password: cannot less than 6 characters
     */
    public boolean registerNewAccount(final String userName, String email, String password) {
        registerStatus = false; // reset status when new user registers
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            DatabaseReference userTable = FirebaseDatabase.getInstance()
                                    .getReference("Users").child(getUserId());

                            // Initialize user's information
                            HashMap<String, Object> userParams = new HashMap<>();
                            userParams.put("id", getUserId());
                            userParams.put("name", userName);
                            userParams.put("status", OFFLINE.getValue());

                            // Add value to table Users
                            userTable.setValue(userParams).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.i(TAG, "Add new account successfully");
                                        registerStatus = true;
                                        Toast.makeText(context, "Add new account successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        String message = task.getException() == null ? "Add new account to Users erroneously" : task.getException().getMessage();
                                        Log.e(TAG, message);
                                        registerStatus = false;
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            String message = task.getException() == null ? "Cannot create new user" : task.getException().getMessage();
                            Log.e(TAG, message);
                            registerStatus = false;
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        return registerStatus;
    }

    /**
     * Login using email and password
     *
     * @param email
     * @param password
     * @return
     */
    public boolean signIn(String email, String password) {
        signIn = false;
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "Sign In successfully");
                            signIn = true;
                            Toast.makeText(context, "Sign In successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            String message = task.getException() == null ? "Sign In failed" : task.getException().getMessage();
                            Log.e(TAG, message);
                            signIn = false;
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        return signIn;
    }

    /**
     * Sign out
     */
    public void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    /**
     * Send piece's location to each other while playing
     * ==> hierarchy:
     * Games
     *     Match number (random number then remove later)
     *          senderId
     *              (obj 1)
     *                  receiverId
     *                  pieceColor
     *                  pieceLocation
     *
     * @param senderId
     * @param receiverId
     * @param location
     */
    public void sendPieceLocation(String senderId, String receiverId, String location, String pieceColor, String matchNumber) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Games").child(matchNumber);

        HashMap<String, Object> params = new HashMap<>();
        params.put("receiverId", receiverId);
        params.put("location", location);
        params.put("pieceColor", pieceColor);

        // Send message to other player
        databaseReference.child(senderId).push().setValue(params);
    }

    /**
     * Receive piece's location from friend while playing
     *
     * @return friendId
     */
    public void receivePieceLocation(String friendId, String matchNumber, ChessBroadAdapter chessBroadAdapter) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Games").child(matchNumber).child(friendId);
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                PlayingPiece receivedPiece = dataSnapshot.getValue(PlayingPiece.class);
                Log.i(TAG, receivedPiece.toString());
                // Toast.makeText(context, receivedPiece.toString(), Toast.LENGTH_SHORT).show();

                // TODO: update chessBroadAdapter after receiving data
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Send invitation, send notification when 1 user is disconnected or they quit
     * ==>> Hierarchy:
     *      Messages
     *          (obj 1)
     *              senderId
     *              receiverId
     *              message
     *              messageType
     * @param senderId
     * @param receiverId
     * @param message
     * @param messageType
     */
    public void sendMessage(String senderId, String receiverId, String message, String messageType) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Messages");

        HashMap<String, Object> params = new HashMap<>();
        params.put("senderId", senderId);
        params.put("receiverId", receiverId);
        params.put("message", message);
        params.put("messageType", messageType);

        // Send message to other player
        databaseReference.push().setValue(params);
    }

    public void receiveMessage(String friendId, ChessBroadAdapter chessBroadAdapter) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Messages");
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);
                Log.i(TAG, message.toString());
                // TODO: perform action when retrieve data
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
