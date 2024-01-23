/**
 * Copyright © 2023 Apple Inc. and the Pkl project authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pkl.commons.test

import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpsConfigurator
import com.sun.net.httpserver.HttpsParameters
import com.sun.net.httpserver.HttpsServer
import java.net.BindException
import java.net.InetSocketAddress
import java.nio.file.*
import java.security.KeyStore
import java.util.concurrent.Executors
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import kotlin.io.path.isRegularFile
import org.pkl.commons.createParentDirectories
import org.pkl.commons.deleteRecursively

object PackageServer {
  private val keystore = javaClass.getResource("/localhost.p12")!!

  // When tests are run via Gradle (i.e. from ./gradlew check), resources are packaged into a jar.
  // When run directly in IntelliJ, resources are just directories.
  private val packagesDir: Path = let {
    val uri = javaClass.getResource("packages")!!.toURI()
    try {
      Path.of(uri)
    } catch (e: FileSystemNotFoundException) {
      FileSystems.newFileSystem(uri, mapOf<String, String>())
      Path.of(uri)
    }
  }

  fun populateCacheDir(cacheDir: Path) {
    val basePath = cacheDir.resolve("package-1/localhost:$PORT")
    basePath.deleteRecursively()
    Files.walk(packagesDir).use { stream ->
      stream.forEach { source ->
        if (!source.isRegularFile()) return@forEach
        val relativized =
          source.toString().replaceFirst(packagesDir.toString(), "").drop(1).ifEmpty {
            return@forEach
          }
        val dest = basePath.resolve(relativized)
        dest.createParentDirectories()
        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING)
      }
    }
  }

  private const val PORT = 12110

  private val sslContext by lazy {
    SSLContext.getInstance("SSL").apply {
      val pass = "password".toCharArray()
      val ks = KeyStore.getInstance("PKCS12").apply { load(keystore.openStream(), pass) }
      val kmf = KeyManagerFactory.getInstance("SunX509").apply { init(ks, pass) }
      init(kmf.keyManagers, null, null)
    }
  }

  private val engine by lazy { sslContext.createSSLEngine() }

  private val simpleHttpsConfigurator =
    object : HttpsConfigurator(sslContext) {
      override fun configure(params: HttpsParameters) {
        params.needClientAuth = false
        params.cipherSuites = engine.enabledCipherSuites
        params.protocols = engine.enabledProtocols
        params.setSSLParameters(sslContext.supportedSSLParameters)
      }
    }

  private val handler = HttpHandler { exchange ->
    assert(exchange.requestMethod == "GET")
    val path = exchange.requestURI.path
    val localPath =
      if (path.endsWith(".zip")) packagesDir.resolve(path.drop(1))
      else packagesDir.resolve("${path.drop(1)}${path}.json")
    if (Files.exists(localPath)) {
      exchange.sendResponseHeaders(200, Files.size(localPath))
      exchange.responseBody.use { outputStream -> Files.copy(localPath, outputStream) }
    } else {
      exchange.sendResponseHeaders(404, 0)
      exchange.close()
    }
  }

  private val myExecutor = Executors.newFixedThreadPool(1)

  private val server by lazy {
    HttpsServer.create().apply {
      httpsConfigurator = simpleHttpsConfigurator
      createContext("/", handler)
      executor = myExecutor
    }
  }

  fun ensureStarted() =
    synchronized(this) {
      try {
        server.bind(InetSocketAddress(PORT), 0)
        server.start()
      } catch (_: BindException) {}
    }
}
