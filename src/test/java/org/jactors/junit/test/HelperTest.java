package org.jactors.junit.test;

import java.io.IOException;
import java.lang.reflect.Modifier;

import org.jactors.junit.helper.AccessHelper;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Static helper class test.
 */
@Ignore
public abstract class HelperTest extends ParameterTest {

    /**
     * Test parameter.
     */
    @Parameterized.Parameter
    public Class<?> type;

    /**
     * Validate helper class that can only be created by default constructor.
     *
     * @throws IOException when reading of helper class fails.
     */
    @Test
    public void test() throws IOException {
        Assume.assumeTrue(Helper.check(this.type));
        Assert.assertNotNull(Helper.create(this.type));
    }

    /**
     * Helper class creation helper.
     */
    private static abstract class Helper {

        /**
         * Types for empty constructor.
         */
        private static final Class<?>[] TYPES = new Class[] {};

        /**
         * Arguments for empty constructor.
         */
        private static final Object[] ARGS = new Object[] {};

        /**
         * Check whether given class type is really a helper class type. This requires, that it has
         * a default constructor.
         *
         * @param type helper class type.
         * @return whether given class type is a helper class type.
         */
        public static boolean check(Class<?> type) {
            return AccessHelper.Objects.resolve(type, AccessHelper.Failure.Mode.RETURN_NULL, TYPES) != null;
        }

        /**
         * Create object of helper class using default constructor.
         *
         * @param type helper class type.
         * @return created object of helper class.
         *
         * @throws IOException when reading of helper class fails.
         */
        public static Object create(Class<?> type) throws IOException {
            if ((type.getModifiers() & Modifier.ABSTRACT) != 0) {
                type = Helper.type(type);
            }
            return AccessHelper.Objects.create(type, TYPES, ARGS);
        }

        /**
         * Evaluate class type that must be created to test helper class default constructor. This
         * is either the helper class itself or a child class extending the helper class.
         *
         * @param type helper class type.
         * @return class type used for testing helper class type.
         *
         * @throws IOException when reading of helper class fails.
         */
        private static Class<?> type(Class<?> type) throws IOException {
            String owner = type.getName(), name = owner + "$Test";

            Creator creator;
            ClassLoader loader;
            if ((type.getModifiers() & Modifier.PRIVATE) != 0) {
                loader = new Loader(type.getClassLoader());
                byte[] bytes = Helper.extend(owner.replace('.', '/'));
                AccessHelper.Classes.create(loader, owner, bytes);
                creator = new Creator.Private(owner.replace('.', '/'), name.replace('.', '/'));
            } else {
                loader = type.getClassLoader();
                creator = new Creator.Public(owner.replace('.', '/'), name.replace('.', '/'));
            }

            byte[] bytes = creator.create();
            return AccessHelper.Classes.create(loader, name, bytes);
        }

        /**
         * Extend helper class with synthetic constructor that allows access to the default
         * constructor.
         *
         * @param name internal name of helper class.
         * @return byte code representation of helper class.
         *
         * @throws IOException when reading of helper class fails.
         */
        private static byte[] extend(String name) throws IOException {
            ClassReader cr = new ClassReader(name);
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            ClassVisitor cv = new Visitor(cw, name);
            cr.accept(cv, 0);
            return cw.toByteArray();
        }

        /**
         * Helper class visitor to extend helper class with synthetic constructor.
         */
        private static final class Visitor extends ClassVisitor implements Opcodes {
            /**
             * Internal owner class name.
             */
            private final String owner;

            /**
             * Create helper class visitor with given subsequent class visitor and given internal
             * owner class name.
             *
             * @param cv subsequent class visitor.
             * @param owner internal owner class name.
             */
            public Visitor(ClassVisitor cv, String owner) {
                super(ASM5, cv);
                this.owner = owner;
            }

            /**
             * {@inheritDoc}
             */
            public void visitEnd() {
                MethodVisitor mv = this.cv.visitMethod(ACC_SYNTHETIC, "<init>", //
                        "(L" + this.owner + ";)V", null, null);
                mv.visitCode();
                Label start = new Label();
                mv.visitLabel(start);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESPECIAL, this.owner, "<init>", "()V", false);
                mv.visitInsn(RETURN);
                mv.visitMaxs(1, 2);
                mv.visitEnd();
                super.visitEnd();
            }
        }

        /**
         * Helper class type child creator.
         */
        private static abstract class Creator implements Opcodes {
            /**
             * Owner class type name (outer class = helper class).
             */
            protected String owner;

            /**
             * Child class type name (inner class).
             */
            protected String name;

