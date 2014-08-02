/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package server;

/**
 *
 * @author Dongle
 */
public class RunServer {
    
    
    public static void main(String[] args) { 
       new ServerManager(9000, 3, 4, 60, true).run();
    }
}
