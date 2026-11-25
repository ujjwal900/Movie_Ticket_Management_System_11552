import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ============================================================================
 * MOVIE TICKET BOOKING MANAGEMENT SYSTEM (SINGLE FILE VERSION)
 * ============================================================================
 * Course: Data Structures and Algorithms (DSA)
 * Technology: Pure Java (Core Java Standard Edition)
 * Zero external frameworks or dependencies required.
 *
 * HOW TO COMPILE & RUN:
 *   1. Compile: javac MovieTicketBookingSystem.java
 *   2. Run:     java MovieTicketBookingSystem
 * ============================================================================
 * DATA STRUCTURES & ALGORITHMS IMPLEMENTED:
 *   1. 2D Array (char[][]): Auditorium seating layout matrix ('O' = Available, 'X' = Booked)
 *   2. ArrayList: Dynamic collection for movies and show schedules
 *   3. HashMap: Constant time O(1) reservation lookups by unique Booking ID
 *   4. LinkedList: Chronological customer booking history with O(1) head insertion
 *   5. Stack (LIFO): Recently cancelled bookings for O(1) Undo Cancellation
 *   6. Queue (FIFO): First-Come-First-Served waitlist for standard customers
 *   7. PriorityQueue (Min-Heap): VIP priority waitlist serving tier-1 members first
 *   8. Merge Sort: O(N log N) divide-and-conquer sorting of movie titles (A to Z)
 *   9. Quick Sort: O(N log N) recursive pivot partitioning of IMDB ratings
 *  10. Selection Sort: O(N^2) in-place comparison sorting of ticket base prices
 *  11. Binary Search: O(log N) logarithmic exact title search on sorted catalog
 *  12. Linear Search: O(N) substring search across titles, genres, and languages
 *
 * OOP CONCEPTS IMPLEMENTED:
 *   - Encapsulation: Private fields with validated getters, setters & locks
 *   - Abstraction: Abstract User base class & PaymentMethod interface
 *   - Inheritance: Customer and Admin extending User
 *   - Polymorphism: Overridden dashboard/role methods & Payment Strategy Pattern
 * ============================================================================
 */
public class MovieTicketBookingSystem {

    private static MovieManager movieManager;
    private static BookingManager bookingManager;
    private static Map<String, User> userDatabase;
    private static List<Theatre> theatreList;
    private static Scanner scanner;

    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        initializeSystem();

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = InputValidator.readInt(scanner, "Enter your choice (1-10): ", 1, 10);

