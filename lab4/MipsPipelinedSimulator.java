import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

class PipelineRegister {
    String instruction;
    int[] operands;
    int result;
    boolean isEmpty;

    PipelineRegister() {
        this.instruction = null;
        this.operands = new int[3]; // Assuming max three operands
        this.result = 0;
        this.isEmpty = true;
    }

    void clear() {
        instruction = null;
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

    // Pipeline stages
    private PipelineRegister ifId, idEx, exMem, memWb;

    public MipsPipelinedSimulator(Map<String, Integer> labels, List<String> instructions) {
        this.instructions = instructions;
        this.labels = labels;
        this.registers = new HashMap<>();
        this.memory = new int[8192]; // Simple memory
        this.pc = 0;
        this.cycleCounter = 0;
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
            running = updatePc();
        }
    }

    public void simulateOne() {
        cycleCounter++;
        writeBack();
        memoryAccess();
        execute();
        decode();
        fetch();
        updatePc();
    }

    private void fetch() {
        if (pc < instructions.size()) {
            ifId.instruction = instructions.get(pc);
            ifId.isEmpty = false;
        }
    }

    private void decode() {
        if (!ifId.isEmpty) {
            String[] parts = ifId.instruction.split("\\s+");
            String opcode = parts[0];
            idEx.instruction = opcode;
            idEx.operands = new int[parts.length - 1]; // Assuming all other parts are operands

            for (int i = 1; i < parts.length; i++) {
                String operand = parts[i].replace(",", "").trim();
                if (operand.startsWith("$")) {
                    idEx.operands[i - 1] = registers.get(operand);
                } else {
                    // Handle immediate values
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
            int[] operands = idEx.operands;

            // Assume operands[0] is the destination, operands[1] and operands[2] are the sources
            // for arithmetic operations, or source and immediate for I-type instructions
            switch (opcode) {
                case "add":
                    exMem.result = operands[1] + operands[2];
                    exMem.isEmpty = false;
                    break;
                case "sub":
                    exMem.result = operands[1] - operands[2];
                    exMem.isEmpty = false;
                    break;
                case "and":
                    exMem.result = operands[1] & operands[2];
                    exMem.isEmpty = false;
                    break;
                case "or":
                    exMem.result = operands[1] | operands[2];
                    exMem.isEmpty = false;
                    break;
                case "slt":
                    exMem.result = (operands[1] < operands[2]) ? 1 : 0;
                    exMem.isEmpty = false;
                    break;
                case "addi":
                    exMem.result = operands[1] + operands[0]; // operands[0] is treated as immediate here
                    exMem.isEmpty = false;
                    break;
                case "beq":
                    if (operands[1] == operands[2]) {
                        pc += operands[0]; // Using immediate value as a branch offset
                    }
                    break;
                case "bne":
                    if (operands[1] != operands[2]) {
                        pc += operands[0]; // Using immediate value as a branch offset
                    }
                    break;
                case "lw":
                case "sw":
                    // Storing the address calculation for memory access stage
                    exMem.operands = new int[]{operands[0], operands[1] + operands[2]}; // Register, Address
                    exMem.isEmpty = false;
                    break;
                default:
                    System.out.println("Unsupported operation");
            }
            idEx.clear();
        }
    }


    private int performOperation(String instruction, int[] operands) {
        // Handle the operation logic here
        return 0;
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

    private boolean updatePc() {
        pc++;
        return pc < instructions.size();
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
    public void showPipelineRegisters(){
        String[] pRegContents = {"empty", "empty", "empty", "empty"};
        System.out.print("\n");
        System.out.print("pc      if/id   id/exe  exe/mem mem/wb\n");
        System.out.printf("%d       %s     %s    %s   %s", 
            pc,
            pRegContents[0],
            pRegContents[1],
            pRegContents[2],
            pRegContents[3]
        );
	
        System.out.print("\n");
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
    public  void runTheRest(){
        while(pc < instructions.size() && !(ifId.isEmpty && 
                                            idEx.isEmpty && 
                                            exMem.isEmpty &&
                                            memWb.isEmpty)){
            simulateOne();
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
    public void clear(){
        for (String reg : registers.keySet()){
            registers.put(reg, 0);
        }

        // Clear memory
        memory = new int[8192];

        // Pc back to 0
        pc = 0;

        System.out.print("        Simulator reset\n\n");
    }

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

