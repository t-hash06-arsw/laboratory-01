package com.lab01.app.blacklistvalidator;

import com.lab01.app.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import java.util.LinkedList;
import java.util.List;

public class BlacklistSearchThread extends Thread {

    private final HostBlacklistsDataSourceFacade facade;
    private final String ip;
    private final int startInclusive;
    private final int endExclusive;
    private final LinkedList<Integer> found;
    private int checked;

    public BlacklistSearchThread(HostBlacklistsDataSourceFacade facade, String ip, int startInclusive,
            int endExclusive) {
        this.facade = facade;
        this.ip = ip;
        this.startInclusive = startInclusive;
        this.endExclusive = endExclusive;
        this.found = new LinkedList<>();
        this.checked = 0;
    }

    @Override
    public void run() {
        for (int i = startInclusive; i < endExclusive; i++) {
            checked++;
            if (facade.isInBlackListServer(i, ip)) {
                found.add(i);
            }
        }
    }

    public List<Integer> getFound() {
        return found;
    }

    public int getChecked() {
        return checked;
    }

    public int getOccurrencesCount() {
        return found.size();
    }
}
