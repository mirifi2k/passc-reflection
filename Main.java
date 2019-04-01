import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Add file path to the arguments.");
            System.exit(1);
        }

        File file = new File(args[0]);
        JarInputStream stream = null;
        URLClassLoader loader;

        String code = "";

        try {
            loader = URLClassLoader.newInstance(new URL[] {new URL("jar:file:" + file + "!/")});
            stream = new JarInputStream(new FileInputStream(file));
            JarEntry entry;

            while ((entry = stream.getNextJarEntry()) != null) {
                /*
                * Skipping non '.class' files.
                */

                if (!entry.getName().endsWith(".class"))
                    continue;
                /*
                * Store the class name and remove .class extension.
                * Then load the class to get its attributes.
                */

                String className = entry.getName().substring(0, entry.getName().length() - 6);
                Class c = loader.loadClass(className);

                /*
                * Check for public, private and protected access modifiers.
                */

                if (Modifier.isPublic(c.getModifiers())) {
                    code += "public";
                } else if (Modifier.isPrivate(c.getModifiers())) {
                    code += "private";
                } else if (Modifier.isProtected(c.getModifiers())) {
                    code += "protected";
                }

                /*
                * Check if is an abstract class.
                */

                if (!c.isInterface() && Modifier.isAbstract(c.getModifiers())) {
                    code += " abstract";
                }

                /*
                * Check if is an interface/simple class.
                */

                if (c.isInterface()) {
                    code += " interface " + className;
                } else {
                    code += " class " + className;
                }

                /*
                * If the class/interface inherits any other class/interface.
                */

                if (c.getSuperclass() != null && !c.getSuperclass().getName().equals("java.lang.Object")) {
                    code += " extends " + c.getSuperclass().getName();
                }

                /*
                * Checking if the class is implementing any interface.
                * Only classes can implement interfaces.
                */

                if (!c.isInterface()) {
                    Class[] interfaces = c.getInterfaces(); // Get the interfaces

                    if (interfaces.length > 0) { // Check if there is any
                        code += " implements ";

                        for (Class i : interfaces) {
                            code += i.getName() + ", ";
                        }

                        /*
                        * Remove the additional comma and space.
                        */
                        code += "\b\b";
                    }
                }
                code += "\n";

                /*
                * Get the class fields.
                */

                code += c.getName() + " has following fields:\n";

                Field[] fields = c.getDeclaredFields();
                for (Field f : fields) {
                    code += "\t> " + f + "\n";
                }

                /*
                 * Get the class constructors.
                 */

                code += c.getName() + " has following constructors:\n";

                Constructor[] constructors = c.getDeclaredConstructors();
                for (Constructor ct : constructors) {
                    code += "\t> " + ct + "\n";
                }

                /*
                 * Get the class methods.
                 */

                code += c.getName() + " has following methods:\n";

                Method[] methods = c.getDeclaredMethods();
                for (Method m : methods) {
                    int modifiers = m.getModifiers();
                    code += "\t> ";

                    if (Modifier.isProtected(modifiers)) {
                        code += "protected ";
                    } else if (Modifier.isPrivate(modifiers)) {
                        code += "private ";
                    } else if (Modifier.isPublic(modifiers)) {
                        code += "public ";
                    }

                    if (Modifier.isAbstract(modifiers)) {
                        code += "abstract ";
                    }
                    if (Modifier.isFinal(modifiers)) {
                        code += "final ";
                    }
                    if (Modifier.isStatic(modifiers)) {
                        code += "static ";
                    }

                    code += m.getReturnType().getName() + " " + m.getName() + "(";

                    /*
                    * Get the method parameters.
                    */

                    Parameter[] parameters = m.getParameters();

                    for (int i = 0, j = parameters.length; i < j; i++) {
                        code += parameters[i].getType().getName() + " " + parameters[i].getName() + ", ";
                    }

                    if (parameters.length > 0)
                        code += "\b\b";

                    code += ")\n";
                }

                code += "\n";
            }

            stream.close();
        }
        catch (FileNotFoundException ex) {
            System.out.println("Invalid path given.");
            System.exit(1);
        }
        catch (IOException | ClassNotFoundException | NullPointerException ex) {
            ex.printStackTrace();
        } finally {
            System.out.println(code);
        }
    }
}
