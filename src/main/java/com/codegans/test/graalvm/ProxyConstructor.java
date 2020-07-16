package com.codegans.test.graalvm;

import org.graalvm.polyglot.Value;

/**
 * JavaDoc here
 *
 * @author Victor Polischuk
 * @since 7/12/2020 10:21 PM
 */
public interface ProxyConstructor<T extends JsObject> {
    Object execute(T instance, Value[] args);
}
