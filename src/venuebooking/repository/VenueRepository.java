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
            if (!Files.exists(filePath) || Files.size(filePath) == 0) {
                seedDefaults();
                saveAll();
                return;
            }
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    venues.add(parseVenue(trimmed));
                }
            }
            if (venues.isEmpty()) {
                seedDefaults();
                saveAll();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Gagal membaca data ruangan: " + e.getMessage(), e);
        }
    }

    private void seedDefaults() {
        venues.add(new Auditorium("AUD-01", "Auditorium Utama Graha", 300, 750000,
                "Panggung, Sound System, Proyektor, AC Sentral",
                "assets/images/auditorium.png", true, Auditorium.DEFAULT_SOUND_FEE));
        venues.add(new HallRoom("HAL-01", "Aula Serbaguna Melati", 120, 400000,
                "Kursi Lipat, Mic Wireless, Proyektor, AC",
                "assets/images/hall.png", true));
        venues.add(new HallRoom("HAL-02", "Ruang Seminar Cendana", 80, 300000,
                "Podium, Layar Proyektor, Sound, Wi-Fi",
                "assets/images/hall.png", true));
        venues.add(new MeetingRoom("MTG-01", "Ruang Rapat Eksekutif A", 20, 150000,
                "Smart TV, Whiteboard, Wi-Fi, AC",
                "assets/images/meeting_room.png", true));
        venues.add(new MeetingRoom("MTG-02", "Ruang Diskusi B", 10, 90000,
                "Proyektor, Whiteboard, AC",
                "assets/images/meeting_room.png", true));
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
