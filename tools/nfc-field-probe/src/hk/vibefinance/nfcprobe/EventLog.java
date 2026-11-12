package hk.vibefinance.nfcprobe;

import java.util.ArrayDeque;

/** Bounded event-only diagnostics: never accepts card data or coordinates. */
final class EventLog {
    private final ArrayDeque<String> lines = new ArrayDeque<>();
    synchronized void add(String timestamp, boolean fieldOn) {
        lines.addLast(timestamp + (fieldOn ? "  FIELD_ON" : "  FIELD_OFF"));
        while (lines.size() > 200) lines.removeFirst();
    }
    synchronized int size() { return lines.size(); }
    synchronized String text() { return String.join("\n", lines); }
}