            switch (choice) {
                case 1:  handleAdminLogin(); break;
                case 2:  handleCustomerRegistration(); break;
                case 3:  handleCustomerLogin(); break;
                case 4:  displayAllMoviesAndShows(); break;
                case 5:  handleSearchMovies(); break;
                case 6:  handleSortMovies(); break;
                case 7:  handleQuickBookingGuestPrompt(); break;
                case 8:  handleCancellationPrompt(); break;
                case 9:  handleUndoCancellation(); break;
                case 10:
                    running = false;
                    System.out.println("\n  👋 Thank you for using CineDSA Movie Ticket Booking System. Goodbye!");
                    break;
            }
        }
        scanner.close();
    }

    private static void initializeSystem() {
        movieManager = new MovieManager();
        bookingManager = new BookingManager();
        userDatabase = new HashMap<>();
        theatreList = new ArrayList<>();

        // 1. Seed Theatres
        Theatre t1 = new Theatre("TH01", "PVR Cinemas, Phoenix Mall", "Downtown", 4);
        Theatre t2 = new Theatre("TH02", "INOX Megaplex, Galaxy Mall", "Westside", 6);
        Theatre t3 = new Theatre("TH03", "Cinepolis VIP, Royal Square", "North Arena", 3);
        theatreList.add(t1);
        theatreList.add(t2);
        theatreList.add(t3);

        // 2. Seed Shows across auditoriums (6 rows x 8 columns = 48 seats each)
        List<Movie> movies = movieManager.getMovieList();
        bookingManager.addShow(new Show("SH101", movies.get(0), t1, "10:30 AM", 280.0, 6, 8)); // Inception
        bookingManager.addShow(new Show("SH102", movies.get(0), t2, "06:45 PM", 300.0, 6, 8)); // Inception
        bookingManager.addShow(new Show("SH103", movies.get(1), t1, "02:15 PM", 300.0, 6, 8)); // Interstellar
        bookingManager.addShow(new Show("SH104", movies.get(2), t3, "08:00 PM", 350.0, 5, 8)); // The Dark Knight
        bookingManager.addShow(new Show("SH105", movies.get(3), t2, "11:00 AM", 220.0, 6, 8)); // Dangal
        bookingManager.addShow(new Show("SH106", movies.get(4), t1, "05:30 PM", 200.0, 6, 8)); // Spirited Away
        bookingManager.addShow(new Show("SH107", movies.get(5), t3, "09:15 PM", 380.0, 6, 8)); // Avengers: Endgame

        // 3. Seed Users in HashMap (DSA 3)
        Admin admin = new Admin("U001", "System Administrator", "admin@cinema.com", "admin123", "Operations", 1);
        Customer cust1 = new Customer("U101", "John Doe", "john@gmail.com", "pass123", "9876543210", false);
        Customer cust2 = new Customer("U102", "Alice Smith", "alice@vip.com", "pass123", "9123456780", true);

        userDatabase.put(admin.getEmail().toLowerCase(), admin);
        userDatabase.put(cust1.getEmail().toLowerCase(), cust1);
        userDatabase.put(cust2.getEmail().toLowerCase(), cust2);

        // 4. Pre-book sample seats for initial display realism
        Show s1 = bookingManager.findShowById("SH101");
        bookingManager.createBooking(cust1, s1, Arrays.asList("A1", "A2", "B3"), "Credit Card");
    }

    private static void printMainMenu() {
        System.out.println("\n==================================================================");
        System.out.println("            🎬 MOVIE TICKET BOOKING MANAGEMENT SYSTEM 🎬           ");
        System.out.println("==================================================================");
        System.out.println("  1.  Admin Login");
        System.out.println("  2.  Customer Registration");
        System.out.println("  3.  Customer Login");
        System.out.println("  4.  View All Movies & Shows (Guest Mode)");
        System.out.println("  5.  Search Movies (DSA Linear & Binary Search)");
        System.out.println("  6.  Sort Movies (DSA Merge, Quick & Selection Sort)");
        System.out.println("  7.  Book Movie Tickets (2D Seating Matrix)");
        System.out.println("  8.  Cancel Ticket by Booking ID (DSA Stack Push)");
        System.out.println("  9.  Undo Last Cancellation (DSA Stack Pop)");
        System.out.println("  10. Exit Application");
        System.out.println("==================================================================");
    }

    // =========================================================================
    // AUTHENTICATION MODULE
    // =========================================================================

    private static void handleAdminLogin() {
        System.out.println("\n--- ADMIN LOGIN ---");
        System.out.println("  (Demo Credentials: admin@cinema.com / admin123)");
        String email = InputValidator.readNonEmptyString(scanner, "  Enter Admin Email: ").trim().toLowerCase();
        String pass = InputValidator.readNonEmptyString(scanner, "  Enter Password: ").trim();

        User user = userDatabase.get(email); // O(1) HashMap lookup
        if (user instanceof Admin && user.getPassword().equals(pass)) {
            System.out.println("  ✅ Admin Authentication Successful!");
            runAdminMenu((Admin) user);
        } else {
            System.out.println("  ❌ Access Denied: Invalid admin credentials.");
        }
    }

    private static void handleCustomerRegistration() {
        System.out.println("\n--- NEW CUSTOMER REGISTRATION ---");
        String name = InputValidator.readNonEmptyString(scanner, "  Enter Full Name: ");
        String email = InputValidator.readNonEmptyString(scanner, "  Enter Email Address: ").trim().toLowerCase();

        if (userDatabase.containsKey(email)) {
            System.out.println("  ❌ Registration Error: An account with this email already exists!");
            return;
        }

        String pass = InputValidator.readNonEmptyString(scanner, "  Create Password: ");
        String phone = InputValidator.readNonEmptyString(scanner, "  Enter Phone Number: ");
        boolean isVip = InputValidator.readYesNo(scanner, "  Join as VIP Member? (Priority booking & 10% discount)");

        String newId = "U" + (userDatabase.size() + 101);
        Customer newCustomer = new Customer(newId, name, email, pass, phone, isVip);
        userDatabase.put(email, newCustomer);
        System.out.println("  🎉 Registration Successful! Welcome, " + name + "!");
        System.out.println("     Customer ID: " + newId + (isVip ? " [VIP Tier]" : " [Standard Tier]"));
    }

    private static void handleCustomerLogin() {
        System.out.println("\n--- CUSTOMER LOGIN ---");
        System.out.println("  (Demo Accounts: john@gmail.com or alice@vip.com / pass123)");
        String email = InputValidator.readNonEmptyString(scanner, "  Enter Email Address: ").trim().toLowerCase();
        String pass = InputValidator.readNonEmptyString(scanner, "  Enter Password: ").trim();

        User user = userDatabase.get(email);
        if (user instanceof Customer && user.getPassword().equals(pass)) {
            System.out.println("  ✅ Login Successful!");
            runCustomerMenu((Customer) user);
        } else {
            System.out.println("  ❌ Login Failed: Invalid email or password.");
        }
    }

    // =========================================================================
    // ADMIN DASHBOARD
    // =========================================================================

    private static void runAdminMenu(Admin admin) {
        boolean inAdmin = true;
        while (inAdmin) {
            admin.displayDashboard();
            System.out.println("  1. View All Movies");
            System.out.println("  2. Add New Movie to ArrayList");
            System.out.println("  3. Remove Movie from Catalog");
            System.out.println("  4. Update Movie Details");
            System.out.println("  5. View All Shows & Seating Capacity");
            System.out.println("  6. Schedule New Show Timing");
            System.out.println("  7. Update Show Ticket Price");
            System.out.println("  8. View All Bookings (HashMap Storage)");
            System.out.println("  9. View Recent Cancellations Stack (DSA Stack)");
            System.out.println("  10. View Waiting Lists (Queue & PriorityQueue)");
            System.out.println("  11. Logout to Main Menu");
            System.out.println("--------------------------------------------------");

            int choice = InputValidator.readInt(scanner, "  Admin Option (1-11): ", 1, 11);
            switch (choice) {
                case 1:  MovieManager.printMovieTable(movieManager.getMovieList()); break;
                case 2:  adminAddMovie(); break;
                case 3:  adminRemoveMovie(); break;
                case 4:  adminUpdateMovie(); break;
                case 5:  displayAllShowsTable(); break;
                case 6:  adminAddShow(); break;
                case 7:  adminUpdateTicketPrice(); break;
                case 8:  bookingManager.displayAllBookings(); break;
                case 9:  bookingManager.displayRecentCancellations(); break;
                case 10: adminViewWaitingLists(); break;
                case 11: inAdmin = false; break;
            }
        }
    }

    private static void adminAddMovie() {
        System.out.println("\n--- ADD NEW MOVIE (ArrayList Insertion) ---");
        String id = "M" + (movieManager.getMovieList().size() + 101);
        String title = InputValidator.readNonEmptyString(scanner, "  Enter Movie Title: ");
        String genre = InputValidator.readNonEmptyString(scanner, "  Enter Genre (Action, Sci-Fi, Drama): ");
        String lang = InputValidator.readNonEmptyString(scanner, "  Enter Language: ");
        int duration = InputValidator.readInt(scanner, "  Enter Duration (minutes): ", 30, 360);
        double rating = InputValidator.readDouble(scanner, "  Enter IMDB Rating (0.0 to 10.0): ", 0.0, 10.0);
        double price = InputValidator.readDouble(scanner, "  Enter Base Ticket Price (₹): ", 50.0, 2000.0);

        Movie movie = new Movie(id, title, genre, lang, duration, rating, price);
        movieManager.addMovie(movie);
        System.out.println("  ✅ Movie '" + title + "' added successfully with ID: " + id);
    }

    private static void adminRemoveMovie() {
        System.out.println("\n--- REMOVE MOVIE ---");
        MovieManager.printMovieTable(movieManager.getMovieList());
        String id = InputValidator.readNonEmptyString(scanner, "  Enter Movie ID to remove: ").trim();
        if (movieManager.removeMovie(id)) {
            System.out.println("  ✅ Movie " + id + " has been removed from catalog.");
        } else {
            System.out.println("  ❌ Movie ID not found.");
        }
    }

    private static void adminUpdateMovie() {
        System.out.println("\n--- UPDATE MOVIE DETAILS ---");
        MovieManager.printMovieTable(movieManager.getMovieList());
        String id = InputValidator.readNonEmptyString(scanner, "  Enter Movie ID to update: ").trim();
        Movie movie = movieManager.findMovieById(id);
        if (movie == null) {
            System.out.println("  ❌ Movie ID not found.");
            return;
        }

        String newTitle = InputValidator.readNonEmptyString(scanner, "  Enter New Title (" + movie.getTitle() + "): ");
        String newGenre = InputValidator.readNonEmptyString(scanner, "  Enter New Genre: ");
        double newRating = InputValidator.readDouble(scanner, "  Enter New Rating: ", 0.0, 10.0);
        double newPrice = InputValidator.readDouble(scanner, "  Enter New Base Price: ", 50.0, 2000.0);

        movie.setTitle(newTitle);
        movie.setGenre(newGenre);
        movie.setRating(newRating);
        movie.setBasePrice(newPrice);
        System.out.println("  ✅ Movie details updated successfully!");
    }

    private static void adminAddShow() {
        System.out.println("\n--- SCHEDULE NEW SHOW TIMING ---");
        MovieManager.printMovieTable(movieManager.getMovieList());
        String movieId = InputValidator.readNonEmptyString(scanner, "  Enter Movie ID for the show: ").trim();
        Movie movie = movieManager.findMovieById(movieId);
        if (movie == null) {
            System.out.println("  ❌ Movie ID not found.");
            return;
        }

        for (int i = 0; i < theatreList.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + theatreList.get(i));
        }
        int theatreChoice = InputValidator.readInt(scanner, "  Select Theatre (1-" + theatreList.size() + "): ", 1, theatreList.size());
        Theatre theatre = theatreList.get(theatreChoice - 1);

        String showId = "SH" + (bookingManager.getShowList().size() + 101);
        String timing = InputValidator.readNonEmptyString(scanner, "  Enter Show Timing (e.g. 07:30 PM): ");
        double price = InputValidator.readDouble(scanner, "  Enter Show Ticket Price (₹): ", 50.0, 2000.0);

        int rows = InputValidator.readInt(scanner, "  Enter Number of Seating Rows (2-10): ", 2, 10);
        int cols = InputValidator.readInt(scanner, "  Enter Number of Columns (4-12): ", 4, 12);

        Show newShow = new Show(showId, movie, theatre, timing, price, rows, cols);
        bookingManager.addShow(newShow);
        System.out.println("  ✅ Show scheduled successfully! Show ID: " + showId + " (" + (rows * cols) + " seats)");
    }

    private static void adminUpdateTicketPrice() {
        displayAllShowsTable();
        String showId = InputValidator.readNonEmptyString(scanner, "  Enter Show ID: ").trim();
        Show show = bookingManager.findShowById(showId);
        if (show == null) {
            System.out.println("  ❌ Show ID not found.");
            return;
        }
        double newPrice = InputValidator.readDouble(scanner, "  Enter New Ticket Price (Current: ₹" + show.getTicketPrice() + "): ", 50.0, 2000.0);
        show.setTicketPrice(newPrice);
        System.out.println("  ✅ Ticket price updated to ₹" + newPrice + " for Show " + showId);
    }

    private static void adminViewWaitingLists() {
        System.out.println("\n--- SHOW WAITING LIST STATUS (QUEUE & PRIORITYQUEUE) ---");
        for (Show show : bookingManager.getShowList()) {
            System.out.println("\n  Show: " + show.getShowId() + " - " + show.getMovie().getTitle() + " (" + show.getStartTime() + ")");
            System.out.println("  Available Seats: " + show.getSeatManager().getAvailableSeatsCount() + " / " + show.getSeatManager().getTotalSeats());
            System.out.println("  ⭐ VIP PriorityQueue (Min-Heap): " + show.getVipPriorityWaitingQueue().size() + " entries");
            System.out.println("  📋 Standard Queue (FIFO): " + show.getStandardWaitingQueue().size() + " entries");
        }
    }

    // =========================================================================
    // CUSTOMER DASHBOARD
    // =========================================================================

    private static void runCustomerMenu(Customer customer) {
        boolean inCustomer = true;
        while (inCustomer) {
            customer.displayDashboard();
            System.out.println("  1. Browse Available Movies & Shows");
            System.out.println("  2. Search Movies (Linear & Binary Search)");
            System.out.println("  3. Sort Movies (Merge Sort / Quick Sort / Selection Sort)");
            System.out.println("  4. View Seat Map (2D Array) & Book Tickets");
            System.out.println("  5. Cancel a Booking (DSA Stack Push)");
            System.out.println("  6. View My Booking History (DSA LinkedList)");
            System.out.println("  7. Undo Last Cancellation (DSA Stack Pop)");
            System.out.println("  8. Join Waiting Queue (For full shows)");
            System.out.println("  9. Logout to Main Menu");
            System.out.println("--------------------------------------------------");

            int choice = InputValidator.readInt(scanner, "  Customer Option (1-9): ", 1, 9);
            switch (choice) {
                case 1: displayAllMoviesAndShows(); break;
                case 2: handleSearchMovies(); break;
                case 3: handleSortMovies(); break;
                case 4: executeBookingFlow(customer); break;
                case 5: customerCancelFlow(customer); break;
                case 6: displayCustomerBookingHistory(customer); break;
                case 7: handleUndoCancellation(); break;
                case 8: customerJoinWaitlist(customer); break;
                case 9:
                    inCustomer = false;
                    System.out.println("  👋 Logged out successfully. See you again, " + customer.getName() + "!");
                    break;
            }
        }
    }

    private static void handleQuickBookingGuestPrompt() {
        System.out.println("\n--- BOOK MOVIE TICKETS ---");
        System.out.println("  1. Login with existing account");
        System.out.println("  2. Register new account");
        System.out.println("  3. Return to Main Menu");
        int opt = InputValidator.readInt(scanner, "  Select (1-3): ", 1, 3);
        if (opt == 1) handleCustomerLogin();
        else if (opt == 2) handleCustomerRegistration();
    }

    private static void executeBookingFlow(Customer customer) {
        System.out.println("\n==================== STEP 1: SELECT A SHOW ====================");
        displayAllShowsTable();
        String showId = InputValidator.readNonEmptyString(scanner, "  Enter Show ID to Book (e.g. SH101): ").trim();
        Show show = bookingManager.findShowById(showId);

        if (show == null) {
            System.out.println("  ❌ Show ID '" + showId + "' not found.");
            return;
        }

        if (show.getSeatManager().isHousefull()) {
            System.out.println("\n  ⚠️ NOTICE: This show is completely HOUSEFULL (0 seats available)!");
            if (InputValidator.readYesNo(scanner, "  Would you like to join the Waiting List?")) {
                int count = InputValidator.readInt(scanner, "  Number of seats needed: ", 1, 6);
                show.enqueueWaitlist(customer, count);
            }
            return;
        }

        System.out.println("\n==================== STEP 2: SELECT YOUR SEATS (2D ARRAY) ====================");
        System.out.println("  Movie: " + show.getMovie().getTitle() + " | Theatre: " + show.getTheatre().getName());
        System.out.println("  Time : " + show.getStartTime() + " | Ticket Price: ₹" + show.getTicketPrice() + " per seat");
        if (customer.isVip()) {
            System.out.println("  ⭐ VIP Benefit: 10% Discount applied at checkout!");
        }

        show.getSeatManager().displaySeatingChart();

        int seatCount = InputValidator.readInt(scanner, "  How many seats would you like to book? (1-10): ", 1, 10);
        if (seatCount > show.getSeatManager().getAvailableSeatsCount()) {
            System.out.println("  ❌ Only " + show.getSeatManager().getAvailableSeatsCount() + " seats are available.");
            return;
        }

        List<String> chosenSeats = new ArrayList<>();
        System.out.println("  Enter " + seatCount + " seat codes one by one (e.g. A1, A2, B3):");
        for (int i = 1; i <= seatCount; i++) {
            while (true) {
                String seatCode = InputValidator.readNonEmptyString(scanner, "    Seat #" + i + ": ").trim().toUpperCase();
                if (chosenSeats.contains(seatCode)) {
                    System.out.println("    ❌ Duplicate seat " + seatCode + " in your selection! Choose another.");
                    continue;
                }
                if (!show.getSeatManager().isSeatAvailable(seatCode)) {
                    System.out.println("    ❌ Seat " + seatCode + " is invalid or already BOOKED! Choose another.");
                    continue;
                }
                chosenSeats.add(seatCode);
                break;
            }
        }

        double total = bookingManager.calculateTotalPrice(show, seatCount, customer.isVip());
        System.out.println("\n==================== STEP 3: PAYMENT & CONFIRMATION ====================");
        System.out.println("  Seats Selected: " + String.join(", ", chosenSeats));
        System.out.println("  Base Price    : " + seatCount + " x ₹" + show.getTicketPrice() + " = ₹" + (seatCount * show.getTicketPrice()));
        System.out.println("  Taxes (18% GST): ₹" + String.format("%.2f", (seatCount * show.getTicketPrice() * 0.18)));
        if (customer.isVip()) {
            System.out.println("  VIP Discount (10%): -₹" + String.format("%.2f", (seatCount * show.getTicketPrice() * 0.10)));
        }
        System.out.println("  TOTAL PAYABLE : ₹" + String.format("%.2f", total));

        Payment.PaymentMethod payment = Payment.handlePayment(scanner, total);
        if (payment == null) {
            System.out.println("  ❌ Booking aborted due to incomplete payment.");
            return;
        }

        Booking booking = bookingManager.createBooking(customer, show, chosenSeats, payment.getMethodName());
        if (booking != null) {
            System.out.println("\n  🎉 CONGRATULATIONS! YOUR TICKETS ARE CONFIRMED!\n");
            booking.printTicket();
        }
    }

    private static void customerCancelFlow(Customer customer) {
        displayCustomerBookingHistory(customer);
        String bookingId = InputValidator.readNonEmptyString(scanner, "  Enter Booking ID to cancel: ").trim().toUpperCase();
        Booking booking = bookingManager.getBookingMap().get(bookingId);

        if (booking == null || !booking.getCustomer().getUserId().equals(customer.getUserId())) {
            System.out.println("  ❌ Booking ID not found in your account.");
            return;
        }

        if (InputValidator.readYesNo(scanner, "  Are you sure you want to cancel booking " + bookingId + "?")) {
            bookingManager.cancelBooking(bookingId);
        }
    }

    private static void handleCancellationPrompt() {
        System.out.println("\n--- DIRECT CANCEL TICKET ---");
        String bookingId = InputValidator.readNonEmptyString(scanner, "  Enter Booking ID (e.g. BK-1001): ").trim().toUpperCase();
        bookingManager.cancelBooking(bookingId);
    }

    private static void handleUndoCancellation() {
        System.out.println("\n--- UNDO LAST CANCELLATION (DSA STACK LIFO) ---");
        System.out.println("  Pops the most recently cancelled booking from the Stack and restores seats.");
        bookingManager.undoLastCancellation();
    }

    private static void customerJoinWaitlist(Customer customer) {
        System.out.println("\n--- JOIN SHOW WAITING LIST ---");
        displayAllShowsTable();
        String showId = InputValidator.readNonEmptyString(scanner, "  Enter Show ID: ").trim();
        Show show = bookingManager.findShowById(showId);
        if (show == null) {
            System.out.println("  ❌ Show not found.");
            return;
        }
        int seats = InputValidator.readInt(scanner, "  Number of seats requested (1-6): ", 1, 6);
        show.enqueueWaitlist(customer, seats);
    }

    private static void displayCustomerBookingHistory(Customer customer) {
        System.out.println("\n================ BOOKING HISTORY (DSA LINKEDLIST) ================");
        System.out.println("  Customer: " + customer.getName() + " | Total Records: " + customer.getBookingHistory().size());

        if (customer.getBookingHistory().isEmpty()) {
            System.out.println("  [!] You have no booking history yet.");
            System.out.println("==================================================================");
            return;
        }

        System.out.println("+------------+--------------------+----------------------+------------+------------------+------------+-------------+");
        System.out.println("| Booking ID | Movie              | Theatre              | Timing     | Seats            | Total (₹)  | Status      |");
        System.out.println("+------------+--------------------+----------------------+------------+------------------+------------+-------------+");
        for (Booking b : customer.getBookingHistory()) {
            System.out.printf("| %-10s | %-18s | %-20s | %-10s | %-16s | %-10.2f | %-11s |\n",
                    b.getBookingId(),
                    b.getShow().getMovie().getTitle(),
                    b.getShow().getTheatre().getName(),
                    b.getShow().getStartTime(),
                    String.join(", ", b.getBookedSeats()),
                    b.getTotalAmount(),
                    b.getStatus());
        }
        System.out.println("+------------+--------------------+----------------------+------------+------------------+------------+-------------+");
    }

    private static void handleSearchMovies() {
        System.out.println("\n==================== SEARCH MOVIES (DSA ALGORITHMS) ====================");
        System.out.println("  1. Search by Keyword / Name Substring (DSA: Linear Search - O(N))");
        System.out.println("  2. Search by Exact Movie Title (DSA: Binary Search - O(log N))");
        System.out.println("  3. Filter by Genre (Linear Search)");
        System.out.println("  4. Filter by Language (Linear Search)");
        System.out.println("  5. Return to Previous Menu");

        int opt = InputValidator.readInt(scanner, "  Choose search mode (1-5): ", 1, 5);
        switch (opt) {
            case 1: {
                String query = InputValidator.readNonEmptyString(scanner, "  Enter movie keyword: ");
                System.out.println("\n  🔍 [Linear Search O(N)] Scanning movie catalog...");
                List<Movie> results = movieManager.linearSearchByKeyword(query);
                MovieManager.printMovieTable(results);
                break;
            }
            case 2: {
                String title = InputValidator.readNonEmptyString(scanner, "  Enter exact movie title: ");
                System.out.println("\n  ⚡ [Binary Search O(log N)] Searching title on sorted list...");
                Movie found = movieManager.binarySearchByTitle(title);
                if (found != null) {
                    System.out.println("  ✅ Movie FOUND via Binary Search:");
                    MovieManager.printMovieTable(Collections.singletonList(found));
                } else {
                    System.out.println("  ❌ Movie with title '" + title + "' was not found.");
                }
                break;
            }
            case 3: {
                String genre = InputValidator.readNonEmptyString(scanner, "  Enter Genre (e.g. Sci-Fi, Action, Drama): ");
                List<Movie> results = movieManager.linearSearchByGenre(genre);
                MovieManager.printMovieTable(results);
                break;
            }
            case 4: {
                String lang = InputValidator.readNonEmptyString(scanner, "  Enter Language (e.g. English, Hindi, Japanese): ");
                List<Movie> results = movieManager.linearSearchByLanguage(lang);
                MovieManager.printMovieTable(results);
                break;
            }
            case 5: break;
        }
    }

    private static void handleSortMovies() {
        System.out.println("\n==================== SORT MOVIES (DSA ALGORITHMS) ====================");
        System.out.println("  1. Sort by Movie Title (A to Z) -> DSA: Merge Sort [O(N log N)]");
        System.out.println("  2. Sort by IMDB Rating (Highest First) -> DSA: Quick Sort [O(N log N)]");
        System.out.println("  3. Sort by Ticket Base Price (Cheapest First) -> DSA: Selection Sort [O(N^2)]");
        System.out.println("  4. Return to Previous Menu");

        int opt = InputValidator.readInt(scanner, "  Choose sorting algorithm (1-4): ", 1, 4);
        switch (opt) {
            case 1:
                System.out.println("\n  🔄 Executing Divide-and-Conquer Merge Sort [O(N log N)]...");
                ArrayList<Movie> titleSorted = movieManager.getMoviesSortedByTitle();
                MovieManager.printMovieTable(titleSorted);
                break;
            case 2:
                System.out.println("\n  ⚡ Executing Recursive Quick Sort with Pivot Partitioning [O(N log N)]...");
                ArrayList<Movie> ratingSorted = movieManager.getMoviesSortedByRating();
                MovieManager.printMovieTable(ratingSorted);
                break;
            case 3:
                System.out.println("\n  📊 Executing In-Place Selection Sort [O(N^2)]...");
                ArrayList<Movie> priceSorted = movieManager.getMoviesSortedByPrice();
                MovieManager.printMovieTable(priceSorted);
                break;
            case 4: break;
        }
    }

    private static void displayAllMoviesAndShows() {
        System.out.println("\n==================== CURRENTLY RUNNING MOVIES ====================");
        MovieManager.printMovieTable(movieManager.getMovieList());

        System.out.println("\n==================== UPCOMING SHOW TIMINGS ====================");
        displayAllShowsTable();
    }

    private static void displayAllShowsTable() {
        List<Show> shows = bookingManager.getShowList();
        if (shows.isEmpty()) {
            System.out.println("  [!] No shows currently scheduled.");
            return;
        }

        System.out.println("+---------+----------------------+------------------+------------+----------+-----------------------+");
        System.out.println("| Show ID | Movie Title          | Theatre          | Time       | Price    | Seating Status        |");
        System.out.println("+---------+----------------------+------------------+------------+----------+-----------------------+");
        for (Show s : shows) {
            s.displayShowDetails();
        }
        System.out.println("+---------+----------------------+------------------+------------+----------+-----------------------+");
    }
}

