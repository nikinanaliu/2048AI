/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package new2048;

import java.io.PrintStream;
import static java.lang.Math.pow;

/**
 *
 * @author NETLAB
 */
public class New2048 {

    /* We can perform state lookups one row at a time by using arrays with 65536 entries. */

    /* Move tables. Each row or compressed column is mapped to (oldrow^newrow) assuming row/col 0.
     *
     * Thus, the value is 0 if there is no move, and otherwise equals a value that can easily be
     * xor'ed into the current board state to update the board. */
    static int row_left_table[] = new int[65536];
    static int row_right_table[] = new int[65536];
    static long col_up_table[] = new long[65536];
    static long col_down_table[] = new long[65536];
    static float heur_score_table[] = new float[65536];
    static float score_table[] = new float[65536];

    // Heuristic scoring settings
    static float SCORE_LOST_PENALTY = 200000.0f;
    static float SCORE_MONOTONICITY_POWER = 4.0f;
    static float SCORE_MONOTONICITY_WEIGHT = 47.0f;
    static float SCORE_SUM_POWER = 3.5f;
    static float SCORE_SUM_WEIGHT = 11.0f;
    static float SCORE_MERGES_WEIGHT = 700.0f;
    static float SCORE_EMPTY_WEIGHT = 270.0f;

    static long ROW_MASK = 0xFFFFL;
    static long COL_MASK = 0x000F000F000F000FL;

    static {
        init_tables();
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        // 完整矩阵输出测试和矩阵反转函数测评1�7
        /**
         * *
         * long board; board = 0xFEDCBA9876543210L; print_board(board); board =
         * transpose(board); print_board(board); *
         */
        // 行反转函数测评1�7
        /*
         int x = 0x0100;
         int z = x;
         for (int i = 0; i < 4; i++) {
         int y = z & 0x000F;
         System.out.printf("%c",
         "0123456789ABCDEF".charAt(y));
         z >>= 4;
         }
         System.out.println("");
         x = reverse_row(x);
         for (int i = 0; i < 4; i++) {
         int y = x & 0x000F;
         System.out.printf("%c",
         "0123456789ABCDEF".charAt(y));
         x >>= 4;
         }
         */
        init_tables();
        play_game();
        System.out.println("");
    }

    private static int Max(int i, int i0) {
        return i >= i0 ? i : i0;
    }

    private static double Max(double i, double i0) {
        return i >= i0 ? i : i0;
    }

    /* Find the best move for a given board. */
    static int find_best_move(long board) {
        int move;
        float best = 0;
        int bestmove = -1;

        //print_board(board);
        //System.out.printf("Current scores: heur %.0f, actual %.0f\n", score_heur_board(board), score_board(board));

        for (move = 0; move < 4; move++) {
            float res = score_toplevel_move(board, move);

            if (res > best) {
                best = res;
                bestmove = move;
            }
        }

        return bestmove;
    }

    static float score_heur_board(long board) {
        return score_helper(board, heur_score_table)
                + score_helper(transpose(board), heur_score_table);
    }

    static float score_helper(long board, float[] table) {
        return table[(int) ((board >> 0) & ROW_MASK)]
                + table[(int) ((board >> 16) & ROW_MASK)]
                + table[(int) ((board >> 32) & ROW_MASK)]
                + table[(int) ((board >> 48) & ROW_MASK)];
    }

    static float score_board(long board) {
        return score_helper(board, score_table);
    }

    static float score_toplevel_move(long board, int move) {
        float res;
        long start, finish;
        double elapsed;
        eval_state state = new eval_state();
        state.setDepth_limit(Max(3, count_distinct_tiles(board) - 5
        ));

        start = new java.util.Date().getTime();

        res = _score_toplevel_move(state, board, move);
        finish = new java.util.Date().getTime();

        elapsed = (finish - start) / 1000000.0;

        //System.out.println("Move "+move+": result "+res+": eval'd "+state.getMoves_evaled()+" moves ("+state.getCachehits()+" cache hits, "+state.getTrans_table().size()+" cache size) in "+elapsed+" seconds (maxdepth="+state.getMaxdepth()+")\n");
        return res;
    }

