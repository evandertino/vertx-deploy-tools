package nl.jpoint.vertx.mod.cluster.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeployRequest {
    private final UUID id = UUID.randomUUID();
    private final List<DeployModuleRequest> modules;
    private final List<DeployConfigRequest> configs;
    private final List<DeployArtifactRequest> artifacts;
    private final boolean elb;
    private final boolean autoScaling;
    private final boolean decrementDesiredCapacity;
    private final String autoScalingGroup;

    private final String instanceId;
    private final boolean restart;
    private DeployState state;

    @JsonCreator
    public DeployRequest(@JsonProperty("modules") List<DeployModuleRequest> modules,
                         @JsonProperty("artifacts") List<DeployArtifactRequest> artifacts,
                         @JsonProperty("configs") List<DeployConfigRequest> configs,
                         @JsonProperty("with_elb") boolean elb,
                         @JsonProperty("with_as") boolean autoScaling,
                         @JsonProperty("as_decrement_desired_capacity") boolean decrementDesiredCapacity,
                         @JsonProperty("as_group_id") String autoScalingGroup,
                         @JsonProperty("instance_id") String instanceId,
                         @JsonProperty("restart") boolean restart) {
        this.modules = modules;
        this.artifacts = artifacts;
        this.configs = configs;
        this.elb = elb;
        this.autoScaling = autoScaling;
        this.decrementDesiredCapacity = decrementDesiredCapacity;
        this.autoScalingGroup = autoScalingGroup;
        this.instanceId = instanceId;
        this.restart = restart;
    }

    public List<DeployArtifactRequest> getArtifacts() {
        return artifacts;
    }

    public List<DeployModuleRequest> getModules() {
        return modules;
    }

    public List<DeployConfigRequest> getConfigs() {
        return configs;
    }

    public UUID getId() {
        return id;
    }

    public String getAutoScalingGroup() {
        return autoScalingGroup;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public boolean withElb() {
        return elb;
    }

    public boolean withAutoScaling() {
        return elb && autoScaling;
    }

    public boolean withRestart() {
        return restart;
    }

    public void setState(DeployState state) {
        this.state = state;
    }

    public DeployState getState() {
        return this.state;
    }

    public boolean isDecrementDesiredCapacity() {
        return decrementDesiredCapacity;
    }
}
