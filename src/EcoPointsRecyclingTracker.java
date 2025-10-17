import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.*;

public class EcoPointsRecyclingTracker {

    private static Scanner scanner = new Scanner(System.in);

    private static Map<String, Household> households = new HashMap<>();

    public static void main(String[] args) {

        loadHouseholdsFromFile();

        boolean running = true;

        while (running) {
            System.out.println("\n=== Eco-Points Recycling Tracker ===");
            System.out.println("1. Register Household");
            System.out.println("2. Log Recycling Event");
            System.out.println("3. Display Households");
            System.out.println("4. Display Household Recycling Events");
            System.out.println("5. Generate Reports");
            System.out.println("6. Save and Exit");

            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    registerHousehold();
                    break;
                case "2":
                    logRecyclingEvent();
                    break;
                case "3":
                    displayHouseholds();
                    break;
                case "4":
                    displayHouseholdEvents();
                    break;
                case "5":
                    generateReports();
                    break;
                case "6":
                    saveHouseholdsToFile();
                    running = false;
                    System.out.println("Data saved. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }

    private static void registerHousehold() {

        System.out.print("Enter Household ID: ");
        String id = scanner.nextLine().trim();

        System.out.print("Enter Household Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Household Address: ");
        String address = scanner.nextLine().trim();

        // Check if a household with this ID already exists in the map
        if (households.containsKey(id)) {
            System.out.println("Household with this ID already exists.");
        } else {
            Household household = new Household(id, name, address);
            households.put(id, household);

            // Confirm to the user that the household was registered successfully
            System.out.println("Household registered successfully on " + household.getJoinDate());
        }
    }

    private static void logRecyclingEvent() {

        System.out.print("Enter Household ID: ");
        String id = scanner.nextLine().trim();

        Household household = households.get(id);

        if (household == null) {
            System.out.println("Household not found.");
            return;
        }

        // Ask the user for the material type they recycled
        System.out.print("Enter Material Type (Plastic / Glass / Metal / Paper): ");
        String materialType = scanner.nextLine().trim();

        double weight = 0.0;

        while (true) {
            try {
                System.out.println("Enter Wight in Kilograms: ");
                weight = Double.parseDouble(scanner.nextLine().trim());

                if (weight <= 0)
                    throw new IllegalArgumentException();
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid weight. Must be a positive number.");
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid weight. Must be a positive number.");
            }
        }

        RecyclingEvent event = new RecyclingEvent(materialType, weight);
        household.addEvent(event);

        System.out.println("Recycling event logged successfully on " + event.getDate() + ". Ecopoints earned: "
                + event.getEcoPoints());
    }

    private static void displayHouseholds() {

        if (households.isEmpty()) {
            System.out.println("No households registered yet.");
            return;
        }

        System.out.println("\n=== Registered Households ===");

        for (Household household : households.values()) {
            System.out.println("---------------------------");
            System.out.println("ID: " + household.getId());
            System.out.println("Name: " + household.getName());
            System.out.println("Address: " + household.getAddress());
            System.out.println("Join Date: " + household.getJoinDate());
            System.out.println("---------------------------");
        }
    }

    private static void displayHouseholdEvents() {
        System.out.print("Enter Household ID: ");
        String id = scanner.nextLine().trim();

        Household household = households.get(id);

        if (household == null) {
            System.out.println("Household not found.");
            return;
        }

        System.out.println("\nRecycling Events for " + household.getName() + ":");

        if (household.getEvents().isEmpty()) {
            System.out.println("No recycling events logged for this household.");
            return;
        } else {
            for (RecyclingEvent e : household.getEvents()) {
                System.out.println(e);
            }

            // After listing events, show the total weight recycled by this household
            System.out.println("Total Weight: " + household.getTotalWeight() + " kg");
            // Show the total eco points earned by this household
            System.out.println("Total Points: " + household.getTotalPoints() + " pts");
        }
    }

    private static void generateReports() {

        if (households.isEmpty()) {
            System.out.println("No households registered.");
            return;
        }

        // ------------------------------
        // Find the household with the highest points
        // ------------------------------
        Household top = null; // Start with no top household
        for (Household h : households.values()) {
            // If 'top' is still null, or this household has more points, update 'top'
            if (top == null || h.getTotalPoints() > top.getTotalPoints()) {
                top = h;
            }
        }

        // Print details of the top household
        System.out.println("\nHousehold with Highest Points:");
        System.out.println("ID: " + top.getId() +
                ", Name: " + top.getName() +
                ", Points: " + top.getTotalPoints());

        // ------------------------------
        // Calculate total community recycling weight
        // ------------------------------
        double totalWeight = 0.0;

        // Loop through all households to sum up their total weights
        for (Household h : households.values()) {
            totalWeight += h.getTotalWeight();
        }

        // Print total community weight
        System.out.println("Total Community Recycling Weight: " + totalWeight + " kg");
    }

    private static void saveHouseholdsToFile() {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream("households.ser"))) {

            // Write the entire households map to the file
            out.writeObject(households);
            System.out.println("Data saved successfully to households.ser");

            // Also write to a text file in a readable format
            try (PrintWriter writer = new PrintWriter(new FileWriter("households.txt"))) {
                for (Map.Entry<String, Household> entry : households.entrySet()) {
                    String key = entry.getKey();
                    Household household = entry.getValue();
                    writer.println("Household ID: " + key);
                    writer.println("Household Details: " + household.toString());
                    writer.println("-----------------------------");
                }
                System.out.println("Data saved successfully to households.txt");
            }

        } catch (IOException e) {
            // If something goes wrong while saving, print an error message
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static void loadHouseholdsFromFile() {
        try (
                ObjectInputStream in = new ObjectInputStream(new FileInputStream("households.ser"))) {
            households = (Map<String, Household>) in.readObject();
            System.out.println("Household data loaded.");
        } catch (FileNotFoundException e) {
            // If the file doesn't exist yet, that's okay — start with empty data
            System.out.println("No saved data found. Starting fresh.");
        } catch (IOException | ClassNotFoundException e) {
            // Handle other errors, like if the file is corrupted or unreadable
            System.out.println("Error loading data: " + e.getMessage());
        }
    }

}
