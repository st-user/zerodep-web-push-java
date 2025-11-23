package com.zerodeplibs.webpush.key;

import com.zerodeplibs.webpush.internal.WebPushPreConditions;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The internal utility class for handling files.
 *
 * @author Tomoki Sato
 */
class FileUtil {

    private FileUtil() {
    }

    static byte[] readAllBytes(Path path) throws IOException {
        WebPushPreConditions.checkNotNull(path, "path");
        return Files.readAllBytes(path);
    }

    static String readAsString(Path path, Charset charset) throws IOException {
        WebPushPreConditions.checkNotNull(path, "path");
        WebPushPreConditions.checkNotNull(charset, "charset");

        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes, charset);
    }
}
