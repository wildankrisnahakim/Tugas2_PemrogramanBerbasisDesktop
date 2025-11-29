import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

class Menu {
    private String name;
    private String category; // "makanan" atau "minuman"
    private int price;       // dalam rupiah

    public Menu(String name, String category, int price) {
        this.name = name;
        this.category = category.toLowerCase();
        this.price = price;
    }

    public String getName() { return name; }
    public String getCategory() { return category; }
    public int getPrice() { return price; }

    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category.toLowerCase(); }
    public void setPrice(int price) { this.price = price; }

    @Override
    public String toString() {
        return name + " (" + capitalize(category) + ") - " + formatRp(price);
    }

    private static String capitalize(String s){
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }

    private static String formatRp(int v) {
        NumberFormat IDR = NumberFormat.getCurrencyInstance(new Locale("id","ID"));
        return IDR.format(v);
    }
}

public class RestaurantAppT2 {

    private static final Scanner sc = new Scanner(System.in);
    private static final NumberFormat IDR = NumberFormat.getCurrencyInstance(new Locale("id","ID"));

    // Kebijakan harga / promo
    private static final double TAX_RATE = 0.10;      // 10%
    private static final int SERVICE_FEE = 20_000;    // Rp 20.000
    private static final int BOGO_THRESHOLD = 50_000; // Pesan > 50k memicu pesan promo BOGO
    private static final int DISC_THRESHOLD = 100_000;// Diskon 10% jika > 100k
    private static final double DISC_RATE = 0.10;     // 10%

    // Data menu (minimal 4 makanan + 4 minuman, bisa diedit via pengelolaan)
    private static final ArrayList<Menu> menus = new ArrayList<>();

    public static void main(String[] args) {
        initSampleMenu();
        mainMenuLoop();
        System.out.println("Program selesai. Terima kasih!");
    }

    // =================== MAIN MENU ===================
    private static void mainMenuLoop() {
        boolean running = true;
        while (running) { // while loop
            System.out.println("\n=== APLIKASI RESTORAN ===");
            System.out.println("1. Menu Pelanggan (Pesan)");
            System.out.println("2. Menu Pengelolaan (Pemilik)");
            System.out.println("3. Keluar");
            System.out.print("Pilih (1-3): ");
            String choice = sc.nextLine().trim();

            switch (choice) { // switch-case
                case "1":
                    customerOrderFlow();
                    break;
                case "2":
                    managementFlow();
                    break;
                case "3":
                    running = false;
                    break;
                default:
                    System.out.println("Pilihan tidak valid. Silakan pilih 1-3.");
            }
        }
    }

