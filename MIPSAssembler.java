import java.util.*;
import java.io.*;

//Still need to do:
//Complete a preprocesser function that gets rid of whitelines and newlines
//make functions for and, xor, or, etc.

public class MIPSAssembler {
    // Maps to hold the binary codes for opcodes, registers, and addresses of labels.
    public static Map<String, String> opcodes = new HashMap<>();
    public static Map<String, Integer> labelAddresses = new HashMap<>();
    public static Map<String, String> registers = new HashMap<>();
    public static List<String> instructions = new ArrayList<>();

    // Static initializer to populate the opcode and register maps.
    static {
        //maps opcodes and registers
        registers.put("$0", "00000");
        registers.put("$zero", "00000");
        registers.put("$at", "00001");
        registers.put("$v0", "00010");
        registers.put("$v1", "00011");
        registers.put("$a0", "00100");
        registers.put("$a1", "00101");
        registers.put("$a2", "00110");
        registers.put("$a3", "00111");
        registers.put("$t0", "01000");
        registers.put("$t1", "01001");
        registers.put("$t2", "01010");
        registers.put("$t3", "01011");
        registers.put("$t4", "01100");
        registers.put("$t5", "01101");
        registers.put("$t6", "01110");
        registers.put("$t7", "01111");
        registers.put("$s0", "10000");
        registers.put("$s1", "10001");
        registers.put("$s2", "10010");
        registers.put("$s3", "10011");
        registers.put("$s4", "10100");
        registers.put("$s5", "10101");
        registers.put("$s6", "10110");
        registers.put("$s7", "10111");
        registers.put("$t8", "11000");
        registers.put("$t9", "11001");
        registers.put("$k0", "11010");
        registers.put("$k1", "11011");
        registers.put("$gp", "11100");
        registers.put("$sp", "11101");
        registers.put("$fp", "11110");
        registers.put("$ra", "11111");

        opcodes.put("add", "000000");
        opcodes.put("addi", "001000");
        opcodes.put("sll", "000000");
        opcodes.put("sub", "000000");
        opcodes.put("slt", "000000");
        opcodes.put("beq", "000100");
        opcodes.put("bne", "000101");
        opcodes.put("lw", "100011");
        opcodes.put("sw", "101011");
        opcodes.put("j", "000010");
        opcodes.put("jr", "000000");
        opcodes.put("jal", "000011");
        opcodes.put("000000", "R-type");

        opcodes.put("and", "what");
        opcodes.put("or", "what");

    }
    // Entry point of the program
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("File missing");
            return;
        }
        String file = args[0];
        firstLoop(file);
        secondLoop();
    }
    // First pass of the assembler: Reads the assembly file and extracts labels and instructions
    public static void firstLoop(String filename) throws IOException {
        Scanner scanner = new Scanner(new File(filename));
        int address = 0;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            // Skip empty lines and comments
            if (line.isEmpty()){
                continue;
            }
            if(line.startsWith("#")){
                continue;
            }
            // Remove inline comments and process labels
            if (line.contains("#")) {
                line = line.substring(0, line.indexOf("#")).trim();
            }
            if (line.contains(":")) {
                String label = line.substring(0, line.indexOf(":")).trim();
                labelAddresses.put(label, address);
                line = line.substring(line.indexOf(":") + 1).trim();
                if (line.isEmpty()){
                    continue;
                }
            }
            instructions.add(line);
            address+=1;
        }
        scanner.close();
    }
    // Second pass of the assembler: Converts each instruction into machine code
    public static void secondLoop(){
        for (int index = 0; index < instructions.size(); index++) {
            boolean invalidCatch = convertInstruction(instructions.get(index), index);
            if(invalidCatch){
                break;
            }
        }
    }
    // Converts a single instruction into machine code based on its type and operands
    public static boolean convertInstruction(String line, int instructionIndex) {
        List<String> parts = new ArrayList<>(Arrays.asList(line.split("[,\\s]+")));
        if (parts.size() < 1) return false; // Ignore blank or incomplete lines

        for(int i = 0; i < parts.size(); i++){
            int newIndex = i;
            // System.out.println(parts);
            int indexOfDollar = parts.get(newIndex).indexOf('$');
            if (indexOfDollar > 0){
                int indexOfLeft = parts.get(newIndex).indexOf('(');
                if((indexOfLeft >= indexOfDollar) || (indexOfLeft == -1)){
                    String[] subPart = parts.get(newIndex).split("\\$", 2);
                    // System.out.println(parts);
                    // System.out.print("[");
                    // for(String x : subPart){
                    //     System.out.print(x + ", ");
                    // }
                    // System.out.println("]");
                    parts.set(newIndex, "$" + subPart[1]);
                    parts.add(newIndex, subPart[0]);
                    i++;
                }
            }   
        }



        String opcode = parts.get(0);
        if (opcodes.containsKey(opcode)) {
            String[] partsAsArray = parts.toArray(new String[parts.size()]);
            switch (opcode) {
                // Process R-type instructions
                case "add":
                case "sub":
                case "slt":
                case "and":
                case "or":
                    R_instruction(opcode, parts.toArray(partsAsArray));
                    break;
                case "addi":
                case "lw":
                case "sw":
                    // I-type instructions
                    I_instruction(opcode, partsAsArray);
                    break;
                case "beq":
                case "bne":
                    // Branch instructions
                    Branch_instruction(opcode, partsAsArray, instructionIndex);
                    break;
                case "j":
                case "jal":
                    // J-type instructions
                    J_instruction(opcode, partsAsArray);
                    break;
                case "jr":
                    // JR instruction
                    JR_Instruction(opcode, partsAsArray);
                    break;
                case "sll":
                    // SLL instruction
                    SLL_instruction(opcode, partsAsArray);
                    break;
                default:
                    System.out.println("unsupported instruction: " + line);
                    return true;
                    // break;
            }
        } else {
            System.out.println("invalid instruction: " + opcode);
            return true;
        }

        return false;
    }

    // Method implementations for R-type, I-type, J-type, etc.
    public static void R_instruction(String opcode, String[] parts) {
        String funct;
        switch (opcode) {
            case "add":
                funct = "100000";
                break;
            case "sub":
                funct = "100010";
                break;
            case "slt":
                funct = "101010";
                break;
            case "sll":
                funct = "000000";
                break;
            case "or":
                funct = "100101";
                break;
            case "and":
                funct = "100100";
                break;

            default:
                funct = "";
        }
        //all R-type instructions use opcode "000000"
        System.out.println("000000" + " " + convertToBinary(parts[2]) + " " + convertToBinary(parts[3])
                + " " + convertToBinary(parts[1]) + " 00000 " + funct);
    }

    public static void I_instruction(String opcode, String[] parts) {
        if (opcode.equals("lw") || opcode.equals("sw")) {
            String offsetPart = parts[2];
            String[] offsetParts = offsetPart.split("\\(");
            String offset = offsetParts[0].trim();
            String rs = offsetParts[1].replace(")", "").trim();
            rs = convertToBinary(rs);
            System.out.println(opcodes.get(opcode) + " " + rs + " " + convertToBinary(parts[1]) + " " + convert_immediate_To_Binary(offset, 16));
        } else {
            System.out.println(opcodes.get(opcode) + " " + convertToBinary(parts[2]) + " "
                    + convertToBinary(parts[1]) + " " + convert_immediate_To_Binary(parts[3], 16));
        }
    }
    public static void Branch_instruction(String opcode, String[] parts, int index) {
        int offset = labelAddresses.get(parts[3]) - index - 1;
        String offsetBin = convert_immediate_To_Binary(String.valueOf(offset), 16);
        System.out.println(opcodes.get(opcode) + " " + convertToBinary(parts[1]) + " "
                + convertToBinary(parts[2]) + " " + offsetBin);
    }

    public static void J_instruction(String opcode, String[] parts){
        System.out.println(opcodes.get(opcode) + " " +
                convert_immediate_To_Binary(String.valueOf(labelAddresses.get(parts[1])), 26));
    }

    public static void JR_Instruction(String opcode, String[] parts) {
        System.out.println(opcodes.get(opcode) + " " + convertToBinary(parts[1]) + " 000000000000000 001000");
    }
    public static void SLL_instruction(String opcode, String[] parts) {
        System.out.println(opcodes.get(opcode) + " 00000 " +
                convertToBinary(parts[2]) + " " + convertToBinary(parts[1]) + " " +
                convert_immediate_To_Binary(parts[3], 5) + " 000000");
    }

    public static String convertToBinary(String reg) {
        reg = reg.trim();
        if (registers.containsKey(reg)) {
            return registers.get(reg);
        }
        return null;
    }

    public static String convert_immediate_To_Binary(String imm, int bits) {
        return String.format("%" + bits + "s",
                Integer.toBinaryString(0xFFFFF & Integer.parseInt(imm.trim()))).replace
                (' ', '0');
    }

}









