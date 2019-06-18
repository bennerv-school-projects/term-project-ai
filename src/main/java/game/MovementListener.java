package game;

import constants.ReversiConstants;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MovementListener implements ActionListener {

    private Reversi game;

    MovementListener(Reversi reversiGame) {
        this.game = reversiGame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int row = Integer.parseInt(((JButton) e.getSource()).getClientProperty(ReversiConstants.ROW).toString());
        int column = Integer.parseInt(((JButton) e.getSource()).getClientProperty(ReversiConstants.COLUMN).toString());

        game.attemptMove(row, column);

    }
}
