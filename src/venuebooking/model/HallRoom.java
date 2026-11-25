package venuebooking.model;

public class HallRoom extends Venue {
    public HallRoom(String id, String name, int capacity, double hourlyRate,
                    String facilities, String imagePath, boolean available) {
        super(id, name, capacity, hourlyRate, facilities, imagePath, available);
    }

    @Override
    public String getCategory() {
        return "Hall / Aula";
    }

    @Override
    public double calculatePrice(int durationHours) {
        double base = getHourlyRate() * durationHours;
        return durationHours >= 5 ? base * 0.90 : base;
    }

    @Override
    public String getPricingNote() {
        return "Diskon 10% untuk sewa >= 5 jam";
    }

    @Override
    public String toCsvRow() {
        return String.join("|",
                "HALL",
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
