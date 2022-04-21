require=(function(){function r(e,n,t){function o(i,f){if(!n[i]){if(!e[i]){var c="function"==typeof require&&require;if(!f&&c)return c(i,!0);if(u)return u(i,!0);var a=new Error("Cannot find module '"+i+"'");throw a.code="MODULE_NOT_FOUND",a}var p=n[i]={exports:{}};e[i][0].call(p.exports,function(r){var n=e[i][1][r];return o(n||r)},p,p.exports,r,e,n,t)}return n[i].exports}for(var u="function"==typeof require&&require,i=0;i<t.length;i++)o(t[i]);return o}return r})()({1:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,setImmediate,clearImmediate,__filename,__dirname){(function (){
'use strict'

exports.byteLength = byteLength
exports.toByteArray = toByteArray
exports.fromByteArray = fromByteArray

var lookup = []
var revLookup = []
var Arr = typeof Uint8Array !== 'undefined' ? Uint8Array : Array

var code = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/'
for (var i = 0, len = code.length; i < len; ++i) {
  lookup[i] = code[i]
  revLookup[code.charCodeAt(i)] = i
}

// Support decoding URL-safe base64 strings, as Node.js does.
// See: https://en.wikipedia.org/wiki/Base64#URL_applications
revLookup['-'.charCodeAt(0)] = 62
revLookup['_'.charCodeAt(0)] = 63

function getLens (b64) {
  var len = b64.length

  if (len % 4 > 0) {
    throw new Error('Invalid string. Length must be a multiple of 4')
  }

  // Trim off extra bytes after placeholder bytes are found
  // See: https://github.com/beatgammit/base64-js/issues/42
  var validLen = b64.indexOf('=')
  if (validLen === -1) validLen = len

  var placeHoldersLen = validLen === len
    ? 0
    : 4 - (validLen % 4)

  return [validLen, placeHoldersLen]
}

// base64 is 4/3 + up to two characters of the original data
function byteLength (b64) {
  var lens = getLens(b64)
  var validLen = lens[0]
  var placeHoldersLen = lens[1]
  return ((validLen + placeHoldersLen) * 3 / 4) - placeHoldersLen
}

function _byteLength (b64, validLen, placeHoldersLen) {
  return ((validLen + placeHoldersLen) * 3 / 4) - placeHoldersLen
}

function toByteArray (b64) {
  var tmp
  var lens = getLens(b64)
  var validLen = lens[0]
  var placeHoldersLen = lens[1]

  var arr = new Arr(_byteLength(b64, validLen, placeHoldersLen))

  var curByte = 0

  // if there are placeholders, only get up to the last complete 4 chars
  var len = placeHoldersLen > 0
    ? validLen - 4
    : validLen

  var i
  for (i = 0; i < len; i += 4) {
    tmp =
      (revLookup[b64.charCodeAt(i)] << 18) |
      (revLookup[b64.charCodeAt(i + 1)] << 12) |
      (revLookup[b64.charCodeAt(i + 2)] << 6) |
      revLookup[b64.charCodeAt(i + 3)]
    arr[curByte++] = (tmp >> 16) & 0xFF
    arr[curByte++] = (tmp >> 8) & 0xFF
    arr[curByte++] = tmp & 0xFF
  }

  if (placeHoldersLen === 2) {
    tmp =
      (revLookup[b64.charCodeAt(i)] << 2) |
      (revLookup[b64.charCodeAt(i + 1)] >> 4)
    arr[curByte++] = tmp & 0xFF
  }

  if (placeHoldersLen === 1) {
    tmp =
      (revLookup[b64.charCodeAt(i)] << 10) |
      (revLookup[b64.charCodeAt(i + 1)] << 4) |
      (revLookup[b64.charCodeAt(i + 2)] >> 2)
    arr[curByte++] = (tmp >> 8) & 0xFF
    arr[curByte++] = tmp & 0xFF
  }

  return arr
}

function tripletToBase64 (num) {
  return lookup[num >> 18 & 0x3F] +
    lookup[num >> 12 & 0x3F] +
    lookup[num >> 6 & 0x3F] +
    lookup[num & 0x3F]
}

function encodeChunk (uint8, start, end) {
  var tmp
  var output = []
  for (var i = start; i < end; i += 3) {
    tmp =
      ((uint8[i] << 16) & 0xFF0000) +
      ((uint8[i + 1] << 8) & 0xFF00) +
      (uint8[i + 2] & 0xFF)
    output.push(tripletToBase64(tmp))
  }
  return output.join('')
}

function fromByteArray (uint8) {
  var tmp
  var len = uint8.length
  var extraBytes = len % 3 // if we have 1 byte left, pad 2 bytes
  var parts = []
  var maxChunkLength = 16383 // must be multiple of 3

  // go through the array every three bytes, we'll deal with trailing stuff later
  for (var i = 0, len2 = len - extraBytes; i < len2; i += maxChunkLength) {
    parts.push(encodeChunk(uint8, i, (i + maxChunkLength) > len2 ? len2 : (i + maxChunkLength)))
  }

  // pad the end with zeros, but make sure to not forget the extra bytes
  if (extraBytes === 1) {
    tmp = uint8[len - 1]
    parts.push(
      lookup[tmp >> 2] +
      lookup[(tmp << 4) & 0x3F] +
      '=='
    )
  } else if (extraBytes === 2) {
    tmp = (uint8[len - 2] << 8) + uint8[len - 1]
    parts.push(
      lookup[tmp >> 10] +
      lookup[(tmp >> 4) & 0x3F] +
      lookup[(tmp << 2) & 0x3F] +
      '='
    )
  }

  return parts.join('')
}

}).call(this)}).call(this,require('_process'),typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],require("timers").setImmediate,require("timers").clearImmediate,"/node_modules/base64-js/index.js","/node_modules/base64-js")

},{"_process":4,"buffer":2,"timers":5}],2:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,setImmediate,clearImmediate,__filename,__dirname){(function (){
/*!
 * The buffer module from node.js, for the browser.
 *
 * @author   Feross Aboukhadijeh <https://feross.org>
 * @license  MIT
 */
/* eslint-disable no-proto */

'use strict'

var base64 = require('base64-js')
var ieee754 = require('ieee754')
var customInspectSymbol =
  (typeof Symbol === 'function' && typeof Symbol['for'] === 'function') // eslint-disable-line dot-notation
    ? Symbol['for']('nodejs.util.inspect.custom') // eslint-disable-line dot-notation
    : null

exports.Buffer = Buffer
exports.SlowBuffer = SlowBuffer
exports.INSPECT_MAX_BYTES = 50

var K_MAX_LENGTH = 0x7fffffff
exports.kMaxLength = K_MAX_LENGTH

/**
 * If `Buffer.TYPED_ARRAY_SUPPORT`:
 *   === true    Use Uint8Array implementation (fastest)
 *   === false   Print warning and recommend using `buffer` v4.x which has an Object
 *               implementation (most compatible, even IE6)
 *
 * Browsers that support typed arrays are IE 10+, Firefox 4+, Chrome 7+, Safari 5.1+,
 * Opera 11.6+, iOS 4.2+.
 *
 * We report that the browser does not support typed arrays if the are not subclassable
 * using __proto__. Firefox 4-29 lacks support for adding new properties to `Uint8Array`
 * (See: https://bugzilla.mozilla.org/show_bug.cgi?id=695438). IE 10 lacks support
 * for __proto__ and has a buggy typed array implementation.
 */
Buffer.TYPED_ARRAY_SUPPORT = typedArraySupport()

if (!Buffer.TYPED_ARRAY_SUPPORT && typeof console !== 'undefined' &&
    typeof console.error === 'function') {
  console.error(
    'This browser lacks typed array (Uint8Array) support which is required by ' +
    '`buffer` v5.x. Use `buffer` v4.x if you require old browser support.'
  )
}

function typedArraySupport () {
  // Can typed array instances can be augmented?
  try {
    var arr = new Uint8Array(1)
    var proto = { foo: function () { return 42 } }
    Object.setPrototypeOf(proto, Uint8Array.prototype)
    Object.setPrototypeOf(arr, proto)
    return arr.foo() === 42
  } catch (e) {
    return false
  }
}

Object.defineProperty(Buffer.prototype, 'parent', {
  enumerable: true,
  get: function () {
    if (!Buffer.isBuffer(this)) return undefined
    return this.buffer
  }
})

Object.defineProperty(Buffer.prototype, 'offset', {
  enumerable: true,
  get: function () {
    if (!Buffer.isBuffer(this)) return undefined
    return this.byteOffset
  }
})

function createBuffer (length) {
  if (length > K_MAX_LENGTH) {
    throw new RangeError('The value "' + length + '" is invalid for option "size"')
  }
  // Return an augmented `Uint8Array` instance
  var buf = new Uint8Array(length)
  Object.setPrototypeOf(buf, Buffer.prototype)
  return buf
}

/**
 * The Buffer constructor returns instances of `Uint8Array` that have their
 * prototype changed to `Buffer.prototype`. Furthermore, `Buffer` is a subclass of
 * `Uint8Array`, so the returned instances will have all the node `Buffer` methods
 * and the `Uint8Array` methods. Square bracket notation works as expected -- it
 * returns a single octet.
 *
 * The `Uint8Array` prototype remains unmodified.
 */

function Buffer (arg, encodingOrOffset, length) {
  // Common case.
  if (typeof arg === 'number') {
    if (typeof encodingOrOffset === 'string') {
      throw new TypeError(
        'The "string" argument must be of type string. Received type number'
      )
    }
    return allocUnsafe(arg)
  }
  return from(arg, encodingOrOffset, length)
}

Buffer.poolSize = 8192 // not used by this implementation

function from (value, encodingOrOffset, length) {
  if (typeof value === 'string') {
    return fromString(value, encodingOrOffset)
  }

  if (ArrayBuffer.isView(value)) {
    return fromArrayView(value)
  }

  if (value == null) {
    throw new TypeError(
      'The first argument must be one of type string, Buffer, ArrayBuffer, Array, ' +
      'or Array-like Object. Received type ' + (typeof value)
    )
  }

  if (isInstance(value, ArrayBuffer) ||
      (value && isInstance(value.buffer, ArrayBuffer))) {
    return fromArrayBuffer(value, encodingOrOffset, length)
  }

  if (typeof SharedArrayBuffer !== 'undefined' &&
      (isInstance(value, SharedArrayBuffer) ||
      (value && isInstance(value.buffer, SharedArrayBuffer)))) {
    return fromArrayBuffer(value, encodingOrOffset, length)
  }

  if (typeof value === 'number') {
    throw new TypeError(
      'The "value" argument must not be of type number. Received type number'
    )
  }

  var valueOf = value.valueOf && value.valueOf()
  if (valueOf != null && valueOf !== value) {
    return Buffer.from(valueOf, encodingOrOffset, length)
  }

  var b = fromObject(value)
  if (b) return b

  if (typeof Symbol !== 'undefined' && Symbol.toPrimitive != null &&
      typeof value[Symbol.toPrimitive] === 'function') {
    return Buffer.from(
      value[Symbol.toPrimitive]('string'), encodingOrOffset, length
    )
  }

  throw new TypeError(
    'The first argument must be one of type string, Buffer, ArrayBuffer, Array, ' +
    'or Array-like Object. Received type ' + (typeof value)
  )
}

/**
 * Functionally equivalent to Buffer(arg, encoding) but throws a TypeError
 * if value is a number.
 * Buffer.from(str[, encoding])
 * Buffer.from(array)
 * Buffer.from(buffer)
 * Buffer.from(arrayBuffer[, byteOffset[, length]])
 **/
Buffer.from = function (value, encodingOrOffset, length) {
  return from(value, encodingOrOffset, length)
}

// Note: Change prototype *after* Buffer.from is defined to workaround Chrome bug:
// https://github.com/feross/buffer/pull/148
Object.setPrototypeOf(Buffer.prototype, Uint8Array.prototype)
Object.setPrototypeOf(Buffer, Uint8Array)

function assertSize (size) {
  if (typeof size !== 'number') {
    throw new TypeError('"size" argument must be of type number')
  } else if (size < 0) {
    throw new RangeError('The value "' + size + '" is invalid for option "size"')
  }
}

function alloc (size, fill, encoding) {
  assertSize(size)
  if (size <= 0) {
    return createBuffer(size)
  }
  if (fill !== undefined) {
    // Only pay attention to encoding if it's a string. This
    // prevents accidentally sending in a number that would
    // be interpreted as a start offset.
    return typeof encoding === 'string'
      ? createBuffer(size).fill(fill, encoding)
      : createBuffer(size).fill(fill)
  }
  return createBuffer(size)
}

/**
 * Creates a new filled Buffer instance.
 * alloc(size[, fill[, encoding]])
 **/
Buffer.alloc = function (size, fill, encoding) {
  return alloc(size, fill, encoding)
}

function allocUnsafe (size) {
  assertSize(size)
  return createBuffer(size < 0 ? 0 : checked(size) | 0)
}

/**
 * Equivalent to Buffer(num), by default creates a non-zero-filled Buffer instance.
 * */
Buffer.allocUnsafe = function (size) {
  return allocUnsafe(size)
}
/**
 * Equivalent to SlowBuffer(num), by default creates a non-zero-filled Buffer instance.
 */
Buffer.allocUnsafeSlow = function (size) {
  return allocUnsafe(size)
}

function fromString (string, encoding) {
  if (typeof encoding !== 'string' || encoding === '') {
    encoding = 'utf8'
  }

  if (!Buffer.isEncoding(encoding)) {
    throw new TypeError('Unknown encoding: ' + encoding)
  }

  var length = byteLength(string, encoding) | 0
  var buf = createBuffer(length)

  var actual = buf.write(string, encoding)

  if (actual !== length) {
    // Writing a hex string, for example, that contains invalid characters will
    // cause everything after the first invalid character to be ignored. (e.g.
    // 'abxxcd' will be treated as 'ab')
    buf = buf.slice(0, actual)
  }

  return buf
}

function fromArrayLike (array) {
  var length = array.length < 0 ? 0 : checked(array.length) | 0
  var buf = createBuffer(length)
  for (var i = 0; i < length; i += 1) {
    buf[i] = array[i] & 255
  }
  return buf
}

function fromArrayView (arrayView) {
  if (isInstance(arrayView, Uint8Array)) {
    var copy = new Uint8Array(arrayView)
    return fromArrayBuffer(copy.buffer, copy.byteOffset, copy.byteLength)
  }
  return fromArrayLike(arrayView)
}

function fromArrayBuffer (array, byteOffset, length) {
  if (byteOffset < 0 || array.byteLength < byteOffset) {
    throw new RangeError('"offset" is outside of buffer bounds')
  }

  if (array.byteLength < byteOffset + (length || 0)) {
    throw new RangeError('"length" is outside of buffer bounds')
  }

  var buf
  if (byteOffset === undefined && length === undefined) {
    buf = new Uint8Array(array)
  } else if (length === undefined) {
    buf = new Uint8Array(array, byteOffset)
  } else {
    buf = new Uint8Array(array, byteOffset, length)
  }

  // Return an augmented `Uint8Array` instance
  Object.setPrototypeOf(buf, Buffer.prototype)

  return buf
}

function fromObject (obj) {
  if (Buffer.isBuffer(obj)) {
    var len = checked(obj.length) | 0
    var buf = createBuffer(len)

    if (buf.length === 0) {
      return buf
    }

    obj.copy(buf, 0, 0, len)
    return buf
  }

  if (obj.length !== undefined) {
    if (typeof obj.length !== 'number' || numberIsNaN(obj.length)) {
      return createBuffer(0)
    }
    return fromArrayLike(obj)
  }

  if (obj.type === 'Buffer' && Array.isArray(obj.data)) {
    return fromArrayLike(obj.data)
  }
}

function checked (length) {
  // Note: cannot use `length < K_MAX_LENGTH` here because that fails when
  // length is NaN (which is otherwise coerced to zero.)
  if (length >= K_MAX_LENGTH) {
    throw new RangeError('Attempt to allocate Buffer larger than maximum ' +
                         'size: 0x' + K_MAX_LENGTH.toString(16) + ' bytes')
  }
  return length | 0
}

function SlowBuffer (length) {
  if (+length != length) { // eslint-disable-line eqeqeq
    length = 0
  }
  return Buffer.alloc(+length)
}

Buffer.isBuffer = function isBuffer (b) {
  return b != null && b._isBuffer === true &&
    b !== Buffer.prototype // so Buffer.isBuffer(Buffer.prototype) will be false
}

Buffer.compare = function compare (a, b) {
  if (isInstance(a, Uint8Array)) a = Buffer.from(a, a.offset, a.byteLength)
  if (isInstance(b, Uint8Array)) b = Buffer.from(b, b.offset, b.byteLength)
  if (!Buffer.isBuffer(a) || !Buffer.isBuffer(b)) {
    throw new TypeError(
      'The "buf1", "buf2" arguments must be one of type Buffer or Uint8Array'
    )
  }

  if (a === b) return 0

  var x = a.length
  var y = b.length

  for (var i = 0, len = Math.min(x, y); i < len; ++i) {
    if (a[i] !== b[i]) {
      x = a[i]
      y = b[i]
      break
    }
  }

  if (x < y) return -1
  if (y < x) return 1
  return 0
}

Buffer.isEncoding = function isEncoding (encoding) {
  switch (String(encoding).toLowerCase()) {
    case 'hex':
    case 'utf8':
    case 'utf-8':
    case 'ascii':
    case 'latin1':
    case 'binary':
    case 'base64':
    case 'ucs2':
    case 'ucs-2':
    case 'utf16le':
    case 'utf-16le':
      return true
    default:
      return false
  }
}

Buffer.concat = function concat (list, length) {
  if (!Array.isArray(list)) {
    throw new TypeError('"list" argument must be an Array of Buffers')
  }

  if (list.length === 0) {
    return Buffer.alloc(0)
  }

  var i
  if (length === undefined) {
    length = 0
    for (i = 0; i < list.length; ++i) {
      length += list[i].length
    }
  }

  var buffer = Buffer.allocUnsafe(length)
  var pos = 0
  for (i = 0; i < list.length; ++i) {
    var buf = list[i]
    if (isInstance(buf, Uint8Array)) {
      if (pos + buf.length > buffer.length) {
        Buffer.from(buf).copy(buffer, pos)
      } else {
        Uint8Array.prototype.set.call(
          buffer,
          buf,
          pos
        )
      }
    } else if (!Buffer.isBuffer(buf)) {
      throw new TypeError('"list" argument must be an Array of Buffers')
    } else {
      buf.copy(buffer, pos)
    }
    pos += buf.length
  }
  return buffer
}

function byteLength (string, encoding) {
  if (Buffer.isBuffer(string)) {
    return string.length
  }
  if (ArrayBuffer.isView(string) || isInstance(string, ArrayBuffer)) {
    return string.byteLength
  }
  if (typeof string !== 'string') {
    throw new TypeError(
      'The "string" argument must be one of type string, Buffer, or ArrayBuffer. ' +
      'Received type ' + typeof string
    )
  }

  var len = string.length
  var mustMatch = (arguments.length > 2 && arguments[2] === true)
  if (!mustMatch && len === 0) return 0

  // Use a for loop to avoid recursion
  var loweredCase = false
  for (;;) {
    switch (encoding) {
      case 'ascii':
      case 'latin1':
      case 'binary':
        return len
      case 'utf8':
      case 'utf-8':
        return utf8ToBytes(string).length
      case 'ucs2':
      case 'ucs-2':
      case 'utf16le':
      case 'utf-16le':
        return len * 2
      case 'hex':
        return len >>> 1
      case 'base64':
        return base64ToBytes(string).length
      default:
        if (loweredCase) {
          return mustMatch ? -1 : utf8ToBytes(string).length // assume utf8
        }
        encoding = ('' + encoding).toLowerCase()
        loweredCase = true
    }
  }
}
Buffer.byteLength = byteLength

function slowToString (encoding, start, end) {
  var loweredCase = false

  // No need to verify that "this.length <= MAX_UINT32" since it's a read-only
  // property of a typed array.

  // This behaves neither like String nor Uint8Array in that we set start/end
  // to their upper/lower bounds if the value passed is out of range.
  // undefined is handled specially as per ECMA-262 6th Edition,
  // Section 13.3.3.7 Runtime Semantics: KeyedBindingInitialization.
  if (start === undefined || start < 0) {
    start = 0
  }
  // Return early if start > this.length. Done here to prevent potential uint32
  // coercion fail below.
  if (start > this.length) {
    return ''
  }

  if (end === undefined || end > this.length) {
    end = this.length
  }

  if (end <= 0) {
    return ''
  }

  // Force coercion to uint32. This will also coerce falsey/NaN values to 0.
  end >>>= 0
  start >>>= 0

  if (end <= start) {
    return ''
  }

  if (!encoding) encoding = 'utf8'

  while (true) {
    switch (encoding) {
      case 'hex':
        return hexSlice(this, start, end)

      case 'utf8':
      case 'utf-8':
        return utf8Slice(this, start, end)

      case 'ascii':
        return asciiSlice(this, start, end)

      case 'latin1':
      case 'binary':
        return latin1Slice(this, start, end)

      case 'base64':
        return base64Slice(this, start, end)

      case 'ucs2':
      case 'ucs-2':
      case 'utf16le':
      case 'utf-16le':
        return utf16leSlice(this, start, end)

      default:
        if (loweredCase) throw new TypeError('Unknown encoding: ' + encoding)
        encoding = (encoding + '').toLowerCase()
        loweredCase = true
    }
  }
}

// This property is used by `Buffer.isBuffer` (and the `is-buffer` npm package)
// to detect a Buffer instance. It's not possible to use `instanceof Buffer`
// reliably in a browserify context because there could be multiple different
// copies of the 'buffer' package in use. This method works even for Buffer
// instances that were created from another copy of the `buffer` package.
// See: https://github.com/feross/buffer/issues/154
Buffer.prototype._isBuffer = true

function swap (b, n, m) {
  var i = b[n]
  b[n] = b[m]
  b[m] = i
}

Buffer.prototype.swap16 = function swap16 () {
  var len = this.length
  if (len % 2 !== 0) {
    throw new RangeError('Buffer size must be a multiple of 16-bits')
  }
  for (var i = 0; i < len; i += 2) {
    swap(this, i, i + 1)
  }
  return this
}

Buffer.prototype.swap32 = function swap32 () {
  var len = this.length
  if (len % 4 !== 0) {
    throw new RangeError('Buffer size must be a multiple of 32-bits')
  }
  for (var i = 0; i < len; i += 4) {
    swap(this, i, i + 3)
    swap(this, i + 1, i + 2)
  }
  return this
}

Buffer.prototype.swap64 = function swap64 () {
  var len = this.length
  if (len % 8 !== 0) {
    throw new RangeError('Buffer size must be a multiple of 64-bits')
  }
  for (var i = 0; i < len; i += 8) {
    swap(this, i, i + 7)
    swap(this, i + 1, i + 6)
    swap(this, i + 2, i + 5)
    swap(this, i + 3, i + 4)
  }
  return this
}

Buffer.prototype.toString = function toString () {
  var length = this.length
  if (length === 0) return ''
  if (arguments.length === 0) return utf8Slice(this, 0, length)
  return slowToString.apply(this, arguments)
}

Buffer.prototype.toLocaleString = Buffer.prototype.toString

Buffer.prototype.equals = function equals (b) {
  if (!Buffer.isBuffer(b)) throw new TypeError('Argument must be a Buffer')
  if (this === b) return true
  return Buffer.compare(this, b) === 0
}

Buffer.prototype.inspect = function inspect () {
  var str = ''
  var max = exports.INSPECT_MAX_BYTES
  str = this.toString('hex', 0, max).replace(/(.{2})/g, '$1 ').trim()
  if (this.length > max) str += ' ... '
  return '<Buffer ' + str + '>'
}
if (customInspectSymbol) {
  Buffer.prototype[customInspectSymbol] = Buffer.prototype.inspect
}

Buffer.prototype.compare = function compare (target, start, end, thisStart, thisEnd) {
  if (isInstance(target, Uint8Array)) {
    target = Buffer.from(target, target.offset, target.byteLength)
  }
  if (!Buffer.isBuffer(target)) {
    throw new TypeError(
      'The "target" argument must be one of type Buffer or Uint8Array. ' +
      'Received type ' + (typeof target)
    )
  }

  if (start === undefined) {
    start = 0
  }
  if (end === undefined) {
    end = target ? target.length : 0
  }
  if (thisStart === undefined) {
    thisStart = 0
  }
  if (thisEnd === undefined) {
    thisEnd = this.length
  }

  if (start < 0 || end > target.length || thisStart < 0 || thisEnd > this.length) {
    throw new RangeError('out of range index')
  }

  if (thisStart >= thisEnd && start >= end) {
    return 0
  }
  if (thisStart >= thisEnd) {
    return -1
  }
  if (start >= end) {
    return 1
  }

  start >>>= 0
  end >>>= 0
  thisStart >>>= 0
  thisEnd >>>= 0

  if (this === target) return 0

  var x = thisEnd - thisStart
  var y = end - start
  var len = Math.min(x, y)

  var thisCopy = this.slice(thisStart, thisEnd)
  var targetCopy = target.slice(start, end)

  for (var i = 0; i < len; ++i) {
    if (thisCopy[i] !== targetCopy[i]) {
      x = thisCopy[i]
      y = targetCopy[i]
      break
    }
  }

  if (x < y) return -1
  if (y < x) return 1
  return 0
}

// Finds either the first index of `val` in `buffer` at offset >= `byteOffset`,
// OR the last index of `val` in `buffer` at offset <= `byteOffset`.
//
// Arguments:
// - buffer - a Buffer to search
// - val - a string, Buffer, or number
// - byteOffset - an index into `buffer`; will be clamped to an int32
// - encoding - an optional encoding, relevant is val is a string
// - dir - true for indexOf, false for lastIndexOf
function bidirectionalIndexOf (buffer, val, byteOffset, encoding, dir) {
  // Empty buffer means no match
  if (buffer.length === 0) return -1

  // Normalize byteOffset
  if (typeof byteOffset === 'string') {
    encoding = byteOffset
    byteOffset = 0
  } else if (byteOffset > 0x7fffffff) {
    byteOffset = 0x7fffffff
  } else if (byteOffset < -0x80000000) {
    byteOffset = -0x80000000
  }
  byteOffset = +byteOffset // Coerce to Number.
  if (numberIsNaN(byteOffset)) {
    // byteOffset: it it's undefined, null, NaN, "foo", etc, search whole buffer
    byteOffset = dir ? 0 : (buffer.length - 1)
  }

  // Normalize byteOffset: negative offsets start from the end of the buffer
  if (byteOffset < 0) byteOffset = buffer.length + byteOffset
  if (byteOffset >= buffer.length) {
    if (dir) return -1
    else byteOffset = buffer.length - 1
  } else if (byteOffset < 0) {
    if (dir) byteOffset = 0
    else return -1
  }

  // Normalize val
  if (typeof val === 'string') {
    val = Buffer.from(val, encoding)
  }

  // Finally, search either indexOf (if dir is true) or lastIndexOf
  if (Buffer.isBuffer(val)) {
    // Special case: looking for empty string/buffer always fails
    if (val.length === 0) {
      return -1
    }
    return arrayIndexOf(buffer, val, byteOffset, encoding, dir)
  } else if (typeof val === 'number') {
    val = val & 0xFF // Search for a byte value [0-255]
    if (typeof Uint8Array.prototype.indexOf === 'function') {
      if (dir) {
        return Uint8Array.prototype.indexOf.call(buffer, val, byteOffset)
      } else {
        return Uint8Array.prototype.lastIndexOf.call(buffer, val, byteOffset)
      }
    }
    return arrayIndexOf(buffer, [val], byteOffset, encoding, dir)
  }

  throw new TypeError('val must be string, number or Buffer')
}

function arrayIndexOf (arr, val, byteOffset, encoding, dir) {
  var indexSize = 1
  var arrLength = arr.length
  var valLength = val.length

  if (encoding !== undefined) {
    encoding = String(encoding).toLowerCase()
    if (encoding === 'ucs2' || encoding === 'ucs-2' ||
        encoding === 'utf16le' || encoding === 'utf-16le') {
      if (arr.length < 2 || val.length < 2) {
        return -1
      }
      indexSize = 2
      arrLength /= 2
      valLength /= 2
      byteOffset /= 2
    }
  }

  function read (buf, i) {
    if (indexSize === 1) {
      return buf[i]
    } else {
      return buf.readUInt16BE(i * indexSize)
    }
  }

  var i
  if (dir) {
    var foundIndex = -1
    for (i = byteOffset; i < arrLength; i++) {
      if (read(arr, i) === read(val, foundIndex === -1 ? 0 : i - foundIndex)) {
        if (foundIndex === -1) foundIndex = i
        if (i - foundIndex + 1 === valLength) return foundIndex * indexSize
      } else {
        if (foundIndex !== -1) i -= i - foundIndex
        foundIndex = -1
      }
    }
  } else {
    if (byteOffset + valLength > arrLength) byteOffset = arrLength - valLength
    for (i = byteOffset; i >= 0; i--) {
      var found = true
      for (var j = 0; j < valLength; j++) {
        if (read(arr, i + j) !== read(val, j)) {
          found = false
          break
        }
      }
      if (found) return i
    }
  }

  return -1
}

Buffer.prototype.includes = function includes (val, byteOffset, encoding) {
  return this.indexOf(val, byteOffset, encoding) !== -1
}

Buffer.prototype.indexOf = function indexOf (val, byteOffset, encoding) {
  return bidirectionalIndexOf(this, val, byteOffset, encoding, true)
}

Buffer.prototype.lastIndexOf = function lastIndexOf (val, byteOffset, encoding) {
  return bidirectionalIndexOf(this, val, byteOffset, encoding, false)
}

function hexWrite (buf, string, offset, length) {
  offset = Number(offset) || 0
  var remaining = buf.length - offset
  if (!length) {
    length = remaining
  } else {
    length = Number(length)
    if (length > remaining) {
      length = remaining
    }
  }

  var strLen = string.length

  if (length > strLen / 2) {
    length = strLen / 2
  }
  for (var i = 0; i < length; ++i) {
    var parsed = parseInt(string.substr(i * 2, 2), 16)
    if (numberIsNaN(parsed)) return i
    buf[offset + i] = parsed
  }
  return i
}

function utf8Write (buf, string, offset, length) {
  return blitBuffer(utf8ToBytes(string, buf.length - offset), buf, offset, length)
}

function asciiWrite (buf, string, offset, length) {
  return blitBuffer(asciiToBytes(string), buf, offset, length)
}

function base64Write (buf, string, offset, length) {
  return blitBuffer(base64ToBytes(string), buf, offset, length)
}

function ucs2Write (buf, string, offset, length) {
  return blitBuffer(utf16leToBytes(string, buf.length - offset), buf, offset, length)
}

Buffer.prototype.write = function write (string, offset, length, encoding) {
  // Buffer#write(string)
  if (offset === undefined) {
    encoding = 'utf8'
    length = this.length
    offset = 0
  // Buffer#write(string, encoding)
  } else if (length === undefined && typeof offset === 'string') {
    encoding = offset
    length = this.length
    offset = 0
  // Buffer#write(string, offset[, length][, encoding])
  } else if (isFinite(offset)) {
    offset = offset >>> 0
    if (isFinite(length)) {
      length = length >>> 0
      if (encoding === undefined) encoding = 'utf8'
    } else {
      encoding = length
      length = undefined
    }
  } else {
    throw new Error(
      'Buffer.write(string, encoding, offset[, length]) is no longer supported'
    )
  }

  var remaining = this.length - offset
  if (length === undefined || length > remaining) length = remaining

  if ((string.length > 0 && (length < 0 || offset < 0)) || offset > this.length) {
    throw new RangeError('Attempt to write outside buffer bounds')
  }

  if (!encoding) encoding = 'utf8'

  var loweredCase = false
  for (;;) {
    switch (encoding) {
      case 'hex':
        return hexWrite(this, string, offset, length)

      case 'utf8':
      case 'utf-8':
        return utf8Write(this, string, offset, length)

      case 'ascii':
      case 'latin1':
      case 'binary':
        return asciiWrite(this, string, offset, length)

      case 'base64':
        // Warning: maxLength not taken into account in base64Write
        return base64Write(this, string, offset, length)

      case 'ucs2':
      case 'ucs-2':
      case 'utf16le':
      case 'utf-16le':
        return ucs2Write(this, string, offset, length)

      default:
        if (loweredCase) throw new TypeError('Unknown encoding: ' + encoding)
        encoding = ('' + encoding).toLowerCase()
        loweredCase = true
    }
  }
}

Buffer.prototype.toJSON = function toJSON () {
  return {
    type: 'Buffer',
    data: Array.prototype.slice.call(this._arr || this, 0)
  }
}

function base64Slice (buf, start, end) {
  if (start === 0 && end === buf.length) {
    return base64.fromByteArray(buf)
  } else {
    return base64.fromByteArray(buf.slice(start, end))
  }
}

function utf8Slice (buf, start, end) {
  end = Math.min(buf.length, end)
  var res = []

  var i = start
  while (i < end) {
    var firstByte = buf[i]
    var codePoint = null
    var bytesPerSequence = (firstByte > 0xEF)
      ? 4
      : (firstByte > 0xDF)
          ? 3
          : (firstByte > 0xBF)
              ? 2
              : 1

    if (i + bytesPerSequence <= end) {
      var secondByte, thirdByte, fourthByte, tempCodePoint

      switch (bytesPerSequence) {
        case 1:
          if (firstByte < 0x80) {
            codePoint = firstByte
          }
          break
        case 2:
          secondByte = buf[i + 1]
          if ((secondByte & 0xC0) === 0x80) {
            tempCodePoint = (firstByte & 0x1F) << 0x6 | (secondByte & 0x3F)
            if (tempCodePoint > 0x7F) {
              codePoint = tempCodePoint
            }
          }
          break
        case 3:
          secondByte = buf[i + 1]
          thirdByte = buf[i + 2]
          if ((secondByte & 0xC0) === 0x80 && (thirdByte & 0xC0) === 0x80) {
            tempCodePoint = (firstByte & 0xF) << 0xC | (secondByte & 0x3F) << 0x6 | (thirdByte & 0x3F)
            if (tempCodePoint > 0x7FF && (tempCodePoint < 0xD800 || tempCodePoint > 0xDFFF)) {
              codePoint = tempCodePoint
            }
          }
          break
        case 4:
          secondByte = buf[i + 1]
          thirdByte = buf[i + 2]
          fourthByte = buf[i + 3]
          if ((secondByte & 0xC0) === 0x80 && (thirdByte & 0xC0) === 0x80 && (fourthByte & 0xC0) === 0x80) {
            tempCodePoint = (firstByte & 0xF) << 0x12 | (secondByte & 0x3F) << 0xC | (thirdByte & 0x3F) << 0x6 | (fourthByte & 0x3F)
            if (tempCodePoint > 0xFFFF && tempCodePoint < 0x110000) {
              codePoint = tempCodePoint
            }
          }
      }
    }

    if (codePoint === null) {
      // we did not generate a valid codePoint so insert a
      // replacement char (U+FFFD) and advance only 1 byte
      codePoint = 0xFFFD
      bytesPerSequence = 1
    } else if (codePoint > 0xFFFF) {
      // encode to utf16 (surrogate pair dance)
      codePoint -= 0x10000
      res.push(codePoint >>> 10 & 0x3FF | 0xD800)
      codePoint = 0xDC00 | codePoint & 0x3FF
    }

    res.push(codePoint)
    i += bytesPerSequence
  }

  return decodeCodePointsArray(res)
}

// Based on http://stackoverflow.com/a/22747272/680742, the browser with
// the lowest limit is Chrome, with 0x10000 args.
// We go 1 magnitude less, for safety
var MAX_ARGUMENTS_LENGTH = 0x1000

function decodeCodePointsArray (codePoints) {
  var len = codePoints.length
  if (len <= MAX_ARGUMENTS_LENGTH) {
    return String.fromCharCode.apply(String, codePoints) // avoid extra slice()
  }

  // Decode in chunks to avoid "call stack size exceeded".
  var res = ''
  var i = 0
  while (i < len) {
    res += String.fromCharCode.apply(
      String,
      codePoints.slice(i, i += MAX_ARGUMENTS_LENGTH)
    )
  }
  return res
}

function asciiSlice (buf, start, end) {
  var ret = ''
  end = Math.min(buf.length, end)

  for (var i = start; i < end; ++i) {
    ret += String.fromCharCode(buf[i] & 0x7F)
  }
  return ret
}

function latin1Slice (buf, start, end) {
  var ret = ''
  end = Math.min(buf.length, end)

  for (var i = start; i < end; ++i) {
    ret += String.fromCharCode(buf[i])
  }
  return ret
}

function hexSlice (buf, start, end) {
  var len = buf.length

  if (!start || start < 0) start = 0
  if (!end || end < 0 || end > len) end = len

  var out = ''
  for (var i = start; i < end; ++i) {
    out += hexSliceLookupTable[buf[i]]
  }
  return out
}

function utf16leSlice (buf, start, end) {
  var bytes = buf.slice(start, end)
  var res = ''
  // If bytes.length is odd, the last 8 bits must be ignored (same as node.js)
  for (var i = 0; i < bytes.length - 1; i += 2) {
    res += String.fromCharCode(bytes[i] + (bytes[i + 1] * 256))
  }
  return res
}

Buffer.prototype.slice = function slice (start, end) {
  var len = this.length
  start = ~~start
  end = end === undefined ? len : ~~end

  if (start < 0) {
    start += len
    if (start < 0) start = 0
  } else if (start > len) {
    start = len
  }

  if (end < 0) {
    end += len
    if (end < 0) end = 0
  } else if (end > len) {
    end = len
  }

  if (end < start) end = start

  var newBuf = this.subarray(start, end)
  // Return an augmented `Uint8Array` instance
  Object.setPrototypeOf(newBuf, Buffer.prototype)

  return newBuf
}

/*
 * Need to make sure that buffer isn't trying to write out of bounds.
 */
function checkOffset (offset, ext, length) {
  if ((offset % 1) !== 0 || offset < 0) throw new RangeError('offset is not uint')
  if (offset + ext > length) throw new RangeError('Trying to access beyond buffer length')
}

Buffer.prototype.readUintLE =
Buffer.prototype.readUIntLE = function readUIntLE (offset, byteLength, noAssert) {
  offset = offset >>> 0
  byteLength = byteLength >>> 0
  if (!noAssert) checkOffset(offset, byteLength, this.length)

  var val = this[offset]
  var mul = 1
  var i = 0
  while (++i < byteLength && (mul *= 0x100)) {
    val += this[offset + i] * mul
  }

  return val
}

Buffer.prototype.readUintBE =
Buffer.prototype.readUIntBE = function readUIntBE (offset, byteLength, noAssert) {
  offset = offset >>> 0
  byteLength = byteLength >>> 0
  if (!noAssert) {
    checkOffset(offset, byteLength, this.length)
  }

  var val = this[offset + --byteLength]
  var mul = 1
  while (byteLength > 0 && (mul *= 0x100)) {
    val += this[offset + --byteLength] * mul
  }

  return val
}

Buffer.prototype.readUint8 =
Buffer.prototype.readUInt8 = function readUInt8 (offset, noAssert) {
  offset = offset >>> 0
  if (!noAssert) checkOffset(offset, 1, this.length)
  return this[offset]
}

Buffer.prototype.readUint16LE =
Buffer.prototype.readUInt16LE = function readUInt16LE (offset, noAssert) {
  offset = offset >>> 0
  if (!noAssert) checkOffset(offset, 2, this.length)
  return this[offset] | (this[offset + 1] << 8)
}

Buffer.prototype.readUint16BE =
Buffer.prototype.readUInt16BE = function readUInt16BE (offset, noAssert) {
  offset = offset >>> 0
  if (!noAssert) checkOffset(offset, 2, this.length)
  return (this[offset] << 8) | this[offset + 1]
}

Buffer.prototype.readUint32LE =
Buffer.prototype.readUInt32LE = function readUInt32LE (offset, noAssert) {
  offset = offset >>> 0
  if (!noAssert) checkOffset(offset, 4, this.length)

  return ((this[offset]) |
      (this[offset + 1] << 8) |
      (this[offset + 2] << 16)) +
      (this[offset + 3] * 0x1000000)
}

Buffer.prototype.readUint32BE =
Buffer.prototype.readUInt32BE = function readUInt32BE (offset, noAssert) {
  offset = offset >>> 0
  if (!noAssert) checkOffset(offset, 4, this.length)

  return (this[offset] * 0x1000000) +
    ((this[offset + 1] << 16) |
    (this[offset + 2] << 8) |
    this[offset + 3])
}

Buffer.prototype.readIntLE = function readIntLE (offset, byteLength, noAssert) {
  offset = offset >>> 0
  byteLength = byteLength >>> 0
  if (!noAssert) checkOffset(offset, byteLength, this.length)

  var val = this[offset]
  var mul = 1
  var i = 0
  while (++i < byteLength && (mul *= 0x100)) {
    val += this[offset + i] * mul
  }
  mul *= 0x80

  if (val >= mul) val -= Math.pow(2, 8 * byteLength)

  return val
}

Buffer.prototype.readIntBE = function readIntBE (offset, byteLength, noAssert) {
  offset = offset >>> 0
  byteLength = byteLength >>> 0
  if (!noAssert) checkOffset(offset, byteLength, this.length)

  var i = byteLength
  var mul = 1
  var val = this[offset + --i]
  while (i > 0 && (mul *= 0x100)) {
    val += this[offset + --i] * mul
  }
  mul *= 0x80

  if (val >= mul) val -= Math.pow(2, 8 * byteLength)

  return val
}

Buffer.prototype.readInt8 = function readInt8 (offset, noAssert) {
  offset = offset >>> 0
  if (!noAssert) checkOffset(offset, 1, this.length)
  if (!(this[offset] & 0x80)) return (this[offset])
  return ((0xff - this[offset] + 1) * -1)
}

Buffer.prototype.readInt16LE = function readInt16LE (offset, noAssert) {
  offset = offset >>> 0
  if (!noAssert) checkOffset(offset, 2, this.length)
  var val = this[offset] | (this[offset + 1] << 8)
  return (val & 0x8000) ? val | 0xFFFF0000 : val
}

Buffer.prototype.readInt16BE = function readInt16BE (offset, noAssert) {
  offset = offset >>> 0
  if (!noAssert) checkOffset(offset, 2, this.length)
  var val = this[offset + 1] | (this[offset] << 8)
  return (val & 0x8000) ? val | 0xFFFF0000 : val
}

Buffer.prototype.readInt32LE = function readInt32LE (offset, noAssert) {
  offset = offset >>> 0
  if (!noAssert) checkOffset(offset, 4, this.length)

  return (this[offset]) |
    (this[offset + 1] << 8) |
    (this[offset + 2] << 16) |
    (this[offset + 3] << 24)
}

Buffer.prototype.readInt32BE = function readInt32BE (offset, noAssert) {
  offset = offset >>> 0
  if (!noAssert) checkOffset(offset, 4, this.length)

  return (this[offset] << 24) |
    (this[offset + 1] << 16) |
    (this[offset + 2] << 8) |
    (this[offset + 3])
}

Buffer.prototype.readFloatLE = function readFloatLE (offset, noAssert) {
  offset = offset >>> 0
  if (!noAssert) checkOffset(offset, 4, this.length)
  return ieee754.read(this, offset, true, 23, 4)
}

Buffer.prototype.readFloatBE = function readFloatBE (offset, noAssert) {
  offset = offset >>> 0
  if (!noAssert) checkOffset(offset, 4, this.length)
  return ieee754.read(this, offset, false, 23, 4)
}

Buffer.prototype.readDoubleLE = function readDoubleLE (offset, noAssert) {
  offset = offset >>> 0
  if (!noAssert) checkOffset(offset, 8, this.length)
  return ieee754.read(this, offset, true, 52, 8)
}

Buffer.prototype.readDoubleBE = function readDoubleBE (offset, noAssert) {
  offset = offset >>> 0
  if (!noAssert) checkOffset(offset, 8, this.length)
  return ieee754.read(this, offset, false, 52, 8)
}

function checkInt (buf, value, offset, ext, max, min) {
  if (!Buffer.isBuffer(buf)) throw new TypeError('"buffer" argument must be a Buffer instance')
  if (value > max || value < min) throw new RangeError('"value" argument is out of bounds')
  if (offset + ext > buf.length) throw new RangeError('Index out of range')
}

Buffer.prototype.writeUintLE =
Buffer.prototype.writeUIntLE = function writeUIntLE (value, offset, byteLength, noAssert) {
  value = +value
  offset = offset >>> 0
  byteLength = byteLength >>> 0
  if (!noAssert) {
    var maxBytes = Math.pow(2, 8 * byteLength) - 1
    checkInt(this, value, offset, byteLength, maxBytes, 0)
  }

  var mul = 1
  var i = 0
  this[offset] = value & 0xFF
  while (++i < byteLength && (mul *= 0x100)) {
    this[offset + i] = (value / mul) & 0xFF
  }

  return offset + byteLength
}

Buffer.prototype.writeUintBE =
Buffer.prototype.writeUIntBE = function writeUIntBE (value, offset, byteLength, noAssert) {
  value = +value
  offset = offset >>> 0
  byteLength = byteLength >>> 0
  if (!noAssert) {
    var maxBytes = Math.pow(2, 8 * byteLength) - 1
    checkInt(this, value, offset, byteLength, maxBytes, 0)
  }

  var i = byteLength - 1
  var mul = 1
  this[offset + i] = value & 0xFF
  while (--i >= 0 && (mul *= 0x100)) {
    this[offset + i] = (value / mul) & 0xFF
  }

  return offset + byteLength
}

Buffer.prototype.writeUint8 =
Buffer.prototype.writeUInt8 = function writeUInt8 (value, offset, noAssert) {
  value = +value
  offset = offset >>> 0
  if (!noAssert) checkInt(this, value, offset, 1, 0xff, 0)
  this[offset] = (value & 0xff)
  return offset + 1
}

Buffer.prototype.writeUint16LE =
Buffer.prototype.writeUInt16LE = function writeUInt16LE (value, offset, noAssert) {
  value = +value
  offset = offset >>> 0
  if (!noAssert) checkInt(this, value, offset, 2, 0xffff, 0)
  this[offset] = (value & 0xff)
  this[offset + 1] = (value >>> 8)
  return offset + 2
}

Buffer.prototype.writeUint16BE =
Buffer.prototype.writeUInt16BE = function writeUInt16BE (value, offset, noAssert) {
  value = +value
  offset = offset >>> 0
  if (!noAssert) checkInt(this, value, offset, 2, 0xffff, 0)
  this[offset] = (value >>> 8)
  this[offset + 1] = (value & 0xff)
  return offset + 2
}

Buffer.prototype.writeUint32LE =
Buffer.prototype.writeUInt32LE = function writeUInt32LE (value, offset, noAssert) {
  value = +value
  offset = offset >>> 0
  if (!noAssert) checkInt(this, value, offset, 4, 0xffffffff, 0)
  this[offset + 3] = (value >>> 24)
  this[offset + 2] = (value >>> 16)
  this[offset + 1] = (value >>> 8)
  this[offset] = (value & 0xff)
  return offset + 4
}

Buffer.prototype.writeUint32BE =
Buffer.prototype.writeUInt32BE = function writeUInt32BE (value, offset, noAssert) {
  value = +value
  offset = offset >>> 0
  if (!noAssert) checkInt(this, value, offset, 4, 0xffffffff, 0)
  this[offset] = (value >>> 24)
  this[offset + 1] = (value >>> 16)
  this[offset + 2] = (value >>> 8)
  this[offset + 3] = (value & 0xff)
  return offset + 4
}

Buffer.prototype.writeIntLE = function writeIntLE (value, offset, byteLength, noAssert) {
  value = +value
  offset = offset >>> 0
  if (!noAssert) {
    var limit = Math.pow(2, (8 * byteLength) - 1)

    checkInt(this, value, offset, byteLength, limit - 1, -limit)
  }

  var i = 0
  var mul = 1
  var sub = 0
  this[offset] = value & 0xFF
  while (++i < byteLength && (mul *= 0x100)) {
    if (value < 0 && sub === 0 && this[offset + i - 1] !== 0) {
      sub = 1
    }
    this[offset + i] = ((value / mul) >> 0) - sub & 0xFF
  }

  return offset + byteLength
}

Buffer.prototype.writeIntBE = function writeIntBE (value, offset, byteLength, noAssert) {
  value = +value
  offset = offset >>> 0
  if (!noAssert) {
    var limit = Math.pow(2, (8 * byteLength) - 1)

    checkInt(this, value, offset, byteLength, limit - 1, -limit)
  }

  var i = byteLength - 1
  var mul = 1
  var sub = 0
  this[offset + i] = value & 0xFF
  while (--i >= 0 && (mul *= 0x100)) {
    if (value < 0 && sub === 0 && this[offset + i + 1] !== 0) {
      sub = 1
    }
    this[offset + i] = ((value / mul) >> 0) - sub & 0xFF
  }

  return offset + byteLength
}

Buffer.prototype.writeInt8 = function writeInt8 (value, offset, noAssert) {
  value = +value
  offset = offset >>> 0
  if (!noAssert) checkInt(this, value, offset, 1, 0x7f, -0x80)
  if (value < 0) value = 0xff + value + 1
  this[offset] = (value & 0xff)
  return offset + 1
}

Buffer.prototype.writeInt16LE = function writeInt16LE (value, offset, noAssert) {
  value = +value
  offset = offset >>> 0
  if (!noAssert) checkInt(this, value, offset, 2, 0x7fff, -0x8000)
  this[offset] = (value & 0xff)
  this[offset + 1] = (value >>> 8)
  return offset + 2
}

Buffer.prototype.writeInt16BE = function writeInt16BE (value, offset, noAssert) {
  value = +value
  offset = offset >>> 0
  if (!noAssert) checkInt(this, value, offset, 2, 0x7fff, -0x8000)
  this[offset] = (value >>> 8)
  this[offset + 1] = (value & 0xff)
  return offset + 2
}

Buffer.prototype.writeInt32LE = function writeInt32LE (value, offset, noAssert) {
  value = +value
  offset = offset >>> 0
  if (!noAssert) checkInt(this, value, offset, 4, 0x7fffffff, -0x80000000)
  this[offset] = (value & 0xff)
  this[offset + 1] = (value >>> 8)
  this[offset + 2] = (value >>> 16)
  this[offset + 3] = (value >>> 24)
  return offset + 4
}

Buffer.prototype.writeInt32BE = function writeInt32BE (value, offset, noAssert) {
  value = +value
  offset = offset >>> 0
  if (!noAssert) checkInt(this, value, offset, 4, 0x7fffffff, -0x80000000)
  if (value < 0) value = 0xffffffff + value + 1
  this[offset] = (value >>> 24)
  this[offset + 1] = (value >>> 16)
  this[offset + 2] = (value >>> 8)
  this[offset + 3] = (value & 0xff)
  return offset + 4
}

function checkIEEE754 (buf, value, offset, ext, max, min) {
  if (offset + ext > buf.length) throw new RangeError('Index out of range')
  if (offset < 0) throw new RangeError('Index out of range')
}

function writeFloat (buf, value, offset, littleEndian, noAssert) {
  value = +value
  offset = offset >>> 0
  if (!noAssert) {
    checkIEEE754(buf, value, offset, 4, 3.4028234663852886e+38, -3.4028234663852886e+38)
  }
  ieee754.write(buf, value, offset, littleEndian, 23, 4)
  return offset + 4
}

Buffer.prototype.writeFloatLE = function writeFloatLE (value, offset, noAssert) {
  return writeFloat(this, value, offset, true, noAssert)
}

Buffer.prototype.writeFloatBE = function writeFloatBE (value, offset, noAssert) {
  return writeFloat(this, value, offset, false, noAssert)
}

function writeDouble (buf, value, offset, littleEndian, noAssert) {
  value = +value
  offset = offset >>> 0
  if (!noAssert) {
    checkIEEE754(buf, value, offset, 8, 1.7976931348623157E+308, -1.7976931348623157E+308)
  }
  ieee754.write(buf, value, offset, littleEndian, 52, 8)
  return offset + 8
}

Buffer.prototype.writeDoubleLE = function writeDoubleLE (value, offset, noAssert) {
  return writeDouble(this, value, offset, true, noAssert)
}

Buffer.prototype.writeDoubleBE = function writeDoubleBE (value, offset, noAssert) {
  return writeDouble(this, value, offset, false, noAssert)
}

// copy(targetBuffer, targetStart=0, sourceStart=0, sourceEnd=buffer.length)
Buffer.prototype.copy = function copy (target, targetStart, start, end) {
  if (!Buffer.isBuffer(target)) throw new TypeError('argument should be a Buffer')
  if (!start) start = 0
  if (!end && end !== 0) end = this.length
  if (targetStart >= target.length) targetStart = target.length
  if (!targetStart) targetStart = 0
  if (end > 0 && end < start) end = start

  // Copy 0 bytes; we're done
  if (end === start) return 0
  if (target.length === 0 || this.length === 0) return 0

  // Fatal error conditions
  if (targetStart < 0) {
    throw new RangeError('targetStart out of bounds')
  }
  if (start < 0 || start >= this.length) throw new RangeError('Index out of range')
  if (end < 0) throw new RangeError('sourceEnd out of bounds')

  // Are we oob?
  if (end > this.length) end = this.length
  if (target.length - targetStart < end - start) {
    end = target.length - targetStart + start
  }

  var len = end - start

  if (this === target && typeof Uint8Array.prototype.copyWithin === 'function') {
    // Use built-in when available, missing from IE11
    this.copyWithin(targetStart, start, end)
  } else {
    Uint8Array.prototype.set.call(
      target,
      this.subarray(start, end),
      targetStart
    )
  }

  return len
}

// Usage:
//    buffer.fill(number[, offset[, end]])
//    buffer.fill(buffer[, offset[, end]])
//    buffer.fill(string[, offset[, end]][, encoding])
Buffer.prototype.fill = function fill (val, start, end, encoding) {
  // Handle string cases:
  if (typeof val === 'string') {
    if (typeof start === 'string') {
      encoding = start
      start = 0
      end = this.length
    } else if (typeof end === 'string') {
      encoding = end
      end = this.length
    }
    if (encoding !== undefined && typeof encoding !== 'string') {
      throw new TypeError('encoding must be a string')
    }
    if (typeof encoding === 'string' && !Buffer.isEncoding(encoding)) {
      throw new TypeError('Unknown encoding: ' + encoding)
    }
    if (val.length === 1) {
      var code = val.charCodeAt(0)
      if ((encoding === 'utf8' && code < 128) ||
          encoding === 'latin1') {
        // Fast path: If `val` fits into a single byte, use that numeric value.
        val = code
      }
    }
  } else if (typeof val === 'number') {
    val = val & 255
  } else if (typeof val === 'boolean') {
    val = Number(val)
  }

  // Invalid ranges are not set to a default, so can range check early.
  if (start < 0 || this.length < start || this.length < end) {
    throw new RangeError('Out of range index')
  }

  if (end <= start) {
    return this
  }

  start = start >>> 0
  end = end === undefined ? this.length : end >>> 0

  if (!val) val = 0

  var i
  if (typeof val === 'number') {
    for (i = start; i < end; ++i) {
      this[i] = val
    }
  } else {
    var bytes = Buffer.isBuffer(val)
      ? val
      : Buffer.from(val, encoding)
    var len = bytes.length
    if (len === 0) {
      throw new TypeError('The value "' + val +
        '" is invalid for argument "value"')
    }
    for (i = 0; i < end - start; ++i) {
      this[i + start] = bytes[i % len]
    }
  }

  return this
}

// HELPER FUNCTIONS
// ================

var INVALID_BASE64_RE = /[^+/0-9A-Za-z-_]/g

function base64clean (str) {
  // Node takes equal signs as end of the Base64 encoding
  str = str.split('=')[0]
  // Node strips out invalid characters like \n and \t from the string, base64-js does not
  str = str.trim().replace(INVALID_BASE64_RE, '')
  // Node converts strings with length < 2 to ''
  if (str.length < 2) return ''
  // Node allows for non-padded base64 strings (missing trailing ===), base64-js does not
  while (str.length % 4 !== 0) {
    str = str + '='
  }
  return str
}

function utf8ToBytes (string, units) {
  units = units || Infinity
  var codePoint
  var length = string.length
  var leadSurrogate = null
  var bytes = []

  for (var i = 0; i < length; ++i) {
    codePoint = string.charCodeAt(i)

    // is surrogate component
    if (codePoint > 0xD7FF && codePoint < 0xE000) {
      // last char was a lead
      if (!leadSurrogate) {
        // no lead yet
        if (codePoint > 0xDBFF) {
          // unexpected trail
          if ((units -= 3) > -1) bytes.push(0xEF, 0xBF, 0xBD)
          continue
        } else if (i + 1 === length) {
          // unpaired lead
          if ((units -= 3) > -1) bytes.push(0xEF, 0xBF, 0xBD)
          continue
        }

        // valid lead
        leadSurrogate = codePoint

        continue
      }

      // 2 leads in a row
      if (codePoint < 0xDC00) {
        if ((units -= 3) > -1) bytes.push(0xEF, 0xBF, 0xBD)
        leadSurrogate = codePoint
        continue
      }

      // valid surrogate pair
      codePoint = (leadSurrogate - 0xD800 << 10 | codePoint - 0xDC00) + 0x10000
    } else if (leadSurrogate) {
      // valid bmp char, but last char was a lead
      if ((units -= 3) > -1) bytes.push(0xEF, 0xBF, 0xBD)
    }

    leadSurrogate = null

    // encode utf8
    if (codePoint < 0x80) {
      if ((units -= 1) < 0) break
      bytes.push(codePoint)
    } else if (codePoint < 0x800) {
      if ((units -= 2) < 0) break
      bytes.push(
        codePoint >> 0x6 | 0xC0,
        codePoint & 0x3F | 0x80
      )
    } else if (codePoint < 0x10000) {
      if ((units -= 3) < 0) break
      bytes.push(
        codePoint >> 0xC | 0xE0,
        codePoint >> 0x6 & 0x3F | 0x80,
        codePoint & 0x3F | 0x80
      )
    } else if (codePoint < 0x110000) {
      if ((units -= 4) < 0) break
      bytes.push(
        codePoint >> 0x12 | 0xF0,
        codePoint >> 0xC & 0x3F | 0x80,
        codePoint >> 0x6 & 0x3F | 0x80,
        codePoint & 0x3F | 0x80
      )
    } else {
      throw new Error('Invalid code point')
    }
  }

  return bytes
}

function asciiToBytes (str) {
  var byteArray = []
  for (var i = 0; i < str.length; ++i) {
    // Node's code seems to be doing this and not & 0x7F..
    byteArray.push(str.charCodeAt(i) & 0xFF)
  }
  return byteArray
}

function utf16leToBytes (str, units) {
  var c, hi, lo
  var byteArray = []
  for (var i = 0; i < str.length; ++i) {
    if ((units -= 2) < 0) break

    c = str.charCodeAt(i)
    hi = c >> 8
    lo = c % 256
    byteArray.push(lo)
    byteArray.push(hi)
  }

  return byteArray
}

function base64ToBytes (str) {
  return base64.toByteArray(base64clean(str))
}

function blitBuffer (src, dst, offset, length) {
  for (var i = 0; i < length; ++i) {
    if ((i + offset >= dst.length) || (i >= src.length)) break
    dst[i + offset] = src[i]
  }
  return i
}

// ArrayBuffer or Uint8Array objects from other contexts (i.e. iframes) do not pass
// the `instanceof` check but they should be treated as of that type.
// See: https://github.com/feross/buffer/issues/166
function isInstance (obj, type) {
  return obj instanceof type ||
    (obj != null && obj.constructor != null && obj.constructor.name != null &&
      obj.constructor.name === type.name)
}
function numberIsNaN (obj) {
  // For IE11 support
  return obj !== obj // eslint-disable-line no-self-compare
}

// Create lookup table for `toString('hex')`
// See: https://github.com/feross/buffer/issues/219
var hexSliceLookupTable = (function () {
  var alphabet = '0123456789abcdef'
  var table = new Array(256)
  for (var i = 0; i < 16; ++i) {
    var i16 = i * 16
    for (var j = 0; j < 16; ++j) {
      table[i16 + j] = alphabet[i] + alphabet[j]
    }
  }
  return table
})()

}).call(this)}).call(this,require('_process'),typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],require("timers").setImmediate,require("timers").clearImmediate,"/node_modules/buffer/index.js","/node_modules/buffer")

},{"_process":4,"base64-js":1,"buffer":2,"ieee754":3,"timers":5}],3:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,setImmediate,clearImmediate,__filename,__dirname){(function (){
/*! ieee754. BSD-3-Clause License. Feross Aboukhadijeh <https://feross.org/opensource> */
exports.read = function (buffer, offset, isLE, mLen, nBytes) {
  var e, m
  var eLen = (nBytes * 8) - mLen - 1
  var eMax = (1 << eLen) - 1
  var eBias = eMax >> 1
  var nBits = -7
  var i = isLE ? (nBytes - 1) : 0
  var d = isLE ? -1 : 1
  var s = buffer[offset + i]

  i += d

  e = s & ((1 << (-nBits)) - 1)
  s >>= (-nBits)
  nBits += eLen
  for (; nBits > 0; e = (e * 256) + buffer[offset + i], i += d, nBits -= 8) {}

  m = e & ((1 << (-nBits)) - 1)
  e >>= (-nBits)
  nBits += mLen
  for (; nBits > 0; m = (m * 256) + buffer[offset + i], i += d, nBits -= 8) {}

  if (e === 0) {
    e = 1 - eBias
  } else if (e === eMax) {
    return m ? NaN : ((s ? -1 : 1) * Infinity)
  } else {
    m = m + Math.pow(2, mLen)
    e = e - eBias
  }
  return (s ? -1 : 1) * m * Math.pow(2, e - mLen)
}

exports.write = function (buffer, value, offset, isLE, mLen, nBytes) {
  var e, m, c
  var eLen = (nBytes * 8) - mLen - 1
  var eMax = (1 << eLen) - 1
  var eBias = eMax >> 1
  var rt = (mLen === 23 ? Math.pow(2, -24) - Math.pow(2, -77) : 0)
  var i = isLE ? 0 : (nBytes - 1)
  var d = isLE ? 1 : -1
  var s = value < 0 || (value === 0 && 1 / value < 0) ? 1 : 0

  value = Math.abs(value)

  if (isNaN(value) || value === Infinity) {
    m = isNaN(value) ? 1 : 0
    e = eMax
  } else {
    e = Math.floor(Math.log(value) / Math.LN2)
    if (value * (c = Math.pow(2, -e)) < 1) {
      e--
      c *= 2
    }
    if (e + eBias >= 1) {
      value += rt / c
    } else {
      value += rt * Math.pow(2, 1 - eBias)
    }
    if (value * c >= 2) {
      e++
      c /= 2
    }

    if (e + eBias >= eMax) {
      m = 0
      e = eMax
    } else if (e + eBias >= 1) {
      m = ((value * c) - 1) * Math.pow(2, mLen)
      e = e + eBias
    } else {
      m = value * Math.pow(2, eBias - 1) * Math.pow(2, mLen)
      e = 0
    }
  }

  for (; mLen >= 8; buffer[offset + i] = m & 0xff, i += d, m /= 256, mLen -= 8) {}

  e = (e << mLen) | m
  eLen += mLen
  for (; eLen > 0; buffer[offset + i] = e & 0xff, i += d, e /= 256, eLen -= 8) {}

  buffer[offset + i - d] |= s * 128
}

}).call(this)}).call(this,require('_process'),typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],require("timers").setImmediate,require("timers").clearImmediate,"/node_modules/ieee754/index.js","/node_modules/ieee754")

},{"_process":4,"buffer":2,"timers":5}],4:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,setImmediate,clearImmediate,__filename,__dirname){(function (){
// shim for using process in browser
var process = module.exports = {};

// cached from whatever global is present so that test runners that stub it
// don't break things.  But we need to wrap it in a try catch in case it is
// wrapped in strict mode code which doesn't define any globals.  It's inside a
// function because try/catches deoptimize in certain engines.

var cachedSetTimeout;
var cachedClearTimeout;

function defaultSetTimout() {
    throw new Error('setTimeout has not been defined');
}
function defaultClearTimeout () {
    throw new Error('clearTimeout has not been defined');
}
(function () {
    try {
        if (typeof setTimeout === 'function') {
            cachedSetTimeout = setTimeout;
        } else {
            cachedSetTimeout = defaultSetTimout;
        }
    } catch (e) {
        cachedSetTimeout = defaultSetTimout;
    }
    try {
        if (typeof clearTimeout === 'function') {
            cachedClearTimeout = clearTimeout;
        } else {
            cachedClearTimeout = defaultClearTimeout;
        }
    } catch (e) {
        cachedClearTimeout = defaultClearTimeout;
    }
} ())
function runTimeout(fun) {
    if (cachedSetTimeout === setTimeout) {
        //normal enviroments in sane situations
        return setTimeout(fun, 0);
    }
    // if setTimeout wasn't available but was latter defined
    if ((cachedSetTimeout === defaultSetTimout || !cachedSetTimeout) && setTimeout) {
        cachedSetTimeout = setTimeout;
        return setTimeout(fun, 0);
    }
    try {
        // when when somebody has screwed with setTimeout but no I.E. maddness
        return cachedSetTimeout(fun, 0);
    } catch(e){
        try {
            // When we are in I.E. but the script has been evaled so I.E. doesn't trust the global object when called normally
            return cachedSetTimeout.call(null, fun, 0);
        } catch(e){
            // same as above but when it's a version of I.E. that must have the global object for 'this', hopfully our context correct otherwise it will throw a global error
            return cachedSetTimeout.call(this, fun, 0);
        }
    }


}
function runClearTimeout(marker) {
    if (cachedClearTimeout === clearTimeout) {
        //normal enviroments in sane situations
        return clearTimeout(marker);
    }
    // if clearTimeout wasn't available but was latter defined
    if ((cachedClearTimeout === defaultClearTimeout || !cachedClearTimeout) && clearTimeout) {
        cachedClearTimeout = clearTimeout;
        return clearTimeout(marker);
    }
    try {
        // when when somebody has screwed with setTimeout but no I.E. maddness
        return cachedClearTimeout(marker);
    } catch (e){
        try {
            // When we are in I.E. but the script has been evaled so I.E. doesn't  trust the global object when called normally
            return cachedClearTimeout.call(null, marker);
        } catch (e){
            // same as above but when it's a version of I.E. that must have the global object for 'this', hopfully our context correct otherwise it will throw a global error.
            // Some versions of I.E. have different rules for clearTimeout vs setTimeout
            return cachedClearTimeout.call(this, marker);
        }
    }



}
var queue = [];
var draining = false;
var currentQueue;
var queueIndex = -1;

function cleanUpNextTick() {
    if (!draining || !currentQueue) {
        return;
    }
    draining = false;
    if (currentQueue.length) {
        queue = currentQueue.concat(queue);
    } else {
        queueIndex = -1;
    }
    if (queue.length) {
        drainQueue();
    }
}

function drainQueue() {
    if (draining) {
        return;
    }
    var timeout = runTimeout(cleanUpNextTick);
    draining = true;

    var len = queue.length;
    while(len) {
        currentQueue = queue;
        queue = [];
        while (++queueIndex < len) {
            if (currentQueue) {
                currentQueue[queueIndex].run();
            }
        }
        queueIndex = -1;
        len = queue.length;
    }
    currentQueue = null;
    draining = false;
    runClearTimeout(timeout);
}

process.nextTick = function (fun) {
    var args = new Array(arguments.length - 1);
    if (arguments.length > 1) {
        for (var i = 1; i < arguments.length; i++) {
            args[i - 1] = arguments[i];
        }
    }
    queue.push(new Item(fun, args));
    if (queue.length === 1 && !draining) {
        runTimeout(drainQueue);
    }
};

// v8 likes predictible objects
function Item(fun, array) {
    this.fun = fun;
    this.array = array;
}
Item.prototype.run = function () {
    this.fun.apply(null, this.array);
};
process.title = 'browser';
process.browser = true;
process.env = {};
process.argv = [];
process.version = ''; // empty string to avoid regexp issues
process.versions = {};

function noop() {}

process.on = noop;
process.addListener = noop;
process.once = noop;
process.off = noop;
process.removeListener = noop;
process.removeAllListeners = noop;
process.emit = noop;
process.prependListener = noop;
process.prependOnceListener = noop;

process.listeners = function (name) { return [] }

process.binding = function (name) {
    throw new Error('process.binding is not supported');
};

process.cwd = function () { return '/' };
process.chdir = function (dir) {
    throw new Error('process.chdir is not supported');
};
process.umask = function() { return 0; };

}).call(this)}).call(this,require('_process'),typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],require("timers").setImmediate,require("timers").clearImmediate,"/node_modules/process/browser.js","/node_modules/process")

},{"_process":4,"buffer":2,"timers":5}],5:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,setImmediate,clearImmediate,__filename,__dirname){(function (){
var nextTick = require('process/browser.js').nextTick;
var apply = Function.prototype.apply;
var slice = Array.prototype.slice;
var immediateIds = {};
var nextImmediateId = 0;

// DOM APIs, for completeness

exports.setTimeout = function() {
  return new Timeout(apply.call(setTimeout, window, arguments), clearTimeout);
};
exports.setInterval = function() {
  return new Timeout(apply.call(setInterval, window, arguments), clearInterval);
};
exports.clearTimeout =
exports.clearInterval = function(timeout) { timeout.close(); };

function Timeout(id, clearFn) {
  this._id = id;
  this._clearFn = clearFn;
}
Timeout.prototype.unref = Timeout.prototype.ref = function() {};
Timeout.prototype.close = function() {
  this._clearFn.call(window, this._id);
};

// Does not start the time, just sets up the members needed.
exports.enroll = function(item, msecs) {
  clearTimeout(item._idleTimeoutId);
  item._idleTimeout = msecs;
};

exports.unenroll = function(item) {
  clearTimeout(item._idleTimeoutId);
  item._idleTimeout = -1;
};

exports._unrefActive = exports.active = function(item) {
  clearTimeout(item._idleTimeoutId);

  var msecs = item._idleTimeout;
  if (msecs >= 0) {
    item._idleTimeoutId = setTimeout(function onTimeout() {
      if (item._onTimeout)
        item._onTimeout();
    }, msecs);
  }
};

// That's not how node.js implements it but the exposed api is the same.
exports.setImmediate = typeof setImmediate === "function" ? setImmediate : function(fn) {
  var id = nextImmediateId++;
  var args = arguments.length < 2 ? false : slice.call(arguments, 1);

  immediateIds[id] = true;

  nextTick(function onNextTick() {
    if (immediateIds[id]) {
      // fn.call() is faster so we optimize for the common use-case
      // @see http://jsperf.com/call-apply-segu
      if (args) {
        fn.apply(null, args);
      } else {
        fn.call(null);
      }
      // Prevent ids from leaking
      exports.clearImmediate(id);
    }
  });

  return id;
};

exports.clearImmediate = typeof clearImmediate === "function" ? clearImmediate : function(id) {
  delete immediateIds[id];
};
}).call(this)}).call(this,require('_process'),typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],require("timers").setImmediate,require("timers").clearImmediate,"/node_modules/timers-browserify/main.js","/node_modules/timers-browserify")

},{"_process":4,"buffer":2,"process/browser.js":4,"timers":5}],"eq":[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,setImmediate,clearImmediate,__filename,__dirname){(function (){
/*
 * The global eqjs object that contains all eq.js functionality.
 *
 * eqjs.nodes - List of all nodes to act upon when eqjs.states is called
 * eqjs.nodesLength - Number of nodes in eqjs.nodes
 *
 * eqjs.refreshNodes - Call this function to refresh the list of nodes that eq.js should act on
 * eqjs.sortObj - Sorts a key: value object based on value
 * eqjs.query - Runs through all nodes and finds their widths and points
 * eqjs.nodeWrites - Runs through all nodes and writes their eq status
 */
(function (eqjs)
{
  'use strict';

  function EQjs()
  {
    this.nodes = [];
    this.widths = [];
    this.points = [];
    this.callback = undefined;
  }

  /*
   * Add event (cross browser)
   * From http://stackoverflow.com/a/10150042
   */
  function addEvent(elem, event, fn)
  {
    if (elem.addEventListener) {
      elem.addEventListener(event, fn, false);
    } else {
      elem.attachEvent('on' + event, function ()
      {
        // set the this pointer same as addEventListener when fn is called
        return (fn.call(elem, window.event));
      });
    }
  }

  /*
   * Parse Before
   *
   * Reads `:before` content and splits it at the comma
   * From http://jsbin.com/ramiguzefiji/1/edit?html,css,js,output
   */
  function parseBefore(elem)
  {
    return window.getComputedStyle(elem,
        ':before').getPropertyValue('content').slice(1, -1);
  }

  /*
   * Merges two node lists together.
   *
   * From http://stackoverflow.com/questions/914783/javascript-nodelist/17262552#17262552
   */
  var mergeNodes = function (a, b)
  {
    return [].slice.call(a).concat([].slice.call(b));
  };

  /*
   * Query
   *
   * Reads nodes and finds the widths/points
   *  nodes - optional, an array or NodeList of nodes to query
   *  callback - Either boolean (`true`/`false`) to force a normal callback, or a function to use as a callback once query and nodeWrites have finished.
   */
  EQjs.prototype.query = function (nodes, callback)
  {
    var proto = Object.getPrototypeOf(eqjs);
    var length;

    if (callback && typeof(callback) === 'function') {
      proto.callback = callback;
    }

    if (nodes && typeof(nodes) !== 'number') {
      length = nodes.length;
    }
    else {
      nodes = proto.nodes;
      length = proto.nodesLength;
    }
    var widths = [], points = [], i;

    for (i = 0; i < length; i++) {
      var rect = nodes[i].getBoundingClientRect();
      var width = rect.width;
      if (width === undefined) {
        width = rect.right - rect.left;
      }
      widths.push(width);
      try {
        points.push(proto.sortObj(nodes[i].getAttribute('data-eq-pts')));
      }
      catch (e) {
        try {
          points.push(proto.sortObj(parseBefore(nodes[i])));
        }
        catch (e2) {
          points.push([
            {
              key  : '',
              value: 0
            }
          ]);
        }
      }
    }

    proto.widths = widths;
    proto.points = points;

    if (nodes && typeof(nodes) !== 'number') {
      proto.nodeWrites(nodes, widths, points);
    }
    else if (callback && typeof(callback) !== 'function') {
      proto.nodeWrites();
    }
    else {
      window.requestAnimationFrame(proto.nodeWrites);
    }
  };

  /*
   * NodeWrites
   *
   * Writes the data attribute to the object
   *  nodes - optional, an array or NodeList of nodes to query
   *  widths - optional, widths of nodes to use. Only used if `nodes` is passed in
   *  points - optional, points of nodes to use. Only used if `nodes` is passed in
   */
  EQjs.prototype.nodeWrites = function (nodes)
  {
    var i,
        j,
        k,
        length,
        callback,
        eqResizeEvent,
        eqState,
        proto  = Object.getPrototypeOf(eqjs),
        widths = proto.widths,
        points = proto.points;

    if (nodes && typeof(nodes) !== 'number') {
      length = nodes.length;
    }
    else {
      nodes = proto.nodes;
      length = proto.nodesLength;
    }

    for (i = 0; i < length; i++) {
      // Set object width to found width
      var objWidth = widths[i];
      var obj = nodes[i];
      var eqPts = points[i];
      var eqStates = [];

      // Get keys for states
      var eqPtsLength = eqPts.length;

      // Be greedy for smallest state
      if (objWidth < eqPts[0].value) {
        eqState = null;
      }
      // Be greedy for largest state
      else if (objWidth >= eqPts[eqPtsLength - 1].value) {
        for (k = 0; k < eqPtsLength; k++) {
          eqStates.push(eqPts[k].key);
        }
        eqState = eqStates.join(' ');
      }
      // Traverse the states if not found
      else {
        for (j = 0; j < eqPtsLength; j++) {
          var current = eqPts[j];
          var next = eqPts[j + 1];
          eqStates.push(current.key);

          if (j === 0 && objWidth < current.value) {
            eqState = null;
            break;
          }
          else if (next !== undefined && next.value === undefined) {
            eqStates.push(next.key);
            eqState = eqStates.join(' ');
            break;
          }
          else if (objWidth >= current.value && objWidth < next.value) {
            eqState = eqStates.join(' ');
            break;
          }
        }
      }

      // Determine what to set the attribute to
      if (eqState === null) {
        obj.removeAttribute('data-eq-state');
      }
      else {
        obj.setAttribute('data-eq-state', eqState);
      }
      // Set the details of `eqResize`
      eqResizeEvent = new CustomEvent('eqResize', {
        'detail' : eqState,
        'bubbles': true
      });

      // Fire resize event
      obj.dispatchEvent(eqResizeEvent);
    }

    // Run Callback
    if (proto.callback) {
      callback = proto.callback;
      proto.callback = undefined;
      callback(nodes);
    }
  };

  /*
   * Refresh Nodes
   * Refreshes the list of nodes for eqjs to work with
   */
  EQjs.prototype.refreshNodes = function ()
  {
    var proto = Object.getPrototypeOf(eqjs), cssNodes;

    proto.nodes = document.querySelectorAll('[data-eq-pts]');

    cssNodes = parseBefore(document.querySelector('html')).split(', ');
    cssNodes.forEach(function (v)
    {
      if (v !== '') {
        proto.nodes = mergeNodes(proto.nodes, document.querySelectorAll(v));
      }
    });


    proto.nodesLength = proto.nodes.length;
  };

  /*
   * Sort Object
   * Sorts a simple object (key: value) by value and returns a sorted object
   */
  EQjs.prototype.sortObj = function (obj)
  {
    var arr = [];

    var objSplit = obj.split(',');

    for (var i = 0; i < objSplit.length; i++) {
      var sSplit = objSplit[i].split(':');
      arr.push({
        'key'  : sSplit[0].replace(/^\s+|\s+$/g, ''),
        'value': parseFloat(sSplit[1])
      });
    }

    return arr.sort(function (a, b)
    {
      return a.value - b.value;
    });
  };

  /**
   * Query All Nodes
   * Runs refreshNodes and Query
   **/
  EQjs.prototype.all = function (cb)
  {
    var proto = Object.getPrototypeOf(eqjs);
    var hasCB = cb ? true : false;

    proto.refreshNodes();

    if (!hasCB) {
      window.requestAnimationFrame(proto.query);
    }
    else {
      proto.query(undefined, cb);
    }
  };

  /*
   * We only ever want there to be
   * one instance of EQjs in an app
   */
  eqjs = eqjs || new EQjs();

  /*
   * Document Loaded
   *
   * Fires on document load; for HTML based EQs
   */
  addEvent(window, 'DOMContentLoaded', function ()
  {
    eqjs.all(false);
  });

  /*
   * Window Loaded
   */
  addEvent(window, 'load', function ()
  {
    eqjs.all(true);
  });

  /*
   * Window Resize
   *
   * Loop over each `eq-pts` element and pass to eqState
   */
  addEvent(window, 'resize', function ()
  {
    eqjs.all(true);
  });

  // Expose 'eqjs'
  if (typeof module !== 'undefined' && module.exports) {
    module.exports = eqjs;
  } else if (typeof define === 'function' && define.amd) {
    define('eqjs', function ()
    {
      return eqjs;
    });
  } else {
    window.eqjs = eqjs;
  }
})(window.eqjs);

}).call(this)}).call(this,require('_process'),typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],require("timers").setImmediate,require("timers").clearImmediate,"/sources/js-source/libs/eq.js","/sources/js-source/libs")

},{"_process":4,"buffer":2,"timers":5}],"jquery":[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,setImmediate,clearImmediate,__filename,__dirname){(function (){
/*! jQuery v3.2.1 | (c) JS Foundation and other contributors | jquery.org/license */
!function(a,b){"use strict";"object"==typeof module&&"object"==typeof module.exports?module.exports=a.document?b(a,!0):function(a){if(!a.document)throw new Error("jQuery requires a window with a document");return b(a)}:b(a)}("undefined"!=typeof window?window:this,function(a,b){"use strict";var c=[],d=a.document,e=Object.getPrototypeOf,f=c.slice,g=c.concat,h=c.push,i=c.indexOf,j={},k=j.toString,l=j.hasOwnProperty,m=l.toString,n=m.call(Object),o={};function p(a,b){b=b||d;var c=b.createElement("script");c.text=a,b.head.appendChild(c).parentNode.removeChild(c)}var q="3.2.1",r=function(a,b){return new r.fn.init(a,b)},s=/^[\s\uFEFF\xA0]+|[\s\uFEFF\xA0]+$/g,t=/^-ms-/,u=/-([a-z])/g,v=function(a,b){return b.toUpperCase()};r.fn=r.prototype={jquery:q,constructor:r,length:0,toArray:function(){return f.call(this)},get:function(a){return null==a?f.call(this):a<0?this[a+this.length]:this[a]},pushStack:function(a){var b=r.merge(this.constructor(),a);return b.prevObject=this,b},each:function(a){return r.each(this,a)},map:function(a){return this.pushStack(r.map(this,function(b,c){return a.call(b,c,b)}))},slice:function(){return this.pushStack(f.apply(this,arguments))},first:function(){return this.eq(0)},last:function(){return this.eq(-1)},eq:function(a){var b=this.length,c=+a+(a<0?b:0);return this.pushStack(c>=0&&c<b?[this[c]]:[])},end:function(){return this.prevObject||this.constructor()},push:h,sort:c.sort,splice:c.splice},r.extend=r.fn.extend=function(){var a,b,c,d,e,f,g=arguments[0]||{},h=1,i=arguments.length,j=!1;for("boolean"==typeof g&&(j=g,g=arguments[h]||{},h++),"object"==typeof g||r.isFunction(g)||(g={}),h===i&&(g=this,h--);h<i;h++)if(null!=(a=arguments[h]))for(b in a)c=g[b],d=a[b],g!==d&&(j&&d&&(r.isPlainObject(d)||(e=Array.isArray(d)))?(e?(e=!1,f=c&&Array.isArray(c)?c:[]):f=c&&r.isPlainObject(c)?c:{},g[b]=r.extend(j,f,d)):void 0!==d&&(g[b]=d));return g},r.extend({expando:"jQuery"+(q+Math.random()).replace(/\D/g,""),isReady:!0,error:function(a){throw new Error(a)},noop:function(){},isFunction:function(a){return"function"===r.type(a)},isWindow:function(a){return null!=a&&a===a.window},isNumeric:function(a){var b=r.type(a);return("number"===b||"string"===b)&&!isNaN(a-parseFloat(a))},isPlainObject:function(a){var b,c;return!(!a||"[object Object]"!==k.call(a))&&(!(b=e(a))||(c=l.call(b,"constructor")&&b.constructor,"function"==typeof c&&m.call(c)===n))},isEmptyObject:function(a){var b;for(b in a)return!1;return!0},type:function(a){return null==a?a+"":"object"==typeof a||"function"==typeof a?j[k.call(a)]||"object":typeof a},globalEval:function(a){p(a)},camelCase:function(a){return a.replace(t,"ms-").replace(u,v)},each:function(a,b){var c,d=0;if(w(a)){for(c=a.length;d<c;d++)if(b.call(a[d],d,a[d])===!1)break}else for(d in a)if(b.call(a[d],d,a[d])===!1)break;return a},trim:function(a){return null==a?"":(a+"").replace(s,"")},makeArray:function(a,b){var c=b||[];return null!=a&&(w(Object(a))?r.merge(c,"string"==typeof a?[a]:a):h.call(c,a)),c},inArray:function(a,b,c){return null==b?-1:i.call(b,a,c)},merge:function(a,b){for(var c=+b.length,d=0,e=a.length;d<c;d++)a[e++]=b[d];return a.length=e,a},grep:function(a,b,c){for(var d,e=[],f=0,g=a.length,h=!c;f<g;f++)d=!b(a[f],f),d!==h&&e.push(a[f]);return e},map:function(a,b,c){var d,e,f=0,h=[];if(w(a))for(d=a.length;f<d;f++)e=b(a[f],f,c),null!=e&&h.push(e);else for(f in a)e=b(a[f],f,c),null!=e&&h.push(e);return g.apply([],h)},guid:1,proxy:function(a,b){var c,d,e;if("string"==typeof b&&(c=a[b],b=a,a=c),r.isFunction(a))return d=f.call(arguments,2),e=function(){return a.apply(b||this,d.concat(f.call(arguments)))},e.guid=a.guid=a.guid||r.guid++,e},now:Date.now,support:o}),"function"==typeof Symbol&&(r.fn[Symbol.iterator]=c[Symbol.iterator]),r.each("Boolean Number String Function Array Date RegExp Object Error Symbol".split(" "),function(a,b){j["[object "+b+"]"]=b.toLowerCase()});function w(a){var b=!!a&&"length"in a&&a.length,c=r.type(a);return"function"!==c&&!r.isWindow(a)&&("array"===c||0===b||"number"==typeof b&&b>0&&b-1 in a)}var x=function(a){var b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u="sizzle"+1*new Date,v=a.document,w=0,x=0,y=ha(),z=ha(),A=ha(),B=function(a,b){return a===b&&(l=!0),0},C={}.hasOwnProperty,D=[],E=D.pop,F=D.push,G=D.push,H=D.slice,I=function(a,b){for(var c=0,d=a.length;c<d;c++)if(a[c]===b)return c;return-1},J="checked|selected|async|autofocus|autoplay|controls|defer|disabled|hidden|ismap|loop|multiple|open|readonly|required|scoped",K="[\\x20\\t\\r\\n\\f]",L="(?:\\\\.|[\\w-]|[^\0-\\xa0])+",M="\\["+K+"*("+L+")(?:"+K+"*([*^$|!~]?=)"+K+"*(?:'((?:\\\\.|[^\\\\'])*)'|\"((?:\\\\.|[^\\\\\"])*)\"|("+L+"))|)"+K+"*\\]",N=":("+L+")(?:\\((('((?:\\\\.|[^\\\\'])*)'|\"((?:\\\\.|[^\\\\\"])*)\")|((?:\\\\.|[^\\\\()[\\]]|"+M+")*)|.*)\\)|)",O=new RegExp(K+"+","g"),P=new RegExp("^"+K+"+|((?:^|[^\\\\])(?:\\\\.)*)"+K+"+$","g"),Q=new RegExp("^"+K+"*,"+K+"*"),R=new RegExp("^"+K+"*([>+~]|"+K+")"+K+"*"),S=new RegExp("="+K+"*([^\\]'\"]*?)"+K+"*\\]","g"),T=new RegExp(N),U=new RegExp("^"+L+"$"),V={ID:new RegExp("^#("+L+")"),CLASS:new RegExp("^\\.("+L+")"),TAG:new RegExp("^("+L+"|[*])"),ATTR:new RegExp("^"+M),PSEUDO:new RegExp("^"+N),CHILD:new RegExp("^:(only|first|last|nth|nth-last)-(child|of-type)(?:\\("+K+"*(even|odd|(([+-]|)(\\d*)n|)"+K+"*(?:([+-]|)"+K+"*(\\d+)|))"+K+"*\\)|)","i"),bool:new RegExp("^(?:"+J+")$","i"),needsContext:new RegExp("^"+K+"*[>+~]|:(even|odd|eq|gt|lt|nth|first|last)(?:\\("+K+"*((?:-\\d)?\\d*)"+K+"*\\)|)(?=[^-]|$)","i")},W=/^(?:input|select|textarea|button)$/i,X=/^h\d$/i,Y=/^[^{]+\{\s*\[native \w/,Z=/^(?:#([\w-]+)|(\w+)|\.([\w-]+))$/,$=/[+~]/,_=new RegExp("\\\\([\\da-f]{1,6}"+K+"?|("+K+")|.)","ig"),aa=function(a,b,c){var d="0x"+b-65536;return d!==d||c?b:d<0?String.fromCharCode(d+65536):String.fromCharCode(d>>10|55296,1023&d|56320)},ba=/([\0-\x1f\x7f]|^-?\d)|^-$|[^\0-\x1f\x7f-\uFFFF\w-]/g,ca=function(a,b){return b?"\0"===a?"\ufffd":a.slice(0,-1)+"\\"+a.charCodeAt(a.length-1).toString(16)+" ":"\\"+a},da=function(){m()},ea=ta(function(a){return a.disabled===!0&&("form"in a||"label"in a)},{dir:"parentNode",next:"legend"});try{G.apply(D=H.call(v.childNodes),v.childNodes),D[v.childNodes.length].nodeType}catch(fa){G={apply:D.length?function(a,b){F.apply(a,H.call(b))}:function(a,b){var c=a.length,d=0;while(a[c++]=b[d++]);a.length=c-1}}}function ga(a,b,d,e){var f,h,j,k,l,o,r,s=b&&b.ownerDocument,w=b?b.nodeType:9;if(d=d||[],"string"!=typeof a||!a||1!==w&&9!==w&&11!==w)return d;if(!e&&((b?b.ownerDocument||b:v)!==n&&m(b),b=b||n,p)){if(11!==w&&(l=Z.exec(a)))if(f=l[1]){if(9===w){if(!(j=b.getElementById(f)))return d;if(j.id===f)return d.push(j),d}else if(s&&(j=s.getElementById(f))&&t(b,j)&&j.id===f)return d.push(j),d}else{if(l[2])return G.apply(d,b.getElementsByTagName(a)),d;if((f=l[3])&&c.getElementsByClassName&&b.getElementsByClassName)return G.apply(d,b.getElementsByClassName(f)),d}if(c.qsa&&!A[a+" "]&&(!q||!q.test(a))){if(1!==w)s=b,r=a;else if("object"!==b.nodeName.toLowerCase()){(k=b.getAttribute("id"))?k=k.replace(ba,ca):b.setAttribute("id",k=u),o=g(a),h=o.length;while(h--)o[h]="#"+k+" "+sa(o[h]);r=o.join(","),s=$.test(a)&&qa(b.parentNode)||b}if(r)try{return G.apply(d,s.querySelectorAll(r)),d}catch(x){}finally{k===u&&b.removeAttribute("id")}}}return i(a.replace(P,"$1"),b,d,e)}function ha(){var a=[];function b(c,e){return a.push(c+" ")>d.cacheLength&&delete b[a.shift()],b[c+" "]=e}return b}function ia(a){return a[u]=!0,a}function ja(a){var b=n.createElement("fieldset");try{return!!a(b)}catch(c){return!1}finally{b.parentNode&&b.parentNode.removeChild(b),b=null}}function ka(a,b){var c=a.split("|"),e=c.length;while(e--)d.attrHandle[c[e]]=b}function la(a,b){var c=b&&a,d=c&&1===a.nodeType&&1===b.nodeType&&a.sourceIndex-b.sourceIndex;if(d)return d;if(c)while(c=c.nextSibling)if(c===b)return-1;return a?1:-1}function ma(a){return function(b){var c=b.nodeName.toLowerCase();return"input"===c&&b.type===a}}function na(a){return function(b){var c=b.nodeName.toLowerCase();return("input"===c||"button"===c)&&b.type===a}}function oa(a){return function(b){return"form"in b?b.parentNode&&b.disabled===!1?"label"in b?"label"in b.parentNode?b.parentNode.disabled===a:b.disabled===a:b.isDisabled===a||b.isDisabled!==!a&&ea(b)===a:b.disabled===a:"label"in b&&b.disabled===a}}function pa(a){return ia(function(b){return b=+b,ia(function(c,d){var e,f=a([],c.length,b),g=f.length;while(g--)c[e=f[g]]&&(c[e]=!(d[e]=c[e]))})})}function qa(a){return a&&"undefined"!=typeof a.getElementsByTagName&&a}c=ga.support={},f=ga.isXML=function(a){var b=a&&(a.ownerDocument||a).documentElement;return!!b&&"HTML"!==b.nodeName},m=ga.setDocument=function(a){var b,e,g=a?a.ownerDocument||a:v;return g!==n&&9===g.nodeType&&g.documentElement?(n=g,o=n.documentElement,p=!f(n),v!==n&&(e=n.defaultView)&&e.top!==e&&(e.addEventListener?e.addEventListener("unload",da,!1):e.attachEvent&&e.attachEvent("onunload",da)),c.attributes=ja(function(a){return a.className="i",!a.getAttribute("className")}),c.getElementsByTagName=ja(function(a){return a.appendChild(n.createComment("")),!a.getElementsByTagName("*").length}),c.getElementsByClassName=Y.test(n.getElementsByClassName),c.getById=ja(function(a){return o.appendChild(a).id=u,!n.getElementsByName||!n.getElementsByName(u).length}),c.getById?(d.filter.ID=function(a){var b=a.replace(_,aa);return function(a){return a.getAttribute("id")===b}},d.find.ID=function(a,b){if("undefined"!=typeof b.getElementById&&p){var c=b.getElementById(a);return c?[c]:[]}}):(d.filter.ID=function(a){var b=a.replace(_,aa);return function(a){var c="undefined"!=typeof a.getAttributeNode&&a.getAttributeNode("id");return c&&c.value===b}},d.find.ID=function(a,b){if("undefined"!=typeof b.getElementById&&p){var c,d,e,f=b.getElementById(a);if(f){if(c=f.getAttributeNode("id"),c&&c.value===a)return[f];e=b.getElementsByName(a),d=0;while(f=e[d++])if(c=f.getAttributeNode("id"),c&&c.value===a)return[f]}return[]}}),d.find.TAG=c.getElementsByTagName?function(a,b){return"undefined"!=typeof b.getElementsByTagName?b.getElementsByTagName(a):c.qsa?b.querySelectorAll(a):void 0}:function(a,b){var c,d=[],e=0,f=b.getElementsByTagName(a);if("*"===a){while(c=f[e++])1===c.nodeType&&d.push(c);return d}return f},d.find.CLASS=c.getElementsByClassName&&function(a,b){if("undefined"!=typeof b.getElementsByClassName&&p)return b.getElementsByClassName(a)},r=[],q=[],(c.qsa=Y.test(n.querySelectorAll))&&(ja(function(a){o.appendChild(a).innerHTML="<a id='"+u+"'></a><select id='"+u+"-\r\\' msallowcapture=''><option selected=''></option></select>",a.querySelectorAll("[msallowcapture^='']").length&&q.push("[*^$]="+K+"*(?:''|\"\")"),a.querySelectorAll("[selected]").length||q.push("\\["+K+"*(?:value|"+J+")"),a.querySelectorAll("[id~="+u+"-]").length||q.push("~="),a.querySelectorAll(":checked").length||q.push(":checked"),a.querySelectorAll("a#"+u+"+*").length||q.push(".#.+[+~]")}),ja(function(a){a.innerHTML="<a href='' disabled='disabled'></a><select disabled='disabled'><option/></select>";var b=n.createElement("input");b.setAttribute("type","hidden"),a.appendChild(b).setAttribute("name","D"),a.querySelectorAll("[name=d]").length&&q.push("name"+K+"*[*^$|!~]?="),2!==a.querySelectorAll(":enabled").length&&q.push(":enabled",":disabled"),o.appendChild(a).disabled=!0,2!==a.querySelectorAll(":disabled").length&&q.push(":enabled",":disabled"),a.querySelectorAll("*,:x"),q.push(",.*:")})),(c.matchesSelector=Y.test(s=o.matches||o.webkitMatchesSelector||o.mozMatchesSelector||o.oMatchesSelector||o.msMatchesSelector))&&ja(function(a){c.disconnectedMatch=s.call(a,"*"),s.call(a,"[s!='']:x"),r.push("!=",N)}),q=q.length&&new RegExp(q.join("|")),r=r.length&&new RegExp(r.join("|")),b=Y.test(o.compareDocumentPosition),t=b||Y.test(o.contains)?function(a,b){var c=9===a.nodeType?a.documentElement:a,d=b&&b.parentNode;return a===d||!(!d||1!==d.nodeType||!(c.contains?c.contains(d):a.compareDocumentPosition&&16&a.compareDocumentPosition(d)))}:function(a,b){if(b)while(b=b.parentNode)if(b===a)return!0;return!1},B=b?function(a,b){if(a===b)return l=!0,0;var d=!a.compareDocumentPosition-!b.compareDocumentPosition;return d?d:(d=(a.ownerDocument||a)===(b.ownerDocument||b)?a.compareDocumentPosition(b):1,1&d||!c.sortDetached&&b.compareDocumentPosition(a)===d?a===n||a.ownerDocument===v&&t(v,a)?-1:b===n||b.ownerDocument===v&&t(v,b)?1:k?I(k,a)-I(k,b):0:4&d?-1:1)}:function(a,b){if(a===b)return l=!0,0;var c,d=0,e=a.parentNode,f=b.parentNode,g=[a],h=[b];if(!e||!f)return a===n?-1:b===n?1:e?-1:f?1:k?I(k,a)-I(k,b):0;if(e===f)return la(a,b);c=a;while(c=c.parentNode)g.unshift(c);c=b;while(c=c.parentNode)h.unshift(c);while(g[d]===h[d])d++;return d?la(g[d],h[d]):g[d]===v?-1:h[d]===v?1:0},n):n},ga.matches=function(a,b){return ga(a,null,null,b)},ga.matchesSelector=function(a,b){if((a.ownerDocument||a)!==n&&m(a),b=b.replace(S,"='$1']"),c.matchesSelector&&p&&!A[b+" "]&&(!r||!r.test(b))&&(!q||!q.test(b)))try{var d=s.call(a,b);if(d||c.disconnectedMatch||a.document&&11!==a.document.nodeType)return d}catch(e){}return ga(b,n,null,[a]).length>0},ga.contains=function(a,b){return(a.ownerDocument||a)!==n&&m(a),t(a,b)},ga.attr=function(a,b){(a.ownerDocument||a)!==n&&m(a);var e=d.attrHandle[b.toLowerCase()],f=e&&C.call(d.attrHandle,b.toLowerCase())?e(a,b,!p):void 0;return void 0!==f?f:c.attributes||!p?a.getAttribute(b):(f=a.getAttributeNode(b))&&f.specified?f.value:null},ga.escape=function(a){return(a+"").replace(ba,ca)},ga.error=function(a){throw new Error("Syntax error, unrecognized expression: "+a)},ga.uniqueSort=function(a){var b,d=[],e=0,f=0;if(l=!c.detectDuplicates,k=!c.sortStable&&a.slice(0),a.sort(B),l){while(b=a[f++])b===a[f]&&(e=d.push(f));while(e--)a.splice(d[e],1)}return k=null,a},e=ga.getText=function(a){var b,c="",d=0,f=a.nodeType;if(f){if(1===f||9===f||11===f){if("string"==typeof a.textContent)return a.textContent;for(a=a.firstChild;a;a=a.nextSibling)c+=e(a)}else if(3===f||4===f)return a.nodeValue}else while(b=a[d++])c+=e(b);return c},d=ga.selectors={cacheLength:50,createPseudo:ia,match:V,attrHandle:{},find:{},relative:{">":{dir:"parentNode",first:!0}," ":{dir:"parentNode"},"+":{dir:"previousSibling",first:!0},"~":{dir:"previousSibling"}},preFilter:{ATTR:function(a){return a[1]=a[1].replace(_,aa),a[3]=(a[3]||a[4]||a[5]||"").replace(_,aa),"~="===a[2]&&(a[3]=" "+a[3]+" "),a.slice(0,4)},CHILD:function(a){return a[1]=a[1].toLowerCase(),"nth"===a[1].slice(0,3)?(a[3]||ga.error(a[0]),a[4]=+(a[4]?a[5]+(a[6]||1):2*("even"===a[3]||"odd"===a[3])),a[5]=+(a[7]+a[8]||"odd"===a[3])):a[3]&&ga.error(a[0]),a},PSEUDO:function(a){var b,c=!a[6]&&a[2];return V.CHILD.test(a[0])?null:(a[3]?a[2]=a[4]||a[5]||"":c&&T.test(c)&&(b=g(c,!0))&&(b=c.indexOf(")",c.length-b)-c.length)&&(a[0]=a[0].slice(0,b),a[2]=c.slice(0,b)),a.slice(0,3))}},filter:{TAG:function(a){var b=a.replace(_,aa).toLowerCase();return"*"===a?function(){return!0}:function(a){return a.nodeName&&a.nodeName.toLowerCase()===b}},CLASS:function(a){var b=y[a+" "];return b||(b=new RegExp("(^|"+K+")"+a+"("+K+"|$)"))&&y(a,function(a){return b.test("string"==typeof a.className&&a.className||"undefined"!=typeof a.getAttribute&&a.getAttribute("class")||"")})},ATTR:function(a,b,c){return function(d){var e=ga.attr(d,a);return null==e?"!="===b:!b||(e+="","="===b?e===c:"!="===b?e!==c:"^="===b?c&&0===e.indexOf(c):"*="===b?c&&e.indexOf(c)>-1:"$="===b?c&&e.slice(-c.length)===c:"~="===b?(" "+e.replace(O," ")+" ").indexOf(c)>-1:"|="===b&&(e===c||e.slice(0,c.length+1)===c+"-"))}},CHILD:function(a,b,c,d,e){var f="nth"!==a.slice(0,3),g="last"!==a.slice(-4),h="of-type"===b;return 1===d&&0===e?function(a){return!!a.parentNode}:function(b,c,i){var j,k,l,m,n,o,p=f!==g?"nextSibling":"previousSibling",q=b.parentNode,r=h&&b.nodeName.toLowerCase(),s=!i&&!h,t=!1;if(q){if(f){while(p){m=b;while(m=m[p])if(h?m.nodeName.toLowerCase()===r:1===m.nodeType)return!1;o=p="only"===a&&!o&&"nextSibling"}return!0}if(o=[g?q.firstChild:q.lastChild],g&&s){m=q,l=m[u]||(m[u]={}),k=l[m.uniqueID]||(l[m.uniqueID]={}),j=k[a]||[],n=j[0]===w&&j[1],t=n&&j[2],m=n&&q.childNodes[n];while(m=++n&&m&&m[p]||(t=n=0)||o.pop())if(1===m.nodeType&&++t&&m===b){k[a]=[w,n,t];break}}else if(s&&(m=b,l=m[u]||(m[u]={}),k=l[m.uniqueID]||(l[m.uniqueID]={}),j=k[a]||[],n=j[0]===w&&j[1],t=n),t===!1)while(m=++n&&m&&m[p]||(t=n=0)||o.pop())if((h?m.nodeName.toLowerCase()===r:1===m.nodeType)&&++t&&(s&&(l=m[u]||(m[u]={}),k=l[m.uniqueID]||(l[m.uniqueID]={}),k[a]=[w,t]),m===b))break;return t-=e,t===d||t%d===0&&t/d>=0}}},PSEUDO:function(a,b){var c,e=d.pseudos[a]||d.setFilters[a.toLowerCase()]||ga.error("unsupported pseudo: "+a);return e[u]?e(b):e.length>1?(c=[a,a,"",b],d.setFilters.hasOwnProperty(a.toLowerCase())?ia(function(a,c){var d,f=e(a,b),g=f.length;while(g--)d=I(a,f[g]),a[d]=!(c[d]=f[g])}):function(a){return e(a,0,c)}):e}},pseudos:{not:ia(function(a){var b=[],c=[],d=h(a.replace(P,"$1"));return d[u]?ia(function(a,b,c,e){var f,g=d(a,null,e,[]),h=a.length;while(h--)(f=g[h])&&(a[h]=!(b[h]=f))}):function(a,e,f){return b[0]=a,d(b,null,f,c),b[0]=null,!c.pop()}}),has:ia(function(a){return function(b){return ga(a,b).length>0}}),contains:ia(function(a){return a=a.replace(_,aa),function(b){return(b.textContent||b.innerText||e(b)).indexOf(a)>-1}}),lang:ia(function(a){return U.test(a||"")||ga.error("unsupported lang: "+a),a=a.replace(_,aa).toLowerCase(),function(b){var c;do if(c=p?b.lang:b.getAttribute("xml:lang")||b.getAttribute("lang"))return c=c.toLowerCase(),c===a||0===c.indexOf(a+"-");while((b=b.parentNode)&&1===b.nodeType);return!1}}),target:function(b){var c=a.location&&a.location.hash;return c&&c.slice(1)===b.id},root:function(a){return a===o},focus:function(a){return a===n.activeElement&&(!n.hasFocus||n.hasFocus())&&!!(a.type||a.href||~a.tabIndex)},enabled:oa(!1),disabled:oa(!0),checked:function(a){var b=a.nodeName.toLowerCase();return"input"===b&&!!a.checked||"option"===b&&!!a.selected},selected:function(a){return a.parentNode&&a.parentNode.selectedIndex,a.selected===!0},empty:function(a){for(a=a.firstChild;a;a=a.nextSibling)if(a.nodeType<6)return!1;return!0},parent:function(a){return!d.pseudos.empty(a)},header:function(a){return X.test(a.nodeName)},input:function(a){return W.test(a.nodeName)},button:function(a){var b=a.nodeName.toLowerCase();return"input"===b&&"button"===a.type||"button"===b},text:function(a){var b;return"input"===a.nodeName.toLowerCase()&&"text"===a.type&&(null==(b=a.getAttribute("type"))||"text"===b.toLowerCase())},first:pa(function(){return[0]}),last:pa(function(a,b){return[b-1]}),eq:pa(function(a,b,c){return[c<0?c+b:c]}),even:pa(function(a,b){for(var c=0;c<b;c+=2)a.push(c);return a}),odd:pa(function(a,b){for(var c=1;c<b;c+=2)a.push(c);return a}),lt:pa(function(a,b,c){for(var d=c<0?c+b:c;--d>=0;)a.push(d);return a}),gt:pa(function(a,b,c){for(var d=c<0?c+b:c;++d<b;)a.push(d);return a})}},d.pseudos.nth=d.pseudos.eq;for(b in{radio:!0,checkbox:!0,file:!0,password:!0,image:!0})d.pseudos[b]=ma(b);for(b in{submit:!0,reset:!0})d.pseudos[b]=na(b);function ra(){}ra.prototype=d.filters=d.pseudos,d.setFilters=new ra,g=ga.tokenize=function(a,b){var c,e,f,g,h,i,j,k=z[a+" "];if(k)return b?0:k.slice(0);h=a,i=[],j=d.preFilter;while(h){c&&!(e=Q.exec(h))||(e&&(h=h.slice(e[0].length)||h),i.push(f=[])),c=!1,(e=R.exec(h))&&(c=e.shift(),f.push({value:c,type:e[0].replace(P," ")}),h=h.slice(c.length));for(g in d.filter)!(e=V[g].exec(h))||j[g]&&!(e=j[g](e))||(c=e.shift(),f.push({value:c,type:g,matches:e}),h=h.slice(c.length));if(!c)break}return b?h.length:h?ga.error(a):z(a,i).slice(0)};function sa(a){for(var b=0,c=a.length,d="";b<c;b++)d+=a[b].value;return d}function ta(a,b,c){var d=b.dir,e=b.next,f=e||d,g=c&&"parentNode"===f,h=x++;return b.first?function(b,c,e){while(b=b[d])if(1===b.nodeType||g)return a(b,c,e);return!1}:function(b,c,i){var j,k,l,m=[w,h];if(i){while(b=b[d])if((1===b.nodeType||g)&&a(b,c,i))return!0}else while(b=b[d])if(1===b.nodeType||g)if(l=b[u]||(b[u]={}),k=l[b.uniqueID]||(l[b.uniqueID]={}),e&&e===b.nodeName.toLowerCase())b=b[d]||b;else{if((j=k[f])&&j[0]===w&&j[1]===h)return m[2]=j[2];if(k[f]=m,m[2]=a(b,c,i))return!0}return!1}}function ua(a){return a.length>1?function(b,c,d){var e=a.length;while(e--)if(!a[e](b,c,d))return!1;return!0}:a[0]}function va(a,b,c){for(var d=0,e=b.length;d<e;d++)ga(a,b[d],c);return c}function wa(a,b,c,d,e){for(var f,g=[],h=0,i=a.length,j=null!=b;h<i;h++)(f=a[h])&&(c&&!c(f,d,e)||(g.push(f),j&&b.push(h)));return g}function xa(a,b,c,d,e,f){return d&&!d[u]&&(d=xa(d)),e&&!e[u]&&(e=xa(e,f)),ia(function(f,g,h,i){var j,k,l,m=[],n=[],o=g.length,p=f||va(b||"*",h.nodeType?[h]:h,[]),q=!a||!f&&b?p:wa(p,m,a,h,i),r=c?e||(f?a:o||d)?[]:g:q;if(c&&c(q,r,h,i),d){j=wa(r,n),d(j,[],h,i),k=j.length;while(k--)(l=j[k])&&(r[n[k]]=!(q[n[k]]=l))}if(f){if(e||a){if(e){j=[],k=r.length;while(k--)(l=r[k])&&j.push(q[k]=l);e(null,r=[],j,i)}k=r.length;while(k--)(l=r[k])&&(j=e?I(f,l):m[k])>-1&&(f[j]=!(g[j]=l))}}else r=wa(r===g?r.splice(o,r.length):r),e?e(null,g,r,i):G.apply(g,r)})}function ya(a){for(var b,c,e,f=a.length,g=d.relative[a[0].type],h=g||d.relative[" "],i=g?1:0,k=ta(function(a){return a===b},h,!0),l=ta(function(a){return I(b,a)>-1},h,!0),m=[function(a,c,d){var e=!g&&(d||c!==j)||((b=c).nodeType?k(a,c,d):l(a,c,d));return b=null,e}];i<f;i++)if(c=d.relative[a[i].type])m=[ta(ua(m),c)];else{if(c=d.filter[a[i].type].apply(null,a[i].matches),c[u]){for(e=++i;e<f;e++)if(d.relative[a[e].type])break;return xa(i>1&&ua(m),i>1&&sa(a.slice(0,i-1).concat({value:" "===a[i-2].type?"*":""})).replace(P,"$1"),c,i<e&&ya(a.slice(i,e)),e<f&&ya(a=a.slice(e)),e<f&&sa(a))}m.push(c)}return ua(m)}function za(a,b){var c=b.length>0,e=a.length>0,f=function(f,g,h,i,k){var l,o,q,r=0,s="0",t=f&&[],u=[],v=j,x=f||e&&d.find.TAG("*",k),y=w+=null==v?1:Math.random()||.1,z=x.length;for(k&&(j=g===n||g||k);s!==z&&null!=(l=x[s]);s++){if(e&&l){o=0,g||l.ownerDocument===n||(m(l),h=!p);while(q=a[o++])if(q(l,g||n,h)){i.push(l);break}k&&(w=y)}c&&((l=!q&&l)&&r--,f&&t.push(l))}if(r+=s,c&&s!==r){o=0;while(q=b[o++])q(t,u,g,h);if(f){if(r>0)while(s--)t[s]||u[s]||(u[s]=E.call(i));u=wa(u)}G.apply(i,u),k&&!f&&u.length>0&&r+b.length>1&&ga.uniqueSort(i)}return k&&(w=y,j=v),t};return c?ia(f):f}return h=ga.compile=function(a,b){var c,d=[],e=[],f=A[a+" "];if(!f){b||(b=g(a)),c=b.length;while(c--)f=ya(b[c]),f[u]?d.push(f):e.push(f);f=A(a,za(e,d)),f.selector=a}return f},i=ga.select=function(a,b,c,e){var f,i,j,k,l,m="function"==typeof a&&a,n=!e&&g(a=m.selector||a);if(c=c||[],1===n.length){if(i=n[0]=n[0].slice(0),i.length>2&&"ID"===(j=i[0]).type&&9===b.nodeType&&p&&d.relative[i[1].type]){if(b=(d.find.ID(j.matches[0].replace(_,aa),b)||[])[0],!b)return c;m&&(b=b.parentNode),a=a.slice(i.shift().value.length)}f=V.needsContext.test(a)?0:i.length;while(f--){if(j=i[f],d.relative[k=j.type])break;if((l=d.find[k])&&(e=l(j.matches[0].replace(_,aa),$.test(i[0].type)&&qa(b.parentNode)||b))){if(i.splice(f,1),a=e.length&&sa(i),!a)return G.apply(c,e),c;break}}}return(m||h(a,n))(e,b,!p,c,!b||$.test(a)&&qa(b.parentNode)||b),c},c.sortStable=u.split("").sort(B).join("")===u,c.detectDuplicates=!!l,m(),c.sortDetached=ja(function(a){return 1&a.compareDocumentPosition(n.createElement("fieldset"))}),ja(function(a){return a.innerHTML="<a href='#'></a>","#"===a.firstChild.getAttribute("href")})||ka("type|href|height|width",function(a,b,c){if(!c)return a.getAttribute(b,"type"===b.toLowerCase()?1:2)}),c.attributes&&ja(function(a){return a.innerHTML="<input/>",a.firstChild.setAttribute("value",""),""===a.firstChild.getAttribute("value")})||ka("value",function(a,b,c){if(!c&&"input"===a.nodeName.toLowerCase())return a.defaultValue}),ja(function(a){return null==a.getAttribute("disabled")})||ka(J,function(a,b,c){var d;if(!c)return a[b]===!0?b.toLowerCase():(d=a.getAttributeNode(b))&&d.specified?d.value:null}),ga}(a);r.find=x,r.expr=x.selectors,r.expr[":"]=r.expr.pseudos,r.uniqueSort=r.unique=x.uniqueSort,r.text=x.getText,r.isXMLDoc=x.isXML,r.contains=x.contains,r.escapeSelector=x.escape;var y=function(a,b,c){var d=[],e=void 0!==c;while((a=a[b])&&9!==a.nodeType)if(1===a.nodeType){if(e&&r(a).is(c))break;d.push(a)}return d},z=function(a,b){for(var c=[];a;a=a.nextSibling)1===a.nodeType&&a!==b&&c.push(a);return c},A=r.expr.match.needsContext;function B(a,b){return a.nodeName&&a.nodeName.toLowerCase()===b.toLowerCase()}var C=/^<([a-z][^\/\0>:\x20\t\r\n\f]*)[\x20\t\r\n\f]*\/?>(?:<\/\1>|)$/i,D=/^.[^:#\[\.,]*$/;function E(a,b,c){return r.isFunction(b)?r.grep(a,function(a,d){return!!b.call(a,d,a)!==c}):b.nodeType?r.grep(a,function(a){return a===b!==c}):"string"!=typeof b?r.grep(a,function(a){return i.call(b,a)>-1!==c}):D.test(b)?r.filter(b,a,c):(b=r.filter(b,a),r.grep(a,function(a){return i.call(b,a)>-1!==c&&1===a.nodeType}))}r.filter=function(a,b,c){var d=b[0];return c&&(a=":not("+a+")"),1===b.length&&1===d.nodeType?r.find.matchesSelector(d,a)?[d]:[]:r.find.matches(a,r.grep(b,function(a){return 1===a.nodeType}))},r.fn.extend({find:function(a){var b,c,d=this.length,e=this;if("string"!=typeof a)return this.pushStack(r(a).filter(function(){for(b=0;b<d;b++)if(r.contains(e[b],this))return!0}));for(c=this.pushStack([]),b=0;b<d;b++)r.find(a,e[b],c);return d>1?r.uniqueSort(c):c},filter:function(a){return this.pushStack(E(this,a||[],!1))},not:function(a){return this.pushStack(E(this,a||[],!0))},is:function(a){return!!E(this,"string"==typeof a&&A.test(a)?r(a):a||[],!1).length}});var F,G=/^(?:\s*(<[\w\W]+>)[^>]*|#([\w-]+))$/,H=r.fn.init=function(a,b,c){var e,f;if(!a)return this;if(c=c||F,"string"==typeof a){if(e="<"===a[0]&&">"===a[a.length-1]&&a.length>=3?[null,a,null]:G.exec(a),!e||!e[1]&&b)return!b||b.jquery?(b||c).find(a):this.constructor(b).find(a);if(e[1]){if(b=b instanceof r?b[0]:b,r.merge(this,r.parseHTML(e[1],b&&b.nodeType?b.ownerDocument||b:d,!0)),C.test(e[1])&&r.isPlainObject(b))for(e in b)r.isFunction(this[e])?this[e](b[e]):this.attr(e,b[e]);return this}return f=d.getElementById(e[2]),f&&(this[0]=f,this.length=1),this}return a.nodeType?(this[0]=a,this.length=1,this):r.isFunction(a)?void 0!==c.ready?c.ready(a):a(r):r.makeArray(a,this)};H.prototype=r.fn,F=r(d);var I=/^(?:parents|prev(?:Until|All))/,J={children:!0,contents:!0,next:!0,prev:!0};r.fn.extend({has:function(a){var b=r(a,this),c=b.length;return this.filter(function(){for(var a=0;a<c;a++)if(r.contains(this,b[a]))return!0})},closest:function(a,b){var c,d=0,e=this.length,f=[],g="string"!=typeof a&&r(a);if(!A.test(a))for(;d<e;d++)for(c=this[d];c&&c!==b;c=c.parentNode)if(c.nodeType<11&&(g?g.index(c)>-1:1===c.nodeType&&r.find.matchesSelector(c,a))){f.push(c);break}return this.pushStack(f.length>1?r.uniqueSort(f):f)},index:function(a){return a?"string"==typeof a?i.call(r(a),this[0]):i.call(this,a.jquery?a[0]:a):this[0]&&this[0].parentNode?this.first().prevAll().length:-1},add:function(a,b){return this.pushStack(r.uniqueSort(r.merge(this.get(),r(a,b))))},addBack:function(a){return this.add(null==a?this.prevObject:this.prevObject.filter(a))}});function K(a,b){while((a=a[b])&&1!==a.nodeType);return a}r.each({parent:function(a){var b=a.parentNode;return b&&11!==b.nodeType?b:null},parents:function(a){return y(a,"parentNode")},parentsUntil:function(a,b,c){return y(a,"parentNode",c)},next:function(a){return K(a,"nextSibling")},prev:function(a){return K(a,"previousSibling")},nextAll:function(a){return y(a,"nextSibling")},prevAll:function(a){return y(a,"previousSibling")},nextUntil:function(a,b,c){return y(a,"nextSibling",c)},prevUntil:function(a,b,c){return y(a,"previousSibling",c)},siblings:function(a){return z((a.parentNode||{}).firstChild,a)},children:function(a){return z(a.firstChild)},contents:function(a){return B(a,"iframe")?a.contentDocument:(B(a,"template")&&(a=a.content||a),r.merge([],a.childNodes))}},function(a,b){r.fn[a]=function(c,d){var e=r.map(this,b,c);return"Until"!==a.slice(-5)&&(d=c),d&&"string"==typeof d&&(e=r.filter(d,e)),this.length>1&&(J[a]||r.uniqueSort(e),I.test(a)&&e.reverse()),this.pushStack(e)}});var L=/[^\x20\t\r\n\f]+/g;function M(a){var b={};return r.each(a.match(L)||[],function(a,c){b[c]=!0}),b}r.Callbacks=function(a){a="string"==typeof a?M(a):r.extend({},a);var b,c,d,e,f=[],g=[],h=-1,i=function(){for(e=e||a.once,d=b=!0;g.length;h=-1){c=g.shift();while(++h<f.length)f[h].apply(c[0],c[1])===!1&&a.stopOnFalse&&(h=f.length,c=!1)}a.memory||(c=!1),b=!1,e&&(f=c?[]:"")},j={add:function(){return f&&(c&&!b&&(h=f.length-1,g.push(c)),function d(b){r.each(b,function(b,c){r.isFunction(c)?a.unique&&j.has(c)||f.push(c):c&&c.length&&"string"!==r.type(c)&&d(c)})}(arguments),c&&!b&&i()),this},remove:function(){return r.each(arguments,function(a,b){var c;while((c=r.inArray(b,f,c))>-1)f.splice(c,1),c<=h&&h--}),this},has:function(a){return a?r.inArray(a,f)>-1:f.length>0},empty:function(){return f&&(f=[]),this},disable:function(){return e=g=[],f=c="",this},disabled:function(){return!f},lock:function(){return e=g=[],c||b||(f=c=""),this},locked:function(){return!!e},fireWith:function(a,c){return e||(c=c||[],c=[a,c.slice?c.slice():c],g.push(c),b||i()),this},fire:function(){return j.fireWith(this,arguments),this},fired:function(){return!!d}};return j};function N(a){return a}function O(a){throw a}function P(a,b,c,d){var e;try{a&&r.isFunction(e=a.promise)?e.call(a).done(b).fail(c):a&&r.isFunction(e=a.then)?e.call(a,b,c):b.apply(void 0,[a].slice(d))}catch(a){c.apply(void 0,[a])}}r.extend({Deferred:function(b){var c=[["notify","progress",r.Callbacks("memory"),r.Callbacks("memory"),2],["resolve","done",r.Callbacks("once memory"),r.Callbacks("once memory"),0,"resolved"],["reject","fail",r.Callbacks("once memory"),r.Callbacks("once memory"),1,"rejected"]],d="pending",e={state:function(){return d},always:function(){return f.done(arguments).fail(arguments),this},"catch":function(a){return e.then(null,a)},pipe:function(){var a=arguments;return r.Deferred(function(b){r.each(c,function(c,d){var e=r.isFunction(a[d[4]])&&a[d[4]];f[d[1]](function(){var a=e&&e.apply(this,arguments);a&&r.isFunction(a.promise)?a.promise().progress(b.notify).done(b.resolve).fail(b.reject):b[d[0]+"With"](this,e?[a]:arguments)})}),a=null}).promise()},then:function(b,d,e){var f=0;function g(b,c,d,e){return function(){var h=this,i=arguments,j=function(){var a,j;if(!(b<f)){if(a=d.apply(h,i),a===c.promise())throw new TypeError("Thenable self-resolution");j=a&&("object"==typeof a||"function"==typeof a)&&a.then,r.isFunction(j)?e?j.call(a,g(f,c,N,e),g(f,c,O,e)):(f++,j.call(a,g(f,c,N,e),g(f,c,O,e),g(f,c,N,c.notifyWith))):(d!==N&&(h=void 0,i=[a]),(e||c.resolveWith)(h,i))}},k=e?j:function(){try{j()}catch(a){r.Deferred.exceptionHook&&r.Deferred.exceptionHook(a,k.stackTrace),b+1>=f&&(d!==O&&(h=void 0,i=[a]),c.rejectWith(h,i))}};b?k():(r.Deferred.getStackHook&&(k.stackTrace=r.Deferred.getStackHook()),a.setTimeout(k))}}return r.Deferred(function(a){c[0][3].add(g(0,a,r.isFunction(e)?e:N,a.notifyWith)),c[1][3].add(g(0,a,r.isFunction(b)?b:N)),c[2][3].add(g(0,a,r.isFunction(d)?d:O))}).promise()},promise:function(a){return null!=a?r.extend(a,e):e}},f={};return r.each(c,function(a,b){var g=b[2],h=b[5];e[b[1]]=g.add,h&&g.add(function(){d=h},c[3-a][2].disable,c[0][2].lock),g.add(b[3].fire),f[b[0]]=function(){return f[b[0]+"With"](this===f?void 0:this,arguments),this},f[b[0]+"With"]=g.fireWith}),e.promise(f),b&&b.call(f,f),f},when:function(a){var b=arguments.length,c=b,d=Array(c),e=f.call(arguments),g=r.Deferred(),h=function(a){return function(c){d[a]=this,e[a]=arguments.length>1?f.call(arguments):c,--b||g.resolveWith(d,e)}};if(b<=1&&(P(a,g.done(h(c)).resolve,g.reject,!b),"pending"===g.state()||r.isFunction(e[c]&&e[c].then)))return g.then();while(c--)P(e[c],h(c),g.reject);return g.promise()}});var Q=/^(Eval|Internal|Range|Reference|Syntax|Type|URI)Error$/;r.Deferred.exceptionHook=function(b,c){a.console&&a.console.warn&&b&&Q.test(b.name)&&a.console.warn("jQuery.Deferred exception: "+b.message,b.stack,c)},r.readyException=function(b){a.setTimeout(function(){throw b})};var R=r.Deferred();r.fn.ready=function(a){return R.then(a)["catch"](function(a){r.readyException(a)}),this},r.extend({isReady:!1,readyWait:1,ready:function(a){(a===!0?--r.readyWait:r.isReady)||(r.isReady=!0,a!==!0&&--r.readyWait>0||R.resolveWith(d,[r]))}}),r.ready.then=R.then;function S(){d.removeEventListener("DOMContentLoaded",S),
a.removeEventListener("load",S),r.ready()}"complete"===d.readyState||"loading"!==d.readyState&&!d.documentElement.doScroll?a.setTimeout(r.ready):(d.addEventListener("DOMContentLoaded",S),a.addEventListener("load",S));var T=function(a,b,c,d,e,f,g){var h=0,i=a.length,j=null==c;if("object"===r.type(c)){e=!0;for(h in c)T(a,b,h,c[h],!0,f,g)}else if(void 0!==d&&(e=!0,r.isFunction(d)||(g=!0),j&&(g?(b.call(a,d),b=null):(j=b,b=function(a,b,c){return j.call(r(a),c)})),b))for(;h<i;h++)b(a[h],c,g?d:d.call(a[h],h,b(a[h],c)));return e?a:j?b.call(a):i?b(a[0],c):f},U=function(a){return 1===a.nodeType||9===a.nodeType||!+a.nodeType};function V(){this.expando=r.expando+V.uid++}V.uid=1,V.prototype={cache:function(a){var b=a[this.expando];return b||(b={},U(a)&&(a.nodeType?a[this.expando]=b:Object.defineProperty(a,this.expando,{value:b,configurable:!0}))),b},set:function(a,b,c){var d,e=this.cache(a);if("string"==typeof b)e[r.camelCase(b)]=c;else for(d in b)e[r.camelCase(d)]=b[d];return e},get:function(a,b){return void 0===b?this.cache(a):a[this.expando]&&a[this.expando][r.camelCase(b)]},access:function(a,b,c){return void 0===b||b&&"string"==typeof b&&void 0===c?this.get(a,b):(this.set(a,b,c),void 0!==c?c:b)},remove:function(a,b){var c,d=a[this.expando];if(void 0!==d){if(void 0!==b){Array.isArray(b)?b=b.map(r.camelCase):(b=r.camelCase(b),b=b in d?[b]:b.match(L)||[]),c=b.length;while(c--)delete d[b[c]]}(void 0===b||r.isEmptyObject(d))&&(a.nodeType?a[this.expando]=void 0:delete a[this.expando])}},hasData:function(a){var b=a[this.expando];return void 0!==b&&!r.isEmptyObject(b)}};var W=new V,X=new V,Y=/^(?:\{[\w\W]*\}|\[[\w\W]*\])$/,Z=/[A-Z]/g;function $(a){return"true"===a||"false"!==a&&("null"===a?null:a===+a+""?+a:Y.test(a)?JSON.parse(a):a)}function _(a,b,c){var d;if(void 0===c&&1===a.nodeType)if(d="data-"+b.replace(Z,"-$&").toLowerCase(),c=a.getAttribute(d),"string"==typeof c){try{c=$(c)}catch(e){}X.set(a,b,c)}else c=void 0;return c}r.extend({hasData:function(a){return X.hasData(a)||W.hasData(a)},data:function(a,b,c){return X.access(a,b,c)},removeData:function(a,b){X.remove(a,b)},_data:function(a,b,c){return W.access(a,b,c)},_removeData:function(a,b){W.remove(a,b)}}),r.fn.extend({data:function(a,b){var c,d,e,f=this[0],g=f&&f.attributes;if(void 0===a){if(this.length&&(e=X.get(f),1===f.nodeType&&!W.get(f,"hasDataAttrs"))){c=g.length;while(c--)g[c]&&(d=g[c].name,0===d.indexOf("data-")&&(d=r.camelCase(d.slice(5)),_(f,d,e[d])));W.set(f,"hasDataAttrs",!0)}return e}return"object"==typeof a?this.each(function(){X.set(this,a)}):T(this,function(b){var c;if(f&&void 0===b){if(c=X.get(f,a),void 0!==c)return c;if(c=_(f,a),void 0!==c)return c}else this.each(function(){X.set(this,a,b)})},null,b,arguments.length>1,null,!0)},removeData:function(a){return this.each(function(){X.remove(this,a)})}}),r.extend({queue:function(a,b,c){var d;if(a)return b=(b||"fx")+"queue",d=W.get(a,b),c&&(!d||Array.isArray(c)?d=W.access(a,b,r.makeArray(c)):d.push(c)),d||[]},dequeue:function(a,b){b=b||"fx";var c=r.queue(a,b),d=c.length,e=c.shift(),f=r._queueHooks(a,b),g=function(){r.dequeue(a,b)};"inprogress"===e&&(e=c.shift(),d--),e&&("fx"===b&&c.unshift("inprogress"),delete f.stop,e.call(a,g,f)),!d&&f&&f.empty.fire()},_queueHooks:function(a,b){var c=b+"queueHooks";return W.get(a,c)||W.access(a,c,{empty:r.Callbacks("once memory").add(function(){W.remove(a,[b+"queue",c])})})}}),r.fn.extend({queue:function(a,b){var c=2;return"string"!=typeof a&&(b=a,a="fx",c--),arguments.length<c?r.queue(this[0],a):void 0===b?this:this.each(function(){var c=r.queue(this,a,b);r._queueHooks(this,a),"fx"===a&&"inprogress"!==c[0]&&r.dequeue(this,a)})},dequeue:function(a){return this.each(function(){r.dequeue(this,a)})},clearQueue:function(a){return this.queue(a||"fx",[])},promise:function(a,b){var c,d=1,e=r.Deferred(),f=this,g=this.length,h=function(){--d||e.resolveWith(f,[f])};"string"!=typeof a&&(b=a,a=void 0),a=a||"fx";while(g--)c=W.get(f[g],a+"queueHooks"),c&&c.empty&&(d++,c.empty.add(h));return h(),e.promise(b)}});var aa=/[+-]?(?:\d*\.|)\d+(?:[eE][+-]?\d+|)/.source,ba=new RegExp("^(?:([+-])=|)("+aa+")([a-z%]*)$","i"),ca=["Top","Right","Bottom","Left"],da=function(a,b){return a=b||a,"none"===a.style.display||""===a.style.display&&r.contains(a.ownerDocument,a)&&"none"===r.css(a,"display")},ea=function(a,b,c,d){var e,f,g={};for(f in b)g[f]=a.style[f],a.style[f]=b[f];e=c.apply(a,d||[]);for(f in b)a.style[f]=g[f];return e};function fa(a,b,c,d){var e,f=1,g=20,h=d?function(){return d.cur()}:function(){return r.css(a,b,"")},i=h(),j=c&&c[3]||(r.cssNumber[b]?"":"px"),k=(r.cssNumber[b]||"px"!==j&&+i)&&ba.exec(r.css(a,b));if(k&&k[3]!==j){j=j||k[3],c=c||[],k=+i||1;do f=f||".5",k/=f,r.style(a,b,k+j);while(f!==(f=h()/i)&&1!==f&&--g)}return c&&(k=+k||+i||0,e=c[1]?k+(c[1]+1)*c[2]:+c[2],d&&(d.unit=j,d.start=k,d.end=e)),e}var ga={};function ha(a){var b,c=a.ownerDocument,d=a.nodeName,e=ga[d];return e?e:(b=c.body.appendChild(c.createElement(d)),e=r.css(b,"display"),b.parentNode.removeChild(b),"none"===e&&(e="block"),ga[d]=e,e)}function ia(a,b){for(var c,d,e=[],f=0,g=a.length;f<g;f++)d=a[f],d.style&&(c=d.style.display,b?("none"===c&&(e[f]=W.get(d,"display")||null,e[f]||(d.style.display="")),""===d.style.display&&da(d)&&(e[f]=ha(d))):"none"!==c&&(e[f]="none",W.set(d,"display",c)));for(f=0;f<g;f++)null!=e[f]&&(a[f].style.display=e[f]);return a}r.fn.extend({show:function(){return ia(this,!0)},hide:function(){return ia(this)},toggle:function(a){return"boolean"==typeof a?a?this.show():this.hide():this.each(function(){da(this)?r(this).show():r(this).hide()})}});var ja=/^(?:checkbox|radio)$/i,ka=/<([a-z][^\/\0>\x20\t\r\n\f]+)/i,la=/^$|\/(?:java|ecma)script/i,ma={option:[1,"<select multiple='multiple'>","</select>"],thead:[1,"<table>","</table>"],col:[2,"<table><colgroup>","</colgroup></table>"],tr:[2,"<table><tbody>","</tbody></table>"],td:[3,"<table><tbody><tr>","</tr></tbody></table>"],_default:[0,"",""]};ma.optgroup=ma.option,ma.tbody=ma.tfoot=ma.colgroup=ma.caption=ma.thead,ma.th=ma.td;function na(a,b){var c;return c="undefined"!=typeof a.getElementsByTagName?a.getElementsByTagName(b||"*"):"undefined"!=typeof a.querySelectorAll?a.querySelectorAll(b||"*"):[],void 0===b||b&&B(a,b)?r.merge([a],c):c}function oa(a,b){for(var c=0,d=a.length;c<d;c++)W.set(a[c],"globalEval",!b||W.get(b[c],"globalEval"))}var pa=/<|&#?\w+;/;function qa(a,b,c,d,e){for(var f,g,h,i,j,k,l=b.createDocumentFragment(),m=[],n=0,o=a.length;n<o;n++)if(f=a[n],f||0===f)if("object"===r.type(f))r.merge(m,f.nodeType?[f]:f);else if(pa.test(f)){g=g||l.appendChild(b.createElement("div")),h=(ka.exec(f)||["",""])[1].toLowerCase(),i=ma[h]||ma._default,g.innerHTML=i[1]+r.htmlPrefilter(f)+i[2],k=i[0];while(k--)g=g.lastChild;r.merge(m,g.childNodes),g=l.firstChild,g.textContent=""}else m.push(b.createTextNode(f));l.textContent="",n=0;while(f=m[n++])if(d&&r.inArray(f,d)>-1)e&&e.push(f);else if(j=r.contains(f.ownerDocument,f),g=na(l.appendChild(f),"script"),j&&oa(g),c){k=0;while(f=g[k++])la.test(f.type||"")&&c.push(f)}return l}!function(){var a=d.createDocumentFragment(),b=a.appendChild(d.createElement("div")),c=d.createElement("input");c.setAttribute("type","radio"),c.setAttribute("checked","checked"),c.setAttribute("name","t"),b.appendChild(c),o.checkClone=b.cloneNode(!0).cloneNode(!0).lastChild.checked,b.innerHTML="<textarea>x</textarea>",o.noCloneChecked=!!b.cloneNode(!0).lastChild.defaultValue}();var ra=d.documentElement,sa=/^key/,ta=/^(?:mouse|pointer|contextmenu|drag|drop)|click/,ua=/^([^.]*)(?:\.(.+)|)/;function va(){return!0}function wa(){return!1}function xa(){try{return d.activeElement}catch(a){}}function ya(a,b,c,d,e,f){var g,h;if("object"==typeof b){"string"!=typeof c&&(d=d||c,c=void 0);for(h in b)ya(a,h,c,d,b[h],f);return a}if(null==d&&null==e?(e=c,d=c=void 0):null==e&&("string"==typeof c?(e=d,d=void 0):(e=d,d=c,c=void 0)),e===!1)e=wa;else if(!e)return a;return 1===f&&(g=e,e=function(a){return r().off(a),g.apply(this,arguments)},e.guid=g.guid||(g.guid=r.guid++)),a.each(function(){r.event.add(this,b,e,d,c)})}r.event={global:{},add:function(a,b,c,d,e){var f,g,h,i,j,k,l,m,n,o,p,q=W.get(a);if(q){c.handler&&(f=c,c=f.handler,e=f.selector),e&&r.find.matchesSelector(ra,e),c.guid||(c.guid=r.guid++),(i=q.events)||(i=q.events={}),(g=q.handle)||(g=q.handle=function(b){return"undefined"!=typeof r&&r.event.triggered!==b.type?r.event.dispatch.apply(a,arguments):void 0}),b=(b||"").match(L)||[""],j=b.length;while(j--)h=ua.exec(b[j])||[],n=p=h[1],o=(h[2]||"").split(".").sort(),n&&(l=r.event.special[n]||{},n=(e?l.delegateType:l.bindType)||n,l=r.event.special[n]||{},k=r.extend({type:n,origType:p,data:d,handler:c,guid:c.guid,selector:e,needsContext:e&&r.expr.match.needsContext.test(e),namespace:o.join(".")},f),(m=i[n])||(m=i[n]=[],m.delegateCount=0,l.setup&&l.setup.call(a,d,o,g)!==!1||a.addEventListener&&a.addEventListener(n,g)),l.add&&(l.add.call(a,k),k.handler.guid||(k.handler.guid=c.guid)),e?m.splice(m.delegateCount++,0,k):m.push(k),r.event.global[n]=!0)}},remove:function(a,b,c,d,e){var f,g,h,i,j,k,l,m,n,o,p,q=W.hasData(a)&&W.get(a);if(q&&(i=q.events)){b=(b||"").match(L)||[""],j=b.length;while(j--)if(h=ua.exec(b[j])||[],n=p=h[1],o=(h[2]||"").split(".").sort(),n){l=r.event.special[n]||{},n=(d?l.delegateType:l.bindType)||n,m=i[n]||[],h=h[2]&&new RegExp("(^|\\.)"+o.join("\\.(?:.*\\.|)")+"(\\.|$)"),g=f=m.length;while(f--)k=m[f],!e&&p!==k.origType||c&&c.guid!==k.guid||h&&!h.test(k.namespace)||d&&d!==k.selector&&("**"!==d||!k.selector)||(m.splice(f,1),k.selector&&m.delegateCount--,l.remove&&l.remove.call(a,k));g&&!m.length&&(l.teardown&&l.teardown.call(a,o,q.handle)!==!1||r.removeEvent(a,n,q.handle),delete i[n])}else for(n in i)r.event.remove(a,n+b[j],c,d,!0);r.isEmptyObject(i)&&W.remove(a,"handle events")}},dispatch:function(a){var b=r.event.fix(a),c,d,e,f,g,h,i=new Array(arguments.length),j=(W.get(this,"events")||{})[b.type]||[],k=r.event.special[b.type]||{};for(i[0]=b,c=1;c<arguments.length;c++)i[c]=arguments[c];if(b.delegateTarget=this,!k.preDispatch||k.preDispatch.call(this,b)!==!1){h=r.event.handlers.call(this,b,j),c=0;while((f=h[c++])&&!b.isPropagationStopped()){b.currentTarget=f.elem,d=0;while((g=f.handlers[d++])&&!b.isImmediatePropagationStopped())b.rnamespace&&!b.rnamespace.test(g.namespace)||(b.handleObj=g,b.data=g.data,e=((r.event.special[g.origType]||{}).handle||g.handler).apply(f.elem,i),void 0!==e&&(b.result=e)===!1&&(b.preventDefault(),b.stopPropagation()))}return k.postDispatch&&k.postDispatch.call(this,b),b.result}},handlers:function(a,b){var c,d,e,f,g,h=[],i=b.delegateCount,j=a.target;if(i&&j.nodeType&&!("click"===a.type&&a.button>=1))for(;j!==this;j=j.parentNode||this)if(1===j.nodeType&&("click"!==a.type||j.disabled!==!0)){for(f=[],g={},c=0;c<i;c++)d=b[c],e=d.selector+" ",void 0===g[e]&&(g[e]=d.needsContext?r(e,this).index(j)>-1:r.find(e,this,null,[j]).length),g[e]&&f.push(d);f.length&&h.push({elem:j,handlers:f})}return j=this,i<b.length&&h.push({elem:j,handlers:b.slice(i)}),h},addProp:function(a,b){Object.defineProperty(r.Event.prototype,a,{enumerable:!0,configurable:!0,get:r.isFunction(b)?function(){if(this.originalEvent)return b(this.originalEvent)}:function(){if(this.originalEvent)return this.originalEvent[a]},set:function(b){Object.defineProperty(this,a,{enumerable:!0,configurable:!0,writable:!0,value:b})}})},fix:function(a){return a[r.expando]?a:new r.Event(a)},special:{load:{noBubble:!0},focus:{trigger:function(){if(this!==xa()&&this.focus)return this.focus(),!1},delegateType:"focusin"},blur:{trigger:function(){if(this===xa()&&this.blur)return this.blur(),!1},delegateType:"focusout"},click:{trigger:function(){if("checkbox"===this.type&&this.click&&B(this,"input"))return this.click(),!1},_default:function(a){return B(a.target,"a")}},beforeunload:{postDispatch:function(a){void 0!==a.result&&a.originalEvent&&(a.originalEvent.returnValue=a.result)}}}},r.removeEvent=function(a,b,c){a.removeEventListener&&a.removeEventListener(b,c)},r.Event=function(a,b){return this instanceof r.Event?(a&&a.type?(this.originalEvent=a,this.type=a.type,this.isDefaultPrevented=a.defaultPrevented||void 0===a.defaultPrevented&&a.returnValue===!1?va:wa,this.target=a.target&&3===a.target.nodeType?a.target.parentNode:a.target,this.currentTarget=a.currentTarget,this.relatedTarget=a.relatedTarget):this.type=a,b&&r.extend(this,b),this.timeStamp=a&&a.timeStamp||r.now(),void(this[r.expando]=!0)):new r.Event(a,b)},r.Event.prototype={constructor:r.Event,isDefaultPrevented:wa,isPropagationStopped:wa,isImmediatePropagationStopped:wa,isSimulated:!1,preventDefault:function(){var a=this.originalEvent;this.isDefaultPrevented=va,a&&!this.isSimulated&&a.preventDefault()},stopPropagation:function(){var a=this.originalEvent;this.isPropagationStopped=va,a&&!this.isSimulated&&a.stopPropagation()},stopImmediatePropagation:function(){var a=this.originalEvent;this.isImmediatePropagationStopped=va,a&&!this.isSimulated&&a.stopImmediatePropagation(),this.stopPropagation()}},r.each({altKey:!0,bubbles:!0,cancelable:!0,changedTouches:!0,ctrlKey:!0,detail:!0,eventPhase:!0,metaKey:!0,pageX:!0,pageY:!0,shiftKey:!0,view:!0,"char":!0,charCode:!0,key:!0,keyCode:!0,button:!0,buttons:!0,clientX:!0,clientY:!0,offsetX:!0,offsetY:!0,pointerId:!0,pointerType:!0,screenX:!0,screenY:!0,targetTouches:!0,toElement:!0,touches:!0,which:function(a){var b=a.button;return null==a.which&&sa.test(a.type)?null!=a.charCode?a.charCode:a.keyCode:!a.which&&void 0!==b&&ta.test(a.type)?1&b?1:2&b?3:4&b?2:0:a.which}},r.event.addProp),r.each({mouseenter:"mouseover",mouseleave:"mouseout",pointerenter:"pointerover",pointerleave:"pointerout"},function(a,b){r.event.special[a]={delegateType:b,bindType:b,handle:function(a){var c,d=this,e=a.relatedTarget,f=a.handleObj;return e&&(e===d||r.contains(d,e))||(a.type=f.origType,c=f.handler.apply(this,arguments),a.type=b),c}}}),r.fn.extend({on:function(a,b,c,d){return ya(this,a,b,c,d)},one:function(a,b,c,d){return ya(this,a,b,c,d,1)},off:function(a,b,c){var d,e;if(a&&a.preventDefault&&a.handleObj)return d=a.handleObj,r(a.delegateTarget).off(d.namespace?d.origType+"."+d.namespace:d.origType,d.selector,d.handler),this;if("object"==typeof a){for(e in a)this.off(e,b,a[e]);return this}return b!==!1&&"function"!=typeof b||(c=b,b=void 0),c===!1&&(c=wa),this.each(function(){r.event.remove(this,a,c,b)})}});var za=/<(?!area|br|col|embed|hr|img|input|link|meta|param)(([a-z][^\/\0>\x20\t\r\n\f]*)[^>]*)\/>/gi,Aa=/<script|<style|<link/i,Ba=/checked\s*(?:[^=]|=\s*.checked.)/i,Ca=/^true\/(.*)/,Da=/^\s*<!(?:\[CDATA\[|--)|(?:\]\]|--)>\s*$/g;function Ea(a,b){return B(a,"table")&&B(11!==b.nodeType?b:b.firstChild,"tr")?r(">tbody",a)[0]||a:a}function Fa(a){return a.type=(null!==a.getAttribute("type"))+"/"+a.type,a}function Ga(a){var b=Ca.exec(a.type);return b?a.type=b[1]:a.removeAttribute("type"),a}function Ha(a,b){var c,d,e,f,g,h,i,j;if(1===b.nodeType){if(W.hasData(a)&&(f=W.access(a),g=W.set(b,f),j=f.events)){delete g.handle,g.events={};for(e in j)for(c=0,d=j[e].length;c<d;c++)r.event.add(b,e,j[e][c])}X.hasData(a)&&(h=X.access(a),i=r.extend({},h),X.set(b,i))}}function Ia(a,b){var c=b.nodeName.toLowerCase();"input"===c&&ja.test(a.type)?b.checked=a.checked:"input"!==c&&"textarea"!==c||(b.defaultValue=a.defaultValue)}function Ja(a,b,c,d){b=g.apply([],b);var e,f,h,i,j,k,l=0,m=a.length,n=m-1,q=b[0],s=r.isFunction(q);if(s||m>1&&"string"==typeof q&&!o.checkClone&&Ba.test(q))return a.each(function(e){var f=a.eq(e);s&&(b[0]=q.call(this,e,f.html())),Ja(f,b,c,d)});if(m&&(e=qa(b,a[0].ownerDocument,!1,a,d),f=e.firstChild,1===e.childNodes.length&&(e=f),f||d)){for(h=r.map(na(e,"script"),Fa),i=h.length;l<m;l++)j=e,l!==n&&(j=r.clone(j,!0,!0),i&&r.merge(h,na(j,"script"))),c.call(a[l],j,l);if(i)for(k=h[h.length-1].ownerDocument,r.map(h,Ga),l=0;l<i;l++)j=h[l],la.test(j.type||"")&&!W.access(j,"globalEval")&&r.contains(k,j)&&(j.src?r._evalUrl&&r._evalUrl(j.src):p(j.textContent.replace(Da,""),k))}return a}function Ka(a,b,c){for(var d,e=b?r.filter(b,a):a,f=0;null!=(d=e[f]);f++)c||1!==d.nodeType||r.cleanData(na(d)),d.parentNode&&(c&&r.contains(d.ownerDocument,d)&&oa(na(d,"script")),d.parentNode.removeChild(d));return a}r.extend({htmlPrefilter:function(a){return a.replace(za,"<$1></$2>")},clone:function(a,b,c){var d,e,f,g,h=a.cloneNode(!0),i=r.contains(a.ownerDocument,a);if(!(o.noCloneChecked||1!==a.nodeType&&11!==a.nodeType||r.isXMLDoc(a)))for(g=na(h),f=na(a),d=0,e=f.length;d<e;d++)Ia(f[d],g[d]);if(b)if(c)for(f=f||na(a),g=g||na(h),d=0,e=f.length;d<e;d++)Ha(f[d],g[d]);else Ha(a,h);return g=na(h,"script"),g.length>0&&oa(g,!i&&na(a,"script")),h},cleanData:function(a){for(var b,c,d,e=r.event.special,f=0;void 0!==(c=a[f]);f++)if(U(c)){if(b=c[W.expando]){if(b.events)for(d in b.events)e[d]?r.event.remove(c,d):r.removeEvent(c,d,b.handle);c[W.expando]=void 0}c[X.expando]&&(c[X.expando]=void 0)}}}),r.fn.extend({detach:function(a){return Ka(this,a,!0)},remove:function(a){return Ka(this,a)},text:function(a){return T(this,function(a){return void 0===a?r.text(this):this.empty().each(function(){1!==this.nodeType&&11!==this.nodeType&&9!==this.nodeType||(this.textContent=a)})},null,a,arguments.length)},append:function(){return Ja(this,arguments,function(a){if(1===this.nodeType||11===this.nodeType||9===this.nodeType){var b=Ea(this,a);b.appendChild(a)}})},prepend:function(){return Ja(this,arguments,function(a){if(1===this.nodeType||11===this.nodeType||9===this.nodeType){var b=Ea(this,a);b.insertBefore(a,b.firstChild)}})},before:function(){return Ja(this,arguments,function(a){this.parentNode&&this.parentNode.insertBefore(a,this)})},after:function(){return Ja(this,arguments,function(a){this.parentNode&&this.parentNode.insertBefore(a,this.nextSibling)})},empty:function(){for(var a,b=0;null!=(a=this[b]);b++)1===a.nodeType&&(r.cleanData(na(a,!1)),a.textContent="");return this},clone:function(a,b){return a=null!=a&&a,b=null==b?a:b,this.map(function(){return r.clone(this,a,b)})},html:function(a){return T(this,function(a){var b=this[0]||{},c=0,d=this.length;if(void 0===a&&1===b.nodeType)return b.innerHTML;if("string"==typeof a&&!Aa.test(a)&&!ma[(ka.exec(a)||["",""])[1].toLowerCase()]){a=r.htmlPrefilter(a);try{for(;c<d;c++)b=this[c]||{},1===b.nodeType&&(r.cleanData(na(b,!1)),b.innerHTML=a);b=0}catch(e){}}b&&this.empty().append(a)},null,a,arguments.length)},replaceWith:function(){var a=[];return Ja(this,arguments,function(b){var c=this.parentNode;r.inArray(this,a)<0&&(r.cleanData(na(this)),c&&c.replaceChild(b,this))},a)}}),r.each({appendTo:"append",prependTo:"prepend",insertBefore:"before",insertAfter:"after",replaceAll:"replaceWith"},function(a,b){r.fn[a]=function(a){for(var c,d=[],e=r(a),f=e.length-1,g=0;g<=f;g++)c=g===f?this:this.clone(!0),r(e[g])[b](c),h.apply(d,c.get());return this.pushStack(d)}});var La=/^margin/,Ma=new RegExp("^("+aa+")(?!px)[a-z%]+$","i"),Na=function(b){var c=b.ownerDocument.defaultView;return c&&c.opener||(c=a),c.getComputedStyle(b)};!function(){function b(){if(i){i.style.cssText="box-sizing:border-box;position:relative;display:block;margin:auto;border:1px;padding:1px;top:1%;width:50%",i.innerHTML="",ra.appendChild(h);var b=a.getComputedStyle(i);c="1%"!==b.top,g="2px"===b.marginLeft,e="4px"===b.width,i.style.marginRight="50%",f="4px"===b.marginRight,ra.removeChild(h),i=null}}var c,e,f,g,h=d.createElement("div"),i=d.createElement("div");i.style&&(i.style.backgroundClip="content-box",i.cloneNode(!0).style.backgroundClip="",o.clearCloneStyle="content-box"===i.style.backgroundClip,h.style.cssText="border:0;width:8px;height:0;top:0;left:-9999px;padding:0;margin-top:1px;position:absolute",h.appendChild(i),r.extend(o,{pixelPosition:function(){return b(),c},boxSizingReliable:function(){return b(),e},pixelMarginRight:function(){return b(),f},reliableMarginLeft:function(){return b(),g}}))}();function Oa(a,b,c){var d,e,f,g,h=a.style;return c=c||Na(a),c&&(g=c.getPropertyValue(b)||c[b],""!==g||r.contains(a.ownerDocument,a)||(g=r.style(a,b)),!o.pixelMarginRight()&&Ma.test(g)&&La.test(b)&&(d=h.width,e=h.minWidth,f=h.maxWidth,h.minWidth=h.maxWidth=h.width=g,g=c.width,h.width=d,h.minWidth=e,h.maxWidth=f)),void 0!==g?g+"":g}function Pa(a,b){return{get:function(){return a()?void delete this.get:(this.get=b).apply(this,arguments)}}}var Qa=/^(none|table(?!-c[ea]).+)/,Ra=/^--/,Sa={position:"absolute",visibility:"hidden",display:"block"},Ta={letterSpacing:"0",fontWeight:"400"},Ua=["Webkit","Moz","ms"],Va=d.createElement("div").style;function Wa(a){if(a in Va)return a;var b=a[0].toUpperCase()+a.slice(1),c=Ua.length;while(c--)if(a=Ua[c]+b,a in Va)return a}function Xa(a){var b=r.cssProps[a];return b||(b=r.cssProps[a]=Wa(a)||a),b}function Ya(a,b,c){var d=ba.exec(b);return d?Math.max(0,d[2]-(c||0))+(d[3]||"px"):b}function Za(a,b,c,d,e){var f,g=0;for(f=c===(d?"border":"content")?4:"width"===b?1:0;f<4;f+=2)"margin"===c&&(g+=r.css(a,c+ca[f],!0,e)),d?("content"===c&&(g-=r.css(a,"padding"+ca[f],!0,e)),"margin"!==c&&(g-=r.css(a,"border"+ca[f]+"Width",!0,e))):(g+=r.css(a,"padding"+ca[f],!0,e),"padding"!==c&&(g+=r.css(a,"border"+ca[f]+"Width",!0,e)));return g}function $a(a,b,c){var d,e=Na(a),f=Oa(a,b,e),g="border-box"===r.css(a,"boxSizing",!1,e);return Ma.test(f)?f:(d=g&&(o.boxSizingReliable()||f===a.style[b]),"auto"===f&&(f=a["offset"+b[0].toUpperCase()+b.slice(1)]),f=parseFloat(f)||0,f+Za(a,b,c||(g?"border":"content"),d,e)+"px")}r.extend({cssHooks:{opacity:{get:function(a,b){if(b){var c=Oa(a,"opacity");return""===c?"1":c}}}},cssNumber:{animationIterationCount:!0,columnCount:!0,fillOpacity:!0,flexGrow:!0,flexShrink:!0,fontWeight:!0,lineHeight:!0,opacity:!0,order:!0,orphans:!0,widows:!0,zIndex:!0,zoom:!0},cssProps:{"float":"cssFloat"},style:function(a,b,c,d){if(a&&3!==a.nodeType&&8!==a.nodeType&&a.style){var e,f,g,h=r.camelCase(b),i=Ra.test(b),j=a.style;return i||(b=Xa(h)),g=r.cssHooks[b]||r.cssHooks[h],void 0===c?g&&"get"in g&&void 0!==(e=g.get(a,!1,d))?e:j[b]:(f=typeof c,"string"===f&&(e=ba.exec(c))&&e[1]&&(c=fa(a,b,e),f="number"),null!=c&&c===c&&("number"===f&&(c+=e&&e[3]||(r.cssNumber[h]?"":"px")),o.clearCloneStyle||""!==c||0!==b.indexOf("background")||(j[b]="inherit"),g&&"set"in g&&void 0===(c=g.set(a,c,d))||(i?j.setProperty(b,c):j[b]=c)),void 0)}},css:function(a,b,c,d){var e,f,g,h=r.camelCase(b),i=Ra.test(b);return i||(b=Xa(h)),g=r.cssHooks[b]||r.cssHooks[h],g&&"get"in g&&(e=g.get(a,!0,c)),void 0===e&&(e=Oa(a,b,d)),"normal"===e&&b in Ta&&(e=Ta[b]),""===c||c?(f=parseFloat(e),c===!0||isFinite(f)?f||0:e):e}}),r.each(["height","width"],function(a,b){r.cssHooks[b]={get:function(a,c,d){if(c)return!Qa.test(r.css(a,"display"))||a.getClientRects().length&&a.getBoundingClientRect().width?$a(a,b,d):ea(a,Sa,function(){return $a(a,b,d)})},set:function(a,c,d){var e,f=d&&Na(a),g=d&&Za(a,b,d,"border-box"===r.css(a,"boxSizing",!1,f),f);return g&&(e=ba.exec(c))&&"px"!==(e[3]||"px")&&(a.style[b]=c,c=r.css(a,b)),Ya(a,c,g)}}}),r.cssHooks.marginLeft=Pa(o.reliableMarginLeft,function(a,b){if(b)return(parseFloat(Oa(a,"marginLeft"))||a.getBoundingClientRect().left-ea(a,{marginLeft:0},function(){return a.getBoundingClientRect().left}))+"px"}),r.each({margin:"",padding:"",border:"Width"},function(a,b){r.cssHooks[a+b]={expand:function(c){for(var d=0,e={},f="string"==typeof c?c.split(" "):[c];d<4;d++)e[a+ca[d]+b]=f[d]||f[d-2]||f[0];return e}},La.test(a)||(r.cssHooks[a+b].set=Ya)}),r.fn.extend({css:function(a,b){return T(this,function(a,b,c){var d,e,f={},g=0;if(Array.isArray(b)){for(d=Na(a),e=b.length;g<e;g++)f[b[g]]=r.css(a,b[g],!1,d);return f}return void 0!==c?r.style(a,b,c):r.css(a,b)},a,b,arguments.length>1)}});function _a(a,b,c,d,e){return new _a.prototype.init(a,b,c,d,e)}r.Tween=_a,_a.prototype={constructor:_a,init:function(a,b,c,d,e,f){this.elem=a,this.prop=c,this.easing=e||r.easing._default,this.options=b,this.start=this.now=this.cur(),this.end=d,this.unit=f||(r.cssNumber[c]?"":"px")},cur:function(){var a=_a.propHooks[this.prop];return a&&a.get?a.get(this):_a.propHooks._default.get(this)},run:function(a){var b,c=_a.propHooks[this.prop];return this.options.duration?this.pos=b=r.easing[this.easing](a,this.options.duration*a,0,1,this.options.duration):this.pos=b=a,this.now=(this.end-this.start)*b+this.start,this.options.step&&this.options.step.call(this.elem,this.now,this),c&&c.set?c.set(this):_a.propHooks._default.set(this),this}},_a.prototype.init.prototype=_a.prototype,_a.propHooks={_default:{get:function(a){var b;return 1!==a.elem.nodeType||null!=a.elem[a.prop]&&null==a.elem.style[a.prop]?a.elem[a.prop]:(b=r.css(a.elem,a.prop,""),b&&"auto"!==b?b:0)},set:function(a){r.fx.step[a.prop]?r.fx.step[a.prop](a):1!==a.elem.nodeType||null==a.elem.style[r.cssProps[a.prop]]&&!r.cssHooks[a.prop]?a.elem[a.prop]=a.now:r.style(a.elem,a.prop,a.now+a.unit)}}},_a.propHooks.scrollTop=_a.propHooks.scrollLeft={set:function(a){a.elem.nodeType&&a.elem.parentNode&&(a.elem[a.prop]=a.now)}},r.easing={linear:function(a){return a},swing:function(a){return.5-Math.cos(a*Math.PI)/2},_default:"swing"},r.fx=_a.prototype.init,r.fx.step={};var ab,bb,cb=/^(?:toggle|show|hide)$/,db=/queueHooks$/;function eb(){bb&&(d.hidden===!1&&a.requestAnimationFrame?a.requestAnimationFrame(eb):a.setTimeout(eb,r.fx.interval),r.fx.tick())}function fb(){return a.setTimeout(function(){ab=void 0}),ab=r.now()}function gb(a,b){var c,d=0,e={height:a};for(b=b?1:0;d<4;d+=2-b)c=ca[d],e["margin"+c]=e["padding"+c]=a;return b&&(e.opacity=e.width=a),e}function hb(a,b,c){for(var d,e=(kb.tweeners[b]||[]).concat(kb.tweeners["*"]),f=0,g=e.length;f<g;f++)if(d=e[f].call(c,b,a))return d}function ib(a,b,c){var d,e,f,g,h,i,j,k,l="width"in b||"height"in b,m=this,n={},o=a.style,p=a.nodeType&&da(a),q=W.get(a,"fxshow");c.queue||(g=r._queueHooks(a,"fx"),null==g.unqueued&&(g.unqueued=0,h=g.empty.fire,g.empty.fire=function(){g.unqueued||h()}),g.unqueued++,m.always(function(){m.always(function(){g.unqueued--,r.queue(a,"fx").length||g.empty.fire()})}));for(d in b)if(e=b[d],cb.test(e)){if(delete b[d],f=f||"toggle"===e,e===(p?"hide":"show")){if("show"!==e||!q||void 0===q[d])continue;p=!0}n[d]=q&&q[d]||r.style(a,d)}if(i=!r.isEmptyObject(b),i||!r.isEmptyObject(n)){l&&1===a.nodeType&&(c.overflow=[o.overflow,o.overflowX,o.overflowY],j=q&&q.display,null==j&&(j=W.get(a,"display")),k=r.css(a,"display"),"none"===k&&(j?k=j:(ia([a],!0),j=a.style.display||j,k=r.css(a,"display"),ia([a]))),("inline"===k||"inline-block"===k&&null!=j)&&"none"===r.css(a,"float")&&(i||(m.done(function(){o.display=j}),null==j&&(k=o.display,j="none"===k?"":k)),o.display="inline-block")),c.overflow&&(o.overflow="hidden",m.always(function(){o.overflow=c.overflow[0],o.overflowX=c.overflow[1],o.overflowY=c.overflow[2]})),i=!1;for(d in n)i||(q?"hidden"in q&&(p=q.hidden):q=W.access(a,"fxshow",{display:j}),f&&(q.hidden=!p),p&&ia([a],!0),m.done(function(){p||ia([a]),W.remove(a,"fxshow");for(d in n)r.style(a,d,n[d])})),i=hb(p?q[d]:0,d,m),d in q||(q[d]=i.start,p&&(i.end=i.start,i.start=0))}}function jb(a,b){var c,d,e,f,g;for(c in a)if(d=r.camelCase(c),e=b[d],f=a[c],Array.isArray(f)&&(e=f[1],f=a[c]=f[0]),c!==d&&(a[d]=f,delete a[c]),g=r.cssHooks[d],g&&"expand"in g){f=g.expand(f),delete a[d];for(c in f)c in a||(a[c]=f[c],b[c]=e)}else b[d]=e}function kb(a,b,c){var d,e,f=0,g=kb.prefilters.length,h=r.Deferred().always(function(){delete i.elem}),i=function(){if(e)return!1;for(var b=ab||fb(),c=Math.max(0,j.startTime+j.duration-b),d=c/j.duration||0,f=1-d,g=0,i=j.tweens.length;g<i;g++)j.tweens[g].run(f);return h.notifyWith(a,[j,f,c]),f<1&&i?c:(i||h.notifyWith(a,[j,1,0]),h.resolveWith(a,[j]),!1)},j=h.promise({elem:a,props:r.extend({},b),opts:r.extend(!0,{specialEasing:{},easing:r.easing._default},c),originalProperties:b,originalOptions:c,startTime:ab||fb(),duration:c.duration,tweens:[],createTween:function(b,c){var d=r.Tween(a,j.opts,b,c,j.opts.specialEasing[b]||j.opts.easing);return j.tweens.push(d),d},stop:function(b){var c=0,d=b?j.tweens.length:0;if(e)return this;for(e=!0;c<d;c++)j.tweens[c].run(1);return b?(h.notifyWith(a,[j,1,0]),h.resolveWith(a,[j,b])):h.rejectWith(a,[j,b]),this}}),k=j.props;for(jb(k,j.opts.specialEasing);f<g;f++)if(d=kb.prefilters[f].call(j,a,k,j.opts))return r.isFunction(d.stop)&&(r._queueHooks(j.elem,j.opts.queue).stop=r.proxy(d.stop,d)),d;return r.map(k,hb,j),r.isFunction(j.opts.start)&&j.opts.start.call(a,j),j.progress(j.opts.progress).done(j.opts.done,j.opts.complete).fail(j.opts.fail).always(j.opts.always),r.fx.timer(r.extend(i,{elem:a,anim:j,queue:j.opts.queue})),j}r.Animation=r.extend(kb,{tweeners:{"*":[function(a,b){var c=this.createTween(a,b);return fa(c.elem,a,ba.exec(b),c),c}]},tweener:function(a,b){r.isFunction(a)?(b=a,a=["*"]):a=a.match(L);for(var c,d=0,e=a.length;d<e;d++)c=a[d],kb.tweeners[c]=kb.tweeners[c]||[],kb.tweeners[c].unshift(b)},prefilters:[ib],prefilter:function(a,b){b?kb.prefilters.unshift(a):kb.prefilters.push(a)}}),r.speed=function(a,b,c){var d=a&&"object"==typeof a?r.extend({},a):{complete:c||!c&&b||r.isFunction(a)&&a,duration:a,easing:c&&b||b&&!r.isFunction(b)&&b};return r.fx.off?d.duration=0:"number"!=typeof d.duration&&(d.duration in r.fx.speeds?d.duration=r.fx.speeds[d.duration]:d.duration=r.fx.speeds._default),null!=d.queue&&d.queue!==!0||(d.queue="fx"),d.old=d.complete,d.complete=function(){r.isFunction(d.old)&&d.old.call(this),d.queue&&r.dequeue(this,d.queue)},d},r.fn.extend({fadeTo:function(a,b,c,d){return this.filter(da).css("opacity",0).show().end().animate({opacity:b},a,c,d)},animate:function(a,b,c,d){var e=r.isEmptyObject(a),f=r.speed(b,c,d),g=function(){var b=kb(this,r.extend({},a),f);(e||W.get(this,"finish"))&&b.stop(!0)};return g.finish=g,e||f.queue===!1?this.each(g):this.queue(f.queue,g)},stop:function(a,b,c){var d=function(a){var b=a.stop;delete a.stop,b(c)};return"string"!=typeof a&&(c=b,b=a,a=void 0),b&&a!==!1&&this.queue(a||"fx",[]),this.each(function(){var b=!0,e=null!=a&&a+"queueHooks",f=r.timers,g=W.get(this);if(e)g[e]&&g[e].stop&&d(g[e]);else for(e in g)g[e]&&g[e].stop&&db.test(e)&&d(g[e]);for(e=f.length;e--;)f[e].elem!==this||null!=a&&f[e].queue!==a||(f[e].anim.stop(c),b=!1,f.splice(e,1));!b&&c||r.dequeue(this,a)})},finish:function(a){return a!==!1&&(a=a||"fx"),this.each(function(){var b,c=W.get(this),d=c[a+"queue"],e=c[a+"queueHooks"],f=r.timers,g=d?d.length:0;for(c.finish=!0,r.queue(this,a,[]),e&&e.stop&&e.stop.call(this,!0),b=f.length;b--;)f[b].elem===this&&f[b].queue===a&&(f[b].anim.stop(!0),f.splice(b,1));for(b=0;b<g;b++)d[b]&&d[b].finish&&d[b].finish.call(this);delete c.finish})}}),r.each(["toggle","show","hide"],function(a,b){var c=r.fn[b];r.fn[b]=function(a,d,e){return null==a||"boolean"==typeof a?c.apply(this,arguments):this.animate(gb(b,!0),a,d,e)}}),r.each({slideDown:gb("show"),slideUp:gb("hide"),slideToggle:gb("toggle"),fadeIn:{opacity:"show"},fadeOut:{opacity:"hide"},fadeToggle:{opacity:"toggle"}},function(a,b){r.fn[a]=function(a,c,d){return this.animate(b,a,c,d)}}),r.timers=[],r.fx.tick=function(){var a,b=0,c=r.timers;for(ab=r.now();b<c.length;b++)a=c[b],a()||c[b]!==a||c.splice(b--,1);c.length||r.fx.stop(),ab=void 0},r.fx.timer=function(a){r.timers.push(a),r.fx.start()},r.fx.interval=13,r.fx.start=function(){bb||(bb=!0,eb())},r.fx.stop=function(){bb=null},r.fx.speeds={slow:600,fast:200,_default:400},r.fn.delay=function(b,c){return b=r.fx?r.fx.speeds[b]||b:b,c=c||"fx",this.queue(c,function(c,d){var e=a.setTimeout(c,b);d.stop=function(){a.clearTimeout(e)}})},function(){var a=d.createElement("input"),b=d.createElement("select"),c=b.appendChild(d.createElement("option"));a.type="checkbox",o.checkOn=""!==a.value,o.optSelected=c.selected,a=d.createElement("input"),a.value="t",a.type="radio",o.radioValue="t"===a.value}();var lb,mb=r.expr.attrHandle;r.fn.extend({attr:function(a,b){return T(this,r.attr,a,b,arguments.length>1)},removeAttr:function(a){return this.each(function(){r.removeAttr(this,a)})}}),r.extend({attr:function(a,b,c){var d,e,f=a.nodeType;if(3!==f&&8!==f&&2!==f)return"undefined"==typeof a.getAttribute?r.prop(a,b,c):(1===f&&r.isXMLDoc(a)||(e=r.attrHooks[b.toLowerCase()]||(r.expr.match.bool.test(b)?lb:void 0)),void 0!==c?null===c?void r.removeAttr(a,b):e&&"set"in e&&void 0!==(d=e.set(a,c,b))?d:(a.setAttribute(b,c+""),c):e&&"get"in e&&null!==(d=e.get(a,b))?d:(d=r.find.attr(a,b),
null==d?void 0:d))},attrHooks:{type:{set:function(a,b){if(!o.radioValue&&"radio"===b&&B(a,"input")){var c=a.value;return a.setAttribute("type",b),c&&(a.value=c),b}}}},removeAttr:function(a,b){var c,d=0,e=b&&b.match(L);if(e&&1===a.nodeType)while(c=e[d++])a.removeAttribute(c)}}),lb={set:function(a,b,c){return b===!1?r.removeAttr(a,c):a.setAttribute(c,c),c}},r.each(r.expr.match.bool.source.match(/\w+/g),function(a,b){var c=mb[b]||r.find.attr;mb[b]=function(a,b,d){var e,f,g=b.toLowerCase();return d||(f=mb[g],mb[g]=e,e=null!=c(a,b,d)?g:null,mb[g]=f),e}});var nb=/^(?:input|select|textarea|button)$/i,ob=/^(?:a|area)$/i;r.fn.extend({prop:function(a,b){return T(this,r.prop,a,b,arguments.length>1)},removeProp:function(a){return this.each(function(){delete this[r.propFix[a]||a]})}}),r.extend({prop:function(a,b,c){var d,e,f=a.nodeType;if(3!==f&&8!==f&&2!==f)return 1===f&&r.isXMLDoc(a)||(b=r.propFix[b]||b,e=r.propHooks[b]),void 0!==c?e&&"set"in e&&void 0!==(d=e.set(a,c,b))?d:a[b]=c:e&&"get"in e&&null!==(d=e.get(a,b))?d:a[b]},propHooks:{tabIndex:{get:function(a){var b=r.find.attr(a,"tabindex");return b?parseInt(b,10):nb.test(a.nodeName)||ob.test(a.nodeName)&&a.href?0:-1}}},propFix:{"for":"htmlFor","class":"className"}}),o.optSelected||(r.propHooks.selected={get:function(a){var b=a.parentNode;return b&&b.parentNode&&b.parentNode.selectedIndex,null},set:function(a){var b=a.parentNode;b&&(b.selectedIndex,b.parentNode&&b.parentNode.selectedIndex)}}),r.each(["tabIndex","readOnly","maxLength","cellSpacing","cellPadding","rowSpan","colSpan","useMap","frameBorder","contentEditable"],function(){r.propFix[this.toLowerCase()]=this});function pb(a){var b=a.match(L)||[];return b.join(" ")}function qb(a){return a.getAttribute&&a.getAttribute("class")||""}r.fn.extend({addClass:function(a){var b,c,d,e,f,g,h,i=0;if(r.isFunction(a))return this.each(function(b){r(this).addClass(a.call(this,b,qb(this)))});if("string"==typeof a&&a){b=a.match(L)||[];while(c=this[i++])if(e=qb(c),d=1===c.nodeType&&" "+pb(e)+" "){g=0;while(f=b[g++])d.indexOf(" "+f+" ")<0&&(d+=f+" ");h=pb(d),e!==h&&c.setAttribute("class",h)}}return this},removeClass:function(a){var b,c,d,e,f,g,h,i=0;if(r.isFunction(a))return this.each(function(b){r(this).removeClass(a.call(this,b,qb(this)))});if(!arguments.length)return this.attr("class","");if("string"==typeof a&&a){b=a.match(L)||[];while(c=this[i++])if(e=qb(c),d=1===c.nodeType&&" "+pb(e)+" "){g=0;while(f=b[g++])while(d.indexOf(" "+f+" ")>-1)d=d.replace(" "+f+" "," ");h=pb(d),e!==h&&c.setAttribute("class",h)}}return this},toggleClass:function(a,b){var c=typeof a;return"boolean"==typeof b&&"string"===c?b?this.addClass(a):this.removeClass(a):r.isFunction(a)?this.each(function(c){r(this).toggleClass(a.call(this,c,qb(this),b),b)}):this.each(function(){var b,d,e,f;if("string"===c){d=0,e=r(this),f=a.match(L)||[];while(b=f[d++])e.hasClass(b)?e.removeClass(b):e.addClass(b)}else void 0!==a&&"boolean"!==c||(b=qb(this),b&&W.set(this,"__className__",b),this.setAttribute&&this.setAttribute("class",b||a===!1?"":W.get(this,"__className__")||""))})},hasClass:function(a){var b,c,d=0;b=" "+a+" ";while(c=this[d++])if(1===c.nodeType&&(" "+pb(qb(c))+" ").indexOf(b)>-1)return!0;return!1}});var rb=/\r/g;r.fn.extend({val:function(a){var b,c,d,e=this[0];{if(arguments.length)return d=r.isFunction(a),this.each(function(c){var e;1===this.nodeType&&(e=d?a.call(this,c,r(this).val()):a,null==e?e="":"number"==typeof e?e+="":Array.isArray(e)&&(e=r.map(e,function(a){return null==a?"":a+""})),b=r.valHooks[this.type]||r.valHooks[this.nodeName.toLowerCase()],b&&"set"in b&&void 0!==b.set(this,e,"value")||(this.value=e))});if(e)return b=r.valHooks[e.type]||r.valHooks[e.nodeName.toLowerCase()],b&&"get"in b&&void 0!==(c=b.get(e,"value"))?c:(c=e.value,"string"==typeof c?c.replace(rb,""):null==c?"":c)}}}),r.extend({valHooks:{option:{get:function(a){var b=r.find.attr(a,"value");return null!=b?b:pb(r.text(a))}},select:{get:function(a){var b,c,d,e=a.options,f=a.selectedIndex,g="select-one"===a.type,h=g?null:[],i=g?f+1:e.length;for(d=f<0?i:g?f:0;d<i;d++)if(c=e[d],(c.selected||d===f)&&!c.disabled&&(!c.parentNode.disabled||!B(c.parentNode,"optgroup"))){if(b=r(c).val(),g)return b;h.push(b)}return h},set:function(a,b){var c,d,e=a.options,f=r.makeArray(b),g=e.length;while(g--)d=e[g],(d.selected=r.inArray(r.valHooks.option.get(d),f)>-1)&&(c=!0);return c||(a.selectedIndex=-1),f}}}}),r.each(["radio","checkbox"],function(){r.valHooks[this]={set:function(a,b){if(Array.isArray(b))return a.checked=r.inArray(r(a).val(),b)>-1}},o.checkOn||(r.valHooks[this].get=function(a){return null===a.getAttribute("value")?"on":a.value})});var sb=/^(?:focusinfocus|focusoutblur)$/;r.extend(r.event,{trigger:function(b,c,e,f){var g,h,i,j,k,m,n,o=[e||d],p=l.call(b,"type")?b.type:b,q=l.call(b,"namespace")?b.namespace.split("."):[];if(h=i=e=e||d,3!==e.nodeType&&8!==e.nodeType&&!sb.test(p+r.event.triggered)&&(p.indexOf(".")>-1&&(q=p.split("."),p=q.shift(),q.sort()),k=p.indexOf(":")<0&&"on"+p,b=b[r.expando]?b:new r.Event(p,"object"==typeof b&&b),b.isTrigger=f?2:3,b.namespace=q.join("."),b.rnamespace=b.namespace?new RegExp("(^|\\.)"+q.join("\\.(?:.*\\.|)")+"(\\.|$)"):null,b.result=void 0,b.target||(b.target=e),c=null==c?[b]:r.makeArray(c,[b]),n=r.event.special[p]||{},f||!n.trigger||n.trigger.apply(e,c)!==!1)){if(!f&&!n.noBubble&&!r.isWindow(e)){for(j=n.delegateType||p,sb.test(j+p)||(h=h.parentNode);h;h=h.parentNode)o.push(h),i=h;i===(e.ownerDocument||d)&&o.push(i.defaultView||i.parentWindow||a)}g=0;while((h=o[g++])&&!b.isPropagationStopped())b.type=g>1?j:n.bindType||p,m=(W.get(h,"events")||{})[b.type]&&W.get(h,"handle"),m&&m.apply(h,c),m=k&&h[k],m&&m.apply&&U(h)&&(b.result=m.apply(h,c),b.result===!1&&b.preventDefault());return b.type=p,f||b.isDefaultPrevented()||n._default&&n._default.apply(o.pop(),c)!==!1||!U(e)||k&&r.isFunction(e[p])&&!r.isWindow(e)&&(i=e[k],i&&(e[k]=null),r.event.triggered=p,e[p](),r.event.triggered=void 0,i&&(e[k]=i)),b.result}},simulate:function(a,b,c){var d=r.extend(new r.Event,c,{type:a,isSimulated:!0});r.event.trigger(d,null,b)}}),r.fn.extend({trigger:function(a,b){return this.each(function(){r.event.trigger(a,b,this)})},triggerHandler:function(a,b){var c=this[0];if(c)return r.event.trigger(a,b,c,!0)}}),r.each("blur focus focusin focusout resize scroll click dblclick mousedown mouseup mousemove mouseover mouseout mouseenter mouseleave change select submit keydown keypress keyup contextmenu".split(" "),function(a,b){r.fn[b]=function(a,c){return arguments.length>0?this.on(b,null,a,c):this.trigger(b)}}),r.fn.extend({hover:function(a,b){return this.mouseenter(a).mouseleave(b||a)}}),o.focusin="onfocusin"in a,o.focusin||r.each({focus:"focusin",blur:"focusout"},function(a,b){var c=function(a){r.event.simulate(b,a.target,r.event.fix(a))};r.event.special[b]={setup:function(){var d=this.ownerDocument||this,e=W.access(d,b);e||d.addEventListener(a,c,!0),W.access(d,b,(e||0)+1)},teardown:function(){var d=this.ownerDocument||this,e=W.access(d,b)-1;e?W.access(d,b,e):(d.removeEventListener(a,c,!0),W.remove(d,b))}}});var tb=a.location,ub=r.now(),vb=/\?/;r.parseXML=function(b){var c;if(!b||"string"!=typeof b)return null;try{c=(new a.DOMParser).parseFromString(b,"text/xml")}catch(d){c=void 0}return c&&!c.getElementsByTagName("parsererror").length||r.error("Invalid XML: "+b),c};var wb=/\[\]$/,xb=/\r?\n/g,yb=/^(?:submit|button|image|reset|file)$/i,zb=/^(?:input|select|textarea|keygen)/i;function Ab(a,b,c,d){var e;if(Array.isArray(b))r.each(b,function(b,e){c||wb.test(a)?d(a,e):Ab(a+"["+("object"==typeof e&&null!=e?b:"")+"]",e,c,d)});else if(c||"object"!==r.type(b))d(a,b);else for(e in b)Ab(a+"["+e+"]",b[e],c,d)}r.param=function(a,b){var c,d=[],e=function(a,b){var c=r.isFunction(b)?b():b;d[d.length]=encodeURIComponent(a)+"="+encodeURIComponent(null==c?"":c)};if(Array.isArray(a)||a.jquery&&!r.isPlainObject(a))r.each(a,function(){e(this.name,this.value)});else for(c in a)Ab(c,a[c],b,e);return d.join("&")},r.fn.extend({serialize:function(){return r.param(this.serializeArray())},serializeArray:function(){return this.map(function(){var a=r.prop(this,"elements");return a?r.makeArray(a):this}).filter(function(){var a=this.type;return this.name&&!r(this).is(":disabled")&&zb.test(this.nodeName)&&!yb.test(a)&&(this.checked||!ja.test(a))}).map(function(a,b){var c=r(this).val();return null==c?null:Array.isArray(c)?r.map(c,function(a){return{name:b.name,value:a.replace(xb,"\r\n")}}):{name:b.name,value:c.replace(xb,"\r\n")}}).get()}});var Bb=/%20/g,Cb=/#.*$/,Db=/([?&])_=[^&]*/,Eb=/^(.*?):[ \t]*([^\r\n]*)$/gm,Fb=/^(?:about|app|app-storage|.+-extension|file|res|widget):$/,Gb=/^(?:GET|HEAD)$/,Hb=/^\/\//,Ib={},Jb={},Kb="*/".concat("*"),Lb=d.createElement("a");Lb.href=tb.href;function Mb(a){return function(b,c){"string"!=typeof b&&(c=b,b="*");var d,e=0,f=b.toLowerCase().match(L)||[];if(r.isFunction(c))while(d=f[e++])"+"===d[0]?(d=d.slice(1)||"*",(a[d]=a[d]||[]).unshift(c)):(a[d]=a[d]||[]).push(c)}}function Nb(a,b,c,d){var e={},f=a===Jb;function g(h){var i;return e[h]=!0,r.each(a[h]||[],function(a,h){var j=h(b,c,d);return"string"!=typeof j||f||e[j]?f?!(i=j):void 0:(b.dataTypes.unshift(j),g(j),!1)}),i}return g(b.dataTypes[0])||!e["*"]&&g("*")}function Ob(a,b){var c,d,e=r.ajaxSettings.flatOptions||{};for(c in b)void 0!==b[c]&&((e[c]?a:d||(d={}))[c]=b[c]);return d&&r.extend(!0,a,d),a}function Pb(a,b,c){var d,e,f,g,h=a.contents,i=a.dataTypes;while("*"===i[0])i.shift(),void 0===d&&(d=a.mimeType||b.getResponseHeader("Content-Type"));if(d)for(e in h)if(h[e]&&h[e].test(d)){i.unshift(e);break}if(i[0]in c)f=i[0];else{for(e in c){if(!i[0]||a.converters[e+" "+i[0]]){f=e;break}g||(g=e)}f=f||g}if(f)return f!==i[0]&&i.unshift(f),c[f]}function Qb(a,b,c,d){var e,f,g,h,i,j={},k=a.dataTypes.slice();if(k[1])for(g in a.converters)j[g.toLowerCase()]=a.converters[g];f=k.shift();while(f)if(a.responseFields[f]&&(c[a.responseFields[f]]=b),!i&&d&&a.dataFilter&&(b=a.dataFilter(b,a.dataType)),i=f,f=k.shift())if("*"===f)f=i;else if("*"!==i&&i!==f){if(g=j[i+" "+f]||j["* "+f],!g)for(e in j)if(h=e.split(" "),h[1]===f&&(g=j[i+" "+h[0]]||j["* "+h[0]])){g===!0?g=j[e]:j[e]!==!0&&(f=h[0],k.unshift(h[1]));break}if(g!==!0)if(g&&a["throws"])b=g(b);else try{b=g(b)}catch(l){return{state:"parsererror",error:g?l:"No conversion from "+i+" to "+f}}}return{state:"success",data:b}}r.extend({active:0,lastModified:{},etag:{},ajaxSettings:{url:tb.href,type:"GET",isLocal:Fb.test(tb.protocol),global:!0,processData:!0,async:!0,contentType:"application/x-www-form-urlencoded; charset=UTF-8",accepts:{"*":Kb,text:"text/plain",html:"text/html",xml:"application/xml, text/xml",json:"application/json, text/javascript"},contents:{xml:/\bxml\b/,html:/\bhtml/,json:/\bjson\b/},responseFields:{xml:"responseXML",text:"responseText",json:"responseJSON"},converters:{"* text":String,"text html":!0,"text json":JSON.parse,"text xml":r.parseXML},flatOptions:{url:!0,context:!0}},ajaxSetup:function(a,b){return b?Ob(Ob(a,r.ajaxSettings),b):Ob(r.ajaxSettings,a)},ajaxPrefilter:Mb(Ib),ajaxTransport:Mb(Jb),ajax:function(b,c){"object"==typeof b&&(c=b,b=void 0),c=c||{};var e,f,g,h,i,j,k,l,m,n,o=r.ajaxSetup({},c),p=o.context||o,q=o.context&&(p.nodeType||p.jquery)?r(p):r.event,s=r.Deferred(),t=r.Callbacks("once memory"),u=o.statusCode||{},v={},w={},x="canceled",y={readyState:0,getResponseHeader:function(a){var b;if(k){if(!h){h={};while(b=Eb.exec(g))h[b[1].toLowerCase()]=b[2]}b=h[a.toLowerCase()]}return null==b?null:b},getAllResponseHeaders:function(){return k?g:null},setRequestHeader:function(a,b){return null==k&&(a=w[a.toLowerCase()]=w[a.toLowerCase()]||a,v[a]=b),this},overrideMimeType:function(a){return null==k&&(o.mimeType=a),this},statusCode:function(a){var b;if(a)if(k)y.always(a[y.status]);else for(b in a)u[b]=[u[b],a[b]];return this},abort:function(a){var b=a||x;return e&&e.abort(b),A(0,b),this}};if(s.promise(y),o.url=((b||o.url||tb.href)+"").replace(Hb,tb.protocol+"//"),o.type=c.method||c.type||o.method||o.type,o.dataTypes=(o.dataType||"*").toLowerCase().match(L)||[""],null==o.crossDomain){j=d.createElement("a");try{j.href=o.url,j.href=j.href,o.crossDomain=Lb.protocol+"//"+Lb.host!=j.protocol+"//"+j.host}catch(z){o.crossDomain=!0}}if(o.data&&o.processData&&"string"!=typeof o.data&&(o.data=r.param(o.data,o.traditional)),Nb(Ib,o,c,y),k)return y;l=r.event&&o.global,l&&0===r.active++&&r.event.trigger("ajaxStart"),o.type=o.type.toUpperCase(),o.hasContent=!Gb.test(o.type),f=o.url.replace(Cb,""),o.hasContent?o.data&&o.processData&&0===(o.contentType||"").indexOf("application/x-www-form-urlencoded")&&(o.data=o.data.replace(Bb,"+")):(n=o.url.slice(f.length),o.data&&(f+=(vb.test(f)?"&":"?")+o.data,delete o.data),o.cache===!1&&(f=f.replace(Db,"$1"),n=(vb.test(f)?"&":"?")+"_="+ub++ +n),o.url=f+n),o.ifModified&&(r.lastModified[f]&&y.setRequestHeader("If-Modified-Since",r.lastModified[f]),r.etag[f]&&y.setRequestHeader("If-None-Match",r.etag[f])),(o.data&&o.hasContent&&o.contentType!==!1||c.contentType)&&y.setRequestHeader("Content-Type",o.contentType),y.setRequestHeader("Accept",o.dataTypes[0]&&o.accepts[o.dataTypes[0]]?o.accepts[o.dataTypes[0]]+("*"!==o.dataTypes[0]?", "+Kb+"; q=0.01":""):o.accepts["*"]);for(m in o.headers)y.setRequestHeader(m,o.headers[m]);if(o.beforeSend&&(o.beforeSend.call(p,y,o)===!1||k))return y.abort();if(x="abort",t.add(o.complete),y.done(o.success),y.fail(o.error),e=Nb(Jb,o,c,y)){if(y.readyState=1,l&&q.trigger("ajaxSend",[y,o]),k)return y;o.async&&o.timeout>0&&(i=a.setTimeout(function(){y.abort("timeout")},o.timeout));try{k=!1,e.send(v,A)}catch(z){if(k)throw z;A(-1,z)}}else A(-1,"No Transport");function A(b,c,d,h){var j,m,n,v,w,x=c;k||(k=!0,i&&a.clearTimeout(i),e=void 0,g=h||"",y.readyState=b>0?4:0,j=b>=200&&b<300||304===b,d&&(v=Pb(o,y,d)),v=Qb(o,v,y,j),j?(o.ifModified&&(w=y.getResponseHeader("Last-Modified"),w&&(r.lastModified[f]=w),w=y.getResponseHeader("etag"),w&&(r.etag[f]=w)),204===b||"HEAD"===o.type?x="nocontent":304===b?x="notmodified":(x=v.state,m=v.data,n=v.error,j=!n)):(n=x,!b&&x||(x="error",b<0&&(b=0))),y.status=b,y.statusText=(c||x)+"",j?s.resolveWith(p,[m,x,y]):s.rejectWith(p,[y,x,n]),y.statusCode(u),u=void 0,l&&q.trigger(j?"ajaxSuccess":"ajaxError",[y,o,j?m:n]),t.fireWith(p,[y,x]),l&&(q.trigger("ajaxComplete",[y,o]),--r.active||r.event.trigger("ajaxStop")))}return y},getJSON:function(a,b,c){return r.get(a,b,c,"json")},getScript:function(a,b){return r.get(a,void 0,b,"script")}}),r.each(["get","post"],function(a,b){r[b]=function(a,c,d,e){return r.isFunction(c)&&(e=e||d,d=c,c=void 0),r.ajax(r.extend({url:a,type:b,dataType:e,data:c,success:d},r.isPlainObject(a)&&a))}}),r._evalUrl=function(a){return r.ajax({url:a,type:"GET",dataType:"script",cache:!0,async:!1,global:!1,"throws":!0})},r.fn.extend({wrapAll:function(a){var b;return this[0]&&(r.isFunction(a)&&(a=a.call(this[0])),b=r(a,this[0].ownerDocument).eq(0).clone(!0),this[0].parentNode&&b.insertBefore(this[0]),b.map(function(){var a=this;while(a.firstElementChild)a=a.firstElementChild;return a}).append(this)),this},wrapInner:function(a){return r.isFunction(a)?this.each(function(b){r(this).wrapInner(a.call(this,b))}):this.each(function(){var b=r(this),c=b.contents();c.length?c.wrapAll(a):b.append(a)})},wrap:function(a){var b=r.isFunction(a);return this.each(function(c){r(this).wrapAll(b?a.call(this,c):a)})},unwrap:function(a){return this.parent(a).not("body").each(function(){r(this).replaceWith(this.childNodes)}),this}}),r.expr.pseudos.hidden=function(a){return!r.expr.pseudos.visible(a)},r.expr.pseudos.visible=function(a){return!!(a.offsetWidth||a.offsetHeight||a.getClientRects().length)},r.ajaxSettings.xhr=function(){try{return new a.XMLHttpRequest}catch(b){}};var Rb={0:200,1223:204},Sb=r.ajaxSettings.xhr();o.cors=!!Sb&&"withCredentials"in Sb,o.ajax=Sb=!!Sb,r.ajaxTransport(function(b){var c,d;if(o.cors||Sb&&!b.crossDomain)return{send:function(e,f){var g,h=b.xhr();if(h.open(b.type,b.url,b.async,b.username,b.password),b.xhrFields)for(g in b.xhrFields)h[g]=b.xhrFields[g];b.mimeType&&h.overrideMimeType&&h.overrideMimeType(b.mimeType),b.crossDomain||e["X-Requested-With"]||(e["X-Requested-With"]="XMLHttpRequest");for(g in e)h.setRequestHeader(g,e[g]);c=function(a){return function(){c&&(c=d=h.onload=h.onerror=h.onabort=h.onreadystatechange=null,"abort"===a?h.abort():"error"===a?"number"!=typeof h.status?f(0,"error"):f(h.status,h.statusText):f(Rb[h.status]||h.status,h.statusText,"text"!==(h.responseType||"text")||"string"!=typeof h.responseText?{binary:h.response}:{text:h.responseText},h.getAllResponseHeaders()))}},h.onload=c(),d=h.onerror=c("error"),void 0!==h.onabort?h.onabort=d:h.onreadystatechange=function(){4===h.readyState&&a.setTimeout(function(){c&&d()})},c=c("abort");try{h.send(b.hasContent&&b.data||null)}catch(i){if(c)throw i}},abort:function(){c&&c()}}}),r.ajaxPrefilter(function(a){a.crossDomain&&(a.contents.script=!1)}),r.ajaxSetup({accepts:{script:"text/javascript, application/javascript, application/ecmascript, application/x-ecmascript"},contents:{script:/\b(?:java|ecma)script\b/},converters:{"text script":function(a){return r.globalEval(a),a}}}),r.ajaxPrefilter("script",function(a){void 0===a.cache&&(a.cache=!1),a.crossDomain&&(a.type="GET")}),r.ajaxTransport("script",function(a){if(a.crossDomain){var b,c;return{send:function(e,f){b=r("<script>").prop({charset:a.scriptCharset,src:a.url}).on("load error",c=function(a){b.remove(),c=null,a&&f("error"===a.type?404:200,a.type)}),d.head.appendChild(b[0])},abort:function(){c&&c()}}}});var Tb=[],Ub=/(=)\?(?=&|$)|\?\?/;r.ajaxSetup({jsonp:"callback",jsonpCallback:function(){var a=Tb.pop()||r.expando+"_"+ub++;return this[a]=!0,a}}),r.ajaxPrefilter("json jsonp",function(b,c,d){var e,f,g,h=b.jsonp!==!1&&(Ub.test(b.url)?"url":"string"==typeof b.data&&0===(b.contentType||"").indexOf("application/x-www-form-urlencoded")&&Ub.test(b.data)&&"data");if(h||"jsonp"===b.dataTypes[0])return e=b.jsonpCallback=r.isFunction(b.jsonpCallback)?b.jsonpCallback():b.jsonpCallback,h?b[h]=b[h].replace(Ub,"$1"+e):b.jsonp!==!1&&(b.url+=(vb.test(b.url)?"&":"?")+b.jsonp+"="+e),b.converters["script json"]=function(){return g||r.error(e+" was not called"),g[0]},b.dataTypes[0]="json",f=a[e],a[e]=function(){g=arguments},d.always(function(){void 0===f?r(a).removeProp(e):a[e]=f,b[e]&&(b.jsonpCallback=c.jsonpCallback,Tb.push(e)),g&&r.isFunction(f)&&f(g[0]),g=f=void 0}),"script"}),o.createHTMLDocument=function(){var a=d.implementation.createHTMLDocument("").body;return a.innerHTML="<form></form><form></form>",2===a.childNodes.length}(),r.parseHTML=function(a,b,c){if("string"!=typeof a)return[];"boolean"==typeof b&&(c=b,b=!1);var e,f,g;return b||(o.createHTMLDocument?(b=d.implementation.createHTMLDocument(""),e=b.createElement("base"),e.href=d.location.href,b.head.appendChild(e)):b=d),f=C.exec(a),g=!c&&[],f?[b.createElement(f[1])]:(f=qa([a],b,g),g&&g.length&&r(g).remove(),r.merge([],f.childNodes))},r.fn.load=function(a,b,c){var d,e,f,g=this,h=a.indexOf(" ");return h>-1&&(d=pb(a.slice(h)),a=a.slice(0,h)),r.isFunction(b)?(c=b,b=void 0):b&&"object"==typeof b&&(e="POST"),g.length>0&&r.ajax({url:a,type:e||"GET",dataType:"html",data:b}).done(function(a){f=arguments,g.html(d?r("<div>").append(r.parseHTML(a)).find(d):a)}).always(c&&function(a,b){g.each(function(){c.apply(this,f||[a.responseText,b,a])})}),this},r.each(["ajaxStart","ajaxStop","ajaxComplete","ajaxError","ajaxSuccess","ajaxSend"],function(a,b){r.fn[b]=function(a){return this.on(b,a)}}),r.expr.pseudos.animated=function(a){return r.grep(r.timers,function(b){return a===b.elem}).length},r.offset={setOffset:function(a,b,c){var d,e,f,g,h,i,j,k=r.css(a,"position"),l=r(a),m={};"static"===k&&(a.style.position="relative"),h=l.offset(),f=r.css(a,"top"),i=r.css(a,"left"),j=("absolute"===k||"fixed"===k)&&(f+i).indexOf("auto")>-1,j?(d=l.position(),g=d.top,e=d.left):(g=parseFloat(f)||0,e=parseFloat(i)||0),r.isFunction(b)&&(b=b.call(a,c,r.extend({},h))),null!=b.top&&(m.top=b.top-h.top+g),null!=b.left&&(m.left=b.left-h.left+e),"using"in b?b.using.call(a,m):l.css(m)}},r.fn.extend({offset:function(a){if(arguments.length)return void 0===a?this:this.each(function(b){r.offset.setOffset(this,a,b)});var b,c,d,e,f=this[0];if(f)return f.getClientRects().length?(d=f.getBoundingClientRect(),b=f.ownerDocument,c=b.documentElement,e=b.defaultView,{top:d.top+e.pageYOffset-c.clientTop,left:d.left+e.pageXOffset-c.clientLeft}):{top:0,left:0}},position:function(){if(this[0]){var a,b,c=this[0],d={top:0,left:0};return"fixed"===r.css(c,"position")?b=c.getBoundingClientRect():(a=this.offsetParent(),b=this.offset(),B(a[0],"html")||(d=a.offset()),d={top:d.top+r.css(a[0],"borderTopWidth",!0),left:d.left+r.css(a[0],"borderLeftWidth",!0)}),{top:b.top-d.top-r.css(c,"marginTop",!0),left:b.left-d.left-r.css(c,"marginLeft",!0)}}},offsetParent:function(){return this.map(function(){var a=this.offsetParent;while(a&&"static"===r.css(a,"position"))a=a.offsetParent;return a||ra})}}),r.each({scrollLeft:"pageXOffset",scrollTop:"pageYOffset"},function(a,b){var c="pageYOffset"===b;r.fn[a]=function(d){return T(this,function(a,d,e){var f;return r.isWindow(a)?f=a:9===a.nodeType&&(f=a.defaultView),void 0===e?f?f[b]:a[d]:void(f?f.scrollTo(c?f.pageXOffset:e,c?e:f.pageYOffset):a[d]=e)},a,d,arguments.length)}}),r.each(["top","left"],function(a,b){r.cssHooks[b]=Pa(o.pixelPosition,function(a,c){if(c)return c=Oa(a,b),Ma.test(c)?r(a).position()[b]+"px":c})}),r.each({Height:"height",Width:"width"},function(a,b){r.each({padding:"inner"+a,content:b,"":"outer"+a},function(c,d){r.fn[d]=function(e,f){var g=arguments.length&&(c||"boolean"!=typeof e),h=c||(e===!0||f===!0?"margin":"border");return T(this,function(b,c,e){var f;return r.isWindow(b)?0===d.indexOf("outer")?b["inner"+a]:b.document.documentElement["client"+a]:9===b.nodeType?(f=b.documentElement,Math.max(b.body["scroll"+a],f["scroll"+a],b.body["offset"+a],f["offset"+a],f["client"+a])):void 0===e?r.css(b,c,h):r.style(b,c,e,h)},b,g?e:void 0,g)}})}),r.fn.extend({bind:function(a,b,c){return this.on(a,null,b,c)},unbind:function(a,b){return this.off(a,null,b)},delegate:function(a,b,c,d){return this.on(b,a,c,d)},undelegate:function(a,b,c){return 1===arguments.length?this.off(a,"**"):this.off(b,a||"**",c)}}),r.holdReady=function(a){a?r.readyWait++:r.ready(!0)},r.isArray=Array.isArray,r.parseJSON=JSON.parse,r.nodeName=B,"function"==typeof define&&define.amd&&define("jquery",[],function(){return r});var Vb=a.jQuery,Wb=a.$;return r.noConflict=function(b){return a.$===r&&(a.$=Wb),b&&a.jQuery===r&&(a.jQuery=Vb),r},b||(a.jQuery=a.$=r),r});
}).call(this)}).call(this,require('_process'),typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],require("timers").setImmediate,require("timers").clearImmediate,"/sources/js-source/libs/jquery.js","/sources/js-source/libs")

},{"_process":4,"buffer":2,"timers":5}],"lodash":[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,setImmediate,clearImmediate,__filename,__dirname){(function (){
/**
 * @license
 * lodash 4.11.2 (Custom Build) lodash.com/license | Underscore.js 1.8.3
 *     underscorejs.org/LICENSE Build: `lodash -o ./dist/lodash.js`
 */
;
(function ()
{
    function t(t, n)
    {
        return t.set(n[0], n[1]), t
    }
    
    function n(t, n)
    {
        return t.add(n), t
    }
    
    function r(t, n, r)
    {
        switch (r.length) {
            case 0:
                return t.call(n);
            case 1:
                return t.call(n, r[0]);
            case 2:
                return t.call(n, r[0], r[1]);
            case 3:
                return t.call(n, r[0], r[1], r[2])
        }
        return t.apply(n, r)
    }
    
    function e(t, n, r, e)
    {
        for (var u = -1, o = t.length; ++u < o;) {
            var i = t[u];
            n(e, i, r(i), t)
        }
        return e
    }
    
    function u(t, n)
    {
        for (var r = -1, e = t.length; ++r < e && false !== n(t[r], r, t););
        return t
    }
    
    function o(t, n)
    {
        for (var r = -1, e = t.length; ++r < e;)if (!n(t[r], r, t))return false;
        return true
    }
    
    function i(t, n)
    {
        for (var r = -1, e = t.length, u = 0, o = []; ++r < e;) {
            var i = t[r];
            n(i, r, t) && (o[u++] = i)
        }
        return o
    }
    
    function f(t, n)
    {
        return !!t.length && -1 < g(t, n, 0)
    }
    
    function c(t, n, r)
    {
        for (var e = -1, u = t.length; ++e < u;)if (r(n, t[e]))return true;
        return false
    }
    
    function a(t, n)
    {
        for (var r = -1, e = t.length, u = Array(e); ++r < e;)u[r] = n(t[r], r, t);
        return u
    }
    
    function l(t, n)
    {
        for (var r = -1, e = n.length, u = t.length; ++r < e;)t[u + r] = n[r];
        return t
    }
    
    function s(t, n, r, e)
    {
        var u = -1, o = t.length;
        for (e && o && (r = t[++u]); ++u < o;)r = n(r, t[u], u, t);
        return r
    }
    
    function h(t, n, r, e)
    {
        var u = t.length;
        for (e && u && (r = t[--u]); u--;)r = n(r, t[u], u, t);
        return r
    }
    
    function p(t, n)
    {
        for (var r = -1, e = t.length; ++r < e;)if (n(t[r], r, t))return true;
        return false
    }
    
    function _(t, n, r, e)
    {
        var u;
        return r(t, function (t, r, o)
        {
            return n(t, r, o) ? (u = e ? r : t, false) : void 0
        }), u
    }
    
    function v(t, n, r)
    {
        for (var e = t.length, u = r ? e : -1; r ? u-- : ++u < e;)if (n(t[u], u,
                t))return u;
        return -1
    }
    
    function g(t, n, r)
    {
        if (n !== n)return B(t, r);
        --r;
        for (var e = t.length; ++r < e;)if (t[r] === n)return r;
        return -1
    }
    
    function d(t, n, r, e)
    {
        --r;
        for (var u = t.length; ++r < u;)if (e(t[r], n))return r;
        return -1
    }
    
    function y(t, n)
    {
        var r = t ? t.length : 0;
        return r ? j(t, n) / r : Z
    }
    
    function b(t, n, r, e, u)
    {
        return u(t, function (t, u, o)
        {
            r = e ? (e = false, t) : n(r, t, u, o)
        }), r
    }
    
    function x(t, n)
    {
        var r = t.length;
        for (t.sort(n); r--;)t[r] = t[r].c;
        return t
    }
    
    function j(t, n)
    {
        for (var r, e = -1, u = t.length; ++e < u;) {
            var o = n(t[e]);
            o !== N && (r = r === N ? o : r + o)
        }
        return r
    }
    
    function m(t, n)
    {
        for (var r = -1, e = Array(t); ++r < t;)e[r] = n(r);
        return e
    }
    
    function w(t, n)
    {
        return a(n, function (n)
        {
            return [n, t[n]]
        })
    }
    
    function A(t)
    {
        return function (n)
        {
            return t(n)
        }
    }
    
    function O(t, n)
    {
        return a(n, function (n)
        {
            return t[n]
        })
    }
    
    function k(t, n)
    {
        for (var r = -1, e = t.length; ++r < e && -1 < g(n, t[r], 0););
        return r
    }
    
    function E(t, n)
    {
        for (var r = t.length; r-- && -1 < g(n, t[r], 0););
        return r
    }
    
    function I(t)
    {
        return t && t.Object === Object ? t : null
    }
    
    function S(t)
    {
        return Lt[t]
    }
    
    function R(t)
    {
        return Ct[t]
    }
    
    function W(t)
    {
        return "\\" + zt[t]
    }
    
    function B(t, n, r)
    {
        var e = t.length;
        for (n += r ? 0 : -1; r ? n-- : ++n < e;) {
            var u = t[n];
            if (u !== u)return n
        }
        return -1
    }
    
    function L(t)
    {
        var n = false;
        if (null != t && typeof t.toString != "function")try {n = !!(t + "")} catch (r) {}
        return n
    }
    
    function C(t)
    {
        for (var n, r = []; !(n = t.next()).done;)r.push(n.value);
        return r
    }
    
    function M(t)
    {
        var n = -1, r = Array(t.size);
        return t.forEach(function (t, e)
        {
            r[++n] = [e, t]
        }), r
    }
    
    function U(t, n)
    {
        for (var r = -1, e = t.length, u = 0, o = []; ++r < e;) {
            var i = t[r];
            i !== n && "__lodash_placeholder__" !== i || (t[r] = "__lodash_placeholder__", o[u++] = r)
        }
        return o
    }
    
    function z(t)
    {
        var n = -1, r = Array(t.size);
        return t.forEach(function (t)
        {
            r[++n] = t
        }), r
    }
    
    function D(t)
    {
        if (!t || !It.test(t))return t.length;
        for (var n = kt.lastIndex = 0; kt.test(t);)n++;
        return n
    }
    
    function $(t)
    {
        return Mt[t]
    }
    
    function F(I)
    {
        function jt(t)
        {
            if (De(t) && !li(t) && !(t instanceof Lt)) {
                if (t instanceof wt)return t;
                if (wu.call(t, "__wrapped__"))return oe(t)
            }
            return new wt(t)
        }
        
        function mt()
        {
        }
        
        function wt(t, n)
        {
            this.__wrapped__ = t, this.__actions__ = [], this.__chain__ = !!n, this.__index__ = 0, this.__values__ = N
        }
        
        function Lt(t)
        {
            this.__wrapped__ = t, this.__actions__ = [], this.__dir__ = 1, this.__filtered__ = false, this.__iteratees__ = [], this.__takeCount__ = 4294967295, this.__views__ = []
        }
        
        function Ct()
        {
        }
        
        function Mt(t)
        {
            var n = -1, r = t ? t.length : 0;
            for (this.clear(); ++n < r;) {
                var e = t[n];
                this.set(e[0], e[1])
            }
        }
        
        function Ut(t)
        {
            var n = -1, r = t ? t.length : 0;
            for (this.__data__ = new Mt; ++n < r;)this.push(t[n])
        }
        
        function zt(t, n)
        {
            var r = t.__data__;
            return Hr(n) ? (r = r.__data__, "__lodash_hash_undefined__" === (typeof n == "string" ? r.string : r.hash)[n]) : r.has(n)
        }
        
        function Ft(t)
        {
            var n = -1, r = t ? t.length : 0;
            for (this.clear(); ++n < r;) {
                var e = t[n];
                this.set(e[0], e[1])
            }
        }
        
        function Nt(t, n)
        {
            var r = Tt(t, n);
            return 0 > r ? false : (r == t.length - 1 ? t.pop() : Fu.call(t, r,
                1), true)
        }
        
        function Zt(t, n)
        {
            var r = Tt(t, n);
            return 0 > r ? N : t[r][1]
        }
        
        function Tt(t, n)
        {
            for (var r = t.length; r--;)if (Se(t[r][0], n))return r;
            return -1
        }
        
        function qt(t, n, r)
        {
            var e = Tt(t, n);
            0 > e ? t.push([n, r]) : t[e][1] = r
        }
        
        function Gt(t, n, r, e)
        {
            return t === N || Se(t, xu[r]) && !wu.call(e, r) ? n : t
        }
        
        function Jt(t, n, r)
        {
            (r === N || Se(t[n],
                r)) && (typeof n != "number" || r !== N || n in t) || (t[n] = r)
        }
        
        function Yt(t, n, r)
        {
            var e = t[n];
            wu.call(t, n) && Se(e, r) && (r !== N || n in t) || (t[n] = r)
        }
        
        function Ht(t, n, r, e)
        {
            return yo(t, function (t, u, o)
            {
                n(e, t, r(t), o)
            }), e
        }
        
        function Qt(t, n)
        {
            return t && ar(n, tu(n), t)
        }
        
        function Xt(t, n)
        {
            for (var r = -1, e = null == t, u = n.length, o = Array(u); ++r < u;)o[r] = e ? N : Qe(t,
                n[r]);
            return o
        }
        
        function tn(t, n, r)
        {
            return t === t && (r !== N && (t = r >= t ? t : r),
            n !== N && (t = t >= n ? t : n)), t
        }
        
        function nn(t, n, r, e, o, i, f)
        {
            var c;
            if (e && (c = i ? e(t, o, i, f) : e(t)), c !== N)return c;
            if (!ze(t))return t;
            if (o = li(t)) {
                if (c = Pr(t), !n)return cr(t, c)
            } else {
                var a = Fr(t), l = "[object Function]" == a || "[object GeneratorFunction]" == a;
                if (si(t))return er(t, n);
                if ("[object Object]" == a || "[object Arguments]" == a || l && !i) {
                    if (L(t))return i ? t : {};
                    if (c = Zr(l ? {} : t), !n)return lr(t, Qt(c, t))
                } else {
                    if (!Bt[a])return i ? t : {};
                    c = Tr(t, a, nn, n)
                }
            }
            if (f || (f = new Ft), i = f.get(t))return i;
            if (f.set(t, c), !o)var s = r ? vn(t, tu, $r) : tu(t);
            return u(s || t, function (u, o)
            {
                s && (o = u, u = t[o]), Yt(c, o, nn(u, n, r, e, o, t, f))
            }), c
        }
        
        function rn(t)
        {
            var n = tu(t), r = n.length;
            return function (e)
            {
                if (null == e)return !r;
                for (var u = r; u--;) {
                    var o = n[u], i = t[o], f = e[o];
                    if (f === N && !(o in Object(e)) || !i(f))return false
                }
                return true
            }
        }
        
        function en(t)
        {
            return ze(t) ? zu(t) : {}
        }
        
        function un(t, n, r)
        {
            if (typeof t != "function")throw new yu("Expected a function");
            return $u(function ()
            {
                t.apply(N, r)
            }, n)
        }
        
        function on(t, n, r, e)
        {
            var u = -1, o = f, i = true, l = t.length, s = [], h = n.length;
            if (!l)return s;
            r && (n = a(n, A(r))), e ? (o = c,
                i = false) : n.length >= 200 && (o = zt, i = false, n = new Ut(n));
            t:for (; ++u < l;) {
                var p = t[u], _ = r ? r(p) : p, p = e || 0 !== p ? p : 0;
                if (i && _ === _) {
                    for (var v = h; v--;)if (n[v] === _)continue t;
                    s.push(p)
                } else o(n, _, e) || s.push(p)
            }
            return s
        }
        
        function fn(t, n)
        {
            var r = true;
            return yo(t, function (t, e, u)
            {
                return r = !!n(t, e, u)
            }), r
        }
        
        function cn(t, n, r)
        {
            for (var e = -1, u = t.length; ++e < u;) {
                var o = t[e], i = n(o);
                if (null != i && (f === N ? i === i && !Te(i) : r(i,
                        f)))var f = i, c = o
            }
            return c
        }
        
        function an(t, n)
        {
            var r = [];
            return yo(t, function (t, e, u)
            {
                n(t, e, u) && r.push(t)
            }), r
        }
        
        function ln(t, n, r, e, u)
        {
            var o = -1, i = t.length;
            for (r || (r = Vr), u || (u = []); ++o < i;) {
                var f = t[o];
                n > 0 && r(f) ? n > 1 ? ln(f, n - 1, r, e, u) : l(u,
                    f) : e || (u[u.length] = f)
            }
            return u
        }
        
        function sn(t, n)
        {
            return t && xo(t, n, tu)
        }
        
        function hn(t, n)
        {
            return t && jo(t, n, tu)
        }
        
        function pn(t, n)
        {
            return i(n, function (n)
            {
                return Ce(t[n])
            })
        }
        
        function _n(t, n)
        {
            n = Yr(n, t) ? [n] : nr(n);
            for (var r = 0, e = n.length; null != t && e > r;)t = t[ee(n[r++])];
            return r && r == e ? t : N
        }
        
        function vn(t, n, r)
        {
            return n = n(t), li(t) ? n : l(n, r(t))
        }
        
        function gn(t, n)
        {
            return t > n
        }
        
        function dn(t, n)
        {
            return wu.call(t,
                    n) || typeof t == "object" && n in t && null === Zu(Object(t));
        }
        
        function yn(t, n)
        {
            return n in Object(t)
        }
        
        function bn(t, n, r)
        {
            for (var e = r ? c : f, u = t[0].length, o = t.length, i = o, l = Array(o), s = 1 / 0, h = []; i--;) {
                var p = t[i];
                i && n && (p = a(p, A(n))), s = Gu(p.length,
                    s), l[i] = !r && (n || u >= 120 && p.length >= 120) ? new Ut(i && p) : N
            }
            var p = t[0], _ = -1, v = l[0];
            t:for (; ++_ < u && s > h.length;) {
                var g = p[_], d = n ? n(g) : g, g = r || 0 !== g ? g : 0;
                if (v ? !zt(v, d) : !e(h, d, r)) {
                    for (i = o; --i;) {
                        var y = l[i];
                        if (y ? !zt(y, d) : !e(t[i], d, r))continue t
                    }
                    v && v.push(d), h.push(g)
                }
            }
            return h
        }
        
        function xn(t, n, r)
        {
            var e = {};
            return sn(t, function (t, u, o)
            {
                n(e, r(t), u, o);
            }), e
        }
        
        function jn(t, n, e)
        {
            return Yr(n, t) || (n = nr(n), t = re(t,
                n), n = ae(n)), n = null == t ? t : t[ee(n)], null == n ? N : r(n, t,
                e)
        }
        
        function mn(t, n, r, e, u)
        {
            if (t === n)n = true; else if (null == t || null == n || !ze(t) && !De(n))n = t !== t && n !== n; else t:{
                var o = li(t), i = li(n), f = "[object Array]", c = "[object Array]";
                o || (f = Fr(t), f = "[object Arguments]" == f ? "[object Object]" : f), i || (c = Fr(n), c = "[object Arguments]" == c ? "[object Object]" : c);
                var a = "[object Object]" == f && !L(t), i = "[object Object]" == c && !L(n);
                if ((c = f == c) && !a)u || (u = new Ft), n = o || qe(t) ? Br(t, n,
                    mn, r, e, u) : Lr(t, n, f, mn, r, e, u); else {
                    if (!(2 & e) && (o = a && wu.call(t,
                                "__wrapped__"), f = i && wu.call(n,
                                "__wrapped__"), o || f)) {
                        t = o ? t.value() : t, n = f ? n.value() : n, u || (u = new Ft), n = mn(t,
                            n, r, e, u);
                        break t
                    }
                    if (c)n:if (u || (u = new Ft), o = 2 & e, f = tu(t), i = f.length, c = tu(n).length, i == c || o) {
                        for (a = i; a--;) {
                            var l = f[a];
                            if (!(o ? l in n : dn(n, l))) {
                                n = false;
                                break n
                            }
                        }
                        if (c = u.get(t))n = c == n; else {
                            c = true, u.set(t, n);
                            for (var s = o; ++a < i;) {
                                var l = f[a], h = t[l], p = n[l];
                                if (r)var _ = o ? r(p, h, l, n, t, u) : r(h, p, l, t,
                                    n, u);
                                if (_ === N ? h !== p && !mn(h, p, r, e, u) : !_) {
                                    c = false;
                                    break
                                }
                                s || (s = "constructor" == l)
                            }
                            c && !s && (r = t.constructor,
                                e = n.constructor, r != e && "constructor"in t && "constructor"in n && !(typeof r == "function" && r instanceof r && typeof e == "function" && e instanceof e) && (c = false)), u["delete"](t), n = c
                        }
                    } else n = false; else n = false
                }
            }
            return n
        }
        
        function wn(t, n, r, e)
        {
            var u = r.length, o = u, i = !e;
            if (null == t)return !o;
            for (t = Object(t); u--;) {
                var f = r[u];
                if (i && f[2] ? f[1] !== t[f[0]] : !(f[0]in t))return false
            }
            for (; ++u < o;) {
                var f = r[u], c = f[0], a = t[c], l = f[1];
                if (i && f[2]) {if (a === N && !(c in t))return false} else {
                    if (f = new Ft, e)var s = e(a, l, c, t, n, f);
                    if (s === N ? !mn(l, a, e, 3, f) : !s)return false;
                }
            }
            return true
        }
        
        function An(t)
        {
            return typeof t == "function" ? t : null == t ? au : typeof t == "object" ? li(t) ? Sn(t[0],
                t[1]) : In(t) : pu(t)
        }
        
        function On(t)
        {
            t = null == t ? t : Object(t);
            var n, r = [];
            for (n in t)r.push(n);
            return r
        }
        
        function kn(t, n)
        {
            return n > t
        }
        
        function En(t, n)
        {
            var r = -1, e = We(t) ? Array(t.length) : [];
            return yo(t, function (t, u, o)
            {
                e[++r] = n(t, u, o)
            }), e
        }
        
        function In(t)
        {
            var n = Ur(t);
            return 1 == n.length && n[0][2] ? te(n[0][0], n[0][1]) : function (r)
            {
                return r === t || wn(r, t, n)
            }
        }
        
        function Sn(t, n)
        {
            return Yr(t) && n === n && !ze(n) ? te(ee(t), n) : function (r)
            {
                var e = Qe(r, t);
                return e === N && e === n ? Xe(r, t) : mn(n, e, N, 3)
            }
        }
        
        function Rn(t, n, r, e, o)
        {
            if (t !== n) {
                if (!li(n) && !qe(n))var i = nu(n);
                u(i || n, function (u, f)
                {
                    if (i && (f = u, u = n[f]), ze(u)) {
                        o || (o = new Ft);
                        var c = f, a = o, l = t[c], s = n[c], h = a.get(s);
                        if (h)Jt(t, c, h); else {
                            var h = e ? e(l, s, c + "", t, n, a) : N, p = h === N;
                            p && (h = s, li(s) || qe(s) ? li(l) ? h = l : Be(l) ? h = cr(l) : (p = false, h = nn(s,
                                true)) : Ne(s) || Re(s) ? Re(l) ? h = Ye(l) : !ze(l) || r && Ce(l) ? (p = false, h = nn(s,
                                true)) : h = l : p = false), a.set(s, h), p && Rn(h,
                                s, r, e, a), a["delete"](s), Jt(t, c, h)
                        }
                    } else c = e ? e(t[f], u, f + "", t, n,
                        o) : N, c === N && (c = u),
                        Jt(t, f, c)
                })
            }
        }
        
        function Wn(t, n)
        {
            var r = t.length;
            return r ? (n += 0 > n ? r : 0, Gr(n, r) ? t[n] : N) : void 0
        }
        
        function Bn(t, n, r)
        {
            var e = -1;
            return n = a(n.length ? n : [au], A(Mr())), t = En(t, function (t)
            {
                return {
                    a: a(n, function (n)
                    {
                        return n(t)
                    }),
                    b: ++e,
                    c: t
                }
            }), x(t, function (t, n)
            {
                var e;
                t:{
                    e = -1;
                    for (var u = t.a, o = n.a, i = u.length, f = r.length; ++e < i;) {
                        var c = or(u[e], o[e]);
                        if (c) {
                            e = e >= f ? c : c * ("desc" == r[e] ? -1 : 1);
                            break t
                        }
                    }
                    e = t.b - n.b
                }
                return e
            })
        }
        
        function Ln(t, n)
        {
            return t = Object(t), s(n, function (n, r)
            {
                return r in t && (n[r] = t[r]), n
            }, {})
        }
        
        function Cn(t, n)
        {
            for (var r = -1, e = vn(t, nu, ko), u = e.length, o = {}; ++r < u;) {
                var i = e[r], f = t[i];
                n(f, i) && (o[i] = f)
            }
            return o
        }
        
        function Mn(t)
        {
            return function (n)
            {
                return null == n ? N : n[t]
            }
        }
        
        function Un(t)
        {
            return function (n)
            {
                return _n(n, t)
            }
        }
        
        function zn(t, n, r, e)
        {
            var u = e ? d : g, o = -1, i = n.length, f = t;
            for (r && (f = a(t,
                A(r))); ++o < i;)for (var c = 0, l = n[o], l = r ? r(l) : l; -1 < (c = u(f,
                l, c, e));)f !== t && Fu.call(f, c, 1), Fu.call(t, c, 1);
            return t
        }
        
        function Dn(t, n)
        {
            for (var r = t ? n.length : 0, e = r - 1; r--;) {
                var u = n[r];
                if (r == e || u !== o) {
                    var o = u;
                    if (Gr(u))Fu.call(t, u, 1); else if (Yr(u,
                            t))delete t[ee(u)]; else {
                        var u = nr(u), i = re(t, u);
                        null != i && delete i[ee(ae(u))];
                    }
                }
            }
        }
        
        function $n(t, n)
        {
            return t + Pu(Yu() * (n - t + 1))
        }
        
        function Fn(t, n)
        {
            var r = "";
            if (!t || 1 > n || n > 9007199254740991)return r;
            do n % 2 && (r += t), (n = Pu(n / 2)) && (t += t); while (n);
            return r
        }
        
        function Nn(t, n, r, e)
        {
            n = Yr(n, t) ? [n] : nr(n);
            for (var u = -1, o = n.length, i = o - 1, f = t; null != f && ++u < o;) {
                var c = ee(n[u]);
                if (ze(f)) {
                    var a = r;
                    if (u != i) {
                        var l = f[c], a = e ? e(l, c, f) : N;
                        a === N && (a = null == l ? Gr(n[u + 1]) ? [] : {} : l)
                    }
                    Yt(f, c, a)
                }
                f = f[c]
            }
            return t
        }
        
        function Pn(t, n, r)
        {
            var e = -1, u = t.length;
            for (0 > n && (n = -n > u ? 0 : u + n), r = r > u ? u : r, 0 > r && (r += u), u = n > r ? 0 : r - n >>> 0, n >>>= 0, r = Array(u); ++e < u;)r[e] = t[e + n];
            return r
        }
        
        function Zn(t, n)
        {
            var r;
            return yo(t, function (t, e, u)
            {
                return r = n(t, e, u), !r
            }), !!r
        }
        
        function Tn(t, n, r)
        {
            var e = 0, u = t ? t.length : e;
            if (typeof n == "number" && n === n && 2147483647 >= u) {
                for (; u > e;) {
                    var o = e + u >>> 1, i = t[o];
                    null !== i && !Te(i) && (r ? n >= i : n > i) ? e = o + 1 : u = o
                }
                return u
            }
            return qn(t, n, au, r)
        }
        
        function qn(t, n, r, e)
        {
            n = r(n);
            for (var u = 0, o = t ? t.length : 0, i = n !== n, f = null === n, c = Te(n), a = n === N; o > u;) {
                var l = Pu((u + o) / 2), s = r(t[l]), h = s !== N, p = null === s, _ = s === s, v = Te(s);
                (i ? e || _ : a ? _ && (e || h) : f ? _ && h && (e || !p) : c ? _ && h && !p && (e || !v) : p || v ? 0 : e ? n >= s : n > s) ? u = l + 1 : o = l;
            }
            return Gu(o, 4294967294)
        }
        
        function Vn(t, n)
        {
            for (var r = -1, e = t.length, u = 0, o = []; ++r < e;) {
                var i = t[r], f = n ? n(i) : i;
                if (!r || !Se(f, c)) {
                    var c = f;
                    o[u++] = 0 === i ? 0 : i
                }
            }
            return o
        }
        
        function Kn(t)
        {
            return typeof t == "number" ? t : Te(t) ? Z : +t
        }
        
        function Gn(t)
        {
            if (typeof t == "string")return t;
            if (Te(t))return go ? go.call(t) : "";
            var n = t + "";
            return "0" == n && 1 / t == -P ? "-0" : n
        }
        
        function Jn(t, n, r)
        {
            var e = -1, u = f, o = t.length, i = true, a = [], l = a;
            if (r)i = false, u = c; else if (o >= 200) {
                if (u = n ? null : wo(t))return z(u);
                i = false, u = zt, l = new Ut
            } else l = n ? [] : a;
            t:for (; ++e < o;) {
                var s = t[e], h = n ? n(s) : s, s = r || 0 !== s ? s : 0;
                if (i && h === h) {
                    for (var p = l.length; p--;)if (l[p] === h)continue t;
                    n && l.push(h), a.push(s)
                } else u(l, h, r) || (l !== a && l.push(h), a.push(s))
            }
            return a
        }
        
        function Yn(t, n, r, e)
        {
            for (var u = t.length, o = e ? u : -1; (e ? o-- : ++o < u) && n(t[o], o,
                t););
            return r ? Pn(t, e ? 0 : o, e ? o + 1 : u) : Pn(t, e ? o + 1 : 0,
                e ? u : o)
        }
        
        function Hn(t, n)
        {
            var r = t;
            return r instanceof Lt && (r = r.value()), s(n, function (t, n)
            {
                return n.func.apply(n.thisArg, l([t], n.args))
            }, r)
        }
        
        function Qn(t, n, r)
        {
            for (var e = -1, u = t.length; ++e < u;)var o = o ? l(on(o, t[e], n, r),
                on(t[e], o, n, r)) : t[e];
            return o && o.length ? Jn(o, n, r) : [];
        }
        
        function Xn(t, n, r)
        {
            for (var e = -1, u = t.length, o = n.length, i = {}; ++e < u;)r(i, t[e],
                o > e ? n[e] : N);
            return i
        }
        
        function tr(t)
        {
            return Be(t) ? t : []
        }
        
        function nr(t)
        {
            return li(t) ? t : Io(t)
        }
        
        function rr(t, n, r)
        {
            var e = t.length;
            return r = r === N ? e : r, !n && r >= e ? t : Pn(t, n, r)
        }
        
        function er(t, n)
        {
            if (n)return t.slice();
            var r = new t.constructor(t.length);
            return t.copy(r), r
        }
        
        function ur(t)
        {
            var n = new t.constructor(t.byteLength);
            return new Bu(n).set(new Bu(t)), n
        }
        
        function or(t, n)
        {
            if (t !== n) {
                var r = t !== N, e = null === t, u = t === t, o = Te(t), i = n !== N, f = null === n, c = n === n, a = Te(n);
                if (!f && !a && !o && t > n || o && i && c && !f && !a || e && i && c || !r && c || !u)return 1;
                if (!e && !o && !a && n > t || a && r && u && !e && !o || f && r && u || !i && u || !c)return -1
            }
            return 0
        }
        
        function ir(t, n, r, e)
        {
            var u     = -1, o = t.length, i = r.length, f = -1, c = n.length, a = Ku(o - i,
                0), l = Array(c + a);
            for (e = !e; ++f < c;)l[f] = n[f];
            for (; ++u < i;)(e || o > u) && (l[r[u]] = t[u]);
            for (; a--;)l[f++] = t[u++];
            return l
        }
        
        function fr(t, n, r, e)
        {
            var u     = -1, o = t.length, i = -1, f = r.length, c = -1, a = n.length, l = Ku(o - f,
                0), s = Array(l + a);
            for (e = !e; ++u < l;)s[u] = t[u];
            for (l = u; ++c < a;)s[l + c] = n[c];
            for (; ++i < f;)(e || o > u) && (s[l + r[i]] = t[u++]);
            return s
        }
        
        function cr(t, n)
        {
            var r = -1, e = t.length;
            for (n || (n = Array(e)); ++r < e;)n[r] = t[r];
            return n
        }
        
        function ar(t, n, r, e)
        {
            r || (r = {});
            for (var u = -1, o = n.length; ++u < o;) {
                var i = n[u], f = e ? e(r[i], t[i], i, r, t) : t[i];
                Yt(r, i, f)
            }
            return r
        }
        
        function lr(t, n)
        {
            return ar(t, $r(t), n)
        }
        
        function sr(t, n)
        {
            return function (r, u)
            {
                var o = li(r) ? e : Ht, i = n ? n() : {};
                return o(r, t, Mr(u), i)
            }
        }
        
        function hr(t)
        {
            return Ee(function (n, r)
            {
                var e = -1, u = r.length, o = u > 1 ? r[u - 1] : N, i = u > 2 ? r[2] : N, o = typeof o == "function" ? (u--, o) : N;
                for (i && Jr(r[0], r[1],
                    i) && (o = 3 > u ? N : o, u = 1), n = Object(n); ++e < u;)(i = r[e]) && t(n,
                    i, e, o);
                return n
            })
        }
        
        function pr(t, n)
        {
            return function (r, e)
            {
                if (null == r)return r;
                if (!We(r))return t(r, e);
                for (var u = r.length, o = n ? u : -1, i = Object(r); (n ? o-- : ++o < u) && false !== e(i[o],
                    o, i););
                return r
            }
        }
        
        function _r(t)
        {
            return function (n, r, e)
            {
                var u = -1, o = Object(n);
                e = e(n);
                for (var i = e.length; i--;) {
                    var f = e[t ? i : ++u];
                    if (false === r(o[f], f, o))break
                }
                return n
            }
        }
        
        function vr(t, n, r)
        {
            function e()
            {
                return (this && this !== Vt && this instanceof e ? o : t).apply(u ? r : this,
                    arguments)
            }
            
            var u = 1 & n, o = yr(t);
            return e
        }
        
        function gr(t)
        {
            return function (n)
            {
                n = He(n);
                var r = It.test(n) ? n.match(kt) : N, e = r ? r[0] : n.charAt(0);
                return n = r ? rr(r, 1).join("") : n.slice(1), e[t]() + n
            }
        }
        
        function dr(t)
        {
            return function (n)
            {
                return s(fu(iu(n).replace(At, "")), t, "")
            }
        }
        
        function yr(t)
        {
            return function ()
            {
                var n = arguments;
                switch (n.length) {
                    case 0:
                        return new t;
                    case 1:
                        return new t(n[0]);
                    case 2:
                        return new t(n[0], n[1]);
                    case 3:
                        return new t(n[0], n[1], n[2]);
                    case 4:
                        return new t(n[0], n[1], n[2], n[3]);
                    case 5:
                        return new t(n[0], n[1], n[2], n[3], n[4]);
                    case 6:
                        return new t(n[0], n[1], n[2], n[3], n[4], n[5]);
                    case 7:
                        return new t(n[0], n[1], n[2], n[3], n[4], n[5], n[6])
                }
                var r = en(t.prototype), n = t.apply(r, n);
                return ze(n) ? n : r
            }
        }
        
        function br(t, n, e)
        {
            function u()
            {
                for (var i = arguments.length, f = Array(i), c = i, a = Dr(u); c--;)f[c] = arguments[c];
                return c = 3 > i && f[0] !== a && f[i - 1] !== a ? [] : U(f,
                    a), i -= c.length, e > i ? Sr(t, n, jr, u.placeholder, N, f, c,
                    N, N,
                    e - i) : r(this && this !== Vt && this instanceof u ? o : t,
                    this, f)
            }
            
            var o = yr(t);
            return u
        }
        
        function xr(t)
        {
            return Ee(function (n)
            {
                n = ln(n, 1);
                var r = n.length, e = r, u = wt.prototype.thru;
                for (t && n.reverse(); e--;) {
                    var o = n[e];
                    if (typeof o != "function")throw new yu("Expected a function");
                    if (u && !i && "wrapper" == Cr(o))var i = new wt([], true);
                }
                for (e = i ? e : r; ++e < r;)var o = n[e], u = Cr(o), f = "wrapper" == u ? Ao(o) : N, i = f && Qr(f[0]) && 424 == f[1] && !f[4].length && 1 == f[9] ? i[Cr(f[0])].apply(i,
                    f[3]) : 1 == o.length && Qr(o) ? i[u]() : i.thru(o);
                return function ()
                {
                    var t = arguments, e = t[0];
                    if (i && 1 == t.length && li(e) && e.length >= 200)return i.plant(e).value();
                    for (var u = 0, t = r ? n[u].apply(this,
                        t) : e; ++u < r;)t = n[u].call(this, t);
                    return t
                }
            })
        }
        
        function jr(t, n, r, e, u, o, i, f, c, a)
        {
            function l()
            {
                for (var d = arguments.length, y = d, b = Array(d); y--;)b[y] = arguments[y];
                if (_) {
                    var x, j = Dr(l), y = b.length;
                    for (x = 0; y--;)b[y] === j && x++;
                }
                if (e && (b = ir(b, e, u, _)), o && (b = fr(b, o, i,
                        _)), d -= x, _ && a > d)return j = U(b, j), Sr(t, n, jr,
                    l.placeholder, r, b, j, f, c, a - d);
                if (j = h ? r : this, y = p ? j[t] : t, d = b.length, f) {
                    x = b.length;
                    for (var m = Gu(f.length, x), w = cr(b); m--;) {
                        var A = f[m];
                        b[m] = Gr(A, x) ? w[A] : N
                    }
                } else v && d > 1 && b.reverse();
                return s && d > c && (b.length = c), this && this !== Vt && this instanceof l && (y = g || yr(y)), y.apply(j,
                    b)
            }
            
            var s = 128 & n, h = 1 & n, p = 2 & n, _ = 24 & n, v = 512 & n, g = p ? N : yr(t);
            return l
        }
        
        function mr(t, n)
        {
            return function (r, e)
            {
                return xn(r, t, n(e))
            }
        }
        
        function wr(t)
        {
            return function (n, r)
            {
                var e;
                if (n === N && r === N)return 0;
                if (n !== N && (e = n), r !== N) {
                    if (e === N)return r;
                    typeof n == "string" || typeof r == "string" ? (n = Gn(n), r = Gn(r)) : (n = Kn(n), r = Kn(r)), e = t(n,
                        r)
                }
                return e
            }
        }
        
        function Ar(t)
        {
            return Ee(function (n)
            {
                return n = 1 == n.length && li(n[0]) ? a(n[0], A(Mr())) : a(ln(n, 1,
                    Kr), A(Mr())), Ee(function (e)
                {
                    var u = this;
                    return t(n, function (t)
                    {
                        return r(t, u, e)
                    })
                })
            })
        }
        
        function Or(t, n)
        {
            n = n === N ? " " : Gn(n);
            var r = n.length;
            return 2 > r ? r ? Fn(n, t) : n : (r = Fn(n,
                Nu(t / D(n))), It.test(n) ? rr(r.match(kt), 0,
                t).join("") : r.slice(0, t))
        }
        
        function kr(t, n, e, u)
        {
            function o()
            {
                for (var n = -1, c = arguments.length, a = -1, l = u.length, s = Array(l + c), h = this && this !== Vt && this instanceof o ? f : t; ++a < l;)s[a] = u[a];
                for (; c--;)s[a++] = arguments[++n];
                return r(h, i ? e : this, s)
            }
            
            var i = 1 & n, f = yr(t);
            return o
        }
        
        function Er(t)
        {
            return function (n, r, e)
            {
                e && typeof e != "number" && Jr(n, r,
                    e) && (r = e = N), n = Je(n), n = n === n ? n : 0, r === N ? (r = n, n = 0) : r = Je(r) || 0, e = e === N ? r > n ? 1 : -1 : Je(e) || 0;
                var u = -1;
                r = Ku(Nu((r - n) / (e || 1)), 0);
                for (var o = Array(r); r--;)o[t ? r : ++u] = n, n += e;
                return o
            }
        }
        
        function Ir(t)
        {
            return function (n, r)
            {
                return typeof n == "string" && typeof r == "string" || (n = Je(n),
                    r = Je(r)), t(n, r)
            }
        }
        
        function Sr(t, n, r, e, u, o, i, f, c, a)
        {
            var l = 8 & n, s = l ? i : N;
            i = l ? N : i;
            var h = l ? o : N;
            return o = l ? N : o, n = (n | (l ? 32 : 64)) & ~(l ? 64 : 32), 4 & n || (n &= -4), n = [
                t, n, u, h, s, o, i, f, c, a
            ], r = r.apply(N, n), Qr(t) && Eo(r, n), r.placeholder = e, r
        }
        
        function Rr(t)
        {
            var n = gu[t];
            return function (t, r)
            {
                if (t = Je(t), r = Ke(r)) {
                    var e = (He(t) + "e").split("e"), e = n(e[0] + "e" + (+e[1] + r)), e = (He(e) + "e").split("e");
                    return +(e[0] + "e" + (+e[1] - r))
                }
                return n(t)
            }
        }
        
        function Wr(t, n, r, e, u, o, i, f)
        {
            var c = 2 & n;
            if (!c && typeof t != "function")throw new yu("Expected a function");
            var a = e ? e.length : 0;
            if (a || (n &= -97, e = u = N), i = i === N ? i : Ku(Ke(i),
                    0), f = f === N ? f : Ke(f), a -= u ? u.length : 0, 64 & n) {
                var l = e, s = u;
                e = u = N
            }
            var h = c ? N : Ao(t);
            return o = [
                t, n, r, e, u, l, s, o, i, f
            ], h && (r = o[1], t = h[1], n = r | t, e = 128 == t && 8 == r || 128 == t && 256 == r && h[8] >= o[7].length || 384 == t && h[8] >= h[7].length && 8 == r, 131 > n || e) && (1 & t && (o[2] = h[2], n |= 1 & r ? 0 : 4), (r = h[3]) && (e = o[3], o[3] = e ? ir(e,
                r, h[4]) : r, o[4] = e ? U(o[3],
                "__lodash_placeholder__") : h[4]), (r = h[5]) && (e = o[5], o[5] = e ? fr(e,
                r, h[6]) : r, o[6] = e ? U(o[5],
                "__lodash_placeholder__") : h[6]), (r = h[7]) && (o[7] = r),
            128 & t && (o[8] = null == o[8] ? h[8] : Gu(o[8],
                h[8])), null == o[9] && (o[9] = h[9]), o[0] = h[0], o[1] = n), t = o[0], n = o[1], r = o[2], e = o[3], u = o[4], f = o[9] = null == o[9] ? c ? 0 : t.length : Ku(o[9] - a,
                0), !f && 24 & n && (n &= -25), (h ? mo : Eo)(n && 1 != n ? 8 == n || 16 == n ? br(t,
                n, f) : 32 != n && 33 != n || u.length ? jr.apply(N, o) : kr(t, n, r,
                e) : vr(t, n, r), o)
        }
        
        function Br(t, n, r, e, u, o)
        {
            var i = -1, f = 2 & u, c = 1 & u, a = t.length, l = n.length;
            if (a != l && !(f && l > a))return false;
            if (l = o.get(t))return l == n;
            for (l = true, o.set(t, n); ++i < a;) {
                var s = t[i], h = n[i];
                if (e)var _ = f ? e(h, s, i, n, t, o) : e(s, h, i, t, n, o);
                if (_ !== N) {
                    if (_)continue;
                    l = false;
                    break
                }
                if (c) {
                    if (!p(n, function (t)
                        {
                            return s === t || r(s, t, e, u, o)
                        })) {
                        l = false;
                        break
                    }
                } else if (s !== h && !r(s, h, e, u, o)) {
                    l = false;
                    break
                }
            }
            return o["delete"](t), l
        }
        
        function Lr(t, n, r, e, u, o, i)
        {
            switch (r) {
                case"[object DataView]":
                    if (t.byteLength != n.byteLength || t.byteOffset != n.byteOffset)break;
                    t = t.buffer, n = n.buffer;
                case"[object ArrayBuffer]":
                    if (t.byteLength != n.byteLength || !e(new Bu(t),
                            new Bu(n)))break;
                    return true;
                case"[object Boolean]":
                case"[object Date]":
                    return +t == +n;
                case"[object Error]":
                    return t.name == n.name && t.message == n.message;
                case"[object Number]":
                    return t != +t ? n != +n : t == +n;
                case"[object RegExp]":
                case"[object String]":
                    return t == n + "";
                case"[object Map]":
                    var f = M;
                case"[object Set]":
                    if (f || (f = z), t.size != n.size && !(2 & o))break;
                    return (r = i.get(t)) ? r == n : (o |= 1, i.set(t, n), Br(f(t),
                        f(n), e, u, o, i));
                case"[object Symbol]":
                    if (vo)return vo.call(t) == vo.call(n)
            }
            return false
        }
        
        function Cr(t)
        {
            for (var n = t.name + "", r = co[n], e = wu.call(co,
                n) ? r.length : 0; e--;) {
                var u = r[e], o = u.func;
                if (null == o || o == t)return u.name
            }
            return n
        }
        
        function Mr()
        {
            var t = jt.iteratee || lu, t = t === lu ? An : t;
            return arguments.length ? t(arguments[0], arguments[1]) : t
        }
        
        function Ur(t)
        {
            t = ru(t);
            for (var n = t.length; n--;) {
                var r = t[n][1];
                t[n][2] = r === r && !ze(r)
            }
            return t
        }
        
        function zr(t, n)
        {
            var r = t[n];
            return $e(r) ? r : N
        }
        
        function Dr(t)
        {
            return (wu.call(jt, "placeholder") ? jt : t).placeholder
        }
        
        function $r(t)
        {
            return Mu(Object(t))
        }
        
        function Fr(t)
        {
            return ku.call(t)
        }
        
        function Nr(t, n, r)
        {
            n = Yr(n, t) ? [n] : nr(n);
            for (var e, u = -1, o = n.length; ++u < o;) {
                var i = ee(n[u]);
                if (!(e = null != t && r(t, i)))break;
                t = t[i]
            }
            return e ? e : (o = t ? t.length : 0, !!o && Ue(o) && Gr(i,
                o) && (li(t) || Ze(t) || Re(t)));
        }
        
        function Pr(t)
        {
            var n = t.length, r = t.constructor(n);
            return n && "string" == typeof t[0] && wu.call(t,
                "index") && (r.index = t.index, r.input = t.input), r
        }
        
        function Zr(t)
        {
            return typeof t.constructor != "function" || Xr(t) ? {} : en(Zu(Object(t)))
        }
        
        function Tr(r, e, u, o)
        {
            var i = r.constructor;
            switch (e) {
                case"[object ArrayBuffer]":
                    return ur(r);
                case"[object Boolean]":
                case"[object Date]":
                    return new i(+r);
                case"[object DataView]":
                    return e = o ? ur(r.buffer) : r.buffer, new r.constructor(e,
                        r.byteOffset, r.byteLength);
                case"[object Float32Array]":
                case"[object Float64Array]":
                case"[object Int8Array]":
                case"[object Int16Array]":
                case"[object Int32Array]":
                case"[object Uint8Array]":
                case"[object Uint8ClampedArray]":
                case"[object Uint16Array]":
                case"[object Uint32Array]":
                    return e = o ? ur(r.buffer) : r.buffer, new r.constructor(e,
                        r.byteOffset, r.length);
                case"[object Map]":
                    return e = o ? u(M(r), true) : M(r), s(e, t, new r.constructor);
                case"[object Number]":
                case"[object String]":
                    return new i(r);
                case"[object RegExp]":
                    return e = new r.constructor(r.source,
                        st.exec(r)), e.lastIndex = r.lastIndex,
                        e;
                case"[object Set]":
                    return e = o ? u(z(r), true) : z(r), s(e, n, new r.constructor);
                case"[object Symbol]":
                    return vo ? Object(vo.call(r)) : {}
            }
        }
        
        function qr(t)
        {
            var n = t ? t.length : N;
            return Ue(n) && (li(t) || Ze(t) || Re(t)) ? m(n, String) : null
        }
        
        function Vr(t)
        {
            return Be(t) && (li(t) || Re(t))
        }
        
        function Kr(t)
        {
            return li(t) && !(2 == t.length && !Ce(t[0]))
        }
        
        function Gr(t, n)
        {
            return n = null == n ? 9007199254740991 : n, !!n && (typeof t == "number" || dt.test(t)) && t > -1 && 0 == t % 1 && n > t
        }
        
        function Jr(t, n, r)
        {
            if (!ze(r))return false;
            var e = typeof n;
            return ("number" == e ? We(r) && Gr(n,
                r.length) : "string" == e && n in r) ? Se(r[n], t) : false;
        }
        
        function Yr(t, n)
        {
            if (li(t))return false;
            var r = typeof t;
            return "number" == r || "symbol" == r || "boolean" == r || null == t || Te(t) ? true : nt.test(t) || !tt.test(t) || null != n && t in Object(n)
        }
        
        function Hr(t)
        {
            var n = typeof t;
            return "string" == n || "number" == n || "symbol" == n || "boolean" == n ? "__proto__" !== t : null === t
        }
        
        function Qr(t)
        {
            var n = Cr(t), r = jt[n];
            return typeof r == "function" && n in Lt.prototype ? t === r ? true : (n = Ao(r), !!n && t === n[0]) : false
        }
        
        function Xr(t)
        {
            var n = t && t.constructor;
            return t === (typeof n == "function" && n.prototype || xu)
        }
        
        function te(t, n)
        {
            return function (r)
            {
                return null == r ? false : r[t] === n && (n !== N || t in Object(r))
            }
        }
        
        function ne(t, n, r, e, u, o)
        {
            return ze(t) && ze(n) && Rn(t, n, N, ne, o.set(n, t)), t
        }
        
        function re(t, n)
        {
            return 1 == n.length ? t : _n(t, Pn(n, 0, -1))
        }
        
        function ee(t)
        {
            if (typeof t == "string" || Te(t))return t;
            var n = t + "";
            return "0" == n && 1 / t == -P ? "-0" : n
        }
        
        function ue(t)
        {
            if (null != t) {
                try {return mu.call(t)} catch (n) {}
                return t + ""
            }
            return ""
        }
        
        function oe(t)
        {
            if (t instanceof Lt)return t.clone();
            var n = new wt(t.__wrapped__, t.__chain__);
            return n.__actions__ = cr(t.__actions__), n.__index__ = t.__index__,
                n.__values__ = t.__values__, n
        }
        
        function ie(t, n, r)
        {
            var e = t ? t.length : 0;
            return e ? (n = r || n === N ? 1 : Ke(n), Pn(t, 0 > n ? 0 : n, e)) : []
        }
        
        function fe(t, n, r)
        {
            var e = t ? t.length : 0;
            return e ? (n = r || n === N ? 1 : Ke(n), n = e - n, Pn(t, 0,
                0 > n ? 0 : n)) : []
        }
        
        function ce(t)
        {
            return t && t.length ? t[0] : N
        }
        
        function ae(t)
        {
            var n = t ? t.length : 0;
            return n ? t[n - 1] : N
        }
        
        function le(t, n)
        {
            return t && t.length && n && n.length ? zn(t, n) : t
        }
        
        function se(t)
        {
            return t ? Qu.call(t) : t
        }
        
        function he(t)
        {
            if (!t || !t.length)return [];
            var n = 0;
            return t = i(t, function (t)
            {
                return Be(t) ? (n = Ku(t.length, n),
                    !0) : void 0
            }), m(n, function (n)
            {
                return a(t, Mn(n))
            })
        }
        
        function pe(t, n)
        {
            if (!t || !t.length)return [];
            var e = he(t);
            return null == n ? e : a(e, function (t)
            {
                return r(n, N, t)
            })
        }
        
        function _e(t)
        {
            return t = jt(t), t.__chain__ = true, t
        }
        
        function ve(t, n)
        {
            return n(t)
        }
        
        function ge()
        {
            return this
        }
        
        function de(t, n)
        {
            return typeof n == "function" && li(t) ? u(t, n) : yo(t, Mr(n))
        }
        
        function ye(t, n)
        {
            var r;
            if (typeof n == "function" && li(t)) {
                for (r = t.length; r-- && false !== n(t[r], r, t););
                r = t
            } else r = bo(t, Mr(n));
            return r
        }
        
        function be(t, n)
        {
            return (li(t) ? a : En)(t, Mr(n, 3))
        }
        
        function xe(t, n, r)
        {
            var e = -1, u = Ve(t), o = u.length, i = o - 1;
            for (n = (r ? Jr(t, n, r) : n === N) ? 1 : tn(Ke(n), 0,
                o); ++e < n;)t = $n(e, i), r = u[t], u[t] = u[e], u[e] = r;
            return u.length = n, u
        }
        
        function je(t, n, r)
        {
            return n = r ? N : n, n = t && null == n ? t.length : n, Wr(t, 128, N, N,
                N, N, n)
        }
        
        function me(t, n)
        {
            var r;
            if (typeof n != "function")throw new yu("Expected a function");
            return t = Ke(t), function ()
            {
                return 0 < --t && (r = n.apply(this,
                    arguments)), 1 >= t && (n = N), r
            }
        }
        
        function we(t, n, r)
        {
            return n = r ? N : n, t = Wr(t, 8, N, N, N, N, N,
                n), t.placeholder = we.placeholder, t
        }
        
        function Ae(t, n, r)
        {
            return n = r ? N : n,
                t = Wr(t, 16, N, N, N, N, N, n), t.placeholder = Ae.placeholder, t
        }
        
        function Oe(t, n, r)
        {
            function e(n)
            {
                var r = c, e = a;
                return c = a = N, _ = n, s = t.apply(e, r)
            }
            
            function u(t)
            {
                var r = t - p;
                return t -= _, !p || r >= n || 0 > r || g && t >= l
            }
            
            function o()
            {
                var t = Xo();
                if (u(t))return i(t);
                var r;
                r = t - _, t = n - (t - p), r = g ? Gu(t, l - r) : t, h = $u(o, r)
            }
            
            function i(t)
            {
                return Lu(h), h = N, d && c ? e(t) : (c = a = N, s)
            }
            
            function f()
            {
                var t = Xo(), r = u(t);
                if (c = arguments, a = this, p = t, r) {
                    if (h === N)return _ = t = p, h = $u(o, n), v ? e(t) : s;
                    if (g)return Lu(h), h = $u(o, n), e(p)
                }
                return h === N && (h = $u(o, n)), s
            }
            
            var c, a, l, s, h, p = 0, _ = 0, v = false, g = false, d = true;
            if (typeof t != "function")throw new yu("Expected a function");
            return n = Je(n) || 0, ze(r) && (v = !!r.leading, l = (g = "maxWait"in r) ? Ku(Je(r.maxWait) || 0,
                n) : l, d = "trailing"in r ? !!r.trailing : d), f.cancel = function ()
            {
                h !== N && Lu(h), p = _ = 0, c = a = h = N
            }, f.flush = function ()
            {
                return h === N ? s : i(Xo())
            }, f
        }
        
        function ke(t, n)
        {
            function r()
            {
                var e = arguments, u = n ? n.apply(this, e) : e[0], o = r.cache;
                return o.has(u) ? o.get(u) : (e = t.apply(this,
                    e), r.cache = o.set(u, e), e)
            }
            
            if (typeof t != "function" || n && typeof n != "function")throw new yu("Expected a function");
            return r.cache = new (ke.Cache || Mt), r
        }
        
        function Ee(t, n)
        {
            if (typeof t != "function")throw new yu("Expected a function");
            return n = Ku(n === N ? t.length - 1 : Ke(n), 0), function ()
            {
                for (var e = arguments, u = -1, o = Ku(e.length - n,
                    0), i  = Array(o); ++u < o;)i[u] = e[n + u];
                switch (n) {
                    case 0:
                        return t.call(this, i);
                    case 1:
                        return t.call(this, e[0], i);
                    case 2:
                        return t.call(this, e[0], e[1], i)
                }
                for (o = Array(n + 1), u = -1; ++u < n;)o[u] = e[u];
                return o[n] = i, r(t, this, o)
            }
        }
        
        function Ie()
        {
            if (!arguments.length)return [];
            var t = arguments[0];
            return li(t) ? t : [t]
        }
        
        function Se(t, n)
        {
            return t === n || t !== t && n !== n
        }
        
        function Re(t)
        {
            return Be(t) && wu.call(t, "callee") && (!Du.call(t,
                    "callee") || "[object Arguments]" == ku.call(t))
        }
        
        function We(t)
        {
            return null != t && Ue(Oo(t)) && !Ce(t)
        }
        
        function Be(t)
        {
            return De(t) && We(t)
        }
        
        function Le(t)
        {
            return De(t) ? "[object Error]" == ku.call(t) || typeof t.message == "string" && typeof t.name == "string" : false
        }
        
        function Ce(t)
        {
            return t = ze(t) ? ku.call(t) : "", "[object Function]" == t || "[object GeneratorFunction]" == t
        }
        
        function Me(t)
        {
            return typeof t == "number" && t == Ke(t)
        }
        
        function Ue(t)
        {
            return typeof t == "number" && t > -1 && 0 == t % 1 && 9007199254740991 >= t;
        }
        
        function ze(t)
        {
            var n = typeof t;
            return !!t && ("object" == n || "function" == n)
        }
        
        function De(t)
        {
            return !!t && typeof t == "object"
        }
        
        function $e(t)
        {
            return ze(t) ? (Ce(t) || L(t) ? Iu : vt).test(ue(t)) : false
        }
        
        function Fe(t)
        {
            return typeof t == "number" || De(t) && "[object Number]" == ku.call(t)
        }
        
        function Ne(t)
        {
            return !De(t) || "[object Object]" != ku.call(t) || L(t) ? false : (t = Zu(Object(t)), null === t ? true : (t = wu.call(t,
                    "constructor") && t.constructor, typeof t == "function" && t instanceof t && mu.call(t) == Ou))
        }
        
        function Pe(t)
        {
            return ze(t) && "[object RegExp]" == ku.call(t);
        }
        
        function Ze(t)
        {
            return typeof t == "string" || !li(t) && De(t) && "[object String]" == ku.call(t)
        }
        
        function Te(t)
        {
            return typeof t == "symbol" || De(t) && "[object Symbol]" == ku.call(t)
        }
        
        function qe(t)
        {
            return De(t) && Ue(t.length) && !!Wt[ku.call(t)]
        }
        
        function Ve(t)
        {
            if (!t)return [];
            if (We(t))return Ze(t) ? t.match(kt) : cr(t);
            if (Uu && t[Uu])return C(t[Uu]());
            var n = Fr(t);
            return ("[object Map]" == n ? M : "[object Set]" == n ? z : uu)(t)
        }
        
        function Ke(t)
        {
            if (!t)return 0 === t ? t : 0;
            if (t = Je(t), t === P || t === -P)return 1.7976931348623157e308 * (0 > t ? -1 : 1);
            var n = t % 1;
            return t === t ? n ? t - n : t : 0
        }
        
        function Ge(t)
        {
            return t ? tn(Ke(t), 0, 4294967295) : 0
        }
        
        function Je(t)
        {
            if (typeof t == "number")return t;
            if (Te(t))return Z;
            if (ze(t) && (t = Ce(t.valueOf) ? t.valueOf() : t, t = ze(t) ? t + "" : t), typeof t != "string")return 0 === t ? t : +t;
            t = t.replace(ot, "");
            var n = _t.test(t);
            return n || gt.test(t) ? $t(t.slice(2), n ? 2 : 8) : pt.test(t) ? Z : +t
        }
        
        function Ye(t)
        {
            return ar(t, nu(t))
        }
        
        function He(t)
        {
            return null == t ? "" : Gn(t)
        }
        
        function Qe(t, n, r)
        {
            return t = null == t ? N : _n(t, n), t === N ? r : t
        }
        
        function Xe(t, n)
        {
            return null != t && Nr(t, n, yn)
        }
        
        function tu(t)
        {
            var n = Xr(t);
            if (!n && !We(t))return Vu(Object(t));
            var r, e = qr(t), u = !!e, e = e || [], o = e.length;
            for (r in t)!dn(t, r) || u && ("length" == r || Gr(r,
                o)) || n && "constructor" == r || e.push(r);
            return e
        }
        
        function nu(t)
        {
            for (var n = -1, r = Xr(t), e = On(t), u = e.length, o = qr(t), i = !!o, o = o || [], f = o.length; ++n < u;) {
                var c = e[n];
                i && ("length" == c || Gr(c,
                    f)) || "constructor" == c && (r || !wu.call(t, c)) || o.push(c)
            }
            return o
        }
        
        function ru(t)
        {
            return w(t, tu(t))
        }
        
        function eu(t)
        {
            return w(t, nu(t))
        }
        
        function uu(t)
        {
            return t ? O(t, tu(t)) : []
        }
        
        function ou(t)
        {
            return Mi(He(t).toLowerCase());
        }
        
        function iu(t)
        {
            return (t = He(t)) && t.replace(yt, S).replace(Ot, "")
        }
        
        function fu(t, n, r)
        {
            return t = He(t), n = r ? N : n, n === N && (n = St.test(t) ? Et : ct), t.match(n) || []
        }
        
        function cu(t)
        {
            return function ()
            {
                return t
            }
        }
        
        function au(t)
        {
            return t
        }
        
        function lu(t)
        {
            return An(typeof t == "function" ? t : nn(t, true))
        }
        
        function su(t, n, r)
        {
            var e = tu(n), o = pn(n, e);
            null != r || ze(n) && (o.length || !e.length) || (r = n, n = t, t = this, o = pn(n,
                tu(n)));
            var i = !(ze(r) && "chain"in r && !r.chain), f = Ce(t);
            return u(o, function (r)
            {
                var e = n[r];
                t[r] = e, f && (t.prototype[r] = function ()
                {
                    var n = this.__chain__;
                    if (i || n) {
                        var r = t(this.__wrapped__);
                        return (r.__actions__ = cr(this.__actions__)).push({
                            func   : e,
                            args   : arguments,
                            thisArg: t
                        }), r.__chain__ = n, r
                    }
                    return e.apply(t, l([this.value()], arguments))
                })
            }), t
        }
        
        function hu()
        {
        }
        
        function pu(t)
        {
            return Yr(t) ? Mn(ee(t)) : Un(t)
        }
        
        I = I ? Kt.defaults({}, I, Kt.pick(Vt, Rt)) : Vt;
        var _u                      = I.Date, vu = I.Error, gu = I.Math, du = I.RegExp, yu = I.TypeError, bu = I.Array.prototype, xu = I.Object.prototype, ju = I.String.prototype, mu = I.Function.prototype.toString, wu = xu.hasOwnProperty, Au = 0, Ou = mu.call(Object), ku = xu.toString, Eu = Vt._, Iu = du("^" + mu.call(wu).replace(et,
                "\\$&").replace(/hasOwnProperty|(function).*?(?=\\\()| for .+?(?=\\\])/g,
                "$1.*?") + "$"), Su = Pt ? I.Buffer : N, Ru = I.Reflect, Wu = I.Symbol, Bu = I.Uint8Array, Lu = I.clearTimeout, Cu = Ru ? Ru.f : N, Mu = Object.getOwnPropertySymbols, Uu = typeof(Uu = Wu && Wu.iterator) == "symbol" ? Uu : N, zu = Object.create, Du = xu.propertyIsEnumerable, $u = I.setTimeout, Fu = bu.splice, Nu = gu.ceil, Pu = gu.floor, Zu = Object.getPrototypeOf, Tu = I.isFinite, qu = bu.join, Vu = Object.keys, Ku = gu.max, Gu = gu.min, Ju = I.parseInt, Yu = gu.random, Hu = ju.replace, Qu = bu.reverse, Xu = ju.split, to = zr(I,
            "DataView"), no         = zr(I, "Map"), ro = zr(I, "Promise"), eo = zr(I,
            "Set"), uo              = zr(I, "WeakMap"), oo = zr(Object,
            "create"), io           = uo && new uo, fo = !Du.call({
                valueOf: 1
            },
            "valueOf"), co          = {}, ao = ue(to), lo = ue(no), so = ue(ro), ho = ue(eo), po = ue(uo), _o = Wu ? Wu.prototype : N, vo = _o ? _o.valueOf : N, go = _o ? _o.toString : N;
        jt.templateSettings = {
            escape     : H,
            evaluate   : Q,
            interpolate: X,
            variable   : "",
            imports    : {_: jt}
        }, jt.prototype = mt.prototype, jt.prototype.constructor = jt, wt.prototype = en(mt.prototype), wt.prototype.constructor = wt, Lt.prototype = en(mt.prototype), Lt.prototype.constructor = Lt, Ct.prototype = oo ? oo(null) : xu, Mt.prototype.clear = function ()
        {
            this.__data__ = {
                hash  : new Ct,
                map   : no ? new no : [],
                string: new Ct
            }
        }, Mt.prototype["delete"] = function (t)
        {
            var n = this.__data__;
            return Hr(t) ? (n = typeof t == "string" ? n.string : n.hash, t = (oo ? n[t] !== N : wu.call(n,
                    t)) && delete n[t]) : t = no ? n.map["delete"](t) : Nt(n.map,
                t), t
        }, Mt.prototype.get = function (t)
        {
            var n = this.__data__;
            return Hr(t) ? (n = typeof t == "string" ? n.string : n.hash, oo ? (t = n[t], t = "__lodash_hash_undefined__" === t ? N : t) : t = wu.call(n,
                t) ? n[t] : N) : t = no ? n.map.get(t) : Zt(n.map, t), t
        }, Mt.prototype.has = function (t)
        {
            var n = this.__data__;
            return Hr(t) ? (n = typeof t == "string" ? n.string : n.hash, t = oo ? n[t] !== N : wu.call(n,
                t)) : t = no ? n.map.has(t) : -1 < Tt(n.map, t),
                t
        }, Mt.prototype.set = function (t, n)
        {
            var r = this.__data__;
            return Hr(t) ? (typeof t == "string" ? r.string : r.hash)[t] = oo && n === N ? "__lodash_hash_undefined__" : n : no ? r.map.set(t,
                n) : qt(r.map, t, n), this
        }, Ut.prototype.push = function (t)
        {
            var n = this.__data__;
            Hr(t) ? (n = n.__data__, (typeof t == "string" ? n.string : n.hash)[t] = "__lodash_hash_undefined__") : n.set(t,
                "__lodash_hash_undefined__")
        }, Ft.prototype.clear = function ()
        {
            this.__data__ = {
                array: [],
                map  : null
            }
        }, Ft.prototype["delete"] = function (t)
        {
            var n = this.__data__, r = n.array;
            return r ? Nt(r, t) : n.map["delete"](t);
        }, Ft.prototype.get = function (t)
        {
            var n = this.__data__, r = n.array;
            return r ? Zt(r, t) : n.map.get(t)
        }, Ft.prototype.has = function (t)
        {
            var n = this.__data__, r = n.array;
            return r ? -1 < Tt(r, t) : n.map.has(t)
        }, Ft.prototype.set = function (t, n)
        {
            var r = this.__data__, e = r.array;
            return e && (199 > e.length ? qt(e, t,
                n) : (r.array = null, r.map = new Mt(e))), (r = r.map) && r.set(t,
                n), this
        };
        var yo = pr(sn), bo = pr(hn, true), xo = _r(), jo = _r(true);
        Cu && !Du.call({valueOf: 1}, "valueOf") && (On = function (t)
        {
            return C(Cu(t))
        });
        var mo     = io ? function (t, n)
        {
            return io.set(t, n), t
        } : au, wo = eo && 1 / z(new eo([, -0]))[1] == P ? function (t)
        {
            return new eo(t)
        } : hu, Ao = io ? function (t)
        {
            return io.get(t)
        } : hu, Oo = Mn("length");
        Mu || ($r = function ()
        {
            return []
        });
        var ko = Mu ? function (t)
        {
            for (var n = []; t;)l(n, $r(t)), t = Zu(Object(t));
            return n
        } : $r;
        (to && "[object DataView]" != Fr(new to(new ArrayBuffer(1))) || no && "[object Map]" != Fr(new no) || ro && "[object Promise]" != Fr(ro.resolve()) || eo && "[object Set]" != Fr(new eo) || uo && "[object WeakMap]" != Fr(new uo)) && (Fr = function (t)
        {
            var n = ku.call(t);
            if (t = (t = "[object Object]" == n ? t.constructor : N) ? ue(t) : N)switch (t) {
                case ao:
                    return "[object DataView]";
                case lo:
                    return "[object Map]";
                case so:
                    return "[object Promise]";
                case ho:
                    return "[object Set]";
                case po:
                    return "[object WeakMap]"
            }
            return n
        });
        var Eo  = function ()
        {
            var t = 0, n = 0;
            return function (r, e)
            {
                var u = Xo(), o = 16 - (u - n);
                if (n = u, o > 0) {if (150 <= ++t)return r} else t = 0;
                return mo(r, e)
            }
        }(), Io = ke(function (t)
        {
            var n = [];
            return He(t).replace(rt, function (t, r, e, u)
            {
                n.push(e ? u.replace(at, "$1") : r || t)
            }), n
        }), So  = Ee(function (t, n)
        {
            return Be(t) ? on(t, ln(n, 1, Be, true)) : []
        }), Ro  = Ee(function (t, n)
        {
            var r = ae(n);
            return Be(r) && (r = N), Be(t) ? on(t, ln(n, 1, Be, true), Mr(r)) : [];
        }), Wo  = Ee(function (t, n)
        {
            var r = ae(n);
            return Be(r) && (r = N), Be(t) ? on(t, ln(n, 1, Be, true), N, r) : []
        }), Bo  = Ee(function (t)
        {
            var n = a(t, tr);
            return n.length && n[0] === t[0] ? bn(n) : []
        }), Lo  = Ee(function (t)
        {
            var n = ae(t), r = a(t, tr);
            return n === ae(r) ? n = N : r.pop(), r.length && r[0] === t[0] ? bn(r,
                Mr(n)) : []
        }), Co  = Ee(function (t)
        {
            var n = ae(t), r = a(t, tr);
            return n === ae(r) ? n = N : r.pop(), r.length && r[0] === t[0] ? bn(r,
                N, n) : []
        }), Mo  = Ee(le), Uo = Ee(function (t, n)
        {
            n = ln(n, 1);
            var r = t ? t.length : 0, e = Xt(t, n);
            return Dn(t, a(n, function (t)
            {
                return Gr(t, r) ? +t : t
            }).sort(or)),
                e
        }), zo  = Ee(function (t)
        {
            return Jn(ln(t, 1, Be, true))
        }), Do  = Ee(function (t)
        {
            var n = ae(t);
            return Be(n) && (n = N), Jn(ln(t, 1, Be, true), Mr(n))
        }), $o  = Ee(function (t)
        {
            var n = ae(t);
            return Be(n) && (n = N), Jn(ln(t, 1, Be, true), N, n)
        }), Fo  = Ee(function (t, n)
        {
            return Be(t) ? on(t, n) : []
        }), No  = Ee(function (t)
        {
            return Qn(i(t, Be))
        }), Po  = Ee(function (t)
        {
            var n = ae(t);
            return Be(n) && (n = N), Qn(i(t, Be), Mr(n))
        }), Zo  = Ee(function (t)
        {
            var n = ae(t);
            return Be(n) && (n = N), Qn(i(t, Be), N, n)
        }), To  = Ee(he), qo = Ee(function (t)
        {
            var n = t.length, n = n > 1 ? t[n - 1] : N, n = typeof n == "function" ? (t.pop(),
                n) : N;
            return pe(t, n)
        }), Vo  = Ee(function (t)
        {
            function n(n)
            {
                return Xt(n, t)
            }
            
            t = ln(t, 1);
            var r = t.length, e = r ? t[0] : 0, u = this.__wrapped__;
            return !(r > 1 || this.__actions__.length) && u instanceof Lt && Gr(e) ? (u = u.slice(e,
                +e + (r ? 1 : 0)), u.__actions__.push({
                func   : ve,
                args   : [n],
                thisArg: N
            }), new wt(u, this.__chain__).thru(function (t)
            {
                return r && !t.length && t.push(N), t
            })) : this.thru(n)
        }), Ko  = sr(function (t, n, r)
        {
            wu.call(t, r) ? ++t[r] : t[r] = 1
        }), Go  = sr(function (t, n, r)
        {
            wu.call(t, r) ? t[r].push(n) : t[r] = [n]
        }), Jo  = Ee(function (t, n, e)
        {
            var u = -1, o = typeof n == "function", i = Yr(n), f = We(t) ? Array(t.length) : [];
            return yo(t, function (t)
            {
                var c = o ? n : i && null != t ? t[n] : N;
                f[++u] = c ? r(c, t, e) : jn(t, n, e)
            }), f
        }), Yo  = sr(function (t, n, r)
        {
            t[r] = n
        }), Ho  = sr(function (t, n, r)
        {
            t[r ? 0 : 1].push(n)
        }, function ()
        {
            return [[], []]
        }), Qo  = Ee(function (t, n)
        {
            if (null == t)return [];
            var r = n.length;
            return r > 1 && Jr(t, n[0], n[1]) ? n = [] : r > 2 && Jr(n[0], n[1],
                n[2]) && (n = [n[0]]), n = 1 == n.length && li(n[0]) ? n[0] : ln(n,
                1, Kr), Bn(t, n, [])
        }), Xo  = _u.now, ti = Ee(function (t, n, r)
        {
            var e = 1;
            if (r.length)var u = U(r, Dr(ti)), e = 32 | e;
            return Wr(t, e, n, r, u)
        }), ni  = Ee(function (t, n, r)
        {
            var e = 3;
            if (r.length)var u = U(r, Dr(ni)), e = 32 | e;
            return Wr(n, e, t, r, u)
        }), ri  = Ee(function (t, n)
        {
            return un(t, 1, n)
        }), ei  = Ee(function (t, n, r)
        {
            return un(t, Je(n) || 0, r)
        });
        ke.Cache = Mt;
        var ui            = Ee(function (t, n)
        {
            n = 1 == n.length && li(n[0]) ? a(n[0], A(Mr())) : a(ln(n, 1, Kr),
                A(Mr()));
            var e = n.length;
            return Ee(function (u)
            {
                for (var o = -1, i = Gu(u.length, e); ++o < i;)u[o] = n[o].call(this,
                    u[o]);
                return r(t, this, u)
            })
        }), oi            = Ee(function (t, n)
        {
            var r = U(n, Dr(oi));
            return Wr(t, 32, N, n, r)
        }), ii            = Ee(function (t, n)
        {
            var r = U(n, Dr(ii));
            return Wr(t, 64, N, n, r)
        }), fi            = Ee(function (t, n)
        {
            return Wr(t, 256, N, N, N, ln(n, 1));
        }), ci            = Ir(gn), ai = Ir(function (t, n)
        {
            return t >= n
        }), li            = Array.isArray, si = Su ? function (t)
        {
            return t instanceof Su
        } : cu(false), hi = Ir(kn), pi = Ir(function (t, n)
        {
            return n >= t
        }), _i            = hr(function (t, n)
        {
            if (fo || Xr(n) || We(n))ar(n, tu(n), t); else for (var r in n)wu.call(n,
                r) && Yt(t, r, n[r])
        }), vi            = hr(function (t, n)
        {
            if (fo || Xr(n) || We(n))ar(n, nu(n), t); else for (var r in n)Yt(t, r,
                n[r])
        }), gi            = hr(function (t, n, r, e)
        {
            ar(n, nu(n), t, e)
        }), di            = hr(function (t, n, r, e)
        {
            ar(n, tu(n), t, e)
        }), yi            = Ee(function (t, n)
        {
            return Xt(t, ln(n, 1))
        }), bi            = Ee(function (t)
        {
            return t.push(N, Gt),
                r(gi, N, t)
        }), xi            = Ee(function (t)
        {
            return t.push(N, ne), r(Oi, N, t)
        }), ji            = mr(function (t, n, r)
        {
            t[n] = r
        }, cu(au)), mi    = mr(function (t, n, r)
        {
            wu.call(t, n) ? t[n].push(r) : t[n] = [r]
        }, Mr), wi        = Ee(jn), Ai = hr(function (t, n, r)
        {
            Rn(t, n, r)
        }), Oi            = hr(function (t, n, r, e)
        {
            Rn(t, n, r, e)
        }), ki            = Ee(function (t, n)
        {
            return null == t ? {} : (n = a(ln(n, 1), ee), Ln(t,
                on(vn(t, nu, ko), n)))
        }), Ei            = Ee(function (t, n)
        {
            return null == t ? {} : Ln(t, a(ln(n, 1), ee))
        }), Ii            = dr(function (t, n, r)
        {
            return n = n.toLowerCase(), t + (r ? ou(n) : n)
        }), Si            = dr(function (t, n, r)
        {
            return t + (r ? "-" : "") + n.toLowerCase();
        }), Ri            = dr(function (t, n, r)
        {
            return t + (r ? " " : "") + n.toLowerCase()
        }), Wi            = gr("toLowerCase"), Bi = dr(function (t, n, r)
        {
            return t + (r ? "_" : "") + n.toLowerCase()
        }), Li            = dr(function (t, n, r)
        {
            return t + (r ? " " : "") + Mi(n)
        }), Ci            = dr(function (t, n, r)
        {
            return t + (r ? " " : "") + n.toUpperCase()
        }), Mi            = gr("toUpperCase"), Ui = Ee(function (t, n)
        {
            try {return r(t, N, n)} catch (e) {return Le(e) ? e : new vu(e)}
        }), zi            = Ee(function (t, n)
        {
            return u(ln(n, 1), function (n)
            {
                n = ee(n), t[n] = ti(t[n], t)
            }), t
        }), Di            = xr(), $i = xr(true), Fi = Ee(function (t, n)
        {
            return function (r)
            {
                return jn(r, t, n);
            }
        }), Ni            = Ee(function (t, n)
        {
            return function (r)
            {
                return jn(t, r, n)
            }
        }), Pi            = Ar(a), Zi = Ar(o), Ti = Ar(p), qi = Er(), Vi = Er(true), Ki = wr(function (t, n)
        {
            return t + n
        }), Gi            = Rr("ceil"), Ji = wr(function (t, n)
        {
            return t / n
        }), Yi            = Rr("floor"), Hi = wr(function (t, n)
        {
            return t * n
        }), Qi            = Rr("round"), Xi = wr(function (t, n)
        {
            return t - n
        });
        return jt.after = function (t, n)
        {
            if (typeof n != "function")throw new yu("Expected a function");
            return t = Ke(t), function ()
            {
                return 1 > --t ? n.apply(this, arguments) : void 0
            }
        }, jt.ary = je, jt.assign = _i, jt.assignIn = vi, jt.assignInWith = gi,
            jt.assignWith = di, jt.at = yi, jt.before = me, jt.bind = ti, jt.bindAll = zi, jt.bindKey = ni, jt.castArray = Ie, jt.chain = _e, jt.chunk = function (t, n, r)
        {
            if (n = (r ? Jr(t, n, r) : n === N) ? 1 : Ku(Ke(n),
                    0), r = t ? t.length : 0, !r || 1 > n)return [];
            for (var e = 0, u = 0, o = Array(Nu(r / n)); r > e;)o[u++] = Pn(t, e,
                e += n);
            return o
        }, jt.compact = function (t)
        {
            for (var n = -1, r = t ? t.length : 0, e = 0, u = []; ++n < r;) {
                var o = t[n];
                o && (u[e++] = o)
            }
            return u
        }, jt.concat = function ()
        {
            var t = arguments.length, n = Ie(arguments[0]);
            if (2 > t)return t ? cr(n) : [];
            for (var r = Array(t - 1); t--;)r[t - 1] = arguments[t];
            for (var t = ln(r,
                1), r  = -1, e = n.length, u = -1, o = t.length, i = Array(e + o); ++r < e;)i[r] = n[r];
            for (; ++u < o;)i[r++] = t[u];
            return i
        }, jt.cond = function (t)
        {
            var n = t ? t.length : 0, e = Mr();
            return t = n ? a(t, function (t)
            {
                if ("function" != typeof t[1])throw new yu("Expected a function");
                return [e(t[0]), t[1]]
            }) : [], Ee(function (e)
            {
                for (var u = -1; ++u < n;) {
                    var o = t[u];
                    if (r(o[0], this, e))return r(o[1], this, e)
                }
            })
        }, jt.conforms = function (t)
        {
            return rn(nn(t, true))
        }, jt.constant = cu, jt.countBy = Ko, jt.create = function (t, n)
        {
            var r = en(t);
            return n ? Qt(r, n) : r
        }, jt.curry = we,
            jt.curryRight = Ae, jt.debounce = Oe, jt.defaults = bi, jt.defaultsDeep = xi, jt.defer = ri, jt.delay = ei, jt.difference = So, jt.differenceBy = Ro, jt.differenceWith = Wo, jt.drop = ie, jt.dropRight = fe, jt.dropRightWhile = function (t, n)
        {
            return t && t.length ? Yn(t, Mr(n, 3), true, true) : []
        }, jt.dropWhile = function (t, n)
        {
            return t && t.length ? Yn(t, Mr(n, 3), true) : []
        }, jt.fill = function (t, n, r, e)
        {
            var u = t ? t.length : 0;
            if (!u)return [];
            for (r && typeof r != "number" && Jr(t, n,
                r) && (r = 0, e = u), u = t.length, r = Ke(r), 0 > r && (r = -r > u ? 0 : u + r), e = e === N || e > u ? u : Ke(e), 0 > e && (e += u), e = r > e ? 0 : Ge(e); e > r;)t[r++] = n;
            return t
        }, jt.filter = function (t, n)
        {
            return (li(t) ? i : an)(t, Mr(n, 3))
        }, jt.flatMap = function (t, n)
        {
            return ln(be(t, n), 1)
        }, jt.flatMapDeep = function (t, n)
        {
            return ln(be(t, n), P)
        }, jt.flatMapDepth = function (t, n, r)
        {
            return r = r === N ? 1 : Ke(r), ln(be(t, n), r)
        }, jt.flatten = function (t)
        {
            return t && t.length ? ln(t, 1) : []
        }, jt.flattenDeep = function (t)
        {
            return t && t.length ? ln(t, P) : []
        }, jt.flattenDepth = function (t, n)
        {
            return t && t.length ? (n = n === N ? 1 : Ke(n), ln(t, n)) : []
        }, jt.flip = function (t)
        {
            return Wr(t, 512)
        }, jt.flow = Di, jt.flowRight = $i, jt.fromPairs = function (t)
        {
            for (var n = -1, r = t ? t.length : 0, e = {}; ++n < r;) {
                var u = t[n];
                e[u[0]] = u[1]
            }
            return e
        }, jt.functions = function (t)
        {
            return null == t ? [] : pn(t, tu(t))
        }, jt.functionsIn = function (t)
        {
            return null == t ? [] : pn(t, nu(t))
        }, jt.groupBy = Go, jt.initial = function (t)
        {
            return fe(t, 1)
        }, jt.intersection = Bo, jt.intersectionBy = Lo, jt.intersectionWith = Co, jt.invert = ji, jt.invertBy = mi, jt.invokeMap = Jo, jt.iteratee = lu, jt.keyBy = Yo, jt.keys = tu, jt.keysIn = nu, jt.map = be, jt.mapKeys = function (t, n)
        {
            var r = {};
            return n = Mr(n, 3), sn(t, function (t, e, u)
            {
                r[n(t, e, u)] = t
            }), r
        },
            jt.mapValues = function (t, n)
            {
                var r = {};
                return n = Mr(n, 3), sn(t, function (t, e, u)
                {
                    r[e] = n(t, e, u)
                }), r
            }, jt.matches = function (t)
        {
            return In(nn(t, true))
        }, jt.matchesProperty = function (t, n)
        {
            return Sn(t, nn(n, true))
        }, jt.memoize = ke, jt.merge = Ai, jt.mergeWith = Oi, jt.method = Fi, jt.methodOf = Ni, jt.mixin = su, jt.negate = function (t)
        {
            if (typeof t != "function")throw new yu("Expected a function");
            return function ()
            {
                return !t.apply(this, arguments)
            }
        }, jt.nthArg = function (t)
        {
            return t = Ke(t), Ee(function (n)
            {
                return Wn(n, t)
            })
        }, jt.omit = ki, jt.omitBy = function (t, n)
        {
            return n = Mr(n), Cn(t, function (t, r)
            {
                return !n(t, r)
            })
        }, jt.once = function (t)
        {
            return me(2, t)
        }, jt.orderBy = function (t, n, r, e)
        {
            return null == t ? [] : (li(n) || (n = null == n ? [] : [n]), r = e ? N : r, li(r) || (r = null == r ? [] : [r]), Bn(t,
                n, r))
        }, jt.over = Pi, jt.overArgs = ui, jt.overEvery = Zi, jt.overSome = Ti, jt.partial = oi, jt.partialRight = ii, jt.partition = Ho, jt.pick = Ei, jt.pickBy = function (t, n)
        {
            return null == t ? {} : Cn(t, Mr(n))
        }, jt.property = pu, jt.propertyOf = function (t)
        {
            return function (n)
            {
                return null == t ? N : _n(t, n)
            }
        }, jt.pull = Mo, jt.pullAll = le, jt.pullAllBy = function (t, n, r)
        {
            return t && t.length && n && n.length ? zn(t, n, Mr(r)) : t
        }, jt.pullAllWith = function (t, n, r)
        {
            return t && t.length && n && n.length ? zn(t, n, N, r) : t
        }, jt.pullAt = Uo, jt.range = qi, jt.rangeRight = Vi, jt.rearg = fi, jt.reject = function (t, n)
        {
            var r = li(t) ? i : an;
            return n = Mr(n, 3), r(t, function (t, r, e)
            {
                return !n(t, r, e)
            })
        }, jt.remove = function (t, n)
        {
            var r = [];
            if (!t || !t.length)return r;
            var e = -1, u = [], o = t.length;
            for (n = Mr(n, 3); ++e < o;) {
                var i = t[e];
                n(i, e, t) && (r.push(i), u.push(e))
            }
            return Dn(t, u), r
        }, jt.rest = Ee, jt.reverse = se,jt.sampleSize = xe,jt.set = function (t, n, r)
        {
            return null == t ? t : Nn(t, n, r)
        },jt.setWith = function (t, n, r, e)
        {
            return e = typeof e == "function" ? e : N, null == t ? t : Nn(t, n, r, e)
        },jt.shuffle = function (t)
        {
            return xe(t, 4294967295)
        },jt.slice = function (t, n, r)
        {
            var e = t ? t.length : 0;
            return e ? (r && typeof r != "number" && Jr(t, n,
                r) ? (n = 0, r = e) : (n = null == n ? 0 : Ke(n), r = r === N ? e : Ke(r)), Pn(t,
                n, r)) : []
        },jt.sortBy = Qo,jt.sortedUniq = function (t)
        {
            return t && t.length ? Vn(t) : []
        },jt.sortedUniqBy = function (t, n)
        {
            return t && t.length ? Vn(t, Mr(n)) : []
        },jt.split = function (t, n, r)
        {
            return r && typeof r != "number" && Jr(t, n, r) && (n = r = N),
                r = r === N ? 4294967295 : r >>> 0, r ? (t = He(t)) && (typeof n == "string" || null != n && !Pe(n)) && (n = Gn(n), "" == n && It.test(t)) ? rr(t.match(kt),
                0, r) : Xu.call(t, n, r) : []
        },jt.spread = function (t, n)
        {
            if (typeof t != "function")throw new yu("Expected a function");
            return n = n === N ? 0 : Ku(Ke(n), 0), Ee(function (e)
            {
                var u = e[n];
                return e = rr(e, 0, n), u && l(e, u), r(t, this, e)
            })
        },jt.tail = function (t)
        {
            return ie(t, 1)
        },jt.take = function (t, n, r)
        {
            return t && t.length ? (n = r || n === N ? 1 : Ke(n), Pn(t, 0,
                0 > n ? 0 : n)) : []
        },jt.takeRight = function (t, n, r)
        {
            var e = t ? t.length : 0;
            return e ? (n = r || n === N ? 1 : Ke(n),
                n = e - n, Pn(t, 0 > n ? 0 : n, e)) : []
        },jt.takeRightWhile = function (t, n)
        {
            return t && t.length ? Yn(t, Mr(n, 3), false, true) : []
        },jt.takeWhile = function (t, n)
        {
            return t && t.length ? Yn(t, Mr(n, 3)) : []
        },jt.tap = function (t, n)
        {
            return n(t), t
        },jt.throttle = function (t, n, r)
        {
            var e = true, u = true;
            if (typeof t != "function")throw new yu("Expected a function");
            return ze(r) && (e = "leading"in r ? !!r.leading : e, u = "trailing"in r ? !!r.trailing : u), Oe(t,
                n, {
                    leading : e,
                    maxWait : n,
                    trailing: u
                })
        },jt.thru = ve,jt.toArray = Ve,jt.toPairs = ru,jt.toPairsIn = eu,jt.toPath = function (t)
        {
            return li(t) ? a(t, ee) : Te(t) ? [t] : cr(Io(t))
        },jt.toPlainObject = Ye,jt.transform = function (t, n, r)
        {
            var e = li(t) || qe(t);
            if (n = Mr(n, 4), null == r)if (e || ze(t)) {
                var o = t.constructor;
                r = e ? li(t) ? new o : [] : Ce(o) ? en(Zu(Object(t))) : {}
            } else r = {};
            return (e ? u : sn)(t, function (t, e, u)
            {
                return n(r, t, e, u)
            }), r
        },jt.unary = function (t)
        {
            return je(t, 1)
        },jt.union = zo,jt.unionBy = Do,jt.unionWith = $o,jt.uniq = function (t)
        {
            return t && t.length ? Jn(t) : []
        },jt.uniqBy = function (t, n)
        {
            return t && t.length ? Jn(t, Mr(n)) : []
        },jt.uniqWith = function (t, n)
        {
            return t && t.length ? Jn(t, N, n) : [];
        },jt.unset = function (t, n)
        {
            var r;
            if (null == t)r = true; else {
                r = t;
                var e = n, e = Yr(e, r) ? [e] : nr(e);
                r = re(r, e), e = ee(ae(e)), r = !(null != r && dn(r,
                        e)) || delete r[e]
            }
            return r
        },jt.unzip = he,jt.unzipWith = pe,jt.update = function (t, n, r)
        {
            return null == t ? t : Nn(t, n,
                (typeof r == "function" ? r : au)(_n(t, n)), void 0)
        },jt.updateWith = function (t, n, r, e)
        {
            return e = typeof e == "function" ? e : N, null != t && (t = Nn(t, n,
                (typeof r == "function" ? r : au)(_n(t, n)), e)), t
        },jt.values = uu,jt.valuesIn = function (t)
        {
            return null == t ? [] : O(t, nu(t))
        },jt.without = Fo,jt.words = fu,jt.wrap = function (t, n)
        {
            return n = null == n ? au : n, oi(n, t)
        },jt.xor = No,jt.xorBy = Po,jt.xorWith = Zo,jt.zip = To,jt.zipObject = function (t, n)
        {
            return Xn(t || [], n || [], Yt)
        },jt.zipObjectDeep = function (t, n)
        {
            return Xn(t || [], n || [], Nn)
        },jt.zipWith = qo,jt.entries = ru,jt.entriesIn = eu,jt.extend = vi,jt.extendWith = gi,su(jt,
            jt),jt.add = Ki,jt.attempt = Ui,jt.camelCase = Ii,jt.capitalize = ou,jt.ceil = Gi,jt.clamp = function (t, n, r)
        {
            return r === N && (r = n, n = N), r !== N && (r = Je(r), r = r === r ? r : 0), n !== N && (n = Je(n), n = n === n ? n : 0), tn(Je(t),
                n, r)
        },jt.clone = function (t)
        {
            return nn(t, false, true);
        },jt.cloneDeep = function (t)
        {
            return nn(t, true, true)
        },jt.cloneDeepWith = function (t, n)
        {
            return nn(t, true, true, n)
        },jt.cloneWith = function (t, n)
        {
            return nn(t, false, true, n)
        },jt.deburr = iu,jt.divide = Ji,jt.endsWith = function (t, n, r)
        {
            t = He(t), n = Gn(n);
            var e = t.length;
            return r = r === N ? e : tn(Ke(r), 0,
                e), r -= n.length, r >= 0 && t.indexOf(n, r) == r
        },jt.eq = Se,jt.escape = function (t)
        {
            return (t = He(t)) && Y.test(t) ? t.replace(G, R) : t
        },jt.escapeRegExp = function (t)
        {
            return (t = He(t)) && ut.test(t) ? t.replace(et, "\\$&") : t
        },jt.every = function (t, n, r)
        {
            var e = li(t) ? o : fn;
            return r && Jr(t, n, r) && (n = N),
                e(t, Mr(n, 3))
        },jt.find = function (t, n)
        {
            if (n = Mr(n, 3), li(t)) {
                var r = v(t, n);
                return r > -1 ? t[r] : N
            }
            return _(t, n, yo)
        },jt.findIndex = function (t, n)
        {
            return t && t.length ? v(t, Mr(n, 3)) : -1
        },jt.findKey = function (t, n)
        {
            return _(t, Mr(n, 3), sn, true)
        },jt.findLast = function (t, n)
        {
            if (n = Mr(n, 3), li(t)) {
                var r = v(t, n, true);
                return r > -1 ? t[r] : N
            }
            return _(t, n, bo)
        },jt.findLastIndex = function (t, n)
        {
            return t && t.length ? v(t, Mr(n, 3), true) : -1
        },jt.findLastKey = function (t, n)
        {
            return _(t, Mr(n, 3), hn, true)
        },jt.floor = Yi,jt.forEach = de,jt.forEachRight = ye,jt.forIn = function (t, n)
        {
            return null == t ? t : xo(t, Mr(n), nu)
        },jt.forInRight = function (t, n)
        {
            return null == t ? t : jo(t, Mr(n), nu)
        },jt.forOwn = function (t, n)
        {
            return t && sn(t, Mr(n))
        },jt.forOwnRight = function (t, n)
        {
            return t && hn(t, Mr(n))
        },jt.get = Qe,jt.gt = ci,jt.gte = ai,jt.has = function (t, n)
        {
            return null != t && Nr(t, n, dn)
        },jt.hasIn = Xe,jt.head = ce,jt.identity = au,jt.includes = function (t, n, r, e)
        {
            return t = We(t) ? t : uu(t), r = r && !e ? Ke(r) : 0, e = t.length, 0 > r && (r = Ku(e + r,
                0)), Ze(t) ? e >= r && -1 < t.indexOf(n, r) : !!e && -1 < g(t, n, r)
        },jt.indexOf = function (t, n, r)
        {
            var e = t ? t.length : 0;
            return e ? (r = Ke(r), 0 > r && (r = Ku(e + r, 0)), g(t, n, r)) : -1
        },jt.inRange = function (t, n, r)
        {
            return n = Je(n) || 0, r === N ? (r = n, n = 0) : r = Je(r) || 0, t = Je(t), t >= Gu(n,
                r) && t < Ku(n, r)
        },jt.invoke = wi,jt.isArguments = Re,jt.isArray = li,jt.isArrayBuffer = function (t)
        {
            return De(t) && "[object ArrayBuffer]" == ku.call(t)
        },jt.isArrayLike = We,jt.isArrayLikeObject = Be,jt.isBoolean = function (t)
        {
            return true === t || false === t || De(t) && "[object Boolean]" == ku.call(t)
        },jt.isBuffer = si,jt.isDate = function (t)
        {
            return De(t) && "[object Date]" == ku.call(t)
        },jt.isElement = function (t)
        {
            return !!t && 1 === t.nodeType && De(t) && !Ne(t)
        },jt.isEmpty = function (t)
        {
            if (We(t) && (li(t) || Ze(t) || Ce(t.splice) || Re(t) || si(t)))return !t.length;
            if (De(t)) {
                var n = Fr(t);
                if ("[object Map]" == n || "[object Set]" == n)return !t.size
            }
            for (var r in t)if (wu.call(t, r))return false;
            return !(fo && tu(t).length)
        },jt.isEqual = function (t, n)
        {
            return mn(t, n)
        },jt.isEqualWith = function (t, n, r)
        {
            var e = (r = typeof r == "function" ? r : N) ? r(t, n) : N;
            return e === N ? mn(t, n, r) : !!e
        },jt.isError = Le,jt.isFinite = function (t)
        {
            return typeof t == "number" && Tu(t)
        },jt.isFunction = Ce,
            jt.isInteger = Me,jt.isLength = Ue,jt.isMap = function (t)
        {
            return De(t) && "[object Map]" == Fr(t)
        },jt.isMatch = function (t, n)
        {
            return t === n || wn(t, n, Ur(n))
        },jt.isMatchWith = function (t, n, r)
        {
            return r = typeof r == "function" ? r : N, wn(t, n, Ur(n), r)
        },jt.isNaN = function (t)
        {
            return Fe(t) && t != +t
        },jt.isNative = $e,jt.isNil = function (t)
        {
            return null == t
        },jt.isNull = function (t)
        {
            return null === t
        },jt.isNumber = Fe,jt.isObject = ze,jt.isObjectLike = De,jt.isPlainObject = Ne,jt.isRegExp = Pe,jt.isSafeInteger = function (t)
        {
            return Me(t) && t >= -9007199254740991 && 9007199254740991 >= t;
        },jt.isSet = function (t)
        {
            return De(t) && "[object Set]" == Fr(t)
        },jt.isString = Ze,jt.isSymbol = Te,jt.isTypedArray = qe,jt.isUndefined = function (t)
        {
            return t === N
        },jt.isWeakMap = function (t)
        {
            return De(t) && "[object WeakMap]" == Fr(t)
        },jt.isWeakSet = function (t)
        {
            return De(t) && "[object WeakSet]" == ku.call(t)
        },jt.join = function (t, n)
        {
            return t ? qu.call(t, n) : ""
        },jt.kebabCase = Si,jt.last = ae,jt.lastIndexOf = function (t, n, r)
        {
            var e = t ? t.length : 0;
            if (!e)return -1;
            var u = e;
            if (r !== N && (u = Ke(r), u = (0 > u ? Ku(e + u, 0) : Gu(u,
                        e - 1)) + 1), n !== n)return B(t, u, true);
            for (; u--;)if (t[u] === n)return u;
            return -1
        },jt.lowerCase = Ri,jt.lowerFirst = Wi,jt.lt = hi,jt.lte = pi,jt.max = function (t)
        {
            return t && t.length ? cn(t, au, gn) : N
        },jt.maxBy = function (t, n)
        {
            return t && t.length ? cn(t, Mr(n), gn) : N
        },jt.mean = function (t)
        {
            return y(t, au)
        },jt.meanBy = function (t, n)
        {
            return y(t, Mr(n))
        },jt.min = function (t)
        {
            return t && t.length ? cn(t, au, kn) : N
        },jt.minBy = function (t, n)
        {
            return t && t.length ? cn(t, Mr(n), kn) : N
        },jt.multiply = Hi,jt.nth = function (t, n)
        {
            return t && t.length ? Wn(t, Ke(n)) : N
        },jt.noConflict = function ()
        {
            return Vt._ === this && (Vt._ = Eu),
                this
        },jt.noop = hu,jt.now = Xo,jt.pad = function (t, n, r)
        {
            t = He(t);
            var e = (n = Ke(n)) ? D(t) : 0;
            return !n || e >= n ? t : (n = (n - e) / 2, Or(Pu(n), r) + t + Or(Nu(n),
                r))
        },jt.padEnd = function (t, n, r)
        {
            t = He(t);
            var e = (n = Ke(n)) ? D(t) : 0;
            return n && n > e ? t + Or(n - e, r) : t
        },jt.padStart = function (t, n, r)
        {
            t = He(t);
            var e = (n = Ke(n)) ? D(t) : 0;
            return n && n > e ? Or(n - e, r) + t : t
        },jt.parseInt = function (t, n, r)
        {
            return r || null == n ? n = 0 : n && (n = +n), t = He(t).replace(ot,
                ""), Ju(t, n || (ht.test(t) ? 16 : 10))
        },jt.random = function (t, n, r)
        {
            if (r && typeof r != "boolean" && Jr(t, n,
                    r) && (n = r = N), r === N && (typeof n == "boolean" ? (r = n,
                    n = N) : typeof t == "boolean" && (r = t, t = N)), t === N && n === N ? (t = 0, n = 1) : (t = Je(t) || 0, n === N ? (n = t, t = 0) : n = Je(n) || 0), t > n) {
                var e = t;
                t = n, n = e
            }
            return r || t % 1 || n % 1 ? (r = Yu(), Gu(t + r * (n - t + Dt("1e-" + ((r + "").length - 1))),
                n)) : $n(t, n)
        },jt.reduce = function (t, n, r)
        {
            var e = li(t) ? s : b, u = 3 > arguments.length;
            return e(t, Mr(n, 4), r, u, yo)
        },jt.reduceRight = function (t, n, r)
        {
            var e = li(t) ? h : b, u = 3 > arguments.length;
            return e(t, Mr(n, 4), r, u, bo)
        },jt.repeat = function (t, n, r)
        {
            return n = (r ? Jr(t, n, r) : n === N) ? 1 : Ke(n), Fn(He(t), n)
        },jt.replace = function ()
        {
            var t = arguments, n = He(t[0]);
            return 3 > t.length ? n : Hu.call(n, t[1], t[2])
        },jt.result = function (t, n, r)
        {
            n = Yr(n, t) ? [n] : nr(n);
            var e = -1, u = n.length;
            for (u || (t = N, u = 1); ++e < u;) {
                var o = null == t ? N : t[ee(n[e])];
                o === N && (e = u, o = r), t = Ce(o) ? o.call(t) : o
            }
            return t
        },jt.round = Qi,jt.runInContext = F,jt.sample = function (t)
        {
            t = We(t) ? t : uu(t);
            var n = t.length;
            return n > 0 ? t[$n(0, n - 1)] : N
        },jt.size = function (t)
        {
            if (null == t)return 0;
            if (We(t)) {
                var n = t.length;
                return n && Ze(t) ? D(t) : n
            }
            return De(t) && (n = Fr(t), "[object Map]" == n || "[object Set]" == n) ? t.size : tu(t).length
        },jt.snakeCase = Bi,
            jt.some = function (t, n, r)
            {
                var e = li(t) ? p : Zn;
                return r && Jr(t, n, r) && (n = N), e(t, Mr(n, 3))
            },jt.sortedIndex = function (t, n)
        {
            return Tn(t, n)
        },jt.sortedIndexBy = function (t, n, r)
        {
            return qn(t, n, Mr(r))
        },jt.sortedIndexOf = function (t, n)
        {
            var r = t ? t.length : 0;
            if (r) {
                var e = Tn(t, n);
                if (r > e && Se(t[e], n))return e
            }
            return -1
        },jt.sortedLastIndex = function (t, n)
        {
            return Tn(t, n, true)
        },jt.sortedLastIndexBy = function (t, n, r)
        {
            return qn(t, n, Mr(r), true)
        },jt.sortedLastIndexOf = function (t, n)
        {
            if (t && t.length) {
                var r = Tn(t, n, true) - 1;
                if (Se(t[r], n))return r
            }
            return -1;
        },jt.startCase = Li,jt.startsWith = function (t, n, r)
        {
            return t = He(t), r = tn(Ke(r), 0, t.length), t.lastIndexOf(Gn(n),
                r) == r
        },jt.subtract = Xi,jt.sum = function (t)
        {
            return t && t.length ? j(t, au) : 0
        },jt.sumBy = function (t, n)
        {
            return t && t.length ? j(t, Mr(n)) : 0
        },jt.template = function (t, n, r)
        {
            var e = jt.templateSettings;
            r && Jr(t, n, r) && (n = N), t = He(t), n = gi({}, n, e, Gt), r = gi({},
                n.imports, e.imports, Gt);
            var u, o, i = tu(r), f = O(r, i), c = 0;
            r = n.interpolate || bt;
            var a = "__p+='";
            r = du((n.escape || bt).source + "|" + r.source + "|" + (r === X ? lt : bt).source + "|" + (n.evaluate || bt).source + "|$",
                "g");
            var l = "sourceURL"in n ? "//# sourceURL=" + n.sourceURL + "\n" : "";
            if (t.replace(r, function (n, r, e, i, f, l)
                {
                    return e || (e = i), a += t.slice(c, l).replace(xt,
                        W), r && (u = true, a += "'+__e(" + r + ")+'"), f && (o = true, a += "';" + f + ";\n__p+='"), e && (a += "'+((__t=(" + e + "))==null?'':__t)+'"), c = l + n.length, n
                }), a += "';", (n = n.variable) || (a = "with(obj){" + a + "}"), a = (o ? a.replace(T,
                    "") : a).replace(q, "$1").replace(V,
                    "$1;"), a = "function(" + (n || "obj") + "){" + (n ? "" : "obj||(obj={});") + "var __t,__p=''" + (u ? ",__e=_.escape" : "") + (o ? ",__j=Array.prototype.join;function print(){__p+=__j.call(arguments,'')}" : ";") + a + "return __p}",
                    n = Ui(function ()
                    {
                        return Function(i, l + "return " + a).apply(N, f)
                    }), n.source = a, Le(n))throw n;
            return n
        },jt.times = function (t, n)
        {
            if (t = Ke(t), 1 > t || t > 9007199254740991)return [];
            var r = 4294967295, e = Gu(t, 4294967295);
            for (n = Mr(n), t -= 4294967295, e = m(e, n); ++r < t;)n(r);
            return e
        },jt.toInteger = Ke,jt.toLength = Ge,jt.toLower = function (t)
        {
            return He(t).toLowerCase()
        },jt.toNumber = Je,jt.toSafeInteger = function (t)
        {
            return tn(Ke(t), -9007199254740991, 9007199254740991)
        },jt.toString = He,jt.toUpper = function (t)
        {
            return He(t).toUpperCase()
        },
            jt.trim = function (t, n, r)
            {
                return (t = He(t)) && (r || n === N) ? t.replace(ot,
                    "") : t && (n = Gn(n)) ? (t = t.match(kt), n = n.match(kt), rr(t,
                    k(t, n), E(t, n) + 1).join("")) : t
            },jt.trimEnd = function (t, n, r)
        {
            return (t = He(t)) && (r || n === N) ? t.replace(ft,
                "") : t && (n = Gn(n)) ? (t = t.match(kt), n = E(t,
                    n.match(kt)) + 1, rr(t, 0, n).join("")) : t
        },jt.trimStart = function (t, n, r)
        {
            return (t = He(t)) && (r || n === N) ? t.replace(it,
                "") : t && (n = Gn(n)) ? (t = t.match(kt), n = k(t,
                n.match(kt)), rr(t, n).join("")) : t
        },jt.truncate = function (t, n)
        {
            var r = 30, e = "...";
            if (ze(n))var u = "separator"in n ? n.separator : u, r = "length"in n ? Ke(n.length) : r, e = "omission"in n ? Gn(n.omission) : e;
            t = He(t);
            var o = t.length;
            if (It.test(t))var i = t.match(kt), o = i.length;
            if (r >= o)return t;
            if (o = r - D(e), 1 > o)return e;
            if (r = i ? rr(i, 0, o).join("") : t.slice(0, o), u === N)return r + e;
            if (i && (o += r.length - o), Pe(u)) {
                if (t.slice(o).search(u)) {
                    var f = r;
                    for (u.global || (u = du(u.source,
                        He(st.exec(u)) + "g")), u.lastIndex = 0; i = u.exec(f);)var c = i.index;
                    r = r.slice(0, c === N ? o : c)
                }
            } else t.indexOf(Gn(u),
                o) != o && (u = r.lastIndexOf(u), u > -1 && (r = r.slice(0, u)));
            return r + e
        },jt.unescape = function (t)
        {
            return (t = He(t)) && J.test(t) ? t.replace(K, $) : t
        },jt.uniqueId = function (t)
        {
            var n = ++Au;
            return He(t) + n
        },jt.upperCase = Ci,jt.upperFirst = Mi,jt.each = de,jt.eachRight = ye,jt.first = ce,su(jt,
            function ()
            {
                var t = {};
                return sn(jt, function (n, r)
                {
                    wu.call(jt.prototype, r) || (t[r] = n)
                }), t
            }(),
            {chain: false}),jt.VERSION = "4.11.2",u("bind bindKey curry curryRight partial partialRight".split(" "),
            function (t)
            {
                jt[t].placeholder = jt
            }),u(["drop", "take"], function (t, n)
        {
            Lt.prototype[t] = function (r)
            {
                var e = this.__filtered__;
                if (e && !n)return new Lt(this);
                r = r === N ? 1 : Ku(Ke(r), 0);
                var u = this.clone();
                return e ? u.__takeCount__ = Gu(r,
                    u.__takeCount__) : u.__views__.push({
                    size: Gu(r, 4294967295),
                    type: t + (0 > u.__dir__ ? "Right" : "")
                }), u
            }, Lt.prototype[t + "Right"] = function (n)
            {
                return this.reverse()[t](n).reverse()
            }
        }),u(["filter", "map", "takeWhile"], function (t, n)
        {
            var r = n + 1, e = 1 == r || 3 == r;
            Lt.prototype[t] = function (t)
            {
                var n = this.clone();
                return n.__iteratees__.push({
                    iteratee: Mr(t, 3),
                    type    : r
                }), n.__filtered__ = n.__filtered__ || e, n
            }
        }),u(["head", "last"], function (t, n)
        {
            var r = "take" + (n ? "Right" : "");
            Lt.prototype[t] = function ()
            {
                return this[r](1).value()[0]
            }
        }),u(["initial", "tail"], function (t, n)
        {
            var r = "drop" + (n ? "" : "Right");
            Lt.prototype[t] = function ()
            {
                return this.__filtered__ ? new Lt(this) : this[r](1)
            }
        }),Lt.prototype.compact = function ()
        {
            return this.filter(au)
        },Lt.prototype.find = function (t)
        {
            return this.filter(t).head()
        },Lt.prototype.findLast = function (t)
        {
            return this.reverse().find(t)
        },Lt.prototype.invokeMap = Ee(function (t, n)
        {
            return typeof t == "function" ? new Lt(this) : this.map(function (r)
            {
                return jn(r, t, n)
            })
        }),Lt.prototype.reject = function (t)
        {
            return t = Mr(t, 3), this.filter(function (n)
            {
                return !t(n)
            })
        },Lt.prototype.slice = function (t, n)
        {
            t = Ke(t);
            var r = this;
            return r.__filtered__ && (t > 0 || 0 > n) ? new Lt(r) : (0 > t ? r = r.takeRight(-t) : t && (r = r.drop(t)), n !== N && (n = Ke(n), r = 0 > n ? r.dropRight(-n) : r.take(n - t)), r)
        },Lt.prototype.takeRightWhile = function (t)
        {
            return this.reverse().takeWhile(t).reverse()
        },Lt.prototype.toArray = function ()
        {
            return this.take(4294967295)
        },sn(Lt.prototype, function (t, n)
        {
            var r = /^(?:filter|find|map|reject)|While$/.test(n), e = /^(?:head|last)$/.test(n), u = jt[e ? "take" + ("last" == n ? "Right" : "") : n], o = e || /^find/.test(n);
            u && (jt.prototype[n] = function ()
            {
                function n(t)
                {
                    return t = u.apply(jt, l([t], f)), e && h ? t[0] : t
                }
                
                var i = this.__wrapped__, f = e ? [1] : arguments, c = i instanceof Lt, a = f[0], s = c || li(i);
                s && r && typeof a == "function" && 1 != a.length && (c = s = false);
                var h = this.__chain__, p = !!this.__actions__.length, a = o && !h, c = c && !p;
                return !o && s ? (i = c ? i : new Lt(this), i = t.apply(i,
                    f), i.__actions__.push({
                    func   : ve,
                    args   : [n],
                    thisArg: N
                }), new wt(i, h)) : a && c ? t.apply(this,
                    f) : (i = this.thru(n), a ? e ? i.value()[0] : i.value() : i)
            })
        }),u("pop push shift sort splice unshift".split(" "), function (t)
        {
            var n = bu[t], r = /^(?:push|sort|unshift)$/.test(t) ? "tap" : "thru", e = /^(?:pop|shift)$/.test(t);
            jt.prototype[t] = function ()
            {
                var t = arguments;
                if (e && !this.__chain__) {
                    var u = this.value();
                    return n.apply(li(u) ? u : [], t)
                }
                return this[r](function (r)
                {
                    return n.apply(li(r) ? r : [], t)
                })
            }
        }),sn(Lt.prototype, function (t, n)
        {
            var r = jt[n];
            if (r) {
                var e = r.name + "";
                (co[e] || (co[e] = [])).push({
                    name: n,
                    func: r
                })
            }
        }),co[jr(N, 2).name] = [
            {
                name: "wrapper",
                func: N
            }
        ],Lt.prototype.clone = function ()
        {
            var t = new Lt(this.__wrapped__);
            return t.__actions__ = cr(this.__actions__), t.__dir__ = this.__dir__, t.__filtered__ = this.__filtered__, t.__iteratees__ = cr(this.__iteratees__),
                t.__takeCount__ = this.__takeCount__, t.__views__ = cr(this.__views__), t
        },Lt.prototype.reverse = function ()
        {
            if (this.__filtered__) {
                var t = new Lt(this);
                t.__dir__ = -1, t.__filtered__ = true
            } else t = this.clone(), t.__dir__ *= -1;
            return t
        },Lt.prototype.value = function ()
        {
            var t, n = this.__wrapped__.value(), r = this.__dir__, e = li(n), u = 0 > r, o = e ? n.length : 0;
            t = o;
            for (var i = this.__views__, f = 0, c = -1, a = i.length; ++c < a;) {
                var l = i[c], s = l.size;
                switch (l.type) {
                    case"drop":
                        f += s;
                        break;
                    case"dropRight":
                        t -= s;
                        break;
                    case"take":
                        t = Gu(t, f + s);
                        break;
                    case"takeRight":
                        f = Ku(f, t - s)
                }
            }
            if (t = {
                    start: f,
                    end  : t
                }, i = t.start, f = t.end, t = f - i, u = u ? f : i - 1, i = this.__iteratees__, f = i.length, c = 0, a = Gu(t,
                    this.__takeCount__), !e || 200 > o || o == t && a == t)return Hn(n,
                this.__actions__);
            e = [];
            t:for (; t-- && a > c;) {
                for (u += r, o = -1, l = n[u]; ++o < f;) {
                    var h = i[o], s = h.type, h = (0, h.iteratee)(l);
                    if (2 == s)l = h; else if (!h) {
                        if (1 == s)continue t;
                        break t
                    }
                }
                e[c++] = l
            }
            return e
        },jt.prototype.at = Vo,jt.prototype.chain = function ()
        {
            return _e(this)
        },jt.prototype.commit = function ()
        {
            return new wt(this.value(), this.__chain__)
        },jt.prototype.next = function ()
        {
            this.__values__ === N && (this.__values__ = Ve(this.value()));
            var t = this.__index__ >= this.__values__.length, n = t ? N : this.__values__[this.__index__++];
            return {
                done : t,
                value: n
            }
        },jt.prototype.plant = function (t)
        {
            for (var n, r = this; r instanceof mt;) {
                var e = oe(r);
                e.__index__ = 0, e.__values__ = N, n ? u.__wrapped__ = e : n = e;
                var u = e, r = r.__wrapped__
            }
            return u.__wrapped__ = t, n
        },jt.prototype.reverse = function ()
        {
            var t = this.__wrapped__;
            return t instanceof Lt ? (this.__actions__.length && (t = new Lt(this)), t = t.reverse(), t.__actions__.push({
                func   : ve,
                args   : [se],
                thisArg: N
            }), new wt(t, this.__chain__)) : this.thru(se)
        },jt.prototype.toJSON = jt.prototype.valueOf = jt.prototype.value = function ()
        {
            return Hn(this.__wrapped__, this.__actions__)
        },Uu && (jt.prototype[Uu] = ge),jt
    }
    
    var N, P     = 1 / 0, Z = NaN, T = /\b__p\+='';/g, q = /\b(__p\+=)''\+/g, V = /(__e\(.*?\)|\b__t\))\+'';/g, K = /&(?:amp|lt|gt|quot|#39|#96);/g, G = /[&<>"'`]/g, J = RegExp(K.source), Y = RegExp(G.source), H = /<%-([\s\S]+?)%>/g, Q = /<%([\s\S]+?)%>/g, X = /<%=([\s\S]+?)%>/g, tt = /\.|\[(?:[^[\]]*|(["'])(?:(?!\1)[^\\]|\\.)*?\1)\]/, nt = /^\w*$/, rt = /[^.[\]]+|\[(?:(-?\d+(?:\.\d+)?)|(["'])((?:(?!\2)[^\\]|\\.)*?)\2)\]/g, et = /[\\^$.*+?()[\]{}|]/g, ut = RegExp(et.source), ot = /^\s+|\s+$/g, it = /^\s+/, ft = /\s+$/, ct = /[a-zA-Z0-9]+/g, at = /\\(\\)?/g, lt = /\$\{([^\\}]*(?:\\.[^\\}]*)*)\}/g, st = /\w*$/, ht = /^0x/i, pt = /^[-+]0x[0-9a-f]+$/i, _t = /^0b[01]+$/i, vt = /^\[object .+?Constructor\]$/, gt = /^0o[0-7]+$/i, dt = /^(?:0|[1-9]\d*)$/, yt = /[\xc0-\xd6\xd8-\xde\xdf-\xf6\xf8-\xff]/g, bt = /($^)/, xt = /['\n\r\u2028\u2029\\]/g, jt = "[\\ufe0e\\ufe0f]?(?:[\\u0300-\\u036f\\ufe20-\\ufe23\\u20d0-\\u20f0]|\\ud83c[\\udffb-\\udfff])?(?:\\u200d(?:[^\\ud800-\\udfff]|(?:\\ud83c[\\udde6-\\uddff]){2}|[\\ud800-\\udbff][\\udc00-\\udfff])[\\ufe0e\\ufe0f]?(?:[\\u0300-\\u036f\\ufe20-\\ufe23\\u20d0-\\u20f0]|\\ud83c[\\udffb-\\udfff])?)*", mt = "(?:[\\u2700-\\u27bf]|(?:\\ud83c[\\udde6-\\uddff]){2}|[\\ud800-\\udbff][\\udc00-\\udfff])" + jt, wt = "(?:[^\\ud800-\\udfff][\\u0300-\\u036f\\ufe20-\\ufe23\\u20d0-\\u20f0]?|[\\u0300-\\u036f\\ufe20-\\ufe23\\u20d0-\\u20f0]|(?:\\ud83c[\\udde6-\\uddff]){2}|[\\ud800-\\udbff][\\udc00-\\udfff]|[\\ud800-\\udfff])", At = RegExp("['\u2019]",
        "g"), Ot = RegExp("[\\u0300-\\u036f\\ufe20-\\ufe23\\u20d0-\\u20f0]",
        "g"), kt = RegExp("\\ud83c[\\udffb-\\udfff](?=\\ud83c[\\udffb-\\udfff])|" + wt + jt,
        "g"), Et = RegExp([
            "[A-Z\\xc0-\\xd6\\xd8-\\xde]?[a-z\\xdf-\\xf6\\xf8-\\xff]+(?:['\u2019](?:d|ll|m|re|s|t|ve))?(?=[\\xac\\xb1\\xd7\\xf7\\x00-\\x2f\\x3a-\\x40\\x5b-\\x60\\x7b-\\xbf\\u2000-\\u206f \\t\\x0b\\f\\xa0\\ufeff\\n\\r\\u2028\\u2029\\u1680\\u180e\\u2000\\u2001\\u2002\\u2003\\u2004\\u2005\\u2006\\u2007\\u2008\\u2009\\u200a\\u202f\\u205f\\u3000]|[A-Z\\xc0-\\xd6\\xd8-\\xde]|$)|(?:[A-Z\\xc0-\\xd6\\xd8-\\xde]|[^\\ud800-\\udfff\\xac\\xb1\\xd7\\xf7\\x00-\\x2f\\x3a-\\x40\\x5b-\\x60\\x7b-\\xbf\\u2000-\\u206f \\t\\x0b\\f\\xa0\\ufeff\\n\\r\\u2028\\u2029\\u1680\\u180e\\u2000\\u2001\\u2002\\u2003\\u2004\\u2005\\u2006\\u2007\\u2008\\u2009\\u200a\\u202f\\u205f\\u3000\\d+\\u2700-\\u27bfa-z\\xdf-\\xf6\\xf8-\\xffA-Z\\xc0-\\xd6\\xd8-\\xde])+(?:['\u2019](?:D|LL|M|RE|S|T|VE))?(?=[\\xac\\xb1\\xd7\\xf7\\x00-\\x2f\\x3a-\\x40\\x5b-\\x60\\x7b-\\xbf\\u2000-\\u206f \\t\\x0b\\f\\xa0\\ufeff\\n\\r\\u2028\\u2029\\u1680\\u180e\\u2000\\u2001\\u2002\\u2003\\u2004\\u2005\\u2006\\u2007\\u2008\\u2009\\u200a\\u202f\\u205f\\u3000]|[A-Z\\xc0-\\xd6\\xd8-\\xde](?:[a-z\\xdf-\\xf6\\xf8-\\xff]|[^\\ud800-\\udfff\\xac\\xb1\\xd7\\xf7\\x00-\\x2f\\x3a-\\x40\\x5b-\\x60\\x7b-\\xbf\\u2000-\\u206f \\t\\x0b\\f\\xa0\\ufeff\\n\\r\\u2028\\u2029\\u1680\\u180e\\u2000\\u2001\\u2002\\u2003\\u2004\\u2005\\u2006\\u2007\\u2008\\u2009\\u200a\\u202f\\u205f\\u3000\\d+\\u2700-\\u27bfa-z\\xdf-\\xf6\\xf8-\\xffA-Z\\xc0-\\xd6\\xd8-\\xde])|$)|[A-Z\\xc0-\\xd6\\xd8-\\xde]?(?:[a-z\\xdf-\\xf6\\xf8-\\xff]|[^\\ud800-\\udfff\\xac\\xb1\\xd7\\xf7\\x00-\\x2f\\x3a-\\x40\\x5b-\\x60\\x7b-\\xbf\\u2000-\\u206f \\t\\x0b\\f\\xa0\\ufeff\\n\\r\\u2028\\u2029\\u1680\\u180e\\u2000\\u2001\\u2002\\u2003\\u2004\\u2005\\u2006\\u2007\\u2008\\u2009\\u200a\\u202f\\u205f\\u3000\\d+\\u2700-\\u27bfa-z\\xdf-\\xf6\\xf8-\\xffA-Z\\xc0-\\xd6\\xd8-\\xde])+(?:['\u2019](?:d|ll|m|re|s|t|ve))?|[A-Z\\xc0-\\xd6\\xd8-\\xde]+(?:['\u2019](?:D|LL|M|RE|S|T|VE))?|\\d+",
            mt
        ].join("|"),
        "g"), It = RegExp("[\\u200d\\ud800-\\udfff\\u0300-\\u036f\\ufe20-\\ufe23\\u20d0-\\u20f0\\ufe0e\\ufe0f]"), St = /[a-z][A-Z]|[A-Z]{2,}[a-z]|[0-9][a-zA-Z]|[a-zA-Z][0-9]|[^a-zA-Z0-9 ]/, Rt = "Array Buffer DataView Date Error Float32Array Float64Array Function Int8Array Int16Array Int32Array Map Math Object Promise Reflect RegExp Set String Symbol TypeError Uint8Array Uint8ClampedArray Uint16Array Uint32Array WeakMap _ clearTimeout isFinite parseInt setTimeout".split(" "), Wt = {};
    Wt["[object Float32Array]"] = Wt["[object Float64Array]"] = Wt["[object Int8Array]"] = Wt["[object Int16Array]"] = Wt["[object Int32Array]"] = Wt["[object Uint8Array]"] = Wt["[object Uint8ClampedArray]"] = Wt["[object Uint16Array]"] = Wt["[object Uint32Array]"] = true, Wt["[object Arguments]"] = Wt["[object Array]"] = Wt["[object ArrayBuffer]"] = Wt["[object Boolean]"] = Wt["[object DataView]"] = Wt["[object Date]"] = Wt["[object Error]"] = Wt["[object Function]"] = Wt["[object Map]"] = Wt["[object Number]"] = Wt["[object Object]"] = Wt["[object RegExp]"] = Wt["[object Set]"] = Wt["[object String]"] = Wt["[object WeakMap]"] = false;
    var Bt = {};
    Bt["[object Arguments]"] = Bt["[object Array]"] = Bt["[object ArrayBuffer]"] = Bt["[object DataView]"] = Bt["[object Boolean]"] = Bt["[object Date]"] = Bt["[object Float32Array]"] = Bt["[object Float64Array]"] = Bt["[object Int8Array]"] = Bt["[object Int16Array]"] = Bt["[object Int32Array]"] = Bt["[object Map]"] = Bt["[object Number]"] = Bt["[object Object]"] = Bt["[object RegExp]"] = Bt["[object Set]"] = Bt["[object String]"] = Bt["[object Symbol]"] = Bt["[object Uint8Array]"] = Bt["[object Uint8ClampedArray]"] = Bt["[object Uint16Array]"] = Bt["[object Uint32Array]"] = true,
        Bt["[object Error]"] = Bt["[object Function]"] = Bt["[object WeakMap]"] = false;
    var Lt = {
        "\xc0": "A",
        "\xc1": "A",
        "\xc2": "A",
        "\xc3": "A",
        "\xc4": "A",
        "\xc5": "A",
        "\xe0": "a",
        "\xe1": "a",
        "\xe2": "a",
        "\xe3": "a",
        "\xe4": "a",
        "\xe5": "a",
        "\xc7": "C",
        "\xe7": "c",
        "\xd0": "D",
        "\xf0": "d",
        "\xc8": "E",
        "\xc9": "E",
        "\xca": "E",
        "\xcb": "E",
        "\xe8": "e",
        "\xe9": "e",
        "\xea": "e",
        "\xeb": "e",
        "\xcc": "I",
        "\xcd": "I",
        "\xce": "I",
        "\xcf": "I",
        "\xec": "i",
        "\xed": "i",
        "\xee": "i",
        "\xef": "i",
        "\xd1": "N",
        "\xf1": "n",
        "\xd2": "O",
        "\xd3": "O",
        "\xd4": "O",
        "\xd5": "O",
        "\xd6": "O",
        "\xd8": "O",
        "\xf2": "o",
        "\xf3": "o",
        "\xf4": "o",
        "\xf5": "o",
        "\xf6": "o",
        "\xf8": "o",
        "\xd9": "U",
        "\xda": "U",
        "\xdb": "U",
        "\xdc": "U",
        "\xf9": "u",
        "\xfa": "u",
        "\xfb": "u",
        "\xfc": "u",
        "\xdd": "Y",
        "\xfd": "y",
        "\xff": "y",
        "\xc6": "Ae",
        "\xe6": "ae",
        "\xde": "Th",
        "\xfe": "th",
        "\xdf": "ss"
    }, Ct  = {
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;",
        "`": "&#96;"
    }, Mt  = {
        "&amp;" : "&",
        "&lt;"  : "<",
        "&gt;"  : ">",
        "&quot;": '"',
        "&#39;" : "'",
        "&#96;" : "`"
    }, Ut  = {
        "function": true,
        object    : true
    }, zt  = {
        "\\"    : "\\",
        "'"     : "'",
        "\n"    : "n",
        "\r"    : "r",
        "\u2028": "u2028",
        "\u2029": "u2029"
    }, Dt  = parseFloat, $t = parseInt, Ft = Ut[typeof exports] && exports && !exports.nodeType ? exports : N, Nt = Ut[typeof module] && module && !module.nodeType ? module : N, Pt = Nt && Nt.exports === Ft ? Ft : N, Zt = I(Ut[typeof self] && self), Tt = I(Ut[typeof window] && window), qt = I(Ut[typeof this] && this), Vt = I(Ft && Nt && typeof global == "object" && global) || Tt !== (qt && qt.window) && Tt || Zt || qt || Function("return this")(), Kt = F();
    (Tt || Zt || {})._ = Kt, typeof define == "function" && typeof define.amd == "object" && define.amd ? define(function ()
    {
        return Kt
    }) : Ft && Nt ? (Pt && ((Nt.exports = Kt)._ = Kt),
        Ft._ = Kt) : Vt._ = Kt
}).call(this);

}).call(this)}).call(this,require('_process'),typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],require("timers").setImmediate,require("timers").clearImmediate,"/sources/js-source/libs/lodash.js","/sources/js-source/libs")

},{"_process":4,"buffer":2,"timers":5}]},{},[])
