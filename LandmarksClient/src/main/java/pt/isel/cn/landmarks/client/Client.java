package pt.isel.cn.landmarks.client;

import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.codec.http.HttpResponseStatus;
import io.grpc.stub.StreamObserver;
import landmarks.*;
import pt.isel.cn.landmarks.client.dto.IPsPayload;
import pt.isel.cn.landmarks.client.observers.SubmitPhotoResponseObserver;
import pt.isel.cn.landmarks.domain.Either;
import pt.isel.cn.landmarks.client.error.NoValidIPs;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class Client {
    private static ManagedChannel channel;
    private static LandmarksServiceGrpc.LandmarksServiceBlockingStub blockingStub;
    private static LandmarksServiceGrpc.LandmarksServiceStub noBlockStub;

    private static final Integer BLOCK_SIZE = 4096;
    private static final String GROUP_NAME = "instance-group-landmarks-server";
    private static final String ZONE = "europe-southwest1-a";
    private static final String IP_LOOKUP_URL = "https://europe-west1-cn2425-t1-g06.cloudfunctions.net/funcIPLookup?zone=" + ZONE + "&groupName=" + GROUP_NAME;
    private static final Integer SERVICE_PORT = 8000;

    public static void main(String[] args) {
        connectToService();
//        channel = ManagedChannelBuilder.forAddress("localhost", SERVICE_PORT).usePlaintext().build();
        blockingStub = LandmarksServiceGrpc.newBlockingStub(channel);
        noBlockStub = LandmarksServiceGrpc.newStub(channel);

        Scanner scanner = new Scanner(System.in);
        boolean end = false;
        while (!end) {
            try {
                int option = menu(scanner);
                switch (option) {
                    case 1 -> submitPhoto(scanner);
                    case 2 -> getResults(scanner);
                    case 3 -> getPhotos(scanner);
                    case 99 -> end = true;
                }
            } catch (Exception ex) {
                System.out.println("Execution call Error  !");
                ex.printStackTrace();
            }
        }
    }

    private static void submitPhoto(Scanner scanner) {
        try {
            System.out.print("Enter path to photo file: ");
            String path = scanner.nextLine();

            File file = new File(path);

            if (!file.exists()) {
                System.out.println("File not found: " + path);
                return;
            }

            SubmitPhotoResponseObserver responseObserver = new SubmitPhotoResponseObserver();
            StreamObserver<SubmitPhotoRequest> requestObserver = noBlockStub.submitPhoto(responseObserver);

            String photoName = file.getName().substring(0, file.getName().lastIndexOf('.'));
            String photoHash = calculateFileHash(file);

            requestObserver.onNext(SubmitPhotoRequest.newBuilder()
                    .setMetadata(ImageMetadata.newBuilder()
                            .setName(photoName)
                            .setHash(photoHash)
                            .build()
                    ).build()
            );

            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[BLOCK_SIZE];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    SubmitPhotoRequest request = SubmitPhotoRequest.newBuilder()
                                    .setPhotoChunk(ByteString.copyFrom(buffer, 0, bytesRead))
                                    .build();
                    requestObserver.onNext(request);
                }
                requestObserver.onCompleted();
            }
        } catch (Exception e) {
            System.err.println("Exception in submitPhoto: " + e.getMessage());
        }
    }

    static void getResults(Scanner scanner) {
        System.out.print("ID: ");
        String id = scanner.nextLine();

        SubmitIdentifier request = SubmitIdentifier.newBuilder().setIdentifier(id).build();
        LookupResults results = blockingStub.lookupResults(request);

        List<Landmark> landmarks = results.getLandmarksList();

        if (landmarks.isEmpty()) {
            System.out.println("No landmarks identified.");
        } else {
            System.out.println("Landmarks identified:");
            for (Landmark lm : landmarks) {
                System.out.printf("- %s (%.6f, %.6f) Confidence: %.2f\n",
                        lm.getName(), lm.getLatitude(), lm.getLongitude(), lm.getConfidence());
            }
        }

        if (!results.getMap().isEmpty()) {
            String filename = "map-" + id + ".png";
            if (!Files.exists(Paths.get(filename))) {
                try (FileOutputStream fos = new FileOutputStream(filename)) {
                    fos.write(results.getMap().toByteArray());
                    System.out.println("Map saved as " + filename);
                } catch (IOException e) {
                    System.err.println("Error saving map: " + e.getMessage());
                }
            } else {
                System.out.println("Map already exists: " + filename);
            }
        } else {
            System.out.println("No map available.");
        }
    }

    static void getPhotos(Scanner scanner) {
        System.out.print("Minimum confidence [0.0 - 1.0]: ");
        float threshold = scanner.nextFloat();
        scanner.nextLine();

        ConfidenceThreshold req = ConfidenceThreshold.newBuilder()
                .setConfidenceThreshold(threshold)
                .build();

        GetPhotosResponse response = blockingStub.getPhotos(req);
        List<Photo> photos = response.getPhotosList();

        if (photos.isEmpty()) {
            System.out.println("No photo with confidence >= " + threshold);
        } else {
            System.out.println("Photos found:");
            for (Photo p : photos) {
                System.out.printf("- %s: %s (confidence: %.2f)\n",
                        p.getPhotoName(), p.getLandmarkName(), p.getConfidence());
            }
        }
    }

    private static Either<NoValidIPs, String> getNewIP() {
        try(HttpClient httpClient = HttpClient.newHttpClient()) {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(IP_LOOKUP_URL))
                    .GET()
                    .build();
            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() != HttpResponseStatus.OK.code()) {
                return Either.left(new NoValidIPs());
            }

            String response = httpResponse.body();
            IPsPayload ips = new Gson().fromJson(response, IPsPayload.class);

            if (ips != null && !ips.ips().isEmpty()) {

                System.out.println("Available IPs:");

                for (int i = 0; i < ips.ips().size(); i++) {
                    System.out.printf("%d: %s\n", i + 1, ips.ips().get(i));
                }

                System.out.print("Choose an IP by number: ");
                Scanner scanner = new Scanner(System.in);

                int choice = -1;
                while (choice < 1 || choice > ips.ips().size()) {
                    try {
                        choice = Integer.parseInt(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.print("Invalid input. Choose an IP by number: ");
                    }
                }

                return Either.right(ips.ips().get(choice - 1));
            }

            return Either.left(new NoValidIPs());
        } catch (IOException | InterruptedException e) {
            System.err.println("Error getting new IP: " + e.getMessage());
            return Either.left(new NoValidIPs());
        }
    }

    private static void connectToService() {
        boolean connected = false;

        while (!connected) {
            Either<NoValidIPs, String> ipResult = getNewIP();

            if (ipResult.isLeft()) {
                System.out.println("No valid IPs available. Retrying...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                continue;
            }

            channel = ManagedChannelBuilder.forAddress(ipResult.getRight(), SERVICE_PORT)
                    .usePlaintext()
                    .build();

            ConnectivityState state = channel.getState(true);

            while (state != ConnectivityState.READY) {
                System.out.println("Connection state: " + state);
                final CountDownLatch latch = new CountDownLatch(1);
                channel.notifyWhenStateChanged(state, latch::countDown);
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                state = channel.getState(true);
            }

            connected = true;
        }
    }

    private static String calculateFileHash(File file) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[BLOCK_SIZE];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            System.err.println("Error calculating file hash: " + e.getMessage());
        }
        return bytesToHex(digest.digest());
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static int menu(Scanner scanner) {
        int op;
        do {
            System.out.println();
            System.out.println("    MENU");
            System.out.println(" 1 - Submit photo");
            System.out.println(" 2 - Lookup results");
            System.out.println(" 3 - Get photos");
            System.out.println(" 99 - Exit");
            System.out.println();
            System.out.println("Choose an Option?");
            op = scanner.nextInt();
            scanner.nextLine();
        } while (!((op >= 1 && op <= 3) || op == 99));
        return op;
    }
}