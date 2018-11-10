package com.example.son.othellogame.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.son.othellogame.GamePlayActivity;
import com.example.son.othellogame.R;
import com.example.son.othellogame.entities.ChessPiece;
import com.example.son.othellogame.firebase.FirebaseModel;
import com.example.son.othellogame.logic.OthelloLogic;

import static com.example.son.othellogame.entities.ChessPiece.PieceColor.BLACK;
import static com.example.son.othellogame.entities.ChessPiece.PieceColor.WHITE;

import java.util.List;

public class ChessBroadAdapter extends RecyclerView.Adapter<ChessBroadAdapter.ViewHolder> {

    private List<ChessPiece> listChess;
    private GamePlayActivity gamePlayActivity;
    private boolean firstTime = true; // first time user clicks on a broad
    private OthelloLogic othelloLogic;
    private String yourId, friendId;
    private int matchNumber;
    private String yourColor;

    /**
     * GamePlayActivity will handle send and receive chess piece
     */
    public interface HandleSendAndReceiveMessage {
        void receivePieceLocation(String receiverId, int matchNumber);
        void sendPieceLocation(String senderId, int location, String pieceColor, int matchNumber);
    }

    public ChessBroadAdapter(GamePlayActivity gamePlayActivity, String yourColor, String yourId, String friendId, int matchNumber) {
        this.gamePlayActivity = gamePlayActivity;
        this.yourColor = yourColor;
        this.yourId = yourId;
        this.friendId = friendId;
        this.matchNumber = matchNumber;
        othelloLogic = new OthelloLogic(gamePlayActivity, yourColor);
        listChess = othelloLogic.createBroad();

        gamePlayActivity.receivePieceLocation(friendId, matchNumber);
    }

    @NonNull
    @Override
    public ChessBroadAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // return each chess
        View chess = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_chess_layout, parent, false);
        return new ViewHolder(chess);
    }

    @Override
    public void onBindViewHolder(@NonNull ChessBroadAdapter.ViewHolder holder, int position) {
        ChessPiece piece = listChess.get(position);
        holder.chess.setText("" + (position % OthelloLogic.COLUMN_QUANTITY) + "," + (position / OthelloLogic.COLUMN_QUANTITY) + "\n" + position);
        holder.chess.setTextColor(Color.RED);
        // this holder is a piece
        if (piece.getColor() != null) {
            if (piece.getColor().equals(BLACK.getValue())) {
                holder.chess.setBackground(gamePlayActivity.getResources().getDrawable(R.drawable.black_piece));
            } else if (piece.getColor().equals(WHITE.getValue())) {
                holder.chess.setBackground(gamePlayActivity.getResources().getDrawable(R.drawable.white_piece));
            }
        }
    }

    @Override
    public int getItemCount() {
        return listChess.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private Button chess;

        public ViewHolder(View itemView) {
            super(itemView);
            chess = (Button) itemView.findViewById(R.id.chess);
            chess.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String result = updateBoard(getPosition(), yourColor, true);
                    if (result.equals("OK")) {
                        gamePlayActivity.sendPieceLocation(yourId, getPosition(), yourColor, matchNumber);
                    }
                }
            });
        }
    }

    /**
     * Update board when receiving chess from user or click on broad
     * @param clickedPosition
     * @param color
     */
    public String updateBoard(int clickedPosition, String color, boolean hitOnBoard) {
        List<Integer> changedPositions = othelloLogic.onClickedListener(clickedPosition, color, hitOnBoard);
        if (changedPositions == null) {
            return "FAILED";
        }

        // reload recycler view: notify each changed piece
        for (int i = 0; i < changedPositions.size(); i++) {
            notifyItemChanged(changedPositions.get(i));
        }

        return "OK";
    }

    public GamePlayActivity getGamePlayActivity() {
        return gamePlayActivity;
    }

    public OthelloLogic getOthelloLogic() {
        return othelloLogic;
    }
}
