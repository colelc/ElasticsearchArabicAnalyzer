package elasticsesarch.service;

import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import util.ConfigUtils;

public class ClientService {

	private static Logger log = Logger.getLogger(ClientService.class);

	private static RestHighLevelClient client = null;

	public static RestHighLevelClient getESClient() throws Exception {
		if (client == null) {
			try {
				String host = ConfigUtils.getProperty("es.host");
				int port = ConfigUtils.getPropertyAsInt("es.port");

				log.info("Acquiring Elasticsearch RestClient");
				client = new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, "http")));
				log.info("Elasticsearch RestClient acquired: " + host + ":" + port);
			} catch (Exception e) {
				throw e;
			}
		}
		return client;
	}
}
