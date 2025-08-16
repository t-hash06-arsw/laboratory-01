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
        List<Integer> blackListOcurrences = hblv.checkHost("200.24.34.55", n);
        System.out.println("The host was found in the following blacklists:" + blackListOcurrences);

    }

}