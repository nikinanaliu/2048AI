package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import new2048.NewAI;

/**
 * 用户需要继承这个类,并填写solve方法，在其中实现移动操作。
 * @author 2048组委会
 *
 */
public abstract class Client2048Framwork {
	
	protected Socket socket;
	private BufferedReader br;
	private BufferedWriter pw;
	protected String host = "localhost";
	protected int port = 9000;
	
	private int size;
	private boolean over = false;
	private boolean canMove = false;
	
	private long[][] currentGrid;
	
	/**
	 * 构建一个对象，并运行。
	 */
	public Client2048Framwork() {
	}
	
	/**
	 * 需要用户填写的方法。</br>
	 * 游戏开始时，或前一步操作已经被服务器响应后，这个方法会被调用。</br>
	 * 选取最好的方法，调用move方法，进行移动吧~（一次solve调用中只允许调用一次move）</br>
	 * 如果你需要获取当前方阵信息，可以调用getCurrentGrid方法。</br>
	 * 如果你需要获取当前方阵大小，可以调用getSize方法。
	 * @throws IOException move方法可能会抛出异常，直接抛出即可，用户不必解决。
	 */
	protected abstract void solve() throws IOException;
	
	/**
	 * 按照输入的指令进行移动。返回本次移动是否有效的标记。</br>
	 * 支持的指令有：</br>
	 * 0：向上移动</br>
	 * 1：向右移动</br>
	 * 2：向下移动</br>
	 * 3：向左移动</br>
	 * 移动无效当且仅当 1）输入的指令不在支持范围之内；2）同一次solve方法里多次调用。
	 * @param order 指令
	 * @throws IOException 当服务器异常时抛出。
	 * @return 本次移动是否有效的标记
	 */
	protected boolean move(int order) throws IOException {
		if (canMove && order >= 0 && order < 4) {
			canMove = false;
			pw.write(order + "\r\n");
			pw.flush();
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 获得当前游戏的方阵的大小
	 * @return n*n方阵的n值
	 */
	protected final int getSize() {
		return size;
	}
	
	/**
	 * 获得当前这一步的n*n方阵。每一个元素是该位置方格的数字，以字符串形式给出。如果该位置没有数字，字符串为空串。
	 * @return 表示当前n*n方阵的字符串二维数组
	 */
	protected final long[][] getCurrentGrid() {
		return currentGrid;
	}
	
	/**
	 * 开始运行
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	public void start() {
            
               // NewAI.initProcess();
		try {
                        //System.out.println("Client init.");
			init();
                        //System.out.println("Clinet init finished. starting get size from server. ");
			getSizeFromServer();
                       // System.out.println("Clinet get size ok.");
                        //System.out.println("Clinet starting get state from server.");
			getState();
                        //System.out.println("Client get state ok.");
			
			while(true) {
				switch(parser()) {
					case 0:
                                               	canMove = true;
						solve();
						break;
					case 1:
						//System.out.println("Client send start command when recieve 'ready' or 'over'");
						sendStart();
						break;
					case 2:
						getGrid();
						break;
					case 3:
						over=true;
						break;
					default:
                                        
                                                this.start();
						break;
				}
				
				if(over) {
                                        over = false;
                                        this.start();
					
				}
			}
		} catch (IOException e) {
                        //this.start();
			System.out.println("Server is dead. Game over.");
			e.printStackTrace();
		}
	}
	
	private void sendStart() throws IOException {
		pw.write("4"+"\r\n");
		pw.flush();
	}
	
	private void getState() throws IOException {
		pw.write("5"+"\r\n");
		pw.flush();
	}
	
	@SuppressWarnings("unused")
	private void getScore() throws IOException {
		pw.write("6"+"\r\n");
		pw.flush();
	}
	
	private void getGrid() throws IOException {
		pw.write("7"+"\r\n");
		pw.flush();
	}
	
	private void getSizeFromServer() throws IOException {
		pw.write("8\r\n");
		pw.flush();
		parser();
	}
	
	/**
	 * 新建一个socket，并获取构建IO
	 * @throws IOException
	 */
	private void init() throws IOException {
		socket = new Socket(host, port);
		br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		pw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));  
	}
	
	private int parser() throws IOException {
		String msg = getMsg();
                //System.out.println(msg);
		String[] msgs = msg.split(":");
		if(msgs[0].contains("grid")) {
			String[] buf = msgs[1].split("\\.");
                        //System.out.println("recieve grid:"+msgs[1]);
			currentGrid = new long[size][size];
			for (int i = 0; i < size; ++i) {
				for (int j = 0; j < size; ++j) {
					currentGrid[i][j] = buf[i * size + j].equals(" ") ? 0 : Long.parseLong(buf[i * size + j]);
				}
			}
                        /**
                        for(long[] ll : currentGrid)
                        {
                            for(long l : ll)
                            {
                                System.out.print(l + " ");
                            }
                            System.out.println();
                        }
                        **/
			return 0;
		} else if(msgs[0].contains("state")) {
			if(msgs[1].contains("ready")||msgs[1].contains("Over")) {
				return 1;
			} else if(msgs[1].contains("playing") || msgs[1].contains("WrongOrder")) {
				return 2;
			} else {										//other result   timeout   roundout
 				return 3;
			}
		} else if(msgs[0].contains("size")) {
			size = Integer.parseInt(msgs[1]);
		} else if(msgs[0].contains("score")) {
			/**
			 * do something about score
			 */
			return 2;
		}
		return 3;
	}
	
	/**
	 * 从socket获取消息
	 * @return 消息
	 * @throws IOException 一千次获取消息失败后抛出
	 */
	private String getMsg() throws IOException{
		String msg = "";
		final int MAXTRYTIMES = 1000;
		int tryCount = 0;
		do {
			try {
				msg = br.readLine();
			} catch (IOException e) {
				tryCount++;
			}
		} while (msg == "" && tryCount < MAXTRYTIMES);
		if (tryCount == MAXTRYTIMES) {
			throw new IOException("Failed to get message from server.");
		}
		return msg;
	}
}
