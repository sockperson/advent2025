public class Day4 {

    private static char[][] sampleMap = Utils.readLinesAsCharArray("advent2025/input/day4_sample.txt");
    private static char[][] map = Utils.readLinesAsCharArray("advent2025/input/day4.txt");
    
    // check if a roll "@" is valid
    // a valid roll has less than 4 rolls in the adjacent 8 positions
    private static boolean isValidRoll(char[][] map, int row, int col) {
        if (map[row][col] != '@') {
            return false;
        }
        int rollCount = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                int newRow = row + i;
                int newCol = col + j;
                if (newRow >= 0 && newRow < map.length && newCol >= 0 && newCol < map[0].length) {
                    if (map[newRow][newCol] == '@') {
                        rollCount++;
                    }
                }
            }
        }
        return rollCount < 4;
    }

    private static int getValidRollCount(char[][] map) {
        int count = 0;
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                if (isValidRoll(map, i, j)) {
                    count++;
                }
            }
        }
        return count;
    }

    private static int getValidRollCount2(char[][] map) {
        int count = 0;
        char[][] currMap = map;
        char[][] nextMap = new char[map.length][map[0].length];
        
        while (true) {
            int currCount = 0;
            for (int i = 0; i < map.length; i++) {
                for (int j = 0; j < map[0].length; j++) {
                    if (currMap[i][j] == '@') {
                        if (isValidRoll(currMap, i, j)) {
                            nextMap[i][j] = '.';
                            currCount++;
                        } else {
                            nextMap[i][j] = '@';
                        }
                    } else {
                        nextMap[i][j] = '.';
                    }
                }
            }
            currMap = nextMap;
            nextMap = new char[map.length][map[0].length];
            count += currCount;
            if (currCount == 0) {
                break;
            }
        }
        return count;
    }
    
    public static void main(String[] args) throws Exception {
        // Sample
        System.out.println("Sample valid roll count: " + getValidRollCount(sampleMap));

        // Day4
        System.out.println("Day4 valid roll count: " + getValidRollCount(map));

        // Sample P2
        System.out.println("Sample valid roll count P2: " + getValidRollCount2(sampleMap));
        // Day4 P2
        System.out.println("Day4 valid roll count P2: " + getValidRollCount2(map));
    }
}
