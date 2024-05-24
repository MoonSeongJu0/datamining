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
        Integer mu = 4;
        Double epsilon = 0.5;

        if (args.length >= 2) {
            boolean isInteger = false;
            boolean isDouble = false;
            try {
                mu = Integer.parseInt(args[1]);
                isInteger = true;
                if (args.length == 2){
                    System.out.println("Estimated eps : 0.5");
                }
            } catch (NumberFormatException e) {
                try {
                    epsilon = Double.parseDouble(args[1]);
                    isDouble = true;
                    if (args.length == 2){
                        System.out.println("Estimated eps : 4");
                    }
                } catch (NumberFormatException ex) {
                    System.out.println("Estimated MinPts : 4");
                    System.out.println("Estimated eps : 0.5");
                }
            }
            if (args.length >= 3) {
                if (!isInteger && isDouble){
                    try {
                        mu = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        System.out.println("Estimated MinPts : 4");
                    }
                }
                else if (isInteger && !isDouble) {
                    try {
                        epsilon = Double.parseDouble(args[2]);
                    } catch (NumberFormatException e) {
                        System.out.println("Estimated eps : 0.5");
                    }
                }
            }
        } else {
            System.out.println("Estimated MinPts : 4");
            System.out.println("Estimated eps : 0.5");
        }
        
        //print DBSCAN cluster
        Map<String, Integer> clusters = DBSCAN(data, epsilon, mu);
        printclusters(clusters);
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
        Map<String, Integer> ClusterID = new HashMap<>();
        Integer Cluster_ID = 1;
        for (String point : data.keySet()) {
            if (ClusterID.get(point) == null){
                if (ExpandCluster(data, point, ClusterID, epsilon, mu, Cluster_ID)){
                    Cluster_ID++;
                }
            }
        }
        return ClusterID;
    }

    private static boolean ExpandCluster(HashMap<String, List<Double>> data, String point, Map<String, Integer> ClusterID, Double epsilon, int mu, int Cluster_ID) {
        Queue<String> seeds = new LinkedList<>();
        for (String q : data.keySet()) {
            if (distance(data.get(point), data.get(q)) <= epsilon) {
                seeds.add(q);
            }
        }
        if (seeds.size() < mu ){
            ClusterID.put(point, 0);
            return false;
        }
        else{
            while (!seeds.isEmpty()) {
                String currentP = seeds.poll();
                ClusterID.put(currentP, Cluster_ID);
                Queue<String> result = new LinkedList<>();
                for (String q : data.keySet()) {
                    if (distance(data.get(currentP), data.get(q)) <= epsilon) {
                        result.add(q);
                    }
                }
                if (result.size() >= mu){
                    while (!result.isEmpty()) {
                        String resultP = result.poll();
                        if (ClusterID.get(resultP) == null || ClusterID.get(resultP).equals(0)){
                            if (ClusterID.get(resultP) == null){
                                seeds.add(resultP);
                            }
                            ClusterID.put(resultP, Cluster_ID);
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
    
        for (Map.Entry<Integer, List<String>> entry : clusterMap.entrySet()) {
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
            for (String point : points) {
                System.out.print(point + " ");
            }
            System.out.println();
        }
    }
}