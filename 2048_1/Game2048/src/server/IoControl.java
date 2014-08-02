package server ;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import  static client.Contants.vectors;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IoControl implements Runnable {
		
	public int isFile = 0;
	
	/**
	 * @author WuYinan
	 * 增加cnt用于记录连接的Socket数
	 */
//	private static int cnt = 0;
	
	private GameManager game = null;
	private int size;
	private int timeCount;
	private int gameState;						//0,null;1 connect;2 ready;3 playing;4 round over;5time out 6 round out
	private boolean isTimeOut;
	private String[] states = {
			"null",									//0
			"connect",								//1
			"ready",								//2
			"playing",								//3
			"roundOver",                                                            //4
			"timeOut",								//5
			"roundOut"								//6
	};
	// tips				wrong order;    game over;    
	private int[] score;
	private int round;
	private final int maxRound = 99999;
	private String[] gridHistory;
	
//	/**
//	 * @author WuYinan
//	 * 把服务器Socket和端口号改为静态
//	 */
//	static private ServerSocket serverSocket;
//	static private int port = 9000;
	
	private Socket socket;
	private BufferedWriter pw = null;
	private BufferedReader br = null;
	
	private String[] recordBuff;
	private int recordCount;
        
        private boolean saveLog;
        private ArrayList<String> optLog;
        
        private String roundName;
	
	public IoControl(Socket s, int size, int timeCount, boolean saveLog, String rName) {
		/**
		historyMove = new int[historyMax-2];
		historyTile = new Tile[historyMax];
		stepCount = 0;
		tileCount = 0;
		*/
                this.socket = s;
		this.size = size;
		this.timeCount = timeCount;
                this.saveLog = saveLog;
		score = new int[maxRound];
		round = 0;
		gridHistory = new String[maxRound];
		isTimeOut = false;
                
                optLog = new ArrayList<String>(500);   
                
                roundName = rName;
	}
        
        public void run() {
            try {
                start();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
			
            int bestRound = 0;
            int bestScore = 0;

            for(int i = 0; i < round; i++) {
                    // System.out.println(io.score[i]);
                    // System.out.println(io.gridHistory[i]);
                    if(score[i] > bestScore) {
                            bestRound = i;
                            bestScore = score[i];
                    }
            }
            
            final int bRound = bestRound;
            final int bScore = bestScore;

            System.out.println("round!!!"+round+" best:"+bestRound+" score:"+bestScore);                     
                       
            // 写日志      
                new Thread(){
                    String logDir = "record";
                    String fileName = "0_round.txt";
                    String timeNow = Util.getTime();
                    final int roundNow = round;                      
                    final int sizeNow = size;
                    final int timeLim = timeCount;
                    String rName = roundName;

                    @Override
                    public void run() {
                            try {	//指定路径
                                    File dir = new File(logDir);
                                    if(!dir.exists()) dir.mkdirs();
                                    BufferedWriter bw = new BufferedWriter(new FileWriter(String.format("%s/%s", logDir, fileName),true));
                                    bw.write(String.format("%s,%s,%d,%d,%d,%d,%d", timeNow, rName, sizeNow, timeLim, roundNow, bRound, bScore));
                                    bw.newLine();
                                    bw.flush();
                                    bw.close();
                                    
                                    dir = new File(String.format("%s/%s", logDir, rName));
                                    if(!dir.exists()) dir.mkdirs();
                                    bw = new BufferedWriter(new FileWriter(String.format("%s/%s/%s", logDir, rName, fileName),true));
                                    for(int i = 0; i < round; i++)
                                    {
                                        bw.write(String.format("%08d,%06d", score[i], i));
                                        bw.newLine();
                                    }
                                    bw.flush();
                                    bw.close();
                            } catch (IOException e) {				
                                    e.printStackTrace();
                            }                       
                    }

                }.start(); 
        }
	
	public void init() {
		do {
			try {
				try {
					pw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));  
				} catch (IOException e) {
					e.printStackTrace();
				}  
				try {
					br = new BufferedReader(new InputStreamReader(socket.getInputStream()));   
				} catch (IOException e) {
					e.printStackTrace();
				}  
			} catch (Exception e) {
				e.printStackTrace();
			}
		} while (socket == null);
		
		gameState = 1;
		game = new GameManager(size);
		gameState = 2;
	}
	
	public void start() throws IOException {
		init();
		String msgClient = "";
		/**
		 * 挨个根据客户端来的方向键做出相应处理
		 */
		while((msgClient = getMsg()) !=  "") {
			try {
				process(Integer.parseInt(msgClient));
			} catch (NumberFormatException e) {
				System.out.println("Unable to parse int: " + msgClient);
			}
			if(gameState>4) {
				/**
				 * do something about output
				 */
				pw.write("state:"+states[gameState]+"\r\n");
				pw.flush();
				return;
			}
		}
		threadRecord();
	}
	
	/*
	 * recordBuff = new String[10000];
		recordCount = 0;
	 */
	
	public void process(int msgClient) throws IOException {
		boolean success = false;
		switch(msgClient) {
		case 0:	
			success = game.ioMove(vectors[0]);
			if(updateState(success, msgClient)>3) {
				pw.write("state:GameOver"+"\r\n");
				pw.flush();
			} else {
				pw.write("grid:"+game.getGrid()+"\r\n");
				pw.flush();
			}
			if (success) {
				updateRecord(game.getGrid()+game.getNewTile());
			}
			break;
		case 1:	
			success = game.ioMove(vectors[1]);
			if(updateState(success, msgClient)>3) {
				pw.write("state:GameOver"+"\r\n");
				pw.flush();
			} else {
				pw.write("grid:"+game.getGrid()+"\r\n");
				pw.flush();
			}
			if (success) {
				updateRecord(game.getGrid()+game.getNewTile());
			}		
			break;
		case 2:
			success = game.ioMove(vectors[2]);
			if(updateState(success, msgClient)>3) {
				pw.write("state:GameOver"+"\r\n");
				pw.flush();
			} else {
				pw.write("grid:"+game.getGrid()+"\r\n");
				pw.flush();
			}
			if (success) {
				updateRecord(game.getGrid()+game.getNewTile());
			}
			break;
		case 3:
			success = game.ioMove(vectors[3]);
			if(updateState(success, msgClient)>3) {
				pw.write("state:GameOver"+"\r\n");
				pw.flush();
			} else {
				pw.write("grid:"+game.getGrid()+"\r\n");
				pw.flush();
			}
			if (success) {
				updateRecord(game.getGrid()+game.getNewTile());
			}
			break;
		case 4:															//start
			//System.out.println("restart!!!");
			if(gameState == 2) {											//first
				System.out.println("game start");
				game.ioStart();
				initRecord(game.getGrid());
				gameState = 3;					
				timeStart();
				pw.write("state:"+states[gameState]+"\r\n");
				pw.flush();
//				pw.println("grid:"+game.getGrid());
			} else if(gameState < 5) {											//restart
				gridHistory[round] = game.getGrid();
				score[round] = game.ioScore;
                                newRound();
				if(round >= maxRound) {
					gameState = 6;
					pw.write("state:"+states[gameState]+"\r\n");
					pw.flush();
				} else {
					game.ioStart();
					threadRecord();
					initRecord(game.getGrid());
					if(!isTimeOut) {
						gameState = 3;
						pw.write("state:"+states[gameState]+"\r\n");
						pw.flush();
				//		pw.println("grid:"+game.getGrid());
					} else {
						gameState = 5;
					}
				}
			}
			break;
		case 5:									//getState(ready,playing,none,round over,game over)
			pw.write("state:"+states[gameState]+"\r\n");
			pw.flush();
			break;
		case 6:													//getscore[]
			pw.write("score:"+game.ioScore+"\r\n");
			pw.flush();
			break;
		case 7:													//getgrid
			pw.write("grid:"+game.getGrid()+"\r\n");
			pw.flush();
			break;
		case 8:													//getsize
			pw.write("size:"+size+"\r\n");
			pw.flush();
			break;
		default:
			pw.write("state:WrongOrder"+"\r\n");
			pw.flush();
		}
	}
	
	private void updateRecord(String g) {
		if(recordCount < 10000)
			recordBuff[recordCount++] = g;
		else {
			threadRecord();
			recordBuff[recordCount++] = g;
		}
	}
	
	private void initRecord(String g) {
		recordBuff = new String[10000];
		recordCount = 0;
		recordBuff[recordCount++] = g;
	}
	
	private void threadRecord() {
		new FileWrite(round,recordBuff).start();		
		initRecord(game.getGrid());
	}
	
	class FileWrite extends Thread{
		
		private int count;
		private String[] ss;
		
		public FileWrite(int count,String[] ss) {
			this.count = count;
			this.ss = ss;
		}
		public void run() {
			if(isFile!= 1)
				return;
			FileWriter fw = null;
			try {	//指定路径
				fw = new FileWriter("record"+count+".txt",true);
			} catch (IOException e) {
				
				e.printStackTrace();
			}  
			int i = 0;
			while(i<10000) {
				if(ss[i] == null)
					break;
				try {
					fw.write(ss[i]+"\r\n");
				} catch (IOException e) {
					
					e.printStackTrace();
				}				
				i++;
			}
			try {
				fw.close();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}
	}
	
	public int updateState(boolean success, int mvDirection) {
		if(!isTimeOut) {
                        if(success && saveLog){
                            optLog.add(String.format("%s,%d,%d", game.getPreGrid(), mvDirection, game.getNewTile()));
                        }
			if(game.movesAvailable()) {
				gameState = 3;
			}
			else {
				gameState = 4;												//a lot to do
			}
		} else {
			gridHistory[round] = game.getGrid();
			score[round] = game.ioScore;
                        newRound();
			gameState = 5;
		}
		return gameState;
	}
        
        private void newRound()
        {
            if(saveLog)
            {
                optLog.add(String.format("%s,%d,%d", game.getGrid(), -1, -1));
                final String[] log = optLog.toArray(new String[0]);
                final String logDir = String.format("record/%s", roundName);
                final String fileName = String.format("%08d_%06d_%s_%06d_%s.txt", game.ioScore, log.length, Util.getTime(), round, roundName);
                optLog.clear();
            // 写日志      
                new Thread(){

                    @Override
                    public void run() {
                            try {	//指定路径
                                    File dir = new File(logDir);
                                    if(!dir.exists()) dir.mkdirs();
                                    BufferedWriter bw = new BufferedWriter(new FileWriter(String.format("%s/%s", logDir, fileName),true));
                                    for(String str : log)
                                    {
                                        if(str != null)
                                        {
                                            bw.write(str);
                                            bw.newLine();
                                        }
                                    }

                                    bw.flush();
                                    bw.close();
                            } catch (IOException e) {				
                                    e.printStackTrace();
                            }                       
                    }

                }.start();  
            }
            
            round++;
        }

	public String getMsg() throws IOException {
		String msg = "";
		do{try {
			msg = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			break;
		}}while(msg == "");
		return msg;
	}
	
	public void timeStart() {
		Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                //System.out.println("-------时间到--------");
                isTimeOut = true;
                gameState = 5;
//				score[round++] = game.ioScore;
                //System.out.println("gameState"+gameState);             
            }
        }, 1000*timeCount);// 设定指定的时间time,此处为2000毫秒
	}