            /**
             * Create helper class type child creator with given owner class type name and given
             * child class type name.
             *
             * @param owner owner class type name (outer class = helper class).
             * @param name child class type name (inner class).
             */
            protected Creator(String owner, String name) {
                this.owner = owner;
                this.name = name;
            }

            /**
             * Create and return byte code of helper class child.
             *
             * @return byte code of helper class child.
             */
            public byte[] create() {
                ClassWriter cw = new ClassWriter(0);
                this.create(cw);
                return cw.toByteArray();
            }

            /**
             * Create helper class using given class visitor.
             *
             * @param cv class visitor.
             */
            protected abstract void create(ClassVisitor cv);

            /**
             * Create helper class constructor using given method visitor.
             *
             * @param mv method visitor.
             */
            protected abstract void create(MethodVisitor mv);

            /**
             * Public helper class type child creator.
             */
            protected static final class Public extends Creator {

                /**
                 * Create public helper class type child creator with given owner class type name
                 * and given child class type name.
                 *
                 * @param owner owner class type name (outer class = helper class).
                 * @param name child class type name (inner class).
                 */
                public Public(String owner, String name) {
                    super(owner, name);
                }

                /**
                 * {@inheritDoc}
                 */
                protected void create(ClassVisitor cv) {
                    cv.visit(V1_6, ACC_SUPER + ACC_FINAL, this.name, null, this.owner, null);
                    cv.visitSource(null, null);
                    int index = this.owner.lastIndexOf('$');
                    if (index >= 0) {
                        cv.visitInnerClass(this.owner, this.owner.substring(0, index),
                                this.owner.substring(index + 1), //
                                ACC_PUBLIC + ACC_STATIC + ACC_ABSTRACT);
                    }
                    cv.visitInnerClass(this.name, this.owner, "Test", //
                            ACC_PRIVATE + ACC_STATIC + ACC_FINAL);
                    this.create(cv.visitMethod(ACC_PRIVATE, "<init>", "()V", null, null));
                    cv.visitEnd();
                }

                /**
                 * {@inheritDoc}
                 */
                protected void create(MethodVisitor mv) {
                    mv.visitCode();
                    Label start = new Label();
                    mv.visitLabel(start);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitMethodInsn(INVOKESPECIAL, this.owner, "<init>", "()V", false);
                    mv.visitInsn(RETURN);
                    Label end = new Label();
                    mv.visitLabel(end);
                    mv.visitLocalVariable("this", "L" + this.name + ";", null, start, end, 0);
                    mv.visitMaxs(1, 1);
                    mv.visitEnd();
                }
            }

            /**
             * Private helper class type child creator.
             */
            protected static final class Private extends Creator {
                /**
                 * Create private helper class type child creator with given owner class type name
                 * and given child class type name.
                 *
                 * @param owner owner class type name (outer class = helper class).
                 * @param name child class type name (inner class).
                 */
                public Private(String owner, String name) {
                    super(owner, name);
                }

                /**
                 * {@inheritDoc}
                 */
                protected void create(ClassVisitor cv) {
                    cv.visit(V1_6, ACC_SUPER + ACC_FINAL, this.name, null, this.owner, null);
                    cv.visitSource(null, null);
                    int index = this.owner.lastIndexOf('$');
                    if (index >= 0) {
                        cv.visitInnerClass(this.owner, this.owner.substring(0, index),
                                this.owner.substring(index + 1), //
                                ACC_PRIVATE + ACC_STATIC + ACC_ABSTRACT);
                    }
                    cv.visitInnerClass(this.name, this.owner, "Test", //
                            ACC_PRIVATE + ACC_STATIC + ACC_FINAL);
                    this.create(cv.visitMethod(ACC_PRIVATE, "<init>", "()V", null, null));
                    cv.visitEnd();
                }

                /**
                 * {@inheritDoc}
                 */
                protected void create(MethodVisitor mv) {
                    mv.visitCode();
                    Label start = new Label();
                    mv.visitLabel(start);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitInsn(ACONST_NULL);
                    mv.visitMethodInsn(INVOKESPECIAL, this.owner, "<init>", //
                            "(L" + this.owner + ";)V", false);
                    mv.visitInsn(RETURN);
                    Label end = new Label();
                    mv.visitLabel(end);
                    mv.visitLocalVariable("this", "L" + this.name + ";", null, start, end, 0);
                    mv.visitMaxs(2, 1);
                    mv.visitEnd();
                }
            }
        }

        /**
         * Test context class loader
         */
        private static final class Loader extends ClassLoader {
            /**
             * Test context class loader.
             *
             * @param loader parent class loader.
             */
            public Loader(ClassLoader loader) {
                super(loader);
            }
        }
    }
}