// ============================================================================
// 1. ABSTRACT BASE CLASS: USER (Abstraction & Encapsulation)
// ============================================================================
abstract class User {
    private String userId;
    private String name;
    private String email;
    private String password;

    public User(String userId, String name, String email, String password) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }

    public abstract String getRole();
    public abstract void displayDashboard();
}

// ============================================================================
// 2. CUSTOMER CLASS (Inheritance & LinkedList)
// ============================================================================
class Customer extends User {
    private String phone;
    private boolean isVip;
    private int loyaltyPoints;
    
    // DSA 4: LinkedList for booking history
    private LinkedList<Booking> bookingHistory;

    public Customer(String userId, String name, String email, String password, String phone, boolean isVip) {
        super(userId, name, email, password);
        this.phone = phone;
        this.isVip = isVip;
        this.loyaltyPoints = isVip ? 100 : 20;
        this.bookingHistory = new LinkedList<>();
    }

    public String getPhone() { return phone; }
    public boolean isVip() { return isVip; }
    public int getLoyaltyPoints() { return loyaltyPoints; }
    public void addLoyaltyPoints(int points) { this.loyaltyPoints += points; }
    public LinkedList<Booking> getBookingHistory() { return bookingHistory; }

    /**
     * DSA Operation: Add booking to head of LinkedList in O(1) time.
     */
    public void addBookingToHistory(Booking booking) {
        bookingHistory.addFirst(booking);
    }

