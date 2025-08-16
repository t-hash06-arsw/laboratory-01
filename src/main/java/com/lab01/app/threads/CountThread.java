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
public class CountThread extends Thread {

    private final int start;
    private final int end;

    /**
     * Creates a thread that prints the integers in the inclusive range [start,
     * end].
     *
     * @param start start of the range (inclusive)
     * @param end   end of the range (inclusive)
     */
    public CountThread(int start, int end) {
        this.start = start;
        this.end = end;
        setName("CountThread-" + start + "-" + end);
    }

    @Override
    public void run() {
        if (start <= end) {
            for (int i = start; i <= end; i++) {
                System.out.println(i);
            }
        } else {
            // If given in reverse, still count through the interval.
            for (int i = start; i >= end; i--) {
                System.out.println(i);
            }
        }
    }

}