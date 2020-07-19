const proxyFn = Java.type("com.codegans.test.graalvm.Main").fn("test");

console.log('------------------------');
console.log('proxyFn:', proxyFn);
console.log('typeof proxyFn:', typeof proxyFn);
console.log('proxyFn.name:', proxyFn.name);
console.log('proxyFn.length:', proxyFn.length);
console.log('proxyFn.__proto__:', proxyFn.__proto__);
console.log('proxyFn.prototype:', proxyFn.prototype);
console.log('proxyFn.isPrototypeOf(Object):', proxyFn.isPrototypeOf(Object));
console.log('proxyFn.isPrototypeOf(Function):', proxyFn.isPrototypeOf(Function));
console.log('proxyFn instanceof Object:', proxyFn instanceof Object);
console.log('proxyFn instanceof Function:', proxyFn instanceof Function);
console.log('proxyFn.constructor:', proxyFn.constructor);
console.log('proxyFn.toString():', proxyFn.toString());
console.log('new proxyFn():', new proxyFn());
console.log('new proxyFn().__proto__:', new proxyFn().__proto__);
console.log('------------------------');