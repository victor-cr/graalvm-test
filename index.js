const proxyFn = Java.type("com.codegans.test.graalvm.Main").function();

console.log('------------------------');
console.log('proxyFn:', proxyFn);
console.log('typeof proxyFn:', typeof proxyFn);
console.log('proxyFn.name:', proxyFn.name);
console.log('proxyFn.length:', proxyFn.length);
console.log('proxyFn.__proto__:', proxyFn.__proto__);
console.log('proxyFn.prototype:', proxyFn.prototype);
console.log('proxyFn.constructor:', proxyFn.constructor);
console.log('proxyFn.toString():', proxyFn.toString());
console.log('new proxyFn():', new proxyFn());
console.log('new proxyFn().__proto__:', new proxyFn().__proto__);
console.log('------------------------');