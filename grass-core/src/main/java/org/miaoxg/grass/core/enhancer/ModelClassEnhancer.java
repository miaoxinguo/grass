package org.miaoxg.grass.core.enhancer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import org.miaoxg.grass.core.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelClassEnhancer {
    
    private static final Logger logger = LoggerFactory.getLogger(ModelClassEnhancer.class);
    
    private static Map<ClassLoader, Context> contextMap = new HashMap<ClassLoader, Context>();
    
    public byte[] enhance(ClassLoader loader, String className) {
        Context context = getContext(loader);
        className = className.replace("/", ".");
        try {
            logger.trace("Attempting to enhance the class - " + className);
            if (! context.isClassLoaded(className)) {
                CtClass ctClass = context.getCtClass(className);
                if (! canEnhance(context, ctClass)) {
                    return null;
                }
                logger.info("Transforming the class - " + className);
                ctClass.defrost();
                createModelMethods(context, ctClass);
                byte[] byteCode = ctClass.toBytecode();
                context.addClass(className);
                return byteCode;
            } else {
                logger.info("Class already enhanced - " + className);
                return null;
            }
        } catch (NotFoundException e) {
            // Can't do much. Just log and ignore
            logger.trace("Failed while transforming the class " + className, e);
        } catch (Exception e) {
            logger.error("Failed while transforming the class " + className, e);
            throw new RuntimeException("Failed while transforming the class " + className, e);
        }
        return null;
    }
    
    public Context getContext(ClassLoader classLoader) {
        Context context = contextMap.get(classLoader);
        Context parent = null;
        if (context == null) {
            if (classLoader != null) {
                if (classLoader.getParent() != null) {
                    parent = getContext(classLoader.getParent()); 
                }
            }
            context = new Context(parent, classLoader);
            contextMap.put(classLoader, context);
        }
        return context;
    }
    
    public boolean canEnhance(String className) {
        Context context = getContext(Thread.currentThread().getContextClassLoader());
        try {
            return canEnhance(context, context.getCtClass(className));
        } catch (Exception e) {
            logger.trace("Error while checking is the class can be enhanced", e);
            return false;
        }
    }
    
    protected boolean canEnhance(Context context, CtClass ctClass) throws IOException, NotFoundException {
        return isExtendingModel(context, ctClass);
    }
    
    private void createModelMethods(Context context, CtClass ctClass) throws CannotCompileException {
        createMethod(context, ctClass, "deleteAll", "int", "String condition", "Object[] paramValues");
        createMethod(context, ctClass, "deleteAll", "int");
        
        createMethod(context, ctClass, "findById", Model.class.getName(), "java.io.Serializable id");
        createMethod(context, ctClass, "findOne", Model.class.getName(), "String condition", "Object[] paramValues");
        createMethod(context, ctClass, "findAll", "java.util.List", "String condition", "Object[] paramValues");
//        createMethod(context, ctClass, "findAll", "java.util.List");
//        createMethod(context, ctClass, "count", "long", "String condition", "Object[] paramValues");
//        createMethod(context, ctClass, "count", "long");
    }
    
    private void createMethod(Context context, CtClass ctClass, String methodName, String returnType, String... arguments) throws CannotCompileException {
        logger.info("Creating the method - " + methodName + " under the class - " + ctClass.getName());
        CtMethod method = null;
        try {
            method = getMethod(context, ctClass, methodName, arguments);
            if (method != null) {
                ctClass.removeMethod(method);
            }
        } catch (NotFoundException e) {
            logger.trace("The method {} doesn't exist, will create...", methodName);
            // Just ignore if the method doesn't exist already
        }
        
        StringWriter writer = new StringWriter();
        writer.append("public static ").append(returnType).append(" ").append(methodName).append("(");
        if (arguments != null && arguments.length > 0) {
            for (int i = 0; i < arguments.length - 1; i++) {
                writer.append(arguments[i]).append(", ");
            }
            writer.append(arguments[arguments.length - 1]);
        }
        writer.append(") {");
        if (! returnType.equals("void")) {
            writer.append("return (" + returnType + ")");
        }
        writer.append(methodName).append("(").append(ctClass.getName()).append(".class");
        if (arguments != null && arguments.length > 0) {
            for (int i = 0; i < arguments.length; i++) {
                writer.append(", ").append(arguments[i].split(" ")[1]);
            }
        }
        writer.append(");}");
        
        logger.debug("Method src - " + writer.toString());
        
        method = CtNewMethod.make(writer.toString(), ctClass);
        ctClass.addMethod(method);
    }
    
    private CtMethod getMethod(Context context, CtClass ctClass, String methodName, String... arguments) throws NotFoundException {
        List<CtClass> paramTypes = new ArrayList<CtClass>();
        if (arguments != null) {
            for (String argument : arguments) {
                paramTypes.add(context.getCtClass(argument.split(" ")[0]));
            }
        }
        
        /*
         * TODO 这里获取CtMethod时候， 如果名为methodName，参数为arguments的方法不存在，抛出异常。
         * 最好是根据方法列表判断一下要动态增加的方法是不是存在， 如果不存在直接返回null
         */
//        CtMethod[] ms = ctClass.getDeclaredMethods();
//        for(CtMethod m : ms){
//            System.out.println(m.getMethodInfo2().getDescriptor());
//        }
        
        return ctClass.getDeclaredMethod(methodName, paramTypes.toArray(new CtClass[0]));
    }
    
    protected boolean isExtendingModel(Context context, CtClass ctClass) throws NotFoundException {
        return getSuperClasses(ctClass).contains(context.getCtClass(Model.class.getName()));
    }
    
    /**
     * Returns the super classes from top to bottom. The {@link Object} class name will always be returned at index 0.
     * 
     * @param className
     * @return
     * @throws IOException
     */
    protected List<CtClass> getSuperClasses(CtClass modelClass) throws NotFoundException {
        List<CtClass> superClasses = new ArrayList<CtClass>();
        CtClass superClass = getSuperClass(modelClass);
        if (superClass != null) {
            superClasses.addAll(getSuperClasses(superClass));
            superClasses.add(superClass);
        }
        return superClasses;
    }
    
    protected CtClass getSuperClass(CtClass modelClass) throws NotFoundException {
        return modelClass.getSuperclass();
    }
    
    public static class Context {
        
        private Context parent;
        
        private Set<String> loadedClasses = new HashSet<String>();
        
        private ClassPool classPool;
        
        public Context(Context parent, ClassLoader loader) {
            this.parent = parent;
            if (parent == null) {
                classPool = ClassPool.getDefault();
            } else {
                classPool =  new ClassPool(parent.classPool);
                classPool.appendClassPath(new LoaderClassPath(loader));
            }
        }

        /**
         * @return the parent
         */
        public Context getParent() {
            return parent;
        }

        /**
         * @param parent the parent to set
         */
        public void setParent(Context parent) {
            this.parent = parent;
        }

        /**
         * @return the loadedClasses
         */
        public Set<String> getLoadedClasses() {
            return loadedClasses;
        }

        /**
         * @param loadedClasses the loadedClasses to set
         */
        public void setLoadedClasses(Set<String> loadedClasses) {
            this.loadedClasses = loadedClasses;
        }

        /**
         * @return the classPool
         */
        public ClassPool getClassPool() {
            return classPool;
        }

        /**
         * @param classPool the classPool to set
         */
        public void setClassPool(ClassPool classPool) {
            this.classPool = classPool;
        }
        
        public boolean isClassLoaded(String className) {
            if (loadedClasses.contains(className)) {
                return true;
            }
            if (parent != null) {
                return parent.isClassLoaded(className);
            }
            return false;
        }
        
        public CtClass getCtClass(String className) throws NotFoundException {
            return classPool.get(className);
        }
        
        public void addClass(String className) {
            loadedClasses.add(className);
        }
    }
}