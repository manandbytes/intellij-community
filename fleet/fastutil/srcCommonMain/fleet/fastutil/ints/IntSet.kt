// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package fleet.fastutil.ints

interface IntSet {
  val size: Int
  val values: IntIterator
  operator fun contains(value: Int): Boolean
}