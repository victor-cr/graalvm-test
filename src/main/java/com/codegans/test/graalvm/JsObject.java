package com.codegans.test.graalvm;

import com.oracle.truffle.js.lang.JavaScriptLanguage;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.TypeLiteral;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class JsObject implements ProxyObject {
    private static final ThreadLocal<WeakHashMap<String, Object>> PROTOTYPES = new ThreadLocal<>();
    private static final TypeLiteral<Supplier<?>> SUPPLIER_TYPE = new TypeLiteral<>() {
    };
    private static final TypeLiteral<Consumer<Value>> CONSUMER_TYPE = new TypeLiteral<>() {
    };
    protected static final String JS = JavaScriptLanguage.ID;

    protected final Context context;
    protected final Value constructor;
    protected final Map<String, Supplier<Object>> internal = new HashMap<>();
    protected final Map<String, Supplier<?>> getters = new HashMap<>();
    protected final Map<String, Consumer<Value>> setters = new HashMap<>();
    protected final Map<String, Value> values = new HashMap<>();

    public JsObject() {
        this("Object");
    }

    protected JsObject(String constructorName) {
        this(c -> c.getBindings(JS).getMember(constructorName));
    }

    protected JsObject(JsFunction constructor) {
        this(c -> c.asValue(constructor));
    }

    protected JsObject(Function<Context, Value> constructorFn) {
        Context current = Context.getCurrent();

        if (current == null) {
            throw new IllegalCallerException("Expected to be called within GraalVM Context");
        }

        this.context = current;
        this.constructor = constructorFn.apply(current);

        WeakHashMap<String, Object> map = PROTOTYPES.get();

        if (map == null) {
            map = new WeakHashMap<>();
            PROTOTYPES.set(map);
        }

        internal.put("__defineGetter__", () -> (ProxyExecutable) args -> {
            this.defineGetter(args[0].asString(), args[1].as(SUPPLIER_TYPE));
            return null;
        });
        internal.put("__defineSetter__", () -> (ProxyExecutable) args -> {
            this.defineSetter(args[0].asString(), args[1].as(CONSUMER_TYPE));
            return null;
        });
        internal.put("__lookupGetter__", () -> (ProxyExecutable) args -> {
            Supplier<?> fn = this.getters.get(args[0].asString());
            return fn == null ? null : ((ProxyExecutable) a -> fn.get());
        });
        internal.put("__lookupSetter__", () -> (ProxyExecutable) args -> {
            Consumer<Value> fn = this.setters.get(args[0].asString());
            return fn == null ? null : ((ProxyExecutable) a -> {
                fn.accept(a[0]);
                return null;
            });
        });
        internal.put("constructor", () -> constructor);
        internal.put("hasOwnProperty", () -> (ProxyExecutable) args -> this.hasOwnProperty(args[0].asString()));
        internal.put("isPrototypeOf", () -> (ProxyExecutable) args -> this.isPrototypeOf(args[0]));
        internal.put("propertyIsEnumerable", () -> (ProxyExecutable) args -> this.propertyIsEnumerable(args[0].asString()));
        internal.put("toLocalString", () -> (ProxyExecutable) args -> this.toLocalString());
        internal.put("toString", () -> (ProxyExecutable) args -> this.toString());
        internal.put("valueOf", () -> (ProxyExecutable) args -> this);

        defineGetter("__proto__", this::proto);
        defineSetter("__proto__", this::proto);
    }

    protected void defineGetter(String name, Supplier<?> getter) {
        getters.put(name, getter);
    }

    protected void defineSetter(String name, Consumer<Value> setter) {
        setters.put(name, setter);
    }

    protected Value proto() {
        return this.constructor.getMember("prototype");
    }

    protected void proto(Value obj) {
    }

    protected boolean hasOwnProperty(String name) {
        return false;
    }

    protected boolean isPrototypeOf(Value constructor) {
        return this.constructor.isMetaInstance(constructor)
                || constructor.isProxyObject() && constructor.asProxyObject() instanceof JsObject;
    }

    protected boolean propertyIsEnumerable(String name) {
        return false;
    }

    protected String toLocalString() {
        return toString();
    }

    @Override
    public Object getMember(String key) {
        if (internal.containsKey(key)) {
            return internal.get(key).get();
        }

        if (getters.containsKey(key)) {
            return getters.get(key).get();
        }

        return values.get(key);
    }

    @Override
    public Object getMemberKeys() {
        return new ArrayList<>(values.keySet());
    }

    @Override
    public boolean hasMember(String key) {
        return internal.containsKey(key) || getters.containsKey(key) || setters.containsKey(key) || values.containsKey(key);
    }

    @Override
    public void putMember(String key, Value value) {
        if (setters.containsKey(key)) {
            setters.get(key).accept(value);
        } else if (!internal.containsKey(key)) {
            values.put(key, value);
        }
    }

    @Override
    public boolean removeMember(String key) {
        return internal.containsKey(key) || getters.containsKey(key) || setters.containsKey(key) || values.remove(key) != null;
    }

    @Override
    public String toString() {
        return values.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining(", ", "{", "}"));
    }
}
