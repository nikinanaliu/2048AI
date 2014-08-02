/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Dongle
 */
public class ServerManager implements Runnable {

    private int port = 9000; // 端口号
    private static List<Socket> list = new ArrayList<Socket>(); // 保存连接对象
    private ExecutorService exec;
    private ServerSocket server;

    private int size = 4;
    private int timeCount = 120;
    private int poolSize = 3;
    private boolean saveLog = true;

    public ServerManager() {
        this(9000, 3, 4, 60, true);
    }

    public ServerManager(int port, int threads, int gameSize, int timeLimit, boolean saveLog) {
        this.port = port;
        this.poolSize = threads;
        this.size = gameSize;
        this.timeCount = timeLimit;
        this.saveLog = saveLog;

        try {
            server = new ServerSocket(port);
            exec = Executors.newScheduledThreadPool(poolSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("Sever Run.");
        Socket client = null;
        try {
            while (true) {
                client = server.accept(); // 接收客户连接
                String rName = Util.encodeByMD5(String.format("%s%11s", Util.getTime(), Util.encodeBySHA1(String.valueOf(Util.getRandomInt(99999)))));
                list.add(client);
                exec.execute(new IoControl(client, size, timeCount, saveLog, rName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
