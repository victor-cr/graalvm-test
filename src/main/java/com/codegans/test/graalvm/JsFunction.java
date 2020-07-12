package com.codegans.test.graalvm;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyInstantiable;

public class JsFunction extends JsObject implements ProxyExecutable, ProxyInstantiable {
    private final String name;
    private final int length;

    public JsFunction(String name) {
        super("Function");

        context.getBindings(JS).putMember(name, this);

        this.name = name;
        this.length = 2;

        internal.put("name", () -> this.name);
        internal.put("length", () -> this.length);
        internal.put("prototype", () -> this.constructor);
    }

    protected Object prototype() {
        return this.constructor;
    }

    @Override
    public Object execute(Value... arguments) {
        return null;
    }

    @Override
    public Object newInstance(Value... arguments) {
        return new JsObject();
    }
}
