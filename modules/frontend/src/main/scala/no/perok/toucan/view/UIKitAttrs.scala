// package no.perok.toucan.view

// import slinky.core._

// object UIKitAttrs {
//   object grid {
//     val init: CustomAttribute[String] = new CustomAttribute[String]("data-uk-grid")
//   }

//   object lightBox {
//     val init: CustomAttribute[String] = new CustomAttribute[String]("data-uk-lightbox")
//   }

//   object accordion {
//     val init: CustomAttribute[String] = new CustomAttribute[String]("data-uk-accordion")
//   }

//   object icon {
//     val init: String => TagMod[Any] = iconType =>
//       new CustomAttribute[String]("data-uk-icon") := s"icon: $iconType"

//     val vote: Boolean => TagMod[String] = b => init(if (b) "chevron-down" else "chevron-up")

//     val question = init("question")
//   }
