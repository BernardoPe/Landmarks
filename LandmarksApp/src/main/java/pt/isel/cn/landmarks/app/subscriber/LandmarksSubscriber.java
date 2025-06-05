package pt.isel.cn.landmarks.app.subscriber;

import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import pt.isel.cn.landmarks.domain.Config;

/**
 * Class responsible for subscribing to the Google Cloud Pub/Sub topic for landmarks photos.
 * It uses the Subscriber class to listen for messages and process them using a LandmarksProcessor.
 */
public class LandmarksSubscriber {
    private static final String PROJECT_ID = Config.PROJECT_ID;
    private static final String SUBSCRIPTION_ID = "landmarks-photos-sub";

    public void subscribe(LandmarksProcessor subscriber) {
        ProjectSubscriptionName subscriptionName =
                ProjectSubscriptionName.of(PROJECT_ID, SUBSCRIPTION_ID);
        ExecutorProvider executorProvider = InstantiatingExecutorProvider
                .newBuilder()
                .setExecutorThreadCount(1)
                .build();
        Subscriber sub = Subscriber.newBuilder(subscriptionName, new LandmarksReceiver(subscriber))
                .setExecutorProvider(executorProvider)
                .build();
        sub.startAsync().awaitTerminated();
    }
}