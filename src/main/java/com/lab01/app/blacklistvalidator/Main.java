/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lab01.app.blacklistvalidator;

import java.util.List;

/**
 *
 * @author hcadavid
 */
public class Main {

    public static void main(String a[]) {
        HostBlackListsValidator hblv = new HostBlackListsValidator();
        int n = (a != null && a.length > 0) ? Integer.parseInt(a[0]) : 1;
        String ip = (a != null && a.length > 1) ? a[1] : "200.24.34.55";
        long t0 = System.nanoTime();
        List<Integer> blackListOcurrences = hblv.checkHost(ip, n);
        long t1 = System.nanoTime();
        long ms = (t1 - t0) / 1_000_000L;
        System.out.println("Threads=" + n + ", IP=" + ip + ", Elapsed(ms)=" + ms);
        System.out.println("The host was found in the following blacklists:" + blackListOcurrences);
    }

}