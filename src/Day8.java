import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Day8 {

    private static int[] parent;
    private static int[] size;

    private static int find(int x) {
        while (parent[x] != x) {
            parent[x] = parent[parent[x]];
            x = parent[x];
        }
        return x;
    }

    private static void union(int a, int b) {
        int ra = find(a);
        int rb = find(b);
        if (ra == rb) return;
        if (size[ra] < size[rb]) { int t = ra; ra = rb; rb = t; }
        parent[rb] = ra;
        size[ra] += size[rb];
    }

    private static long[][] parsePoints(List<String> lines) {
        int n = lines.size();
        long[][] points = new long[n][3];
        for (int i = 0; i < n; i++) {
            String[] parts = lines.get(i).trim().split(",");
            points[i][0] = Long.parseLong(parts[0].trim());
            points[i][1] = Long.parseLong(parts[1].trim());
            points[i][2] = Long.parseLong(parts[2].trim());
        }
        return points;
    }

    private static List<long[]> sortedPairsByDistance(long[][] points) {
        int n = points.length;
        List<long[]> pairs = new ArrayList<>(); // {distSquared, i, j}
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                long dx = points[i][0] - points[j][0];
                long dy = points[i][1] - points[j][1];
                long dz = points[i][2] - points[j][2];
                long distSq = dx * dx + dy * dy + dz * dz;
                pairs.add(new long[]{distSq, i, j});
            }
        }
        pairs.sort((a, b) -> Long.compare(a[0], b[0]));
        return pairs;
    }

    private static void initDsu(int n) {
        parent = new int[n];
        size = new int[n];
        for (int i = 0; i < n; i++) { parent[i] = i; size[i] = 1; }
    }

    private static long topThreeProduct(List<String> lines, int connectionsToMake) {
        long[][] points = parsePoints(lines);
        int n = points.length;
        List<long[]> pairs = sortedPairsByDistance(points);
        initDsu(n);

        int limit = Math.min(connectionsToMake, pairs.size());
        for (int k = 0; k < limit; k++) {
            long[] pair = pairs.get(k);
            union((int) pair[1], (int) pair[2]);
        }

        int[] circuitSizes = new int[n];
        for (int i = 0; i < n; i++) {
            circuitSizes[find(i)]++;
        }
        Arrays.sort(circuitSizes);

        long product = 1;
        for (int i = 0; i < 3; i++) {
            product *= circuitSizes[n - 1 - i];
        }
        return product;
    }

    private static long finalConnectionProduct(List<String> lines) {
        long[][] points = parsePoints(lines);
        int n = points.length;
        List<long[]> pairs = sortedPairsByDistance(points);
        initDsu(n);

        int componentsRemaining = n;
        for (long[] pair : pairs) {
            int a = (int) pair[1];
            int b = (int) pair[2];
            if (find(a) == find(b)) continue;
            union(a, b);
            componentsRemaining--;
            if (componentsRemaining == 1) {
                return points[a][0] * points[b][0];
            }
        }
        throw new IllegalStateException("Boxes never formed a single circuit");
    }

    public static void main(String[] args) throws Exception {
        List<String> sampleLines = Utils.readLines("advent2025/input/day8_sample.txt");
        List<String> lines = Utils.readLines("advent2025/input/day8.txt");

        long sampleResult = topThreeProduct(sampleLines, 10);
        System.out.println("Sample top-three product: " + sampleResult);

        long result = topThreeProduct(lines, 1000);
        System.out.println("Top-three product: " + result);

        long sampleFinal = finalConnectionProduct(sampleLines);
        System.out.println("Sample final connection product: " + sampleFinal);

        long finalProduct = finalConnectionProduct(lines);
        System.out.println("Final connection product: " + finalProduct);
    }
}
