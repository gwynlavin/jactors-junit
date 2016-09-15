package org.jactors.junit.helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Helper class to clone object via serialization.
 */
public abstract class CloneHelper {

    /**
     * Create cloned object for given source object via simple serialization.
     *
     * @param   <Type>  object type.
     * @param   object  source object.
     *
     * @return  cloned object.
     *
     * @throws  IOException             if any I/O problem occurs on the stream.
     * @throws  ClassNotFoundException  if class cannot be found.
     */
    @SuppressWarnings("unchecked")
    public static <Type> Type clone(Type object) throws IOException, ClassNotFoundException {
        CloneHelper.Output cout = null;
        CloneHelper.Input cin = null;
        Queue<Class<?>> queue = new LinkedList<Class<?>>();
        try {
            ByteArrayOutputStream sout = new ByteArrayOutputStream();
            cout = new Output(sout, queue);
            cout.writeObject(object);
            ByteArrayInputStream sin = new ByteArrayInputStream(sout.toByteArray());
            cin = new Input(sin, queue);
            return (Type) cin.readObject();
        } finally {
            if (cout != null) {
                cout.close();
            }
            if (cin != null) {
                cin.close();
            }
        }
    }

    /**
     * Clone object output stream.
     */
    private static final class Output extends ObjectOutputStream {

        /**
         * Serialized class list queue.
         */
        private final Queue<Class<?>> queue;

        /**
         * Create clone object output stream with given delegating output stream and given
         * serialized class list queue.
         *
         * @param   out    delegating output stream.
         * @param   queue  serialized class list queue.
         *
         * @throws  IOException  if creation of object output stream failed.
         */
        protected Output(OutputStream out, Queue<Class<?>> queue) throws IOException {
            super(out);
            this.queue = queue;
        }

        /**
         * {@inheritDoc}
         */
        protected void annotateClass(Class<?> c) {
            this.queue.add(c);
        }

        /**
         * {@inheritDoc}
         */
        protected void annotateProxyClass(Class<?> c) {
            this.queue.add(c);
        }
    }

    /**
     * Clone input output stream.
     */
    private static final class Input extends ObjectInputStream {

        /**
         * Serialized class list queue.
         */
        private final Queue<Class<?>> queue;

        /**
         * Create clone object input stream with given delegating input stream and given
         * serialized class list queue.
         *
         * @param   in     delegating input stream.
         * @param   queue  serialized class list queue.
         *
         * @throws  IOException  if creation of object output stream failed.
         */
        protected Input(InputStream in, Queue<Class<?>> queue) throws IOException {
            super(in);
            this.queue = queue;
        }

        /**
         * {@inheritDoc}
         */
        protected Class<?> resolveClass(ObjectStreamClass oclazz) throws IOException {
            Class<?> clazz = this.queue.poll();
            String actual = (clazz == null) ? null : clazz.getName();
            if (!oclazz.getName().equals(actual)) {
                throw new InvalidClassException("invalid class [expected=" + oclazz.getName() //
                    + ", actual=" + actual + "]");
            }
            return clazz;
        }

        /**
         * {@inheritDoc}
         */
        protected Class<?> resolveProxyClass(String[] ifaces) {
            return this.queue.poll();
        }
    }
}