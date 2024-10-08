package io.github.g4lowy

import io.github.g4lowy.http.api.{ ClientApi, ProductApi }
import zio.&

package object http {

  type AppEnvironment = ClientApi.Environment & ProductApi.Environment

}
