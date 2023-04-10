/*
 * MIT License
 *
 * Copyright (c) 2023 Christophe Deleray
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.cdeleray.pdfmerger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * A {@code OpenPdfMergerTest} object represents a test class for {@link OpenPdfMerger}.
 *
 * @author Christophe Deleray
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OpenPdfMergerTest {
    private OpenPdfMerger merger;
    private static byte[] content;
    private String viewer;
    private ByteArrayOutputStream out;
    private boolean openPDF;

    private File tempFile() throws IOException {
        var file = File.createTempFile("temp", ".pdf");
        file.deleteOnExit();
        return file;
    }

    private final Function<byte[], Path> toFile = bytes -> {
        try {
            var file = tempFile().toPath();
            Files.copy(new ByteArrayInputStream(bytes), file, REPLACE_EXISTING);
            return file;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    };

    @BeforeEach
    void beforeMethod() {
        merger = new OpenPdfMerger();
        out = new ByteArrayOutputStream();
    }

    @BeforeAll
    void beforeClass() throws Exception {
        content = IOUtils.resourceToByteArray("/com/github/cdeleray/pdfmerger/sample.pdf");

        Properties prop = new Properties();
        try (InputStream in = getClass().getResourceAsStream("/placeholder.properties")) {
            prop.load(in);
        }

        viewer = prop.getProperty("acroread");
        openPDF = Boolean.parseBoolean(prop.getProperty("openPDF"));
    }

    private void showPDF() {
        if (!openPDF) {
            return;
        }

        try {
            var file = tempFile();
            FileUtils.copyInputStreamToFile(new ByteArrayInputStream(out.toByteArray()), file);
            new ProcessBuilder(viewer, file.getPath()).start().waitFor();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Test method for {@link OpenPdfMerger#merge(Collection, java.io.OutputStream)}.
     */
    @Test
    public void testMerge() {
        List<byte[]> pdfs = Stream.generate(() -> content.clone())
                .limit(5)
                .collect(toList());

        merger.merge(pdfs, out);

        assertNotEquals(out.toByteArray().length, 0);

        showPDF();
    }

    /**
     * Test method for {@link OpenPdfMerger#merge(Collection, Path)}.
     */
    @Test
    public void testMergePath() throws IOException {
        List<byte[]> pdfs = Stream.generate(() -> content.clone())
                .limit(5)
                .collect(toList());

        var dest = tempFile().toPath();
        merger.merge(pdfs, dest);

        FileUtils.copyFile(dest.toFile(), out);
        assertNotEquals(out.toByteArray().length, 0);

        showPDF();
    }

    /**
     * Test method for {@link OpenPdfMerger#mergeFiles(Collection, OutputStream)}.
     */
    @Test
    public void testMergeFiles() {
        List<Path> pdfs = Stream.generate(() -> content.clone())
                .map(toFile)
                .limit(5).collect(toList());

        merger.mergeFiles(pdfs, out);

        assertNotEquals(out.toByteArray().length, 0);

        showPDF();
    }

    /**
     * Test method for {@link OpenPdfMerger#mergeFiles(Collection, Path)}.
     */
    @Test
    public void testMergeFilesPath() throws IOException {
        Path dest = tempFile().toPath();
        List<Path> pdfs = Stream.generate(() -> content.clone())
                .map(toFile)
                .limit(5)
                .collect(toList());

        merger.mergeFiles(pdfs, dest);

        FileUtils.copyFile(dest.toFile(), out);
        assertNotEquals(out.toByteArray().length, 0);

        showPDF();
    }
}