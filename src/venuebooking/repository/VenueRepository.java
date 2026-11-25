package venuebooking.repository;

import venuebooking.model.Auditorium;
import venuebooking.model.HallRoom;
import venuebooking.model.MeetingRoom;
import venuebooking.model.Venue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class VenueRepository {
    private final List<Venue> venues = new ArrayList<>();
    private final Path filePath;

    public VenueRepository() {
        this(Paths.get("data", "venues.csv"));
    }

    public VenueRepository(Path filePath) {
        this.filePath = filePath;
        load();
    }

    private void load() {
        venues.clear();
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
                    venues.add(parseVenue(trimmed));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Gagal membaca data ruangan: " + e.getMessage(), e);
        }
    }

    private Venue parseVenue(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length < 9) {
            throw new IllegalArgumentException("Format baris CSV ruangan tidak valid: " + line);
        }
        String type = p[0];
        String id = p[1];
        String name = p[2];
        int capacity = Integer.parseInt(p[3]);
        double rate = Double.parseDouble(p[4]);
        String facilities = p[5];
        String imagePath = p[6];
        boolean available = Boolean.parseBoolean(p[7]);
        double extraFee = Double.parseDouble(p[8]);

        switch (type) {
            case "AUDITORIUM":
                return new Auditorium(id, name, capacity, rate, facilities, imagePath, available, extraFee);
            case "HALL":
                return new HallRoom(id, name, capacity, rate, facilities, imagePath, available);
            case "MEETING":
                return new MeetingRoom(id, name, capacity, rate, facilities, imagePath, available);
            default:
                throw new IllegalArgumentException("Tipe ruangan tidak dikenal: " + type);
        }
    }

    public List<Venue> getAll() {
        return new ArrayList<>(venues);
    }

    public List<Venue> getAvailableVenues() {
        List<Venue> result = new ArrayList<>();
        for (Venue venue : venues) {
            if (venue.isAvailable()) {
                result.add(venue);
            }
        }
        return result;
    }

    public Venue findById(String id) {
        for (Venue venue : venues) {
            if (venue.getId().equalsIgnoreCase(id)) {
                return venue;
            }
        }
        return null;
    }

    public void addVenue(Venue venue) {
        venues.add(venue);
        saveAll();
    }

    public boolean removeVenue(String id) {
        boolean removed = venues.removeIf(v -> v.getId().equalsIgnoreCase(id));
        if (removed) {
            saveAll();
        }
        return removed;
    }

    public String generateNextId(String prefix) {
        int max = 0;
        for (Venue venue : venues) {
            if (venue.getId().startsWith(prefix + "-")) {
                try {
                    int num = Integer.parseInt(venue.getId().substring(prefix.length() + 1));
                    if (num > max) {
                        max = num;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return String.format("%s-%02d", prefix, max + 1);
    }

    public void saveAll() {
        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            List<String> lines = new ArrayList<>();
            for (Venue venue : venues) {
                lines.add(venue.toCsvRow());
            }
            Files.write(filePath, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Gagal menyimpan data ruangan: " + e.getMessage(), e);
        }
    }
}