    // =================== CUSTOMER FLOW ===================
    private static void customerOrderFlow() {
        System.out.println("\n== MENU PELANGGAN ==");
        printMenuGrouped();

        ArrayList<Menu> orderedItems = new ArrayList<>();
        ArrayList<Integer> orderedQtys = new ArrayList<>();

        System.out.println("\nKetik nama menu yang ingin dipesan.");
        System.out.println("Ketik 'selesai' jika sudah selesai memesan.");

        while (true) { // input berulang sampai 'selesai'
            System.out.print("Nama Menu (atau 'selesai'): ");
            String input = sc.nextLine().trim();
            if (input.equalsIgnoreCase("selesai")) break;

            if (input.isEmpty()) {
                System.out.println("Input kosong, coba lagi.");
                continue;
            }

            Menu selected = findMenuByName(input);
            if (selected == null) {
                System.out.println("Menu tidak ditemukan. Silakan input kembali.");
                continue; // catatan: jika teks di luar menu, sistem minta input lagi
            }

            int qty = 0;
            // do-while: validasi jumlah
            do {
                System.out.print("Jumlah untuk '" + selected.getName() + "': ");
                String qstr = sc.nextLine().trim();
                try {
                    qty = Integer.parseInt(qstr);
                    if (qty <= 0) {
                        System.out.println("Jumlah harus > 0.");
                        qty = 0;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Masukkan angka bulat.");
                    qty = 0;
                }
            } while (qty == 0);

            // Gabungkan jika item yang sama sudah ada dalam pesanan
            boolean found = false;
            for (int i = 0; i < orderedItems.size(); i++) { // for loop
                if (orderedItems.get(i).getName().equalsIgnoreCase(selected.getName())) {
                    orderedQtys.set(i, orderedQtys.get(i) + qty);
                    found = true;
                    break;
                }
            }
            if (!found) {
                orderedItems.add(selected);
                orderedQtys.add(qty);
            }

            System.out.println(selected.getName() + " x" + qty + " ditambahkan ke pesanan.");
        }

        if (orderedItems.isEmpty()) {
            System.out.println("Belum ada pesanan. Kembali ke menu utama.");
            return;
        }

        // Hitung subtotal
        int subtotal = 0;
        for (int i = 0; i < orderedItems.size(); i++) {
            subtotal += orderedItems.get(i).getPrice() * orderedQtys.get(i);
        }

        // Cek apakah ada minuman dipesan
        boolean hasDrink = false;
        for (Menu m : orderedItems) { // for-each
            if ("minuman".equals(m.getCategory())) {
                hasDrink = true;
                break;
            }
        }

        // Tentukan pesan promo BOGO (tidak mengurangi harga sama sekali)
        String bogoMessage = null;
        if (subtotal > BOGO_THRESHOLD) {              // nested if
            if (hasDrink) {
                bogoMessage = "Selamat kamu mendapatkan promo Buy 1 get 1 untuk minuman yang kamu pesan";
            } else {
                bogoMessage = "Selamat kamu mendapatkan promo Buy 1 get 1 untuk pemesanan minuman selanjutnya";
            }
        }

        // Diskon 10% jika subtotal > 100.000
        int percentDiscount = 0;
        if (subtotal > DISC_THRESHOLD) {              // if-else-if konsep
            percentDiscount = (int) Math.round(subtotal * DISC_RATE);
        }

        int afterDiscounts = subtotal - percentDiscount;

        // Pajak + layanan
        int tax = (int) Math.round(afterDiscounts * TAX_RATE);
        int grandTotal = afterDiscounts + tax + SERVICE_FEE;

        // Cetak struk
        printReceipt(orderedItems, orderedQtys,
                subtotal, percentDiscount, tax, SERVICE_FEE, grandTotal,
                bogoMessage);
    }

    // =================== MANAGEMENT FLOW ===================
    private static void managementFlow() {
        boolean inManage = true;
        while (inManage) { // while loop untuk menu pengelolaan
            System.out.println("\n== MENU PENGELOLAAN (Pemilik) ==");
            System.out.println("1. Tambah Menu");
            System.out.println("2. Ubah Menu");
            System.out.println("3. Hapus Menu");
            System.out.println("4. Tampilkan Daftar Menu");
            System.out.println("5. Kembali ke Menu Utama");
            System.out.print("Pilih (1-5): ");
            String ch = sc.nextLine().trim();

            switch (ch) {
                case "1":
                    addMenuFlow();
                    break;
                case "2":
                    editMenuFlow();
                    break;
                case "3":
                    deleteMenuFlow();
                    break;
                case "4":
                    printMenuGrouped();
                    break;
                case "5":
                    inManage = false;
                    break;
                default:
                    System.out.println("Pilihan tidak valid.");
            }
        }
    }

    // Tambah menu
    private static void addMenuFlow() {
        System.out.println("\n== TAMBAH MENU ==");
        System.out.print("Nama menu: ");
        String name = sc.nextLine().trim();

        String category;
        while (true) {
            System.out.print("Kategori (makanan/minuman): ");
            category = sc.nextLine().trim().toLowerCase();
            if (category.equals("makanan") || category.equals("minuman")) break;
            System.out.println("Kategori harus 'makanan' atau 'minuman'.");
        }

        int price = 0;
        while (price <= 0) {
            System.out.print("Harga (angka): ");
            String p = sc.nextLine().trim();
            try {
                price = Integer.parseInt(p);
                if (price <= 0) System.out.println("Harga harus > 0.");
            } catch (NumberFormatException e) {
                System.out.println("Masukkan angka.");
            }
        }

        System.out.println("Konfirmasi: Tambah menu -> " + name + " | " + category + " | " + IDR.format(price));
        System.out.print("Ketik 'Ya' untuk konfirmasi: ");
        String conf = sc.nextLine().trim();
        if (conf.equalsIgnoreCase("ya")) {
            menus.add(new Menu(name, category, price));
            System.out.println("Menu ditambahkan.");
        } else {
            System.out.println("Penambahan dibatalkan.");
        }
    }

    // Ubah menu
    private static void editMenuFlow() {
        System.out.println("\n== UBAH MENU ==");
        if (menus.isEmpty()) {
            System.out.println("Belum ada menu.");
            return;
        }
        printMenuIndexed();
        int idx = chooseMenuIndex("ubah");
        if (idx == -1) return;

        Menu m = menus.get(idx);
        System.out.println("Menu terpilih: " + m.toString());

        System.out.print("Ubah nama (kosongkan untuk tidak mengubah): ");
        String newName = sc.nextLine().trim();
        System.out.print("Ubah kategori (makanan/minuman) (kosongkan untuk tidak mengubah): ");
        String newCat = sc.nextLine().trim();
        System.out.print("Ubah harga (angka) (kosongkan untuk tidak mengubah): ");
        String newPriceStr = sc.nextLine().trim();

        String finalName = newName.isEmpty() ? m.getName() : newName;
        String finalCat = newCat.isEmpty() ? m.getCategory() : newCat.toLowerCase();
        int finalPrice = m.getPrice();

        if (!newPriceStr.isEmpty()) {
            try {
                int p = Integer.parseInt(newPriceStr);
                if (p > 0) finalPrice = p;
                else System.out.println("Harga tidak valid, tetap menggunakan harga lama.");
            } catch (NumberFormatException e) {
                System.out.println("Harga tidak valid, tetap menggunakan harga lama.");
            }
        }

        System.out.println("Konfirmasi perubahan menjadi: " + finalName + " | " + finalCat + " | " + IDR.format(finalPrice));
        System.out.print("Ketik 'Ya' untuk konfirmasi: ");
        String conf = sc.nextLine().trim();
        if (conf.equalsIgnoreCase("ya")) {
            m.setName(finalName);
            m.setCategory(finalCat);
            m.setPrice(finalPrice);
            System.out.println("Perubahan disimpan.");
        } else {
            System.out.println("Perubahan dibatalkan.");
        }
    }

    // Hapus menu
    private static void deleteMenuFlow() {
        System.out.println("\n== HAPUS MENU ==");
        if (menus.isEmpty()) {
            System.out.println("Belum ada menu.");
            return;
        }
        printMenuIndexed();
        int idx = chooseMenuIndex("hapus");
        if (idx == -1) return;

        Menu m = menus.get(idx);
        System.out.println("Konfirmasi: Hapus menu -> " + m.toString());
        System.out.print("Ketik 'Ya' untuk konfirmasi: ");
        String conf = sc.nextLine().trim();
        if (conf.equalsIgnoreCase("ya")) {
            menus.remove(idx);
            System.out.println("Menu dihapus.");
        } else {
            System.out.println("Penghapusan dibatalkan.");
        }
    }

    // Pilih indeks menu dengan validasi
    private static int chooseMenuIndex(String action) {
        int idx = -1;
        while (true) { // while loop validasi input
            System.out.print("Masukkan nomor menu untuk " + action + " (atau 'batal'): ");
            String s = sc.nextLine().trim();
            if (s.equalsIgnoreCase("batal")) return -1;
            try {
                int num = Integer.parseInt(s);
                if (num >= 1 && num <= menus.size()) {
                    idx = num - 1;
                    break;
                } else {
                    System.out.println("Nomor di luar jangkauan.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Masukkan nomor valid.");
            }
        }
        return idx;
    }

    // =================== PRINT / UTIL ===================
    private static void printMenuGrouped() {
        System.out.println("---- Daftar Menu (Makanan) ----");
        for (Menu m : menus) { // for-each
            if ("makanan".equals(m.getCategory())) {
                System.out.println(" - " + m.toString());
            }
        }
        System.out.println("---- Daftar Menu (Minuman) ----");
        for (Menu m : menus) {
            if ("minuman".equals(m.getCategory())) {
                System.out.println(" - " + m.toString());
            }
        }
    }

    private static void printMenuIndexed() {
        System.out.println("Daftar menu:");
        for (int i = 0; i < menus.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, menus.get(i).toString());
        }
    }

    private static void printReceipt(ArrayList<Menu> items, ArrayList<Integer> qtys,
                                     int subtotal, int percentDiscount,
                                     int tax, int serviceFee, int grandTotal,
                                     String bogoMessage) {
        System.out.println("\n=========== STRUK PEMBAYARAN ===========");
        System.out.printf("%-25s %4s %14s %14s%n", "Item", "Qty", "Harga Satuan", "Total/Item");
        System.out.println("-------------------------------------------------------------");

        for (int i = 0; i < items.size(); i++) {
            Menu m = items.get(i);
            int q = qtys.get(i);
            System.out.printf("%-25s %4d %14s %14s%n",
                    m.getName(), q, IDR.format(m.getPrice()), IDR.format(m.getPrice() * q));
        }

        System.out.println("-------------------------------------------------------------");

        // Tampilkan pesan promo BOGO jika ada (tidak mengurangi biaya)
        if (bogoMessage != null) {
            System.out.println(">> " + bogoMessage);
        }

        System.out.printf("%-40s %14s%n", "Subtotal", IDR.format(subtotal));
        if (percentDiscount > 0) {
            System.out.printf("%-40s %14s%n", "Diskon 10% (> 100rb)", "-" + IDR.format(percentDiscount));
        } else {
            System.out.printf("%-40s %14s%n", "Diskon 10%", IDR.format(0));
        }

        int after = subtotal - percentDiscount;
        System.out.printf("%-40s %14s%n", "DPP (setelah diskon)", IDR.format(after));
        System.out.printf("%-40s %14s%n", "Pajak 10%", IDR.format(tax));
        System.out.printf("%-40s %14s%n", "Biaya Layanan", IDR.format(serviceFee));
        System.out.println("-------------------------------------------------------------");
        System.out.printf("%-40s %14s%n", "Grand Total", IDR.format(grandTotal));
        System.out.println("============================================");
    }

    private static Menu findMenuByName(String raw) {
        if (raw == null) return null;
        String name = raw.trim().toLowerCase();

        for (Menu m : menus) {
            if (m.getName().equalsIgnoreCase(name)) return m;
        }
        for (Menu m : menus) {
            if (m.getName().toLowerCase().contains(name)) return m;
        }
        return null;
    }

    private static void initSampleMenu() {
        menus.add(new Menu("Nasi Padang", "makanan", 25000));
        menus.add(new Menu("Ayam Bakar", "makanan", 30000));
        menus.add(new Menu("Sate Ayam", "makanan", 28000));
        menus.add(new Menu("Mie Goreng", "makanan", 22000));

        menus.add(new Menu("Teh Manis", "minuman", 8000));
        menus.add(new Menu("Es Jeruk", "minuman", 10000));
        menus.add(new Menu("Kopi Hitam", "minuman", 12000));
        menus.add(new Menu("Jus Alpukat", "minuman", 18000));
    }
}
