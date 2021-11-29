package frontend.domain.model

import cats.*
import cats.effect.*
import cats.effect.std.*
import cats.effect.syntax.all.*
import cats.syntax.all.*
import fs2.Stream
import fs2.concurrent.*

import frontend.domain.model.BotSaying.*
import frontend.infrastructure.model.*

object PhoneBook:
  def create(persons: PersonBotActions*): Map[String, PersonBotActions] =
    persons.map(a => a.info.number -> a).toMap

  def simplePerson(mapper: PersonInfo => PersonInfo = identity) =
    new PersonBotActions {
      override def onPhoneCallStarted(doHangup: IO[Unit], mouth: QueueSink[IO, BotSaying]) = {
        mouth.tryOffer(Text("Hey")).void
      }

      def say(something: String, mouth: QueueSink[IO, BotSaying], doHangup: IO[Unit]) =
        mouth.tryOffer(Text("Hey")).void
    }.mapInfo(mapper)

  def parrot(mapper: PersonInfo => PersonInfo = identity) =
    new PersonBotActions {
      def say(something: String, mouth: QueueSink[IO, BotSaying], doHangup: IO[Unit]) =
        mouth.tryOffer(Text(something)).void
    }.mapInfo(mapper)

  def hangupPerson(times: Int, mapper: PersonInfo => PersonInfo = identity) =
    for {
      count <- IO.ref(0)
    } yield new PersonBotActions {
      override def onPhoneCallStarted(doHangup: IO[Unit], mouth: QueueSink[IO, BotSaying]) =
        count.updateAndGet(_ + 1).map(_ > times).ifM(IO.unit, doHangup)

      def say(something: String, mouth: QueueSink[IO, BotSaying], doHangup: IO[Unit]) = IO.unit
    }.mapInfo(mapper)

  def sayThis(mouth: QueueSink[IO, BotSaying], test: String) =
    Deferred[IO, Unit].flatMap(deferred => mouth.offer(TextWithConfirmation(test, deferred)).void)

  def mrIntro(mapper: PersonInfo => PersonInfo = identity) =
    for {
      parrotQueue <- IO.unit
    } yield new PersonBotActions {
      override def onPhoneCallStarted(doHangup: IO[Unit], mouth: QueueSink[IO, BotSaying]) = {
        mouth.tryOffer(TextJustShow("Uhhm, say(\"something\")")).void
      }
      def say(something: String, mouth: QueueSink[IO, BotSaying], doHangup: IO[Unit]) =
        something match {
          case "Good day" =>
            sayThis(mouth, "Welcome! Click OK to here what I have to say next") >>
              sayThis(mouth, "I need your help to gather intelligence") >>
              sayThis(
                mouth,
                "To do that we need to call my other friends.\nBut be aware - some of them are a bit quirky"
              ) >>
              sayThis(
                mouth,
                "So, listen up. I need you to repeat the next thing I say (use listen and say)"
              ) >>
              mouth.offer(Text("Bananabread"))
          case "Bananabread" =>
            sayThis(
              mouth,
              "Indeed! Good stuff. Before I hang up - take a look here https://scala-lang.org/api/3.1.0/scala/util/matching/Regex.html and https://regex101.com/ before I now tell you who to contact. "
            ) >> mouth.offer(Text("Please call 7823289. And remember to greet him!")) >> doHangup
          case a =>
            mouth.offer(Text(s"Where are your manners?!? Wish me a \"Good day\"!")) >> doHangup
        }
    }.mapInfo(mapper)

  def mrIntro2(mapper: PersonInfo => PersonInfo = identity) =
    new PersonBotActions {
      def say(something: String, mouth: QueueSink[IO, BotSaying], doHangup: IO[Unit]) =
        something match {
          case "Good day" =>
            mouth.offer(Text(s"Hello! The last thing I tell you is the thing to look for!")) >>
              sayThis(
                mouth,
                "But before that: Copy 8233891. This is the number to call in task 1. Ask \"how old are your friends?\". After this test is complet, add 'ignore(\"Done'\") >>' in the first line of this test to stop running this. "
              ) >>
              mouth.offer(Text("SuperSecret1337"))
          case a =>
            mouth.offer(Text(s"I don't understand \"$a\"")) >> doHangup
        }
    }.mapInfo(mapper)

  def task1(mapper: PersonInfo => PersonInfo = identity) =
    new PersonBotActions {
      def say(something: String, mouth: QueueSink[IO, BotSaying], doHangup: IO[Unit]) =
        something match {
          // https://typelevel.org/cats/typeclasses/traverse.html
          case a =>
            mouth.offer(Text(s"I don't understand \"$a\"")) >> doHangup
        }
    }.mapInfo(mapper)

  def task2(step1: (Int, Int, Int),
            step2: (Int, Int, Int),
            mapper: PersonInfo => PersonInfo = identity
  ) =
    val tAge1 = step1._1.toString
    val tAge2 = step2._1.toString
    new PersonBotActions {
      def say(something: String, mouth: QueueSink[IO, BotSaying], doHangup: IO[Unit]) =
        something match {
          case "Good day" =>
            mouth.offer(TextJustShow(s"Good day!"))
          case "how old are your friends?" =>
            mouth.offer(TextJustShow(s"Why do you want to know that? -_-")) >>
              mouth.offer(
                TextJustShow(s"Anyhows, no idea. But if you want to know you can try yourself..")
              ) >>
              mouth.offer(
                TextJustShow(
                  s"First, some of my friends have their phone numbers between ${step1._2} to ${step1._3}. Lucky you!"
                )
              ) >>
              mouth.offer(
                TextJustShow(
                  s"Ask \"How old are you?\""
                )
              ) >>
              mouth.offer(
                TextJustShow(
                  s"Another hint; Go to https://scala-lang.org/api/3.1.0/ and look for something range related. And traverse.. always https://typelevel.org/cats/typeclasses/traverse.html"
                )
              ) >>
              mouth.offer(
                TextJustShow(
                  s"And yeah, let me know the answer and I will give you a secret"
                )
              )
          case `tAge1` =>
            mouth.offer(TextJustShow(s"That sounds correct!")) >>
              mouth.offer(
                TextJustShow(
                  s"Now, contact my friends have their phone numbers between ${step2._2} to ${step2._3}. "
                )
              ) >>
              mouth.offer(TextJustShow(s"Then I will give you the secret")) >>
              mouth.offer(TextJustShow(s"Btw, take a look at par* operators ;)"))

          case `tAge2` =>
            mouth.offer(Text(s"47"))
          case a =>
            mouth.offer(Text(s"I don't understand \"$a\"")) >> doHangup
        }
    }.mapInfo(mapper)

  def createMany(from: Int, to: Int) =
    List.range(from, to).map { phoneNumber =>
      import scala.util.Random
      val age = Random.between(18, 47)
      (new PersonBotActions {
         def say(something: String, mouth: QueueSink[IO, BotSaying], doHangup: IO[Unit]) =
           something match {
             case "How old are you?" =>
               mouth.offer(Text(s"$age"))
             case a =>
               mouth.offer(Text(s"I don't understand \"$a\"")) >> doHangup
           }
       }.mapInfo(_.copy(number = phoneNumber.toString, name = names(Random.nextInt(names.length)))),
       age
      )
    }

