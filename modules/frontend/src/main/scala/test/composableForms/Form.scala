package test.composableForms

import cats.syntax.all._
import test.composableForms.Form.Field
import test.composableForms.Form.Form

object Form {

  sealed trait Error
  object Error {
    object RequiredFieldIsEmpty
    case class ValidationFailed(errror: String)
    case class External(error: String)
  }

//  {-| A [`Form`](Form#Form) that can contain any type of `field`.
//    -}
  // TODO variance?
  // TODO base
  case class Form[values, output, field](fill: values => FilledForm[output, field])

//  {-| Like [`Form.succeed`](Form#succeed) but not tied to a particular type of `field`.
//    -}
//  succeed : output -> Form values output field
  def succeed[values, output, field](success: output): Form[values, output, field] =
    Form(_ => FilledForm(List.empty, success.asRight, isEmpty = false))

  //  {-| Create a custom field with total freedom.
//    You only need to provide a function that given some `values` produces a [`FilledField`](#FilledField).
//    You can check the [custom fields example][custom-fields] for some inspiration.
//    [custom-fields]: https://hecrj.github.io/composable-form/#/custom-fields
//    -}
//  custom : (values -> CustomField output field) -> Form values output field

//  append : Form values a field -> Form values (a -> b) field -> Form values b field
  /* Append a form to another one while **capturing** the `output` of the first one.
For instance, we could build a signup form:
    signupEmailField : Form { r | email : String } EmailAddress
    signupEmailField =
        Form.emailField
            { -- ...
            }
    signupPasswordField : Form { r | password : String } Password
    signupPasswordField =
        Form.passwordField
            { -- ...
            }
    signupForm :
        Form
            { email : String
            , password : String
            }
            ( EmailAddress, Password )
    signupForm =
        Form.succeed Tuple.pair
            |> Form.append signupEmailField
            |> Form.append signupPasswordField
In this pipeline, `append` is being used to feed the `Tuple.pair` function and combine two forms
into a bigger form that outputs `( EmailAddress, Password )` when submitted.
**Note:** You can use [`succeed`](#succeed) smartly to **skip** some values.
This is useful when you want to append some fields in your form to perform validation, but
you do not care about the `output` they produce. An example of this is a "repeat password" field:
    passwordForm :
        Form
            { password : String
            , repeatPassword : String
            }
            Password
    passwordForm =
        Form.succeed (\password repeatedPassword -> password)
            |> Form.append passwordField
            |> Form.append repeatPasswordField

   */
  def append[values, a, field, b](
      _new: Form[values, a, field]
  )(current: Form[values, a => b, field]): Form[values, b, field] =
    Form(values => {
      val filledNew = _new.fill(values)
      val filledCurrent = current.fill(values)

      val fields = filledNew.fields ++ filledCurrent.fields
      val isEmpty = filledNew.isEmpty && filledCurrent.isEmpty

      (filledCurrent.result, filledNew.result) match {
        case (Right(value), _) =>
          FilledForm(fields, filledNew.result.map(value(_)), isEmpty)
        case (Left(currentErrors), Right(_)) =>
          FilledForm(fields, currentErrors.asLeft, isEmpty)
        case (Left((firstError, otherErrors)), Left((newFirstError, newOtherErrors))) =>
          val newErrors =
            (firstError, otherErrors ++ (newFirstError :: newOtherErrors)).asLeft

          FilledForm(fields, newErrors, isEmpty)
      }
    })

  // Form functions

//  {-| Like [`Form.andThen`](Form#andThen) but not tied to a particular type of `field`.
//    -}
  def flatMap[values, a, field, b](next: a => Form[values, b, field])(
      current: Form[values, a, field]
  ): Form[values, b, field] =
    Form(values => {
      val filled = current.fill(values)

      filled.result match {
        case Right(output) =>
          val childFilled = next(output).fill(values)

          FilledForm(
            filled.fields ++ childFilled.fields,
            childFilled.result,
            filled.isEmpty && childFilled.isEmpty
          )
        case Left(value) =>
          filled.copy(result = value.asLeft)
      }
    })

//  {-| Like [`Form.optional`](Form#optional) but not tied to a particular type of `field`.
//    -}
//  optional : Form values output field -> Form values (Maybe output) field

//  {-| Like [`Form.disable`](Form#disable) but not tied to a particular type of `field`.
//    -}
//  disable : Form values output field -> Form values output field

//  {-| Like [`Form.meta`](Form#meta) but not tied to a particular type of `field`.
//    -}
//  meta : (values -> Form values output field) -> Form values output field

//  {-| Like [`Form.map`](Form#map) but not tied to a particular type of `field`.
//    -}
//  map : (a -> b) -> Form values a field -> Form values b fieldz

//  {-| Apply a function to the input `values` of the form.
//    -}
//  mapValues : (a -> b) -> Form b output field -> Form a output field

//  {-| Apply a function to each form `field`.
//    -}
//  mapField : (a -> b) -> Form values output a -> Form values output b

