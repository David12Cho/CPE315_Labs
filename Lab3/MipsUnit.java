import java.io.*;
import java.util.*;

public class MipsUnit {

    private Map<String, Integer> labels;
    private List<String> instructions;
    private int programCounter;
    private Map<String, Integer> registers;
    private int[] dataMemory;

    public MipsUnit(HashMap<String, Integer> labels, ArrayList<String> instructions) {
        // Initialize programCounter and dataMemory
        programCounter = 0;
        dataMemory = new int[8192];

        // Initialize label
        this.labels = labels;

        // Initialize instructions
        this.instructions = instructions;

        // Initialize registers
        registers = new HashMap<>();
        registers.put("$0", 0);
        registers.put("$v0", 0);
        registers.put("$v1", 0);
        registers.put("$a0", 0);
        registers.put("$a1", 0);
        registers.put("$a2", 0);
        registers.put("$a3", 0);
        registers.put("$t0", 0);
        registers.put("$t1", 0);
        registers.put("$t2", 0);
        registers.put("$t3", 0);
        registers.put("$t4", 0);
        registers.put("$t5", 0);
        registers.put("$t6", 0);
        registers.put("$t7", 0);
        registers.put("$s0", 0);
        registers.put("$s1", 0);
        registers.put("$s2", 0);
        registers.put("$s3", 0);
        registers.put("$s4", 0);
        registers.put("$s5", 0);
        registers.put("$s6", 0);
        registers.put("$s7", 0);
        registers.put("$t8", 0);
        registers.put("$t9", 0);
        registers.put("$sp", 0);
        registers.put("$ra", 0);
    }

    public MipsUnit() {
        this(new HashMap<String, Integer>(), new ArrayList<String>());
    }

    // Prints out valid commands
    public void showHelp() {
        System.out.print(
                """
                h = show help
                d = dump register state
                s = single step through the program (i.e. execute 1 instruction and stop)
                s num = step through num instructions of the program
                r = run until the program ends
                m num1 num2 = display data memory from location num1 to num2
                c = clear all registers, memory, and the program counter to 0
                q = exit the program
                \n
                """
        );
    }

    // Set all registers to 0
    public void dumpRegisters() {
        System.out.printf("pc = %d\n", programCounter);

        System.out.printf("$0 = %d\t$v0 = %d\t$v1 = %d\t$a0 = %d\t\n",
                registers.get("$0"),
                registers.get("$v0"),
                registers.get("$v1"),
                registers.get("$a0")
        );

        System.out.printf("$a1 = %d\t$a2 = %d\t$a3 = %d\t$t0 = %d\t\n",
                registers.get("$a1"),
                registers.get("$a2"),
                registers.get("$a3"),
                registers.get("$t0")
        );
        System.out.printf("$t1 = %d\t$t2 = %d\t$t3 = %d\t$t4 = %d\t\n",
                registers.get("$t1"),
                registers.get("$t2"),
                registers.get("$t3"),
                registers.get("$t4")
        );
        System.out.printf("$t5 = %d\t$t6 = %d\t$t7 = %d\t$s0 = %d\t\n",
                registers.get("$t5"),
                registers.get("$t6"),
                registers.get("$t7"),
                registers.get("$s0")
        );
        System.out.printf("$s1 = %d\t$s2 = %d\t$s3 = %d\t$s4 = %d\t\n",
                registers.get("$s1"),
                registers.get("$s2"),
                registers.get("$s3"),
                registers.get("$s4")
        );
        System.out.printf("$s5 = %d\t$s6 = %d\t$s7 = %d\t$t8 = %d\t\n",
                registers.get("$s5"),
                registers.get("$s6"),
                registers.get("$s7"),
                registers.get("$t8")
        );
        System.out.printf("$t9 = %d\t$sp = %d\t$ra = %d\n",
                registers.get("$t9"),
                registers.get("$sp"),
                registers.get("$ra")
        );
    }

