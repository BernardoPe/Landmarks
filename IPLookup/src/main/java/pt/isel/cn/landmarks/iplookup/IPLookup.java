package pt.isel.cn.landmarks.iplookup;

import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;
import pt.isel.cn.landmarks.domain.Config;
import pt.isel.cn.landmarks.iplookup.dto.IPsPayload;
import pt.isel.cn.landmarks.iplookup.error.IPLookupError;
import pt.isel.cn.landmarks.iplookup.error.InstanceGroupNotFoundError;
import pt.isel.cn.landmarks.iplookup.error.InvalidParameterError;
import pt.isel.cn.landmarks.domain.Either;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements a Google Cloud Function that retrieves the IP addresses of instances in a given instance group.
 */
public class IPLookup implements HttpFunction {
    private static final String PROJECT_ID = Config.PROJECT_ID;

    /**
     * This method validates the parameters from the HTTP request.
     *
     * @param request The HTTP request.
     * @return Either a InvalidParameterError or a pair of zone and group name.
     */
    Either<InvalidParameterError, Pair<String, String>> validateParameters(HttpRequest request) {
        String zone = request.getFirstQueryParameter("zone").orElse(null);
        String groupName = request.getFirstQueryParameter("groupName").orElse(null);

        if (zone == null || groupName == null) {
            return Either.left(new InvalidParameterError());
        }

        return Either.right(new Pair<>(zone, groupName));
    }
    
    /**
     * This method is called when the function is invoked.
     *
     * @param request The HTTP request.
     * @param response The HTTP response.
     * @throws IOException If an I/O pt.isel.cn.landmarks.iplookup.error occurs.
     */
    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        Either<InvalidParameterError, Pair<String, String>> params = validateParameters(request);
        if(params.isLeft()) {
            response.setStatusCode(400);
            response.getWriter().write("Error: " + params.getLeft().getMessage());
            return;
        }

        String zone = params.getRight().first();
        String groupName = params.getRight().second();

        Either<IPLookupError, List<String>> result = listIpInstancesFromGroup(zone, groupName);

        if (result.isLeft()) {
            response.setStatusCode(404);
            response.getWriter().write("Error: " + result.getLeft().getMessage());
        } else {
            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(new IPsPayload(result.getRight())));
        }
    }

    /**
     * This method lists all the IP addresses of instances in a given instance group.
     *
     * @param zone      The zone where the instances are located.
     * @param groupName The name of the instance group.
     * @return A list of IP addresses of the instances in the group.
     */
    private Either<IPLookupError, List<String>> listIpInstancesFromGroup(String zone, String groupName) {
        List<String> ipList = new ArrayList<>();
        try (InstancesClient client = InstancesClient.create()) {
            for (Instance curInst : client.list(PROJECT_ID, zone).iterateAll()) {
                if (curInst.getName().contains(groupName)) {
                    String ip = curInst.getNetworkInterfaces(0).getAccessConfigs(0).getNatIP();
                    if (!ip.isBlank()) {
                        ipList.add(ip);
                    }
                }
            }
            if (ipList.isEmpty()) {
                return Either.left(new InstanceGroupNotFoundError());
            }
            return Either.right(ipList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}