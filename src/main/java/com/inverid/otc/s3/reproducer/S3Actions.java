// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.inverid.otc.s3.reproducer;

import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryMode;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.File;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

// snippet-start:[s3.java2.s3_actions.main]
public class S3Actions {

    private static S3AsyncClient s3AsyncClient;

    public static S3AsyncClient getAsyncClient() {
        if (s3AsyncClient == null) {
            /*
            The `NettyNioAsyncHttpClient` class is part of the AWS SDK for Java, version 2,
            and it is designed to provide a high-performance, asynchronous HTTP client for interacting with AWS services.
             It uses the Netty framework to handle the underlying network communication and the Java NIO API to
             provide a non-blocking, event-driven approach to HTTP requests and responses.
             */

            SdkAsyncHttpClient httpClient = NettyNioAsyncHttpClient.builder()
                    .maxConcurrency(50)  // Adjust as needed.
                    .connectionTimeout(Duration.ofSeconds(60))  // Set the connection timeout.
                    .readTimeout(Duration.ofSeconds(60))  // Set the read timeout.
                    .writeTimeout(Duration.ofSeconds(60))  // Set the write timeout.
                    .build();

            ClientOverrideConfiguration overrideConfig = ClientOverrideConfiguration.builder()
                    .apiCallTimeout(Duration.ofMinutes(2))  // Set the overall API call timeout.
                    .apiCallAttemptTimeout(Duration.ofSeconds(90))  // Set the individual call attempt timeout.
                    .retryStrategy(RetryMode.STANDARD)
                    .build();

            s3AsyncClient = S3AsyncClient.builder()
                    .httpClient(httpClient)
                    .overrideConfiguration(overrideConfig)
                    .endpointOverride(URI.create("https://obs.eu-nl.otc.t-systems.com"))
                    .forcePathStyle(true)
                    .region(Region.EU_WEST_1)
                    .build();
        }
        return s3AsyncClient;
    }

    /**
     * Uploads a local file to an AWS S3 bucket asynchronously.
     *
     * @param bucketName the name of the S3 bucket to upload the file to
     * @param key        the key (object name) to use for the uploaded file
     * @param objectPath the local file path of the file to be uploaded
     * @return a {@link CompletableFuture} that completes with the {@link PutObjectResponse} when the upload is successful, or throws a {@link RuntimeException} if the upload fails
     */
    public CompletableFuture<PutObjectResponse> uploadLocalFileAsync(String bucketName, String key, File objectPath) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        CompletableFuture<PutObjectResponse> response = getAsyncClient().putObject(objectRequest, AsyncRequestBody.fromFile(objectPath));
        return response.whenComplete((resp, ex) -> {
            if (ex != null) {
                throw new RuntimeException("Failed to upload file", ex);
            }
        });
    }

}