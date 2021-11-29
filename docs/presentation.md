---
title: Scala 3 - Programs as values
description: A short dive
theme: gaia
footer: 'Per Øyvind Kanestrøm'
paginate: true
---

# Workshop: Programs as values

- Langt fra ferdig 😅
- Vil gjerne ha tilbakemelding
- "Hva er verdien av å bruke `IO` og venner?"
- 2 & 2

---

# Outline

- Liten Scala 3 intro
- Liten intro til "programs as values"
- Vi prøver oss på workshoppen

---

# Om språket

- Scala 3, Dotty, dot calculus
- [Mye nytt](https://docs.scala-lang.org/scala3/new-in-scala3.html)
  - Type class derivation
    - `case class MyData(name: String) derives Eq`
  - Opaque types
  - Intersection & union types
  - Enums
  - Explicit nulls

---

# Om språket

- Opinionated
  - Fra `implicits` til `using` og `given`, `extension methods`, `Conversion`
- Plattformer: JVM, Scala.js, native
- Etc, for mye å gå igjennom!

---

# Verdier

```scala mdoc
val x = 1
List(x, x)
```

```scala mdoc:fail
val z: Int = "Sneak"
```

---
# Variabler

```scala mdoc:reset
var x = 1
x = 2
List(x, x)
```

---

# Funksjoner

```scala mdoc:reset
def hello(who: String) = s"Hello, $who!"

hello("fagruppa")

def helloV2(who: String)(likes: List[String]) =
  s"Hello, $who! You like: ${likes.mkString(",")}"

helloV2("fagruppa")(List("FP"))
```

---

## Extension methods

```scala mdoc:reset

extension (who: String)
  def hello = s"Hello, $who!"
  def helloV2(likes: String*) =
    s"Hello, $who! You like: ${likes.mkString(",")}"

"fagruppa".hello
"fagruppa".helloV2("fp", "🍺")
```


---

# Data

```scala mdoc:reset

case class MinData(navn: String, alder: Int)

val person1 = MinData("Olav", 50)
val person2 = MinData("Olav", 50)

person1 == person2
person1 == person2.copy(navn = "Ikke Olav")
```

---

# Classes

```scala mdoc:reset

class MinData(var navn: String, val alder: Int)

val person1 = MinData("Olav", 50)
val person2 = MinData("Olav", 50)

person1 == person1
person1.navn = "Ikke Olav"
person1 == person1
person1 == person2
```

---

# Syntax - for

```scala mdoc:reset
val a3: Option[String] = for {
  a11 <- Some("Hello")
  a22 <- None
} yield s"$a11, $a22"

val a4: Option[String] = for {
  a11 <- Some("Hello")
  a22 <- Some("World")
} yield s"$a11, $a22"

Some("Hello").flatMap(hello => Some("World")
             .map(world => s"$hello, $world"))
```

---

# Higher kinded types

- (husk `???`)

```scala
// Generics
def funk[A](in: A): A = ???

funk(1)
funk("Hey")
funk(new {})

// Higher kinded types
def funk2[F[_], A](in: F[A]): A = ???
funk2(List(1, 2, 3))
funk2(Some(1))
funk2(Some("Hey"))
```


---

![](https://glebbahmutov.com/blog/images/how-to-draw-fp-owl/how-to-draw-an-owl.jpg)

---

# FP

- [FP standardbibliotek - Cats](https://typelevel.org/cats/)
- [Effekthåndtering - Cats Effect](https://typelevel.org/cats-effect/)
- (plugins for å linte bruk av `var`, `throws`, etc)

---

# FP - Programs as values

- Composition
- Control flow

---

# FP - Hello world

```scala mdoc:reset
import cats.effect.*

object MyApp extends IOApp.Simple:
  def run = IO.println("Hello World")
```

---

# FP

```scala mdoc
import cats.effect.*

println("Impure")

IO(println("Pure"))
```

---

```scala mdoc
import cats.effect.unsafe.implicits.global

IO(println("Hello World")).unsafeRunSync()
```

---

# FP - Fra boller til burritos

```scala mdoc

// Fra tidligere - Med Option
val a4: Option[String] = for {
  a11 <- Some("Hello")
  a22 <- Some("World")
} yield s"$a11, $a22"
```


---

# FP - Fra boller til burritos

```scala mdoc
//               - Med IO
val a5: IO[String] = for {
  a11 <- IO("Hello")
  a22 <- IO("World")
} yield s"$a11, $a22"

import cats.effect.unsafe.implicits.global
a5.unsafeRunSync()
```

---

# Programmet er data

- Hva kan man gjøre med det?

...

- Alt
- Komposisjon blir til kontrol flyt
- Og språk er gode på endring av data


---

# Kontrollflyt - traverse

- `   F[A]   => (A   =>  G[B]) => G[F[B]])`
- `List[Int] => (Int => IO[String]) => IO[List[String]])`

```scala mdoc:silent
import cats.syntax.all.*

val mittProgram: IO[List[Int]] = List(1, 2, 3, 4)
  .traverse(r => IO(println(s"Tall: $r")).as(r))
```

```scala mdoc
mittProgram.unsafeRunSync()
```

---

# Kontrollflyt

```scala mdoc:silent
def doBusiness(in: Int) = IO {
  if (in == 2) then throw Exception("💥")
  else in * 10 }

val mittProgram2 = List(1, 2, 3, 4).traverse(r =>
  for {
    calculated <- doBusiness(r).handleError { case _ => -10 }
    _ <- IO(println(s"Tall: $calculated"))
  } yield calculated)
```

```scala mdoc
mittProgram2.unsafeRunSync()
```

---

# Kontrollflyt - parallellisere

```scala mdoc:silent
val mittProgram3 = List(1, 2, 3, 4).parTraverse(r =>
  for {
    calculated <- doBusiness(r).handleError { case _ => -10 }
    _ <- IO(println(s"Tall: $calculated"))
  } yield calculated)
```

```scala mdoc
mittProgram2.unsafeRunSync()
```

---

# Kontrollflyt - Deferred

```scala mdoc:silent
```

---

# Kontrollflyt - Resource

```scala mdoc:silent
```

---

# Workshop!

