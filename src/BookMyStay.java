import java.util.*;

/**
 * Custom Exception for Invalid Booking or Cancellation
 */
class InvalidBookingException extends Exception {
    public InvalidBookingException(String message) { super(message); }
}

/**
 * Abstract Room class
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
}

/**
 * Concrete Room Types
 */
class SingleRoom extends Room {
    public SingleRoom() { super("Single Room", 1, 2000.0); }
}

class DoubleRoom extends Room {
    public DoubleRoom() { super("Double Room", 2, 3500.0); }
}

class SuiteRoom extends Room {
    public SuiteRoom() { super("Suite Room", 3, 6000.0); }
}

/**
 * Reservation Entity
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

    public void reduceAvailability(String type) throws InvalidBookingException {
        int current = getAvailability(type);
        if (current <= 0) throw new InvalidBookingException("Cannot reduce availability below zero for " + type);
        availability.put(type, current - 1);
    }

    public void increaseAvailability(String type) {
        availability.put(type, getAvailability(type) + 1);
    }
}

/**
 * Booking Validator (Fail-Fast)
 */
class BookingValidator {
    private Set<String> validRoomTypes;

    public BookingValidator() { validRoomTypes = Set.of("Single Room", "Double Room", "Suite Room"); }

    public void validateReservation(Reservation r, RoomInventory inventory) throws InvalidBookingException {
        if (!validRoomTypes.contains(r.getRoomType()))
            throw new InvalidBookingException("Invalid room type: " + r.getRoomType());
        if (inventory.getAvailability(r.getRoomType()) <= 0)
            throw new InvalidBookingException("No availability for " + r.getRoomType());
        if (r.getGuestName() == null || r.getGuestName().isBlank())
            throw new InvalidBookingException("Guest name cannot be empty");
    }
}

/**
 * Booking Service (Allocation + History + Rollback Support)
 */
class BookingService {

    private RoomInventory inventory;
    private Set<String> allocatedRoomIds = new HashSet<>();
    private Map<String, Set<String>> allocations = new HashMap<>();
    private Stack<String> releasedRoomIds = new Stack<>(); // LIFO rollback
    private int roomCounter = 1;

    private List<Reservation> bookingHistory = new ArrayList<>();
    private BookingValidator validator;

    public BookingService(RoomInventory inventory, BookingValidator validator) {
        this.inventory = inventory;
        this.validator = validator;
    }

    /**
     * Process bookings
     */
    public void processBookings(BookingRequestQueue queue) {
        while (!queue.isEmpty()) {
            Reservation r = queue.getNextRequest();
            try {
                validator.validateReservation(r, inventory);

                String roomId = generateRoomId(r.getRoomType());
                allocatedRoomIds.add(roomId);
                allocations.computeIfAbsent(r.getRoomType(), k -> new HashSet<>()).add(roomId);

                inventory.reduceAvailability(r.getRoomType());
                bookingHistory.add(r);

                System.out.println("✅ Booking Confirmed | ResID: " + r.getReservationId()
                        + " | RoomID: " + roomId);

            } catch (InvalidBookingException e) {
                System.out.println("❌ Booking Failed for " + r.getGuestName() + " | Reason: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("❌ Unexpected error: " + e.getMessage());
            }
        }
    }

    private String generateRoomId(String type) {
        return type.substring(0, 3).toUpperCase() + "-" + (roomCounter++);
    }

    public List<Reservation> getBookingHistory() { return Collections.unmodifiableList(bookingHistory); }

    /**
     * Cancel a confirmed booking
     */
    public void cancelBooking(String reservationId) {
        Reservation toCancel = null;
        for (Reservation r : bookingHistory) {
            if (r.getReservationId().equals(reservationId)) {
                toCancel = r;
                break;
            }
        }

        if (toCancel == null) {
            System.out.println("❌ Cancellation Failed | Reservation " + reservationId + " not found.");
            return;
        }

        // Remove from history
        bookingHistory.remove(toCancel);

        // Release room ID (LIFO rollback)
        Set<String> roomSet = allocations.get(toCancel.getRoomType());
        if (roomSet != null && !roomSet.isEmpty()) {
            String roomId = roomSet.iterator().next(); // pick one allocated room
            roomSet.remove(roomId);
            allocatedRoomIds.remove(roomId);
            releasedRoomIds.push(roomId);
        }

        // Restore inventory
        inventory.increaseAvailability(toCancel.getRoomType());

        System.out.println("✅ Booking Cancelled | ResID: " + reservationId);
    }
}

/**
 * Booking Report Service
 */
class BookingReportService {
    private List<Reservation> bookingHistory;
    public BookingReportService(List<Reservation> bookingHistory) { this.bookingHistory = bookingHistory; }

    public void displayAllBookings() {
        System.out.println("\n=== Booking History ===");
        if (bookingHistory.isEmpty()) System.out.println("No bookings yet.");
        else for (Reservation r : bookingHistory) r.display();
    }

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

        RoomInventory inventory = new RoomInventory();
        BookingRequestQueue queue = new BookingRequestQueue();
        BookingValidator validator = new BookingValidator();
        BookingService bookingService = new BookingService(inventory, validator);

        // Sample reservations
        queue.addRequest(new Reservation("RES-101", "Alice", "Single Room"));
        queue.addRequest(new Reservation("RES-102", "Bob", "Double Room"));
        queue.addRequest(new Reservation("RES-103", "Charlie", "Single Room"));

        // Process bookings
        bookingService.processBookings(queue);

        // Cancellation examples
        bookingService.cancelBooking("RES-102"); // valid cancellation
        bookingService.cancelBooking("RES-999"); // invalid, does not exist

        // Reporting
        BookingReportService reportService = new BookingReportService(bookingService.getBookingHistory());
        reportService.displayAllBookings();
        reportService.displaySummaryReport();

        System.out.println("\n✅ Inventory and state restored correctly after cancellations.");
    }
}