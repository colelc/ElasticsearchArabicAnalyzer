package elasticsesarch.service;

import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

public class AnalyzerService {
	private static Logger log = Logger.getLogger(AnalyzerService.class);

	public static XContentBuilder getArabicAnalyzer() throws Exception {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder().startObject();

			builder.startObject("analysis");
			builder.startObject("arabic_stemmer")/**/
					.field("type", "stemmer")/**/
					.field("language", "arabic")/**/
					.endObject();

			builder.startObject("analyzer");
			builder.startObject("arabic");
			builder.field("tokenizer", "standard");
			builder.endObject();

			builder.endObject();
			builder.endObject();
			builder.endObject();

			return builder;
		} catch (Exception e) {
			throw e;
		}
	}

}
