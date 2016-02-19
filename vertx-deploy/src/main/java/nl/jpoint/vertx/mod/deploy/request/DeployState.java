package nl.jpoint.vertx.mod.deploy.request;

public enum DeployState {
    WAITING_FOR_AS_DEREGISTER,
    WAITING_FOR_ELB_DEREGISTER,
    DEPLOYING_CONFIGS,
    STOPPING_CONTAINER,
    DEPLOYING_ARTIFACTS,
    DEPLOYING_APPLICATIONS,
    UNKNOWN,
    FAILED,
    SUCCESS,
    CONTINUE,
    WAITING_FOR_AS_REGISTER,
    WAITING_FOR_ELB_REGISTER
}
