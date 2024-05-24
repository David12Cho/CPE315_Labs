import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static MipsPipelinedSimulator simulator;
    public static void main(String[] args) throws IOException{
        // Example usage:
        // List<String> testInstructions = new ArrayList<>();
        // testInstructions.add("add $1, $2, $3");
        // testInstructions.add("sub $1, $2, $3");

        // simulator = new MipsPipelinedSimulator(testInstructions);

        // Load instructions and labels from file if provided
        if (args.length > 0) {
            loadAssemblyFile(args[0]);
        }

        // Run in interactive mode or script mode
        if (args.length > 1) {
            runScriptFile(args[1]);
        } else {
            interactiveMode();
        }


        // simulator.simulate();
    }


    // Load assembly file
    public static void loadAssemblyFile(String filename) throws IOException{ 
        Map<String, Integer> labels = new HashMap<>();
        List<String> instructions = new ArrayList<>();

        Scanner scanner = new Scanner(new File(filename));
        int address = 0;

        while (scanner.hasNextLine()) {
            String nextline = scanner.nextLine().trim();
            // Skip empty lines and comments
            if (nextline.isEmpty()){
                continue;
            }
            if(nextline.charAt(0) == '#'){
                continue;
            }
            // Remove inline comments and process labels
            if (nextline.contains("#")) {
                nextline = nextline.substring(0, nextline.indexOf("#")).trim();
            }
            if (nextline.contains(":")) {
                String label = nextline.substring(0, nextline.indexOf(":")).trim();
                nextline = nextline.substring(nextline.indexOf(":") + 1).trim();
                if (nextline.isEmpty()){
                    labels.put(label, address);
                    continue;
                } else {
                    labels.put(label, address - 1);
                }
            }
            instructions.add(nextline);
            address+=1;
        }
        scanner.close();

        // System.out.println(instructions);

        simulator = new MipsPipelinedSimulator(labels, instructions);

        // simulator.printStuff();
    }

    // Run script file
    public static void runScriptFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.print("mips> ");
                System.out.print(line.trim() + "\n");
                executeCommand(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Interactive mode
    public static void interactiveMode() {
        Scanner scanner = new Scanner(System.in);
        // System.out.println("MIPS Emulator started. Type 'h' for help.");
        boolean keepGoing = true;
        while (keepGoing) {
            System.out.print("mips> ");
            String command = scanner.nextLine().trim();
            keepGoing = executeCommand(command);
        }

        scanner.close();
    }

    // Execute command
    public static boolean executeCommand(String command) {
        if (command.equals("h")) {
            simulator.showHelp();
        } else if (command.equals("d")) {
            simulator.dumpRegisters();
        } else if (command.equals("s")) {
            simulator.stepThrough();
        } else if (command.startsWith("s ")) {
            int num = Integer.parseInt(command.substring(2).trim());
            simulator.stepThrough(num);
        } else if (command.equals("r")) {
            simulator.runTheRest();
        } else if (command.startsWith("m ")) {
            String[] parts = command.substring(2).trim().split(" ");
            int num1 = Integer.parseInt(parts[0]); 
            int num2 = Integer.parseInt(parts[1]);
            simulator.printMemory(num1, num2);
        } else if (command.equals("c")) {
            simulator.clear();
        } else if (command.equals("q")) {
            return false;
        } else {
            System.out.println("Invalid command. Type 'h' for help.");
        }

        return true;
    }
}
