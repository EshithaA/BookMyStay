import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class representing a Room.
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
 * Single Room implementation
 */
class SingleRoom extends Room {

    public SingleRoom() {
        super("Single Room", 1, 2000.0);
    }

    @Override
    public void displayDetails() {
        System.out.println("Type: " + getType());
        System.out.println("Beds: " + getBeds());
        System.out.println("Price: ₹" + getPrice());
    }
}

/**
 * Double Room implementation
 */
class DoubleRoom extends Room {

    public DoubleRoom() {
        super("Double Room", 2, 3500.0);
    }

    @Override
    public void displayDetails() {
        System.out.println("Type: " + getType());
        System.out.println("Beds: " + getBeds());
        System.out.println("Price: ₹" + getPrice());
    }
}

/**
 * Suite Room implementation
 */
class SuiteRoom extends Room {

    public SuiteRoom() {
        super("Suite Room", 3, 6000.0);
    }

    @Override
    public void displayDetails() {
        System.out.println("Type: " + getType());
        System.out.println("Beds: " + getBeds());
        System.out.println("Price: ₹" + getPrice());
    }
}

/**
 * Centralized Room Inventory using HashMap
 */
class RoomInventory {

    private Map<String, Integer> availabilityMap;

    public RoomInventory() {
        availabilityMap = new HashMap<>();

        // Initialize availability
        availabilityMap.put("Single Room", 5);
        availabilityMap.put("Double Room", 3);
        availabilityMap.put("Suite Room", 2);
    }

    public int getAvailability(String roomType) {
        return availabilityMap.getOrDefault(roomType, 0);
    }

    public void bookRoom(String roomType) {
        int current = getAvailability(roomType);

        if (current > 0) {
            availabilityMap.put(roomType, current - 1);
            System.out.println(roomType + " booked successfully.");
        } else {
            System.out.println(roomType + " is not available.");
        }
    }

    public void displayInventory() {
        System.out.println("=== Room Availability ===");
        for (Map.Entry<String, Integer> entry : availabilityMap.entrySet()) {
            System.out.println(entry.getKey() + " -> Available: " + entry.getValue());
        }
    }
}

/**
 * Main Application Entry Point
 */
public class BookMyStay {

    public static void main(String[] args) {

        // Create room objects (Polymorphism)
        Room single = new SingleRoom();
        Room dbl = new DoubleRoom();
        Room suite = new SuiteRoom();

        // Initialize inventory
        RoomInventory inventory = new RoomInventory();

        System.out.println("=== Hotel Booking System v2.0 ===\n");

        // Display room details + availability
        single.displayDetails();
        System.out.println("Available: " + inventory.getAvailability(single.getType()));
        System.out.println("---------------------------");

        dbl.displayDetails();
        System.out.println("Available: " + inventory.getAvailability(dbl.getType()));
        System.out.println("---------------------------");

        suite.displayDetails();
        System.out.println("Available: " + inventory.getAvailability(suite.getType()));
        System.out.println("---------------------------");

        // Simulate booking
        System.out.println("\nBooking a Single Room...");
        inventory.bookRoom("Single Room");

        // Show updated inventory
        System.out.println("\nUpdated Inventory:");
        inventory.displayInventory();
    }
}