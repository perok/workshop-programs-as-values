package no.perok.toucan.shared.api

object Data:
  import sttp.tapir.Schema
  import io.circe.Codec

  case class Book(title: String) derives Codec.AsObject, Schema

object ApiRequest:
  import sttp.tapir._
  import sttp.tapir.json.circe._

  import Data._

  case class BooksFromYear(genre: String, year: Int)

  type Limit = Int
  type AuthToken = String

  val test: PublicEndpoint[String, Nothing, Int, Any] =
    infallibleEndpoint.get
      .in(("books" / path[String]("genre")))
      .out(jsonBody[Int])

  val booksListing: PublicEndpoint[(BooksFromYear, Limit, AuthToken), String, List[Book], Any] =
    endpoint.get
      .in(("books" / path[String]("genre") / path[Int]("year")).mapTo[BooksFromYear])
      .in(query[Limit]("limit").description("Maximum number of books to retrieve"))
      .in(header[AuthToken]("X-Auth-Token"))
      .errorOut(stringBody)
      .out(jsonBody[List[Book]])
