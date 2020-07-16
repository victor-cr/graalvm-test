package com.codegans.test.graalvm;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;

import java.util.function.Function;

public class JsFunction extends JsObject implements ProxyExecutable {
    private final String name;
    private final int length;
    private final ProxyExecutable fn;

    public JsFunction(String name, int length, ProxyExecutable fn) {
        super("Function");

        context.getBindings(JS).putMember(name, this);

        this.name = name;
        this.length = length;
        this.fn = fn;

        internal.put("name", () -> this.name);
        internal.put("length", () -> this.length);
    }

    protected JsFunction(String constructorName, String name, int length, ProxyExecutable fn) {
        super(constructorName);
        this.name = name;
        this.length = length;
        this.fn = fn;
    }

    public JsFunction(JsFunction constructor, String name, int length, ProxyExecutable fn) {
        super(constructor);
        this.name = name;
        this.length = length;
        this.fn = fn;
    }

    public JsFunction(Function<Context, Value> constructorFn, String name, int length, ProxyExecutable fn) {
        super(constructorFn);
        this.name = name;
        this.length = length;
        this.fn = fn;
    }

    @Override
    public Object execute(Value... arguments) {
        return fn.execute(arguments);
    }
}