    static float _score_toplevel_move(eval_state state, long board, int move) {
        //int maxrank = get_max_rank(board);
        long newboard = execute_move(move, board);

        if (board == newboard) {
            return 0;
        }

        return (float) (score_tilechoose_node(state, newboard, 1.0f) + 1e-6);
    }

// Statistics and controls
// cprob: cumulative probability
// don't recurse into a node with a cprob less than this threshold
    static final float CPROB_THRESH_BASE = 0.0001f;
    static final int CACHE_DEPTH_LIMIT = 6;

    static float score_tilechoose_node(eval_state state, long board, float cprob) {
        if (cprob < CPROB_THRESH_BASE || state.getCurdepth() >= state.getDepth_limit()) {
            state.setMaxdepth(Max(state.getCurdepth(), state.getMaxdepth()));
            return score_heur_board(board);
        }

        if (state.getCurdepth() < CACHE_DEPTH_LIMIT) {
            if (state.getTrans_table().containsKey(board)) {
                state.addCachehits();
                return state.getTrans_table().get(board);
            }
        }

        int num_open = (int) count_empty(board);
        cprob /= num_open;

        float res = 0.0f;
        long tmp = board;
        long tile_2 = 1;
        while (tile_2 > 0) {
            if ((tmp & 0xf) == 0) {
                res += score_move_node(state, board | tile_2, cprob * 0.9f) * 0.9f;
                res += score_move_node(state, board | (tile_2 << 1), cprob * 0.1f) * 0.1f;
            }
            tmp >>= 4;
            tile_2 <<= 4;
        }
        res = res / num_open;

        if (state.getCurdepth() < CACHE_DEPTH_LIMIT) {
            state.getTrans_table().put(board, res);
        }

        return res;
    }

    static float score_move_node(eval_state state, long board, float cprob) {
        float best = 0.0f;
        state.addCurdepth();
        for (int move = 0; move < 4; ++move) {
            long newboard = execute_move(move, board);
            state.addMoves_evaled();

            if (board != newboard) {
                best = (float) Max(best, score_tilechoose_node(state, newboard, cprob));
            }
        }
        state.minusCurdepth();

        return best;
    }

    /* Execute a move. */
    static long execute_move(int move, long board) {
        switch (move) {
            case 0: // up
                return execute_move_0(board);
            case 1: // down
                return execute_move_1(board);
            case 2: // left
                return execute_move_2(board);
            case 3: // right
                return execute_move_3(board);
            default:
                return ~0L;
        }
    }

    static long execute_move_0(long board) {
        long ret = board;
        long t = transpose(board);
        ret ^= col_up_table[(int) ((t >> 0) & ROW_MASK)] << 0;
        ret ^= col_up_table[(int) ((t >> 16) & ROW_MASK)] << 4;
        ret ^= col_up_table[(int) ((t >> 32) & ROW_MASK)] << 8;
        ret ^= col_up_table[(int) ((t >> 48) & ROW_MASK)] << 12;
        return ret;
    }

    static long execute_move_1(long board) {
        long ret = board;
        long t = transpose(board);
        ret ^= col_down_table[(int) ((t >> 0) & ROW_MASK)] << 0;
        ret ^= col_down_table[(int) ((t >> 16) & ROW_MASK)] << 4;
        ret ^= col_down_table[(int) ((t >> 32) & ROW_MASK)] << 8;
        ret ^= col_down_table[(int) ((t >> 48) & ROW_MASK)] << 12;
        return ret;
    }

    static long execute_move_2(long board) {
        long ret = board;
        ret ^= (long) (row_left_table[(int) ((board >> 0) & ROW_MASK)]) << 0;
        ret ^= (long) (row_left_table[(int) ((board >> 16) & ROW_MASK)]) << 16;
        ret ^= (long) (row_left_table[(int) ((board >> 32) & ROW_MASK)]) << 32;
        ret ^= (long) (row_left_table[(int) ((board >> 48) & ROW_MASK)]) << 48;
        return ret;
    }

    static long execute_move_3(long board) {
        long ret = board;
        ret ^= (long) (row_right_table[(int) ((board >> 0) & ROW_MASK)]) << 0;
        ret ^= (long) (row_right_table[(int) ((board >> 16) & ROW_MASK)]) << 16;
        ret ^= (long) (row_right_table[(int) ((board >> 32) & ROW_MASK)]) << 32;
        ret ^= (long) (row_right_table[(int) ((board >> 48) & ROW_MASK)]) << 48;
        return ret;
    }

