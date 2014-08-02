/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package new2048;

import server.Grid;

/**
 *
 * @author NETLAB
 */
public class NewAI {

    public static void initProcess() {

        New2048.init_tables();
    }

    public static int findBestMove(long[][] board) {
        //int ret = New2048.find_best_move(trans(board));
        //System.out.println("starting find best move.");
        int ret = New2048.play_game(trans(board));
        switch (ret) {
            case 0:
                return 0;
            case 1:
                return 2;
            case 2:
                return 3;
            case 3:
                return 1;
        }

        return -1;
    }

    /*
     public static int find_best_move(Grid grid) {
     int ret = New2048.find_best_move(grid.getLong());
     switch (ret) {
     case 0:
     return 0;
     case 1:
     return 2;
     case 2:
     return 3;
     case 3:
     return 1;
     }

     return -1;
     }
     */
    private static long trans(long[][] board) {
        long ret = 0;
        int x;
        for (int i = board.length - 1; i >= 0; i--) {
            for (int j = board[i].length - 1; j >= 0; j--) {
                x = board[i][j] == 0 ? 0 : (int) (Math.log(board[i][j]) / Math.log(2));
                ret = ret << 4;
                ret = ret | x;
            }
        }
        return ret;
    }
}
