// AFTER-WARNING: Variable 't' is never used
// PRIORITY: LOW
class A(val n: Int) {
    val <caret>foo: Boolean
        get() {
            return n > 1
        }
}

fun test() {
    val t = A(1).foo
}