package elasticsesarch.service;

import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import util.ConfigUtils;

public class QueryService {
	private static Logger log = Logger.getLogger(QueryService.class);

	private static String[] languages;

	static {
		try {
			languages = ConfigUtils.getProperty("es.language.fields").split(",");
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			System.exit(99);
		}
	}

	public static void testMatchPhraseQuery(RestHighLevelClient client, String searchTerm) throws Exception {
		try {
			SearchRequest request = new SearchRequest();
			SearchSourceBuilder builder = new SearchSourceBuilder();

			builder.query(QueryBuilders.matchPhraseQuery("contents.en", searchTerm));

			request.source(builder);

			SearchResponse response = client.search(request, RequestOptions.DEFAULT);
			listResponse(response);

		} catch (Exception e) {
			throw e;
		}
	}

	public static void testQueryService(RestHighLevelClient client, String searchTerm) throws Exception {
		try {
			testMatchAll(client);
			testMatch(client, searchTerm);
			testMultiMatch(client, searchTerm);
		} catch (Exception e) {
			throw e;
		}
	}

	public static void testMultiMatch(RestHighLevelClient client, String searchTerm) throws Exception {
		// multi-match query will search across all specified fields
		// Here, I use the "*" wildcard to indicate 'all fields'
		try {
			// without param passed in, this request will be run against all
			// indices
			SearchRequest request = new SearchRequest();
			SearchSourceBuilder builder = new SearchSourceBuilder();
			// builder.query(QueryBuilders.multiMatchQuery(searchTerm, "*"));
			// builder.query(QueryBuilders.multiMatchQuery("english", "*"));

			// builder.query(QueryBuilders.multiMatchQuery("الكلب", "*")); //
			// dog
			// log.info("multiMatchQuery(\"الكلب\", \"*\"");

			builder.query(QueryBuilders.multiMatchQuery("الإطلاق.", "*"));

			request.source(builder);

			SearchResponse response = client.search(request, RequestOptions.DEFAULT);
			listResponse(response);
		} catch (Exception e) {
			throw e;
		}
	}

	public static void testMultilingualMultiMatch(RestHighLevelClient client, String searchTerm) throws Exception {

		try {
			// this request will be run against all fields in all indices
			SearchSourceBuilder builder = new SearchSourceBuilder();
			// builder.query(QueryBuilders.multiMatchQuery(searchTerm, "contents.en",
			// "contents.de", "contents.ar"));
			builder.query(QueryBuilders.multiMatchQuery(searchTerm, languages));

			SearchRequest request = new SearchRequest();
			request.source(builder);

			SearchResponse response = client.search(request, RequestOptions.DEFAULT);
			listResponse(response);
		} catch (Exception e) {
			throw e;
		}
	}

	private static void listResponse(SearchResponse response) {
		SearchHits hits = response.getHits();
		TotalHits totalHits = hits.getTotalHits();
		if (totalHits.value == 0) {
			log.info("0 documents returned.");
			return;
		}

		log.info(String.valueOf(totalHits.value) + " documents were returned.");
		SearchHit[] sh = hits.getHits();
		for (SearchHit h : sh) {
			Map<String, Object> map = h.getSourceAsMap();
			map.forEach((k, v) -> log.info(k + " -> " + String.valueOf(v)));
		}

	}

	public static void testMatch(RestHighLevelClient client, String searchTerm) throws Exception {
		try {
			// without param passed in, this request will be run against all
			// indices
			SearchRequest request = new SearchRequest();
			SearchSourceBuilder builder = new SearchSourceBuilder();
			builder.query(QueryBuilders.matchQuery("arabic", searchTerm));
			request.source(builder);
			log.info("matchQuery(\"arabic\", " + searchTerm);

			SearchResponse response = client.search(request, RequestOptions.DEFAULT);
			SearchHits hits = response.getHits();
			TotalHits totalHits = hits.getTotalHits();
			if (totalHits.value == 0) {
				log.info("0 documents returned from match query.");
				return;
			}

			log.info(String.valueOf(totalHits.value) + " documents were returned from match query.");
			SearchHit[] sh = hits.getHits();
			for (SearchHit h : sh) {
				Map<String, Object> map = h.getSourceAsMap();
				map.forEach((k, v) -> log.info(k + " -> " + String.valueOf(v)));
			}

		} catch (Exception e) {
			throw e;
		}
	}

	public static void testMatchAll(RestHighLevelClient client) throws Exception {
		try {
			// without param passed in, this request will be run against all
			// indices
			SearchRequest request = new SearchRequest();
			SearchSourceBuilder builder = new SearchSourceBuilder();
			builder.query(QueryBuilders.matchAllQuery());
			request.source(builder);

			SearchResponse response = client.search(request, RequestOptions.DEFAULT);
			SearchHits hits = response.getHits();
			TotalHits totalHits = hits.getTotalHits();
			if (totalHits.value == 0) {
				log.info("0 documents returned from match_all query.  This may suggest a problem.");
				return;
			}

			log.info(String.valueOf(totalHits.value) + " documents were returned from match_all query.");
			SearchHit[] sh = hits.getHits();
			for (SearchHit h : sh) {
				Map<String, Object> map = h.getSourceAsMap();
				map.forEach((k, v) -> log.info(k + " -> " + String.valueOf(v)));
			}

		} catch (Exception e) {
			throw e;
		}
	}

}
