package venuebooking.model;

import java.text.NumberFormat;
import java.util.Locale;

public class Auditorium extends Venue {
    public static final double DEFAULT_SOUND_FEE = 250000;
    private final double soundFee;

    public Auditorium(String id, String name, int capacity, double hourlyRate,
                      String facilities, String imagePath, boolean available) {
        this(id, name, capacity, hourlyRate, facilities, imagePath, available, DEFAULT_SOUND_FEE);
    }

    public Auditorium(String id, String name, int capacity, double hourlyRate,
                      String facilities, String imagePath, boolean available, double soundFee) {
        super(id, name, capacity, hourlyRate, facilities, imagePath, available);
        this.soundFee = soundFee;
    }

    public double getSoundFee() {
        return soundFee;
    }

    @Override
    public String getCategory() {
        return "Auditorium";
    }

    @Override
    public double calculatePrice(int durationHours) {
        return (getHourlyRate() * durationHours) + soundFee;
    }

    @Override
    public String getPricingNote() {
        NumberFormat nf = NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID"));
        return "Tarif/jam + biaya sound & panggung Rp " + nf.format(Math.round(soundFee));
    }

    @Override
    public String toCsvRow() {
        return String.join("|",
                "AUDITORIUM",
                CsvSerializable.sanitize(getId()),
                CsvSerializable.sanitize(getName()),
                String.valueOf(getCapacity()),
                String.valueOf(getHourlyRate()),
                CsvSerializable.sanitize(getFacilities()),
                CsvSerializable.sanitize(getImagePath()),
                String.valueOf(isAvailable()),
                String.valueOf(soundFee));
    }
}
