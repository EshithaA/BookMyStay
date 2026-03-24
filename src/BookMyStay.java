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

    public String getType() {
        return type;
    }

    public int getBeds() {
        return beds;
    }

    public double getPrice() {
        return price;
    }

    public abstract void displayDetails();
}

/**
 * Concrete Room Types
 */
class SingleRoom extends Room {
    public SingleRoom() {
        super("Single Room", 1, 2000.0);
    }

    public void displayDetails() {
        System.out.println("Type: " + getType() + ", Beds: " + getBeds() + ", Price: ₹" + getPrice());
    }
}

class DoubleRoom extends Room {
    public DoubleRoom() {
        super("Double Room", 2, 3500.0);
    }

    public void displayDetails() {
        System.out.println("Type: " + getType() + ", Beds: " + getBeds() + ", Price: ₹" + getPrice());
    }
}

class SuiteRoom extends Room {
    public SuiteRoom() {
        super("Suite Room", 3, 6000.0);
    }

    public void displayDetails() {
        System.out.println("Type: " + getType() + ", Beds: " + getBeds() + ", Price: ₹" + getPrice());
    }
}

/**
 * Reservation (Represents a booking request)
 */
class Reservation {

    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getRoomType() {
        return roomType;
    }

    public void display() {
        System.out.println("Guest: " + guestName + " | Requested: " + roomType);
    }
}

/**
 * Booking Request Queue (FIFO)
 */
class BookingRequestQueue {

    private Queue<Reservation> queue;

    public BookingRequestQueue() {
        queue = new LinkedList<>();
    }

    /**
     * Add a booking request (enqueue)
     */
    public void addRequest(Reservation reservation) {
        queue.offer(reservation);
        System.out.println("Request added for " + reservation.getGuestName());
    }

    /**
     * View all queued requests (read-only)
     */
    public void displayQueue() {
        System.out.println("\n=== Booking Request Queue (FIFO Order) ===");

        if (queue.isEmpty()) {
            System.out.println("No pending requests.");
            return;
        }

        for (Reservation r : queue) {
            r.display();
        }
    }

    /**
     * Peek next request (without removing)
     */
    public Reservation peekNext() {
        return queue.peek();
    }
}

/**
 * Main Application
 */
public class BookMyStay {

    public static void main(String[] args) {

        // Initialize queue system (no inventory interaction here)
        BookingRequestQueue requestQueue = new BookingRequestQueue();

        // Simulate incoming booking requests (arrival order)
        requestQueue.addRequest(new Reservation("Alice", "Single Room"));
        requestQueue.addRequest(new Reservation("Bob", "Double Room"));
        requestQueue.addRequest(new Reservation("Charlie", "Suite Room"));
        requestQueue.addRequest(new Reservation("Diana", "Single Room"));

        // Display queued requests (FIFO order preserved)
        requestQueue.displayQueue();

        // Show next request to be processed
        System.out.println("\nNext request to process:");
        Reservation next = requestQueue.peekNext();
        if (next != null) {
            next.display();
        }

        System.out.println("\nNote: No rooms allocated yet. Inventory unchanged.");
    }
}