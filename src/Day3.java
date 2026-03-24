import java.util.List;

public class Day3 {

    private static List<String> lines_sample = Utils.readLines("advent2025/input/day3_sample.txt");
    private static List<String> lines = Utils.readLines("advent2025/input/day3.txt");

    // find the biggest 2 digit number that can be formed by concatenating
    // two digits somewhere in the line
    // ex: "1491282" --> 98
    private static int maxVoltage(String line) {
        // first find the highest digit in the line except in the last index
        String mostOfLine = line.substring(0, line.length() - 1);
        int highest = highestNumber(mostOfLine);
        int indexOfHighest = mostOfLine.indexOf(Integer.toString(highest));
        // then find the highest digit in the substring from the
        // index of the highest digit + 1 to the end of the line
        String restOfLine = line.substring(indexOfHighest + 1);
        int secondHighest = highestNumber(restOfLine);
        return highest * 10 + secondHighest;
    }

    // find the biggest 12 digit number that can be formed by concatenating
    // 12 digits somewhere in the line
    private static long maxVoltage2(String line) {
        int[] digits = new int[12];
        int nextIndex = 0;
        for (int i = 11; i >= 0; i--) {
            String substr = line.substring(nextIndex, line.length() - i);
            int bigDigit = highestNumber(substr);
            digits[11 - i] = bigDigit;
            nextIndex = nextIndex + substr.indexOf(Integer.toString(bigDigit)) + 1;
        }
        // make new int from the digits
        StringBuilder sb = new StringBuilder();
        for (int digit : digits) {
            sb.append(digit);
        }
        return Long.parseLong(sb.toString());
    }

    // find highest digit in string
    private static int highestNumber(String sequence) {
        for (int i = 9; i >= 0; i--) {
            if (sequence.contains(Integer.toString(i))) {
                return i;
            }
        }
        return 0;
    }
    
    public static void main(String[] args) throws Exception {
        // Sample
        int out = 0;
        for (String line : lines_sample) {
            out += maxVoltage(line);
        }
        System.out.println("Sample max voltage sum: " + out);

        // Day3
        out = 0;
        for (String line : lines) {
            out += maxVoltage(line);
        }
        System.out.println("Day3 max voltage sum: " + out);

        // Sample P2
        long out2 = 0;
        for (String line : lines_sample) {
            out2 += maxVoltage2(line);
        }
        System.out.println("Sample max voltage sum: " + out2);
        
        // Day3 P2
        out2 = 0;
        for (String line : lines) {
            out2 += maxVoltage2(line);
        }
        System.out.println("Day3 max voltage sum: " + out2);
    }
}
