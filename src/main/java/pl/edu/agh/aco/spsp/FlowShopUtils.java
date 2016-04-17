package pl.edu.agh.aco.spsp;

import java.util.*;

/**
 * Utilities methods for Flow-Shop problem solving.
 *
 * @author Adrián Pareja (adrian@pareja.com)
 * @author Carlos G. Gavidia (cgavidia@acm.org)
 */
public class FlowShopUtils {

    /**
     * Gets the makespan for an Schedule.
     *
     * @param solution Schedule to evaluate.
     * @param graph    Problem graph.
     * @return Schedule makespan.
     */
    public static double getScheduleMakespan(int[] solution, double[][] graph, int employees) {
        int randomMax = solution.length - employees;

        Random random = new Random();

        int left = 0;
        int[] splits = new int[employees +1];
        splits[0]=0;
        splits[employees] = solution.length;
        int res=0;
        for (int i = 0; i < employees - 1; i++) {
            while(!((res = random.nextInt(solution.length)+1) > left) || !(res < solution.length-employees+i)) {
            }
            splits[i+1] = res;
            left = res;
        }
        List<List> list = new ArrayList<>();
        List<Integer> intList = new ArrayList<Integer>();
        for (int index = 0; index < solution.length; index++)
        {
            intList.add(solution[index]);
        }
        for(int i=0;i< splits.length-1;i++){
            list.add(intList.subList(splits[i],splits[i+1]));
            System.out.println(list.get(i));
        }
        List<Integer> sums = new ArrayList<>();
        for (List list1 : list) {
            sums.add(sum(list1, graph));
        }
        return Collections.max(sums);

    /*    int machines = jobInfo[0].length;
        double[] machinesTime = new double[machines];
        double tiempo = 0;



        for (int job : solution) {
            for (int i = 0; i < machines; i++) {
                tiempo = jobInfo[job][i];
                if (i == 0) {
                    machinesTime[i] = machinesTime[i] + tiempo;
                } else {
                    if (machinesTime[i] > machinesTime[i - 1]) {
                        machinesTime[i] = machinesTime[i] + tiempo;
                    } else {
                        machinesTime[i] = machinesTime[i - 1] + tiempo;
                    }
                }
            }
        }
        return machinesTime[machines - 1];
    */
    }

    public static int sum(List<Integer> list, double[][] graph){
        int sum=0;
        for (Integer i : list) {
            sum += graph[i][0];
        }

        return sum;
    }

}
