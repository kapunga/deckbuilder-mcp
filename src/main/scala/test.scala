import dbmcp.*
import dbmcp.service.ScryfallService
import cats.implicits.*
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.http4s.ember.client.EmberClientBuilder

def lookupById(setId: SetId, setNum: SetNum): String =
  EmberClientBuilder.default[IO].build.use({ client =>
    ScryfallService(client).findBySet(setId, setNum)  
  }).unsafeRunSync().show

def lookupByName(name: String, setId: Option[SetId] = None, exact: Boolean = false): String =
  EmberClientBuilder.default[IO].build.use({ client =>
    ScryfallService(client).findByName(name, setId, exact)  
  }).unsafeRunSync().show

def search(query: String, limit: Int = 20): String =
  EmberClientBuilder.default[IO].build.use({ client =>
    ScryfallService(client).search(query, limit)
  }).unsafeRunSync().map(_.show).mkString("\n\n=======\n\n")
