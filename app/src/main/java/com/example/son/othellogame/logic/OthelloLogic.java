package com.example.son.othellogame.logic;

import android.util.Log;
import android.widget.Toast;

import com.example.son.othellogame.GamePlayActivity;
import com.example.son.othellogame.adapter.ChessBroadAdapter;
import com.example.son.othellogame.entities.ChessPiece;
import com.example.son.othellogame.entities.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.son.othellogame.entities.ChessPiece.PieceColor.BLACK;
import static com.example.son.othellogame.entities.ChessPiece.PieceColor.WHITE;

public class OthelloLogic {

    public static final int ROW_QUANTITY = 8;
    public static final int COLUMN_QUANTITY = 8;
    private static final String TAG = OthelloLogic.class.getSimpleName();

    private ChessBroadAdapter chessBroadAdapter;
    private List<ChessPiece> listChess; // get piece by broad position (not by dimension values) and show on UI (0 -> 64)
    private ChessPiece[][] matrixChess; // get by dimension values (row, col)
    private String currentColor; // to find who will go next and update board bases on this color
    private String yourColor, opponentColor;
    private ChessPiece clickedPiece; // clickedPiece each turn onClickedListener
    private List<ChessPiece> capturedPieces; // captured pieces from opponent each turn
    private String previousSwapTurnColor;
    private GamePlayActivity gamePlayActivity;
    private boolean lostTurn = false; // when you have no other move then change to component
    private boolean yourTurn = true;
    private boolean firstTime = true; // first time a piece hits on board
    private boolean opponentLostTurn;

    public OthelloLogic(GamePlayActivity gamePlayActivity, String yourColor) {
        this.gamePlayActivity = gamePlayActivity; // context of GamePlayActivity
        this.yourColor = yourColor;
        opponentColor = (yourColor.equals(BLACK.getValue()) ? WHITE.getValue() : BLACK.getValue());
    }

    public void setOpponentLostTurn(boolean opponentLostTurn) {
        this.opponentLostTurn = opponentLostTurn;
    }

    /**
     * <p>Create broad when start a game
     * <p>The listChess will get a chess by sequential number
     * <p>E.g: Count from zero: listChess.get(8) => row 1 col 0
     * <p>
     * <p>First initializing components on 3rd and 4th rows + columns (count from zero)
     * <p>white (x = 3, y = 3) black (x = 4, y = 3)
     * <p>black (x = 3, y = 4) white (x = 4, y = 4)
     *
     * @return
     */
    public List<ChessPiece> createBroad() {
        listChess = new ArrayList<>();
        matrixChess = new ChessPiece[COLUMN_QUANTITY][ROW_QUANTITY];
        // add pieces
        int position = 0;
        for (int y = 0; y < COLUMN_QUANTITY; y++) {
            for (int x = 0; x < ROW_QUANTITY; x++) {
                // initialize UI position (0 -> 64) for piece
                ChessPiece chessPiece = new ChessPiece(null, x, y, position);
                listChess.add(chessPiece);
                matrixChess[y][x] = chessPiece;
                position++;
            }
        }

        // Start index by arraylist in 3rd and 4th rows
        int thirdRowIndex = 3 * ROW_QUANTITY;
        int fourthRowIndex = 4 * COLUMN_QUANTITY;
        ChessPiece firstWhite = listChess.get(thirdRowIndex + 3);
        firstWhite.setColor(WHITE.getValue());
        ChessPiece firstBlack = listChess.get(thirdRowIndex + 4);
        firstBlack.setColor(BLACK.getValue());
        ChessPiece secondBlack = listChess.get(fourthRowIndex + 3);
        secondBlack.setColor(BLACK.getValue());
        ChessPiece secondWhite = listChess.get(fourthRowIndex + 4);
        secondWhite.setColor(WHITE.getValue());

        return listChess;
    }

