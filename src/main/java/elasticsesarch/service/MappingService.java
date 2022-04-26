package elasticsesarch.service;

import java.util.Map;

import org.apache.log4j.Logger;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.GetMappingsResponse;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

public class MappingService {
	private static Logger log = Logger.getLogger(MappingService.class);
	private static final String TIRE_KICK = "Tire Kick: ";

	public static XContentBuilder getMulticulturalMapping() throws Exception {
		try {
			return XContentFactory.jsonBuilder().startObject()/**/
					.field("dynamic", "strict")/**/
					.startObject("properties")/**/
					.startObject("contents")/**/
					.startObject("properties")/**/
					.startObject("language")/**/
					.field("type", "keyword")/**/
					.endObject()/**/ // language

					.startObject("supported")/**/
					.field("type", "boolean")/**/
					.endObject()/**/ // supported

					.startObject("default")/**/
					.field("type", "text")/**/
					.field("analyzer", "default")/**/
					.startObject("fields")/**/
					.startObject("icu")/**/
					.field("type", "text")/**/
					.field("analyzer", "icu_analyzer")/**/
					.endObject()/**/ // icu
					.endObject()/**/ // fields
					.endObject()/**/ // default

					.startObject("en")/**/
					.field("type", "text")/**/
					.field("analyzer", "english")/**/
					.endObject()/**/ // en

					.startObject("ar")/**/
					.field("type", "text")/**/
					.field("analyzer", "arabic")/**/
					.endObject()/**/ // ar

					.startObject("de")/**/
					.field("type", "text")/**/
					.field("analyzer", "german")/**/
					.endObject()/**/ // de

					.endObject()/**/ // properties
					.endObject()/**/ // contents
					.endObject()/**/ // properties
					.endObject();

		} catch (Exception e) {
			throw e;
		}
	}

	public static void putMappings(RestHighLevelClient client, String indexName) throws Exception {
		try {

			XContentBuilder builder = XContentFactory.jsonBuilder().startObject();

			builder.startObject("properties");
			builder.startObject("raw").field("type", "text").endObject();
			builder.startObject("arabic").field("type", "text").endObject();
			builder.startObject("english").field("type", "text").endObject();
			builder.startObject("unicode").field("type", "text").endObject();
			builder.endObject(); // properties

			builder.endObject();

			log.info(TIRE_KICK + Strings.toString(builder));

			PutMappingRequest request = new PutMappingRequest(indexName).source(builder);
			AcknowledgedResponse response = client.indices().putMapping(request, RequestOptions.DEFAULT);

			if (response.isAcknowledged()) {
				log.info(TIRE_KICK + "The mapping for index \"" + indexName + "\" " + "has been created");
				// getMappings(client, indexName);
			}
		} catch (Exception e) {
			throw e;
		}
	}

	public static void getMappings(RestHighLevelClient client, String indexName) throws Exception {
		try {
			GetMappingsRequest request = new GetMappingsRequest().indices(indexName);
			GetMappingsResponse response = client.indices().getMapping(request, RequestOptions.DEFAULT);

			log.info(TIRE_KICK + "The mapping for index \"" + indexName + "\" " + "is: ");
			Map<String, MappingMetadata> mappings = response.mappings();
			mappings.forEach((key, value) -> {
				Map<String, Object> map = value.getSourceAsMap();
				map.forEach((k, v) -> {
					log.info(TIRE_KICK + k + ": " + String.valueOf(v));
				});
			});

		} catch (Exception e) {
			throw e;
		}
	}

}
