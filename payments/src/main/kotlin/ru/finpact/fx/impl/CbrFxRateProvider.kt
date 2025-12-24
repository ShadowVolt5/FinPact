package ru.finpact.fx.impl

import org.w3c.dom.Element
import ru.finpact.fx.FxRateProvider
import java.math.BigDecimal
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference
import javax.xml.parsers.DocumentBuilderFactory

class CbrFxRateProvider(
    private val ttl: Duration = Duration.ofMinutes(10)
) : FxRateProvider {

    private val http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(3))
        .build()

    private data class Cache(
        val loadedAt: Instant,
        val rates: Map<String, BigDecimal>
    )

    private val cacheRef = AtomicReference<Cache?>(null)

    override fun rubPerUnit(currency: String): BigDecimal {
        val code = currency.trim().uppercase()
        if (code == "RUB") return BigDecimal.ONE

        val cache = cacheRef.get()
        val now = Instant.now()

        val rates = if (cache == null || now.isAfter(cache.loadedAt.plus(ttl))) {
            val fresh = loadRates()
            cacheRef.set(Cache(now, fresh))
            fresh
        } else {
            cache.rates
        }

        return rates[code]
            ?: throw IllegalStateException("FX rate for $code not found in CBR feed")
    }

    private fun loadRates(): Map<String, BigDecimal> {
        val url = URI.create("https://www.cbr.ru/scripts/XML_daily.asp")

        val req = HttpRequest.newBuilder()
            .uri(url)
            .timeout(Duration.ofSeconds(5))
            .GET()
            .build()

        val res = http.send(req, HttpResponse.BodyHandlers.ofString())
        if (res.statusCode() !in 200..299) {
            throw IllegalStateException("CBR rate feed HTTP ${res.statusCode()}")
        }

        val xml = res.body()

        val dbf = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = false
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        }

        val doc = dbf.newDocumentBuilder()
            .parse(xml.byteInputStream())

        val list = doc.getElementsByTagName("Valute")
        val out = HashMap<String, BigDecimal>(list.length)

        for (i in 0 until list.length) {
            val el = list.item(i) as? Element ?: continue

            val charCode = el.getElementsByTagName("CharCode").item(0)?.textContent
                ?.trim()?.uppercase()
                ?: continue

            val nominalStr = el.getElementsByTagName("Nominal").item(0)?.textContent?.trim()
                ?: continue

            val valueStr = el.getElementsByTagName("Value").item(0)?.textContent?.trim()
                ?: continue

            val nominal = nominalStr.toIntOrNull() ?: continue
            val value = valueStr.replace(',', '.').toBigDecimalOrNull() ?: continue

            val perUnit = value.divide(BigDecimal(nominal), 10, java.math.RoundingMode.HALF_UP)
            out[charCode] = perUnit
        }

        return out
    }
}
