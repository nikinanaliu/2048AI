package server;

import client.Contants;
import java.util.Collections;

public class Grid {

    public Tile[][] gridTile;
    public int tilesCount;
    public int size;

    public static Position[][] snakePosList = null;

    /**
     * 按照向量方向模拟一次滑动，返回是否有成功的移动操作
     *
     * @param vector 方向向量
     * @return 是否有成功的移动操作
     */
    public boolean move(Position vector) {
        //取消所有方格已合并过的标志，以便继续合并            
        unMerged();
        Position cell;
        Tile tTile;
        boolean isMove = false;
        /**
         * new Position(0,-1), //up new Position(1,0), //r new Position(0,1),
         * //d new Position(-1,0) }; //l
         */
        //起始检测位置
        int ox;
        int oy;
        //结束检测位置
        int dx;
        int dy;
        //检测过程中位置的变化量
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
                //获取当前检测方格
                cell = new Position(x, y);
                //获取方格中的内容
                tTile = getTile(cell);
                if (tTile != null) { //如果内容不为空
                    Position[] farPos = findFarthestPosition(cell, vector);
                    Tile next = getTile(farPos[1]);

                    if (next != null && next.value == tTile.value && !next.ismerged) {
						//如果可用位置之后的那个方块能够合并，并且在本轮合并中没有被合并过
                        //先生成一个合并过的方格数据
                        Tile merged = new Tile(next.position, next.value << 1);
                        //标记为合并过的
                        merged.ismerged = true;
                        //清除掉原先合并的数据
                        removeTile(next);
                        //添加合并后的方格数据
                        insertTile(merged);
                        //清除掉当前检测的点
                        removeTile(tTile);

                        tTile = merged;			//tile.update?can instead?

                        //	store.score+=merged.value;			
                    } else {
                        //不能合并，直接移动当前方块
                        moveTile(tTile, farPos[0]);
                    }
                    if (!positionsEqual(tTile.position, cell)) {
                        //如果移动成功，记录下来
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
     * 获取向量方向上，距离当前位置最远的可用方格位置，以及后一个方格位置。
     *
     * @param cell 当前位置
     * @param vector 方向向量
     * @return 二元素位置数组
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
     * 判断两个位置是否重叠
     *
     * @param first 第一个位置
     * @param second 第二个位置
     * @return 是否重叠
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
     * 初始化所有方格为空
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
     * 返回所有可用方格的位置集合
     *
     * @return 可用方格的位置集合
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
     * 随机返回一个可用方格的位置信息
     *
     * @return 一个随机的可用方格的位置信息
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
     * 返回一个方格中的内容。如果位置信息超出边界，返回null。
     *
     * @param p 位置
     * @return 一个方格中的内容。如果位置信息超出边界，返回null。
     */
    public Tile getTile(Position p) {
        if (withinBounds(p)) {
            return gridTile[p.x][p.y];
        } else {
            return null;
        }
    }

    /**
     * 添加一个数字
     *
     * @param tile
     */
    public void insertTile(Tile tile) {
        if (tile == null) {
            return;//null则不添加
        }
        gridTile[tile.position.x][tile.position.y] = tile;
        tilesCount++;
    }

    /**
     * 将特定位置的空格清空
     *
     * @param tile
     */
    public void removeTile(Tile tile) {
        gridTile[tile.position.x][tile.position.y] = null;
        tilesCount--;
    }

    /**
     * 判断是否还有空闲方格
     *
     * @return 是否还有空闲方格
     */
    public final boolean cellsAvailable() {
        return (tilesCount < size * size);
    }

    /**
     * 判断一个位置的方格是否空闲
     *
     * @param pos 位置
     * @return 是否空闲
     */
    public final boolean cellAvailable(Position pos) {
        return (gridTile[pos.x][pos.y] == null);
    }

    /**
     * 判断一个位置是否在边界内
     *
     * @param p 位置
     * @return 是否在边界内
     */
    public final boolean withinBounds(Position p) {
        return ((p.x < size) && (p.x >= 0) && (p.y < size) && (p.y >= 0));
    }

    public final boolean withinBounds(int x, int y) {
        return ((x < size) && (x >= 0) && (y < size) && (y >= 0));
    }

    /**
     * 取消所有方格的已合并过的标志
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
     * 返回平滑度
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
     * 返回蛇形平滑度
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
     * 这里没看明白，不过先根据js来实现
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
     * 获取矩阵的最大值
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
     * 将方阵符号化，用于输出
     *
     * @return 符号化得到的字符串
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
