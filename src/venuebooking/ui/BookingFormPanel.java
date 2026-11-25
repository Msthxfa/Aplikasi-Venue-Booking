package venuebooking.ui;

import venuebooking.exception.BookingException;
import venuebooking.model.Booking;
import venuebooking.model.Venue;
import venuebooking.service.BookingService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class BookingFormPanel extends JPanel {
    private final BookingService bookingService;
    private final Runnable onBookingSaved;

    private final JComboBox<Venue> venueComboBox = new JComboBox<>();
    private final JTextField customerNameField = new JTextField();
    private final JTextField customerPhoneField = new JTextField();
    private final JTextField participantsField = new JTextField();
    private final JTextField dateField = new JTextField(LocalDate.now().toString());
    private final JComboBox<String> startHourComboBox = new JComboBox<>();
    private final JTextField durationField = new JTextField("2");
    private final JLabel estimateLabel = new JLabel("Rp 0");

    public BookingFormPanel(BookingService bookingService, Runnable onBookingSaved) {
        this.bookingService = bookingService;
        this.onBookingSaved = onBookingSaved;

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        for (int hour = 8; hour <= 21; hour++) {
            startHourComboBox.addItem(String.format("%02d:00", hour));
        }

        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Formulir Pemesanan Ruangan"));
        formPanel.add(new JLabel("Pilih Ruangan:"));
        formPanel.add(venueComboBox);
        formPanel.add(new JLabel("Nama Penyewa / Instansi:"));
        formPanel.add(customerNameField);
        formPanel.add(new JLabel("Nomor Telepon:"));
        formPanel.add(customerPhoneField);
        formPanel.add(new JLabel("Jumlah Peserta:"));
        formPanel.add(participantsField);
        formPanel.add(new JLabel("Tanggal Sewa (YYYY-MM-DD):"));
        formPanel.add(dateField);
        formPanel.add(new JLabel("Jam Mulai (08:00 - 21:00):"));
        formPanel.add(startHourComboBox);
        formPanel.add(new JLabel("Durasi Sewa (Jam):"));
        formPanel.add(durationField);
        formPanel.add(new JLabel("Estimasi Total Biaya:"));
        estimateLabel.setFont(estimateLabel.getFont().deriveFont(Font.BOLD));
        formPanel.add(estimateLabel);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        JButton estimateButton = new JButton("Hitung Estimasi");
        JButton saveButton = new JButton("Simpan Booking");
        JButton resetButton = new JButton("Reset Form");

        estimateButton.addActionListener(e -> updateEstimate(true));
        saveButton.addActionListener(e -> saveBooking());
        resetButton.addActionListener(e -> resetForm());
        venueComboBox.addActionListener(e -> updateEstimate(false));

        buttonPanel.add(estimateButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(resetButton);
        add(buttonPanel, BorderLayout.SOUTH);

        refreshVenues();
    }

    public void refreshVenues() {
        Venue selected = (Venue) venueComboBox.getSelectedItem();
        String selectedId = selected == null ? null : selected.getId();

        venueComboBox.removeAllItems();
        List<Venue> availableVenues = bookingService.getVenueRepository().getAvailableVenues();
        for (Venue venue : availableVenues) {
            venueComboBox.addItem(venue);
            if (venue.getId().equals(selectedId)) {
                venueComboBox.setSelectedItem(venue);
            }
        }
        updateEstimate(false);
    }

    private void updateEstimate(boolean showErrorDialog) {
        Venue venue = (Venue) venueComboBox.getSelectedItem();
        if (venue == null) {
            estimateLabel.setText("Tidak ada ruangan tersedia");
            return;
        }
        try {
            int duration = Integer.parseInt(durationField.getText().trim());
            if (duration < 1) {
                throw new NumberFormatException();
            }
            double total = venue.calculatePrice(duration);
            estimateLabel.setText(BookingService.formatRupiah(total) + " (" + venue.getPricingNote() + ")");
        } catch (NumberFormatException ex) {
            estimateLabel.setText("Durasi tidak valid");
            if (showErrorDialog) {
                JOptionPane.showMessageDialog(this, "Durasi sewa harus berupa angka minimal 1 jam.",
                        "Validasi", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void saveBooking() {
        Venue venue = (Venue) venueComboBox.getSelectedItem();
        String startText = (String) startHourComboBox.getSelectedItem();
        int startHour = startText == null ? 8 : Integer.parseInt(startText.substring(0, 2));

        int duration;
        try {
            duration = Integer.parseInt(durationField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Durasi sewa harus berupa angka bulat.",
                    "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Booking booking = bookingService.createBooking(
                    venue,
                    customerNameField.getText(),
                    customerPhoneField.getText(),
                    dateField.getText(),
                    startHour,
                    duration,
                    participantsField.getText()
            );

            String summary = "Booking berhasil disimpan!\n\n"
                    + "ID Booking : " + booking.getBookingId() + "\n"
                    + "Ruangan    : " + booking.getVenueName() + "\n"
                    + "Penyewa    : " + booking.getCustomerName() + "\n"
                    + "Tanggal    : " + booking.getDate() + " (" + booking.getFormattedTime() + ")\n"
                    + "Total Biaya: " + BookingService.formatRupiah(booking.getTotalPrice());

            JOptionPane.showMessageDialog(this, summary, "Reservasi Berhasil", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
            onBookingSaved.run();
        } catch (BookingException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Gagal Menyimpan Booking", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void resetForm() {
        customerNameField.setText("");
        customerPhoneField.setText("");
        participantsField.setText("");
        dateField.setText(LocalDate.now().toString());
        startHourComboBox.setSelectedIndex(0);
        durationField.setText("2");
        updateEstimate(false);
    }
}
