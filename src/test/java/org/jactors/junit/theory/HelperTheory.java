package org.jactors.junit.theory;

import java.lang.reflect.Modifier;

import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;

import org.jactors.junit.helper.AccessHelper;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

/**
 *
 */
@Ignore
@RunWith(Theories.class)
public abstract class HelperTheory {

    /**
     * Types for empty constructor.
     */
    private static final Class<?>[] TYPES = new Class[] {};

    /**
     * Arguments for empty constructor.
     */
    private static final Object[] ARGS = new Object[] {};

    /**
     * Validate that helper object can only be created by default constructor.
     *
     * @param type value object class type.
     */
    @Theory(nullsAccepted = false)
    public final void isCreatable(Class<?> type) {
        Assume.assumeNotNull(AccessHelper.Objects.resolve(type,
                AccessHelper.Failure.Mode.RETURN_NULL, TYPES));
        LoggerFactory.getLogger(HelperTheory.class).info("create {}", type);

        Assume.assumeTrue(type == AccessHelper.class);
        if ((type.getModifiers() & Modifier.ABSTRACT) == 0) {
            Assert.assertNotNull(AccessHelper.Objects.create(type, TYPES, ARGS));
        } else {
            Assert.assertNotNull(AccessHelper.Objects.create(Abstracts.load(type), TYPES, ARGS));
        }
    }

    private static class Abstracts implements Opcodes {

        /** */
        private static final String CLASS = HelperTheory.class.getCanonicalName() + "$Test";
        /** */
        private static final String ICLASS = CLASS.replace('.', '/');

        public static <Types> Class<? extends Types> load(Class<Types> type) {
            ClassWriter cw = new ClassWriter(0);
            ClassVisitor cv = cw;// new ClassAdapter(cw);
            Abstracts.create(cv, type.getCanonicalName().replace('.', '/'), ICLASS);
            return AccessHelper.Classes.create(CLASS, cw.toByteArray());
        }

        /**
         * @param cv
         * @param owner
         * @param name
         */
        private static void create(ClassVisitor cv, String owner, String name) {
            cv.visit(V1_6, ACC_PUBLIC + ACC_SUPER, name, null, owner, null);
            cv.visitSource(null, null);
            cv.visitInnerClass(name, owner, "Test", ACC_PUBLIC + ACC_STATIC);
            Abstracts.create(cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null), owner, name);
            cv.visitEnd();
        }

        /**
         * @param mv
         * @param owner
         * @param name
         */
        private static void create(MethodVisitor mv, String owner, String name) {
            mv.visitCode();
            Label start = new Label();
            mv.visitLabel(start);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, owner, "<init>", "()V", false);
            mv.visitInsn(RETURN);
            Label end = new Label();
            mv.visitLabel(end);
            mv.visitLocalVariable("this", "L" + name + ";", null, start, end, 0);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
    }
}
