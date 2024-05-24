import java.io.*;
import java.util.*;

public class A2_G9_t1 {
    public static void main(String[] args) {
        if (args.length < 1 || args.length > 2) {
            System.out.println("Error! Usage: java A2_G1_t1 [input file] [k_value]");
            return;
        }

        String inputFile = args[0];
        int k_value = 15;

        if (args.length == 2) {
            try {
                k_value = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid k value, using default k=15");
            }
        } else {
            System.out.println("Estimated k: 15");
        }

        HashMap<String, List<Double>> data = read_csv(inputFile);
        List<List<Double>> centroids = centroid_selection(data, k_value);
        Map<Integer, List<String>> clusters = kmeans_pp(data, centroids);
        printclusters(clusters, "clusters_output.txt");
    }

    private static HashMap<String, List<Double>> read_csv(String filePath) {
        HashMap<String, List<Double>> data = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                String pointLabel = tokens[0];
                List<Double> values = new ArrayList<>();
                for (int i = 1; i < tokens.length; i++) {
                    values.add(Double.parseDouble(tokens[i]));
                }
                data.put(pointLabel, values);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    private static double cal_dist(List<Double> point1, List<Double> point2) {
        double sum = 0.0;
        for (int i = 0; i < point1.size(); i++) {
            sum += Math.pow(point1.get(i) - point2.get(i), 2);
        }
        return Math.sqrt(sum);
    }

    private static List<List<Double>> centroid_selection(HashMap<String, List<Double>> data, int k) {
        List<List<Double>> centroids = new ArrayList<>();
        List<String> points = new ArrayList<>(data.keySet());
        Random rand = new Random();

        // Choose the first centroid randomly
        centroids.add(data.get(points.get(rand.nextInt(points.size()))));

        while (centroids.size() < k) {
            double[] distances = new double[points.size()];
            double sum = 0.0;

            for (int i = 0; i < points.size(); i++) {
                List<Double> point = data.get(points.get(i));
                double minDist = Double.MAX_VALUE;
                for (List<Double> centroid : centroids) {
                    double dist = cal_dist(point, centroid);
                    if (dist < minDist) {
                        minDist = dist;
                    }
                }
                distances[i] = minDist;
                sum += minDist;
            }

            double r = rand.nextDouble() * sum;
            sum = 0.0;
            for (int i = 0; i < points.size(); i++) {
                sum += distances[i];
                if (sum >= r) {
                    centroids.add(data.get(points.get(i)));
                    break;
                }
            }
        }

        return centroids;
    }

    private static Map<Integer, List<String>> kmeans_pp(HashMap<String, List<Double>> data, List<List<Double>> centroids) {
        List<String> points = new ArrayList<>(data.keySet());
        int k = centroids.size();
        Map<Integer, List<String>> clusters = new HashMap<>();
        Map<String, Integer> assignments = new HashMap<>();
        boolean changes = true;

        while (changes) {
            changes = false;
            clusters.clear();
            for (int i = 0; i < k; i++) {
                clusters.put(i, new ArrayList<>());
            }

            for (String pointLabel : points) {
                List<Double> point = data.get(pointLabel);
                int closest = -1;
                double minDist = Double.MAX_VALUE;
                for (int i = 0; i < k; i++) {
                    double dist = cal_dist(point, centroids.get(i));
                    if (dist < minDist) {
                        minDist = dist;
                        closest = i;
                    }
                }
                clusters.get(closest).add(pointLabel);
                if (!assignments.containsKey(pointLabel) || assignments.get(pointLabel) != closest) {
                    assignments.put(pointLabel, closest);
                    changes = true;
                }
            }

            // Update centroids
            for (int i = 0; i < k; i++) {
                List<Double> newCentroid = new ArrayList<>(Collections.nCopies(centroids.get(0).size(), 0.0));
                List<String> clusterPoints = clusters.get(i);
                if (clusterPoints.size() > 0) {
                    for (String pointLabel : clusterPoints) {
                        List<Double> point = data.get(pointLabel);
                        for (int j = 0; j < newCentroid.size(); j++) {
                            newCentroid.set(j, newCentroid.get(j) + point.get(j));
                        }
                    }
                    for (int j = 0; j < newCentroid.size(); j++) {
                        newCentroid.set(j, newCentroid.get(j) / clusterPoints.size());
                    }
                    centroids.set(i, newCentroid);
                }
            }
        }

        return clusters;
    }

    private static void printclusters(Map<Integer, List<String>> clusters, String outputFile) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            for (Map.Entry<Integer, List<String>> entry : clusters.entrySet()) {
                writer.write("Cluster #" + (entry.getKey() + 1) + " => ");
                List<String> cluster_members = entry.getValue();
                Collections.sort(cluster_members);
                for (int i = 0; i < cluster_members.size(); i++) {
                    writer.write(cluster_members.get(i));
                    if (i < cluster_members.size() - 1) {
                        writer.write(" ");
                    }
                }
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
