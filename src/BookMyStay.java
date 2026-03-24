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

class SingleRoom extends Room {
    public SingleRoom() { super("Single Room", 1, 2000.0); }
    public void displayDetails() {
        System.out.println(getType() + " | ₹" + getPrice());
    }
}

class DoubleRoom extends Room {
    public DoubleRoom() { super("Double Room", 2, 3500.0); }
    public void displayDetails() {
        System.out.println(getType() + " | ₹" + getPrice());
    }
}

class SuiteRoom extends Room {
    public SuiteRoom() { super("Suite Room", 3, 6000.0); }
    public void displayDetails() {
        System.out.println(getType() + " | ₹" + getPrice());
    }
}

/**
 * Reservation (Now includes reservationId)
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
}

/**
 * Booking Queue (FIFO)
 */
class BookingRequestQueue {
    private Queue<Reservation> queue = new LinkedList<>();

    public void addRequest(Reservation r) {
        queue.offer(r);
    }

    public Reservation getNextRequest() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}

/**
 * Inventory (State)
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
}

/**
 * Booking Service (Core Allocation)
 */
class BookingService {

    private RoomInventory inventory;
    private Set<String> allocatedRoomIds = new HashSet<>();
    private Map<String, Set<String>> allocations = new HashMap<>();
    private int roomCounter = 1;

    public BookingService(RoomInventory inventory) {
        this.inventory = inventory;
    }

    public void processBookings(BookingRequestQueue queue) {

        while (!queue.isEmpty()) {

            Reservation r = queue.getNextRequest();
            String type = r.getRoomType();

            if (inventory.getAvailability(type) <= 0) {
                System.out.println("❌ No availability for " + r.getGuestName());
                continue;
            }

            String roomId = generateRoomId(type);

            if (allocatedRoomIds.contains(roomId)) {
                continue;
            }

            // Atomic allocation
            allocatedRoomIds.add(roomId);
            allocations.computeIfAbsent(type, k -> new HashSet<>()).add(roomId);
            inventory.reduceAvailability(type);

            System.out.println("✅ Booking Confirmed | ResID: " + r.getReservationId()
                    + " | RoomID: " + roomId);
        }
    }

    private String generateRoomId(String type) {
        return type.substring(0, 3).toUpperCase() + "-" + (roomCounter++);
    }
}

/**
 * Add-On Service (Composition Model)
 */
class AddOnService {
    private String name;
    private double price;

    public AddOnService(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
}

/**
 * Add-On Service Manager
 */
class AddOnServiceManager {

    // Map: Reservation ID -> List of Services
    private Map<String, List<AddOnService>> serviceMap = new HashMap<>();

    /**
     * Attach service to reservation
     */
    public void addService(String reservationId, AddOnService service) {
        serviceMap
                .computeIfAbsent(reservationId, k -> new ArrayList<>())
                .add(service);

        System.out.println("Service added: " + service.getName()
                + " to Reservation " + reservationId);
    }

    /**
     * Calculate total add-on cost
     */
    public double calculateTotalCost(String reservationId) {
        List<AddOnService> services = serviceMap.getOrDefault(reservationId, new ArrayList<>());

        double total = 0;
        for (AddOnService s : services) {
            total += s.getPrice();
        }
        return total;
    }

    /**
     * Display services for reservation
     */
    public void displayServices(String reservationId) {
        List<AddOnService> services = serviceMap.get(reservationId);

        System.out.println("\nServices for Reservation " + reservationId + ":");

        if (services == null || services.isEmpty()) {
            System.out.println("No services selected.");
            return;
        }

        for (AddOnService s : services) {
            System.out.println("- " + s.getName() + " (₹" + s.getPrice() + ")");
        }

        System.out.println("Total Add-On Cost: ₹" + calculateTotalCost(reservationId));
    }
}

/**
 * Main Application
 */
public class BookMyStay {

    public static void main(String[] args) {

        // Core system setup
        RoomInventory inventory = new RoomInventory();
        BookingRequestQueue queue = new BookingRequestQueue();
        BookingService bookingService = new BookingService(inventory);

        // Add-On system
        AddOnServiceManager serviceManager = new AddOnServiceManager();

        // Create reservations
        Reservation r1 = new Reservation("RES-101", "Alice", "Single Room");
        Reservation r2 = new Reservation("RES-102", "Bob", "Double Room");

        // Add to queue
        queue.addRequest(r1);
        queue.addRequest(r2);

        // Process bookings
        bookingService.processBookings(queue);

        // Guest selects add-on services (AFTER booking)
        serviceManager.addService("RES-101", new AddOnService("Breakfast", 500));
        serviceManager.addService("RES-101", new AddOnService("WiFi", 200));
        serviceManager.addService("RES-102", new AddOnService("Airport Pickup", 1000));

        // Display services + cost
        serviceManager.displayServices("RES-101");
        serviceManager.displayServices("RES-102");

        System.out.println("\n✅ Core booking & inventory remain unchanged by add-ons.");
    }
}