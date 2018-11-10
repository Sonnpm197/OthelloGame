package com.example.son.othellogame.firebase;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.son.othellogame.GamePlayActivity;
import com.example.son.othellogame.LoginActivity;
import com.example.son.othellogame.MainActivity;
import com.example.son.othellogame.RegisterActivity;
import com.example.son.othellogame.ResetPasswordActivity;
import com.example.son.othellogame.adapter.ChessBroadAdapter;
import com.example.son.othellogame.entities.Message;
import com.example.son.othellogame.entities.PlayingPiece;
import com.example.son.othellogame.entities.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FirebaseModel {

    private static final String LOGIN_ACTIVITY_NAME = LoginActivity.class.getSimpleName();
    private static final String REGISTER_ACTIVITY_NAME = RegisterActivity.class.getSimpleName();
    private static final String RESET_PASSWORD_ACTIVITY_NAME = ResetPasswordActivity.class.getSimpleName();
    private static final String MAIN_ACTIVITY_NAME = MainActivity.class.getSimpleName();
    private static final String GAME_PLAY_ACTIVITY = GamePlayActivity.class.getSimpleName();

    private FirebaseUser currentUser;
    private String currentUserName = "";
    private FirebaseAuth firebaseAuth;
    private final String TAG;
    private List<Message> messages; // messages when player quits, send invitation, ect.
    private AppCompatActivity activity;

    public FirebaseModel(AppCompatActivity activity) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseAuth = FirebaseAuth.getInstance();
        TAG = FirebaseModel.class.getSimpleName();
        this.activity = activity;
    }

    private String getExactActivityName() {
        if (activity instanceof LoginActivity) {
            return LOGIN_ACTIVITY_NAME;
        } else if (activity instanceof RegisterActivity) {
            return REGISTER_ACTIVITY_NAME;
        } else if (activity instanceof ResetPasswordActivity) {
            return RESET_PASSWORD_ACTIVITY_NAME;
        } else if (activity instanceof MainActivity) {
            return MAIN_ACTIVITY_NAME;
        } else if (activity instanceof GamePlayActivity) {
            return GAME_PLAY_ACTIVITY;
        }

        return "";
    }

    /**
     * Update current user name to show on tool bar
     *
     * @return
     */
    public void updateCurrentUserName(final TextView userName) {
        DatabaseReference userTable = FirebaseDatabase.getInstance().getReference("Users").child(getCurrentUserId());
        userTable.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                userName.setText("Player: " + ((user == null) ? "" : user.getUserName()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Change status of current user depend on stage of activity
     * onResume -> online -> update to firebase
     * onPause -> offline -> update to firebase
     */
    public void updateCurrentUserStatus(String status) {
        DatabaseReference userTable = FirebaseDatabase.getInstance().getReference("Users").child(getCurrentUserId());
        HashMap<String, Object> params = new HashMap<>();
        params.put("status", status);
        userTable.updateChildren(params);
    }

    /**
     * Get list online friends in real time
     */
    public void getRealTimeOnlineFriends() {

        if (getExactActivityName().equals(MAIN_ACTIVITY_NAME)) {
            final MainActivity mainActivity = (MainActivity) activity;
            if (mainActivity != null) {
                DatabaseReference userTable = FirebaseDatabase.getInstance().getReference("Users");
                userTable.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<User> listFriends = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null && user.getId() != null && !user.getId().equals(getCurrentUserId())) {
                                listFriends.add(user);
                            }
                        }

                        mainActivity.updateNewListFriends(listFriends);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    public String getCurrentUserId() {
        return currentUser.getUid();
    }

    /**
     * Add new account to firebase DB with 3 parameters:
     *
     * @param userName
     * @param email:    must have true email form
     * @param password: cannot less than 6 characters
     */
    public void createUserWithEmailAndPassword(final String userName, String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            DatabaseReference userTable = FirebaseDatabase.getInstance()
                                    .getReference("Users").child(getCurrentUserId());

                            // Initialize user's information
                            HashMap<String, Object> userParams = new HashMap<>();
                            userParams.put("id", getCurrentUserId());
                            userParams.put("userName", userName);
                            userParams.put("status", User.Status.OFFLINE.getValue());

                            // Add value to table Users
                            userTable.setValue(userParams).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.i(TAG, "Add new account successfully");
                                        Toast.makeText(activity, "Add new account successfully", Toast.LENGTH_SHORT).show();
                                        if (getExactActivityName().equals(REGISTER_ACTIVITY_NAME)) {
                                            RegisterActivity registerActivity = (RegisterActivity) activity;
                                            registerActivity.redirectToMainActivity();
                                        }
                                    } else {
                                        String message = task.getException() == null ? "Add new account to Users erroneously" : task.getException().getMessage();
                                        Log.e(TAG, message);
                                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            String message = task.getException() == null ? "Cannot create new user" : task.getException().getMessage();
                            Log.e(TAG, message);
                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, e.getMessage());
                Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * reset password for registered email
     *
     * @param email
     * @return
     */
    public void sendPasswordResetEmail(final String email) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(activity, "Please check your email: " + email + " to update password", Toast.LENGTH_SHORT).show();
                            if (getExactActivityName().equals(RESET_PASSWORD_ACTIVITY_NAME)) {
                                ResetPasswordActivity resetPasswordActivity = (ResetPasswordActivity) activity;
                                resetPasswordActivity.redirectToLoginActivity();
                            }
                        } else {
                            Toast.makeText(activity, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, e.getMessage());
                Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Login using email and password
     *
     * @param email
     * @param password
     * @return
     */
    public void signInWithEmailAndPassword(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "Sign In successfully");
                            Toast.makeText(activity, "Sign In successfully", Toast.LENGTH_SHORT).show();
                            if (getExactActivityName().equals(LOGIN_ACTIVITY_NAME)) {
                                LoginActivity loginActivity = (LoginActivity) activity;
                                loginActivity.redirectToMainActivity();
                            }
                        } else {
                            String message = task.getException() == null ? "Sign In failed" : task.getException().getMessage();
                            Log.e(TAG, message);
                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, e.getMessage());
                Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sign out
     */
    public void signOut() {
        firebaseAuth.signOut();
        Toast.makeText(activity, "Sign out successfully", Toast.LENGTH_SHORT).show();
    }

    /**
     * Send piece's location to each other while playing
     * ==> hierarchy:
     * Games
     *     Match number (random number then remove later)
     *          senderId
     *              (obj 1)
     *                  pieceColor
     *                  location
     *
     * @param senderId
     * @param location
     */
    public void sendPieceLocation(String senderId, int location, String pieceColor, int matchNumber) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Games").child(String.valueOf(matchNumber));

        HashMap<String, Object> params = new HashMap<>();
        params.put("location", location);
        params.put("pieceColor", pieceColor);

        // Send message to other player
        databaseReference.child(senderId).push().setValue(params);
        Log.i(TAG, "Send: " + " location: " + location + "; color: " + pieceColor);
    }

    /**
     * Receive piece's location from friend while playing
     *
     * @return friendId
     */
    public void receivePieceLocation(String friendId, final int matchNumber) {

        if (getExactActivityName().equals(GAME_PLAY_ACTIVITY)) {
            final GamePlayActivity gamePlayActivity = (GamePlayActivity) activity;

            if (gamePlayActivity != null) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Games")
                        .child(String.valueOf(matchNumber)).child(friendId); // query data from sender(fiend) to get sent piece from he/she
                databaseReference.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        PlayingPiece receivedPiece = dataSnapshot.getValue(PlayingPiece.class);
                        Log.i(TAG, "Receive: " + receivedPiece.toString() + " ; match number: " + matchNumber);

                        if (!gamePlayActivity.getYourColor().equals(receivedPiece.getPieceColor())) {
                            gamePlayActivity.updateBoard(receivedPiece.getLocation(), receivedPiece.getPieceColor(), false);
                            Log.i(TAG, "Receive different color: " + receivedPiece.getPieceColor() + " -> update to chess board");
                        }

                        dataSnapshot.getRef().removeValue();
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
     * @param messageType
     */
    public void sendMessage(String senderName, String senderId, String receiverId, String messageType, Integer matchNumber) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Messages").child(receiverId);

        HashMap<String, Object> params = new HashMap<>();
        params.put("senderName", senderName);
        params.put("senderId", senderId);
        params.put("receiverId", receiverId);
        params.put("messageType", messageType);
        params.put("matchNumber", matchNumber);

        // Send message to other player
        databaseReference.push().setValue(params);
    }

    /**
     * Receive message from other user
     * This method will be used in 2 cases:
     *      1. Handle invitation
     *      2. Handle lost turn, quit (GamePlayActivity)
     * @param receiverId
     */
    public void receiveMessage(String receiverId) {

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Messages").child(receiverId);

        if (getExactActivityName().equals(MAIN_ACTIVITY_NAME)) {
            final MainActivity mainActivity = (MainActivity) activity;
            databaseReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Message message = dataSnapshot.getValue(Message.class);
                    mainActivity.handleMessage(message);
                    // Remove immediately to prevent next time this method is invoked and read all the old messages
                    dataSnapshot.getRef().removeValue();
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
        } else if (getExactActivityName().equals(GAME_PLAY_ACTIVITY)) {
            final GamePlayActivity gamePlayActivity = (GamePlayActivity) activity;
            databaseReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Message message = dataSnapshot.getValue(Message.class);
                    gamePlayActivity.handleMessage(message);
                    // Remove immediately to prevent next time this method is invoked and read all the old messages
                    dataSnapshot.getRef().removeValue();
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
}
