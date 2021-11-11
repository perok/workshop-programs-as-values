package no.perok.toucan.view.components

/** Note: In my experience, optics are absolutely instrumental to writing a scalajs-react UI app
  * where components are nearly all stateless and actually modular and reusable. Have the components
  * ask for as little as possible, use StateSnapshots instead of actual React state and use optics
  * to glue all the layers together. On very large codebases especially, this approach scales very,
  * very well.
  */
class button {}
