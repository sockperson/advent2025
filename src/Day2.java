import java.util.List;

public class Day2 {

    private static List<String> lines = Utils.readLines("advent2025/input/day2.txt");

    // check if the number is made of some sequence of digits repeated twice
    private static boolean isInvalid(long number) {
        String stringNumber = Long.toString(number);
        if (stringNumber.length() % 2 != 0) {
            return false;
        }
        String firstHalf = stringNumber.substring(0, stringNumber.length() / 2);
        String secondHalf = stringNumber.substring(stringNumber.length() / 2);
        return firstHalf.equals(secondHalf);
    }

    // check if the number is made of some sequence of digits repeated 2+ times
    private static boolean isInvalid2(long number) {
        return Long.toString(number).matches("^(.+)\\1+$");
    }

    private static long checkRange(long start, long end) {
        long sum = 0;
        for (long i = start; i <= end; i++) {
            if (isInvalid(i)) {
                sum += i;
            }
        }
        return sum;
    }
    
    public static void main(String[] args) throws Exception {
        long out = 0;
        String line = lines.get(0);
        String[] ranges = line.split(",");
        for (String range : ranges) {
            String[] parts = range.split("-");
            long start = Long.parseLong(parts[0]);
            long end = Long.parseLong(parts[1]);
            out += checkRange(start, end);
        }
        System.out.println("Invalid number sum: " + out);

        out = 0;
        for (String range : ranges) {
            String[] parts = range.split("-");
            long start = Long.parseLong(parts[0]);
            long end = Long.parseLong(parts[1]);
            for (long i = start; i <= end; i++) {
                if (isInvalid2(i)) {
                    out += i;
                }
            }
        }
        System.out.println("Invalid number sum: " + out);
    }
}