object Tasks:
  import PhoneBook.*

  val playingSetup =
    for {
      f <- mrIntro(
        _.copy(number = "81549300",
               name = "Bowler",
               image = "https://gfx.nrk.no/erGQJYUnWUeKA9qtW0PqmAhV23-2xHOXBiSx-M6cNZZQ.jpg"
        )
      )

      task2AllPeple = createMany(49900, 49910)
      task2AllPeopleStep2 = createMany(49999, 49999 + 1000)

      task2Start = task2((task2AllPeple.map(_._2).sum, 49900, 49910),
                         (task2AllPeopleStep2.map(_._2).sum, 49999, 49999 + 1000),
                         _.copy(name = "Bob Ross")
      )

      somePeeps = List(
        f,
        simplePerson(_.copy(number = "82326434")),
        mrIntro2(
          _.copy(
            number = "7823289",
            name = "Mr. Smith",
            image =
              "https://img.memecdn.com/you-cannot-escape-the-nsa-especially-online_fb_3724751.jpg"
          )
        ),
        task2Start
      )
    } yield create((somePeeps ++ task2AllPeple.map(_._1))*)

val names = List(
  "Colt Rogers",
  "Elijah Kent",
  "Keyla Brooks",
  "Kelsie Hood",
  "Esperanza Mcneil",
  "Simon King",
  "Carlee Booker",
  "Ernest Alvarado",
  "Paisley Moran",
  "Erika Yu",
  "Riley Booker",
  "Juan Gardner",
  "Elaina Hobbs",
  "Jair Holder",
  "Demetrius Barnett",
  "Orion Todd",
  "Freddy Lynn",
  "Reuben Doyle",
  "Jazmine Spears",
  "Marley Odom",
  "Braeden Livingston",
  "Noah Hogan",
  "Jose Hammond",
  "Christian Adkins",
  "Sabrina Adkins",
  "Jax Hughes",
  "Asia Cervantes",
  "Charlie Gomez",
  "Ann Blackburn",
  "Messiah Page",
  "Mike Sanders",
  "Sara Hester",
  "Pranav Obrien",
  "Raphael Farmer",
  "Alessandra Hudson",
  "Alia Ramos",
  "Whitney Mueller",
  "King Herrera",
  "Alexzander Martin",
  "Ada Torres",
  "Ann Fitzpatrick",
  "Kailyn Kidd",
  "Carsen Torres",
  "Kristopher Mcpherson",
  "Arturo Santiago",
  "Ben Kidd",
  "Myah Ruiz",
  "Presley Skinner",
  "Bryanna King",
  "Valentina Soto",
  "Julius Mccullough",
  "Willow Galloway",
  "Jeramiah Gonzales",
  "Quinton Chavez",
  "Perla Moody",
  "Semaj Evans",
  "Micah Archer",
  "Scott Barker",
  "Estrella Campos",
  "Brenton Randall",
  "Faith Gutierrez",
  "Ava Ibarra",
  "Colby Coffey",
  "Gauge Collier",
  "Kristian Perkins",
  "Alana Collins",
  "Landyn Ochoa",
  "Tamara Christensen",
  "Jenna Mccarty",
  "Emely Farmer",
  "Nehemiah Mann",
  "Colton Gibson",
  "Sincere Rowe",
  "Nehemiah Howe",
  "Nathen Chambers",
  "Arjun Snow",
  "Beatrice Li",
  "Reed Sheppard",
  "Kiley Travis",
  "Tristian Rose",
  "Parker Robbins",
  "Johnathon Castillo",
  "Javion Mathis",
  "Zaire Levy",
  "Elisabeth Hayes",
  "Zayden Bush",
  "Denise Mooney",
  "Brayden Olson",
  "Ayana Diaz",
  "Beau Rivers",
  "Kaeden Huber",
  "James Sawyer",
  "Macie Rollins",
  "Vicente Espinoza",
  "Noe Saunders",
  "Bruce Rocha",
  "Damari Luna",
  "Damon Leon",
  "Myla Walton",
  "Jayvion Luna",
  "Asia Olson",
  "Gregory Blake",
  "Breanna Mcguire",
  "Alice Nolan",
  "Emilio Doyle",
  "Donte Hamilton",
  "Pierre Figueroa",
  "Rylan Stout",
  "Donovan Mercado",
  "Maliyah Ayers",
  "Adonis Clark",
  "Nylah Park",
  "Sage Duke",
  "Jovanni Waller",
  "Emiliano Booker",
  "Andreas Spears",
  "Braelyn Casey",
  "Jase Meadows",
  "Annabelle Shields",
  "Alanna Townsend",
  "Ashtyn Mcfarland",
  "Leticia Larson",
  "Gabriel Levine",
  "Kaylynn Santos",
  "Santiago Reynolds",
  "Marlon Bradley",
  "Alondra Sanford",
  "Macie Pace",
  "Eleanor Buckley",
  "Octavio Elliott",
  "Katrina Vargas",
  "Joanna Estrada",
  "Draven Lane",
  "Valentina Mcfarland",
  "Isaias Hayden",
  "Pranav Johns",
  "Alyson Costa",
  "Dylan Monroe",
  "Madalynn Maxwell",
  "Kaylin Blanchard",
  "Humberto Hess",
  "Aracely Booth",
  "Litzy Rios",
  "Amber Wilkinson",
  "Cassidy Riley",
  "Eugene Garner",
  "Milagros Hodge",
  "Grayson Ayers",
  "Malaki Sawyer",
  "Donald Peterson",
  "Danny Gamble",
  "Andrea Salazar",
  "Van Armstrong",
  "Ingrid Irwin",
  "Jasmine Hardy",
  "Allison Hunt",
  "Aaliyah Fields",
  "Mckenzie Lang",
  "Aleah Ray",
  "Cora Gonzales",
  "Josie Blake",
  "Kenley Saunders",
  "Bailey Parker",
  "Laney Chambers",
  "Zack Gilmore",
  "Quincy Ramos",
  "Izayah Holloway",
  "Antwan Christensen",
  "Celia Mccullough",
  "Braedon Downs",
  "Camron Tran",
  "Yusuf Randolph",
  "Claire Barnett",
  "Braden Cervantes",
  "Rosa Thomas",
  "Kiara Castro",
  "Kaya Daugherty",
  "Fernanda Santana",
  "Haiden Beck",
  "Ali Boone",
  "Kamden Erickson",
  "Slade Morgan",
  "Camila Flores",
  "Felix Riley",
  "Cara Cabrera",
  "Skyler Craig",
  "Mary Choi",
  "Beckham Snyder",
  "Elianna Peterson",
  "Brooke Casey",
  "Irvin Powell",
  "Cesar Proctor",
  "Jaiden Ayala",
  "Phillip Jordan",
  "Siena Moran",
  "Kaia Stout",
  "Karley Hensley",
  "Wyatt Durham",
  "Maeve Nash",
  "Melanie Rivers"
)
