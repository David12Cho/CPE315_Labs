import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Lab6 {

    public static ArrayList<Integer> parse(String filePath) throws IOException {
        ArrayList<Integer> addressList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line = reader.readLine();
        while ((line = reader.readLine()) != null) {
            String[] fields = line.trim().split(",");
            if (fields.length > 1) {
                String secondElement = fields[1].trim();
                int sessionID = Integer.parseInt(secondElement);
                addressList.add(sessionID);
            }
        }
        reader.close();
        return addressList;
    }

    public static void main(String[] args) throws IOException {
        String filePath = "/Users/davidcho/Downloads/lab6/src/input.txt"; // Ensure this path points to your data file
        ArrayList<Integer> addresses = parse(filePath);
        // Debug: Print out the first few session IDs
        for (int i = 0; i < Math.min(10, addresses.size()); i++) {
            System.out.println("Session ID " + i + ": " + addresses.get(i));
        }
    }
}






