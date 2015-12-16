package nl.soutenet.vertx.mod.integration.cluster;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.vertx.core.json.JsonObject;
import nl.jpoint.vertx.mod.deploy.request.DeployApplicationRequest;
import nl.jpoint.vertx.mod.deploy.request.DeployRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.fail;

@Ignore
public class ClusterManagerModuleTest {

    private static final Logger LOG = LoggerFactory.getLogger(ClusterManagerModuleTest.class);

    private static final String POST_URI = "http://localhost:6789/deploy/deploy";
    private static final String POST_URI_SITE = "http://localhost:6789/deploy/artifact";
    private static final String POST_URI_MODULE = "http://localhost:6789/deploy/module";

    @Test
    @Ignore
    public void testInvalidDeployModuleCommand() {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost post = new HttpPost(POST_URI);
        try (CloseableHttpResponse response = httpclient.execute(post)) {
            LOG.info("testDeployModuleCommand : Post response status {}", response.getStatusLine().getStatusCode());
            Assert.assertEquals(400, response.getStatusLine().getStatusCode());
        } catch (IOException e) {
            LOG.error("testDeployModuleCommand : {}", e);
            fail();
        }
    }

    @Test
    @Ignore
    public void testDeployModuleCommand() {

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost post = new HttpPost(POST_URI_MODULE);

        JsonObject postData = createDeployCommand();

        ByteArrayInputStream bos = new ByteArrayInputStream(postData.encode().getBytes());
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(bos);
        entity.setContentLength(postData.encode().getBytes().length);
        post.setEntity(entity);

        try (CloseableHttpResponse response = httpclient.execute(post)) {
            LOG.info("testDeployModuleCommand : Post response status {}", response.getStatusLine().getStatusCode());
            Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        } catch (IOException e) {
            LOG.error("testDeployModuleCommand : {}", e);
            fail();
        }
    }

    @Test
    @Ignore
    public void testDeploySiteModuleCommand() {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost post = new HttpPost(POST_URI_SITE);

        JsonObject postData = createDeploySiteCommand2();

        ByteArrayInputStream bos = new ByteArrayInputStream(postData.encode().getBytes());
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(bos);
        entity.setContentLength(postData.encode().getBytes().length);
        post.setEntity(entity);

        try (CloseableHttpResponse response = httpclient.execute(post)) {
            LOG.info("testDeployModuleCommand : Post response status {}", response.getStatusLine().getStatusCode());
            Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        } catch (IOException e) {
            LOG.error("testDeployModuleCommand : {}", e);
            fail();
        }

    }

    @Test
    @Ignore
    public void testDeployAWSCommand() throws JsonProcessingException {

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost post = new HttpPost(POST_URI);

        final ObjectWriter writer = new ObjectMapper().writer();

        List<DeployApplicationRequest> moduleRequests = new ArrayList<>(1);
        moduleRequests.add(new DeployApplicationRequest("nl.malmberg.edubase.utils", "mongo-connector", "1.0.1-SNAPSHOT", 1, false, "zip"));
        DeployRequest request = new DeployRequest(moduleRequests, Collections.EMPTY_LIST, Collections.EMPTY_LIST, false, false, null, null, false);

        String postData = writer.writeValueAsString(request);
        ByteArrayInputStream bos = new ByteArrayInputStream(postData.getBytes());
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(bos);
        entity.setContentLength(postData.getBytes().length);
        post.setEntity(entity);

        try (CloseableHttpResponse response = httpclient.execute(post)) {
            LOG.info("testDeployModuleCommand : Post response status {}", response.getStatusLine().getStatusCode());
            Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        } catch (IOException e) {
            LOG.error("testDeployModuleCommand : {}", e);
            fail();
        }
    }


    private JsonObject createDeployCommand() {
        return new JsonObject()
                .put("group_id", "nl.malmberg.edubase.utils")
                .put("artifact_id", "mongo-connector")
                .put("version", "1.3.0-SNAPSHOT")
                .put("restart", true)
                .put("instances", 1);
    }

    private JsonObject createDeploySiteCommand() {

        return new JsonObject()
                .put("group_id", "nl.malmberg.vooruit.frontend")
                .put("artifact_id", "vooruit")
                .put("version", "1.0.0-SNAPSHOT")
                .put("classifier", "site")
                .put("context", "/var/www/vooruit");
    }

    private JsonObject createDeploySiteCommand2() {

        return new JsonObject()
                .put("group_id", "nl.malmberg.vooruit")
                .put("artifact_id", "static")
                .put("version", "1.2.0-SNAPSHOT")
                .put("classifier", "site")
                .put("context", "/var/www/vooruit-assets");
    }


}