/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lab01.app.blacklistvalidator;

import com.lab01.app.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class HostBlackListsValidator {

    private static final int BLACK_LIST_ALARM_COUNT = 5;

    /**
     * Check the given host's IP address in all the available black lists,
     * and report it as NOT Trustworthy when such IP was reported in at least
     * BLACK_LIST_ALARM_COUNT lists, or as Trustworthy in any other case.
     * The search is not exhaustive: When the number of occurrences is equal to
     * BLACK_LIST_ALARM_COUNT, the search is finished, the host reported as
     * NOT Trustworthy, and the list of the five blacklists returned.
     * 
     * @param ipaddress suspicious host's IP address.
     * @return Blacklists numbers where the given host's IP address was found.
     */
    public List<Integer> checkHost(String ipaddress) {
        return checkHost(ipaddress, 1);
    }

    public List<Integer> checkHost(String ipaddress, int nThreads) {
        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();
        int totalServers = skds.getRegisteredServersCount();

        if (nThreads <= 0)
            nThreads = 1;
        if (nThreads > totalServers)
            nThreads = totalServers;

        LinkedList<Integer> blackListOcurrences = new LinkedList<>();

        if (nThreads == 1) {
            int ocurrencesCount = 0;
            int checkedListsCount = 0;
            for (int i = 0; i < totalServers && ocurrencesCount < BLACK_LIST_ALARM_COUNT; i++) {
                checkedListsCount++;
                if (skds.isInBlackListServer(i, ipaddress)) {
                    blackListOcurrences.add(i);
                    ocurrencesCount++;
                }
            }
            if (ocurrencesCount >= BLACK_LIST_ALARM_COUNT) {
                skds.reportAsNotTrustworthy(ipaddress);
            } else {
                skds.reportAsTrustworthy(ipaddress);
            }
            LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[] { checkedListsCount, totalServers });
            return blackListOcurrences;
        }

        int base = totalServers / nThreads;
        int rem = totalServers % nThreads;

        BlacklistSearchThread[] workers = new BlacklistSearchThread[nThreads];
        int start = 0;
        for (int i = 0; i < nThreads; i++) {
            int size = base + (i < rem ? 1 : 0);
            int end = start + size;
            workers[i] = new BlacklistSearchThread(skds, ipaddress, start, end);
            start = end;
        }

        for (BlacklistSearchThread w : workers) {
            w.start();
        }

        int checkedListsCount = 0;
        for (BlacklistSearchThread w : workers) {
            try {
                w.join();
                checkedListsCount += w.getChecked();
                blackListOcurrences.addAll(w.getFound());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOG.log(Level.SEVERE, null, ex);
            }
        }

        if (blackListOcurrences.size() >= BLACK_LIST_ALARM_COUNT) {
            skds.reportAsNotTrustworthy(ipaddress);
        } else {
            skds.reportAsTrustworthy(ipaddress);
        }

        LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[] { checkedListsCount, totalServers });
        return blackListOcurrences;
    }

    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());

}