    // Execute a single instruction
    public void executeLine() {
        if (programCounter < instructions.size()) {
            String instruction = instructions.get(programCounter);
            // Parsing and executing instruction logic goes here
            String[] parts = instruction.split("[ ,()]+");
            String opcode = parts[0];

            switch (opcode) {
                case "addi":
                    String regDest = parts[1];
                    String regSrc = parts[2];
                    int immediate = Integer.parseInt(parts[3]);
                    registers.put(regDest, registers.get(regSrc) + immediate);
                    break;
                case "add":
                    regDest = parts[1];
                    regSrc = parts[2];
                    String regSrc2 = parts[3];
                    registers.put(regDest, registers.get(regSrc) + registers.get(regSrc2));
                    break;
                case "sw":
                    regSrc = parts[1];
                    int offset = Integer.parseInt(parts[2]);
                    regDest = parts[3];
                    dataMemory[registers.get(regDest) + offset] = registers.get(regSrc);
                    break;
                case "bne":
                    regSrc = parts[1];
                    regSrc2 = parts[2];
                    String label = parts[3];
                    if (!registers.get(regSrc).equals(registers.get(regSrc2))) {
                        programCounter = labels.get(label) - 1; // -1 because we increment later
                    }
                    break;
                // Add more cases for other instructions as needed
            }

            programCounter++;
        }
    }

    // Step through one instruction in the program
    public void stepThrough() {
        stepThrough(1);
    }

    // Step through n instructions in the program
    public void stepThrough(int numSteps) {
        for (int i = 0; i < numSteps; i++) {
            executeLine();
        }
        System.out.printf("%d instruction(s) executed\n", numSteps);
    }

    // Run until program ends
    public void runTheRest() {
        while (programCounter < instructions.size()) {
            stepThrough();
        }
    }

    // Display data memory between two locations (inclusive)
    public void printMemory(int num1, int num2) {
        for (int i = num1; i <= num2; i++) {
            System.out.printf("[%d] = %d\n", i, dataMemory[i]);
        }
    }

    // Clears registers, data memory, and sets pc back to 0
    public void clear() {
        for (String reg : registers.keySet()) {
            registers.put(reg, 0);
        }

        // Clear memory
        Arrays.fill(dataMemory, 0);

        // Pc back to 0
        programCounter = 0;

        System.out.print("\tsimulator reset\n");
    }

    // Main method to handle command line input
    public static void main(String[] args) {
        MipsUnit emulator = new MipsUnit();

        // Load instructions and labels from file if provided
        if (args.length > 0) {
            emulator.loadAssemblyFile(args[0]);
        }

        // Run in interactive mode or script mode
        if (args.length > 1) {
            emulator.runScriptFile(args[1]);
        } else {
            emulator.interactiveMode();
        }
    }

    // Load assembly file
    public void loadAssemblyFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    if (line.endsWith(":")) {
                        labels.put(line.substring(0, line.length() - 1), instructions.size());
                    } else {
                        instructions.add(line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Run script file
    public void runScriptFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                executeCommand(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Interactive mode
    public void interactiveMode() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("MIPS Emulator started. Type 'h' for help.");
        while (true) {
            System.out.print("mips> ");
            String command = scanner.nextLine().trim();
            executeCommand(command);
        }
    }

    // Execute command
    public void executeCommand(String command) {
        if (command.equals("h")) {
            showHelp();
        } else if (command.equals("d")) {
            dumpRegisters();
        } else if (command.equals("s")) {
            stepThrough();
        } else if (command.startsWith("s ")) {
            int num = Integer.parseInt(command.substring(2).trim());
            stepThrough(num);
        } else if (command.equals("r")) {
            runTheRest();
        } else if (command.startsWith("m ")) {
            String[] parts = command.substring(2).trim().split(" ");
            int num1 = Integer.parseInt(parts[0]);
            int num2 = Integer.parseInt(parts[1]);
            printMemory(num1, num2);
        } else if (command.equals("c")) {
            clear();
        } else if (command.equals("q")) {
            quitProgram();
        } else {
            System.out.println("Invalid command. Type 'h' for help.");
        }
    }

    // Quit program
    public void quitProgram() {
        System.out.println("Exiting MIPS Emulator.");
        System.exit(0);
    }
}