    /**
     * Param color indicates this is a received piece and also your piece
     *
     * This method will be called when you click on board or when you receive a piece from your opponent
     *      - If this method is called on click action then yourTurn = false
     *      - else when you receive piece yourTurn = true
     *
     * Whenever user clicks on a button, get returned list's position and calculate real x & y positions
     * Rules:
     * <p>Black goes first</p>
     * <p>Opponent's pieces must be captured at least 1</p>
     *
     * Check winner 2 times:
     *      - When receiving a piece from opponent
     *      - When hit on board
     *
     *
     * @param broadPosition
     */

    public List<Integer> onClickedListener(int broadPosition, String color, boolean hitOnBoard) {

        // If white player tries to click on broad first
        if (hitOnBoard && firstTime && yourColor.equals(WHITE.getValue()) && color.equals(WHITE.getValue())) {
            Toast.makeText(gamePlayActivity, "Black goes first", Toast.LENGTH_SHORT).show();
            return null;
        }

        // first time any piece hits on board
        firstTime = false;

        // If your opponent lost turn => yourTurn
        // If you receive a reverse color (from opponent) then accept and update to board
        if (color.equals(opponentColor) || opponentLostTurn) {
            yourTurn = true;
            opponentLostTurn = false;
        }

        // If user already hit then prevent them from not hitting the second time
        if (!yourTurn) {
            Toast.makeText(gamePlayActivity, "It is not your turn yet. Please wait...", Toast.LENGTH_SHORT).show();
            return null;
        }

        capturedPieces = new ArrayList<>();
        clickedPiece = listChess.get(broadPosition);
        // Set color for 2 cases:
        //      1. Receive piece from your opponent
        //      2. You hit on board
        currentColor = color;

        Log.i(TAG, "History: X:" + getPositionX(broadPosition) + ";Y:" + getPositionY(broadPosition) + ";color:" + currentColor);
        // Prevent user clicks on a existed chess
        if (clickedPiece.getColor() != null) {
            Toast.makeText(gamePlayActivity, "You cannot click on an existed piece", Toast.LENGTH_SHORT).show();
            clickedPiece = null;
            return null;
        }

        // Prevent user clicks randomly on broad (clicked piece must be near by exited pieces)
        if (!checkAroundClickedPiece(broadPosition)) {
            Toast.makeText(gamePlayActivity, "Your piece has to be near by other pieces", Toast.LENGTH_SHORT).show();
            clickedPiece = null;
            return null;
        }

        // Perform validate main & minor crosses, horizon & vertical
        List<Integer> capturedPosition = updateChessBroad(broadPosition);

        // Scan when you receive opponent piece and you have no turn left
        // After updating all chess broad then check game over / lost turn
        String messageType = "";

        // gamePlayActivity will send and handle message when lost turn / full board
        if (!messageType.equals("")) {
            gamePlayActivity.sendResultMessageFromOthelloLogic(messageType);
        }

        if (capturedPosition.size() <= 1) { // this will include both clickedPiece location and capturedLocation
            Toast.makeText(gamePlayActivity, "Cannot move without capturing any opponent piece(s)", Toast.LENGTH_SHORT).show();
            clickedPiece = null;
            return null;
        } else {

            // This means you already hit on board
            if (currentColor.equals(yourColor)) {
                yourTurn = false;
            }

            // Update data in gamePlayActivity
            gamePlayActivity.updateScore(calculateBlackAndWhitePieces());
            gamePlayActivity.updateCurrentTurn(yourTurn);

            clickedPiece = null;
            return capturedPosition;
        }
    }

    /**
     * Calculate sum of both pieces
     * @return
     */
    private String calculateBlackAndWhitePieces() {
        int blackPieces = 0;
        int whitePieces = 0;
        for (ChessPiece piece : listChess) {
            if (piece.getColor() != null && piece.getColor().equals(BLACK.getValue())) {
                blackPieces++;
            } else if (piece.getColor() != null && piece.getColor().equals(WHITE.getValue())) {
                whitePieces ++;
            }
        }

        return "Black: " + blackPieces + " / White: " + whitePieces;
    }

