

package com.digiprog.uttt;

/**
 * Created by Jetch on 3.4.2014.
 * Jetchi tee nää :D jos keksit lisää hyödyllisiä funktioita nii lisää
 */
public class Logic {

    public static final char CIRCLE = 'o';
    public static final char CROSS = 'x';
    public static final char EMPTY = '_';
    public static final char DRAW = '.';

    public char player = CROSS;
    private subBoard subBoard[][];
    public int currentRow, currentCol, nextRow, nextCol; // current row & column Board
    private boolean chooseFreely = true;
    public char game = EMPTY;


    //creates the new game, initializes values
    public Logic(){
        init();
    }

    public void init() {
        player = CROSS;
        subBoard = new subBoard[3][3];
        chooseFreely = true;
        //new subBoard();
        game = EMPTY;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                subBoard[row][col] = new subBoard(row, col);
            }
        }
        currentRow = 1;
        currentCol = 1;
    }


    public boolean updateGame(int cellRow, int cellCol) {
        if (putGameMark(currentRow, currentCol, cellRow, cellCol)) {
            if (gameWon() == EMPTY && !isDraw()) {
                player = player == CROSS ? CIRCLE : CROSS;
            }
            currentRow = nextRow;
            currentCol = nextCol;
            chooseFreely = subBoardWon(currentRow, currentCol) != EMPTY;
            return true;
        }
        return false;


    }


    //returns CIRCLE, CROSS or EMPTY based on the mark the cell has
    public char getGameMark(int subBoardRow, int subBoardCol, int cellRow, int cellCol){
        return subBoard[subBoardRow][subBoardCol].cell[cellRow][cellCol];
    }

    //puts a new mark on the board
    //returns false if mark cannot be put there, true if mark put successfully
    public boolean putGameMark(int subBoardRow, int subBoardCol, int cellRow, int cellCol) {
        if (subBoard[subBoardRow][subBoardCol].cell[cellRow][cellCol] == EMPTY  && subBoardWon(subBoardRow, subBoardCol) == EMPTY) {
            nextRow = cellRow;
            nextCol = cellCol;
            subBoard[subBoardRow][subBoardCol].cell[cellRow][cellCol] = player;
            return true;
        }
        return false;
    }

    //return CIRCLE or CROSS based on the next player
    public char getNextPlayer(){
        return player;
    }

    //returns boolean based on whether or not the player can choose the sub board freely
    //(this happens when game starts, or when the previous player plays so that the next player would have to move to a won sub board)
    public boolean canChooseSubBoardFreely() {
        return chooseFreely;
    }



        //chooses sub board. returns false if sub board is already won, otherwise returns true
    public boolean chooseSubBoard(int row, int col){
        if (subBoardWon(row, col) == EMPTY && chooseFreely) {
            currentRow = row;
            currentCol = col;
            return true;
        }
        return false;

    }

    //returns who won the sub board
    //if it hasn't been won, return EMPTY
    public char subBoardWon(int subBoardRow, int subBoardCol){
        return subBoard[subBoardRow][subBoardCol].getWon();
    }


    //returns who won the game
    //if game hasn't been won, return EMPTY
    public char gameWon(){
        if ((subBoardWon(currentRow, 0) == player && subBoardWon(currentRow, 1) == player && subBoardWon(currentRow, 2) == player)
                || (subBoardWon(0, currentCol) == player && subBoardWon(1, currentCol) == player && subBoardWon(2, currentCol) == player)
                || (subBoardWon(0, 0) == player && subBoardWon(1, 1) == player && subBoardWon(2, 2) == player)
                || (subBoardWon(0, 2) == player && subBoardWon(1, 1) == player && subBoardWon(2, 0) == player)) {
            game = player;
            return game;
        }
        return game;
    }

    public boolean isDraw() {
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                if(subBoardWon(i, j) == EMPTY)
                    return false;
            }
        }
        game = DRAW;
        return true;
    }
}

class subBoard {
    public static final char CIRCLE = 'o';
    public static final char CROSS = 'x';
    public static final char EMPTY = '_';
    public static final char DRAW = '.';
    char state;
    public char[][] cell = new char[3][3];

    public subBoard(int row, int col) {
        state = EMPTY;
        init();
    }

    public void init() {
        for (int row = 0; row < 3; row++) {
            cell[row] = new char[3];
            for (int col = 0; col < 3; col++) {
                cell[row][col] = EMPTY;
            }
        }
    }

    public boolean isDraw() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (cell[row][col] == EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean hasWon (char player) {

        // diagnonals
        if ((cell[0][0] == player && cell[1][1] == player && cell[2][2] == player)
                || (cell[0][2] == player && cell[1][1] == player && cell[2][0] == player)) {
            return true;
        }
        // rows & columns
        for (int i = 0; i < 3; i++) {
            if ((cell[i][0] == player && cell[i][1] == player && cell[i][2] == player)
                    || (cell[0][i] == player && cell[1][i] == player && cell[2][i] == player)) {

                return true;
            }
        }
        return false;
    }

    public char getWon() {
        if(hasWon(CIRCLE))
            return CIRCLE;
        else if(hasWon(CROSS))
            return CROSS;
        else if(isDraw())
            return DRAW;
        else
            return EMPTY;
    }

}

