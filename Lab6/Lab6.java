import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Lab6 {

    public static ArrayList<Integer> parse(String filePath) {
        ArrayList<Integer> addressList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] fields = line.trim().split(",");
                if (fields.length > 1) {
                    int sessionID = Integer.parseInt(fields[1].trim());
                    addressList.add(sessionID);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading from file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format in file: " + e.getMessage());
        }
        return addressList;
    }

    public static void main(String[] args) {
        String filePath = "/Users/davidcho/Downloads/lab6/src/input.txt";
        ArrayList<Integer> adresseses = parse(filePath);
        // Debug: Print out the first few session IDs
        for (int i = 0; i < Math.min(10, adresseses.size()); i++) {
            System.out.println("Session ID " + i + ": " + adresseses.get(i));
        }
    }
}
