package base

/**
 * @Author fmy
 * @Date 2019-05-23-16:11
 * @Email 635555698@qq.com
 */

class MiTM : javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate>? {
        return null
    }

    fun isServerTrusted(
        certs: Array<java.security.cert.X509Certificate>
    ): Boolean {
        return true
    }

    fun isClientTrusted(
        certs: Array<java.security.cert.X509Certificate>
    ): Boolean {
        return true
    }

    @Throws(java.security.cert.CertificateException::class)
    override fun checkServerTrusted(
        certs: Array<java.security.cert.X509Certificate>, authType: String
    ) {
        return
    }

    @Throws(java.security.cert.CertificateException::class)
    override fun checkClientTrusted(
        certs: Array<java.security.cert.X509Certificate>, authType: String
    ) {
        return
    }
}