/**
 * Abstract class representing a generic Room in the hotel.
 * It defines common properties and behavior shared by all room types.
 *
 * @author YourName
 * @version 1.0
 */
abstract class Room {

    private String type;
    private int beds;
    private double price;

    /**
     * Constructor to initialize common room attributes.
     */
    public Room(String type, int beds, double price) {
        this.type = type;
        this.beds = beds;
        this.price = price;
    }

    // Getters (Encapsulation)
    public String getType() {
        return type;
    }

    public int getBeds() {
        return beds;
    }

    public double getPrice() {
        return price;
    }

    /**
     * Abstract method to display room-specific details.
     */
    public abstract void displayDetails();
}

/**
 * Represents a Single Room.
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
 * Represents a Double Room.
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
 * Represents a Suite Room.
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
 * Entry point of the application.
 * Demonstrates object creation, polymorphism, and simple availability tracking.
 */
public class BookMyStay {

    public static void main(String[] args) {

        // Create room objects (Polymorphism: reference type is Room)
        Room single = new SingleRoom();
        Room dbl = new DoubleRoom();
        Room suite = new SuiteRoom();

        // Static availability (simple variables)
        int singleAvailable = 5;
        int doubleAvailable = 3;
        int suiteAvailable = 2;

        System.out.println("=== Hotel Room Availability ===\n");

        // Display Single Room
        single.displayDetails();
        System.out.println("Available: " + singleAvailable);
        System.out.println("---------------------------");

        // Display Double Room
        dbl.displayDetails();
        System.out.println("Available: " + doubleAvailable);
        System.out.println("---------------------------");

        // Display Suite Room
        suite.displayDetails();
        System.out.println("Available: " + suiteAvailable);
        System.out.println("---------------------------");

        System.out.println("Application  समाप्त (Finished).");
    }
}