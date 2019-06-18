package game;

import constants.Piece;
import constants.ReversiConstants;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MovementListener implements ActionListener {

    private Piece[][] board;
    private Reversi game;

    MovementListener(Piece[][] board, Reversi reversiGame) {
        this.board = board;
        this.game = reversiGame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int row = Integer.parseInt(((JButton) e.getSource()).getClientProperty(ReversiConstants.ROW).toString());
        int column = Integer.parseInt(((JButton) e.getSource()).getClientProperty(ReversiConstants.COLUMN).toString());

        System.out.println("Row: " + row  +" + column: " + column);

    }
}
