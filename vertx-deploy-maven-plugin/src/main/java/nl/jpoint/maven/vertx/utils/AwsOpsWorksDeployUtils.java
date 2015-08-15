package nl.jpoint.maven.vertx.utils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.opsworks.AWSOpsWorksClient;
import com.amazonaws.services.opsworks.model.DescribeInstancesRequest;
import com.amazonaws.services.opsworks.model.DescribeInstancesResult;
import nl.jpoint.maven.vertx.mojo.DeployConfiguration;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;


public class AwsOpsWorksDeployUtils {

    private final AWSOpsWorksClient awsOpsWorksClient;


    public AwsOpsWorksDeployUtils(String serverId, Settings settings) throws MojoFailureException {
        if (settings.getServer(serverId) == null) {
            throw new MojoFailureException("No server config for id : " + serverId);
        }
        Server server = settings.getServer(serverId);

        BasicAWSCredentials credentials = new BasicAWSCredentials(server.getUsername(), server.getPassword());
        awsOpsWorksClient = new AWSOpsWorksClient(credentials);
        awsOpsWorksClient.setRegion(Region.getRegion(Regions.EU_WEST_1));

    }


    public void getHostsOpsWorks(Log log, DeployConfiguration activeConfiguration) throws MojoFailureException {
        log.info("retrieving list of hosts for stack with id : " + activeConfiguration.getOpsWorksStackId());
        activeConfiguration.getHosts().clear();

        try {
            DescribeInstancesResult result = awsOpsWorksClient.describeInstances(new DescribeInstancesRequest().withLayerId(activeConfiguration.getOpsWorksLayerId()));
            result.getInstances().stream()
                    .filter(i -> i.getStatus().equals("online"))
                    .forEach(i -> {
                                String ip = activeConfiguration.getAwsPrivateIp() ? i.getPrivateIp() : i.getPublicIp();
                                log.info("Adding host from opsworks response : " + ip);
                                activeConfiguration.getHosts().add("http://" + ip + ":6789");
                            }
                    );

        } catch (AmazonClientException e) {
            throw new MojoFailureException(e.getMessage());
        }
    }
}