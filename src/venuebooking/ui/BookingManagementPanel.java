package venuebooking.ui;

import venuebooking.exception.BookingException;
import venuebooking.model.Booking;
import venuebooking.model.BookingStatus;
import venuebooking.service.BookingService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BookingManagementPanel extends JPanel {
    private final BookingService bookingService;

    private final JTextField keywordField = new JTextField(16);
    private final JComboBox<String> statusComboBox = new JComboBox<>(
            new String[]{"SEMUA", "BOOKED", "COMPLETED", "CANCELLED"});
    private final JLabel summaryLabel = new JLabel();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<Booking> displayedBookings;

    public BookingManagementPanel(BookingService bookingService) {
        this.bookingService = bookingService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(createTopPanel(), BorderLayout.NORTH);

        String[] columns = {"ID", "Ruangan", "Penyewa", "Telepon", "Tanggal", "Jam", "Peserta", "Total Biaya", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        add(new JScrollPane(table), BorderLayout.CENTER);

        add(createBottomPanel(), BorderLayout.SOUTH);

        refreshTable();
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(8, 8));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterPanel.add(new JLabel("Cari / Tanggal:"));
        filterPanel.add(keywordField);
        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(statusComboBox);

        JButton filterButton = new JButton("Filter");
        JButton resetButton = new JButton("Reset");
        filterButton.addActionListener(e -> refreshTable());
        resetButton.addActionListener(e -> {
            keywordField.setText("");
            statusComboBox.setSelectedIndex(0);
            refreshTable();
        });
        filterPanel.add(filterButton);
        filterPanel.add(resetButton);

        summaryLabel.setFont(summaryLabel.getFont().deriveFont(Font.BOLD));
        topPanel.add(filterPanel, BorderLayout.WEST);
        topPanel.add(summaryLabel, BorderLayout.EAST);
        return topPanel;
    }

    private JPanel createBottomPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JButton receiptButton = new JButton("Lihat Nota");
        JButton completeButton = new JButton("Tandai Selesai");
        JButton cancelButton = new JButton("Batalkan Booking");

        receiptButton.addActionListener(e -> showReceiptDialog());
        completeButton.addActionListener(e -> changeStatus(BookingStatus.COMPLETED));
        cancelButton.addActionListener(e -> cancelSelectedBooking());

        actionPanel.add(receiptButton);
        actionPanel.add(completeButton);
        actionPanel.add(cancelButton);
        return actionPanel;
    }

    public void refreshTable() {
        String keyword = keywordField.getText();
        String statusFilter = (String) statusComboBox.getSelectedItem();
        displayedBookings = bookingService.filterBookings(keyword, statusFilter);

        tableModel.setRowCount(0);
        for (Booking booking : displayedBookings) {
            tableModel.addRow(new Object[]{
                    booking.getBookingId(),
                    booking.getVenueName(),
                    booking.getCustomerName(),
                    booking.getCustomerPhone(),
                    booking.getDate().toString(),
                    booking.getFormattedTime(),
                    booking.getParticipantCount() + " org",
                    BookingService.formatRupiah(booking.getTotalPrice()),
                    booking.getStatus().name()
            });
        }

        summaryLabel.setText("Booking Aktif: " + bookingService.countActiveBookings()
                + " | Total Pendapatan: " + BookingService.formatRupiah(bookingService.calculateTotalActiveRevenue()));
    }

    private Booking getSelectedBooking() {
        int row = table.getSelectedRow();
        if (row < 0 || displayedBookings == null || row >= displayedBookings.size()) {
            JOptionPane.showMessageDialog(this, "Pilih data booking pada tabel terlebih dahulu.",
                    "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return displayedBookings.get(row);
    }

    private void showReceiptDialog() {
        Booking booking = getSelectedBooking();
        if (booking == null) {
            return;
        }
        String receipt = "=== NOTA RESERVASI RUANGAN ===\n"
                + "ID Booking  : " + booking.getBookingId() + "\n"
                + "Ruangan     : " + booking.getVenueName() + " (" + booking.getVenueId() + ")\n"
                + "Penyewa     : " + booking.getCustomerName() + "\n"
                + "No. Telepon : " + booking.getCustomerPhone() + "\n"
                + "Tanggal     : " + booking.getDate() + "\n"
                + "Waktu       : " + booking.getFormattedTime() + " (" + booking.getDurationHours() + " jam)\n"
                + "Peserta     : " + booking.getParticipantCount() + " orang\n"
                + "Total Biaya : " + BookingService.formatRupiah(booking.getTotalPrice()) + "\n"
                + "Status      : " + booking.getStatus().name();
        JOptionPane.showMessageDialog(this, receipt, "Nota Booking " + booking.getBookingId(),
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void changeStatus(BookingStatus newStatus) {
        Booking booking = getSelectedBooking();
        if (booking == null) {
            return;
        }
        try {
            bookingService.updateBookingStatus(booking.getBookingId(), newStatus);
            refreshTable();
        } catch (BookingException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Tidak Dapat Diproses", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void cancelSelectedBooking() {
        Booking booking = getSelectedBooking();
        if (booking == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Batalkan booking " + booking.getBookingId() + " atas nama " + booking.getCustomerName() + "?",
                "Konfirmasi Pembatalan", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            changeStatus(BookingStatus.CANCELLED);
        }
    }
}
