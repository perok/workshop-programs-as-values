package no.perok.toucan.shared.models.moviedb

import io.circe.Decoder

case class SearchResult[A](page: Int, total_results: Int, total_pages: Int, results: List[A])
    derives Decoder
