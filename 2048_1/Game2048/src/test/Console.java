/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package test;

import client.Client2048;
import java.util.concurrent.ExecutorService;
import scanner.RecordScanner;
import server.ServerManager;

/**
 *
 * @author Dongle
 * args:
 *  -g open with GUI
 *  -s run server
 *  -c run client
 *  -z run log scan
 *  -h=host set host
 *  -p=port set port
 *  -t=threads set thread pool size
 *  -l=level set game size
 *  -o=overtime set client run time limit
 *  -d=deep set client deep
 *  -f=dir set scan dir
 *  -nl set server not save log
 */
public class Console {
    
    private ExecutorService exec;
    
    private static int runMode = -1;
    private static String host = "localhost";
    private static int port = 9000;
    private static int thread = 3;
    private static int level = 4;
    private static int time = 60;    
    private static int deep = 4;
    private static String dir = "record";
    private static boolean saveLog = true;
    
    public static void main(String args[]) {       
        boolean help = true;
        
        for(String str : args)
        {
            help = false;
            if(str.toLowerCase().startsWith("-g")) runMode = 0;
            else if(str.toLowerCase().startsWith("-s")) runMode = 1;
            else if(str.toLowerCase().startsWith("-c")) runMode = 2;
            else if(str.toLowerCase().startsWith("-z")) runMode = 3;
            else if(str.toLowerCase().startsWith("-h") && str.contains("="))
            {
                host = str.split("=")[1];
            }
            else if(str.toLowerCase().startsWith("-p") && str.contains("="))
            {
                port = Integer.parseInt(str.split("=")[1]);
            }
            else if(str.toLowerCase().startsWith("-t") && str.contains("="))
            {
                thread = Integer.parseInt(str.split("=")[1]);
            }
            else if(str.toLowerCase().startsWith("-l") && str.contains("="))
            {
                level = Integer.parseInt(str.split("=")[1]);
            }
            else if(str.toLowerCase().startsWith("-o") && str.contains("="))
            {
                time = (int)(Double.parseDouble(str.split("=")[1]) * 60.0);
            }
            else if(str.toLowerCase().startsWith("-d") && str.contains("="))
            {
                deep = Integer.parseInt(str.split("=")[1]);
            }
            else if(str.toLowerCase().startsWith("-f") && str.contains("="))
            {
                dir = str.split("=")[1];
            }
            else if(str.toLowerCase().startsWith("-nl"))
            {
                saveLog = false;
            }
        }
        
        if(help)
        {
            System.out.println("-g(-c)(-s) [-h=host] [-p=port] [-t=threads] [-l=level] [-o=overtime]");
            System.out.println("-g open with GUI");
            System.out.println("-c run client");
            System.out.println("-s run server");
            System.out.println("-z run log scan");
            System.out.println("-h=host set client host when -c enabled (default=localost)");
            System.out.println("-d=deep set client deep when -c enabled (default=4)");
            System.out.println("-p=port set server/client port when -c or -s enabled (default=9000)");
            System.out.println("-t=threads set server thread pool size when -s enabled (default=3)");
            System.out.println("-l=level set game size when -s enabled (default=4)");
            System.out.println("-o=overtime(mins) set client run time limit when -s enabled (default=1 min)");    
            System.out.println("-f=dir set scan dir when -z enabled (default=record)");  
            System.out.println("-nl set server not save log when -s enabled");
        }
        else
        {
            switch(runMode)
            {
                case 0 :
                {
                    GuiManager.RunGui();
                }break;
                case 1 :
                {
                    new ServerManager(port, thread, level, time, saveLog).run();
                }break;
                case 2 :
                {
                    new Client2048(host, port, deep).run();
                }break;
                case 3 :
                {
                    new RecordScanner(dir).scanAndSave(false);
                }break;
                default: break;
            }
        }
    }
}
