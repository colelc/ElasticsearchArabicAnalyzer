package elasticsesarch.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.cluster.metadata.AliasMetadata;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.Settings.Builder;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;

public class IndicesService {
	private static Logger log = Logger.getLogger(IndicesService.class);
	private static final String TIRE_KICK = "Tire Kick: ";

	public static void refreshMultilingualIndex(RestHighLevelClient client, String indexName) throws Exception {
		// NOTE: IMPORTANT IMPORTANT IMPORTANT IMPORTANT

		// The mapping for this multilingual index requires the ICU Analyzer plugin.
		// To install this plugin:
		// From the Elasticsearch bin directory:
		// Enter the command:
		// ./elasticsearch-plugin install analysis-icu
		// And then of course: recycle the ES instance.

		// 1st we send a CreateIndexRequest
		// 2nd we send a PutMappingRequest
		try {
			deleteIndex(client, indexName);

			Builder settings = Settings.builder()/**/
					.put("index.number_of_shards", 1)/**/
					.put("index.number_of_replicas", 0);/**/

			CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName).settings(settings);
			CreateIndexResponse createIndexResponse = client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
			log.info("The index \"" + createIndexResponse.index() + "\" " + "has been created");

			PutMappingRequest putMappingRequest = new PutMappingRequest(indexName).source(MappingService.getMulticulturalMapping());
			AcknowledgedResponse putMappingResponse = client.indices().putMapping(putMappingRequest, RequestOptions.DEFAULT);

			if (putMappingResponse.isAcknowledged()) {
				log.info("The mapping for index \"" + indexName + "\" " + "has been created");
				// getMappings(client, indexName);
			}

		} catch (Exception e) {
			client.close();
			throw e;
		}
	}

	public static void defineIndexWithArabicLanguageAnalyzer(RestHighLevelClient client, String indexName) throws Exception {
		try {
			IndicesService.deleteIndex(client, indexName);

			XContentBuilder builder = AnalyzerService.getArabicAnalyzer();
			String settingsString = Strings.toString(builder);
			log.info(TIRE_KICK + settingsString);

			Builder settings = Settings.builder()/**/
					.put("index.max_inner_result_window", 250)/**/
					.put("index.write.wait_for_active_shards", 1)/**/
					.put("index.query.default_field", "raw")/**/
					.put("index.number_of_shards", 3)/**/
					.put("index.number_of_replicas", 2)/**/
					.loadFromSource(settingsString, XContentType.JSON);

			CreateIndexRequest request = new CreateIndexRequest(indexName).settings(settings);
			CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
			log.info(TIRE_KICK + "The index \"" + response.index() + "\" " + "has been created");
			IndicesService.getListOfIndexesFromIndicesClient(client);
		} catch (Exception e) {
			throw e;
		}
	}

	public static void indicesClientCheckout(RestHighLevelClient client) throws Exception {
		try {
			getListOfIndexesFromIndicesClient(client);
			getListOfAliasesFromIndicesClient(client);
		} catch (Exception e) {
			throw e;
		}
	}

	public static void getListOfAliasesFromIndicesClient(RestHighLevelClient client) throws Exception {

		try {
			IndicesClient indicesClient = client.indices();

			GetAliasesResponse getAliasesResponse = indicesClient.getAlias(new GetAliasesRequest(), RequestOptions.DEFAULT);
			Map<String, Set<AliasMetadata>> map = getAliasesResponse.getAliases();
			map.forEach((key, dataSet) -> {
				dataSet.forEach(amd -> {
					log.info(TIRE_KICK + key + " -> " + amd.getAlias());
				});
			});

		} catch (Exception e) {
			throw e;
		}

	}

	public static void getListOfIndexesFromIndicesClient(RestHighLevelClient client) throws Exception {

		try {
			// this will throw an error if there are 0 indices in the cluster
			GetIndexRequest getIndexRequest = new GetIndexRequest("*");

			// if true, missing or closed indices are not included in result
			boolean ignoreUnavailable = false;

			// if true, the request does not return an error if a wildcard
			// expression or _all value retrieves only missing or closed indices
			boolean allowNoIndices = true;

			// don't know
			boolean expandToOpenIndices = true;

			// don't know
			boolean expandToClosedIndices = true;

			// the request
			getIndexRequest.indicesOptions(/**/
					IndicesOptions.fromOptions(/**/
							ignoreUnavailable, /**/
							allowNoIndices, /**/
							expandToOpenIndices, /**/
							expandToClosedIndices));

			GetIndexResponse getIndexResponse = client.indices().get(getIndexRequest, RequestOptions.DEFAULT);
			List<String> indices = Arrays.asList(getIndexResponse.getIndices());

			log.info(TIRE_KICK + "Listing indexes ...");
			indices.forEach(index -> {
				log.info(TIRE_KICK + "Index found: " + index);
			});

			Map<String, MappingMetadata> mapping = getIndexResponse.getMappings();
			mapping.forEach((key, metadataObject) -> {
				Map<String, Object> thisMap = metadataObject.getSourceAsMap();
				thisMap.forEach((k, v) -> {
					log.info(TIRE_KICK + key + " -> " + k + " ---> " + v);
				});
			});

		} catch (Exception e) {
			throw e;
		}

	}

	public static void deleteIndex(RestHighLevelClient client, String indexName) throws Exception {
		try {
			boolean exists = client.indices().exists(new GetIndexRequest(indexName), RequestOptions.DEFAULT);

			if (exists) {
				log.info("Deleting index: " + indexName);
				DeleteIndexRequest request = new DeleteIndexRequest(indexName);
				client.indices().delete(request, RequestOptions.DEFAULT);
				log.info("Index \"" + indexName + "\" has been deleted");
			} else {
				log.info("Index \"" + indexName + "\" does not exist, therefore it cannot be deleted");
			}
		} catch (Exception e) {
			throw e;
		}
	}

}
