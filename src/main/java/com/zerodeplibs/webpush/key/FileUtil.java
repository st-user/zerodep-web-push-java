package com.zerodeplibs.webpush.key;

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

    static byte[] readAllBytes(Path path) throws IOException {
        return Files.readAllBytes(path);
    }

    static String readAsString(Path path, Charset charset) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes, charset);
    }
}
