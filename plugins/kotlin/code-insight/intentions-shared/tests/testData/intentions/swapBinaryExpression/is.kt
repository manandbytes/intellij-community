// IS_APPLICABLE: false
// ERROR: 'if' must have both main and 'else' branches if used as an expression
// ERROR: Expression 'if "test" is String' of type 'Unit' cannot be invoked as a function. The function 'invoke()' is not found
// K2_ERROR: 'if' must have both main and 'else' branches when used as an expression.
// K2_ERROR: Unresolved reference 'invoke'.
// AFTER-WARNING: Check for instance is always 'true'
fun <T> doSomething(a: T) {}

fun main() {
    if <caret>"test" is String {
        doSomething("Hello")
    }
}
