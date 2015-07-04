import scala.concurrent.ExecutionContext.Implicits.global

import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._

import org.elasticsearch.common.settings.ImmutableSettings.Builder
import org.codelibs.elasticsearch.runner.ElasticsearchClusterRunner
import org.codelibs.elasticsearch.runner.ElasticsearchClusterRunner.newConfigs

import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play._


class SampleSpec extends PlaySpec with BeforeAndAfterAll {

  lazy val runner = new ElasticsearchClusterRunner


  override def beforeAll(): Unit = {
    println(s"${this.getClass}: beforeAll")
    runner.onBuild(new ElasticsearchClusterRunner.Builder() {
      override def build(number: Int, builder: Builder): Unit = {
        builder.put("http.cors.enabled", true)
      }
    }).build(
        newConfigs()
          .clusterName("es-cl-run-" + System.currentTimeMillis())
          .ramIndexStore().numOfNode(1))
    runner.ensureYellow()
  }


  override def afterAll(): Unit = {
    runner.close()
    runner.clean()
  }


  "A Sample" must {
    "test with Elasticsearch" in {
      implicit val client = ElasticClient.fromNode(runner.getNode(0))
      assertClusterStatus()
      val f = client.execute { create index "places" } flatMap { result =>
        runner.ensureYellow("places")
        client.execute {
          index into "places" / "cities" id "uk" fields (
            "name" -> "London",
            "country" -> "United Kingdom",
            "continent" -> "Europe",
            "status" -> "Awesome"
            )
        }
      }
      val actual = f.await

      actual.getIndex mustBe "places"
      actual.getType mustBe "cities"
      actual.getId mustBe "uk"
    }
  }


  private def assertClusterStatus()(implicit client: ElasticClient) = {
    val f = client.execute(clusterHealth)
    val actual = f.await
    actual.getNumberOfNodes mustBe 1
  }

}

