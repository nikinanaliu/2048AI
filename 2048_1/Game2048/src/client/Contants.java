package client;

import server.Position;

public class Contants {
	public static int SIZE = 4 ;
	public static Position[] vectors = {  
			   new Position(0,-1),         //up
			   new Position(1,0),          //r
			   new Position(0,1),          //d
			   new Position(-1,0) };       //l
}