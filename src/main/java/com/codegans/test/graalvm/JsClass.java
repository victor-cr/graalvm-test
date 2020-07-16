package com.codegans.test.graalvm;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyInstantiable;

public class JsClass extends JsFunction implements ProxyInstantiable {
    private final ProxyInstantiable fn;

    public JsClass(String className, int length, ProxyInstantiable fn) {
        super(className, length, fn::newInstance);

        this.fn = fn;

        internal.put("prototype", this::prototype);
    }

    protected Object prototype() {
        return this.constructor;
    }

    @Override
    public Object newInstance(Value... arguments) {
        return fn.newInstance(arguments);
    }
}
