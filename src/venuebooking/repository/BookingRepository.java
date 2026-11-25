package venuebooking.repository;

import venuebooking.model.Booking;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class BookingRepository {
    private final List<Booking> bookings = new ArrayList<>();
    private final Path filePath;

    public BookingRepository() {
        this(Paths.get("data", "bookings.csv"));
    }

    public BookingRepository(Path filePath) {
        this.filePath = filePath;
        load();
    }

    private void load() {
        bookings.clear();
        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
                return;
            }
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    bookings.add(Booking.fromCsvRow(trimmed));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Gagal membaca data booking: " + e.getMessage(), e);
        }
    }

    public List<Booking> getAll() {
        return new ArrayList<>(bookings);
    }

    public Booking findById(String bookingId) {
        for (Booking booking : bookings) {
            if (booking.getBookingId().equalsIgnoreCase(bookingId)) {
                return booking;
            }
        }
        return null;
    }

    public void add(Booking booking) {
        bookings.add(booking);
        saveAll();
    }

    public void saveAll() {
        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            List<String> lines = new ArrayList<>();
            for (Booking booking : bookings) {
                lines.add(booking.toCsvRow());
            }
            Files.write(filePath, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Gagal menyimpan data booking: " + e.getMessage(), e);
        }
    }

    public String generateNextId() {
        int max = 0;
        for (Booking booking : bookings) {
            String id = booking.getBookingId();
            if (id.startsWith("BK-")) {
                try {
                    int num = Integer.parseInt(id.substring(3));
                    if (num > max) {
                        max = num;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return String.format("BK-%03d", max + 1);
    }
}
