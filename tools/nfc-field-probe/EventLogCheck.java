package hk.vibefinance.nfcprobe;
public final class EventLogCheck {
    public static void main(String[] args) {
        EventLog log = new EventLog();
        log.add("00:00:00.000", true);
        log.add("00:00:00.001", false);
        assert log.text().equals("00:00:00.000  FIELD_ON\n00:00:00.001  FIELD_OFF");
        for (int i = 0; i < 201; i++) log.add("event-" + i, true);
        assert log.size() == 200;
        assert !log.text().contains("event-0 ");
        assert log.text().endsWith("event-200  FIELD_ON");
        System.out.println("EventLog checks passed");
    }
}