    /**
     * Clicked piece must be near another colored piece (at least 1)
     * Generally clicked one is surrounded by 8 other pieces
     *
     * @return
     */
    private boolean checkAroundClickedPiece(int broadPosition) {
        int x = getPositionX(broadPosition);
        int y = getPositionY(broadPosition);
        List<ChessPiece> surroundedPieces = new ArrayList<>();

        for (int yIndex = -1; yIndex <= 1; yIndex++) {
            for (int xIndex = -1; xIndex <= 1; xIndex++) {
                // Not count current clicked point
                if (xIndex == 0 && yIndex == 0) {
                    continue;
                }
                // Get surrounded pieces
                try {
                    surroundedPieces.add(matrixChess[y + yIndex][x + xIndex]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    continue;
                }
            }
        }

        // At least 1 piece near by has color
        for (ChessPiece piece : surroundedPieces) {
            if (piece.getColor() != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Update chess broad by othello rules
     * Cannot move without capturing opponent's pieces
     * This method will return clicked piece and all captured pieces by UI location
     * This method is used to check in 2 cases:
     * 1. check if game over (before setting clickedPiece) -> clickedPiece = null
     * 2. check to update color after validating 4 crosses
     *
     * @param broadPosition
     * @return boolean to show the place is available
     */

    private List<Integer> updateChessBroad(int broadPosition) {
        if (capturedPieces == null) {
            capturedPieces = new ArrayList<>();
        }

        capturedPieces.clear();

        int x = getPositionX(broadPosition);
        int y = getPositionY(broadPosition);
        validateMainCross(x, y);
        validateMinorCross(x, y);
        validateHorizon(x, y);
        validateVertical(x, y);
        if (clickedPiece != null) {
            updateCapturedPiecesAndClickedPieceColor();
        }
        //Log.i(tag, "Turn: " + currentColor + ";Size: " + capturedPieces.size());
        // Make sure you can capture at least 1 piece of your opponent or return false
        // List capturedPieces will increase after validating 4 crosses
        List<Integer> capturedPositionsOnUI = new ArrayList<>();
        for (ChessPiece chessPiece : capturedPieces) {
            capturedPositionsOnUI.add(chessPiece.getPosition());
        }

        if (clickedPiece != null) {
            capturedPositionsOnUI.add(clickedPiece.getPosition());
        }
        // When checking gameOver, every piece which has no color surrounding other pieces will be scanned
        // After updating all the captured piece and clickPiece color, we need to set clickPiece to null
        // hence the above logic:
        // if (clickedPiece != null) {
        //            updateCapturedPiecesAndClickedPieceColor();
        //        }
        // will not be able to update color for excessive empty piece
        clickedPiece = null;
        return capturedPositionsOnUI;
    }

    /**
     * (0,0) -> (1,1) -> (2,2)
     */
    private void validateMainCross(int x, int y) {
        int topLeftLastFoundPieceX = -1;
        int bottomRightLastFoundPieceX = -1;
        List<ChessPiece> tempCapturedList = new ArrayList<>();

        // Scan from piece's position -> top-left and to bottom-right
        // If y > x then x reaches 0 first and y reaches COLUMN_QUANTITY
        // If x > y then y reaches 0 first and x reaches ROW_QUANTITY

        if (x - 1 >= 0) {
            // Scan from piece's location to top-left to find the top-left piece same color
            int modifiedY = y;
            for (int modifiedX = x - 1; modifiedX >= 0; modifiedX--) {
                if (modifiedY == 0) {
                    break;
                }
                modifiedY--;
                if (currentColor.equals(matrixChess[modifiedY][modifiedX].getColor())) {
                    topLeftLastFoundPieceX = modifiedX;
                    break;
                }
            }

            // Add to capturedPieces list if user can capture opponent's pieces
            if (topLeftLastFoundPieceX != -1) {
                modifiedY = y;
                for (int modifiedX = x - 1; modifiedX > topLeftLastFoundPieceX; modifiedX--) {
                    if (modifiedY == 0) {
                        break;
                    }

                    modifiedY--;
                    tempCapturedList.add(matrixChess[modifiedY][modifiedX]);
                }
                tempCapturedList = validateAddedPieces(tempCapturedList);
                capturedPieces.addAll(tempCapturedList);
            }
        }

        // Scan from piece's location to bottom-right
        if ((x + 1) < ROW_QUANTITY) {
            int modifiedY = y;
            for (int modifiedX = x + 1; modifiedX < ROW_QUANTITY; modifiedX++) {
                if (modifiedY == COLUMN_QUANTITY - 1) {
                    break;
                }
                modifiedY++;
                if (currentColor.equals(matrixChess[modifiedY][modifiedX].getColor())) {
                    bottomRightLastFoundPieceX = modifiedX;
                    break;
                }
            }

            // Change color of all pieces between x and leftLastFoundPiece to current color if they're opposite color
            if (bottomRightLastFoundPieceX != -1) {
                modifiedY = y; // reset to calculate again from start point
                for (int modifiedX = x + 1; modifiedX < bottomRightLastFoundPieceX; modifiedX++) {
                    if (modifiedY == COLUMN_QUANTITY - 1) {
                        break;
                    }

                    modifiedY++;
                    tempCapturedList.add(matrixChess[modifiedY][modifiedX]);
                }

                tempCapturedList = validateAddedPieces(tempCapturedList);
                capturedPieces.addAll(tempCapturedList);
            }
        }

    }

    /**
     * (7,0) -> (6,1) -> (5,2)
     */
    private void validateMinorCross(int x, int y) {
        int topRightLastFoundPiece = -1;
        int bottomLeftLastFoundPieceX = -1;
        List<ChessPiece> tempCapturedList = new ArrayList<>();

        // If x increases then y decreases
        // If y increase then x decreases

        // Scan from piece's location to top-right
        if (x + 1 < ROW_QUANTITY) {
            int modifiedY = y;
            for (int modifiedX = x + 1; modifiedX < ROW_QUANTITY; modifiedX++) {
                if (modifiedY == 0) {
                    break;
                }
                modifiedY--;
                if (currentColor.equals(matrixChess[modifiedY][modifiedX].getColor())) {
                    topRightLastFoundPiece = modifiedX;
                    break;
                }
            }

            // Change color of all pieces between x and leftLastFoundPiece to current color if they're opposite color
            if (topRightLastFoundPiece != -1) {
                modifiedY = y; // reset to calculate again from start point
                for (int modifiedX = x + 1; modifiedX < topRightLastFoundPiece; modifiedX++) {
                    if (modifiedY == 0) {
                        break;
                    }

                    modifiedY--;
                    tempCapturedList.add(matrixChess[modifiedY][modifiedX]);
                }

                tempCapturedList = validateAddedPieces(tempCapturedList);
                capturedPieces.addAll(tempCapturedList);
            }
        }

        // Scan from piece's location to bottom-left
        if ((x - 1) >= 0) {
            int modifiedY = y;
            for (int modifiedX = x - 1; modifiedX >= 0; modifiedX--) {
                if (modifiedY == COLUMN_QUANTITY - 1) {
                    break;
                }
                modifiedY++;
                if (currentColor.equals(matrixChess[modifiedY][modifiedX].getColor())) {
                    bottomLeftLastFoundPieceX = modifiedX;
                    break;
                }
            }

            // Change color of all pieces between x and leftLastFoundPiece to current color if they're opposite color
            if (bottomLeftLastFoundPieceX != -1) {
                modifiedY = y;
                for (int modifiedX = x - 1; modifiedX > bottomLeftLastFoundPieceX; modifiedX--) {
                    if (modifiedY == COLUMN_QUANTITY - 1) {
                        break;
                    }

                    modifiedY++;
                    tempCapturedList.add(matrixChess[modifiedY][modifiedX]);
                }

                tempCapturedList = validateAddedPieces(tempCapturedList);
                capturedPieces.addAll(tempCapturedList);
            }
        }
    }

    private void validateHorizon(int x, int y) {
        int leftLastFoundPiece = -1;
        int rightLastFoundPiece = -1;
        List<ChessPiece> tempCapturedList = new ArrayList<>();

        // Start with x, validate left to right
        if ((x + 1) < ROW_QUANTITY) {
            for (int i = x + 1; i < ROW_QUANTITY; i++) {
                if (currentColor.equals(matrixChess[y][i].getColor())) {
                    leftLastFoundPiece = i;
                    break;
                }
            }
            // Change color of all pieces between x and leftLastFoundPiece to current color if they're opposite color
            if (leftLastFoundPiece != -1) {
                for (int i = x + 1; i < leftLastFoundPiece; i++) {
                    tempCapturedList.add(matrixChess[y][i]);
                }

                tempCapturedList = validateAddedPieces(tempCapturedList);
                capturedPieces.addAll(tempCapturedList);
            }
        }

        // Start with x, validate right to left
        if ((x - 1) >= 0) {
            for (int i = x - 1; i >= 0; i--) {
                if (currentColor.equals(matrixChess[y][i].getColor())) {
                    rightLastFoundPiece = i;
                    break;
                }
            }
            // Change color of all pieces between x and leftLastFoundPiece to current color if they're opposite color
            if (rightLastFoundPiece != -1) {
                for (int i = x - 1; i > rightLastFoundPiece; i--) {
                    tempCapturedList.add(matrixChess[y][i]);
                }

                tempCapturedList = validateAddedPieces(tempCapturedList);
                capturedPieces.addAll(tempCapturedList);
            }
        }
    }

    private void validateVertical(int x, int y) {
        int topLastFoundPiece = -1;
        int bottomLastFoundPiece = -1;
        List<ChessPiece> tempCapturedList = new ArrayList<>();

        // Start from y, validate top to bottom
        if ((y + 1) < COLUMN_QUANTITY) {
            for (int i = y + 1; i < COLUMN_QUANTITY; i++) {
                if (currentColor.equals(matrixChess[i][x].getColor())) {
                    bottomLastFoundPiece = i;
                    break;
                }
            }
            // Able to find last piece
            if (bottomLastFoundPiece != -1) {
                for (int i = y + 1; i < bottomLastFoundPiece; i++) {
                    tempCapturedList.add(matrixChess[i][x]);
                }

                tempCapturedList = validateAddedPieces(tempCapturedList);
                capturedPieces.addAll(new ArrayList<>(tempCapturedList));
            }
        }

        // Start from y, validate bottom to top
        if ((y - 1) >= 0) {
            for (int i = y - 1; i >= 0; i--) {
                if (currentColor.equals(matrixChess[i][x].getColor())) {
                    topLastFoundPiece = i;
                    break;
                }
            }
            // Able to find last piece
            if (topLastFoundPiece != -1) {
                for (int i = y - 1; i > topLastFoundPiece; i--) {
                    tempCapturedList.add(matrixChess[i][x]);
                }

                tempCapturedList = validateAddedPieces(tempCapturedList);
                capturedPieces.addAll(new ArrayList<>(tempCapturedList));
            }
        }
    }

    /**
     * Get piece's location by x and y
     * E.g : 25 = row 3 col 1
     * <p>row = 25 / row_number</p>
     * <p>col = 25 % col_number</p>
     *
     * @param broadPosition
     * @return
     */
    private int getPositionY(int broadPosition) {
        return broadPosition / ROW_QUANTITY;
    }

    private int getPositionX(int broadPosition) {
        return broadPosition % COLUMN_QUANTITY;
    }

    /**
     * Prevent capturing any pieces in the same row in which at least 1 piece has color
     * E.g. b wwwb
     *
     * @param tempCapturedList
     * @return
     */
    private List<ChessPiece> validateAddedPieces(List<ChessPiece> tempCapturedList) {
        boolean error = false;
        for (ChessPiece chessPiece : tempCapturedList) {
            // List contains an empty place
            if (chessPiece.getColor() == null) {
                error = true;
                break;
            }
        }

        if (error) {
            tempCapturedList.clear();
        }

        return tempCapturedList;
    }

    /**
     * Update captured pieces and clicked piece
     */
    private void updateCapturedPiecesAndClickedPieceColor() {
        if (capturedPieces != null && capturedPieces.size() > 0) {
            // Updating captured pieces and clicked piece
            for (ChessPiece piece : capturedPieces) {
                piece.setColor(currentColor);
            }

            clickedPiece.setColor(currentColor);
        }
    }

    public String scanForFullBoard() {
        boolean fullSlot = true;
        for (ChessPiece piece : listChess) {
            if (piece.getColor() == null) {
                fullSlot = false;
                break;
            }
        }

        if (fullSlot) {
            String message = "Board is full. Game over";
            Log.i(TAG, message);
            Toast.makeText(gamePlayActivity, message, Toast.LENGTH_SHORT).show();
            return Message.Type.GAME_OVER_BY_FULL_BOARD.getValue();
        }
        return "";
    }

    /**
     * Skip turn if you cannot capture opponent's pieces
     * Validate all the empty slots for the current color
     * <p>
     * The game is over if
     * * 1. all slots in broad are full
     * * 2. both players are unable to move
     * <p>
     * return true if game over
     */
    private String scanForColor() {
        List<ChessPiece> availablePlaces = new ArrayList<>();

        // Filter the pieces which are not close to any others and have NO COLOR
        for (int i = 0; i < ROW_QUANTITY * COLUMN_QUANTITY; i++) {
            if (checkAroundClickedPiece(i) && listChess.get(i).getColor() == null) {
                availablePlaces.add(listChess.get(i));
            }
        }

        // Check for any pieces with your color to find any slots to take
        // If don't have any then you lost your turn
        int numberOfAvailableSlots = 0;
        for (ChessPiece piece : availablePlaces) {
            int position = piece.getPosition();
            piece.setColor(currentColor); // assuming color to find available moves for current user
            Log.i(TAG, "Scanning position: " + position + " with color: " + currentColor);
            List<Integer> capturedLocations = updateChessBroad(position);
            // This size will not include clickedPiece location so value can be 0
            if (capturedLocations.size() > 0) {
                numberOfAvailableSlots += capturedLocations.size();
                Log.i(TAG, "Moves found for " + currentColor + " at locations: " + Arrays.toString(capturedLocations.toArray()));
            }
            piece.setColor(null); // reset to prevent update color
        }

        Log.i(TAG, "Total available moves for: " + currentColor + " to place piece is " + numberOfAvailableSlots);

        if (opponentLostTurn && numberOfAvailableSlots == 0) {
            String message = "Both players have no moves available.";
            Log.i(TAG, message);
            Toast.makeText(gamePlayActivity, message, Toast.LENGTH_SHORT).show();
            return Message.Type.GAME_OVER_BY_NO_MOVES_BOTH_PLAYERS.getValue();
        }

        // After the loop if we cannot find any places
        if (numberOfAvailableSlots == 0) {
            String message = "Current color: " + currentColor + " has no move available, swap turn to your opponent.";
            Log.i(TAG, message);
            //Toast.makeText(gamePlayActivity, message, Toast.LENGTH_SHORT).show();
            return Message.Type.LOST_TURN.getValue();
        }

        // if logic can reach here it means opponent has his/her turn back
        opponentLostTurn = false;

        return "";
    }

}
