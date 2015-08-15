package nl.jpoint.vertx.mod.deploy.aws.state;

import nl.jpoint.vertx.mod.deploy.aws.AwsContext;
import nl.jpoint.vertx.mod.deploy.aws.AwsElbUtil;
import nl.jpoint.vertx.mod.deploy.command.Command;
import nl.jpoint.vertx.mod.deploy.request.DeployRequest;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;

public class AwsRegisterFactory {
    public static Command<DeployRequest> getInstance(AwsContext context, DeployRequest deployRequest, JsonObject config, Vertx vertx) {
        if (deployRequest.withElb() && !deployRequest.withAutoScaling()) {
            final AwsElbUtil awsElbUtil = new AwsElbUtil(context.getAwsUtil(),
                    context.getRegion(), config.getString("aws.elb.loadbalancer"), config.getString("aws.elb.instanceid"));
            return new AwsElbRegisterInstance(vertx, awsElbUtil);
        }  else if (deployRequest.withElb() && deployRequest.withAutoScaling()) {
            return new AwsAsRegisterInstance(vertx, context, config.getInteger("aws.as.register.maxduration", 4));
        }
        throw new IllegalStateException("Unable to create registration instance");
    }
}