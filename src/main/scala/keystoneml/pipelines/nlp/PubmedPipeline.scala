package keystoneml.pipelines.nlp

import keystoneml.nodes.nlp.Word2VecNode
import keystoneml.workflow.{Estimator, Pipeline, Transformer}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.deeplearning4j.models.word2vec.Word2Vec
import org.slf4j.LoggerFactory
import scopt.OptionParser
import org.apache.spark.mllib.clustering.PowerIterationClustering
import keystoneml.pipelines.Logging




/**
  * Created by litian on 3/16/17.
  */
object PubmedPipeline extends Logging{
    val appName = "PubmedPipeline"

    // lazy val log = LoggerFactory.getLogger(PubmedPipeline.getClass)

    // transformer node to do word2vec
    class word2vecnode() extends Transformer[String, Word2Vec]{
        def apply(in: String): Word2Vec = Word2VecNode.transform(in)
    }

    // transformer node to construct graph
    class graphconstructor(dict: RDD[String]) extends Transformer[Word2Vec, RDD[(Long, Long, Double)]]{
        def apply(w2vec: Word2Vec): RDD[(Long, Long, Double)] = {
            val strings = dict.zipWithIndex()
            val res = strings.cartesian(strings).flatMap({ case ((string1, i1), (string2, i2)) =>
                val distance = w2vec.similarity(string1, string2)
                if(i1 < i2 && distance > 0.02){
                    Some((i1.toLong, i2.toLong, distance))
                }
                else {
                    None
                }
            })
            res
        }
    }


    // estimator to clustering the nodes in the graph
    class graphclustering() extends Transformer[RDD[(Long, Long, Double)], List[String]]{
        def apply(graph: RDD[(Long, Long, Double)]): List[String] = {
            val model = new PowerIterationClustering()
                    .setK(10)
                    .setMaxIterations(15)
                    .setInitializationMode("degree")
                    .run(graph)
            val clusters = model.assignments.collect().groupBy(_.cluster).mapValues(_.map(_.id))
            val clusterstring = clusters.map(i=>i.toString).toList
            clusterstring
        }
    }




    def run(sc: SparkContext, conf: PubmedConfig): Pipeline[String, List[String]] = {
        val word2vec_transformer = new word2vecnode()
        val dictionary = sc.textFile(conf.dictLocation)
        val graphconstruct_transformer = new graphconstructor(dictionary)
        val graphclustering_transformer = new graphclustering()



        val constructor = word2vec_transformer andThen graphconstruct_transformer andThen graphclustering_transformer
        val res = constructor(conf.textLocation)
        logInfo("RESULT OF THAT" + res.get().toString)
        constructor
    }

    case class PubmedConfig(
        textLocation: String = "",
        dictLocation: String = "")


    def parse(args: Array[String]): PubmedConfig = new OptionParser[PubmedConfig](appName) {
        head(appName, "0.1")
        opt[String]("textLocation") required() action { (x,c) => c.copy(textLocation=x) }
        opt[String]("dictLocation") required() action { (x,c) => c.copy(dictLocation=x) }
    }.parse(args, PubmedConfig()).get


    def main(args: Array[String]) = {
        val conf = new SparkConf().setAppName(appName)
        conf.setIfMissing("spark.master", "local[2]") // This is a fallback if things aren't set via spark submit.

        val sc = new SparkContext(conf)
        logInfo("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
        val appConfig = parse(args)
        run(sc, appConfig)
        sc.stop()

    }
}
