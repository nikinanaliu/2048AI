package server;

import client.Contants;
import java.util.Collections;

public class Grid {

    public Tile[][] gridTile;
    public int tilesCount;
    public int size;

    public static Position[][] snakePosList = null;

    /**
     * ������������ģ��һ�λ����������Ƿ��гɹ����ƶ�����
     *
     * @param vector ��������
     * @return �Ƿ��гɹ����ƶ�����
     */
    public boolean move(Position vector) {
        //ȡ�����з����Ѻϲ����ı�־���Ա�����ϲ�            
        unMerged();
        Position cell;
        Tile tTile;
        boolean isMove = false;
        /**
         * new Position(0,-1), //up new Position(1,0), //r new Position(0,1),
         * //d new Position(-1,0) }; //l
         */
        //��ʼ���λ��
        int ox;
        int oy;
        //�������λ��
        int dx;
        int dy;
        //��������λ�õı仯��
        int xx;
        int yy;
        if (vector.x != 1) {
            ox = 0;
            dx = size;
            xx = 1;
        } else {
            ox = size - 1;
            dx = -1;
            xx = -1;
        }
        if (vector.y != 1) {
            oy = 0;
            dy = size;
            yy = 1;
        } else {
            oy = size - 1;
            dy = -1;
            yy = -1;
        }
        for (int x = ox; x != dx; x += xx) {
            for (int y = oy; y != dy; y += yy) {
                //��ȡ��ǰ��ⷽ��
                cell = new Position(x, y);
                //��ȡ�����е�����
                tTile = getTile(cell);
                if (tTile != null) { //������ݲ�Ϊ��
                    Position[] farPos = findFarthestPosition(cell, vector);
                    Tile next = getTile(farPos[1]);

                    if (next != null && next.value == tTile.value && !next.ismerged) {
						//�������λ��֮����Ǹ������ܹ��ϲ��������ڱ��ֺϲ���û�б��ϲ���
                        //������һ���ϲ����ķ�������
                        Tile merged = new Tile(next.position, next.value << 1);
                        //���Ϊ�ϲ�����
                        merged.ismerged = true;
                        //�����ԭ�Ⱥϲ�������
                        removeTile(next);
                        //��Ӻϲ���ķ�������
                        insertTile(merged);
                        //�������ǰ���ĵ�
                        removeTile(tTile);

                        tTile = merged;			//tile.update?can instead?

                        //	store.score+=merged.value;			
                    } else {
                        //���ܺϲ���ֱ���ƶ���ǰ����
                        moveTile(tTile, farPos[0]);
                    }
                    if (!positionsEqual(tTile.position, cell)) {
                        //����ƶ��ɹ�����¼����
                        isMove = true;
                    }
                }
            }
        }
        return isMove;
    }

    public Grid clone() {
        Grid g = new Grid(this.size);
        for (int rowIndex = 0; rowIndex < this.size; rowIndex++) {
            for (int colIndex = 0; colIndex < this.size; colIndex++) {
                Tile indexTile = this.gridTile[rowIndex][colIndex];
                indexTile = indexTile == null ? null : new Tile(indexTile);
                g.insertTile(indexTile);
            }
        }
        return g;
    }

    public Position[] getSnakePositionList(int ox, int oy, int rowDir, int colDir, int dr) {
        Position[] ret = new Position[size * size];
        int x = ox, y = oy;
        int dx = rowDir;
        int dy = colDir;
        for (int i = 0; i < size * size; i++) {
            ret[i] = new Position(x, y);
            if (dr == 1) {
                if (!withinBounds(x, y + dx)) {
                    dx = 0 - dx;
                    x += dy;
                } else {
                    y += dx;
                }
            } else if (dr == 2) {
                if (!withinBounds(x + dy, y)) {
                    dy = 0 - dy;
                    y += dx;
                } else {
                    x += dy;
                }
            }
        }

        return ret;
    }

    /**
     * ��ȡ���������ϣ����뵱ǰλ����Զ�Ŀ��÷���λ�ã��Լ���һ������λ�á�
     *
     * @param cell ��ǰλ��
     * @param vector ��������
     * @return ��Ԫ��λ������
     */
    private Position[] findFarthestPosition(Position cell, Position vector) {
        Position previous;
        do {
            previous = cell;
            cell = new Position(previous.x + vector.x, previous.y + vector.y);
        } while (withinBounds(cell) && cellAvailable(cell));
        Position preAndNext[] = new Position[2];
        preAndNext[0] = previous;
        preAndNext[1] = cell;
        return preAndNext;
    }

