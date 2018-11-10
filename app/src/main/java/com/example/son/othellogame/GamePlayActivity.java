package com.example.son.othellogame;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.son.othellogame.adapter.ChessBroadAdapter;
import com.example.son.othellogame.entities.ChessPiece;
import com.example.son.othellogame.entities.Message;
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
    }

    @Override
    public void onBackPressed() {
        // TODO: ask if they really want to exit
    }

    /**
     * Show both player score
     * @param score
     */
    public void updateScore(String score) {
        this.score.setText("Score: " + score);
    }

    /**
     * Show current turn (black or white)
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
    public void sendResultMessageFromOthelloLogic(String messageType) {
        // case when both boards are full then go back to MainActivity
        Intent parentIntent = new Intent(this, MainActivity.class);
        if (messageType.equals(Message.Type.GAME_OVER_BY_FULL_BOARD.getValue())
                || messageType.equals(Message.Type.GAME_OVER_BY_NO_MOVES_BOTH_PLAYERS.getValue())) {
            startActivity(parentIntent);
            return;
        }

        // Handle case when lost turn
        firebaseModel.sendMessage(userName.getText().toString(), firebaseModel.getCurrentUserId(), friendId, messageType, null);
    }

    /**
     * Handle message received from friends (use in firebaseModel)
     * @param message
     */
    public void handleMessage(Message message) {
        // Your opponent lost turn
        if (message.getMessageType().equals(Message.Type.LOST_TURN.getValue())) {
            chessBroadAdapter.getOthelloLogic().setOpponentLostTurn(true);
            Toast.makeText(this, "You get a turn, your opponent has no other moves", Toast.LENGTH_SHORT).show();
        }
    }
}
