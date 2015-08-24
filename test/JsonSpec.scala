
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play._
import play.api.libs.json._

/**
 * Play Json/Reads/Writes の勉強
 */
class JsonSpec extends PlaySpec {

  case class Person(name: String, age: Int)

  val jsonString = """{"name":"tanacasino","age":29}"""

  "A Json" must {

    "Jackson" in {
      // PlayがJacksonをどのように使っているのか？
      import com.fasterxml.jackson.databind.ObjectMapper
      import play.api.libs.json.jackson.PlayJsonModule
      val mapper = new ObjectMapper().registerModule(PlayJsonModule)
      val json = mapper.readValue(jsonString, classOf[JsValue])
      json.isInstanceOf[JsValue] mustBe true

      // チルダ特殊扱いなのか・・・Tupleがネスト深くならないという技が
      case class ~[A, B](_1: A, _2: B)
      val x: String ~ Int = new ~("", 10)

    }

    val jsValue = Json.parse(jsonString)
    jsValue.isInstanceOf[JsValue] mustBe true

    "JsValue ops" in {
      val expected = JsObject(Seq(
        "name" -> JsString("tanacasino"),
        "age" -> JsNumber(29)
      ))

      jsValue mustBe expected

      Json.stringify(jsValue) mustBe jsonString
    }

    "Reads Basis" in {
      val personReads = Json.reads[Person] // Macro
      val personResult = Json.fromJson(jsValue)(personReads)
      personResult.isSuccess mustBe true
      personResult.get mustBe Person(name = "tanacasino", age = 29)
    }

    "Reads with functional builder" in {
      val nameReads = (JsPath \ "name").read[String]
      val ageReads = (JsPath \ "age").read[Int]

      import play.api.libs.functional.syntax._ // Combinator syntax

      val personReadsBuilder = nameReads and ageReads
      val personReads = personReadsBuilder.apply(Person.apply _)

      val personResult = Json.fromJson(jsValue)(personReads)
      personResult.isSuccess mustBe true
      personResult.get mustBe Person(name = "tanacasino", age = 29)

      // apply は省略できるので
      val personReadsWithoutApply = personReadsBuilder(Person.apply _)

      // まとめて書くと (JsPath は __ というAliasがある: Type Alias)
      val personReadsStandard = (
        (__ \ "name").read[String] and
        (__ \ "age").read[Int]
      )(Person.apply _)

      // Json.reads[T] による Macro は ↑と等価
    }

    "Reads Validation" in {
      // Validationの追加(文字数や正規表現による制限)

      import play.api.libs.functional.syntax._ // Combinator syntax
      import play.api.libs.json.Reads._ // Reads のデフォルトパターン

      // name が 11文字以下はNGにする
      val personReadsNameMinLength = (
        (__ \ "name").read[String](minLength[String](11)) and
        (__ \ "age").read[Int]
      )(Person.apply _)

      val personResult = Json.fromJson(jsValue)(personReadsNameMinLength)
      personResult.isSuccess mustBe false
      personResult.isError mustBe true

      val jsError = personResult.asInstanceOf[JsError]
      jsError.errors.length mustBe 1
      println(jsError.errors)

      // 複数のValidationError をまとめて取れる
      val personReadsValidation = (
        (__ \ "name").read[String](minLength[String](11)) and
        (__ \ "age").read[Int](min[Int](30))
      )(Person.apply _)

      val personResult2 = Json.fromJson(jsValue)(personReadsValidation)
      personResult2.isSuccess mustBe false
      personResult2.isError mustBe true
      val jsError2 = personResult2.asInstanceOf[JsError]
      jsError2.errors.length mustBe 2
      println(jsError2.errors)
    }

    "Reads with write my self" in {
      // 参考
      // trait Reads[A] {
      //   def reads(json: JsValue): JsResult[A]
      // }
      val stringReads: Reads[String] = new Reads[String] {
        def reads(json: JsValue): JsResult[String] = {
          json match {
            case JsString(s) => JsSuccess(s)
            case _ => JsError("error.expected.jsstring")
          }
        }
      }

      val readString = Json.fromJson(JsString("test"))(stringReads)
      readString.isSuccess mustBe true
      readString.get mustBe "test"

      // では Person の Reads はどう書けばよいのかな？
      val nameReads = (__ \ "name").read[String]
      val ageReads = (__ \ "age").read[Int]

      val personReads: Reads[Person] = new Reads[Person] {
        def reads(json: JsValue): JsResult[Person] = {
          val nameResult = nameReads.reads(json)
          val ageResult = ageReads.reads(json)

          (nameResult, ageResult) match {
            case (JsSuccess(name, _), JsSuccess(age, _)) => JsSuccess(Person(name, age))
            case (JsError(e1), JsError(e2)) => JsError(JsError.merge(e1, e2)) // JsErrorをマージ
            case (JsError(e), _) => JsError(e)
            case (_, JsError(e)) => JsError(e)
          }
          // フィールド増えると辛くない？うまく合成する方法は？

          // 合成といえば？ これだとパスが正しくならない。repathとか使うんだろうか？
          //nameResult.flatMap { name =>
          //  ageResult.map { age =>
          //    Person(name = name, age = age)
          //  }
          //}
        }
      }

      // Reads 合成はどうなる？ これもパスがおかしくなる。
      val personReadsMap = nameReads.flatMap { name =>
        ageReads.map { age =>
          Person(name = name, age = age)
        }
      }

      // なんか違う！
      println(Json.fromJson(jsValue)(Json.reads[Person]))
      println(Json.fromJson(jsValue)(personReads))
      println(Json.fromJson(jsValue)(personReadsMap))

      val jsValue2 = JsObject(Seq("name" -> JsString("name")))
      println(Json.fromJson(jsValue2)(Json.reads[Person]))
      println(Json.fromJson(jsValue2)(personReads))
      println(Json.fromJson(jsValue2)(personReadsMap))

      // and は、implicit conversion の嵐だった。手で直してみよう。
      import play.api.libs.functional._
      import play.api.libs.functional.syntax

      val applicativeJsResult: Applicative[JsResult] = JsResult.applicativeJsResult
      val applicativeReads: Applicative[Reads] = Reads.applicative(applicativeJsResult)
      val functionalCanBuildReads: FunctionalCanBuild[Reads] = syntax.functionalCanBuildApplicative(applicativeReads)
      val ops = syntax.toFunctionalBuilderOps(nameReads)(functionalCanBuildReads)
      val personBuilder = ops.and(ageReads)
      val personReadsMySelf = personBuilder.apply(Person.apply _)

      println("myself")
      println(Json.fromJson(jsValue)(personReadsMySelf))
      println(Json.fromJson(jsValue2)(personReadsMySelf))

      // できた！
      // むずかしぃぃぃぃいいい
    }

    "Writes" in {
      // TODO
    }

  }

}

