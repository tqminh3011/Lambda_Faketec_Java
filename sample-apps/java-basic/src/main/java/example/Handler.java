package example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionAsyncClient;
import software.amazon.awssdk.services.rekognition.model.CreateFaceLivenessSessionRequest;
import software.amazon.awssdk.services.rekognition.model.CreateFaceLivenessSessionResponse;
import software.amazon.awssdk.services.rekognition.model.GetFaceLivenessSessionResultsRequest;
import software.amazon.awssdk.services.rekognition.model.GetFaceLivenessSessionResultsResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

// Handler value: example.Handler
public class Handler implements RequestHandler<Map<String,String>, Void>{

  private RekognitionAsyncClient rekognitionClient;
  private LambdaLogger logger;

  @Override
  public Void handleRequest(Map<String,String> event, Context context) {
    logger = context.getLogger();
    logger.log("EVENT TYPE: " + event.getClass());

    rekognitionClient = RekognitionAsyncClient.builder()
            .credentialsProvider(DefaultCredentialsProvider.create())
            .region(Region.EU_WEST_1)
            .build();

    try {
      String sessionId = createSession();
      logger.log("Created a Face Liveness Session with ID: " + sessionId);

      String status = getSessionResults(sessionId);
      logger.log("Status of Face Liveness Session: " + status);

    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

    private String createSession() throws ExecutionException, InterruptedException {

      CreateFaceLivenessSessionRequest request = CreateFaceLivenessSessionRequest.builder().build();
      CompletableFuture<CreateFaceLivenessSessionResponse> result = rekognitionClient.createFaceLivenessSession(request);

      String sessionId = result.get().sessionId();
      logger.log("SessionId: " + sessionId);

    return sessionId;
  }

    private String getSessionResults(String sessionId) throws ExecutionException, InterruptedException {

      GetFaceLivenessSessionResultsRequest request = GetFaceLivenessSessionResultsRequest.builder()
              .sessionId(sessionId)
              .build();
      CompletableFuture<GetFaceLivenessSessionResultsResponse> result = rekognitionClient.getFaceLivenessSessionResults(request);

      Float confidence = result.get().confidence();
      String status = result.get().status().toString();

      logger.log("Confidence: " + confidence);
      logger.log("status: " + status);

      return status;
  }
}