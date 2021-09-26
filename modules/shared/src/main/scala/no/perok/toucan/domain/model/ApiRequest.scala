package no.perok.toucan.domain.model
// TODO package no.perok.toucan.shared.api

import sttp.tapir._
import sttp.tapir.json.circe._

// TODO remove
import sttp.tapir.generic.auto._
import io.circe.generic.auto._

object ApiRequest {
  type Limit = Int
  type AuthToken = String
  case class BooksFromYear(genre: String, year: Int)
  case class Book(title: String)

  val booksListing: Endpoint[(BooksFromYear, Limit, AuthToken), String, List[Book], Any] =
    endpoint.get
      .in(("books" / path[String]("genre") / path[Int]("year")).mapTo[BooksFromYear])
      .in(query[Limit]("limit").description("Maximum number of books to retrieve"))
      .in(header[AuthToken]("X-Auth-Token"))
      .errorOut(stringBody)
      .out(jsonBody[List[Book]])

}
