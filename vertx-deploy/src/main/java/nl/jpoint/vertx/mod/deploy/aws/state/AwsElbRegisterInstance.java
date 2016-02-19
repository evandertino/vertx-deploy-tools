package nl.jpoint.vertx.mod.deploy.aws.state;

import nl.jpoint.vertx.mod.deploy.aws.*;
import nl.jpoint.vertx.mod.deploy.command.Command;
import nl.jpoint.vertx.mod.deploy.request.DeployRequest;
import nl.jpoint.vertx.mod.deploy.util.LogConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.time.LocalDateTime;
import java.util.function.Function;

public class AwsElbRegisterInstance implements Command<DeployRequest> {
    private static final Logger LOG = LoggerFactory.getLogger(AwsElbRegisterInstance.class);
    private final AwsElbUtil awsElbUtil;
    private final AwsAutoScalingUtil awsAsUtil;
    private final AwsPollingElbStateObservable poller;

    public AwsElbRegisterInstance(io.vertx.core.Vertx vertx, String deployId, AwsContext awsContext, Integer maxDuration, Function<String, Boolean> requestStillActive) {
        this.awsElbUtil = new AwsElbUtil(awsContext);
        this.awsAsUtil = new AwsAutoScalingUtil(awsContext);
        this.poller = new AwsPollingElbStateObservable(vertx, deployId, awsElbUtil, LocalDateTime.now().plusMinutes(maxDuration), requestStillActive, AwsState.INSERVICE);
    }

    public Observable<DeployRequest> executeAsync(DeployRequest request) {
        try {
            return awsAsUtil.listLoadBalancers(request.getAutoScalingGroup())
                    .flatMap(elb -> awsElbUtil.registerInstanceWithLoadBalancer(request.getInstanceId(), elb))
                    .flatMap(elb -> poller.poll(request, elb));
        } catch (AwsException e) {
            LOG.error("[{} - {}]: Error while executing request to AWS -> {}", LogConstants.AWS_ELB_REQUEST, request.getId(), e.getMessage());
            throw new IllegalStateException();
        }
    }
}
