/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package scanner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import server.Util;

/**
 *
 * @author Dongle
 */
public class RecordScanner implements Runnable{
    private String path = "record";
    private HashMap<String, RecordItem> map = new HashMap<String, RecordItem>();

    public RecordScanner(String path) {
        this.path = path;
    }
    
    public void scan()
    {
        if(path != null)
        {
            File dir = new File(path);
            if(dir.exists())
            {
                for(File file : dir.listFiles(Util.createFilenameFilter("txt")))
                {
                    String[] nameArr = file.getName().split("_");
                    if(nameArr.length > 2)
                    {
                        try {
                            BufferedReader br = new BufferedReader(new FileReader(file));
                            for(Object o : br.lines().toArray())
                            {
                                String item = o.toString();
                                String[] cell = item.split(",");
                                if(cell.length >= 3)
                                {
                                    String status = cell[0];
                                    int score = Integer.parseInt(nameArr[0]);  
                                    int nopt = Integer.parseInt(cell[1]);
                                    int step = Integer.parseInt(nameArr[1]);
                                    
                                    if(map.containsKey(status))
                                    {
                                        RecordItem ri = map.get(status);
                                        if(ri.getScore() < score)
                                        {
                                            ri.setNextOpt(nopt);
                                            ri.setScore(score);
                                            ri.setStep(step);
                                        }
                                    }
                                    else
                                    {
                                        RecordItem ri = new RecordItem(status, nopt, score, step);
                                        map.put(status, ri);
                                    }
                                }
                            }
                        } catch (FileNotFoundException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }      
    }
    
    public void showMap()
    {
        for(String str : map.keySet())
        {
            System.out.println(map.get(str));
        }
    }
    
    public void writeFile()
    {
         new Thread(){
                    String logDir = "record";
                    String fileName = "0_scanLog.txt";
                    String timeNow = Util.getTime();

                    @Override
                    public void run() {
                            try {	//Ö¸¶¨Â·¾¶
                                    File dir = new File(logDir);
                                    if(!dir.exists()) dir.mkdir();
                                    BufferedWriter bw = new BufferedWriter(new FileWriter(String.format("%s/%s", logDir, fileName),true));
                                    for(String str : map.keySet())
                                    {
                                        bw.write(map.get(str).toString());
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
    
    public void scanAndSave(boolean show)
    {
        scan();
        if(show) showMap();
        writeFile();
    }

    @Override
    public void run() {
        scanAndSave(false);
    }
}
