/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lab01.app.threads;

/**
 *
 * @author hcadavid
 */
public class CountThreadsMain {

    public static void main(String a[]) {
        Thread t1 = new CountThread(0, 99);
        Thread t2 = new CountThread(99, 199);
        Thread t3 = new CountThread(200, 299);

        t1.start();
        t2.start();
        t3.start();
    }

}