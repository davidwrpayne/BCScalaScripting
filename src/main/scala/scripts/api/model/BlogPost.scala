package scripts.api.model

case class BlogPost(
                      id: Option[Int],
                      title: String,
                      url: Option[String],
                      previewUrl: Option[String],
                      body: String,
                      tags: Option[Seq[String]],
                      summary: Option[String],
                      isPublished: Option[Boolean],
                      publishedDate: Option[PublishedDate],
                      publishedDateIso8601: Option[String],
                      metaDescription: Option[String],
                      metaKeywords: Option[String],
                      author: Option[String],
                      thumbnailPath: Option[String]
                    )

object BlogPost {

}

case class PublishedDate()