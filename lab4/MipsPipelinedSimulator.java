import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

class PipelineRegister {
    String instruction;
    String[] registersInUse;
    int[] operands;
    int result;
    boolean isEmpty;

    PipelineRegister() {
        this.instruction = null;
        this.registersInUse = new String[3];
        this.operands = new int[3]; // Assuming max three operands
        this.result = 0;
        this.isEmpty = true;
    }

    void clear() {
        instruction = null;
        registersInUse = new String[3];
        operands = new int[3];
        result = 0;
        isEmpty = true;
    }
}

public class MipsPipelinedSimulator {
    private Map<String, Integer> labels;
    private List<String> instructions;
    private Map<String, Integer> registers;
    private int[] memory;
    private int pc; // Program counter
    private int cycleCounter;
    private boolean loadStall;
    private int branchStallBy;
    private boolean stillRunning;

    // Pipeline stages
    private PipelineRegister ifId, idEx, exMem, memWb;

    public MipsPipelinedSimulator(Map<String, Integer> labels, List<String> instructions) {
        this.instructions = instructions;
        this.labels = labels;
        this.registers = new HashMap<>();
        this.memory = new int[8192]; // Simple memory
        this.pc = 0;
        this.cycleCounter = 0;
        loadStall = false;
        branchStallBy = 0;
        stillRunning = true;
        initializeRegisters();
        initializePipeline();
    }

