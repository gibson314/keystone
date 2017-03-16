package keystoneml.nodes.nlp

/**
  * Created by litian on 3/16/17.
  */
import org.apache.spark.{SparkConf, SparkContext}
import org.slf4j.LoggerFactory
import org.datavec.api.util.ClassPathResource
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.text.sentenceiterator.{BasicLineIterator, SentenceIterator, SentencePreProcessor}
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory
import org.deeplearning4j.models.word2vec.Word2Vec
import org.deeplearning4j.ui.UiServer



object Word2VecNode {
    //case class Config(textLocation: String = "")
    lazy val log = LoggerFactory.getLogger(Word2VecNode.getClass)


    // def main(args: Array[String]) ={
    def transform(filePath: String): Word2Vec = {
        //val filePath = new ClassPathResource("/home/litian/Spark/abstracts_1000.tsv").getFile.getAbsolutePath
        //val filePath = "/home/litian/Spark/abstracts_1000.tsv"
        log.info("Load sentences...")
        // Strip
        val iter: SentenceIterator = new BasicLineIterator(filePath)
        iter.setPreProcessor(new SentencePreProcessor {
            override def preProcess(sentence: String): String = sentence.toLowerCase()
        })

        // Split on white space
        val token = new DefaultTokenizerFactory()
        token.setTokenPreProcessor(new CommonPreprocessor())

        log.info("Building model...")
        val vec = new Word2Vec.Builder()
                .minWordFrequency(10)
                .iterations(1)
                .layerSize(100)
                .seed(42)
                .windowSize(5)
                .iterate(iter)
                .tokenizerFactory(token)
                .build()

        log.info("Fitting Word2Vec model....")
        vec.fit()
        vec
    }

    def main(args: Array[String]) = {
        //log.info("Writing word vectors to text file....")
        // Write word vectors
        //WordVectorSerializer.writeWordVectors(vec, "pathToWriteto.txt")

        val vec = transform("/home/litian/Spark/abstracts_1000.tsv")
        val lst:java.util.Collection[String] = vec.wordsNearest("cardiac", 10)
        System.out.println("cardiac")
        System.out.println(lst)
    }
}
