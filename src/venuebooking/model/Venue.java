package venuebooking.model;

public abstract class Venue implements CsvSerializable {
    private final String id;
    private String name;
    private int capacity;
    private double hourlyRate;
    private String facilities;
    private String imagePath;
    private boolean available;

    public Venue(String id, String name, int capacity, double hourlyRate,
                 String facilities, String imagePath, boolean available) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.hourlyRate = hourlyRate;
        this.facilities = facilities;
        this.imagePath = imagePath;
        this.available = available;
    }

    public abstract String getCategory();

    public abstract double calculatePrice(int durationHours);

    public abstract String getPricingNote();

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public String getFacilities() {
        return facilities;
    }

    public void setFacilities(String facilities) {
        this.facilities = facilities;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return id + " - " + name + " (Maks " + capacity + " org)";
    }
}
