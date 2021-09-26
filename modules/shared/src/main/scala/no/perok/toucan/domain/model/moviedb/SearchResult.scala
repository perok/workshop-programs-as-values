package no.perok.toucan.domain.model.moviedb

import io.circe.Decoder
import io.circe.derivation.deriveDecoder

case class SearchResult[A](page: Int, total_results: Int, total_pages: Int, results: List[A])

@scala.annotation.nowarn
object SearchResult {
  implicit def decoder[A: Decoder]: Decoder[SearchResult[A]] = deriveDecoder
}
