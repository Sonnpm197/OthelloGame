package com.example.son.othellogame.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.son.othellogame.R;
import com.example.son.othellogame.entities.ChessPiece;
import com.example.son.othellogame.entities.Player;
import com.example.son.othellogame.logic.OthelloLogic;

import static com.example.son.othellogame.entities.ChessPiece.PieceColor.BLACK;
import static com.example.son.othellogame.entities.ChessPiece.PieceColor.WHITE;

import java.util.List;

public class ChessBroadAdapter extends RecyclerView.Adapter<ChessBroadAdapter.ViewHolder> {

    private List<ChessPiece> listChess;
    private Context context;
    private boolean firstTime = true; // first time user clicks on a broad
    private OthelloLogic othelloLogic;

    public ChessBroadAdapter(Context context) {
        this.context = context;
        Player player1 = new Player();
        Player player2 = new Player();
        othelloLogic = new OthelloLogic(this, player1, player2);
        listChess = othelloLogic.createBroad();
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
        holder.chess.setText("" + (position % OthelloLogic.COLUMN_QUANTITY) + "," + (position / OthelloLogic.COLUMN_QUANTITY));
        holder.chess.setTextColor(Color.RED);
        // this holder is a piece
        if (piece.getColor() != null) {
            if (piece.getColor().equals(BLACK.getValue())) {
                holder.chess.setBackground(context.getResources().getDrawable(R.drawable.black_piece));
            } else if (piece.getColor().equals(WHITE.getValue())) {
                holder.chess.setBackground(context.getResources().getDrawable(R.drawable.white_piece));
            }
        }
//        holder.chess.setText(piece.getColor());
        // TODO: add information of each chess here
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
                    List<Integer> changedPositions = othelloLogic.onClickedListener(getPosition());
                    if (changedPositions == null) {
                        Toast.makeText(context, "You cannot move without capturing component's pieces", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // reload recycler view
                    // TODO: notify each changed piece
                    for (int i = 0; i < changedPositions.size(); i++) {
                        notifyItemChanged(changedPositions.get(i));
                    }
//                    Toast.makeText(context, "Position: " + getPosition(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void setFirstTime(boolean firstTime) {
        this.firstTime = firstTime;
    }

    public boolean isFirstTime() {
        return firstTime;
    }
}
