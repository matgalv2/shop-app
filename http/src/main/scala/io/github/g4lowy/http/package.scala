package io.github.g4lowy

import io.github.g4lowy.http.api.{CustomerApi, OrderApi, ProductApi}
import zio.&

package object http {

  type AppEnvironment = CustomerApi.Environment & ProductApi.Environment & OrderApi.Environment

}
