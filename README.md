# Aplikasi-Venue-Booking

Aplikasi desktop berbasis **Java Swing** (_Sistem Reservasi Gedung & Ruangan_) untuk petugas resepsionis/kasir dalam mengelola katalog gedung & ruangan, menginput reservasi baru dengan pengecekan bentrok jadwal otomatis, serta memantau jadwal & riwayat pemesanan. Data disimpan secara permanen ke file lokal CSV (`data/venues.csv` dan `data/bookings.csv`).

---

## Penerapan Konsep PBO & Java GUI

- **Encapsulation**: Seluruh atribut pada class model dan komponen UI bersifat `private` / `private final`, diakses melalui getter/setter tervalidasi.
- **Inheritance**: `Auditorium`, `HallRoom`, dan `MeetingRoom` mewarisi abstract class `Venue`. Pada GUI, `MainFrame extends JFrame` dan panel halaman `extends JPanel`.
- **Abstraction & Interface**: `abstract class Venue` (method abstrak `getCategory()`, `calculatePrice()`, `getPricingNote()`), interface `CsvSerializable`, serta `ActionListener` untuk event tombol.
- **Polymorphism**:
  - _Overriding_: Perhitungan tarif sewa berbeda di setiap subclass ruangan (`Auditorium` menambah biaya sound & panggung tetap, `HallRoom` diskon 10% untuk sewa ≥ 5 jam, `MeetingRoom` tarif normal per jam).
  - _Overloading_: Method `filterBookings(String keyword)` dan `filterBookings(String keyword, String statusFilter)` pada `BookingService`.
- **Exception Handling**: `BookingException` untuk validasi input kosong, kapasitas terlampaui, jam operasional (08:00–22:00), dan bentrok jadwal.
- **Collections & File I/O**: `ArrayList<Venue>` dan `ArrayList<Booking>` disinkronkan ke CSV berdelimiter `|`.
- **Java Swing & Layout Managers**: `SwingUtilities.invokeLater`, kombinasi `BorderLayout`, `GridLayout`, `FlowLayout`, `JTable`, `ImageIcon` pada `JLabel`, dan dialog `JOptionPane`.

---

## Menjalankan Executable (`.exe` / `.jar`)

1. **Windows Executable (tanpa perlu Java terinstal):**
   Klik dua kali `dist\VenueBooking\VenueBooking.exe`.
2. **Runnable JAR (kompatibel Java 8+):**
   Klik dua kali `VenueBooking.jar`, atau jalankan lewat terminal:
   ```bat
   java -jar VenueBooking.jar
   ```
3. **Rebuild setelah edit kode:**
   Jalankan `build.bat` (melalui CMD / PowerShell / klik dua kali) untuk mengompilasi ulang dan membuat ulang `VenueBooking.jar`.

---

## Cara Menjalankan Aplikasi dari Source

Pastikan JDK sudah terpasang (`java` dan `javac`). Clone repositori dan jalankan dari folder `Aplikasi-Venue-Booking`:

```bat
git clone https://github.com/Msthxfa/Aplikasi-Venue-Booking.git
cd Aplikasi-Venue-Booking
javac --release 8 -encoding UTF-8 -sourcepath src -d out src/venuebooking/Main.java
java -cp out venuebooking.Main
```

Saat dijalankan pertama kali, aplikasi otomatis membuat `data/venues.csv` (kosong, ruangan dapat ditambahkan melalui tab **Katalog Ruangan**) dan `data/bookings.csv`.
