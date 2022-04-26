package elasticsesarch.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.ingest.DeletePipelineRequest;
import org.elasticsearch.action.ingest.GetPipelineRequest;
import org.elasticsearch.action.ingest.GetPipelineResponse;
import org.elasticsearch.action.ingest.PutPipelineRequest;
import org.elasticsearch.action.ingest.SimulateDocumentResult;
import org.elasticsearch.action.ingest.SimulatePipelineRequest;
import org.elasticsearch.action.ingest.SimulatePipelineResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;

import util.ConfigUtils;

public class PipelineService {
	private static Logger log = Logger.getLogger(PipelineService.class);
	private static String LANGUAGE_INFERENCE_PIPELINE_ID = null;
	private static boolean includeXPack = false;

	static {
		try {
			LANGUAGE_INFERENCE_PIPELINE_ID = ConfigUtils.getProperty("es.pipeline.language.inference.model.id");
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public static PutPipelineRequest getPipelineRequest() throws Exception {
		try {
			XContentBuilder builder = ProcessorService.getDefaultInferenceProcessor();
			BytesReference source = BytesReference.bytes(builder);

			PutPipelineRequest request = new PutPipelineRequest(/**/
					LANGUAGE_INFERENCE_PIPELINE_ID, /**/
					source, /**/
					XContentType.JSON/**/
			);

			return request;
		} catch (Exception e) {
			throw e;
		}
	}

	public static boolean createPipeline(RestHighLevelClient client) throws Exception {
		boolean retValue = false;
		try {
			PutPipelineRequest request = getPipelineRequest();
			AcknowledgedResponse response = client.ingest().putPipeline(request, RequestOptions.DEFAULT);
			if (response.isAcknowledged()) {
				log.info("Pipeline Request is acknowledged");
				retValue = true;
			} else {
				log.error("Cannot submit pipeline request.");
			}

		} catch (Exception e) {
			throw e;
		}
		return retValue;
	}

	public static Set<String> getAllPipelineIds(RestHighLevelClient client, String... includeXPackPipelines) throws Exception {

		try {
			GetPipelineRequest request = new GetPipelineRequest("*");
			GetPipelineResponse response = client.ingest().getPipeline(request, RequestOptions.DEFAULT);

			Boolean xpackCheck = includeXPackPipelines.length > 0 ? Boolean.TRUE : Boolean.FALSE;
			if (xpackCheck) {
				includeXPack = Boolean.valueOf(includeXPackPipelines[0]).booleanValue();
			}

			return response.pipelines()/**/
					.stream()/**/
					.filter(f -> !f.getId().startsWith("xpack") || (f.getId().startsWith("xpack") && includeXPack))/**/
					.map(m -> m.getId())/**/
					.collect(Collectors.toSet());

		} catch (Exception e) {
			throw e;
		}
	}

	public static void describePipeline(RestHighLevelClient client, String... pipelineId) throws Exception {

		try {
			String pId = pipelineId.length > 0 ? pipelineId[0] : LANGUAGE_INFERENCE_PIPELINE_ID;

			GetPipelineRequest request = new GetPipelineRequest(pId);
			GetPipelineResponse response = client.ingest().getPipeline(request, RequestOptions.DEFAULT);

			if (response.isFound()) {
				response.pipelines().forEach(p -> {
					p.getConfigAsMap().entrySet()/**/
							.forEach(e -> log.info(p.getId() + ": " + e.getKey() + " -> " + e.getValue()));
				});
			} else {
				log.warn("No pipeline is found for pipeline id: " + pId);
			}

		} catch (Exception e) {
			throw e;
		}
	}

	public static void deletePipeline(RestHighLevelClient client, String... pipelineId) throws Exception {
		String pId = null;
		try {
			pId = pipelineId.length > 0 ? pipelineId[0] : LANGUAGE_INFERENCE_PIPELINE_ID;

			DeletePipelineRequest request = new DeletePipelineRequest(pId);
			AcknowledgedResponse response = client.ingest().deletePipeline(request, RequestOptions.DEFAULT);
			if (response.isAcknowledged()) {
				log.info("The pipeline \"" + pId + "\" has been deleted");
			} else {
				log.warn("No acknowledgement of DELETE request for pipeline having id \"" + pipelineId + "\"");
			}
		} catch (ElasticsearchStatusException re) {
			if (re.status().compareTo(RestStatus.NOT_FOUND) == 0) {
				log.info("The pipeline ID \"" + pId + "\" cannot be deleted because it does not exist");
			} else {
				throw re;
			}
		} catch (Exception e) {
			throw e;
		}
	}

	public static void refreshPipelineProcessors(RestHighLevelClient client) throws Exception {
		try {
			deletePipeline(client);
			// Set<String> pipelines = new
			// HashSet<>(PipelineService.getAllPipelineIds(client));
			// pipelines.forEach(p -> log.info("Pipeline: " + p));

			createPipeline(client);
			describePipeline(client);
		} catch (Exception e) {
			throw e;
		}
	}

	public static void simulatePipelineRequest(RestHighLevelClient client, String indexName) throws Exception {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder().prettyPrint()/**/
					.startObject()/**/
					.startObject("pipeline")/**/
					// .field("description", "This is the language ingest pipeline")/**/
					.startArray("processors")/**/

					// inference processor
					.startObject()/**/
					.startObject("inference")/**/
					.field("model_id", "lang_ident_model_1")/**/
					.startObject("inference_config")/**/
					.startObject("classification")/**/
					.field("num_top_classes", 2)/**/
					.field("results_field", "result")/**/
					.endObject()/**/ // classification
					.endObject()/**/ // inference_config
					.startObject("field_map")/**/
					.field("contents", "text")/**/
					.endObject()/**/ // field_mappings
					.field("target_field", "_ml.lang_ident")/**/
					.endObject()/**/ // inference
					.endObject()/**/

					// rename processor
					.startObject()/**/
					.startObject("rename")/**/
					.field("field", "contents")/**/
					.field("target_field", "contents.default")/**/
					.endObject()/**/
					.endObject()/**/

					// rename processor
					.startObject()/**/
					.startObject("rename")/**/
					.field("field", "_ml.lang_ident.result")/**/
					.field("target_field", "contents.language")/**/
					.endObject()/**/
					.endObject()/**/

					// painless script processor
					.startObject()/**/
					.startObject("script")/**/
					.field("lang", "painless")/**/
					.field("source", "ctx.contents.supported = (['en', 'de', 'ar'].contains(ctx.contents.language))")/**/
					.endObject()/**/
					.endObject()/**/

					// set processor
					.startObject()/**/
					.startObject("set")/**/
					.field("if", "ctx.contents.supported")/**/
					.field("field", "contents.{{contents.language}}")/**/
					.field("value", "{{contents.default}}")/**/
					.field("override", false)/**/
					.endObject()/**/
					.endObject()/**/

					.endArray()/**/ // processors
					.endObject()/**/ // pipeline

					// now add the test document
					.startArray("docs")/**/
					.startObject()/**/ // 1st document
					// .field("_index", indexName)/**/
					// .field("_id", "id")/**/
					.startObject("_source")/**/
					.field("contents", "This is my test document")/**/
					.endObject()/**/ // _source
					.endObject()/**/

					.startObject()/**/ // 2nd document
					.startObject("_source")/**/
					.field("contents", "Ich möchte nach Hause gehen")/**/
					.endObject()/**/ // _source
					.endObject()/**/

					.startObject()/**/ // 3rd document - should be unsupported language
					.startObject("_source")/**/
					.field("contents", "Lupus non timet canem latrantem")/**/
					.endObject()/**/ // _source
					.endObject()/**/

					.endArray()/**/

					.endObject();/**/// root

			log.info(Strings.toString(builder));
			// client.close();
			// System.exit(0);

			BytesReference source = BytesReference.bytes(builder);

			SimulatePipelineRequest request = new SimulatePipelineRequest(/**/
					source, /**/
					XContentType.JSON/**/
			);

			SimulatePipelineResponse response = client.ingest().simulate(request, RequestOptions.DEFAULT);
			List<SimulateDocumentResult> list = response.getResults();
			list.forEach(res -> {
				log.info(Strings.toString(res));
			});

		} catch (Exception e) {
			throw e;
		}
	}

	public static String getLanguageInferencePipelineId() {
		return LANGUAGE_INFERENCE_PIPELINE_ID;
	}

}
