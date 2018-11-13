package com.example.son.othellogame;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.son.othellogame.adapter.ChessBroadAdapter;
import com.example.son.othellogame.entities.ChessPiece;
import com.example.son.othellogame.entities.Message;
import com.example.son.othellogame.entities.User;
import com.example.son.othellogame.firebase.FirebaseModel;

import static com.example.son.othellogame.entities.ChessPiece.PieceColor.BLACK;
import static com.example.son.othellogame.entities.ChessPiece.PieceColor.WHITE;

public class GamePlayActivity extends AppCompatActivity implements ChessBroadAdapter.HandleSendAndReceiveMessage {

    private TextView userName, color, score, currentTurn;
    private ChessBroadAdapter chessBroadAdapter;
    private FirebaseModel firebaseModel;
    private String yourColor, opponentColor;
    private String friendId;

    @Override
    protected void onResume() {
        super.onResume();
        firebaseModel.updateCurrentUserStatus(User.Status.PLAYING.getValue());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play);

        userName = (TextView) findViewById(R.id.userName);
        color = (TextView) findViewById(R.id.color);
        score = (TextView) findViewById(R.id.score);
        currentTurn = (TextView) findViewById(R.id.currentTurn);

        // Set user name from called intent
        Bundle bundle = getIntent().getExtras();
        String userNameString = bundle.getString("userName");
        boolean invite = bundle.getBoolean("invite");
        friendId = bundle.getString("friendId");
        int matchNumber = bundle.getInt("matchNumber");
        yourColor = ((invite) ? BLACK.getValue() : WHITE.getValue());
        opponentColor = yourColor.equals(BLACK.getValue()) ? WHITE.getValue() : BLACK.getValue();

        userName.setText(userNameString);
        color.setText("Color: " + yourColor);

        firebaseModel = new FirebaseModel(this);
        RecyclerView chessBroad = (RecyclerView) findViewById(R.id.chessBroad);
        chessBroadAdapter = new ChessBroadAdapter(this, yourColor, firebaseModel.getCurrentUserId(), friendId, matchNumber);
        chessBroad.setLayoutManager(new GridLayoutManager(this, 8));
        chessBroad.setAdapter(chessBroadAdapter);

        firebaseModel.receiveMessage(firebaseModel.getCurrentUserId());
    }

    /**
     * Show both player score on GamePlayActivity
     * @param scores
     */
    public void updateScore(int[] scores) {
        this.score.setText("Score: Black: " + scores[0] + " / White: " + scores[1]);
    }

    /**
     * Show current turn (black or white) on GamePlayActivity
     * @param yourTurn
     */
    public void updateCurrentTurn(boolean yourTurn) {
        String turn = yourTurn ? yourColor : opponentColor;
        this.currentTurn.setText("Turn: " + turn);
    }

    /**
     * If your opponent sends a piece object then you will query data from:
     * matchNumber
     *      your opponent Id
     *          sentObject1
     * @param receiverId
     * @param matchNumber
     */
    @Override
    public void receivePieceLocation(String receiverId, int matchNumber) {
        firebaseModel.receivePieceLocation(receiverId, matchNumber);
    }

    @Override
    public void sendPieceLocation(String senderId, int location, String pieceColor, int matchNumber) {
        firebaseModel.sendPieceLocation(firebaseModel.getCurrentUserId(), location, pieceColor, matchNumber);
    }

    /**
     * This method will be called in firebaseModel.receivePieceLocation
     * @param clickedPosition
     * @param color
     * @param hitOnBoard
     */
    public void updateBoard(int clickedPosition, String color, boolean hitOnBoard) {
        chessBroadAdapter.updateBoard(clickedPosition, color, hitOnBoard);
    }

    public String getYourColor() {
        return yourColor;
    }

    // Send message when lost turn/ full board
    public void handleResultMessageFromOthelloLogic(String messageType) {
        // case when both boards are full then go back to MainActivity
        if (messageType.equals(Message.Type.GAME_OVER_BY_FULL_BOARD.getValue())
                || messageType.equals(Message.Type.GAME_OVER_BY_NO_MOVES_BOTH_PLAYERS.getValue())) {

            Toast.makeText(this, messageType, Toast.LENGTH_SHORT).show();

            // Calculate final scores and show result
            int [] scores = chessBroadAdapter.getOthelloLogic().calculateBlackAndWhitePieces();
            int blackPieces = scores[0];
            int whitePieces = scores[1];

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            if (yourColor.equals(BLACK.getValue()) && blackPieces > whitePieces) {
                builder.setMessage("You win with black color. Result: black: " + blackPieces + " / white: " + whitePieces);
            } else if (yourColor.equals(BLACK.getValue()) && blackPieces < whitePieces){
                builder.setMessage("You lost with black color. Result: black:" + blackPieces + " / white: " + whitePieces);
            } else if (yourColor.equals(WHITE.getValue()) && whitePieces > blackPieces) {
                builder.setMessage("You win with white color. Result: white:" + whitePieces + " / black: " + blackPieces);
            } else if (yourColor.equals(WHITE.getValue()) && whitePieces < blackPieces){
                builder.setMessage("You lost with white color. Result: white:" + whitePieces + " / black: " + blackPieces);
            } else if (whitePieces == blackPieces) {
                builder.setMessage("Draw. Result: white:" + whitePieces + " / black: " + blackPieces);
            }

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent parentIntent = new Intent(GamePlayActivity.this, MainActivity.class);
                    startActivity(parentIntent);
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            return;
        }

        // Handle case when opponent lost turn => send notify to opponent
        firebaseModel.sendMessage(userName.getText().toString(), firebaseModel.getCurrentUserId(), friendId, messageType, null);
    }

    /**
     * Handle message received from friends (use in firebaseModel)
     * @param message
     */
    public void handleMessage(Message message) {
        // Your will lost turn if you receive this message
        if (message.getMessageType().equals(Message.Type.OPPONENT_LOST_TURN.getValue())) {
            chessBroadAdapter.getOthelloLogic().setYouLostTurn(true);
            chessBroadAdapter.getOthelloLogic().setYourTurn(false);
            updateCurrentTurn(false);
            Toast.makeText(this, "You lost your turn because you have no more moves.", Toast.LENGTH_LONG).show();
            Log.i(GamePlayActivity.class.getSimpleName(), "You lost your turn because you have no more moves.");
        } else if (message.getMessageType().equals(Message.Type.QUIT.getValue())) {
            Toast.makeText(this, "Your opponent has quit. You win", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(GamePlayActivity.this, MainActivity.class));
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to go back to main screen ?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        firebaseModel.sendMessage(userName.getText().toString(),
                                firebaseModel.getCurrentUserId(), friendId, Message.Type.QUIT.getValue(), null);
                        startActivity(new Intent(GamePlayActivity.this, MainActivity.class));
                    }
                }).setNegativeButton("Cancel", null);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
