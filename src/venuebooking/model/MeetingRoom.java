package venuebooking.model;

public class MeetingRoom extends Venue {
    public MeetingRoom(String id, String name, int capacity, double hourlyRate,
                       String facilities, String imagePath, boolean available) {
        super(id, name, capacity, hourlyRate, facilities, imagePath, available);
    }

    @Override
    public String getCategory() {
        return "Meeting Room";
    }

    @Override
    public double calculatePrice(int durationHours) {
        return getHourlyRate() * durationHours;
    }

    @Override
    public String getPricingNote() {
        return "Tarif normal per jam";
    }

    @Override
    public String toCsvRow() {
        return String.join("|",
                "MEETING",
                CsvSerializable.sanitize(getId()),
                CsvSerializable.sanitize(getName()),
                String.valueOf(getCapacity()),
                String.valueOf(getHourlyRate()),
                CsvSerializable.sanitize(getFacilities()),
                CsvSerializable.sanitize(getImagePath()),
                String.valueOf(isAvailable()),
                "0");
    }
}