    static int count_distinct_tiles(long board) {
        int bitset = 0;
        while (board > 0) {
            bitset |= 1 << (board & 0xf);
            board >>= 4;
        }

        // Don't count empty tiles.
        bitset >>= 1;

        int count = 0;
        while (bitset > 0) {
            bitset &= bitset - 1;
            count++;
        }
        return count;
    }

    static void play_game() {
        long board = initial_board();
        int moveno = 0;
        int scorepenalty = 0; // "penalty" for obtaining free 4 tiles

        while (true) {
            int move;
            long newboard;

            for (move = 0; move < 4; move++) {
                if (execute_move(move, board) != board) {
                    break;
                }
            }
            if (move == 4) {
                break; // no legal moves
            }
            //System.out.printf("\nMove #%d, current score=%.0f\n", ++moveno, score_board(board) - scorepenalty);

            move = find_best_move(board);
            if (move < 0) {
                break;
            }

            newboard = execute_move(move, board);
            if (newboard == board) {
                System.out.printf("Illegal move!\n");
                moveno--;
                continue;
            }

            long tile = draw_tile();
            if (tile == 2) {
                scorepenalty += 4;
            }
            board = insert_tile_rand(newboard, tile);
        }

        //print_board(board);
        System.out.printf("\nGame over. Your score is %.0f. The highest rank you achieved was %d.\n", score_board(board) - scorepenalty, get_max_rank(board));
    }

    public static int play_game(long orgBoard) {
        long board = orgBoard;
        int moveno = 0;
        int scorepenalty = 0; // "penalty" for obtaining free 4 tiles
        int move;
        //print_board(board);

        for (move = 0; move < 4; move++) {
            if (execute_move(move, board) != board) {
                break;
            }
        }
        
        System.out.printf("\nMove #%d, current score=%.0f\n", ++moveno, score_board(board) - scorepenalty);

        move = find_best_move(board);
        print_board(board);
        return move;
    }

    static long initial_board() {
        long board = draw_tile() << (int) (4 * Math.random() * 16);
        return insert_tile_rand(board, draw_tile());
    }

    static int get_max_rank(long board) {
        int maxrank = 0;
        while (board > 0) {
            maxrank = Max(maxrank, (int) (board & 0xf));
            board >>= 4;
        }
        return maxrank;
    }

    /* Playing the game */
    static long draw_tile() {
        return (Math.random() * 10 < 9) ? 1 : 2;
    }

    static long insert_tile_rand(long board, long tile) {
        int index = (int) (Math.random() * (count_empty(board)));
        long tmp = board;
        while (true) {
            while ((tmp & 0xf) != 0) {
                tmp >>= 4;
                tile <<= 4;
            }
            if (index == 0) {
                break;
            }
            --index;
            tmp >>= 4;
            tile <<= 4;
        }
        return board | tile;
    }

    //矩阵对角线反转，坐上到右丄1�7
    public static long transpose(long x) {
        long a1 = x & 0xF0F00F0FF0F00F0FL;
        long a2 = x & 0x0000F0F00000F0F0L;
        long a3 = x & 0x0F0F00000F0F0000L;
        long a = a1 | (a2 << 12) | (a3 >> 12);
        long b1 = a & 0xFF00FF0000FF00FFL;
        long b2 = a & 0x00FF00FF00000000L;
        long b3 = a & 0x00000000FF00FF00L;
        return b1 | (b2 >> 24) | (b3 << 24);
    }

    // Count the number of empty positions (= zero nibbles) in a board.
    // Precondition: the board cannot be fully empty.
    // 计算空格数量
    // 条件：必须是非全空矩阄1�7
    public static long count_empty(long x) {
        x |= (x >> 2) & 0x3333333333333333L;
        x |= (x >> 1);
        x = ~x & 0x1111111111111111L;
        // At this point each nibble is:
        //  0 if the original nibble was non-zero
        //  1 if the original nibble was zero
        // Next sum them all
        x += x >> 32;
        x += x >> 16;
        x += x >> 8;
        x += x >> 4; // this can overflow to the next nibble if there were 16 empty positions
        return x & 0xfL;
    }

