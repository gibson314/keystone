package keystoneml.nodes.graph;

/**
 * Created by litian on 3/17/17.
 */

import edu.uci.ics.jung.algorithms.cluster.BicomponentClusterer;
import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer;
import edu.uci.ics.jung.algorithms.cluster.VoltageClusterer;
import edu.uci.ics.jung.algorithms.generators.random.ErdosRenyiGenerator;
import edu.uci.ics.jung.graph.event.GraphEvent;
import org.apache.commons.collections15.Factory;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import org.apache.spark.ml.feature.InteractableTerm;

import java.util.Collection;
import java.util.Set;

public class GraphText {
    static Factory<UndirectedGraph<String,Integer>> graphFactory;
    static Factory<String> vertexFactory;
    static Factory<Integer> edgeFactory;

    public static void makeErdosRenyiRandom(int nodes, double p) {
        graphFactory = new Factory<UndirectedGraph<String,Integer>>() {
            public UndirectedGraph<String,Integer> create() {
                return new UndirectedSparseMultigraph<String,Integer>();
            }
        };
        vertexFactory = new Factory<String>() {
            int count;
            public String create() {
                return Character.toString((char)('A'+count++));
            }
        };
        edgeFactory =
                new Factory<Integer>() {
                    int count;
                    public Integer create() {
                        return count++;
                    }
                };



        ErdosRenyiGenerator<String,Integer> generator =
                new ErdosRenyiGenerator<String,Integer>(graphFactory, vertexFactory, edgeFactory,
                        100000,0.001);

        generator.setSeed(0);

        UndirectedGraph<String,Integer> graph = (UndirectedGraph) generator.create();
        System.out.println(graph.getEdges().size());
        EdgeBetweennessClusterer cluster = new EdgeBetweennessClusterer(100);
        long startTime=System.currentTimeMillis();
        //cluster.transform(graph);
        //EdgeBetweennessClusterer<String, Integer> clust = new EdgeBetweennessClusterer<String, Integer>(100);
        //Set<Set<String>> edgeSet = clust.transform(graph);
        VoltageClusterer<Integer, Integer> vc = new VoltageClusterer(graph, 20);
        Collection<Set<Integer>> clusters = vc.cluster(20);
        System.out.println(clusters.size());

        //BicomponentClusterer bicluster = new BicomponentClusterer();
        //bicluster.transform(graph);
        long endTime=System.currentTimeMillis(); //获取结束时间
        System.out.println("程序运行时间： "+(endTime-startTime)+"ms");


        /*

        long startTime1=System.currentTimeMillis();
        VoltageClusterer cluster2 = new VoltageClusterer(graph, 3);
        cluster2.cluster(10);
        long endTime1=System.currentTimeMillis(); //获取结束时间
        System.out.println("程序运行时间： "+(endTime1-startTime1)+"ms");
*/


    }

    public static void main(String[] args){
        makeErdosRenyiRandom(1, 0.2);
    }
}
