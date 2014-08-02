package server ;


public class GameManager {
	//public StorageManager store;
	private int size;
	private final static int startTiles = 2;
	//private InputManager input;
	private Grid grid;
//	private int timeCount;
	private int tilePosition;

	public int ioScore;
        
        private String preGrid;
	
	/**
	 * 构建指定大小的game manager
	 * @param size
	 */
	public GameManager(int size) {
		this.size = size;
		this.grid = new Grid(size);
		ioScore = 0;
	}
	/**
	 * 初始化所有方格，并添加初始的数字
	 */
	public void ioStart() {
		//store.init();
		grid.init();
		
		ioScore=0;
		addStartTiles();
	}
	
        
    /**
	 * 获得本次操作前的状态
	 * @return 符号化得到的操作前状态字符串
    */            
    public String getPreGrid() {
        return preGrid;
    }
    
	/**
	 * 将方阵符号化，用于输出
	 * @return 符号化得到的字符串
	 */
	public String getGrid() {
//		StringBuffer msg = new StringBuffer();
//		for(int y = 0; y < grid.size; y++) {
//			for(int x = 0; x < grid.size; x++) {
//				if(grid.gridTile[x][y] == null) {
//					msg.append(" .");
//				} else {
//					msg.append((grid.gridTile[x][y].value) + ".");
//				}
//			}
//		}
		return grid.getGrid();
	}
	
	/**
	 * 返回新添加的数字的位置信息
	 * @return 位置信息
	 */
	public int getNewTile() {
		return tilePosition;
	}
	
