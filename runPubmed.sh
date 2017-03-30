export SPARK_HOME=/usr/local/spark
KEYSTONE_MEM=4g ./bin/run-pipeline.sh \
		  keystoneml.pipelines.nlp.PubmedPipeline \
		    --textLocation ~/Pubmed/abstracts_text.tsv\
			  --dictLocation ~/Pubmed/sampledict.tsv \

