package venuebooking.ui;

import venuebooking.model.Auditorium;
import venuebooking.model.HallRoom;
import venuebooking.model.MeetingRoom;
import venuebooking.model.Venue;
import venuebooking.repository.VenueRepository;
import venuebooking.service.BookingService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class VenueCatalogPanel extends JPanel {
    private final VenueRepository venueRepository;
    private final Runnable onDataChanged;

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JLabel imageLabel = new JLabel("Pilih ruangan", SwingConstants.CENTER);
    private final JLabel nameLabel = new JLabel("-");
    private final JLabel infoLabel = new JLabel("-");
    private final JLabel facilitiesLabel = new JLabel("-");
    private final JLabel pricingLabel = new JLabel("-");
    private List<Venue> displayedVenues;

    public VenueCatalogPanel(VenueRepository venueRepository, Runnable onDataChanged) {
        this.venueRepository = venueRepository;
        this.onDataChanged = onDataChanged;

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        String[] columns = {"ID", "Nama Ruangan", "Kategori", "Kapasitas", "Tarif/Jam", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updatePreview();
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(createPreviewPanel(), BorderLayout.EAST);
        add(createActionPanel(), BorderLayout.SOUTH);

        refreshTable();
    }

    private JPanel createPreviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setPreferredSize(new Dimension(300, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Detail & Pratinjau Ruangan"));

        imageLabel.setPreferredSize(new Dimension(280, 170));
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(imageLabel, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridLayout(4, 1, 4, 4));
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 13f));
        infoPanel.add(nameLabel);
        infoPanel.add(infoLabel);
        infoPanel.add(facilitiesLabel);
        infoPanel.add(pricingLabel);
        panel.add(infoPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createActionPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JButton addButton = new JButton("Tambah Ruangan");
        JButton toggleButton = new JButton("Ubah Status");
        JButton removeButton = new JButton("Hapus Ruangan");

        addButton.addActionListener(e -> showAddVenueDialog());
        toggleButton.addActionListener(e -> toggleSelectedStatus());
        removeButton.addActionListener(e -> deleteSelectedVenue());

        buttonPanel.add(addButton);
        buttonPanel.add(toggleButton);
        buttonPanel.add(removeButton);
        return buttonPanel;
    }

    public void refreshTable() {
        int selectedRow = table.getSelectedRow();
        displayedVenues = venueRepository.getAll();
        tableModel.setRowCount(0);
        for (Venue venue : displayedVenues) {
            tableModel.addRow(new Object[]{
                    venue.getId(),
                    venue.getName(),
                    venue.getCategory(),
                    venue.getCapacity() + " org",
                    BookingService.formatRupiah(venue.getHourlyRate()),
                    venue.isAvailable() ? "Tersedia" : "Maintenance"
            });
        }
        if (!displayedVenues.isEmpty()) {
            int rowToSelect = (selectedRow >= 0 && selectedRow < displayedVenues.size()) ? selectedRow : 0;
            table.setRowSelectionInterval(rowToSelect, rowToSelect);
        } else {
            updatePreview();
        }
    }

    private void updatePreview() {
        int row = table.getSelectedRow();
        if (row < 0 || displayedVenues == null || row >= displayedVenues.size()) {
            imageLabel.setIcon(null);
            imageLabel.setText(displayedVenues == null || displayedVenues.isEmpty()
                    ? "Belum ada ruangan" : "Pilih ruangan");
            nameLabel.setText("-");
            infoLabel.setText("-");
            facilitiesLabel.setText("-");
            pricingLabel.setText("-");
            return;
        }

        Venue venue = displayedVenues.get(row);
        ImageIcon icon = loadVenueIcon(venue.getImagePath());
        if (icon != null && icon.getIconWidth() > 0) {
            imageLabel.setIcon(icon);
            imageLabel.setText("");
        } else {
            imageLabel.setIcon(null);
            imageLabel.setText("Gambar tidak tersedia");
        }

        nameLabel.setText(venue.getName() + " (" + venue.getId() + ")");
        infoLabel.setText("Kapasitas: " + venue.getCapacity() + " orang | "
                + (venue.isAvailable() ? "Tersedia" : "Maintenance"));
        facilitiesLabel.setText("<html><b>Fasilitas:</b> " + venue.getFacilities() + "</html>");
        pricingLabel.setText("<html><b>Aturan Tarif:</b> " + venue.getPricingNote() + "</html>");
    }

    private ImageIcon loadVenueIcon(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }
        File imgFile = new File(imagePath);
        if (imgFile.exists()) {
            return new ImageIcon(imagePath);
        }
        String normalized = imagePath.startsWith("/") ? imagePath : "/" + imagePath;
        java.net.URL resourceUrl = getClass().getResource(normalized);
        return resourceUrl != null ? new ImageIcon(resourceUrl) : null;
    }

    private void toggleSelectedStatus() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= displayedVenues.size()) {
            JOptionPane.showMessageDialog(this, "Pilih ruangan pada tabel terlebih dahulu.",
                    "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Venue venue = displayedVenues.get(row);
        venue.setAvailable(!venue.isAvailable());
        venueRepository.saveAll();
        refreshTable();
        onDataChanged.run();
    }

    private void showAddVenueDialog() {
        JComboBox<String> categoryBox = new JComboBox<>(new String[]{"Auditorium", "Hall / Aula", "Meeting Room"});
        JTextField nameField = new JTextField();
        JTextField capacityField = new JTextField();
        JTextField rateField = new JTextField();
        JTextField facilitiesField = new JTextField();

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.add(new JLabel("Kategori:"));
        form.add(categoryBox);
        form.add(new JLabel("Nama Ruangan:"));
        form.add(nameField);
        form.add(new JLabel("Kapasitas (orang):"));
        form.add(capacityField);
        form.add(new JLabel("Tarif per Jam (Rp):"));
        form.add(rateField);
        form.add(new JLabel("Fasilitas:"));
        form.add(facilitiesField);

        int option = JOptionPane.showConfirmDialog(this, form, "Tambah Ruangan Baru",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        String name = nameField.getText().trim();
        String facilities = facilitiesField.getText().trim();
        if (name.isEmpty() || facilities.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama ruangan dan fasilitas wajib diisi.",
                    "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int capacity;
        double rate;
        try {
            capacity = Integer.parseInt(capacityField.getText().trim());
            rate = Double.parseDouble(rateField.getText().trim());
            if (capacity <= 0 || rate <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Kapasitas dan tarif harus angka lebih dari 0.",
                    "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedCategory = (String) categoryBox.getSelectedItem();
        Venue newVenue;
        if ("Auditorium".equals(selectedCategory)) {
            newVenue = new Auditorium(venueRepository.generateNextId("AUD"), name, capacity, rate,
                    facilities, "assets/images/auditorium.png", true);
        } else if ("Hall / Aula".equals(selectedCategory)) {
            newVenue = new HallRoom(venueRepository.generateNextId("HAL"), name, capacity, rate,
                    facilities, "assets/images/hall.png", true);
        } else {
            newVenue = new MeetingRoom(venueRepository.generateNextId("MTG"), name, capacity, rate,
                    facilities, "assets/images/meeting_room.png", true);
        }

        venueRepository.addVenue(newVenue);
        refreshTable();
        onDataChanged.run();
        JOptionPane.showMessageDialog(this, "Ruangan baru berhasil ditambahkan: " + newVenue.getId(),
                "Sukses", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteSelectedVenue() {
        int row = table.getSelectedRow();
        if (row < 0 || displayedVenues == null || row >= displayedVenues.size()) {
            JOptionPane.showMessageDialog(this, "Pilih ruangan pada tabel terlebih dahulu.",
                    "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Venue venue = displayedVenues.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Hapus ruangan " + venue.getName() + " (" + venue.getId() + ")?",
                "Konfirmasi Hapus Ruangan", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            venueRepository.removeVenue(venue.getId());
            refreshTable();
            onDataChanged.run();
        }
    }
}
