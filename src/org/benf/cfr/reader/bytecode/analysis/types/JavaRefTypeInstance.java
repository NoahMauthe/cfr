package org.benf.cfr.reader.bytecode.analysis.types;

import org.benf.cfr.reader.entities.ConstantPool;

/**
 * Created with IntelliJ IDEA.
 * User: lee
 * Date: 13/07/2012
 * Time: 08:01
 */
public class JavaRefTypeInstance implements JavaTypeInstance {
    private final String className;
    private final ConstantPool cp;

    public JavaRefTypeInstance(String className, ConstantPool cp) {
        this.className = className;
        this.cp = cp;
        cp.markClassNameUsed(className);
    }

    @Override
    public StackType getStackType() {
        return StackType.REF;
    }

    @Override
    public String toString() {
        return cp.getDisplayableClassName(className);
    }

    @Override
    public JavaTypeInstance getArrayStrippedType() {
        return this;
    }

    @Override
    public int getNumArrayDimensions() {
        return 0;
    }

    @Override
    public int hashCode() {
        return 31 + className.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof JavaRefTypeInstance)) return false;
        JavaRefTypeInstance other = (JavaRefTypeInstance) o;
        return other.className.equals(className);
    }

    @Override
    public boolean isComplexType() {
        return true;
    }

    @Override
    public boolean isUsableType() {
        return true;
    }

    @Override
    public JavaTypeInstance removeAnArrayIndirection() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RawJavaType getRawTypeOfSimpleType() {
        return RawJavaType.REF;
    }
}