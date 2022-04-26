package elasticsesarch.service;

import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

public class ProcessorService {
	private static Logger log = Logger.getLogger(ProcessorService.class);

	public static XContentBuilder getDefaultInferenceProcessor() throws Exception {
		try {

			XContentBuilder builder = XContentFactory.jsonBuilder().prettyPrint()/**/
					.startObject()/**/
					.field("description", "This is the language ingest pipeline")/**/
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

					// 2nd processor in the pipeline would begin here

					.endArray()/**/
					.endObject();/**/

			// log.info(Strings.toString(builder));

			return builder;
		} catch (Exception e) {
			throw e;
		}
	}

}
