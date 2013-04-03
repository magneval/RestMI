/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sii.intlyon.restmi;

import java.io.Writer;
import java.util.*;
import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import org.springframework.stereotype.Service;

//@SupportedAnnotationTypes("javax.ws.rs.Path")
@SupportedAnnotationTypes("org.springframework.stereotype.Service")
//@SupportedAnnotationTypes("org.springframework.stereotype.Controller")
public class RestMIProcessor2 extends AbstractProcessor {

    private ProcessingEnvironment processingEnv;
    private Messager messager;
    private Filer filer;
    private String className;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.processingEnv = processingEnv;
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //processingEnv is a predefined member in AbstractProcessor class
        //Messager allows the processor to output messages to the environment
//        Messager messager = processingEnv.getMessager();

        //Create a hash table to hold the option switch to option bean mapping
        HashMap<String, String> values = new HashMap<String, String>();

        //Loop through the annotations that we are going to process
        //In this case there should only be one: Option
        for (TypeElement te : annotations) {

            //Get the members that are annotated with Option
            for (Element e : roundEnv.getElementsAnnotatedWith(te)) //Process the members. processAnnotation is our own method
            {
                processAnnotation(e, values, messager);
            }
        }

        //If there are any annotations, we will proceed to generate the annotation
        //processor in generateOptionProcessor method
        if (values.size() > 0) {
            try {
                //Generate the option process class
                generateOptionProcessor(processingEnv.getFiler(), values);
            } catch (Exception e) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            }
        }

        return true;
    }

    private void processAnnotation(Element element, HashMap<String, String> values, Messager msg) {

        //Get the Option annotation on the member
        Service service = element.getAnnotation(Service.class);
        //Get the class name of the option bean
        className = element.getEnclosingElement().toString();
        className = element.getClass().getSimpleName();

        //Check if the type in the member is a String. If not we igonre it
        //We are currently only supporting String type
//        if (!element.asType().toString().equals(String.class.getName())) {
//            msg.printMessage(Diagnostic.Kind.WARNING, element.asType() + " not supported. " + service.value() + " not processed");
//            return;
//        }

        //Save the option switch and the member's name in a hash set
        //Eg. -filename (option switch) mapped to fileName (member)
        values.put(service.getClass().getSimpleName(), element.getSimpleName().toString());
    }

    private void generateOptionProcessor(Filer filer, HashMap<String, String> values) throws Exception {

        String generatedClassName = className + "Processor";

        Writer writer = filer.createSourceFile(generatedClassName).openWriter();

        writer.write("/* Generated on " + new Date() + " */\n");

        writer.write("public class " + generatedClassName + " {\n");

        writer.write("\tpublic static " + className + " process(String[] args) {\n");

        writer.write("\t\t" + className + " options = new " + className + "();\n");
        writer.write("\t\tint idx = 0;\n");

        writer.write("\t\twhile (idx < args.length) {\n");

        for (String key : values.keySet()) {
            writer.write("\t\t\tif (args[idx].equals(\"" + key + "\")) {\n");
            writer.write("\t\t\t\toptions." + values.get(key) + " = args[++idx];\n");
            writer.write("\t\t\t\tidx++;\n");
            writer.write("\t\t\t\tcontinue;\n");
            writer.write("\t\t\t}\n");
        }

        writer.write("\t\t\tSystem.err.println(\"Unknown option: \" + args[idx++]);\n");

        writer.write("\t\t}\n");

        writer.write("\t\treturn (options);\n");
        writer.write("\t}\n");

        writer.write("}");

        writer.flush();
        writer.close();
    }
    /* Generated on Tue Apr 11 17:03:41 SGT 2006 */

//    public class FileTransferOptionsProcessor {
//
//        public static FileTransferOptions process(String[] args) {
//            FileTransferOptions options = new FileTransferOptions();
//            int idx = 0;
//            while (idx < args.length) {
//                if (args[idx].equals("-filename")) {
//                    options.fileName = args[++idx];
//                    idx++;
//                    continue;
//                }
//                if (args[idx].equals("-server")) {
//                    options.server = args[++idx];
//                    idx++;
//                    continue;
//                }
//                System.err.println("Unknown option: " + args[idx++]);
//            }
//            return (options);
//        }
//    }

    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
