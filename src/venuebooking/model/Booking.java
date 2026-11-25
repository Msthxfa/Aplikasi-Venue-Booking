package venuebooking.model;

import java.time.LocalDate;

public class Booking implements CsvSerializable {
    private final String bookingId;
    private final String venueId;
    private final String venueName;
    private final String customerName;
    private final String customerPhone;
    private final LocalDate date;
    private final int startHour;
    private final int durationHours;
    private final int participantCount;
    private final double totalPrice;
    private BookingStatus status;

    public Booking(String bookingId, String venueId, String venueName,
                   String customerName, String customerPhone, LocalDate date,
                   int startHour, int durationHours, int participantCount,
                   double totalPrice, BookingStatus status) {
        this.bookingId = bookingId;
        this.venueId = venueId;
        this.venueName = venueName;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.date = date;
        this.startHour = startHour;
        this.durationHours = durationHours;
        this.participantCount = participantCount;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getVenueId() {
        return venueId;
    }

    public String getVenueName() {
        return venueName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getStartHour() {
        return startHour;
    }

    public int getDurationHours() {
        return durationHours;
    }

    public int getEndHour() {
        return startHour + durationHours;
    }

    public int getParticipantCount() {
        return participantCount;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getFormattedTime() {
        return String.format("%02d:00 - %02d:00", startHour, getEndHour());
    }

    @Override
    public String toCsvRow() {
        return String.join("|",
                CsvSerializable.sanitize(bookingId),
                CsvSerializable.sanitize(venueId),
                CsvSerializable.sanitize(venueName),
                CsvSerializable.sanitize(customerName),
                CsvSerializable.sanitize(customerPhone),
                date.toString(),
                String.valueOf(startHour),
                String.valueOf(durationHours),
                String.valueOf(participantCount),
                String.valueOf(totalPrice),
                status.name());
    }

    public static Booking fromCsvRow(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 11) {
            throw new IllegalArgumentException("Baris CSV booking tidak valid: " + line);
        }
        return new Booking(
                parts[0],
                parts[1],
                parts[2],
                parts[3],
                parts[4],
                LocalDate.parse(parts[5]),
                Integer.parseInt(parts[6]),
                Integer.parseInt(parts[7]),
                Integer.parseInt(parts[8]),
                Double.parseDouble(parts[9]),
                BookingStatus.valueOf(parts[10]));
    }
}
