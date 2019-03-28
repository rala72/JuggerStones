package io.rala.jugger.model;

public class HistoryEntry {
    private final Team team1;
    private final Team team2;
    private final long stones;
    private final long mode;
    private final boolean reverse;

    public HistoryEntry(Team team1, Team team2, long stones, long mode, boolean reverse) {
        this.team1 = team1;
        this.team2 = team2;
        this.stones = stones;
        this.mode = mode;
        this.reverse = reverse;
    }

    public Team getTeam1() {
        return team1;
    }

    public Team getTeam2() {
        return team2;
    }

    public long getStones() {
        return stones;
    }

    public long getMode() {
        return mode;
    }

    public boolean isReverse() {
        return reverse;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HistoryEntry)) return false;
        HistoryEntry entry = (HistoryEntry) obj;
        return team1.equals(entry.getTeam1()) && team2.equals(entry.getTeam2()) &&
            stones == entry.getStones() && mode == entry.getMode();
    }

    @Override
    public String toString() {
        return getStones() + ": " + getTeam1() + "-" + getTeam2() + " [" + getMode() + ":" + isReverse() + "]";
    }
}
