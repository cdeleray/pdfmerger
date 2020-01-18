package com.github.cdeleray.pdfmerger;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.stream.Collectors.toList;

/**
 * A {@code PdfMerger} object is designed to merge several PDF files into a
 * single one.
 *
 * @author Christophe Deleray
 */
public interface PdfMerger {
  /**
   * Dumps out the given output stream the bytes that correspond to the 
   * single PDF that results of merging all given PDF data.
   * 
   * @param pdfs a sequence of PDF content
   * @param out an output stream where the PDF will be dumped out
   * @throws PdfMergeException if an error occurs when merging the PDF content
   */
  void merge(Collection<byte[]> pdfs, OutputStream out);

  /**
   * Merges all given PDF content as a single one file designed by {@code dest}.
   *
   * @implSpec
   * The default implementation starts by creating an output stream from
   * the {@code dest} file. Then, it calls the {@link #merge(Collection, OutputStream)}
   * method.
   *
   * @param pdfs the PDF files to merge in a single one
   * @param dest the final file that results of merging all given PDF files
   * @throws PdfMergeException if an error occurs when merging the PDF files
   */
  default void merge(Collection<byte[]> pdfs, Path dest) {
    try {
      merge(pdfs, Files.newOutputStream(dest, CREATE, WRITE, TRUNCATE_EXISTING));
    } catch (IOException e) {
      throw new PdfMergeException(e);
    }
  }

  /**
   * Dumps out the given output stream the bytes that correspond to the
   * single PDF content that results of merging all given PDF files.
   *
   * @implSpec
   * The default implementation calls the {@link #merge(Collection, OutputStream)}
   * method with an array of {@link Path} instances created from the files
   * specified by {@code pdfs}.
   *
   * @param pdfs the PDF files to merge in a single one
   * @param out an output stream where the PDF will be dumped out
   * @throws PdfMergeException if an error occurs when merging the PDF files
   */
  default void mergeFiles(Collection<Path> pdfs, OutputStream out) {
    Function<Path, byte[]> toBytes = path -> {
        try {
          return readAllBytes(path);
        } catch (IOException e) {
          throw new PdfMergeException(e);
        }
    };

    Collection<byte[]> collection = pdfs.stream()
        .filter(Objects::nonNull)
        .filter(Files::exists)
        .filter(Files::isRegularFile)
        .map(toBytes)
        .collect(toList());

    merge(collection, out);
  }

  /**
   * Merges all given PDF files as a single one file designed by {@code dest}.
   *
   * @implSpec
   * The default implementation starts by creating an output stream from
   * the {@code dest} file. Then, it calls the {@link #mergeFiles(Collection, OutputStream)}
   * method.
   *
   * @param pdfs the PDF files to merge in a single one
   * @param dest the final file that results of merging all given PDF files
   * @throws PdfMergeException if an error occurs when merging the PDF files
   */
  default void mergeFiles(Collection<Path> pdfs, Path dest) {
    try {
      mergeFiles(pdfs, Files.newOutputStream(dest, CREATE, WRITE, TRUNCATE_EXISTING));
    } catch (IOException e) {
      throw new PdfMergeException(e);
    }
  }
}