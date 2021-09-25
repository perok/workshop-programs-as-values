package no.perok.toucan.domain.models

/* TheMovieDb data */
// TODO nei.. Dette er kun for å samle film id'er
// Title is available only for ease of DB lookups and fast "first data showing"
// TODO should I store the json blob from TheMovieDb?
final case class Movie(id: ID[Movie], title: String)
