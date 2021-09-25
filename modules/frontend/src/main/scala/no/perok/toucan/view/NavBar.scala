package no.perok.toucan.view

// import japgolly.scalajs.react._
// import japgolly.scalajs.react.vdom.html_<^._
import slinky.web.html._
import slinky.core._

object NavBar {

  val lol = nav(
    className := "uk-navbar uk-navbar-container",
    div(
      className := "uk-navbar-left",
      a(
        className := "uk-navbar-toggle",
        href := "#",
        span(new CustomAttribute("uk-navbar-toggle-icon") := ""),
        span(className := "uk-margin-small-left", "Menu")
      )
    ),
    div(
      className := "uk-navbar-right",
      div(
        a(
          className := "uk-navbar-toggle",
          // ref := "#",
          UIKitAttrs.icon.init("search")
        )
        /* TODO
                          <div class="uk-drop" uk-drop="mode: click; pos: left-center; offset: 0">
              <form class="uk-search uk-search-navbar uk-width-1-1">
                  <input class="uk-search-input" type="search" placeholder="Search..." autofocus>
              </form>
          </div>
       */
      )
    )
  )
}