    @Override
    public String getRole() { return "CUSTOMER"; }

    @Override
    public void displayDashboard() {
        System.out.println("==================================================");
        System.out.println("  WELCOME, " + getName().toUpperCase() + (isVip ? " [VIP MEMBER]" : " [STANDARD MEMBER]"));
        System.out.println("  Email: " + getEmail() + " | Phone: " + phone);
        System.out.println("  Loyalty Points: " + loyaltyPoints + " | Total Bookings: " + bookingHistory.size());
        System.out.println("==================================================");
    }
}

// ============================================================================
// 3. ADMIN CLASS (Inheritance & Access Control)
// ============================================================================
class Admin extends User {
    private String department;
    private int accessLevel;

    public Admin(String userId, String name, String email, String password, String department, int accessLevel) {
        super(userId, name, email, password);
        this.department = department;
        this.accessLevel = accessLevel;
    }

    @Override
    public String getRole() { return "ADMIN"; }

    @Override
    public void displayDashboard() {
        System.out.println("==================================================");
        System.out.println("  ADMIN CONTROL CENTER: " + getName().toUpperCase());
        System.out.println("  Department: " + department + " | Access Level: " + accessLevel);
        System.out.println("==================================================");
    }
}

// ============================================================================
// 4. MOVIE MODEL (Comparable Interface)
// ============================================================================
class Movie implements Comparable<Movie> {
    private String movieId;
    private String title;
    private String genre;
    private String language;
    private int durationMinutes;
    private double rating;
    private double basePrice;