    public static void init_tables() {
        for (int row = 0; row < 65536; ++row) {
            int line[] = new int[4];
            //System.out.println("row="+row);
            line[0] = (row & 0x0000000f);
            //System.out.println(line[0]);
            line[1] = ((row >> 4) & 0x0000000f);
            //System.out.println(line[1]);
            line[2] = ((row >> 8) & 0x0000000f);
            //System.out.println(line[2]);
            line[3] = ((row >> 12) & 0x0000000f);
            //System.out.println(line[3]);

            // Score
            float score = 0.0f;
            for (int i = 0; i < 4; ++i) {
                int rank = line[i];
                if (rank >= 2) {
                    // the score is the total sum of the tile and all intermediate merged tiles
                    score += (rank - 1) * (1 << rank);
                }
            }
            score_table[row] = score;

            // Heuristic score
            float sum = 0;
            int empty = 0;
            int merges = 0;

            int prev = 0;
            int counter = 0;
            for (int i = 0; i < 4; ++i) {
                int rank = line[i];
                sum += pow(rank, SCORE_SUM_POWER);
                if (rank == 0) {
                    empty++;
                } else {
                    if (prev == rank) {
                        counter++;
                    } else if (counter > 0) {
                        merges += 1 + counter;
                        counter = 0;
                    }
                    prev = rank;
                }
            }
            if (counter > 0) {
                merges += 1 + counter;
            }

            float monotonicity_left = 0;
            float monotonicity_right = 0;
            for (int i = 1; i < 4; ++i) {
                if (line[i - 1] > line[i]) {
                    monotonicity_left += pow(line[i - 1], SCORE_MONOTONICITY_POWER) - pow(line[i], SCORE_MONOTONICITY_POWER);
                } else {
                    monotonicity_right += pow(line[i], SCORE_MONOTONICITY_POWER) - pow(line[i - 1], SCORE_MONOTONICITY_POWER);
                }
            }

            heur_score_table[row] = SCORE_LOST_PENALTY
                    + SCORE_EMPTY_WEIGHT * empty
                    + SCORE_MERGES_WEIGHT * merges
                    - SCORE_MONOTONICITY_WEIGHT * Min(monotonicity_left, monotonicity_right)
                    - SCORE_SUM_WEIGHT * sum;

            // execute a move to the left
            for (int i = 0; i < 3; ++i) {
                int j;
                for (j = i + 1; j < 4; ++j) {
                    if (line[j] != 0) {
                        break;
                    }
                }
                if (j == 4) {
                    break; // no more tiles to the right
                }
                if (line[i] == 0) {
                    line[i] = line[j];
                    line[j] = 0;
                    i--; // retry this entry
                } else if (line[i] == line[j] && line[i] != 0xf) {
                    line[i]++;
                    line[j] = 0;
                }
            }

            //print_board(row);
            int result = (line[0] | (line[1] << 4) | (line[2] << 8) | (line[3] << 12));
            //System.out.println(line[0]+":"+line[1]+":"+line[2]+":"+line[3]+"="+result);
            //print_board(result);
            int rev_result = reverse_row(result);
            //print_board(rev_result);
            int rev_row = reverse_row(row);
            //print_board(rev_row);
            //System.out.println("===========");
            row_left_table[ row] = (row ^ result);
            row_right_table[rev_row] = (rev_row ^ rev_result);
            col_up_table[ row] = unpack_col(row) ^ unpack_col(result);
            col_down_table[rev_row] = unpack_col(rev_row) ^ unpack_col(rev_result);
        }
    }

    //
    private static float Min(float monotonicity_left, float monotonicity_right) {
        if (monotonicity_left < monotonicity_right) {
            return monotonicity_left;
        } else {
            return monotonicity_right;
        }
    }

    //将行拆成刄1�7
    public static long unpack_col(int row) {
        long tmp = row;
        return (tmp | (tmp << 12L) | (tmp << 24L) | (tmp << 36L)) & COL_MASK;
    }

    //反转衄1�7
    public static int reverse_row(int row) {
        return ((row >>> 12) | ((row >> 4) & 0x00F0) | ((row << 4) & 0x0F00) | (row << 12) & 0xF000);
    }

    //输出矩阵
    static void print_board(long board) {
        long pb = board;
        int i, j;
        for (i = 0; i < 4; i++) {
            for (j = 0; j < 4; j++) {
                int x = (int) (pb & 0xfL);
                System.out.printf("%c", "0123456789abcdef".charAt(x));
                pb >>= 4;
            }
            System.out.println("");
        }
        System.out.println("");
    }

}
