package game;

import constants.Piece;

public class Move {

    private int score;
    private int row;
    private int column;
    private Piece[][] board;

    public Move(int score, int row, int column) {
        this.score = score;
        this.row = row;
        this.column = column;
        this.board = null;
    }

    public Move(Piece[][] board, int score, int row, int column) {
        this.board = board;
        this.score = score;
        this.row = row;
        this.column = column;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public Piece[][] getBoard() {
        return this.board;
    }
}
