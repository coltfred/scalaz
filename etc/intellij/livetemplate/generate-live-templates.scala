import collection.immutable.List
import collection.Seq
import java.lang.String

case class Variable(name: String) {
  def toXml = <variable name={name} expression=" " defaultValue=" " alwaysStopAt="true"/>
}

case class Template(name: String, value: String, description: String, variables: List[Variable]) {
  def toXml =
  <template name={name} value={value} description={description} toReformat="false" toShortenFQNames="true">
    {for (v <- variables) yield v.toXml}
    <context>
      <option name="SCALA" value="true"/>
    </context>
  </template>
}

case class Arg(val name: String)

def method(shortcut: String, name: String, args: Arg*): List[Template] = {
  val varNames: Seq[String] = args.map("$" + _.name.toUpperCase + "$")
  val dotValue = "." + name + (if (args.isEmpty) "" else varNames.mkString("(", ", ", ")"))
  val vars = args.map((a: Arg) => Variable(a.name.toUpperCase)).toList
  val dotTemplate = Template("." + shortcut, dotValue, name, vars)
  val template = Template(shortcut, name + " " + varNames.mkString(" "), name, vars)
  
  if (name.endsWith(":"))
    List(template)
  else if (args.size == 1)
    List(dotTemplate, template)
  else
    List(dotTemplate)
}

def function(shortcut: String, name: String, args: Arg*): List[Template] = {
  val varNames: Seq[String] = args.map("$" + _.name.toUpperCase + "$")
  val vars = args.map((a: Arg) => Variable(a.name.toUpperCase)).toList
  val template = Template(shortcut, name + " " + varNames.mkString(" "), name, vars)
  List(template)
}

def escapeUnicode(xmlText: String): String = {
  def escapeChar(c: Char): String =
    if (c > 0x7F)
      "&#" + Integer.toString(c, 10) + ";"
    else
      c.toString

  xmlText.map(escapeChar(_)).mkString
}

val templates = List(
  method("apply", "⊛", Arg("f")),
  method("map", "∘", Arg("f")),
  method("mapmap", "∘∘", Arg("f")),
  method("|->", "↦", Arg("f")),
  method("bind", "∗", Arg("f")),
  method("plus", "⊹", Arg("a")),
  method("appendpure", "\u279C:", Arg("a")),
  method("pluspure", "\u279D:", Arg("a")),
  method("suml", "∑"),
  method("exists", "∃", Arg("f")),
  method("forall", "∀", Arg("f")),
  method("traversemonoid", "↣", Arg("f")),
  method("join", "μ"),
  method("cojoin", "υ"),
  method("copure", "ε"),
  method("comap", "∙", Arg("f")),
  method("pure", "η"),
  method("kleisli", "☆", Arg("f")),
  method("dual", "σ"),
  method("equal", "≟", Arg("a")),
  method("notequal", "≠", Arg("a")),
  method("<=", "≤", Arg("a")),
  method(">=", "≥", Arg("a")),
  method("<", "≨", Arg("a")),
  method(">>>", "⋙", Arg("a")),
  method("<<<", "⋘", Arg("a")),
  function("undefined", "⊥", Arg("a")),
  method("conjunction", "∧", Arg("a")),
  method("disjunction", "∨", Arg("a")),
  method("<==", "\u21D0", Arg("a")),
  method("zipstream", "\u0290"),
  method("mult", "\u220f"),
  function("zero", "∅")
).flatten

val templateSet = <templateSet group="scalaz">
  {templates.map(_.toXml)}
</templateSet>

println(escapeUnicode(new xml.PrettyPrinter(120, 4).format(templateSet)))


