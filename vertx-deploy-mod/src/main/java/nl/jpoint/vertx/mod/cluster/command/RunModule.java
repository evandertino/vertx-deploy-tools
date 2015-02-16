package nl.jpoint.vertx.mod.cluster.command;

import nl.jpoint.vertx.mod.cluster.Constants;
import nl.jpoint.vertx.mod.cluster.request.DeployModuleRequest;
import nl.jpoint.vertx.mod.cluster.request.ModuleRequest;
import nl.jpoint.vertx.mod.cluster.util.LogConstants;
import nl.jpoint.vertx.mod.cluster.util.ProcessUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.PlatformManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * TODO : Vertx homedir should be configurable.
 */
public class RunModule implements Command<ModuleRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(RunModule.class);
    private boolean success = false;

    private final boolean startWithInit;
    private final String vertxHome;

    public RunModule(final PlatformManager platformManager, final JsonObject config) {
        this.startWithInit = (config.containsField("deploy.internal") && !config.getBoolean("deploy.internal"));
        this.vertxHome = config.getString("vertx.home");
    }

    @Override
    public JsonObject execute(final ModuleRequest request) {
        LOG.info("[{} - {}]: Running module {}.", LogConstants.DEPLOY_REQUEST, request.getId().toString(), request.getModuleId());

        if (startWithInit) {
            startWithInit(request);
        } else {
            startFromContainer(request);
        }

        return new JsonObject()
                .putString(Constants.DEPLOY_ID, request.getId().toString())
                .putBoolean(Constants.STATUS_SUCCESS, success);
    }

    private void startFromContainer(final ModuleRequest request) {
        try {
            System.setProperty("jdk.lang.Process.launchMechanism", "fork");
            Process p = Runtime.getRuntime().exec(new String[]{vertxHome + "/bin/vertx", "runmod", request.getModuleId(), "-instances", String.valueOf(((DeployModuleRequest) request).getInstances()), "-conf", vertxHome + "/mods/" + request.getModuleId() + "/config.json"}, null, new File(vertxHome));
            p.getErrorStream().close();
            p.getOutputStream().close();
            ProcessUtils.writePid(request.getModuleId());
            success = true;
        } catch (IOException e) {
            LOG.error("[{} - {}]: Failed to initialize module {}", LogConstants.DEPLOY_REQUEST, request.getId(), request.getModuleId());
        }
    }

    public void startWithInit(final ModuleRequest request) {
        try {
            final Process runProcess = Runtime.getRuntime().exec(new String[]{"/etc/init.d/vertx", "start-module", request.getModuleId(), String.valueOf(((DeployModuleRequest) request).getInstances())});

            runProcess.waitFor();

            int exitValue = runProcess.exitValue();
            if (exitValue == 0) {
                success = true;
            }

            BufferedReader output = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
            String outputLine;
            while ((outputLine = output.readLine()) != null) {
                LOG.info("[{} - {}]: {}", LogConstants.DEPLOY_REQUEST, request.getId(), outputLine);
            }

            if (exitValue != 0) {
                BufferedReader errorOut = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()));
                String errorLine;
                while ((errorLine = errorOut.readLine()) != null) {
                    LOG.error("[{} - {}]: {}", LogConstants.DEPLOY_REQUEST, request.getId(), errorLine);
                }
            }
        } catch (IOException | InterruptedException e) {
            LOG.error("[{} - {}]: Failed to initialize module {}", LogConstants.DEPLOY_REQUEST, request.getId(), request.getModuleId());
        }
    }
}

