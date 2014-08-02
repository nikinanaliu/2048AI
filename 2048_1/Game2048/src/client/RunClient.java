/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package client;

/**
 *
 * @author Dongle
 */
public class RunClient {
    public static void main(String[] args) {
                    /**
                     * Æô¶¯¿Í»§¶Ë
                     */
                    new Client2048("localhost", 9000, 4).start();
    }
}