    public Movie(String movieId, String title, String genre, String language, int durationMinutes, double rating, double basePrice) {
        this.movieId = movieId;
        this.title = title;
        this.genre = genre;
        this.language = language;
        this.durationMinutes = durationMinutes;
        this.rating = rating;
        this.basePrice = basePrice;
    }

    public String getMovieId() { return movieId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getLanguage() { return language; }
    public int getDurationMinutes() { return durationMinutes; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }

    @Override
    public int compareTo(Movie other) {
        return this.title.compareToIgnoreCase(other.title);
    }

    public void displayMovieDetails() {
        System.out.printf("| %-6s | %-24s | %-12s | %-10s | %3d mins | ⭐ %-4.1f | ₹%-7.2f |\n",
                movieId, title, genre, language, durationMinutes, rating, basePrice);
    }
}

// ============================================================================
// 5. THEATRE MODEL
// ============================================================================
class Theatre {
    private String theatreId;
    private String name;
    private String location;
    private int totalScreens;

    public Theatre(String theatreId, String name, String location, int totalScreens) {
        this.theatreId = theatreId;
        this.name = name;
        this.location = location;
        this.totalScreens = totalScreens;
    }

    public String getTheatreId() { return theatreId; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public int getTotalScreens() { return totalScreens; }

    @Override
    public String toString() {
        return String.format("%s (%s) [Screens: %d]", name, location, totalScreens);
    }
}

// ============================================================================
// 6. WAITLIST ENTRY (PriorityQueue Comparable Node)
// ============================================================================
class WaitlistEntry implements Comparable<WaitlistEntry> {
    private Customer customer;
    private int requestedSeats;
    private long requestTimestamp;
    private int priorityLevel; // 1 = Highest (VIP), 5 = Standard

    public WaitlistEntry(Customer customer, int requestedSeats) {
        this.customer = customer;
        this.requestedSeats = requestedSeats;
        this.requestTimestamp = System.currentTimeMillis();
        this.priorityLevel = customer.isVip() ? 1 : 5;
    }

    public Customer getCustomer() { return customer; }
    public int getRequestedSeats() { return requestedSeats; }

    @Override
    public int compareTo(WaitlistEntry other) {
        if (this.priorityLevel != other.priorityLevel) {
            return Integer.compare(this.priorityLevel, other.priorityLevel);
        }
        return Long.compare(this.requestTimestamp, other.requestTimestamp);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - Seats: %d (Level: %d)",
                customer.isVip() ? "VIP" : "REGULAR", customer.getName(), requestedSeats, priorityLevel);
    }
}

// ============================================================================
// 7. SEAT MANAGER (2D Array Cinema Hall Seating Matrix)
// ============================================================================
class SeatManager {
    private final int rows;
    private final int cols;
    private final char[][] seats; // DSA 1: 2D Array
    private int bookedSeatsCount;

    public static final char AVAILABLE = 'O';
    public static final char BOOKED = 'X';

    public SeatManager(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.seats = new char[rows][cols];
        this.bookedSeatsCount = 0;
        initializeSeats();
    }

    private void initializeSeats() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                seats[i][j] = AVAILABLE;
            }
        }
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getTotalSeats() { return rows * cols; }
    public int getAvailableSeatsCount() { return getTotalSeats() - bookedSeatsCount; }
    public boolean isHousefull() { return getAvailableSeatsCount() == 0; }

    public void displaySeatingChart() {
        System.out.println("\n  ==================== CINEMA SCREEN THIS WAY ====================");
        System.out.println("  ----------------------------------------------------------------");
        System.out.print("      ");
        for (int j = 1; j <= cols; j++) {
            System.out.printf(" %2d ", j);
            if (j == cols / 2) System.out.print("   ");
        }
        System.out.println();

        for (int i = 0; i < rows; i++) {
            char rowLabel = (char) ('A' + i);
            System.out.printf("  %c   ", rowLabel);
            for (int j = 0; j < cols; j++) {
                System.out.printf("[%c] ", seats[i][j]);
                if (j == cols / 2 - 1) System.out.print(" | ");
            }
            System.out.println();
        }
        System.out.println("  ----------------------------------------------------------------");
        System.out.println("  Legend: [O] Available    [X] Booked");
        System.out.printf("  Seats Summary: %d Available / %d Total\n\n", getAvailableSeatsCount(), getTotalSeats());
    }

    public int[] parseSeatCode(String seatCode) {
        if (seatCode == null) return null;
        seatCode = seatCode.trim().toUpperCase();
        if (seatCode.length() < 2) return null;

        char rowChar = seatCode.charAt(0);
        int rowIndex = rowChar - 'A';
        try {
            int colIndex = Integer.parseInt(seatCode.substring(1)) - 1;
            if (rowIndex >= 0 && rowIndex < rows && colIndex >= 0 && colIndex < cols) {
                return new int[]{rowIndex, colIndex};
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    public boolean isSeatAvailable(String seatCode) {
        int[] coords = parseSeatCode(seatCode);
        if (coords == null) return false;
        return seats[coords[0]][coords[1]] == AVAILABLE;
    }

    public String validateSeats(List<String> seatCodes) {
        if (seatCodes == null || seatCodes.isEmpty()) return "No seats were selected.";

        for (int i = 0; i < seatCodes.size(); i++) {
            for (int j = i + 1; j < seatCodes.size(); j++) {
                if (seatCodes.get(i).trim().equalsIgnoreCase(seatCodes.get(j).trim())) {
                    return "Duplicate seat '" + seatCodes.get(i) + "' found in your selection.";
                }
            }
        }

        for (String seatCode : seatCodes) {
            int[] coords = parseSeatCode(seatCode);
            if (coords == null) {
                return "Seat '" + seatCode + "' is invalid. Rows are A-" + (char) ('A' + rows - 1) + ", columns 1-" + cols + ".";
            }
            if (seats[coords[0]][coords[1]] == BOOKED) {
                return "Seat '" + seatCode.toUpperCase() + "' is already BOOKED!";
            }
        }
        return null;
    }

    public synchronized boolean bookSeats(List<String> seatCodes) {
        if (validateSeats(seatCodes) != null) return false;
        for (String seatCode : seatCodes) {
            int[] coords = parseSeatCode(seatCode);
            if (coords != null) {
                seats[coords[0]][coords[1]] = BOOKED;
                bookedSeatsCount++;
            }
        }
        return true;
    }

    public synchronized void cancelSeats(List<String> seatCodes) {
        if (seatCodes == null) return;
        for (String seatCode : seatCodes) {
            int[] coords = parseSeatCode(seatCode);
            if (coords != null && seats[coords[0]][coords[1]] == BOOKED) {
                seats[coords[0]][coords[1]] = AVAILABLE;
                bookedSeatsCount--;
            }
        }
    }

    public List<String> findAvailableSeats(int count) {
        List<String> allocated = new ArrayList<>();
        if (getAvailableSeatsCount() < count) return allocated;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (seats[i][j] == AVAILABLE) {
                    allocated.add("" + (char) ('A' + i) + (j + 1));
                    if (allocated.size() == count) return allocated;
                }
            }
        }
        return allocated;
    }
}

