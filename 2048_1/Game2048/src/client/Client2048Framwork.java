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
 * �û���Ҫ�̳������,����дsolve������������ʵ���ƶ�������
 * @author 2048��ί��
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
	 * ����һ�����󣬲����С�
	 */
	public Client2048Framwork() {
	}
	
	/**
	 * ��Ҫ�û���д�ķ�����</br>
	 * ��Ϸ��ʼʱ����ǰһ�������Ѿ�����������Ӧ����������ᱻ���á�</br>
	 * ѡȡ��õķ���������move�����������ƶ���~��һ��solve������ֻ�������һ��move��</br>
	 * �������Ҫ��ȡ��ǰ������Ϣ�����Ե���getCurrentGrid������</br>
	 * �������Ҫ��ȡ��ǰ�����С�����Ե���getSize������
	 * @throws IOException move�������ܻ��׳��쳣��ֱ���׳����ɣ��û����ؽ����
	 */
	protected abstract void solve() throws IOException;
	
	/**
	 * ���������ָ������ƶ������ر����ƶ��Ƿ���Ч�ı�ǡ�</br>
	 * ֧�ֵ�ָ���У�</br>
	 * 0�������ƶ�</br>
	 * 1�������ƶ�</br>
	 * 2�������ƶ�</br>
	 * 3�������ƶ�</br>
	 * �ƶ���Ч���ҽ��� 1�������ָ���֧�ַ�Χ֮�ڣ�2��ͬһ��solve�������ε��á�
	 * @param order ָ��
	 * @throws IOException ���������쳣ʱ�׳���
	 * @return �����ƶ��Ƿ���Ч�ı��
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
	 * ��õ�ǰ��Ϸ�ķ���Ĵ�С
	 * @return n*n�����nֵ
	 */
	protected final int getSize() {
		return size;
	}
	
	/**
	 * ��õ�ǰ��һ����n*n����ÿһ��Ԫ���Ǹ�λ�÷�������֣����ַ�����ʽ�����������λ��û�����֣��ַ���Ϊ�մ���
	 * @return ��ʾ��ǰn*n������ַ�����ά����
	 */
	protected final long[][] getCurrentGrid() {
		return currentGrid;
	}
	
	/**
	 * ��ʼ����
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
	 * �½�һ��socket������ȡ����IO
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
	 * ��socket��ȡ��Ϣ
	 * @return ��Ϣ
	 * @throws IOException һǧ�λ�ȡ��Ϣʧ�ܺ��׳�
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
