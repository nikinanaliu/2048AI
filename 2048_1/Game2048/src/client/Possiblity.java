package client;

import java.util.ArrayList;

import server.Grid;
import server.Position;
import  static client.Contants.vectors;

/**
 * ��װһ�����ܵı仯
 * @author lost
 *
 */
public class Possiblity {
	private double  smoothWeight = 0.1;
	private double	mono2Weight  = 1.0;
	private double	emptyWeight  = 2.7;
	private double	maxWeight    = 1.0;
	private Grid grid ;
	//�洢����仯�����еĲ���
	private ArrayList<Integer> opers ;
	private Double  weightCache ; 
        
        public Possiblity(Possiblity p, double smoothWeight, double mono2Weight, double emptyWeight, double maxWeight) {
        this(p);
        this.smoothWeight = smoothWeight;
        this.mono2Weight = mono2Weight;
        this.emptyWeight = emptyWeight;
        this.maxWeight = maxWeight;
    }
	/**
	 * ���ݴ���Ŀ����ԣ���������һ��������
	 * @param p
	 */
	public Possiblity(Possiblity p){
		this(p.getGrid()) ;
		for(Integer i  : p.getOpers())
			opers.add(i) ;
	}
	public Possiblity(Grid grid){
		this.grid = grid.clone() ;
		opers = new ArrayList<Integer>() ;
	}
	public boolean move(int operCode){
		opers.add(operCode) ;
		return getGrid().move(vectors[operCode]) ;
	}

	public Grid getGrid() {
		return grid;
	}
	public void setGrid(Grid grid) {
		this.grid = grid;
	}
	public ArrayList<Integer> getOpers() {
		return opers;
	}
	public void setOpers(ArrayList<Integer> opers) {
		this.opers = opers;
	}
        
        public double getWight()
        {
            return getWight(this.smoothWeight, this.mono2Weight, this.emptyWeight, this.maxWeight);
        }
	/**
	 * ��ȡ��ǰ���ֿ����Ե�Ȩֵ
	 * @return
	 */
	public double getWight(double smoothWeight, double mono2Weight, double emptyWeight, double maxWeight) {
			int emptyCells = this.grid.availableCells().length;
			if(weightCache==null)
				weightCache= this.grid.smoothness() * smoothWeight
                                                //this.grid.snakeSmoothness() * smoothWeight
						+ this.grid.monotonicity2() * mono2Weight
						+ Math.log(emptyCells) * emptyWeight
						+ this.grid.maxValue() * maxWeight;
		 return weightCache ;
	}
}