  /// FORM OUTPUT

//  {-| Represents a filled form.
//    You can obtain this by using [`fill`](#fill).
//    -}
  case class FilledForm[output, field](
      fields: List[FilledField[field]],
      result: Either[(Error, List[Error]), output],
      isEmpty: Boolean
  )

//  {-| Represents a filled field.
//    -}
  case class FilledField[field](
      state: field,
      error: Option[Error],
      isDisabled: Boolean
  )

  /// FIELD

  //
//  {-| Represents a form field.
//    It contains:
//    - the current `value` of the field
//    - an `update` function that takes a new **field** value and returns updated
//    **form** values
//      - the `attributes` of the field
//    These record fields are normally used in view code to set up the `value` and `onInput`
//      attributes. For example, you could render a `TextField` like this:
//    view : (values -> msg) -> Form values output -> values -> Html output
//    view onChange form values =
//    let
//    { fields, result } =
//      Form.fill form values
//    fieldsHtml =
//      List.map (viewField onChange) fields
//        -- ...
//    in
//    Html.form
//      [-- ...
//    ]
//    [ Html.div [] fieldsHtml
//    , submitButton
//    ]
//    viewField : (values -> msg) -> ( Form.Field values, Maybe Error ) -> Html msg
//      viewField onChange ( field, maybeError ) =
//    case field of
//      Form.Text TextField.Raw { value, update, attributes } ->
//    Html.input
//      [ Attributes.type_ "text"
//    , Attributes.value value
//    , Attributes.onInput (update >> onChange)
//    , Attributes.placeholder attributes.placeholder
//    ]
//    []
//    _ ->
//      -- ...
//    -}
  case class Field[attributes, value, values](
      value: value,
      update: value => values,
      attributes: attributes
  )
  object Field {
//    {-| Transform the `values` of a `Field`.
//      It can be useful to build your own [`Form.mapValues`](Form#mapValues) function.
//      -}
//    mapValues : (a -> b) -> Field attributes value a -> Field attributes value b
  }

//  {-| Represents a form field value.
//    -}
//  type Value a
//  = Blank
//  | Filled a
  // => Option

}

object Base {
  // TODO different kinds of Base fields
//  {-| Most form fields require configuration! `FieldConfig` allows you to specify how a
//    concrete field is validated and updated, alongside its attributes:
//    - `parser` must be a function that validates the `input` of the field and produces a `Result`
//    of either:
//      - the correct `output`
//        - a `String` describing a problem
//        - `value` defines how the [`Value`](Form.Value) of the field is obtained from the form `values`
//    - `update` defines how the current form `values` should be updated with a new field
//    [`Value`](Form.Value)
//    - `attributes` represent the attributes of the field
//    -}
//  type alias FieldConfig attrs input values output =
//    { parser : input -> Result String output
//      , value : values -> Value input
//      , update : Value input -> values -> values
//      , attributes : attrs
//    }
  case class FieldConfig[attrs, input, values, output](
      parser: input => Either[String, output],
      value: values => Option[input],
      update: Option[input] => values => values,
      attributes: attrs
  )

  object TextField {

    type AttributesTextField = { val label: String }
    type TextField[values] = Field[AttributesTextField, String, values]
    //  form :
    //    (TextField values -> field)
    //  -> Base.FieldConfig Attributes String values output
    //  -> Base.Form values output field
    //    form =
    //    Base.field { isEmpty = String.isEmpty }

    def form[values, output, field]: (
        TextField[values] => field
    ) => FieldConfig[AttributesTextField, String, values, output] => Form[values, output, field] =
      // TODO https://github.com/hecrj/composable-form/blob/a8dafcac4dd076e71d1a433f493f4d81eb42a6ad/src/Form/Base/TextField.elm#L66
      ???

  }
}

object View {
  // TODO
}

object TopForm {
  // TODO
}

import test.composableForms.Form.{Field, Form}
object Tests {
//  import Form._
  import Base._

  sealed trait InputTypes[values] // GADT? Need Dotty? or Free?
  case class TextInput[values](lol: Base.TextField.TextField[values]) extends InputTypes[values]

//  type alias Form values output =
//    Base.Form values output (Field values)
  type Form[values, output] = Form.Form[values, output, InputTypes[values]]

  /* Belongs to Form
  passwordField :
    { parser : String -> Result String output
    , value : values -> String
    , update : String -> values -> values
    , error : values -> Maybe String
    , attributes : TextField.Attributes
    }
    -> Form values output
passwordField =
    TextField.form (Text TextPassword)
   */
  def passwordField[values, output](
      config: Base.TextField.TextField[values]
  ): Form[values, output] =
    // TODO is config paramtere right? https://github.com/hecrj/composable-form/blob/2eade6914ef941f4f7365ccbd8abb0af62a1f5d9/src/Form.elm#L134
    TextField.form(???)(???) //TextInput[values](config) => ???)

  // Login form
  case class Login(username: String, password: String)
  val a = Form.succeed[Any, String => String => Login, Any]((Login.apply _).curried)

  println(a)

}
