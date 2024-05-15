import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.HashMap;
import java.util.Collections;
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
        Map<Integer, List<String>> clusters = DBSCAN(data, epsilon, mu);
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
                for (int i = 1 ; i<lines.length; i++){
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
        for (int i = 0; i < 2; i++) {
            sum += Math.pow(point1.get(i) - point2.get(i), 2);
        }
        return Math.sqrt(sum);
    }

    private static void printclusters(Map<Integer, List<String>> clusters) {
        for (Map.Entry<Integer, List<String>> entry : clusters.entrySet()) {
            System.out.print("Cluster #" + entry.getKey() + " => ");
            List<String> cluster_members = entry.getValue();
            Collections.sort(cluster_members);
            for (int i = 0; i < cluster_members.size(); i++) {
                System.out.print(cluster_members.get(i));
                if (i < cluster_members.size() - 1) {
                    System.out.print(" ");
                }
            }
            System.out.println();
        }
    }

    private static Map<Integer, List<String>> DBSCAN(HashMap<String, List<Double>> data, Double epsilon, int mu){
    Map<Integer, List<String>> clusters = new HashMap<>();

    Map<String, String> point_labels = new HashMap<>();
    int cluster_count = 0;
    int noise_count = 0;
        //count the number of neighbor
        for (String point : data.keySet()) {
            int neighbor_count = 0;
            for (String neighbor : data.keySet()) {
                if (distance(data.get(point), data.get(neighbor)) <= epsilon) {
                    neighbor_count++;
                }
            }
            //if the number of neighbor of the point is equal or larger than mu, then the point is core point.
            if (neighbor_count >= mu) {
                point_labels.put (point, "core_point");
                // if the point s.t. neighbor of a point is border point.
                for (String neighbor : data.keySet()) {
                    if (distance(data.get(point), data.get(neighbor)) <= epsilon ){
                        if (!point_labels.containsKey(neighbor)){
                            point_labels.put(neighbor, "border_point");
                        }
                        else if(point_labels.get(neighbor).equals("noise_point")) {
                            point_labels.put(neighbor, "border_point");
                            noise_count--;
                        }
                    }
                }
            } 
            //if the number of neighbor of the point is smaller than mu, and if the point is not border point, then the point is noise point.
            else {
                if (!point_labels.containsKey(point)){
                    point_labels.put(point, "noise_point");
                    noise_count++;
                }
            }
        }
        //maked point_labels

        //make clusters
        for (String core_point : data.keySet()) {
            if (point_labels.get(core_point).equals("core_point")) {
                cluster_count++;
                List<String> cluster_members = density_connect(data, point_labels, core_point, epsilon);
                clusters.put(cluster_count, cluster_members);
            }
        }


        System.out.println("Number of clusters: " + cluster_count);
        System.out.println("Number of noise : " + noise_count);

        return clusters;
    }

    private static List<String> density_connect(HashMap<String, List<Double>> data, Map<String, String> point_labels, String core_point, Double epsilon) {
        List<String> cluster_members = new ArrayList<>();
        Queue<String> q = new LinkedList<>();
        q.add(core_point);

        while (!q.isEmpty()) {
            String point = q.poll();
            for (String neighbor : data.keySet()) {
                if (distance(data.get(point), data.get(neighbor)) <= epsilon){
                    if ((point_labels.get(neighbor).equals("core_point"))){
                        q.add(neighbor);
                        cluster_members.add(neighbor);
                        point_labels.put(neighbor, "visited");
                    }
                    else if ((point_labels.get(neighbor).equals("border_point"))){
                        cluster_members.add(neighbor);
                        point_labels.put(neighbor, "visited");
                    }
                }
            }
        }
        
        return cluster_members;
    }
}