package util

import reactor.util.function.Tuple2

object Tuple2Extensions {
    // component1() and component2() implemented for destructuring of Tuple2 instances
    operator fun <T, U> Tuple2<T, U>.component1(): T = this.t1
    operator fun <T, U> Tuple2<T, U>.component2(): U = this.t2
}