	/**
	 * 按照向量方向模拟一次滑动，返回是否有成功的移动操作
	 * 这里有个副操作，就是
	 * @param vector 方向向量
	 * @return 是否有成功的移动操作
	 */
	public boolean ioMove(Position vector) {
		//取消所有方格已合并过的标志，以便继续合并                      
		prepareTiles();
		Position cell;
		Tile tTile;
		boolean isMove=false;
                
                //记录移动前的状态和移动方向
                String statusPreMv = grid.getGrid();
		/**
		 *     new Position(0,-1),         //up
			   new Position(1,0),          //r
			   new Position(0,1),          //d
			   new Position(-1,0) };       //l
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
			ox=size-1;
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
				tTile = grid.getTile(cell);
				if (tTile != null) { //如果内容不为空
					Position[] farPos = findFarthestPosition(cell,vector);
					Tile next = grid.getTile(farPos[1]);
					
					if (next != null && next.value == tTile.value && !next.ismerged) {
						//如果可用位置之后的那个方块能够合并，并且在本轮合并中没有被合并过
						//先生成一个合并过的方格数据
						Tile merged = new Tile(next.position, next.value << 1);
						//标记为合并过的
						merged.ismerged = true;
						//清除掉原先合并的数据
						grid.removeTile(next);
						//添加合并后的方格数据
						grid.insertTile(merged);
						//清除掉当前检测的点
						grid.removeTile(tTile);
						
						tTile = merged;			//tile.update?can instead?
						
					//	store.score+=merged.value;			
						//加分
						ioScore += merged.value;
					} else {
						//不能合并，直接移动当前方块
						moveTile(tTile,farPos[0]);
					}
					if(!positionsEqual(tTile.position,cell)) {
						//如果移动成功，记录下来
						isMove=true;
					}
				}
			}
		}
		//如果有移动成功记录，随机添加一个数字
		if(isMove) {                       
			addRandomTile();
                        preGrid = statusPreMv;
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * 添加初始数字
	 */
	private final void addStartTiles() {
		for(int i=0;i<startTiles;i++) {
			addRandomTile();
		}
	}
	
	/**
	 * 随机添加一个数字，返回是否添加成功
	 * @return 是否添加成功
	 */
	public boolean addRandomTile() {
		if(grid.cellsAvailable()) {
			int value = (Math.random() < 0.9) ? 2 : 4;
			Tile tile = new Tile(grid.randomAvailableCell(), value);
			grid.insertTile(tile);
			//input.recordNewtile(tile);
			tilePosition = tile.position.x + tile.position.y * size;
			return true;
		} else {
			return false;
		}
	}
	
/*	public void actuate() throws IOException{																//检查状态
		if(store.bestCore<store.score)
		{
			store.setBestScore();
		}
		if(store.keepPlaying==false){
			store.clearGameState();	
			input.sendOver(this);
			return;
		}
		else if(!store.isOver)
		{
			input.sendLayout(grid);	
		}
		if(store.isOver)
		{
			store.clearGameState();	
			if(store.keepPlaying==false)
			{
				store.clearGameState();	
				input.sendOver(this);
				return;
			}
			else
				return;
		}		
	}*/
	
	/**
	 * 移动数字
	 * @param tile 数字
	 * @param cell 位置
	 */
	private void moveTile(Tile tile,Position cell){
		grid.gridTile[tile.position.x][tile.position.y] = null;
		grid.gridTile[cell.x][cell.y] = tile;
		tile.updatePosition(cell);
	}
	
	/*public void move(Position vec){
		
		if(store.isOver||!store.keepPlaying)
			return;
		store.moved=false;
		prepareTiles();
		Position cell;
		Tile tTile;
		for(int x=0;x<size;x++){
			for(int y=0;y<size;y++){
				cell=new Position(x,y);
				tTile=grid.getTile(cell);
				if(tTile!=null)
				{
		
					Position[] farPos=findFarthestPosition(cell,vec);
					Tile next=grid.getTile(farPos[1]);
					
					if(next!=null&&next.value==tTile.value&&!next.ismerged){
						Tile merged=new Tile(next.position,next.value *2);
						merged.ismerged=true;
						grid.removeTile(next);
						grid.insertTile(merged);
						grid.removeTile(tTile);
						
						tTile=merged;			//tile.update?can instead?
						
						store.score+=merged.value;			
						
					}
					else
					{
						moveTile(tTile,farPos[0]);
					}
					if(!positionsEqual(tTile.position,cell))
					{
						store.moved=true;
					}
				}
			}
		}
		if(store.moved){
			addRandomTile();
			
			if(movesAvailable()){
				input.sendLayout(grid);
			}
			else{
				System.out.println("gridout:");
				for(int x=0;x<grid.size;x++)
				{
					for(int y=0;y<grid.size;y++)
					{
						if(grid.gridTile[x][y]!=null)
						    System.out.print(grid.gridTile[x][y].value+"	");
						
					}
					System.out.println("");
				}
				store.isOver=true;
			}
			
		}
		else											///??????????
		{
			//addRandomTile();
			
			if(movesAvailable()){
				input.sendLayout(grid);
			}
			else{
				
				System.out.println("gridout:");
				for(int x=0;x<grid.size;x++)
				{
					for(int y=0;y<grid.size;y++)
					{
						if(grid.gridTile[x][y]!=null)
						    System.out.print(grid.gridTile[x][y].value+"\t");
						
					}
					System.out.println("");
				}
				store.isOver=true;
			}
		}
	}*/
	
	/**
	 * 取消所有方格的合并过的标志
	 */
	public void prepareTiles(){
		grid.unMerged();
	}

	/**
	 * 判断两个位置是否重叠
	 * @param first 第一个位置
	 * @param second 第二个位置
	 * @return 是否重叠
	 */
	private final boolean positionsEqual(Position first, Position second) {
		return first.x == second.x && first.y == second.y;
	}
	
	/**
	 * 获取向量方向上，距离当前位置最远的可用方格位置，以及后一个方格位置。
	 * @param cell 当前位置
	 * @param vector 方向向量
	 * @return 二元素位置数组
	 */
	private Position[] findFarthestPosition(Position cell, Position vector){
		Position previous;
		do {
			previous = cell;
			cell = new Position(previous.x + vector.x, previous.y + vector.y);
		} while (grid.withinBounds(cell) && grid.cellAvailable(cell));
		Position preAndNext[]=new Position[2];
		preAndNext[0] = previous;
		preAndNext[1] = cell;
		return preAndNext;
	}
	
	public final boolean movesAvailable() {
		return (grid.cellsAvailable() || tileMatchesAvailable());
	}
	
	public boolean tileMatchesAvailable() {
		for(int x=0;x<size;x++) {
			for(int y=0;y<size;y++) {
				Tile tile=null;
				tile=grid.getTile(new Position(x,y));
				if(tile!=null) {
					Tile tileLink=null;					
					tileLink=grid.getTile(new Position(x+1,y));
					if(tileLink!=null&&tileLink.value==tile.value)
						return true;
					tileLink=grid.getTile(new Position(x,y+1));
					if(tileLink!=null&&tileLink.value==tile.value)
						return true;
				}
			}
		}
		return false;
	}
	
	
	/*public static void main(String[] args) throws IOException{
		InputManager inputManager=new InputManager(4,1);
		inputManager.start();
		
	}*/
	
}
