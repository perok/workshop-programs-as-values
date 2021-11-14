# Scala3 template with CE3, Tapir and scalajs-react

Fullstack setup using shared Tapir API models that the defines the backend API and reuses
it in the frontend.

- Use React in the frontend with scalajs-react and tailwindcss for styling.
- Build the backend with Flyway for maintaining the Postgres schema and create
  queries with [Skunk](https://tpolecat.github.io/skunk/).
- Testing with weaver and testcontainers.
- Create a deployable application with sbt-native-packager.
- Go to localhost:8080/docs for OpenAPI documentation from Tapir.

All this with Scala 3, have a nice day!


TODO
- simplified compile
- simplify package name, backend, shared, frontend as top level names
- archunit
- move out some of the extra scalajs-react stuff as well?
- create template repository


 https://github.com/gbogard/scala3-nextjs-template

- /backend - tapir http4s backend, skunk
- /shared
    - /api Tapir and communication models
- /frontend -  scala.js-react v2

## CLOSER TODO

- Effekter: https://cssfx.dev/ (loading, knapper, osv)
- Bytt til funksjonell WithId (må ha case class for Id'er)
- Frontend
  - Pure rendering. Sjekk kun referanse diff


## TODO

- https://github.com/brettwooldridge/HikariCP
- Omskriv til denne stilen: https://github.com/pheymann/meetup-with-functions-and-monads-into-the-rabbit-hole

## dev
- ./startDb.sh
- sbt 'reStart; ~fastLinkJS'

###  Testing

- sbt test
- sbt it:test

## Scala.js
- sbt fastLinkJS
- npm run start

- sbt fullLinkJS
- npm run build
- TODO setup output to resources folder
- TODO setup webpack-merge with diff for dev production
- TODO react hot reloading

## Libraries and documentation