// ============================================================================
// 8. SHOW CLASS (Queue & PriorityQueue Waitlists)
// ============================================================================
class Show {
    private String showId;
    private Movie movie;
    private Theatre theatre;
    private String startTime;
    private double ticketPrice;
    private SeatManager seatManager;

    // DSA 6: Queue (FIFO) for standard customers
    private Queue<WaitlistEntry> standardWaitingQueue;

    // DSA 7: PriorityQueue (Min-Heap) for VIP customers
    private PriorityQueue<WaitlistEntry> vipPriorityWaitingQueue;

    public Show(String showId, Movie movie, Theatre theatre, String startTime, double ticketPrice, int rows, int cols) {
        this.showId = showId;
        this.movie = movie;
        this.theatre = theatre;
        this.startTime = startTime;
        this.ticketPrice = ticketPrice;
        this.seatManager = new SeatManager(rows, cols);
        this.standardWaitingQueue = new LinkedList<>();
        this.vipPriorityWaitingQueue = new PriorityQueue<>();
    }

    public String getShowId() { return showId; }
    public Movie getMovie() { return movie; }
    public Theatre getTheatre() { return theatre; }
    public String getStartTime() { return startTime; }
    public double getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(double ticketPrice) { this.ticketPrice = ticketPrice; }
    public SeatManager getSeatManager() { return seatManager; }
    public Queue<WaitlistEntry> getStandardWaitingQueue() { return standardWaitingQueue; }
    public PriorityQueue<WaitlistEntry> getVipPriorityWaitingQueue() { return vipPriorityWaitingQueue; }

    public void enqueueWaitlist(Customer customer, int requestedSeats) {
        WaitlistEntry entry = new WaitlistEntry(customer, requestedSeats);
        if (customer.isVip()) {
            vipPriorityWaitingQueue.offer(entry); // O(log N) PriorityQueue
            System.out.println("  ⭐ [VIP PriorityQueue] " + customer.getName() + " added to VIP Waitlist at top priority!");
        } else {
            standardWaitingQueue.offer(entry);    // O(1) FIFO Queue
            System.out.println("  📋 [FIFO Queue] " + customer.getName() + " added to Standard Waiting List at position #"
                    + standardWaitingQueue.size() + ".");
        }
    }

    public boolean hasWaitingCustomers() {
        return !vipPriorityWaitingQueue.isEmpty() || !standardWaitingQueue.isEmpty();
    }

    public WaitlistEntry dequeueNextWaitingCustomer() {
        if (!vipPriorityWaitingQueue.isEmpty()) {
            return vipPriorityWaitingQueue.poll();
        }
        if (!standardWaitingQueue.isEmpty()) {
            return standardWaitingQueue.poll();
        }
        return null;
    }

    public void displayShowDetails() {
        System.out.printf("| %-7s | %-20s | %-16s | %-10s | ₹%-7.2f | %2d/%2d Available |\n",
                showId, movie.getTitle(), theatre.getName(), startTime, ticketPrice,
                seatManager.getAvailableSeatsCount(), seatManager.getTotalSeats());
    }
}

// ============================================================================
// 9. BOOKING MODEL & TICKET PRINTER
// ============================================================================
class Booking {
    private String bookingId;
    private Customer customer;
    private Show show;
    private List<String> bookedSeats;
    private double totalAmount;
    private String bookingTimestamp;
    private String status;
    private String paymentMethod;

    public Booking(String bookingId, Customer customer, Show show, List<String> bookedSeats, double totalAmount, String paymentMethod) {
        this.bookingId = bookingId;
        this.customer = customer;
        this.show = show;
        this.bookedSeats = bookedSeats;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.status = "CONFIRMED";
        this.bookingTimestamp = new SimpleDateFormat("dd-MMM-yyyy hh:mm a").format(new Date());
    }

    public String getBookingId() { return bookingId; }
    public Customer getCustomer() { return customer; }
    public Show getShow() { return show; }
    public List<String> getBookedSeats() { return bookedSeats; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPaymentMethod() { return paymentMethod; }

    public void printTicket() {
        System.out.println("  +-------------------------------------------------------------+");
        System.out.println("  |                  MOVIE TICKET CONFIRMATION                  |");
        System.out.println("  +-------------------------------------------------------------+");
        System.out.printf("  |  Booking ID   : %-43s |\n", bookingId);
        System.out.printf("  |  Customer     : %-43s |\n", customer.getName() + (customer.isVip() ? " [VIP]" : ""));
        System.out.printf("  |  Movie Title  : %-43s |\n", show.getMovie().getTitle());
        System.out.printf("  |  Language/Genre: %-42s |\n", show.getMovie().getLanguage() + " / " + show.getMovie().getGenre());
        System.out.printf("  |  Theatre      : %-43s |\n", show.getTheatre().getName());
        System.out.printf("  |  Show Timing  : %-43s |\n", show.getStartTime());
        System.out.printf("  |  Seats Booked : %-43s |\n", String.join(", ", bookedSeats));
        System.out.printf("  |  Total Seats  : %-43d |\n", bookedSeats.size());
        System.out.printf("  |  Total Paid   : ₹%-42.2f |\n", totalAmount);
        System.out.printf("  |  Payment Mode : %-43s |\n", paymentMethod);
        System.out.printf("  |  Booked On    : %-43s |\n", bookingTimestamp);
        System.out.printf("  |  Status       : %-43s |\n", status);
        System.out.println("  +-------------------------------------------------------------+");
        System.out.println("  |       ||| ||||| |||| |||||| ||||| ||| ||||||| |||| ||       |");
        System.out.println("  |                   PLEASE PRESENT AT ENTRANCE                |");
        System.out.println("  +-------------------------------------------------------------+\n");
    }
}

// ============================================================================
// 10. MOVIE MANAGER (MergeSort, QuickSort, SelectionSort, Binary/Linear Search)
// ============================================================================
class MovieManager {
    // DSA 2: ArrayList
    private ArrayList<Movie> movieList;

    public MovieManager() {
        this.movieList = new ArrayList<>();
        seedInitialMovies();
    }

    private void seedInitialMovies() {
        movieList.add(new Movie("M101", "Inception", "Sci-Fi", "English", 148, 8.8, 280.0));
        movieList.add(new Movie("M102", "Interstellar", "Sci-Fi", "English", 169, 8.7, 300.0));
        movieList.add(new Movie("M103", "The Dark Knight", "Action", "English", 152, 9.0, 250.0));
        movieList.add(new Movie("M104", "Dangal", "Drama", "Hindi", 161, 8.4, 220.0));
        movieList.add(new Movie("M105", "Spirited Away", "Animation", "Japanese", 125, 8.6, 200.0));
        movieList.add(new Movie("M106", "Avengers: Endgame", "Action", "English", 181, 8.4, 320.0));
        movieList.add(new Movie("M107", "Parasite", "Thriller", "Korean", 132, 8.5, 240.0));
        movieList.add(new Movie("M108", "KGF Chapter 2", "Action", "Hindi", 168, 8.3, 260.0));
    }

    public ArrayList<Movie> getMovieList() { return movieList; }
    public void addMovie(Movie movie) { movieList.add(movie); }
    public boolean removeMovie(String movieId) { return movieList.removeIf(m -> m.getMovieId().equalsIgnoreCase(movieId)); }

    public Movie findMovieById(String movieId) {
        for (Movie m : movieList) {
            if (m.getMovieId().equalsIgnoreCase(movieId)) return m;
        }
        return null;
    }

    // DSA 12: Linear Search O(N)
    public List<Movie> linearSearchByKeyword(String keyword) {
        List<Movie> results = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) return results;
        String query = keyword.toLowerCase().trim();
        for (Movie m : movieList) {
            if (m.getTitle().toLowerCase().contains(query) || m.getGenre().toLowerCase().contains(query) || m.getLanguage().toLowerCase().contains(query)) {
                results.add(m);
            }
        }
        return results;
    }

    public List<Movie> linearSearchByGenre(String genre) {
        List<Movie> results = new ArrayList<>();
        for (Movie m : movieList) {
            if (m.getGenre().equalsIgnoreCase(genre.trim())) results.add(m);
        }
        return results;
    }

