// package no.perok.toucan.infrastructure

// import cats._
// import cats.implicits._
// import cats.effect._
// import io.circe.parser._
// import japgolly.scalajs.react._
// import japgolly.scalajs.react.extra.Ajax
// import org.scalajs.dom.XMLHttpRequest

// import scala.concurrent.ExecutionContext.Implicits.global
// import scala.concurrent.Future
// import cats.data.OptionT
// import scala.concurrent.ExecutionContext

// // TODO struktur for fetching | found | Error
// //sealed trait AsyncState[A]
// //object AsyncState {
// //  case class InFlight[A]() extends AsyncState[A]
// //  case class Failed[A]() extends AsyncState[A]
// //  case class Success[A](result: A) extends AsyncState[A]
// //}
// // TODO Skift til sttp? eller vent paa Client fra Http4s
// object api {
//   object moviedb {
//     import no.perok.toucan.domain.model.moviedb._

//     private val apiKey: String = settings.apiKey

//     // TODO implement Async https://stackoverflow.com/questions/55968102/how-to-create-asyncfuture-from-asyncio/55972324#55972324
//     implicit class AsyncCallbackCatsEffect[A](private val fa: AsyncCallback[A]) {
//       def liftToF[F[_]: Async]: F[A] =
//         Async[F].async { f: (Either[Throwable, A] => Unit) =>
//           fa.completeWith(in => Callback(f(in.toEither))).runNow()
//         }
//     }

//     // TODO or maybe use hammock?
//     def getMovieDetail[F[_]: Async](id: TheMovieDbId): F[MovieDetails] = { // TODO missing videos decoding
//       val xhr =
//         Ajax
//           .get(
//             show"https://api.themoviedb.org/3/movie/$id?api_key=$apiKey&append_to_response=videos")
//           .setRequestContentTypeJsonUtf8
//           .send
//           .validateStatusIs(200)(Callback.error)
//           .asAsyncCallback
//           .liftToF

//       xhr
//         .map(_.responseText)
//         .flatMap(
//           decode[MovieDetails](_)
//             .leftMap(a => new Exception(a.show))
//             .liftTo[F])
//     }

//     def queryMovieBy[F[_]: Async](name: String): F[SearchResult[MovieSearch]] = {
//       val xhr = Ajax
//         .get(
//           show"https://api.themoviedb.org/3/search/movie?api_key=$apiKey&query=$name&include_adult=true&language=en")
//         .send
//         .asAsyncCallback
//         .liftToF

//       xhr
//         .map(_.responseText)
//         .flatMap(
//           decode[SearchResult[MovieSearch]](_)
//             .leftMap(a => new Throwable(a.show))
//             .liftTo[F])
//     }
//   }

//   object internal {
//     import no.perok.toucan.domain.model._
//     import no.perok.toucan.domain.model.moviedb._
//     import no.perok.toucan.domain.model.api

// //    def newUser[F[_]](user: NewUserForm): F[Unit] = ???

// //    def vote(movieInTroopId: MovieInTroopId, userId: UserId)

//     // TODO fetch from backend
//     def tryFetchUserData[F[_]: Sync]: F[User] = {
//       Sync[F].delay(
//         User(
//           name = "Peri",
//           troops = List(
//             Troop(
//               id = 0,
//               name = "Ze people",
//               movies = List(
//                 MovieInTroop(movie = Movie(TheMovieDbId(11970), "Hercules"),
//                              watched = false,
//                              wantToSee = true.some,
//                              votes = 10),
//                 MovieInTroop(movie = Movie(TheMovieDbId(268), "Batman"),
//                              watched = false,
//                              wantToSee = None,
//                              votes = 20)
//               )
//             )),
//           defaultTroop = 0
//         )
//       )
//     }
//   }

//   object auth {
//     import scala.scalajs.js
//     import scala.scalajs.js.annotation.JSImport

//     class AuthData(
//         val domain: String,
//         val clientID: String,
//         val redirectUri: String,
//         val responseType: String,
//         val scope: String
//     ) extends js.Object

//     @JSImport("auth0-js", JSImport.Default)
//     @js.native
//     object auth0 extends js.Object {
//       @js.native
//       class WebAuth(options: AuthData) extends js.Object {
//         def authorize(): Unit = js.native
//       }
//     }

//     def login(): Unit = {
//       val authData = new AuthData(
//         "perok.eu.auth0.com",
//         "c8oRmwfbVhHlpocNgUApgPU8bpt5wWft",
//         "http://localhost:8080/api/auth/auth0/callback",
//         "token id_token",
//         "openid"
//       )

//       val webAuth = new auth0.WebAuth(authData)

//       webAuth.authorize()
//     }
//   }

//   private def futureToF[F[_]: LiftIO: ContextShift, A](future: => Future[A]): F[A] = {
//     implicit val csIo: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

//     IO.fromFuture(IO(future)).to[F]
//   }
// }
