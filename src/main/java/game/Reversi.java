package game;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class Reversi {

    private JButton[][] boardButtons;
    private Piece[][] board;
    private JFrame gui;
    private Image blackPiece;
    private Image whitePiece;

    private static final int BOARD_SIZE = 8;

    public Reversi() {
        gui = new JFrame();
        gui.setTitle("Reversi");
        gui.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        gui.setSize(new Dimension(700, 700));
        gui.setLayout(new GridLayout(BOARD_SIZE, BOARD_SIZE));

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            blackPiece = ImageIO.read(Reversi.class.getResource("/black_piece.png"));
            whitePiece = ImageIO.read(Reversi.class.getResource("/white_piece.png"));
        } catch (Exception e) {
            System.out.println("Failed to initialize the board.  Exiting.");
            System.exit(2);
        }

        // Scale the image to fit in the grid
        blackPiece = blackPiece.getScaledInstance(85, 85, Image.SCALE_SMOOTH);
        whitePiece = whitePiece.getScaledInstance(85, 85, Image.SCALE_SMOOTH);

        // Reset the game board
        resetBoard();
        gui.setVisible(true);
    }

    /**
     * Resets the reversi board to default
     */
    private void resetBoard() {

        this.boardButtons = new JButton[BOARD_SIZE][BOARD_SIZE];
        this.board = new Piece[BOARD_SIZE][BOARD_SIZE];

        // Initialize the board with no pieces
        for(int i = 0; i < BOARD_SIZE; i++) {
            Arrays.fill(this.board[i], Piece.NONE);
        }

        // Set the background color as green
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                boardButtons[i][j] = new JButton();
                boardButtons[i][j].setOpaque(true);
                boardButtons[i][j].setBackground(Color.GREEN);
                gui.add(boardButtons[i][j]);
            }
        }

        // Initial piece placement
        this.board[3][3] = Piece.BLACK;
        this.board[3][4] = Piece.WHITE;
        this.board[4][3] = Piece.WHITE;
        this.board[4][4] = Piece.BLACK;


        // draw the board
        drawBoard();
    }

    /**
     * Draws pieces on the baord based on the current board state
     */
    private void drawBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for(int j = 0; j < BOARD_SIZE; j++) {
                switch (board[i][j]) {
                    case BLACK:
                        boardButtons[i][j].setIcon(new ImageIcon((blackPiece)));
                        break;

                    case WHITE:
                        boardButtons[i][j].setIcon(new ImageIcon((whitePiece)));
                        break;

                    case NONE:
                        boardButtons[i][j].setIcon(null);
                }
            }
        }
    }
}

