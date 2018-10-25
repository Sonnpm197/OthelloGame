package com.example.son.othellogame.logic;

import android.util.Log;

import com.example.son.othellogame.adapter.ChessBroadAdapter;
import com.example.son.othellogame.entities.ChessPiece;
import com.example.son.othellogame.entities.Player;

import java.util.ArrayList;
import java.util.List;

import static com.example.son.othellogame.entities.ChessPiece.PieceColor.BLACK;
import static com.example.son.othellogame.entities.ChessPiece.PieceColor.WHITE;

public class OthelloLogic {

    public static final int ROW_QUANTITY = 8;
    public static final int COLUMN_QUANTITY = 8;

    private ChessBroadAdapter chessBroadAdapter;
    private List<ChessPiece> listChess; // get piece by broad position (not by dimension values) and show on UI
    private ChessPiece[][] matrixChess; // get by dimension values (row, col)
    private Player player1, player2;
    private String currentColor = BLACK.getValue(); // black goes first
    private String oppositeColor = WHITE.getValue();
    private ChessPiece clickedPiece; // clickedPiece each turn onClickedListener
    private List<ChessPiece> capturedPieces; // captured pieces from opponent each turn
    private String tag = "logger";

    public OthelloLogic(ChessBroadAdapter chessBroadAdapter, Player player1, Player player2) {
        this.chessBroadAdapter = chessBroadAdapter;
        this.player1 = player1;
        this.player2 = player2;
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
                ChessPiece chessPiece = new ChessPiece(null, x, y, position);
                listChess.add(chessPiece);
                matrixChess[y][x] = chessPiece;
                position ++;
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
     * Whenever user clicks on a button, get returned list's position and calculate real x & y positions
     * Rules:
     * <p>Black goes first</p>
     *
     * @param broadPosition
     */
//    public boolean onClickedListener(int broadPosition) {
//
//        capturedPieces = new ArrayList<>();
//        clickedPiece = listChess.get(broadPosition);
//        Log.i(tag, "X: " + getPositionX(broadPosition) + ";Y: " + getPositionY(broadPosition));
//        // Prevent user clicks on a existed chess
//        if (clickedPiece.getColor() != null) {
//            return false;
//        }
//
//        // Prevent user clicks randomly on broad (clicked piece must be near by exited pieces)
//        if (!checkAroundClickedPiece(broadPosition)) {
//            return false;
//        }
//
//        // Perform validate main & minor crosses, horizon & vertical
//        boolean availablePlace = updateChessBroad(broadPosition);
//        if (!availablePlace) {
//            return false;
//        } else {
//            // Change color after all validations done
//            if (chessBroadAdapter.isFirstTime()) {
//                chessBroadAdapter.setFirstTime(false);
//                currentColor = WHITE.getValue();
//                oppositeColor = BLACK.getValue();
//            } else {
//                if (currentColor.equals(BLACK.getValue())) {
//                    currentColor = WHITE.getValue();
//                    oppositeColor = BLACK.getValue();
//                } else {
//                    currentColor = BLACK.getValue();
//                    oppositeColor = WHITE.getValue();
//                }
//            }
//            return true;
//        }
//    }


    public List<Integer> onClickedListener(int broadPosition) {

        capturedPieces = new ArrayList<>();
        clickedPiece = listChess.get(broadPosition);
        Log.i(tag, "X: " + getPositionX(broadPosition) + ";Y: " + getPositionY(broadPosition));
        // Prevent user clicks on a existed chess
        if (clickedPiece.getColor() != null) {
            return null;
        }

        // Prevent user clicks randomly on broad (clicked piece must be near by exited pieces)
        if (!checkAroundClickedPiece(broadPosition)) {
            return null;
        }

        // Perform validate main & minor crosses, horizon & vertical
        List<Integer> availablePlace = updateChessBroad(broadPosition);
        if (availablePlace.size() <= 1) {
            return null;
        } else {
            // Change color after all validations done
            if (chessBroadAdapter.isFirstTime()) {
                chessBroadAdapter.setFirstTime(false);
                currentColor = WHITE.getValue();
                oppositeColor = BLACK.getValue();
            } else {
                if (currentColor.equals(BLACK.getValue())) {
                    currentColor = WHITE.getValue();
                    oppositeColor = BLACK.getValue();
                } else {
                    currentColor = BLACK.getValue();
                    oppositeColor = WHITE.getValue();
                }
            }
            return availablePlace;
        }
    }

    /**
     * Clicked piece must be near another colored piece (at least 1)
     * Generally clicked one is surrounded by 8 other pieces
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
        for (ChessPiece piece: surroundedPieces) {
            if (piece.getColor() != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Update chess broad by othello rules
     * Cannot move without capturing opponent's pieces
     *
     * @param broadPosition
     * @return boolean to show the place is available
     */
//    private boolean updateChessBroad(int broadPosition) {
//        int x = getPositionX(broadPosition);
//        int y = getPositionY(broadPosition);
//        validateMainCross(x, y);
//        validateMinorCross(x, y);
//        validateHorizon(x, y);
//        validateVertical(x, y);
//        updateCapturedPiecesAndClickedPiece();
//        //Log.i(tag, "Turn: " + currentColor + ";Size: " + capturedPieces.size());
//        // Make sure you can capture at least 1 piece of your opponent or return false
//        return (capturedPieces.size() > 0);
//    }

    private List<Integer> updateChessBroad(int broadPosition) {
        int x = getPositionX(broadPosition);
        int y = getPositionY(broadPosition);
        validateMainCross(x, y);
        validateMinorCross(x, y);
        validateHorizon(x, y);
        validateVertical(x, y);
        updateCapturedPiecesAndClickedPiece();
        //Log.i(tag, "Turn: " + currentColor + ";Size: " + capturedPieces.size());
        // Make sure you can capture at least 1 piece of your opponent or return false
        List<Integer> l = new ArrayList<>();
        for (ChessPiece chessPiece: capturedPieces) {
            l.add(chessPiece.getPosition());
        }
        l.add(clickedPiece.getPosition());
        return l;
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
//                capturedPieces.addAll(new ArrayList<>(tempCapturedList));
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
//                capturedPieces.addAll(new ArrayList<>(tempCapturedList));
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
//                capturedPieces.addAll(new ArrayList<>(tempCapturedList));
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
//                capturedPieces.addAll(new ArrayList<>(tempCapturedList));
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
//                capturedPieces.addAll(new ArrayList<>(tempCapturedList));
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
//                capturedPieces.addAll(new ArrayList<>(tempCapturedList));
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

    private List<ChessPiece> validateAddedPieces(List<ChessPiece> tempCapturedList) {
        boolean error = false;
        for (ChessPiece chessPiece: tempCapturedList) {
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
    private void updateCapturedPiecesAndClickedPiece() {
        if (capturedPieces != null && capturedPieces.size() > 0) {
            // Updating captured pieces and clicked piece
            for (ChessPiece piece : capturedPieces) {
                piece.setColor(currentColor);
            }

            clickedPiece.setColor(currentColor);
        }
    }
}
