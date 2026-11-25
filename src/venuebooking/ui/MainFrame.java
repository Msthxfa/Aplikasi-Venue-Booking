package venuebooking.ui;

import venuebooking.repository.BookingRepository;
import venuebooking.repository.VenueRepository;
import venuebooking.service.BookingService;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        super("Aplikasi Reservasi Gedung & Ruangan");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(940, 580);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        VenueRepository venueRepository = new VenueRepository();
        BookingRepository bookingRepository = new BookingRepository();
        BookingService bookingService = new BookingService(venueRepository, bookingRepository);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(34, 65, 110));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel titleLabel = new JLabel("Sistem Reservasi Gedung & Ruangan");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        JLabel subtitleLabel = new JLabel("Panel Resepsionis - Venue Booking System");
        subtitleLabel.setForeground(new Color(220, 230, 245));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(subtitleLabel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();

        BookingManagementPanel managementPanel = new BookingManagementPanel(bookingService);
        BookingFormPanel formPanel = new BookingFormPanel(bookingService, managementPanel::refreshTable);
        VenueCatalogPanel catalogPanel = new VenueCatalogPanel(venueRepository, formPanel::refreshVenues);

        tabbedPane.addTab("1. Katalog Ruangan", catalogPanel);
        tabbedPane.addTab("2. Input Reservasi", formPanel);
        tabbedPane.addTab("3. Jadwal & Riwayat", managementPanel);

        tabbedPane.addChangeListener(e -> {
            int index = tabbedPane.getSelectedIndex();
            if (index == 0) {
                catalogPanel.refreshTable();
            } else if (index == 1) {
                formPanel.refreshVenues();
            } else if (index == 2) {
                managementPanel.refreshTable();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);
    }
}
