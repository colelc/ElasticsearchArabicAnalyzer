package app;

import org.apache.log4j.Logger;
import org.elasticsearch.client.RestHighLevelClient;

import elasticsesarch.service.ClientService;
import elasticsesarch.service.TireKickerService;

public class ElasticsearchApp {

	private static Logger log = Logger.getLogger(ElasticsearchApp.class);
	private static RestHighLevelClient client = null;

	public static void main(String[] args) {
		log.info("This is an implementation of Elasticsearch 7.8.1 Indexing Capabilities");

		// what a great day it is today
		try {
			client = ClientService.getESClient();

			new TireKickerService().go(client);

			// PipelineService.refreshPipelineProcessors(client);
			// PipelineService.simulatePipelineRequest(client, "multilingual");

			// IndicesService.refreshMultilingualIndex(client, "multilingual");
			// IndexerService.indexMulticulturalDocumentTest(client, "multicultural");

			// log.info("ENGLISH");
			// QueryService.testMultilingualMultiMatch(client, "home");
			// log.info("GERMAN");
			// QueryService.testMultilingualMultiMatch(client, "Hause");

			// IndicesService.deleteIndex(client, "multicultural");
			// IndicesService.deleteIndex(client, "proto");
			client.close();
			log.info("DONE");
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}

}
