import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Queue;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;


public class A2_G9_t2 {
    public static void main(String[] args) {
        //input csv
        HashMap<String, List<Double>> data = read_csv(args[0]);

        //input eps and minpts
        Integer dim = data.values().iterator().next().size();
        Integer mu = 2 * dim;
        Double epsilon = 0.0;

        if (args.length >= 2) {
            boolean isInteger = false;
            boolean isDouble = false;
            try {
                mu = Integer.parseInt(args[1]);
                isInteger = true;
                if (args.length == 2){
                    epsilon = Estimated_epsilon(data, mu);
                    System.out.println("Estimated eps : " + epsilon);
                }
            } catch (NumberFormatException e) {
                try {
                    epsilon = Double.parseDouble(args[1]);
                    isDouble = true;
                    if (args.length == 2){
                        System.out.println("Estimated MinPts : " + mu);
                    }
                } catch (NumberFormatException ex) {
                    System.out.println("Estimated MinPts : " + mu);
                    epsilon = Estimated_epsilon(data, mu);
                    System.out.println("Estimated eps : " + epsilon);
                }
            }
            if (args.length >= 3) {
                if (!isInteger && isDouble){
                    try {
                        mu = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        System.out.println("Estimated MinPts : " + mu);
                    }
                }
                else if (isInteger && !isDouble) {
                    try {
                        epsilon = Double.parseDouble(args[2]);
                    } catch (NumberFormatException e) {
                        epsilon = Estimated_epsilon(data, mu);
                        System.out.println("Estimated eps : " + epsilon);
                    }
                }
            }
        } else {
            System.out.println("Estimated MinPts : " + mu);
            epsilon = Estimated_epsilon(data, mu);
            System.out.println("Estimated eps : " + epsilon);
        }
        
        //print DBSCAN cluster
        Map<String, Integer> clusters = DBSCAN(data, epsilon, mu);
        printclusters(clusters);
    }

    private static Double Estimated_epsilon (HashMap<String, List<Double>> data, Integer mu){
        List<Double> k_distances = new ArrayList<>();
        for (String point : data.keySet()) {
            List<Double> distances = new ArrayList<>();
            for (String otherPoint : data.keySet()) {
                if (!point.equals(otherPoint)) {
                    distances.add(distance(data.get(point), data.get(otherPoint)));
                }
            }
            Collections.sort(distances);
            k_distances.add(distances.get(mu - 1));
        }
        Collections.sort(k_distances, Collections.reverseOrder());
        Double estimated_eps = find_optimal_eps(k_distances);
        return estimated_eps;
    }

    private static Double find_optimal_eps(List<Double> k_distances) {
        int data_size = k_distances.size();
        if (data_size <= 2){
            return k_distances.get(0);
        }

        double maxSecondDerivative = Double.MIN_VALUE;
        double optimalEps = 0.0;

        for (int i = 1; i < data_size - 1; i++) {
            double secondDerivative = k_distances.get(i - 1) - 2 * k_distances.get(i) + k_distances.get(i + 1);
            if (secondDerivative > maxSecondDerivative) {
                maxSecondDerivative = secondDerivative;
                optimalEps = k_distances.get(i);
            }
        }
    return optimalEps;
    }


    private static HashMap<String, List<Double>> read_csv(String filePath){
        HashMap<String, List<Double>> data =  new HashMap<String, List<Double>>();
        try {
            File file = new File(filePath);
            BufferedReader read_data = new BufferedReader(new FileReader(file));
            String line;
            while ((line = read_data.readLine()) != null ) {
                String[] lines = line.split(",");
                String point = lines[0];
                List<Double> values = new ArrayList<Double>();
                for (int i=1 ; i<3; i++){
                    values.add(Double.parseDouble(lines[i]));
                }
                data.put(point, values);
            }
            read_data.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    private static Double distance (List<Double> point1, List<Double> point2) {
        Double sum = 0.0;
        for (int i = 0; i < point1.size(); i++) {
            sum += Math.pow(point1.get(i) - point2.get(i), 2);
        }
        return Math.sqrt(sum);
    }

    private static Map<String, Integer> DBSCAN(HashMap<String, List<Double>> data, Double epsilon, int mu){
        Map<String, Integer> ClusterMap = new HashMap<>();
        Integer ClusterID = 1;
        for (String point : data.keySet()) {
            if (ClusterMap.get(point) == null){
                if (ExpandCluster(data, point, ClusterMap, epsilon, mu, ClusterID)){
                    ClusterID++;
                }
            }
        }
        return ClusterMap;
    }

    private static boolean ExpandCluster(HashMap<String, List<Double>> data, String point, Map<String, Integer> ClusterMap, Double epsilon, int mu, int ClusterID) {
        Queue<String> seeds = new LinkedList<>();
        for (String q : data.keySet()) {
            if (distance(data.get(point), data.get(q)) <= epsilon) {
                seeds.add(q);
            }
        }
        if (seeds.size() < mu ){
            ClusterMap.put(point, 0);
            return false;
        }
        else{
            while (!seeds.isEmpty()) {
                String currentP = seeds.poll();
                ClusterMap.put(currentP, ClusterID);
                Queue<String> result = new LinkedList<>();
                for (String q : data.keySet()) {
                    if (distance(data.get(currentP), data.get(q)) <= epsilon) {
                        result.add(q);
                    }
                }
                if (result.size() >= mu){
                    while (!result.isEmpty()) {
                        String resultP = result.poll();
                        if (ClusterMap.get(resultP) == null || ClusterMap.get(resultP).equals(0)){
                            if (ClusterMap.get(resultP) == null){
                                seeds.add(resultP);
                            }
                            ClusterMap.put(resultP, ClusterID);
                        }
                    }
                }
            }
            return true;
        }
    }

    private static void printclusters(Map<String, Integer> clusters) {
        TreeMap<Integer, List<String>> clusterMap = new TreeMap<>();
        int noise_count = 0;
    
        // Group points by cluster ID
        for (Map.Entry<String, Integer> entry : clusters.entrySet()) {
            String point = entry.getKey();
            int clusterID = entry.getValue();
            if (clusterID == 0){
                noise_count++;
            }
            else {
                clusterMap.putIfAbsent(clusterID, new ArrayList<>());
                clusterMap.get(clusterID).add(point);
            }
        }
    
        System.out.println("Number of noise: " + noise_count);
        System.out.println("Number of clusters: " + (clusterMap.size()));
        
        int c = 0;
        for (Map.Entry<Integer, List<String>> entry : clusterMap.entrySet()) {
            c++;
            int clusterID = entry.getKey();
            List<String> points = entry.getValue();
            
            // Sort points within cluster
            for (int i = 0; i < points.size() - 1; i++) {
                for (int j = i + 1; j < points.size(); j++) {
                    int p1 = Integer.parseInt(points.get(i).substring(1));
                    int p2 = Integer.parseInt(points.get(j).substring(1));
                    if (p1 > p2) {
                        String temp = points.get(i);
                        points.set(i, points.get(j));
                        points.set(j, temp);
                    }
                }
            }
    
            System.out.print("Cluster #" + clusterID + " => ");
            for (int i = 0; i < points.size(); i++) {
                System.out.print(points.get(i));
                if (i < points.size() - 1) {
                    System.out.print(" ");
                }
            }
            if (c != clusterMap.size()){
                System.out.println();
            }
        }
    }
}
