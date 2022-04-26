package elasticsesarch.service;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

public class IndexerService {
	private static Logger log = Logger.getLogger(IndexerService.class);
	private static List<String> testContentList;

	static {
		testContentList = Arrays.asList(new String[] { /**/
				"I want to go home", /**/
				"Ich möchte nach Hause gehen", /**/
				"أريد العودة إلى ديارهم", /**/ // I want to go home
				"هذه جملتي العشوائية التي ليس لها أي معنى جوهري على الإطلاق/**/" });
	}

	public static void indexMulticulturalDocumentTest(RestHighLevelClient client, String indexName) throws Exception {
		try {
			testContentList.forEach(content -> {
				log.info("Indexing content: " + content);
				try {
					IndexRequest indexRequest = new IndexRequest(indexName).source(generateTestDocument(content));
					indexRequest.setPipeline(PipelineService.getLanguageInferencePipelineId());

					IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
					Result result = indexResponse.getResult();
					if (Result.valueOf(result.name()).compareTo(DocWriteResponse.Result.CREATED) == 0) {
						log.info("Document has been indexed");
					}
				} catch (Exception e) {
					log.error(e.getMessage());
					e.printStackTrace();
				}
			});

			Thread.sleep(5000l);

			QueryService.testMatchAll(client);

		} catch (Exception e) {
			throw e;
		}
	}

	private static XContentBuilder generateTestDocument(String content) throws Exception {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
			builder.field("contents", content);
			builder.endObject();

			return builder;
		} catch (Exception e) {
			throw e;
		}
	}

	public static void indexTestDocument(RestHighLevelClient client, String indexName) throws Exception {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
			builder.field("raw", "The quick brown fox jumped over the lazy dog");
			builder.field("random_english", "A lazy white duck flew above the lake");
			builder.field("arabic", "قفز الثعلب البني السريع فوق الكلب الكسول");

			// random arabic translation is "This is my random sentence that has absolutely
			// no essential meaning."
			builder.field("random_arabic", "﻿هذه جملتي العشوائية التي ليس لها أي معنى جوهري على الإطلاق.");

			builder.field("english", "english");
			builder.field("unicode", "unicode");
			builder.endObject();

			IndexRequest request = new IndexRequest(indexName).source(builder);
			IndexResponse response = client.index(request, RequestOptions.DEFAULT);
			Result result = response.getResult();
			log.info("Indexer result is: " + result.toString());

		} catch (Exception e) {
			throw e;
		}
	}
}
