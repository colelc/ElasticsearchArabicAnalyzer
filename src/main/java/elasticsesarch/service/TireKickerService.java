package elasticsesarch.service;

import org.apache.log4j.Logger;
import org.elasticsearch.client.RestHighLevelClient;

import util.ConfigUtils;

public class TireKickerService {
	private static Logger log = Logger.getLogger(TireKickerService.class);

	private static final String TIRE_KICK = "Tire Kick: ";
	private static boolean doTireKick = false;
	private static final String quickBrownFox = "Ù‚Ù�Ø² Ø§Ù„Ø«Ø¹Ù„Ø¨ Ø§Ù„Ø¨Ù†ÙŠ Ø§Ù„Ø³Ø±ÙŠØ¹ Ù�ÙˆÙ‚ Ø§Ù„ÙƒÙ„Ø¨ Ø§Ù„ÙƒØ³ÙˆÙ„";
	private static final String jumpArabic = "Ù‚Ù�Ø²";
	private static final String jump = "jump";

	public TireKickerService() {
		super();
		try {
			doTireKick = Boolean.valueOf(ConfigUtils.getProperty("tire.kick")).booleanValue();
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			System.exit(99);
		}

	}

	public void go(RestHighLevelClient client) throws Exception {
		try {
			if (doTireKick) {
				log.info("Kicking the tires ...");
				kickTheTires(client);
				log.info("The tires have been kicked");
			}
		} catch (Exception e) {
			throw e;
		}
	}

	private void kickTheTires(RestHighLevelClient client) throws Exception {
		try {

			initialCheckouts(client);

			// arabicLanguageAnalyzer(client);

			// log.info(TIRE_KICK + "waiting 1 second for indexing to complete");
			// Thread.sleep(1000l);
			// String searchTermForTireKick = jumpArabic;
			// queryTest(client, searchTermForTireKick);

		} catch (Exception e) {
			throw e;
		}

	}

	private void initialCheckouts(RestHighLevelClient client) throws Exception {
		try {
			ClusterService.clusterClientCheckout(client);
			IndicesService.indicesClientCheckout(client);
		} catch (Exception e) {
			throw e;
		}
	}

	private void arabicLanguageAnalyzer(RestHighLevelClient client) throws Exception {
		try {
			prepareAnalyzerIndexer(client, ConfigUtils.getProperty("es.index.name.for.tire.kick"));
		} catch (Exception e) {
			throw e;
		}
	}

	private void prepareAnalyzerIndexer(RestHighLevelClient client, String indexName) throws Exception {
		try {
			IndicesService.defineIndexWithArabicLanguageAnalyzer(client, indexName);
			MappingService.putMappings(client, indexName);
			MappingService.getMappings(client, indexName);

			if (doTireKick) {
				IndexerService.indexTestDocument(client, indexName);
			}
		} catch (Exception e) {
			throw e;
		}
	}

	private void queryTest(RestHighLevelClient client, String searchTerm) throws Exception {
		try {
			QueryService.testQueryService(client, searchTerm);
		} catch (Exception e) {
			throw e;
		}
	}

}
