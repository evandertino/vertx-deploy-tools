package nl.jpoint.vertx.mod.cluster.request;

public enum DeployState {
    WAITING_FOR_DEREGISTER,
    DEPLOYING_CONFIGS,
    DEPLOYING_ARTIFACTS,
    DEPLOYING_MODULES,
    UNKNOWN,
    FAILED,
    SUCCESS, WAITING_FOR_REGISTER;
}