    /**
     * �ж�����λ���Ƿ��ص�
     *
     * @param first ��һ��λ��
     * @param second �ڶ���λ��
     * @return �Ƿ��ص�
     */
    private final boolean positionsEqual(Position first, Position second) {
        return first.x == second.x && first.y == second.y;
    }

    private void moveTile(Tile tile, Position cell) {
        gridTile[tile.position.x][tile.position.y] = null;
        gridTile[cell.x][cell.y] = tile;
        tile.updatePosition(cell);
    }

    public Grid(int size) {
        this.size = size;
        gridTile = new Tile[size][size];
        init();
    }

    /**
     * ��ʼ�����з���Ϊ��
     */
    public void init() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                gridTile[i][j] = null;
            }
        }
        tilesCount = 0;
    }

    /**
     * �������п��÷����λ�ü���
     *
     * @return ���÷����λ�ü���
     */
    public Position[] availableCells() {
        int i = 0;
        Position[] list = new Position[size * size - tilesCount];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                Position temp = new Position(x, y);
                if (getTile(temp) == null) {
                    list[i++] = temp;
                }
            }
        }
        return list;
    }

    /**
     * �������һ�����÷����λ����Ϣ
     *
     * @return һ������Ŀ��÷����λ����Ϣ
     */
    public Position randomAvailableCell() {
        if (tilesCount >= size * size) {
            return null;
        } else {
            Position[] avilableCells = availableCells();
            return avilableCells[(int) (Math.random() * avilableCells.length)];
        }
    }

    /**
     * ����һ�������е����ݡ����λ����Ϣ�����߽磬����null��
     *
     * @param p λ��
     * @return һ�������е����ݡ����λ����Ϣ�����߽磬����null��
     */
    public Tile getTile(Position p) {
        if (withinBounds(p)) {
            return gridTile[p.x][p.y];
        } else {
            return null;
        }
    }

    /**
     * ���һ������
     *
     * @param tile
     */
    public void insertTile(Tile tile) {
        if (tile == null) {
            return;//null�����
        }
        gridTile[tile.position.x][tile.position.y] = tile;
        tilesCount++;
    }

    /**
     * ���ض�λ�õĿո����
     *
     * @param tile
     */
    public void removeTile(Tile tile) {
        gridTile[tile.position.x][tile.position.y] = null;
        tilesCount--;
    }

    /**
     * �ж��Ƿ��п��з���
     *
     * @return �Ƿ��п��з���
     */
    public final boolean cellsAvailable() {
        return (tilesCount < size * size);
    }

    /**
     * �ж�һ��λ�õķ����Ƿ����
     *
     * @param pos λ��
     * @return �Ƿ����
     */
    public final boolean cellAvailable(Position pos) {
        return (gridTile[pos.x][pos.y] == null);
    }

    /**
     * �ж�һ��λ���Ƿ��ڱ߽���
     *
     * @param p λ��
     * @return �Ƿ��ڱ߽���
     */
    public final boolean withinBounds(Position p) {
        return ((p.x < size) && (p.x >= 0) && (p.y < size) && (p.y >= 0));
    }

    public final boolean withinBounds(int x, int y) {
        return ((x < size) && (x >= 0) && (y < size) && (y >= 0));
    }

    /**
     * ȡ�����з�����Ѻϲ����ı�־
     */
    public void unMerged() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (gridTile[i][j] != null) {
                    gridTile[i][j].ismerged = false;
                }
            }
        }
    }

    /**
     * ����ƽ����
     *
     * @return
     */
    public double smoothness() {
        double res = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (gridTile[i][j] != null) {
                    for (int dir = 1; dir <= 2; dir++) {
                        double value = Math.log(gridTile[i][j].value) / Math.log(2);
                        Tile target = getValiableTile(i, j, dir);
                        if (target != null) {
                            double targetValue = Math.log(target.value) / Math.log(2);
                            res -= Math.abs(value - targetValue);
                        }//end null if
                    }//end for 
                }//end if
            }//end for
        }//end for
        return res;
    }

    public double log2(double val) {
        return Math.log(val) / Math.log(2);
    }

    /**
     * ��������ƽ����
     *
     * @return
     */
    public double snakeSmoothness() {

        if (snakePosList == null) {
            snakePosList[0] = getSnakePositionList(0, 0, 1, 1, 1);
            snakePosList[1] = getSnakePositionList(0, 0, 1, 1, 2);
            snakePosList[2] = getSnakePositionList(0, size - 1, -1, 1, 1);
            snakePosList[3] = getSnakePositionList(0, size - 1, -1, 1, 2);
        }

        double max = 0.0;
        for (int i = 0; i < 4; i++) {
            double w = 0;
            Position[] curDir = snakePosList[i];
            for (int j = 0; j < curDir.length; j++) {
                Position curPos = curDir[j];
                Tile curTile = gridTile[curPos.x][curPos.y];
                if (curTile != null) {
                    double curVal = log2(curTile.value);
                    if (j + 1 < curDir.length) {
                        Position nextPos = curDir[j + 1];
                        Tile nextTile = gridTile[nextPos.x][nextPos.y];
                        if (nextTile != null) {
                            double nextVal = log2(nextTile.value);
                            w += (curVal - nextVal);
                        } else {
                            break;
                        }
                    }
                }
            }

            if (i == 0 || max < w) {
                max = w;
            }
        }

        return max;
    }

    private Tile getValiableTile(int i, int j, int dir) {
        Position p = findFarthestPosition(new Position(i, j), Contants.vectors[dir])[1];
        return getTile(p);
    }

    private boolean cellOccupied(int x, int y) {
        return this.gridTile[x][y] != null;
    }

    /**
     * ����û�����ף������ȸ���js��ʵ��
     *
     * @return
     */
    public double monotonicity2() {
        // scores for all four directions
        int[] totals = new int[4];

        // up/down direction
        for (int x = 0; x < this.size; x++) {
            int current = 0;
            int next = current + 1;
            while (next < this.size) {
                while (next < 4 && !(this.gridTile[x][next] != null)) {
                    next++;
                }
                if (next >= this.size) {
                    next--;
                }
                double currentValue = cellOccupied(x, current)
                        ? Math.log(this.gridTile[x][current].value) / Math.log(2)
                        : 0;
                double nextValue = cellOccupied(x, next)
                        ? Math.log(this.gridTile[x][next].value) / Math.log(2)
                        : 0;
                if (currentValue > nextValue) {
                    totals[0] += nextValue - currentValue;
                } else if (nextValue > currentValue) {
                    totals[1] += currentValue - nextValue;
                }
                current = next;
                next++;
            }
        }

        // left/right direction
        for (int y = 0; y < this.size; y++) {
            int current = 0;
            int next = current + 1;
            while (next < 4) {
                while (next < 4 && !(this.gridTile[next][y] != null)) {
                    next++;
                }
                if (next >= 4) {
                    next--;
                }
                double currentValue = cellOccupied(current, y)
                        ? Math.log(this.gridTile[current][y].value) / Math.log(2)
                        : 0;
                double nextValue = cellOccupied(next, y)
                        ? Math.log(this.gridTile[next][y].value) / Math.log(2)
                        : 0;
                if (currentValue > nextValue) {
                    totals[2] += nextValue - currentValue;
                } else if (nextValue > currentValue) {
                    totals[3] += currentValue - nextValue;
                }
                current = next;
                next++;
            }
        }

        return Math.max(totals[0], totals[1]) + Math.max(totals[2], totals[3]);
    }

    /**
     * ��ȡ��������ֵ
     *
     * @return
     */
    public double maxValue() {
        long max = 0;
        for (Tile[] row : gridTile) {
            for (Tile t : row) {
                if (t != null && t.value > max) {
                    max = t.value;
                }
            }
        }
        return max;
    }

    private static final String fmt = "%-6s";

    public void showGrid() {
        System.out.println();
        for (int rowIndex = 0; rowIndex < gridTile.length; rowIndex++) {
            for (int colIndex = 0; colIndex < gridTile.length; colIndex++) {
                long value = 0;
                if (gridTile[colIndex][rowIndex] != null) {
                    value = gridTile[colIndex][rowIndex].value;
                }
                System.out.print(String.format(fmt, value + ""));
            }
            System.out.println();
        }
    }

    /**
     * ��������Ż����������
     *
     * @return ���Ż��õ����ַ���
     */
    public String getGrid() {
        StringBuffer msg = new StringBuffer();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (gridTile[x][y] == null) {
                    msg.append(" .");
                } else {
                    msg.append(gridTile[x][y].value + ".");
                }
            }
        }
        return msg.toString();
    }

    public long getLong() {
        long ret = 0;
        int x;
        for (int i = gridTile.length - 1; i >= 0; i--) {
            for (int j = gridTile[i].length - 1; j >= 0; j--) {
                x = gridTile[j][i].value == 0 ? 0 : (int) (Math.log((double)gridTile[j][i].value) / Math.log(2));
                ret = ret << 4;
                ret = ret | x;
            }
        }
        return ret;
    }
}
