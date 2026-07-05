/*
Title: SnakeGame
Created by: Dmitriy Shumkin & Beckner Pu. Calderon
Version: 1.2 Stable
Rel date: 6/30/2026
About: This implementation of the classical SnakeGame is written in Java using Swing to create the application and GUI. The snake is represented by a LinkedList which grows each time the snake eats an apple. The game will end if the snake tried to cross over the borders, or itself.
*/

package snakegame;

import javax.swing.JFrame;

public class SnakeGame {
    public static void main(String[] args) {
        JFrame frame = new JFrame("SnakeGame");
        Board board = new Board();

        frame.setSize(1, 1);
        frame.add(board);
        frame.pack();                        // size the window to the board's preferred size
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);   // center on screen
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        board.requestFocusInWindow();        // make sure the board receives key presses
    }
}