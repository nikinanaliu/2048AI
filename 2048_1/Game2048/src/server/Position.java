package server ;
public class Position {
	
	public int x;
	public int y;
	
	public Position(int x,int y){
		this.x=x;
		this.y=y;
	}
	
	public Position clone() {
		return new Position(x, y);
	}

    @Override
    public String toString() {
        return String.format("(%d,%d)", x, y);
    }
        
    public String getDirectString()
    {
        if(x == 0)
        {
            if(y == 1) return "down";
            else return "up";
        }
        else if(x == 1) return "right";
        else return "left";
    }
}
	