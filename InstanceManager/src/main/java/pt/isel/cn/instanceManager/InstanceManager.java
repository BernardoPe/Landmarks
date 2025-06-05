package pt.isel.cn.instanceManager;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.InstanceGroupManagersClient;
import com.google.cloud.compute.v1.ListManagedInstancesInstanceGroupManagersRequest;
import com.google.cloud.compute.v1.Operation;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import com.google.cloud.compute.v1.*;
import pt.isel.cn.landmarks.domain.Config;

public class InstanceManager {
    private static final String PROJECT_ID = Config.PROJECT_ID;
    private static final String ZONE = "europe-southwest1-a";

    private static final String LANDMARKS_APP_INSTANCE_GROUP_NAME = "instance-group-landmarks-app";
    private static final int LANDMARK_APP_INSTANCE_GROUP_MIN_SIZE = 0, LANDMARK_APP_INSTANCE_GROUP_MAX_SIZE = 2;
    private static final String GRPC_SERVER_INSTANCE_GROUP_NAME = "instance-group-landmarks-server";
    private static final int GRPC_SERVER_INSTANCE_GROUP_MIN_SIZE = 0, GRPC_SERVER_INSTANCE_GROUP_MAX_SIZE = 3;

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        try (InstanceGroupManagersClient instanceGroupManager = InstanceGroupManagersClient.create()) {
            int option;
            do {
                option = showMenu();
                switch (option) {
                    case 0 -> listInstances(instanceGroupManager, GRPC_SERVER_INSTANCE_GROUP_NAME);
                    case 1 -> resizeInstances(instanceGroupManager, GRPC_SERVER_INSTANCE_GROUP_NAME, GRPC_SERVER_INSTANCE_GROUP_MIN_SIZE, GRPC_SERVER_INSTANCE_GROUP_MAX_SIZE);
                    case 2 -> listInstances(instanceGroupManager, LANDMARKS_APP_INSTANCE_GROUP_NAME);
                    case 3 -> resizeInstances(instanceGroupManager, LANDMARKS_APP_INSTANCE_GROUP_NAME, LANDMARK_APP_INSTANCE_GROUP_MIN_SIZE, LANDMARK_APP_INSTANCE_GROUP_MAX_SIZE);
                    case 99 -> System.out.println("Exiting...");
                    default -> System.out.println("Invalid option.");
                }
            } while (option != 99);
        }
    }

    private static int showMenu() {
        Scanner scanner = new Scanner(System.in);
        int option;
        do {
            System.out.println("\n########## Landmarks Instance Manager ##########");
            System.out.println(" 0 - List LandmarksServer VMs");
            System.out.println(" 1 - Resize LandmarksServer VMs");
            System.out.println(" 2 - List LandmarksApp VMs");
            System.out.println(" 3 - Resize LandmarksApp VMs");
            System.out.println("99 - Exit");
            System.out.print("Option: ");
            option = scanner.nextInt();
        } while (option < 0 || (option > 3 && option != 99));
        return option;
    }

    private static void listInstances(InstanceGroupManagersClient client, String groupName) {
        ListManagedInstancesInstanceGroupManagersRequest request =
                ListManagedInstancesInstanceGroupManagersRequest.newBuilder()
                        .setProject(PROJECT_ID)
                        .setZone(ZONE)
                        .setInstanceGroupManager(groupName)
                        .setReturnPartialSuccess(true)
                        .build();

        InstanceGroupManagersClient.ListManagedInstancesPagedResponse response = client.listManagedInstances(request);

        if (!response.iterateAll().iterator().hasNext()) {
            System.out.println("No instances found in " + groupName + ".");
            return;
        }

        System.out.println("Instances in " + groupName + ":");
        for (ManagedInstance instance : response.iterateAll())
            System.out.println(" - " + instance.getInstance());
    }

    private static void resizeInstances(InstanceGroupManagersClient client, String groupName, int minSize, int maxSize)
            throws ExecutionException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        int newSize;
        do {
            System.out.printf("New size for %s (%d-%d): ", groupName, minSize, maxSize);
            newSize = scanner.nextInt();
        } while (newSize < minSize || newSize > maxSize);

        OperationFuture<Operation, Operation> future = client.resizeAsync(PROJECT_ID, ZONE, groupName, newSize);
        Operation operation = future.get();

        if (operation.hasError())
            System.out.println("Resize error: " + operation.getError().getErrorsList());
        else
            System.out.println("Operation status: " + operation.getStatus());
    }
}