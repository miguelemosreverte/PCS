package api

object Utils {

  object Transformation {
    /* For each class property, use the camelCase case equivalent
     * to name its column
     * (e.g. exampleFormat --> example_format)
     * (e.g. EXAMPLE_FORMAT --> example_format)
     */
    def to_underscore(property: String): String = {
      def camelToUnderscores(name: String) =
        "[A-Z\\d]".r.replaceAllIn(name, { m =>
          "_" + m.group(0).toLowerCase()
        })
      val proposedUnderscore = camelToUnderscores(property).toLowerCase
      // in this case the string was already correct
      // and now looked like this _s_u_j__t_i_p_o
      if (proposedUnderscore contains "__") property.toLowerCase
      else proposedUnderscore
    }

    def toCamelCase(json: String): String =
      json match {
        case s"${letterA}_${letterB}" if letterB.length > 1 =>
          toCamelCase(letterA + letterB.head.toUpper.toString + letterB.tail.toString)
        case other =>
          other
      }

  }
  import Transformation._

  def standarization(json: String): String = {

    """"(\w*?)"\s?:""".r.replaceAllIn(json, { jsonKey =>
      to_underscore(jsonKey.group(0))
    })

  }

  def reverseStandarization(json: String): String = {

    """"(\w*?)"\s?:""".r.replaceAllIn(json, { jsonKey =>
      toCamelCase(jsonKey.group(0))
    })

  }

}