    private void initializeRegisters() {
        // Initialize registers $0 to $31, assuming $0 is always 0
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

    private void initializePipeline() {
        ifId = new PipelineRegister();
        idEx = new PipelineRegister();
        exMem = new PipelineRegister();
        memWb = new PipelineRegister();
    }

    public void simulate() {
        boolean running = true;
        while (running) {
            cycleCounter++;
            writeBack();
            memoryAccess();
            execute();
            decode();
            fetch();
            showPipelineRegisters(); // Show the state of the pipeline
        }
    }

    public void clear() {
        // Reset all registers to 0, except $0 if your architecture requires it to be always 0
        registers.keySet().forEach(key -> registers.put(key, 0));
        registers.put("$0", 0);  // MIPS convention: Register $0 is always 0

        // Clear memory
        Arrays.fill(memory, 0);

        // Reset pipeline registers
        ifId.clear();
        idEx.clear();
        exMem.clear();
        memWb.clear();

        // Reset the program counter and cycle counter
        pc = 0;
        cycleCounter = 0;

        System.out.println("All registers, memory, and pipeline states have been reset.");
    }


    private void simulateOne() {
        if(pc >= instructions.size() && 
            ifId.isEmpty &&
            idEx.isEmpty &&
            exMem.isEmpty &&
            memWb.isEmpty){
                
                stillRunning = false;
        }
        if (!shouldStall()) {
            if (!exMem.isEmpty) {
                memWb = exMem;  // Move from EX/MEM to MEM/WB
            }
            if (!idEx.isEmpty) {
                exMem = idEx;   // Move from ID/EX to EX/MEM
            }
            if (!ifId.isEmpty) {
                idEx = ifId;    // Move from IF/ID to ID/EX
            }
            fetch();           // Fetch next instruction into IF/ID
        }          // Update program counter if not stalling
        showPipelineRegisters();
    }


    private boolean shouldStall() {
        // Example: stall if loading from a register that will be used in the subsequent instruction
        if (idEx.instruction != null && idEx.instruction.startsWith("lw")) {
            String destReg = idEx.instruction.split("\\s+")[1];
            System.out.print("Returned should stall: ");
            System.out.println(ifId.instruction != null && ifId.instruction.contains(destReg));
            return ifId.instruction != null && ifId.instruction.contains(destReg);
        }
        System.out.println("Returned should stall: false");
        return false;
    }




    private void fetch() {
        if (ifId.isEmpty && pc < instructions.size()) {
            ifId.instruction = instructions.get(pc++);
            ifId.isEmpty = false;
        }
    }


    private boolean checkStallCondition() {
        // Simple stall condition: stall if the next instruction is a load and the following one uses the loaded data
        if (!idEx.isEmpty && idEx.instruction.startsWith("lw")) {
            String loadedReg = idEx.registersInUse[0]; // Assuming the loaded register is the first in use
            if (!ifId.isEmpty && (ifId.instruction.contains(loadedReg))) {
                return true; // Stall condition met
            }
        }
        return false;
    }

    private void decode() {
        if (!ifId.isEmpty) {
            String[] parts = ifId.instruction.split("\\s+");
            String opcode = parts[0];
            idEx.instruction = opcode;
            idEx.registersInUse = new String[parts.length - 1];
            idEx.operands = new int[parts.length - 1]; // Assuming all other parts are operands

            for (int i = 1; i < parts.length; i++) {
                String operand = parts[i].replace(",", "").trim();
                if (operand.contains("(")) {
                    // Handle memory access, e.g., 0($a1)
                    String[] memAccess = operand.split("\\(");
                    int offset = Integer.parseInt(memAccess[0]);
                    String reg = memAccess[1].substring(0, memAccess[1].length() - 1); // Remove the closing ')'
                    idEx.registersInUse[i - 1] = reg;
                    idEx.operands[i - 1] = offset + registers.getOrDefault(reg, 0);
                } else if (operand.startsWith("$")) {
                    idEx.registersInUse[i - 1] = operand;
                    idEx.operands[i - 1] = registers.getOrDefault(operand, 0);
                } else {
                    // Handle immediate values
                    idEx.registersInUse[i - 1]  = "None";
                    idEx.operands[i - 1] = Integer.parseInt(operand);
                }
            }

            idEx.isEmpty = false;
            ifId.clear();
        }
    }

    private void execute() {
        if (!idEx.isEmpty) {
            String opcode = idEx.instruction;
            // Clone operands to allow modifications if forwarding is applied
            int[] operands = Arrays.copyOf(idEx.operands, idEx.operands.length);
            boolean regWrite = false;

            // Forwarding logic (assuming it's correctly implemented before)
            // Your existing forwarding code goes here

            // Execute the operation based on the opcode
            try {
                switch (opcode) {
                    case "add":
                    case "sub":
                    case "and":
                    case "or":
                    case "slt":
                        if (idEx.registersInUse.length > 2) { // Ensure there are enough operands
                            int result = performOperation(opcode, operands);
                            exMem.result = result;
                            exMem.isEmpty = false;
                            regWrite = true;
                        }
                        break;
                    case "addi":
                        if (idEx.registersInUse.length > 1) { // Immediate operations typically use at least two operands
                            exMem.result = operands[1] + operands[0]; // operands[0] might be immediate here
                            exMem.isEmpty = false;
                            regWrite = true;
                        }
                        break;
                    case "lw":
                        // if(idEx.registersInUse[0].equals())
                    case "sw":
                        // Memory operations: handling differently if necessary
                        break;
                    case "beq":
                    case "bne":
                        // Branch operations: handle PC changes
                        break;
                    default:
                        System.out.println("Unsupported operation: " + opcode);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Error executing instruction: " + opcode + " with insufficient operands.");
            }

            // Handling register write-back setup
            if (regWrite && idEx.registersInUse.length > 0) {
                exMem.registersInUse = new String[]{idEx.registersInUse[0]}; // Ensure register destination exists
            } else {
                exMem.registersInUse = new String[]{"None"};
            }

            idEx.clear(); // Clear the ID/EX pipeline register after use
        }
    }

    private int performOperation(String opcode, int[] operands) {
        // Perform operation based on the opcode
        switch (opcode) {
            case "add":
                return operands[1] + operands[2];
            case "sub":
                return operands[1] - operands[2];
            case "and":
                return operands[1] & operands[2];
            case "or":
                return operands[1] | operands[2];
            case "slt":
                return (operands[1] < operands[2]) ? 1 : 0;
            default:
                throw new IllegalArgumentException("Unsupported operation: " + opcode);
        }
    }


    private void memoryAccess() {
        if (!exMem.isEmpty) {
            // Handle memory access
            memWb.result = exMem.result;
            memWb.isEmpty = false;
            exMem.clear();
        }
    }

    private void writeBack() {
        if (!memWb.isEmpty) {
            // Write back to register
            memWb.clear();
        }
    }

    //Everything from Lab 3
    // Prints out valid commands
    public void showHelp() {
        System.out.print(
                """
 
                h = show help
                d = dump register state
                p = show pipeline registers
                s = step through a single clock cycle step (i.e. simulate 1 cycle and stop)
                s num = step through num clock cycles
                r = run until the program ends and display timing summary
                m num1 num2 = display data memory from location num1 to num2
                c = clear all registers, memory, and the program counter to 0
                q = exit the program
               
                """
        );
    }

    // Set all registers to 0
    public void dumpRegisters() {
        System.out.print("\n");
        System.out.printf("pc = %d\n", pc);

        System.out.printf("$0 = %d          $v0 = %d         $v1 = %d         $a0 = %d\n",
                registers.get("$0"),
                registers.get("$v0"),
                registers.get("$v1"),
                registers.get("$a0")
        );

        System.out.printf("$a1 = %d         $a2 = %d         $a3 = %d         $t0 = %d\n",
                registers.get("$a1"),
                registers.get("$a2"),
                registers.get("$a3"),
                registers.get("$t0")
        );
        System.out.printf("$t1 = %d         $t2 = %d         $t3 = %d         $t4 = %d\n",
                registers.get("$t1"),
                registers.get("$t2"),
                registers.get("$t3"),
                registers.get("$t4")
        );
        System.out.printf("$t5 = %d         $t6 = %d         $t7 = %d         $s0 = %d\n",
                registers.get("$t5"),
                registers.get("$t6"),
                registers.get("$t7"),
                registers.get("$s0")
        );
        System.out.printf("$s1 = %d         $s2 = %d         $s3 = %d         $s4 = %d\n",
                registers.get("$s1"),
                registers.get("$s2"),
                registers.get("$s3"),
                registers.get("$s4")
        );
        System.out.printf("$s5 = %d         $s6 = %d         $s7 = %d         $t8 = %d\n",
                registers.get("$s5"),
                registers.get("$s6"),
                registers.get("$s7"),
                registers.get("$t8")
        );
        System.out.printf("$t9 = %d         $sp = %d         $ra = %d\n",
                registers.get("$t9"),
                registers.get("$sp"),
                registers.get("$ra")
        );
        System.out.print("\n");
    }

    // Show pipelineRegisters
    public void showPipelineRegisters() {
        System.out.println("\npc\tif/id\tid/exe\texe/mem\tmem/wb");
        System.out.printf("%d\t%s\t%s\t%s\t%s\n",
                pc,
                ifId.isEmpty ? "empty" : ifId.instruction,
                idEx.isEmpty ? "empty" : idEx.instruction,
                exMem.isEmpty ? "empty" : exMem.instruction,
                memWb.isEmpty ? "empty" : memWb.instruction
        );
    }

    // Step through one instruction in the program
    public void stepThrough(){
        stepThrough(1);
    }

    // Step through n instructions in the program
    public  void stepThrough(int numSteps){
        for (int i = 0; i < numSteps; i++){
            simulateOne();
        }

        System.out.printf("        %d clock cycles(s) executed\n", numSteps);
    }

    // Run until program ends
    public void runTheRest() {
        while (pc < instructions.size() && !ifId.isEmpty && !idEx.isEmpty && !exMem.isEmpty && !memWb.isEmpty) {
            simulateOne();
            if (pc >= instructions.size()) {
                System.out.println("End of instructions reached.");
                break;
            }
        }
    }


    // Display data memory between two locations (inclusive)
    public  void printMemory(int num1, int num2){
        System.out.print("\n");
        for(int i = num1; i <= num2; i++){
            System.out.printf("[%d] = %d\n", i, memory[i]);
        }
        System.out.print("\n");
    }

    // Clears registers, data memory, and sets pc back to 0


    // No quit

    /***  ALL SUPPORTED INSTRUCTION COMMANDS  ***/
    // and, or, add, addi, sll, sub, slt, beq, bne, lw, sw, j, jr, and jal


    /* ALL COMMANDS NEEDING THREE PARAMETERS */
    public void and(String arg1, String arg2, String arg3) {
        registers.put(arg1,
                registers.get(arg2) & registers.get(arg3)
        );
    }

    public void or(String arg1, String arg2, String arg3) {
        registers.put(arg1,
                registers.get(arg2) | registers.get(arg3)
        );
    }

    public void add(String arg1, String arg2, String arg3) {
        registers.put(arg1,
                registers.get(arg2) + registers.get(arg3)
        );

        // System.out.println("arg1 = " + arg1);
        // System.out.println(registers.get(arg2) + registers.get(arg3));
    }

    public void addi(String arg1, String arg2, String arg3) {
        registers.put(arg1,
                registers.get(arg2) + Integer.parseInt(arg3)
        );

    }

    public void sll(String arg1, String arg2, String arg3) {
        registers.put(arg1,
                registers.get(arg2) << Integer.parseInt(arg3)
        );
    }

    public void sub(String arg1, String arg2, String arg3) {
        registers.put(arg1,
                registers.get(arg2) - registers.get(arg3)
        );
    }

    public void slt(String arg1, String arg2, String arg3) {
        int zeroFlag = 0;

        if (registers.get(arg2) < registers.get(arg3)) {
            zeroFlag = 1;
        }

        registers.put(arg1,
                zeroFlag
        );

    }

    public void beq(String arg1, String arg2, String arg3) {
        if (registers.get(arg1) == registers.get(arg2)) {
            pc = labels.get(arg3);
        }
    }

    public void bne(String arg1, String arg2, String arg3) {
        if (registers.get(arg1) != registers.get(arg2)) {
            pc = labels.get(arg3);
        }
    }


    /* ALL COMMANDS NEEDING TWO PARAMETERS */
    public void lw(String arg1, String arg2) {
        int address = offSet(arg2);

        registers.put(arg1, memory[address]);
    }

    public void sw(String arg1, String arg2) {
        // System.out.println("Args: " + arg1 + " " + arg2);
        // System.out.printf("register %s: %d\nregister %s: %d\n\n",
        //             arg1, registers.get(arg1), arg2, registers.get(arg2));

        int address = offSet(arg2);

        memory[address] = registers.get(arg1);
        // System.out.println(address);
    }

    // Helper function to offset for data memory
    public int offSet(String arg1) {


        int signAt = arg1.indexOf('$');

        String result = arg1.substring(0, signAt - 1).replaceAll("[^0-9]", "");
        int num1 = Integer.parseInt(result);

        String register = arg1.substring(signAt, signAt + 3);
        int num2 = registers.get(register);

        return num1 + num2;

    }

    /* ALL COMMANDS WITH ONE ARGUMENT */
    public void j(String arg1) {
        pc = labels.get(arg1);
    }

    public void jr(String arg1) {
        pc = registers.get(arg1) - 1;
    }

    public void jal(String arg1) {
        registers.put("$ra", pc + 1);
        j(arg1);

    }

}

