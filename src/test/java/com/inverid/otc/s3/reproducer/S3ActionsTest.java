package com.inverid.otc.s3.reproducer;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class S3ActionsTest {

    public static final String OBS_BUCKET_NAME = System.getenv("OBS_BUCKET_NAME");
    public static final String INVERID_LOGO_STATUS_CHECK_BLOB_PNG = "inverid_logo_status_check_blob.png";

    @Test
    void uploadLocalFileAsync() throws IOException {
        var s3Actions = new S3Actions();
        var file = File.createTempFile("tempfile", ".tmp");
        try (var input = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(
                INVERID_LOGO_STATUS_CHECK_BLOB_PNG)).openStream()) {
            var bytes = input.readAllBytes();
            try (var stream = new FileOutputStream(file)) {
                stream.write(bytes);
            }
        }
        file.deleteOnExit();
        assertDoesNotThrow(() -> s3Actions.uploadLocalFileAsync(
                OBS_BUCKET_NAME,
                INVERID_LOGO_STATUS_CHECK_BLOB_PNG,
                file
        ).get()
        );
    }

}