//	public static void main(String[] args) throws IOException {
//		
//		/**
//		 * @author WuYinan
//		 * 改为4个参数，第3个参数是语言（0为C/C++，1为java），第4个参数是格子尺寸
//		 */
//		if(args.length != 0) {
//			System.out.println("参数错误");
//			return;
//		}
//		
//		/**
//		 * @author WuYinan
//		 * 服务器Socket的定义放在main函数中
//		 */
//		try {	
//			serverSocket = new ServerSocket(port);	
//		} catch(IOException e) {e.printStackTrace(); }
//		
//		/**
//		 * @author WuYinan
//		 * 新增游戏数据输出文件
//		 */
////		FileWriter fw;
//		
//		/**
//		 * @author WuYinan
//		 * 新增循环体，用于不断接收客户端运行
//		 */
//		while(true) {
//			
//			/**
//			 * @author WuYinan
//			 * 输出记录文件；进入循环体cnt++
//			 */
////			if(Integer.parseInt(args[2]) ==  0)
////				fw = new FileWriter("OutputC.txt", true);
////			else
////				fw = new FileWriter("OutputJava.txt", true);	
////			++cnt;
//			
//			//FIXME 这里为了测试延长时间,后面删除
//			IoControl io = new IoControl(4, 120);
////			if(Integer.parseInt(args[1]) ==  1) {
////				isFile = 1;
////			}
//			io.start();
//			
//			int bestRound = 0;
//			int bestScore = 0;
//			
//			for(int i = 0; i < io.round; i++) {
//				// System.out.println(io.score[i]);
//				// System.out.println(io.gridHistory[i]);
//				if(io.score[i] > bestScore) {
//					bestRound = i;
//					bestScore = io.score[i];
//				}
//			}
//			
//			/**
//			 * @author WuYinan
//			 * 输出文件的内容
//			 */
////			fw.write(cnt+" round:"+io.round+" best:"+bestRound+" score:"+bestScore+"\r\n");
////			fw.close();
//			
//			System.out.println("round!!!"+io.round+"best:"+bestRound+"score"+bestScore);
//		}//end for while
//	}//end main method 

	
}
