package scripts.api

import scripts.api.model.BlogPost

import scala.concurrent.{Await, ExecutionContext, Future}

trait BlogPostsApi {
  this: BigcommerceApi =>
  val blogPath = "/v2/blog/posts"

  import spray.json.lenses.JsonLenses._
  private val IdLens: Lens[Seq] = "data" / * / "id"
  private val paginationLens: ScalarLens = "meta" / "pagination"
  private val blogPost: Lens[Seq] = "data" / *

  def getAllBlogPosts()(implicit ec: ExecutionContext): Future[Seq[BlogPost]] = {

    val blogs: Future[String] = for {
      blogs: String <- getApiPage(blogPath, None)
      blogsObjects = blogs.extract
    } yield {

      blogs
    }
    import scala.concurrent.duration._
    val resp = Await.result(blogs, 10 seconds)
    println(resp)
    // just temporary
    Future.successful(Seq.empty[BlogPost])
  }
}
