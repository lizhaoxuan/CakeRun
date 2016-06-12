package com.cakerun.apt;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Created by lizhaoxuan on 16/6/10.
 */
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class CakeRunProcessor extends AbstractProcessor {

    private Messager messager;
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        ProxyInfo proxyInfo = null;
        try {
            proxyInfo = getProxyInfo(roundEnv);
        } catch (CakeRunException e) {
            error("The use of irregular : %s", e.getMessage());
            e.printStackTrace();
        }

        if (proxyInfo != null) {
            try {
                writeCode(proxyInfo.getApplicationFullName(),
                        proxyInfo.getTypeElement(), proxyInfo.generateApplicationCode());
                writeCode(proxyInfo.getAppInitListFullName(),
                        proxyInfo.getTypeElement(), proxyInfo.generateAppInitListCode());
            } catch (CakeRunException e) {
                error("The use of irregular %s: %s",
                        proxyInfo.getTypeElement(), e.getMessage());
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(AppInit.class.getCanonicalName());
        types.add(AsyncInit.class.getCanonicalName());
        return types;
    }

    private ProxyInfo getProxyInfo(RoundEnvironment roundEnv) throws CakeRunException {
        ProxyInfo proxyInfo = null;
        for (Element element : roundEnv.getElementsAnnotatedWith(AppInit.class)) {
            if (proxyInfo == null) {
                proxyInfo = createProxyInfo(element);
            }
            ExecutableElement executableElement = (ExecutableElement) element;
            if (checkParameter(executableElement)) {
                break;
            }
            String methodName = executableElement.getSimpleName().toString();
            int tag = executableElement.getAnnotation(AppInit.class).tag();
            boolean canSkip = executableElement.getAnnotation(AppInit.class).canSkip();

            AppInitDto appInitDto = new AppInitDto(canSkip, false, methodName, "null", tag);
            proxyInfo.addAppInitDto(appInitDto);
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(AsyncInit.class)) {
            if (proxyInfo == null) {
                proxyInfo = createProxyInfo(element);
            }
            ExecutableElement executableElement = (ExecutableElement) element;
            if (checkParameter(executableElement)) {
                break;
            }
            String methodName = executableElement.getSimpleName().toString();
            int tag = executableElement.getAnnotation(AsyncInit.class).tag();
            boolean canSkip = executableElement.getAnnotation(AsyncInit.class).canSkip();
            String[] packageName = executableElement.getAnnotation(AsyncInit.class).packageName();
            for (String name : packageName) {
                AppInitDto appInitDto = new AppInitDto(canSkip, true, methodName, name, tag);
                proxyInfo.addAppInitDto(appInitDto);
            }
        }
        return proxyInfo;
    }

    private ProxyInfo createProxyInfo(Element element) throws CakeRunException {
        TypeElement classElement = (TypeElement) element
                .getEnclosingElement();
        PackageElement packageElement = elementUtils.getPackageOf(classElement);
        String className = classElement.getSimpleName().toString();
        String supperClass = classElement.getSuperclass().toString();
        if (!supperClass.contains("Application")) {
            throw new CakeRunException("@AppInit or @AsyncInit must be used in Application");
        }
        String packageName = packageElement.getQualifiedName().toString();
        ProxyInfo proxyInfo = new ProxyInfo(packageName, className);
        proxyInfo.setTypeElement(classElement);
        return proxyInfo;
    }


    /**
     * 检查参数列表，方法必须是无参的
     *
     * @return true 无参通过 false 有参不通过
     */
    private boolean checkParameter(ExecutableElement executableElement) {
        List<? extends VariableElement> methodParameters = executableElement.getParameters();
        if (methodParameters.size() != 0) {
            error("@AppInit or AsyncInit methods must be no parameters!!!");
            return true;
        }
        return false;
    }

    private void writeCode(String fullName, TypeElement typeElement, String code) {
        try {
            JavaFileObject jfo = processingEnv.getFiler().createSourceFile(
                    fullName, typeElement);
            Writer writer = jfo.openWriter();
            writer.write(code);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            error("Unable to write injector for type %s: %s",
                    typeElement, e.getMessage());
        }
    }

    private void print(String message) {
        messager.printMessage(Diagnostic.Kind.NOTE, message);
    }

    private void error(String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        messager.printMessage(Diagnostic.Kind.ERROR, message);
    }
}
