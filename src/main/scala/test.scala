import dbmcp.*
import cats.implicits.*
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.http4s.ember.client.EmberClientBuilder

def lookupById(set: String, num: Int): String =
  EmberClientBuilder.default[IO].build.use({ client =>
    ScryfallService(client).findBySet(set, num)  
  }).unsafeRunSync().show
