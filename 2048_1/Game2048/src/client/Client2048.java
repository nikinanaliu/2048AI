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
	//FIXME ����Ĵ�С��д������ʱ����Զ�̬��ȡ
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
	 * ÿһ���жϣ����������ĸ�������
	 * ���˵������ƶ��Ŀ�����
	 * ������������ӵ����ӵ���
	 * ���������ĸ�������
	 * �жϳ���ߵķ�����Ȼ�����жϷ�����ߵ��Ǹ����ƶ�
	 */
	@Override
	public void solve() throws IOException {
		//����ԭʼ���
		//Grid orgGrid = getOrgGrid();
                //orgGrid.showGrid();
		try {
			//getBestPoss(orgGrid);
			//����õĽ���л�ȡ��һ������Ȼ�󷵻ط�����
			//int oper = indexPossible.getOpers().get(0) ;
                        int oper = NewAI.findBestMove(getCurrentGrid());
                        //int oper = NewAI.find_best_move(orgGrid);
                        //System.out.println(oper);
			if(!move(oper))
				System.out.println("�����Ч��");
		} catch (Exception e) {
			//e.printStackTrace();
			move(backOper++%4) ;
		}
	}
	int backOper = 0 ;
	private Possiblity indexPossible ;
	private Possiblity getBestPoss(Possiblity orgPoss , int deep) throws Exception {
		/**
		 * ����Ǽ������ײ�
		 * 		��ô�ͷ���Ψһ�Ŀ�����
		 * ����Ǽ�����м�ֵ
		 * 		��ô�ͼ������ɿ����ԣ�Ȼ�󽻸��²����
		 */
		Possiblity index = null ;
		if(deep==0){//�������ײ�
			index = orgPoss ;
		}else{//������ǣ���ô�ͼ�����ſ���������²�
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
 * ������ݵ�ǰ����ʽ������һ������ĵ�λ��
 * @author lost
 */
class BadBoy {
	public static void addBadTile(Possiblity p){
		//FIXME ����Ӧ����ѡȡһ��������ԣ������ǹ̶�һ��
		p.getGrid().insertTile(new Tile(p.getGrid().availableCells()[0],2));
	}
}