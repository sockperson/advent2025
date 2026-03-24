import java.util.List;

public class Day1 {

    private static int dialLength = 100;

    private static List<String> lines = Utils.readLines("advent2025/input/day1.txt");

    public static int turn(String action, int currentPos) {
        boolean direction = action.charAt(0) == 'R';
        int steps = Integer.parseInt(action.substring(1));
        int newPos = currentPos + (direction ? steps : -steps);
        return Math.floorMod(newPos, dialLength);
    }

    // turn the dial and count how many times we pass by position 0
    public static int[] turn2(String action, int currentPos) {
        boolean direction = action.charAt(0) == 'R';
        int steps = Integer.parseInt(action.substring(1));
        // Right: count multiples of dialLength in (currentPos, currentPos+steps]
        //        = (currentPos + steps) / dialLength
        // Left:  reflect so going left from p becomes going right from (dialLength-p)%dialLength
        //        = ((dialLength - currentPos) % dialLength + steps) / dialLength
        int pos0Count = direction
            ? (currentPos + steps) / dialLength
            : ((dialLength - currentPos) % dialLength + steps) / dialLength;
        int newPos = Math.floorMod(currentPos + (direction ? steps : -steps), dialLength);
        return new int[]{newPos, pos0Count};
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");
        int out = 0;
        int currentPos = 50;
        for (String line : lines) {
            currentPos = turn(line, currentPos);
            if (currentPos == 0) {
                out++;
            }
        }
        System.out.println("Number of times reached 0: " + out);

        out = 0;
        currentPos = 50;
        for (String line : lines) {
            int[] result = turn2(line, currentPos);
            currentPos = result[0];
            out += result[1];
        }
        System.out.println("Number of times reached 0: " + out);
    }
}
