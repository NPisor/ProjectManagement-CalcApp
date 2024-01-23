package com.example.projectmanagement_calcapp;


import java.net.URI;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class S3Handler {

    String bucketName = "anchortaskimages";

    public S3Client createConnection(){
        Region region = Region.US_EAST_2;
        S3Client s3Client = S3Client.builder().region(region).endpointOverride(URI.create("https://s3.us-east-2.amazonaws.com"))
                .forcePathStyle(true)
                .build();

        return s3Client;
    }
}
