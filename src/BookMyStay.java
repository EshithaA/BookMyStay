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
    public int getBeds() { return beds; }
    public double getPrice() { return price; }

    public abstract void displayDetails();
}

/**
 * Concrete Room Types
 */
class SingleRoom extends Room {
    public SingleRoom() { super("Single Room", 1, 2000.0); }
    public void displayDetails() {
        System.out.println(getType() + " | Beds: " + getBeds() + " | ₹" + getPrice());
    }
}

class DoubleRoom extends Room {
    public DoubleRoom() { super("Double Room", 2, 3500.0); }
    public void displayDetails() {
        System.out.println(getType() + " | Beds: " + getBeds() + " | ₹" + getPrice());
    }
}

class SuiteRoom extends Room {
    public SuiteRoom() { super("Suite Room", 3, 6000.0); }
    public void displayDetails() {
        System.out.println(getType() + " | Beds: " + getBeds() + " | ₹" + getPrice());
    }
}

/**
 * Reservation Request
 */
class Reservation {
    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }
}

/**
 * Booking Request Queue (FIFO)
 */
class BookingRequestQueue {
    private Queue<Reservation> queue = new LinkedList<>();

    public void addRequest(Reservation r) {
        queue.offer(r);
        System.out.println("Request added: " + r.getGuestName());
    }

    public Reservation getNextRequest() {
        return queue.poll(); // FIFO removal
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}

/**
 * Inventory Service (State Holder)
 */
class RoomInventory {
    private Map<String, Integer> availability = new HashMap<>();

    public RoomInventory() {
        availability.put("Single Room", 2);
        availability.put("Double Room", 1);
        availability.put("Suite Room", 1);
    }

    public int getAvailability(String type) {
        return availability.getOrDefault(type, 0);
    }

    public void reduceAvailability(String type) {
        availability.put(type, getAvailability(type) - 1);
    }

    public void displayInventory() {
        System.out.println("\n=== Inventory ===");
        for (var e : availability.entrySet()) {
            System.out.println(e.getKey() + " -> " + e.getValue());
        }
    }
}

/**
 * Booking Service (Core Allocation Logic)
 */
class BookingService {

    private RoomInventory inventory;

    // Track ALL allocated room IDs (global uniqueness)
    private Set<String> allocatedRoomIds = new HashSet<>();

    // Track roomType -> assigned room IDs
    private Map<String, Set<String>> roomAllocations = new HashMap<>();

    private int idCounter = 1;

    public BookingService(RoomInventory inventory) {
        this.inventory = inventory;
    }

    /**
     * Process booking queue safely
     */
    public void processBookings(BookingRequestQueue queue) {

        System.out.println("\n=== Processing Bookings ===");

        while (!queue.isEmpty()) {

            Reservation request = queue.getNextRequest();
            String type = request.getRoomType();

            System.out.println("\nProcessing: " + request.getGuestName());

            // Check availability
            if (inventory.getAvailability(type) <= 0) {
                System.out.println("❌ No rooms available for " + type);
                continue;
            }

            // Generate unique room ID
            String roomId = generateRoomId(type);

            // Ensure uniqueness (defensive check)
            if (allocatedRoomIds.contains(roomId)) {
                System.out.println("❌ Duplicate Room ID detected!");
                continue;
            }

            // Atomic allocation (logical unit)
            allocatedRoomIds.add(roomId);

            roomAllocations
                    .computeIfAbsent(type, k -> new HashSet<>())
                    .add(roomId);

            // Update inventory immediately
            inventory.reduceAvailability(type);

            // Confirm booking
            System.out.println("✅ Booking Confirmed!");
            System.out.println("Guest: " + request.getGuestName());
            System.out.println("Room Type: " + type);
            System.out.println("Assigned Room ID: " + roomId);
        }
    }

    /**
     * Generate unique Room ID
     */
    private String generateRoomId(String type) {
        return type.replace(" ", "").substring(0, 3).toUpperCase() + "-" + (idCounter++);
    }

    /**
     * Display allocations
     */
    public void displayAllocations() {
        System.out.println("\n=== Room Allocations ===");
        for (var entry : roomAllocations.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
    }
}

/**
 * Main Application
 */
public class BookMyStay {

    public static void main(String[] args) {

        // Initialize components
        RoomInventory inventory = new RoomInventory();
        BookingRequestQueue queue = new BookingRequestQueue();
        BookingService bookingService = new BookingService(inventory);

        // Add booking requests (FIFO order)
        queue.addRequest(new Reservation("Alice", "Single Room"));
        queue.addRequest(new Reservation("Bob", "Single Room"));
        queue.addRequest(new Reservation("Charlie", "Single Room")); // exceeds availability
        queue.addRequest(new Reservation("Diana", "Suite Room"));

        // Process bookings
        bookingService.processBookings(queue);

        // Show final state
        bookingService.displayAllocations();
        inventory.displayInventory();
    }
}