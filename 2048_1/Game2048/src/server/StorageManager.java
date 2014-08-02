package server ;
public class StorageManager {
//	
//	private static final int maxRound=65535;
//	public int round = 0;
//	public int[] history;
//	int score;
//	int bestCore;
//	boolean moved;
//	boolean isOver;
//	boolean keepPlaying;
//	
//	public StorageManager() {
//		history = new int[maxRound];
//		score = 0;
//		isOver = false;					//
//		keepPlaying = false;				//game keep
//		moved = false;					//
//	}
//	
//	public void setBestScore() {
//		bestCore = score;
//	}
//	
//	
//	public void clearGameState(){
//		history[round++] = score;
//		score = 0;
//		isOver = false;
//		moved = false;
//		if(keepPlaying==false){
//			 for(int i=0;i<round;i++){
// 	         	System.out.println(history[i]);
// 	     }
//		}
//	}
//
//	public void init(){
//		if(round<maxRound) {
//			score = 0;
//			isOver = false;
//			moved = true;
//		}
//		else {
//			System.out.println("round!!!");
//			keepPlaying = false;						///////output error
//		}
//	}
}
