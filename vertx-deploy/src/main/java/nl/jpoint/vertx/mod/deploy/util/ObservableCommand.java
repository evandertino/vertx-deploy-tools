package nl.jpoint.vertx.mod.deploy.util;

import io.vertx.rxjava.core.Vertx;
import nl.jpoint.vertx.mod.deploy.request.ModuleRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.io.*;
import java.util.Map;

import static rx.Observable.just;

public class ObservableCommand<R extends ModuleRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(ObservableCommand.class);
    private static final Long POLLING_INTERVAL_IN_MS = 500L;
    private static Process process;
    private final Integer expectedResultCode;
    private final Vertx rxVertx;
    private final R request;

    public ObservableCommand(R request, Integer expectedResultCode, Vertx vertx) {
        this.request = request;
        this.expectedResultCode = expectedResultCode;
        this.rxVertx = vertx;
    }

    public Observable<R> execute(ProcessBuilder builder) {
        return observableCommand(builder)
                .flatMap(x -> waitForExit())
                .flatMap(x -> {
                    if (process.exitValue() != expectedResultCode) {
                        throw new IllegalStateException("Error executing process");
                    }
                    return just(request);
                });
    }

    private Observable<Integer> waitForExit() {
        return rxVertx.timerStream(POLLING_INTERVAL_IN_MS).toObservable()
                .flatMap(x -> pollProcess());
    }

    private Observable<Integer> pollProcess() {
        if (process.isAlive()) {
            return pollProcess();
        } else {
            if (process.exitValue() != expectedResultCode) {
                throw new IllegalStateException("Error while executing process");
            }
            return just(process.exitValue());
        }
    }


    private Observable<String> observableCommand(ProcessBuilder builder) {
        return Observable.create(subscriber -> {
            process = null;
            try {
                builder.directory(new File(System.getProperty("java.io.tmpdir")));
                process = builder.start();
            } catch (IOException e) {
                subscriber.onError(e);
            }
            if (process != null) {
                InputStream stream = process.getInputStream();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        LOG.trace("[{} - {}]: Command output -> '{}'", LogConstants.CONSOLE_COMMAND, request.getId(), line);
                    }
                    process.destroy();
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            } else {
                subscriber.onError(new IllegalStateException("Unable to create process"));
            }
        });
    }
}