    public List<Movie> linearSearchByLanguage(String language) {
        List<Movie> results = new ArrayList<>();
        for (Movie m : movieList) {
            if (m.getLanguage().equalsIgnoreCase(language.trim())) results.add(m);
        }
        return results;
    }

    // DSA 11: Binary Search O(log N)
    public Movie binarySearchByTitle(String targetTitle) {
        if (targetTitle == null || movieList.isEmpty()) return null;
        ArrayList<Movie> sortedCopy = new ArrayList<>(movieList);
        mergeSortByTitle(sortedCopy, 0, sortedCopy.size() - 1);

        int low = 0;
        int high = sortedCopy.size() - 1;
        while (low <= high) {
            int mid = low + (high - low) / 2;
            int comparison = sortedCopy.get(mid).getTitle().compareToIgnoreCase(targetTitle.trim());
            if (comparison == 0) return sortedCopy.get(mid);
            else if (comparison < 0) low = mid + 1;
            else high = mid - 1;
        }
        return null;
    }

    // DSA 8: Merge Sort O(N log N) Stable Sort (Title A-Z)
    public ArrayList<Movie> getMoviesSortedByTitle() {
        ArrayList<Movie> list = new ArrayList<>(movieList);
        mergeSortByTitle(list, 0, list.size() - 1);
        return list;
    }

    private void mergeSortByTitle(ArrayList<Movie> list, int left, int right) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSortByTitle(list, left, mid);
            mergeSortByTitle(list, mid + 1, right);
            mergeTitles(list, left, mid, right);
        }
    }

    private void mergeTitles(ArrayList<Movie> list, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;
        ArrayList<Movie> leftArr = new ArrayList<>(n1);
        ArrayList<Movie> rightArr = new ArrayList<>(n2);

        for (int i = 0; i < n1; i++) leftArr.add(list.get(left + i));
        for (int j = 0; j < n2; j++) rightArr.add(list.get(mid + 1 + j));

        int i = 0, j = 0, k = left;
        while (i < n1 && j < n2) {
            if (leftArr.get(i).getTitle().compareToIgnoreCase(rightArr.get(j).getTitle()) <= 0) {
                list.set(k++, leftArr.get(i++));
            } else {
                list.set(k++, rightArr.get(j++));
            }
        }
        while (i < n1) list.set(k++, leftArr.get(i++));
        while (j < n2) list.set(k++, rightArr.get(j++));
    }

    // DSA 9: Quick Sort O(N log N) (Rating Descending)
    public ArrayList<Movie> getMoviesSortedByRating() {
        ArrayList<Movie> list = new ArrayList<>(movieList);
        quickSortByRating(list, 0, list.size() - 1);
        return list;
    }

    private void quickSortByRating(ArrayList<Movie> list, int low, int high) {
        if (low < high) {
            int pi = partitionByRating(list, low, high);
            quickSortByRating(list, low, pi - 1);
            quickSortByRating(list, pi + 1, high);
        }
    }

    private int partitionByRating(ArrayList<Movie> list, int low, int high) {
        double pivot = list.get(high).getRating();
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (list.get(j).getRating() >= pivot) {
                i++;
                Movie temp = list.get(i);
                list.set(i, list.get(j));
                list.set(j, temp);
            }
        }
        Movie temp = list.get(i + 1);
        list.set(i + 1, list.get(high));
        list.set(high, temp);
        return i + 1;
    }

    // DSA 10: Selection Sort O(N^2) (Price Ascending)
    public ArrayList<Movie> getMoviesSortedByPrice() {
        ArrayList<Movie> list = new ArrayList<>(movieList);
        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (list.get(j).getBasePrice() < list.get(minIdx).getBasePrice()) minIdx = j;
            }
            if (minIdx != i) {
                Movie temp = list.get(i);
                list.set(i, list.get(minIdx));
                list.set(minIdx, temp);
            }
        }
        return list;
    }

    public static void printMovieTable(List<Movie> list) {
        if (list == null || list.isEmpty()) {
            System.out.println("  [!] No movies found matching the criteria.");
            return;
        }
        System.out.println("+--------+--------------------------+--------------+------------+----------+--------+----------+");
        System.out.println("| ID     | Title                    | Genre        | Language   | Duration | Rating | Price    |");
        System.out.println("+--------+--------------------------+--------------+------------+----------+--------+----------+");
        for (Movie m : list) m.displayMovieDetails();
        System.out.println("+--------+--------------------------+--------------+------------+----------+--------+----------+");
        System.out.println("  Total Movies: " + list.size());
    }
}

// ============================================================================
// 11. BOOKING MANAGER (HashMap, Stack LIFO Undo & Waitlist Promotion)
// ============================================================================
class BookingManager {
    // DSA 3: HashMap for O(1) Booking lookups
    private Map<String, Booking> bookingMap;

    // DSA 5: Stack for LIFO Undo Cancellation
    private Stack<Booking> cancellationStack;

    private List<Show> showList;
    private int bookingCounter = 1001;

    public BookingManager() {
        this.bookingMap = new HashMap<>();
        this.cancellationStack = new Stack<>();
        this.showList = new ArrayList<>();
    }

    public List<Show> getShowList() { return showList; }
    public void addShow(Show show) { showList.add(show); }

    public Show findShowById(String showId) {
        for (Show s : showList) {
            if (s.getShowId().equalsIgnoreCase(showId)) return s;
        }
        return null;
    }

    public Map<String, Booking> getBookingMap() { return bookingMap; }
    public Stack<Booking> getCancellationStack() { return cancellationStack; }

    public double calculateTotalPrice(Show show, int numberOfSeats, boolean isVip) {
        double subtotal = show.getTicketPrice() * numberOfSeats;
        double tax = subtotal * 0.18;
        double discount = isVip ? (subtotal * 0.10) : 0.0;
        return subtotal + tax - discount;
    }

    public Booking createBooking(Customer customer, Show show, List<String> seatCodes, String paymentMethod) {
        if (show.getSeatManager().validateSeats(seatCodes) != null) return null;
        if (!show.getSeatManager().bookSeats(seatCodes)) return null;

        double totalAmount = calculateTotalPrice(show, seatCodes.size(), customer.isVip());
        String bookingId = "BK-" + (bookingCounter++);

        Booking booking = new Booking(bookingId, customer, show, seatCodes, totalAmount, paymentMethod);

        // O(1) HashMap Put
        bookingMap.put(bookingId, booking);

        // O(1) LinkedList Head Insert
        customer.addBookingToHistory(booking);
        customer.addLoyaltyPoints(seatCodes.size() * 10);

        return booking;
    }

    public boolean cancelBooking(String bookingId) {
        Booking booking = bookingMap.get(bookingId); // O(1) HashMap Get
        if (booking == null || "CANCELLED".equalsIgnoreCase(booking.getStatus())) {
            System.out.println("  ❌ Booking not found or already cancelled.");
            return false;
        }

        Show show = booking.getShow();
        show.getSeatManager().cancelSeats(booking.getBookedSeats());
        booking.setStatus("CANCELLED");

        // O(1) Stack Push
        cancellationStack.push(booking);

        System.out.println("\n  ✅ Booking " + bookingId + " has been successfully CANCELLED!");
        System.out.printf("  💵 Refund of ₹%.2f initiated to customer's %s.\n", booking.getTotalAmount(), booking.getPaymentMethod());
        System.out.println("  💺 Released Seats: " + String.join(", ", booking.getBookedSeats()));

        processWaitlistAfterCancellation(show, booking.getBookedSeats().size());
        return true;
    }

    public boolean undoLastCancellation() {
        if (cancellationStack.isEmpty()) {
            System.out.println("  [!] Cancellation Stack is empty. No cancellations to undo.");
            return false;
        }

        Booking lastCancelled = cancellationStack.pop(); // O(1) Stack Pop
        Show show = lastCancelled.getShow();
        List<String> seats = lastCancelled.getBookedSeats();

        if (show.getSeatManager().validateSeats(seats) != null) {
            System.out.println("  ❌ Cannot undo cancellation: Seats were already re-allocated!");
            cancellationStack.push(lastCancelled);
            return false;
        }

        show.getSeatManager().bookSeats(seats);
        lastCancelled.setStatus("CONFIRMED (RESTORED)");

        System.out.println("\n  ↺ [UNDO SUCCESS] Restored Booking: " + lastCancelled.getBookingId());
        System.out.println("  Movie: " + show.getMovie().getTitle() + " | Seats: " + String.join(", ", seats));
        System.out.println("  Customer: " + lastCancelled.getCustomer().getName());
        return true;
    }

