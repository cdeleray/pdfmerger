package fr.cdy.pdf;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfCopyFields;
import com.lowagie.text.pdf.PdfReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A {@code OpenPdfMerger} object is the <a href="https://github.com/LibrePDF/OpenPDF">
 * OpenPDF</a>-based implementation of a {@link PdfMerger}.
 *
 * @author Christophe Deleray
 */
public class OpenPdfMerger implements PdfMerger {
  @Override
  public void merge(Collection<byte[]> pdfs, OutputStream out) {
    ByteArrayOutputStream output = new ByteArrayOutputStream(1<<21); //will avoid to close 'out'

    PdfCopyFields pdfCopyFields;
    try {
      pdfCopyFields = new PdfCopyFields(output);
    } catch (DocumentException e) {
      throw new PdfMergeException(e);
    }

    Consumer<byte[]> addData = data -> {
      try {
        pdfCopyFields.addDocument(new PdfReader(data));
      } catch (IOException | DocumentException e) {
        throw new PdfMergeException(e);
      }
    };
 
    Runnable dumpBytes = () -> {
      try {
        out.write(output.toByteArray());
        out.flush();
      } catch (IOException e) {
        throw new PdfMergeException(e);
      }
    };
    
    try(Stream<byte[]> stream = pdfs.stream()) {
      stream.filter(Objects::nonNull)
            .filter(data -> data.length > 0)
            .onClose(pdfCopyFields::close)
            .onClose(dumpBytes)
            .forEach(addData);
    }
  }
}