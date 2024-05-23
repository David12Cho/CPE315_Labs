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
            idEx.instruction = ifId.instruction;
            // Parse instruction and fill operands, assuming instruction format is known
            ifId.clear();
        }
    }

    private void execute() {
        if (!idEx.isEmpty) {
            // Execute the instruction based on type, e.g., ADD, SUB
            // Assume that the instruction and operands are parsed and available
            exMem.result = performOperation(idEx.instruction, idEx.operands);
            exMem.isEmpty = false;
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

