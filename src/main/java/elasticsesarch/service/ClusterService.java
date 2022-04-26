package elasticsesarch.service;

import java.util.Map;

import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.health.ClusterIndexHealth;

public class ClusterService {
	private static Logger log = Logger.getLogger(ClusterService.class);
	private static final String TIRE_KICK = "Tire Kick: ";

	public static void clusterClientCheckout(RestHighLevelClient client) throws Exception {
		try {
			getListOfIndexesFromClusterClient(client);
		} catch (Exception e) {
			throw e;
		}
	}

	public static void getListOfIndexesFromClusterClient(RestHighLevelClient client) throws Exception {

		try {
			ClusterHealthResponse clusterHealthResponse = client.cluster()/**/
					.health(new ClusterHealthRequest(), RequestOptions.DEFAULT);

			Map<String, ClusterIndexHealth> indicesMap = clusterHealthResponse.getIndices();
			if (indicesMap.size() > 0) {
				indicesMap.forEach((key, obj) -> {
					log.info(TIRE_KICK + key + " -> " + obj.toString());
				});
			} else {
				log.info(TIRE_KICK + "There are no indices in this cluster (" + clusterHealthResponse.getClusterName() + ")");
			}

		} catch (Exception e) {
			throw e;
		}

	}

}
