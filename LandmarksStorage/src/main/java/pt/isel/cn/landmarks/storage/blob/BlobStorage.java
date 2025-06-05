package pt.isel.cn.landmarks.storage.blob;

import com.google.cloud.WriteChannel;

/**
 * This interface represents a BLOB storage system.
 * <p>
 * A BLOB (Binary Large Object) storage is a type of cloud storage
 * meant for storing masses of data in binary form that don't
 * necessarily conform to any file format. This can include images,
 * media files, or any other type of binary data that is too large
 * to be stored in a traditional database.
 * <p>
 * This interface provides methods for uploading, downloading,
 * making public, and checking the existence of blobs in the cloud storage.
 */
public interface BlobStorage {
    /**
     * Uploads a file to the cloud storage.
     *
     * @param bucketName The name of the bucket where the file will be stored.
     * @param blobName The unique identifier for the file.
     * @param contentType The content type of the file (e.g., "image/png").
     * @param data The byte array representing the file data.
     */
    public void upload(String bucketName, String blobName, String contentType, byte[] data);

    /**
     * Downloads a file from the cloud storage.
     *
     * @param bucketName The name of the bucket where the file is stored.
     * @param blobName The unique identifier for the file.
     */
    public byte[] download(String bucketName, String blobName);

    /**
     * Deletes a file from the cloud storage.
     *
     * @param bucketName The name of the bucket where the file is stored.
     * @param blobName The unique identifier for the file.
     */
    public void delete(String bucketName, String blobName);

    /**
     * Makes a file public in the cloud storage.
     * <p>
     * @param bucketName The name of the bucket where the file is stored.
     * @param blobName The unique identifier for the file.
     */
    public void makePublic(String bucketName, String blobName);

    /**
     * Gets the public URL of a file in the cloud storage.
     *
     * @param bucketName The name of the bucket where the file is stored.
     * @param blobName The unique identifier for the file.
     * @return The public URL of the file.
     */
    public String getPublicUrl(String bucketName, String blobName);

    /**
     * Checks if a blob exists in the cloud storage.
     * @param bucketName - The name of the bucket where the file is stored.
     * @param blobName - The unique identifier for the file.
     *
     * @return True if the blob exists, false otherwise.
     */
    public boolean blobExists(String bucketName, String blobName);

    /**
     * Gets a write channel for a blob in the cloud storage.
     *
     * @param bucketName - The name of the bucket where the file is stored.
     * @param blobName - The unique identifier for the file.
     * @param contentType - The content type for the file (e.g., "image/png").
     *
     * @return The write channel for the blob.
     */
    WriteChannel getWriteChannel(String bucketName, String blobName, String contentType);
}
