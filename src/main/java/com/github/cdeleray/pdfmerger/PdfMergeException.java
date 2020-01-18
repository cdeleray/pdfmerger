package com.github.cdeleray.pdfmerger;

/**
 * A {@code PdfMergeException} represents an error that mays occurs when
 * merging PDF files.
 *
 * @author Christophe Deleray
 */
public class PdfMergeException extends RuntimeException {
  /**
   * Creates new {@link PdfMergeException} from the given cause.
   *
   * @param cause the underlying cause of this exception
   */
  public PdfMergeException(Throwable cause) {
    super(cause);
  }
}
