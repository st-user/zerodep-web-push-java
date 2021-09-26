package com.zerodeplibs.webpush.key;

import com.zerodeplibs.webpush.WebPushRuntimeWrapperException;
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

    static byte[] readAllBytes(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new WebPushRuntimeWrapperException(e);
        }
    }

    static String readAsString(Path path, Charset charset) {
        try {
            byte[] bytes = Files.readAllBytes(path);
            return new String(bytes, charset);
        } catch (IOException e) {
            throw new WebPushRuntimeWrapperException(e);
        }
    }
}
