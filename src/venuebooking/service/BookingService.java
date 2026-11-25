package venuebooking.service;

import venuebooking.exception.BookingException;
import venuebooking.model.Booking;
import venuebooking.model.BookingStatus;
import venuebooking.model.Venue;
import venuebooking.repository.BookingRepository;
import venuebooking.repository.VenueRepository;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookingService {
    private final VenueRepository venueRepository;
    private final BookingRepository bookingRepository;

    public BookingService(VenueRepository venueRepository, BookingRepository bookingRepository) {
        this.venueRepository = venueRepository;
        this.bookingRepository = bookingRepository;
    }

    public static String formatRupiah(double amount) {
        NumberFormat nf = NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID"));
        return "Rp " + nf.format(Math.round(amount));
    }

    public Booking createBooking(Venue venue, String customerName, String customerPhone,
                                 String dateText, int startHour, int durationHours,
                                 String participantText) throws BookingException {
        if (venue == null) {
            throw new BookingException("Silakan pilih ruangan terlebih dahulu.");
        }
        if (!venue.isAvailable()) {
            throw new BookingException("Ruangan sedang tidak tersedia / maintenance.");
        }

        String name = customerName == null ? "" : customerName.trim();
        if (name.isEmpty()) {
            throw new BookingException("Nama penyewa wajib diisi.");
        }

        String phone = customerPhone == null ? "" : customerPhone.trim();
        if (!phone.matches("[0-9+\\- ]{8,16}")) {
            throw new BookingException("Nomor telepon harus berisi 8-16 karakter angka yang valid.");
        }

        int participants;
        try {
            participants = Integer.parseInt(participantText == null ? "" : participantText.trim());
        } catch (NumberFormatException e) {
            throw new BookingException("Jumlah peserta harus berupa angka.");
        }
        if (participants <= 0) {
            throw new BookingException("Jumlah peserta minimal 1 orang.");
        }
        if (participants > venue.getCapacity()) {
            throw new BookingException("Jumlah peserta (" + participants + ") melebihi kapasitas ruangan ("
                    + venue.getCapacity() + " orang).");
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateText == null ? "" : dateText.trim());
        } catch (DateTimeParseException e) {
            throw new BookingException("Format tanggal harus YYYY-MM-DD (contoh: 2026-11-26).");
        }

        if (durationHours < 1 || startHour < 8 || (startHour + durationHours) > 22) {
            throw new BookingException("Jam operasional hanya pukul 08:00 - 22:00.");
        }

        int endHour = startHour + durationHours;
        for (Booking existing : bookingRepository.getAll()) {
            if (existing.getStatus() == BookingStatus.BOOKED
                    && existing.getVenueId().equals(venue.getId())
                    && existing.getDate().equals(date)
                    && startHour < existing.getEndHour()
                    && endHour > existing.getStartHour()) {
                throw new BookingException("Jadwal bentrok dengan booking " + existing.getBookingId()
                        + " (" + existing.getFormattedTime() + ").");
            }
        }

        double totalPrice = venue.calculatePrice(durationHours);
        Booking booking = new Booking(
                bookingRepository.generateNextId(),
                venue.getId(),
                venue.getName(),
                name,
                phone,
                date,
                startHour,
                durationHours,
                participants,
                totalPrice,
                BookingStatus.BOOKED
        );
        bookingRepository.add(booking);
        return booking;
    }

    public List<Booking> filterBookings(String keyword) {
        return filterBookings(keyword, "SEMUA");
    }

    public List<Booking> filterBookings(String keyword, String statusFilter) {
        String query = keyword == null ? "" : keyword.trim().toLowerCase();
        String status = statusFilter == null ? "SEMUA" : statusFilter.trim().toUpperCase();

        List<Booking> result = new ArrayList<>();
        for (Booking booking : bookingRepository.getAll()) {
            boolean matchesStatus = "SEMUA".equals(status) || booking.getStatus().name().equals(status);
            boolean matchesKeyword = query.isEmpty()
                    || booking.getBookingId().toLowerCase().contains(query)
                    || booking.getVenueName().toLowerCase().contains(query)
                    || booking.getCustomerName().toLowerCase().contains(query)
                    || booking.getDate().toString().contains(query);
            if (matchesStatus && matchesKeyword) {
                result.add(booking);
            }
        }
        return result;
    }

    public void updateBookingStatus(String bookingId, BookingStatus newStatus) throws BookingException {
        Booking booking = bookingRepository.findById(bookingId);
        if (booking == null) {
            throw new BookingException("Data booking tidak ditemukan: " + bookingId);
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BookingException("Booking yang sudah dibatalkan tidak dapat diubah.");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED && newStatus == BookingStatus.CANCELLED) {
            throw new BookingException("Booking yang sudah selesai tidak dapat dibatalkan.");
        }
        booking.setStatus(newStatus);
        bookingRepository.saveAll();
    }

    public double calculateTotalActiveRevenue() {
        double total = 0;
        for (Booking booking : bookingRepository.getAll()) {
            if (booking.getStatus() != BookingStatus.CANCELLED) {
                total += booking.getTotalPrice();
            }
        }
        return total;
    }

    public int countActiveBookings() {
        int count = 0;
        for (Booking booking : bookingRepository.getAll()) {
            if (booking.getStatus() == BookingStatus.BOOKED) {
                count++;
            }
        }
        return count;
    }

    public VenueRepository getVenueRepository() {
        return venueRepository;
    }

    public BookingRepository getBookingRepository() {
        return bookingRepository;
    }
}