    private void processWaitlistAfterCancellation(Show show, int freedSeatsCount) {
        if (!show.hasWaitingCustomers()) return;

        System.out.println("\n  🔔 [WAITLIST NOTIFICATION] Checking waiting queue for Show " + show.getShowId() + "...");
        while (show.hasWaitingCustomers() && show.getSeatManager().getAvailableSeatsCount() > 0) {
            WaitlistEntry nextInLine = show.dequeueNextWaitingCustomer();
            if (nextInLine == null) break;

            int needed = nextInLine.getRequestedSeats();
            if (show.getSeatManager().getAvailableSeatsCount() >= needed) {
                List<String> autoAllocatedSeats = show.getSeatManager().findAvailableSeats(needed);
                if (autoAllocatedSeats.size() == needed) {
                    Booking autoBooking = createBooking(
                            nextInLine.getCustomer(), show, autoAllocatedSeats, "Auto-Promoted from Waitlist"
                    );
                    System.out.println("  🎉 SUCCESS: Customer " + nextInLine.getCustomer().getName()
                            + (nextInLine.getCustomer().isVip() ? " [VIP]" : "") + " promoted from waitlist!");
                    System.out.println("     Assigned Booking ID: " + autoBooking.getBookingId()
                            + " | Seats: " + String.join(", ", autoAllocatedSeats));
                }
            }
        }
    }

    public void displayRecentCancellations() {
        if (cancellationStack.isEmpty()) {
            System.out.println("  [!] No recent cancellations recorded in the Stack.");
            return;
        }
        System.out.println("\n  ====== RECENT CANCELLATIONS STACK (Top = Most Recent) ======");
        for (int i = cancellationStack.size() - 1; i >= 0; i--) {
            Booking b = cancellationStack.get(i);
            System.out.printf("  [%d] Booking: %-8s | Movie: %-18s | Customer: %-12s | Seats: %s | Refund: ₹%.2f\n",
                    (cancellationStack.size() - i), b.getBookingId(), b.getShow().getMovie().getTitle(),
                    b.getCustomer().getName(), String.join(", ", b.getBookedSeats()), b.getTotalAmount());
        }
        System.out.println("  =============================================================");
    }

    public void displayAllBookings() {
        if (bookingMap.isEmpty()) {
            System.out.println("  [!] No bookings recorded yet.");
            return;
        }
        System.out.println("\n+------------+--------------------+----------------------+------------+------------------+------------+-------------+");
        System.out.println("| Booking ID | Customer           | Movie                | Timing     | Seats            | Total (₹)  | Status      |");
        System.out.println("+------------+--------------------+----------------------+------------+------------------+------------+-------------+");
        for (Booking b : bookingMap.values()) {
            System.out.printf("| %-10s | %-18s | %-20s | %-10s | %-16s | %-10.2f | %-11s |\n",
                    b.getBookingId(), b.getCustomer().getName(), b.getShow().getMovie().getTitle(),
                    b.getShow().getStartTime(), String.join(", ", b.getBookedSeats()), b.getTotalAmount(), b.getStatus());
        }
        System.out.println("+------------+--------------------+----------------------+------------+------------------+------------+-------------+");
    }
}

// ============================================================================
// 12. PAYMENT MODULE (Abstraction & Polymorphism Strategy Pattern)
// ============================================================================
class Payment {

    public interface PaymentMethod {
        boolean pay(double amount);
        String getMethodName();
    }

    public static class CardPayment implements PaymentMethod {
        private String cardNumber;
        private String cvv;

        public CardPayment(String cardNumber, String cvv) {
            this.cardNumber = cardNumber;
            this.cvv = cvv;
        }

        @Override
        public boolean pay(double amount) {
            System.out.println("\n  [💳 Card Gateway] Contacting Bank Network...");
            if (cardNumber.replaceAll("\\s+", "").length() < 12 || cvv.length() < 3) {
                System.out.println("  ❌ Payment Failed: Invalid Card Number or CVV!");
                return false;
            }
            System.out.println("  [💳 Card Gateway] Processing ₹" + String.format("%.2f", amount) + " from card ending in "
                    + cardNumber.substring(cardNumber.length() - 4));
            System.out.println("  ✅ Card Payment Authorized Successfully!");
            return true;
        }

        @Override
        public String getMethodName() {
            return "Credit/Debit Card (**** " + cardNumber.substring(cardNumber.length() - 4) + ")";
        }
    }

    public static class UPIPayment implements PaymentMethod {
        private String upiId;

        public UPIPayment(String upiId) { this.upiId = upiId; }

        @Override
        public boolean pay(double amount) {
            System.out.println("\n  [📱 UPI Portal] Request sent to VPA: " + upiId);
            if (!upiId.contains("@")) {
                System.out.println("  ❌ Payment Failed: Invalid UPI ID format!");
                return false;
            }
            System.out.println("  ✅ UPI Payment Received Successfully via NPCI!");
            return true;
        }

        @Override
        public String getMethodName() { return "UPI (" + upiId + ")"; }
    }

    public static class CashPayment implements PaymentMethod {
        @Override
        public boolean pay(double amount) {
            System.out.println("\n  [💵 Cash Counter] Booking held. Please pay ₹" + String.format("%.2f", amount) + " at Box Office.");
            System.out.println("  ✅ Cash on Arrival option confirmed!");
            return true;
        }

        @Override
        public String getMethodName() { return "Cash at Counter"; }
    }

    public static PaymentMethod handlePayment(Scanner scanner, double amount) {
        System.out.println("\n==================== SELECT PAYMENT METHOD ====================");
        System.out.println("  Total Payable Amount: ₹" + String.format("%.2f", amount));
        System.out.println("  1. Credit / Debit Card");
        System.out.println("  2. UPI (GooglePay / PhonePe / Paytm)");
        System.out.println("  3. Cash at Counter (Box Office)");
        System.out.println("  4. Cancel Payment");
        System.out.print("  Choose an option (1-4): ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                System.out.print("  Enter 16-digit Card Number: ");
                String cardNum = scanner.nextLine().trim();
                System.out.print("  Enter 3-digit CVV: ");
                String cvv = scanner.nextLine().trim();
                if (cardNum.length() < 4) cardNum = "1111222233334444";
                CardPayment card = new CardPayment(cardNum, cvv);
                return card.pay(amount) ? card : null;
            case "2":
                System.out.print("  Enter UPI ID (e.g. user@okaxis): ");
                String upiId = scanner.nextLine().trim();
                UPIPayment upi = new UPIPayment(upiId);
                return upi.pay(amount) ? upi : null;
            case "3":
                CashPayment cash = new CashPayment();
                return cash.pay(amount) ? cash : null;
            default:
                System.out.println("  ❌ Payment Cancelled.");
                return null;
        }
    }
}

// ============================================================================
// 13. INPUT VALIDATOR UTILITY
// ============================================================================
class InputValidator {
    public static int readInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) return value;
                System.out.println("  ❌ Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  ❌ Invalid input. Please enter a whole number.");
            }
        }
    }

    public static double readDouble(Scanner scanner, String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(input);
                if (value >= min && value <= max) return value;
                System.out.println("  ❌ Please enter a value between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  ❌ Invalid input. Please enter a valid decimal number.");
            }
        }
    }

    public static String readNonEmptyString(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("  ❌ Input cannot be empty. Please try again.");
        }
    }

    public static boolean readYesNo(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("y") || input.equals("yes")) return true;
            if (input.equals("n") || input.equals("no")) return false;
            System.out.println("  ❌ Invalid choice. Please enter 'y' or 'n'.");
        }
    }
}
