/*
 * MIT License
 *
 * Copyright (c) 2020 Christophe Deleray
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

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfCopyFields;
import com.lowagie.text.pdf.PdfReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A {@code OpenPdfMerger} object is the <a href="https://github.com/LibrePDF/OpenPDF">
 * OpenPDF</a>-based implementation of a {@link PdfMerger}.
 *
 * @author Christophe Deleray
 */
public class OpenPdfMerger implements PdfMerger {
    @Override
    public void merge(Collection<byte[]> pdfs, OutputStream out) {
        var output = new ByteArrayOutputStream(1 << 21); //will avoid to close 'out'

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

        try (var stream = pdfs.stream()) {
            stream.filter(Objects::nonNull)
                    .filter(data -> data.length > 0)
                    .onClose(pdfCopyFields::close)
                    .onClose(dumpBytes)
                    .forEach(addData);
        }
    }
}