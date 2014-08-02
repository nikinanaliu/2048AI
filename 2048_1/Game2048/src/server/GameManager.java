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
	 * ����ָ����С��game manager
	 * @param size
	 */
	public GameManager(int size) {
		this.size = size;
		this.grid = new Grid(size);
		ioScore = 0;
	}
	/**
	 * ��ʼ�����з��񣬲���ӳ�ʼ������
	 */
	public void ioStart() {
		//store.init();
		grid.init();
		
		ioScore=0;
		addStartTiles();
	}
	
        
    /**
	 * ��ñ��β���ǰ��״̬
	 * @return ���Ż��õ��Ĳ���ǰ״̬�ַ���
    */            
    public String getPreGrid() {
        return preGrid;
    }
    
	/**
	 * ��������Ż����������
	 * @return ���Ż��õ����ַ���
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
	 * ��������ӵ����ֵ�λ����Ϣ
	 * @return λ����Ϣ
	 */
	public int getNewTile() {
		return tilePosition;
	}
	
	/**
	 * ������������ģ��һ�λ����������Ƿ��гɹ����ƶ�����
	 * �����и�������������
	 * @param vector ��������
	 * @return �Ƿ��гɹ����ƶ�����
	 */
	public boolean ioMove(Position vector) {
		//ȡ�����з����Ѻϲ����ı�־���Ա�����ϲ�                      
		prepareTiles();
		Position cell;
		Tile tTile;
		boolean isMove=false;
                
                //��¼�ƶ�ǰ��״̬���ƶ�����
                String statusPreMv = grid.getGrid();
		/**
		 *     new Position(0,-1),         //up
			   new Position(1,0),          //r
			   new Position(0,1),          //d
			   new Position(-1,0) };       //l
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
				//��ȡ��ǰ��ⷽ��
				cell = new Position(x, y);
				//��ȡ�����е�����
				tTile = grid.getTile(cell);
				if (tTile != null) { //������ݲ�Ϊ��
					Position[] farPos = findFarthestPosition(cell,vector);
					Tile next = grid.getTile(farPos[1]);
					
					if (next != null && next.value == tTile.value && !next.ismerged) {
						//�������λ��֮����Ǹ������ܹ��ϲ��������ڱ��ֺϲ���û�б��ϲ���
						//������һ���ϲ����ķ�������
						Tile merged = new Tile(next.position, next.value << 1);
						//���Ϊ�ϲ�����
						merged.ismerged = true;
						//�����ԭ�Ⱥϲ�������
						grid.removeTile(next);
						//��Ӻϲ���ķ�������
						grid.insertTile(merged);
						//�������ǰ���ĵ�
						grid.removeTile(tTile);
						
						tTile = merged;			//tile.update?can instead?
						
					//	store.score+=merged.value;			
						//�ӷ�
						ioScore += merged.value;
					} else {
						//���ܺϲ���ֱ���ƶ���ǰ����
						moveTile(tTile,farPos[0]);
					}
					if(!positionsEqual(tTile.position,cell)) {
						//����ƶ��ɹ�����¼����
						isMove=true;
					}
				}
			}
		}
		//������ƶ��ɹ���¼��������һ������
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
	 * ��ӳ�ʼ����
	 */
	private final void addStartTiles() {
		for(int i=0;i<startTiles;i++) {
			addRandomTile();
		}
	}
	
	/**
	 * ������һ�����֣������Ƿ���ӳɹ�
	 * @return �Ƿ���ӳɹ�
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
	
/*	public void actuate() throws IOException{																//���״̬
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
	 * �ƶ�����
	 * @param tile ����
	 * @param cell λ��
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
	 * ȡ�����з���ĺϲ����ı�־
	 */
	public void prepareTiles(){
		grid.unMerged();
	}

	/**
	 * �ж�����λ���Ƿ��ص�
	 * @param first ��һ��λ��
	 * @param second �ڶ���λ��
	 * @return �Ƿ��ص�
	 */
	private final boolean positionsEqual(Position first, Position second) {
		return first.x == second.x && first.y == second.y;
	}
	
	/**
	 * ��ȡ���������ϣ����뵱ǰλ����Զ�Ŀ��÷���λ�ã��Լ���һ������λ�á�
	 * @param cell ��ǰλ��
	 * @param vector ��������
	 * @return ��Ԫ��λ������
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
