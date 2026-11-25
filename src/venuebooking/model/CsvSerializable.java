package venuebooking.model;

public interface CsvSerializable {
    String toCsvRow();

    static String sanitize(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("|", " ").replace("\r", " ").replace("\n", " ").trim();
    }
}
