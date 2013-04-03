/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sii.intlyon.restmi;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.*;
import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.springframework.stereotype.Service;
import javax.tools.Diagnostic.Kind;

//@SupportedAnnotationTypes("javax.ws.rs.Path")
@SupportedAnnotationTypes("org.springframework.stereotype.Service")
//@SupportedAnnotationTypes("org.springframework.stereotype.Controller")
public class RestMIProcessor extends AbstractProcessor {

    private ProcessingEnvironment processingEnv;
    private Messager messager;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.processingEnv = processingEnv;
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!annotations.isEmpty()) {
            round(annotations, roundEnv);
        }
        return true;
    }

    /**
     * Perform a round of processing
     */
    private void round(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        TypeElement genClassAnno = annotations.iterator().next();
        Set<? extends Element> annotatedEls = roundEnv.getElementsAnnotatedWith(genClassAnno);
        for (Element e : annotatedEls) {
            Service service = e.getAnnotation(Service.class);
            generateType(service, e);
        }
    }

    /**
     * @param genResourceMirror
     */
    private void generateType(Service service, Element annotatedEl) {
        // Collect and validate the parameters of the annotation
        String pkg = "";
        String relativeName;
        String stringContent = "Essai";
        byte[] binaryContent;
        messager.printMessage(Diagnostic.Kind.WARNING, annotatedEl.getEnclosingElement().getSimpleName(), annotatedEl);
        for (Element element : annotatedEl.getEnclosedElements()) {
            messager.printMessage(Diagnostic.Kind.NOTE, element.getSimpleName(), annotatedEl);
        }
        try {
            relativeName = annotatedEl.getSimpleName().toString();
            if (annotatedEl instanceof TypeElement) {
                pkg = ((TypeElement) annotatedEl).getQualifiedName().toString();
            }
//			pkg = genResourceMirror.pkg();
//			relativeName = genResourceMirror.relativeName();
//			stringContent = genResourceMirror.stringContent();
//			binaryContent = genResourceMirror.binaryContent();
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.WARNING, "Unable to read @GenResource annotation" + e.getLocalizedMessage(), annotatedEl);
            return;
        }
        if (relativeName.length() == 0) {
            // User hasn't specified relativeName yet
            messager.printMessage(Diagnostic.Kind.WARNING, "The relativeName attribute is missing", annotatedEl);
            return;
        }

        FileObject fo = null;
        try {
            messager.printMessage(Diagnostic.Kind.NOTE, "pkg = '" + pkg + "'", annotatedEl);
            messager.printMessage(Diagnostic.Kind.NOTE, "relativeName = '" + relativeName + "'", annotatedEl);
            fo = filer.createResource(StandardLocation.SOURCE_OUTPUT, pkg, relativeName, annotatedEl);
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.WARNING,
                    "Unable to open resource file for pkg " + pkg + ", relativeName "
                    + relativeName + ": " + e.getLocalizedMessage(), annotatedEl);
            return;
        }
        if (null == fo) {
            messager.printMessage(Diagnostic.Kind.WARNING, "Filer.createResource() returned null", annotatedEl);
            return;
        }
//		if (stringContent.isEmpty()) {
//			// Binary content.  Open an OutputStream.
//			OutputStream os = null;
//			try {
//				os = fo.openOutputStream();
//				os.write(binaryContent);
//			}
//			catch (Exception e) {
//				messager.printMessage(Kind.ERROR, e.getLocalizedMessage(), annotatedEl);
//				return;
//			}
//			finally {
//				try {
//					os.close();
//				} catch (IOException e) {
//					messager.printMessage(Kind.ERROR, e.getLocalizedMessage(), annotatedEl);
//				}
//			}
//		}
//		else {
        // String content.  Open a Writer.
        Writer w = null;
        try {
            w = fo.openWriter();
            w.write(stringContent);
        } catch (Exception e) {
            messager.printMessage(Kind.ERROR, e.getLocalizedMessage(), annotatedEl);
            return;
        } finally {
            try {
                w.close();
            } catch (IOException e) {
                messager.printMessage(Kind.ERROR, e.getLocalizedMessage(), annotatedEl);
            }
        }
//		}


    }

    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
