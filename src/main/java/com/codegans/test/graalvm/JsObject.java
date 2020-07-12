package com.codegans.test.graalvm;

import com.oracle.truffle.js.lang.JavaScriptLanguage;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class JsObject implements ProxyObject {
    private static final ThreadLocal<WeakHashMap<String, Object>> PROTOTYPES = new ThreadLocal<>();
    protected static final String JS = JavaScriptLanguage.ID;

    protected final Context context;
    protected final Value constructor;
    protected final Map<String, Supplier<Object>> internal = new HashMap<>();
    protected final Map<String, Value> values = new HashMap<>();

    public JsObject() {
        this("Object");
    }

    protected JsObject(String constructorName) {
        Context current = Context.getCurrent();

        if (current == null) {
            throw new IllegalCallerException("Expected to be called within GraalVM Context");
        }

        this.context = current;
        this.constructor = current.getBindings(JS).getMember(constructorName);

        WeakHashMap<String, Object> map = PROTOTYPES.get();

        if (map == null) {
            map = new WeakHashMap<>();
            PROTOTYPES.set(map);
        }

        internal.put("__proto__", this::proto);
        internal.put("constructor", () -> constructor);
        internal.put("toString", () -> (ProxyExecutable) args -> this.toString());
    }

    protected Value proto() {
        return this.constructor.getMember("prototype");
    }

    @Override
    public Object getMember(String key) {
        if (internal.containsKey(key)) {
            return internal.get(key).get();
        }

        return values.get(key);
    }

    @Override
    public Object getMemberKeys() {
        return new ArrayList<>(values.keySet());
    }

    @Override
    public boolean hasMember(String key) {
        return internal.containsKey(key) || values.containsKey(key);
    }

    @Override
    public void putMember(String key, Value value) {
        if (!internal.containsKey(key)) {
            values.put(key, value);
        }
    }

    @Override
    public boolean removeMember(String key) {
        return internal.containsKey(key) || values.remove(key) != null;
    }

    @Override
    public String toString() {
        return values.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining(", ", "{", "}"));
    }
}
