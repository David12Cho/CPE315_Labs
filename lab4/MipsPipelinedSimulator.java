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
    private List<String> instructions;
    private Map<String, Integer> registers;
    private int[] memory;
    private int pc; // Program counter

    // Pipeline stages
    private PipelineRegister ifId, idEx, exMem, memWb;

    public MipsPipelinedSimulator(List<String> instructions) {
        this.instructions = instructions;
        this.registers = new HashMap<>();
        this.memory = new int[1024]; // Simple memory
        this.pc = 0;
        initializeRegisters();
        initializePipeline();
    }

    private void initializeRegisters() {
        // Initialize registers $0 to $31, assuming $0 is always 0
        for (int i = 0; i < 32; i++) {
            registers.put("$" + i, 0);
        }
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
            writeBack();
            memoryAccess();
            execute();
            decode();
            fetch();
            running = updatePc();
        }
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



    public static void main(String[] args) {
        // Example usage:
        List<String> testInstructions = new ArrayList<>();
        testInstructions.add("add $1, $2, $3");
        testInstructions.add("sub $1, $2, $3");

        MipsPipelinedSimulator simulator = new MipsPipelinedSimulator(testInstructions);
        simulator.simulate();
    }
}

