package keystoneml.pipelines.nlp

import keystoneml.nodes.nlp.Word2VecNode
import keystoneml.workflow.{Pipeline, Transformer}
import org.apache.spark.{SparkConf, SparkContext}
import org.deeplearning4j.models.word2vec.Word2Vec
import org.slf4j.LoggerFactory
import scopt.OptionParser

/**
  * Created by litian on 3/16/17.
  */
object PubmedPipeline {
    val appName = "PubmedPipeline"

    lazy val log = LoggerFactory.getLogger(PubmedPipeline.getClass)

    // transformer node to do word2vec
    class word2vecnode() extends Transformer[String, Word2Vec]{
        def apply(in: String): Word2Vec = Word2VecNode.transform(in)
    }

    // transformer node to construct graph


    def run(sc: SparkContext, conf: PubmedConfig) : Pipeline[String, Int] = {
        val word2vec_transformer = new word2vecnode()
        val constructor = word2vec_transformer andThen
    }

    case class PubmedConfig(
        textLocation: String = "")


    def parse(args: Array[String]): PubmedConfig = new OptionParser[PubmedConfig](appName) {
        head(appName, "0.1")
        opt[String]("textLocation") required() action { (x,c) => c.copy(textLocation=x) }
    }.parse(args, PubmedConfig()).get


    def main(args: Array[String]) = {
        val conf = new SparkConf().setAppName(appName)
        conf.setIfMissing("spark.master", "local[2]") // This is a fallback if things aren't set via spark submit.

        val sc = new SparkContext(conf)

        val appConfig = parse(args)
        run(sc, appConfig)

        sc.stop()
    }
}
