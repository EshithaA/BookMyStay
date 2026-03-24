import java.util.*;

/**
 * Abstract Room class (Domain Model)
 */
abstract class Room {
    private String type;
    private int beds;
    private double price;

    public Room(String type, int beds, double price) {
        this.type = type;
        this.beds = beds;
        this.price = price;
    }

    public String getType() { return type; }
    public double getPrice() { return price; }

    public abstract void displayDetails();
}

class SingleRoom extends Room {
    public SingleRoom() { super("Single Room", 1, 2000.0); }
    public void displayDetails() { System.out.println(getType() + " | ₹" + getPrice()); }
}

class DoubleRoom extends Room {
    public DoubleRoom() { super("Double Room", 2, 3500.0); }
    public void displayDetails() { System.out.println(getType() + " | ₹" + getPrice()); }
}

class SuiteRoom extends Room {
    public SuiteRoom() { super("Suite Room", 3, 6000.0); }
    public void displayDetails() { System.out.println(getType() + " | ₹" + getPrice()); }
}

/**
 * Reservation with reservationId
 */
class Reservation {
    private String reservationId;
    private String guestName;
    private String roomType;

    public Reservation(String reservationId, String guestName, String roomType) {
        this.reservationId = reservationId;
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getReservationId() { return reservationId; }
    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }

    public void display() {
        System.out.println(reservationId + " | " + guestName + " | " + roomType);
    }
}

/**
 * Booking Request Queue (FIFO)
 */
class BookingRequestQueue {
    private Queue<Reservation> queue = new LinkedList<>();

    public void addRequest(Reservation r) { queue.offer(r); }

    public Reservation getNextRequest() { return queue.poll(); }

    public boolean isEmpty() { return queue.isEmpty(); }
}

/**
 * Inventory Service
 */
class RoomInventory {
    private Map<String, Integer> availability = new HashMap<>();

    public RoomInventory() {
        availability.put("Single Room", 2);
        availability.put("Double Room", 1);
        availability.put("Suite Room", 1);
    }

    public int getAvailability(String type) { return availability.getOrDefault(type, 0); }

    public void reduceAvailability(String type) {
        availability.put(type, getAvailability(type) - 1);
    }
}

/**
 * Booking Service (Safe Allocation)
 */
class BookingService {

    private RoomInventory inventory;
    private Set<String> allocatedRoomIds = new HashSet<>();
    private Map<String, Set<String>> allocations = new HashMap<>();
    private int roomCounter = 1;

    // Booking history
    private List<Reservation> bookingHistory = new ArrayList<>();

    public BookingService(RoomInventory inventory) { this.inventory = inventory; }

    public void processBookings(BookingRequestQueue queue) {
        while (!queue.isEmpty()) {
            Reservation r = queue.getNextRequest();
            String type = r.getRoomType();

            if (inventory.getAvailability(type) <= 0) {
                System.out.println("❌ No availability for " + r.getGuestName());
                continue;
            }

            String roomId = generateRoomId(type);
            if (allocatedRoomIds.contains(roomId)) continue;

            // Allocate
            allocatedRoomIds.add(roomId);
            allocations.computeIfAbsent(type, k -> new HashSet<>()).add(roomId);
            inventory.reduceAvailability(type);

            // Record in history
            bookingHistory.add(r);

            System.out.println("✅ Booking Confirmed | ResID: " + r.getReservationId()
                    + " | RoomID: " + roomId);
        }
    }

    private String generateRoomId(String type) {
        return type.substring(0, 3).toUpperCase() + "-" + (roomCounter++);
    }

    // Access booking history (read-only)
    public List<Reservation> getBookingHistory() {
        return Collections.unmodifiableList(bookingHistory);
    }
}

/**
 * Booking Report Service (Read-only reporting)
 */
class BookingReportService {

    private List<Reservation> bookingHistory;

    public BookingReportService(List<Reservation> bookingHistory) {
        this.bookingHistory = bookingHistory;
    }

    // Display all reservations in chronological order
    public void displayAllBookings() {
        System.out.println("\n=== Booking History ===");
        if (bookingHistory.isEmpty()) {
            System.out.println("No bookings yet.");
            return;
        }
        for (Reservation r : bookingHistory) {
            r.display();
        }
    }

    // Generate a summary: number of bookings per room type
    public void displaySummaryReport() {
        System.out.println("\n=== Booking Summary ===");
        Map<String, Integer> summary = new HashMap<>();
        for (Reservation r : bookingHistory) {
            summary.put(r.getRoomType(), summary.getOrDefault(r.getRoomType(), 0) + 1);
        }
        for (var entry : summary.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue() + " bookings");
        }
    }
}

/**
 * Main Application
 */
public class BookMyStay {

    public static void main(String[] args) {

        // Setup
        RoomInventory inventory = new RoomInventory();
        BookingRequestQueue queue = new BookingRequestQueue();
        BookingService bookingService = new BookingService(inventory);

        // Sample reservations
        Reservation r1 = new Reservation("RES-101", "Alice", "Single Room");
        Reservation r2 = new Reservation("RES-102", "Bob", "Double Room");
        Reservation r3 = new Reservation("RES-103", "Charlie", "Single Room"); // exceeds availability

        // Add to queue
        queue.addRequest(r1);
        queue.addRequest(r2);
        queue.addRequest(r3);

        // Process bookings
        bookingService.processBookings(queue);

        // Access booking history
        BookingReportService reportService = new BookingReportService(bookingService.getBookingHistory());
        reportService.displayAllBookings();
        reportService.displaySummaryReport();

        System.out.println("\n✅ Historical tracking complete. Inventory state preserved.");
    }
}