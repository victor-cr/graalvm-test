package com.codegans.test.graalvm;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyInstantiable;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) {
        String lang = "js";

        try (var context = Context.newBuilder().allowAllAccess(true).build()) {
            context.getBindings(lang).putMember("proxyFn", new ProxyFunction());

            context.eval(lang, /*language=js*/
                    "console.log('------------------------');\n" +
                            "console.log('proxyFn:', proxyFn);\n" +
                            "console.log('typeof proxyFn:', typeof proxyFn);\n" +
                            "console.log('proxyFn.name:', proxyFn.name);\n" +
                            "console.log('proxyFn.length:', proxyFn.length);\n" +
                            "console.log('proxyFn.prototype:', proxyFn.prototype);" +
                            "console.log('proxyFn.constructor:', proxyFn.constructor);" +
                            "console.log('proxyFn.toString():', proxyFn.toString());" +
                            "console.log('new proxyFn():', new proxyFn());\n" +
                            "console.log('new proxyFn().__proto__:', new proxyFn().__proto__);\n" +
                            "console.log('------------------------');"
            );
        }
    }

    public static Node node() {
        return new Node();
    }

    public static ProxyFunction function() {
        return new ProxyFunction();
    }

    public static JsFunction fn(String name) {
        return new JsFunction(name);
    }

    public static class ProxyNode implements ProxyObject {
        private final ProxyFunction parentFn;

        public ProxyNode() {
            this(null);
        }

        public ProxyNode(ProxyFunction fn) {
            this.parentFn = fn;
        }

        public Object getMember(String key) {
            if ("__proto__".equals(key)) {
                return parentFn;
            } else if ("test".equals(key)) {
                return "value";
            }

            return null;
        }

        public Object getMemberKeys() {
            return Arrays.asList("test");
        }

        public boolean hasMember(String key) {
            return "__proto__".equals(key) || "test".equals(key);
        }

        public void putMember(String key, Value value) {
        }
    }

    public static class ProxyFunction implements ProxyExecutable, ProxyInstantiable, ProxyObject {
        public int length = 2;
        public final AtomicReference<Value> prototype = new AtomicReference<>();

        public Object execute(Value... arguments) {
            return new ProxyNode(this);
        }

        public Object newInstance(Value... arguments) {
            return new ProxyNode(this);
        }

        public Object getMember(String key) {
            if ("prototype".equals(key)) {
                return prototype.updateAndGet(v -> {
                    if (v == null || v.isNull()) {
                        return Context.getCurrent().getBindings("js").getMember("Object").newInstance();
                    } else {
                        return v;
                    }
                });
            } else if ("__proto__".equals(key)) {
                return Context.getCurrent().getBindings("js").getMember("Function").getMember("prototype");
            } else if ("constructor".equals(key)) {
                return Context.getCurrent().getBindings("js").getMember("Function");
            } else if ("name".equals(key)) {
                return "proxyFunction";
            } else if ("length".equals(key)) {
                return length;
            } else if ("toString".equals(key)) {
                return (ProxyExecutable) arguments -> "function(" + IntStream.range(0, length).mapToObj(i -> "arg" + i).collect(Collectors.joining(", ")) + ") { [native JVM code] }";
            }

            return null;
        }

        public Object getMemberKeys() {
            return Collections.emptyList();
        }

        public boolean hasMember(String key) {
            return Arrays.asList("prototype", "__proto__", "constructor", "name", "length", "toString").contains(key);
        }

        public void putMember(String key, Value value) {
        }
    }

    public static class Node extends EventTarget {
        public void some() {
        }
    }

    public static class EventTarget {
        public <V> void addEventListener(String type, Consumer<V> listener) {
        }

        public <V> void addEventListener(String type, Consumer<V> listener, boolean useCapture) {
        }
    }
}