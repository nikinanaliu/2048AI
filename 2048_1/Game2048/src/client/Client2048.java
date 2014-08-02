package client;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import new2048.NewAI;


import server.Grid;
import server.Position;
import server.Tile;

/**
 * 
 *  
 */
public class Client2048 extends Client2048Framwork implements Runnable {
	//FIXME 这里的大小先写死，到时候可以动态获取
	private static final int SIZE = 4 ;
	private static final String fmt = "%-6s" ;
        
        private int deep = 4;
        private double  smoothWeight = 0.1;
	private double	mono2Weight  = 1.0;
	private double	emptyWeight  = 2.7;
	private double	maxWeight    = 1.0;
	
	public Client2048()
        {
            this("localhost", 9000, 4);
        }
        
        public Client2048(String ipAddr, int port, int deep)
        {
            this.host = ipAddr;
            this.port = port;
            this.deep = deep;
        }
        
        public Client2048(String ipAddr, int port, int deep, double smoothWeight, double mono2Weight, double emptyWeight, double maxWeight)
        {
            this.host = ipAddr;
            this.port = port;
            this.deep = deep;
            this.smoothWeight = smoothWeight;
            this.mono2Weight = mono2Weight;
            this.emptyWeight = emptyWeight;
            this.maxWeight = maxWeight;
        }
        
        public void run() {
            start();
        }
        
        public void close()
        {
            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
	
	/**
	 * 每一次判断，首先生成四个可能性
	 * 过滤掉不可移动的可能性
	 * 生成最坏结果来添加到格子当中
	 * 继续生成四个可能性
	 * 判断出最高的分数，然后按照判断分数最高的那个来移动
	 */
	@Override
	public void solve() throws IOException {
		//生成原始表格
		//Grid orgGrid = getOrgGrid();
                //orgGrid.showGrid();
		try {
			//getBestPoss(orgGrid);
			//从最好的结果中获取第一个操作然后返回服务器
			//int oper = indexPossible.getOpers().get(0) ;
                        int oper = NewAI.findBestMove(getCurrentGrid());
                        //int oper = NewAI.find_best_move(orgGrid);
                        //System.out.println(oper);
			if(!move(oper))
				System.out.println("结果无效！");
		} catch (Exception e) {
			//e.printStackTrace();
			move(backOper++%4) ;
		}
	}
	int backOper = 0 ;
	private Possiblity indexPossible ;
	private Possiblity getBestPoss(Possiblity orgPoss , int deep) throws Exception {
		/**
		 * 如果是计算的最底层
		 * 		那么就返回唯一的可能性
		 * 如果是计算的中间值
		 * 		那么就继续生成可能性，然后交给下层计算
		 */
		Possiblity index = null ;
		if(deep==0){//如果是最底层
			index = orgPoss ;
		}else{//如果不是，那么就加入干扰块继续交给下层
			int subDeep = --deep ;
			for(int i = 0 ;i < 4 ; i ++){
				Possiblity p = new Possiblity(orgPoss, smoothWeight, mono2Weight, emptyWeight, maxWeight) ;
				if(p.move(i)){
					if(p.getWight()>indexPossible.getWight())
						indexPossible = p ;
					BadBoy.addBadTile(p);
					getBestPoss(p,subDeep) ;
				}
			}//end for
		}//end else
		
		return index ;
	}
	
	private Possiblity getBestPoss(Grid orgPoss) throws Exception {
		indexPossible = new Possiblity(orgPoss) ;
		Possiblity tmp =getBestPoss(indexPossible, deep) ;
		return tmp;
	}

	private Grid getOrgGrid() {
		long[][] data = getCurrentGrid() ;
		Grid orgPoss = new Grid(data.length) ;
		for(int i = 0 ; i < data.length ; i ++){
			cell:
				for(int j=0 ; j < data.length ; j++ ){
					if(data[i][j]==0) continue cell;
					Tile t = new Tile(new Position(j,i) ,data[i][j]) ;
					orgPoss.insertTile(t);
				}
		}
		return orgPoss ;
	}
	
}

/**
 * 负责根据当前的形式来加入一个最恶心的位置
 * @author lost
 */
class BadBoy {
	public static void addBadTile(Possiblity p){
		//FIXME 这里应该是选取一个最坏可能性，而不是固定一个
		p.getGrid().insertTile(new Tile(p.getGrid().availableCells()[0],2));
	}
}