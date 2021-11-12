- https://github.com/gbogard/scala3-nextjs-template
- [ ] Make it compile again
- [ ] Cleanup unwanted stuff

- /backend - tapir http4s backend, skunk
- /shared
    - /api Tapir and communication models
- /frontned -  scala.js-react v2

- Things:
  - Tapir https://github.com/softwaremill/tapir
  - Refined
  - Circe -> json
  - testcontainers -> db tests


# Strategi

- Postgres lokal i starten
  - `docker run --name some-postgres -p 5432:5432 -e POSTGRES_PASSWORD=mysecretpassword -d postgres:9.6.3`

## CLOSER TODO

- Doobie to Skunk
- https://github.com/fthomas/refined
- Effekter: https://cssfx.dev/ (loading, knapper, osv)
- Bytt til funksjonell WithId (må ha case class for Id'er)
- Frontend
  - Pure rendering. Sjekk kun referanse diff
- PROBLEM INTELLIJ https://github.com/scalacenter/scalajs-bundler/issues/189
  - Problemet er npm, path og intellij. "Fiksa" med sbt i term?
- Filter havner i backend/target/web/.../..{js|html} men hvordan få Http4s til å serve de?
  - Blir de i det heletatt pakka med av revolver?
  - Pakke med i sbt-assemly
  - Hva er pathen mot de?
- https://www.scala-lang.org/2019/10/17/dependency-management.html


## TODO

- https://github.com/brettwooldridge/HikariCP
- Omskriv til denne stilen: https://github.com/pheymann/meetup-with-functions-and-monads-into-the-rabbit-hole


## Scala.js
- sbt fastLinkJS
- npm run start

- sbt fullLinkJS
- npm run build
- TODO setup output to resources folder
- TODO setup webpack-merge with diff for dev production

## Libraries and documentation

- Byggverktøy: SBT
- Core libs: [Cats (functional std lib)](), [Cats Effect (functional effects std lib]()
- HTTP: [Tapir (OpenAPI endpoints)](https://tapir.softwaremill.com/en/latest/endpoint/basics.html) with http4s
- DB: Doobie, Flyway
- JSON serialization: Circe
- Injection: https://gist.github.com/gvolpe/1454db0ed9476ed0189dcc016fd758aa#fp-for-the-win
- Testing: mdoc


## Testing

- sbt test
- sbt it:test
