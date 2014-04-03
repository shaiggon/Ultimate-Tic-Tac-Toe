package com.digiprog.uttt;

/**
 * Created by lauri on 3.4.2014.
 * Jetchi tee nää :D jos keksit lisää hyödyllisiä funktioita nii lisää
 */
public class Logic {

    public static final char CIRCLE = 'o';
    public static final char CROSS = 'x';
    public static final char EMPTY = '_';

    //creates the new game, initializes values
    public Logic(){}

    //returns CIRCLE, CROSS or EMPTY based on the mark the cell has
    public char getGameMark(int parent_x, int parent_y, int child_x, int child_y){return EMPTY;}

    //puts a new mark on the board
    //returns false if mark cannot be put there, true if mark put successfully
    public boolean putGameMark(int parent_x, int parent_y, int child_x, int child_y){return true;}

    //return CIRCLE or CROSS based on the next player
    public char getNextPlayer(){return CIRCLE;}

    //returns boolean based on whether or not the player can choose the sub board freely
    //(this happens when game starts, or when the previous player plays so that the next player would have to move to a won sub board)
    public boolean canChooseSubBoardFreely(){return true;}

    //chooses sub board. returns false if sub board is already won, otherwise returns true
    public boolean chooseSubBoard(int x, int y){return true;}

    //returns who won the sub board
    //if it hasn't been won, return EMPTY
    public char subBoardWon(int parent_x, int parent_y, int child_x, int child_y){return EMPTY;}

    //returns the player who won the game
    //if game hasn't been won, it returns EMPTY
    public char gameWon(){return EMPTY;}
}
