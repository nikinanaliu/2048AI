package server ;
public class Tile {
	
	public long value;
	public Position position;
	public boolean ismerged = false;
	
	public Tile(Position p, long value) {
		this.value=value;
		this.position=p;
//		this.ismerged=false;
	}
	
//	public Tile(Position p) {
//		this.value=0;
//		this.position=p;
//		this.ismerged=false;
//	}
	
	public Tile(Tile t) {
		this.value=t.value;
		this.position=t.position.clone();
//		this.ismerged=false;
	}
	
	public void updatePosition(Position p) {
		this.position = p.clone();
	}
	
}
