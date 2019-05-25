package login

import base.MiTM
import com.google.gson.Gson
import login.bean.GetInfoToekn
import login.bean.RSAInfo
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.mozilla.javascript.Context
import java.io.File
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection


/**

 * @Author fmy
 * @Date 2019-05-24-09:53
 * @Email 635555698@qq.com
 */

object LoginHelper {


    init {
        /***
         * ##########################################################
         *
         *
         *  SSL配置
         *
         *
         * ##########################################################
         */
        val hv = HostnameVerifier { _, _ ->
            return@HostnameVerifier true
        }

        val trustAllCerts = arrayOfNulls<javax.net.ssl.TrustManager>(1)
        val tm = MiTM()
        trustAllCerts[0] = tm
        val sc = javax.net.ssl.SSLContext
            .getInstance("SSL")
        sc.init(null, trustAllCerts, null)
        HttpsURLConnection.setDefaultSSLSocketFactory(
            sc.socketFactory
        )
        HttpsURLConnection.setDefaultHostnameVerifier(hv)
    }


    /**
     * 打开贴吧首页
     */
    fun openTiebaHome(): Connection.Response {
        val tiebaBaseUrl = """https://tieba.baidu.com/"""
        val baseBaiduResponse = Jsoup
            .connect(tiebaBaseUrl)
            .ignoreContentType(true)
            .method(Connection.Method.GET)
            .execute()


        return baseBaiduResponse
    }


    /**
     *  使用gid获取token
     */
    fun getTokenFromGID(tiebaResponse: Connection.Response = openTiebaHome(), gid: String): Connection.Response {
        var time = System.currentTimeMillis()
        val getTokenkUrl =
            """https://passport.baidu.com/v2/api/?getapi&tpl=tb&apiver=v3&tt=$time&class=login&gid=$gid&loginversion=v4&logintype=dialogLogin&traceid=&callback="""
        val getTokenResponse = Jsoup
            .connect(getTokenkUrl)
            .ignoreContentType(true)
            .method(Connection.Method.GET)
            .cookies(tiebaResponse.cookies())
            .execute()

        return getTokenResponse

    }

    /**
     * 生成gid
     */
    fun guideRandom(): String {
        val cx = Context.enter()
        var rhinoScope = cx.initStandardObjects()
//        rhinoScope.put("encypteText", rhinoScope, msg)
        val encryptyJsFile = File("/Users/fmy/IdeaProjects/tieba/src/main/resources/baiduGid.js")
        var encryptyJs = encryptyJsFile.readText()
        val rhinoRel = cx.evaluateString(rhinoScope, encryptyJs, "<baiduGid>", 1, null)
        val stringrel = Context.jsToJava(rhinoRel, java.lang.String::class.java)

        Context.exit()

        return stringrel as String
    }

    /**
     * 生成dv参数
     */
    fun generatedDv(): String {
        val cx = Context.enter()
        var rhinoScope = cx.initStandardObjects()
//        rhinoScope.put("encypteText", rhinoScope, msg)
        val encryptyJsFile = File("/Users/fmy/IdeaProjects/tieba/src/main/resources/baiduDv.js")
        var encryptyJs = encryptyJsFile.readText()
        val rhinoRel = cx.evaluateString(rhinoScope, encryptyJs, "<baiduGid>", 1, null)
        val stringrel = Context.jsToJava(rhinoRel, java.lang.String::class.java)

        Context.exit()

        return stringrel as String
    }

    /**
     * 账号检测是否需要验证码
     */
    fun loginCheck(
        token: GetInfoToekn,
        cookiesMap: Map<String, String>,
        dv: String,
        username: String
    ): Connection.Response {

        var token = token.data.token

        var time = System.currentTimeMillis()


        val logincheckUrl =
            """https://passport.baidu.com/v2/api/?logincheck&token=$token&tpl=tb&apiver=v3&tt=$time&sub_source=leadsetpwd&username=$username&loginversion=v4&dv=$dv&traceid"""

        val response = Jsoup
            .connect(logincheckUrl)
            .ignoreContentType(true)
            .method(Connection.Method.GET)
            .cookies(cookiesMap)
            .execute()

        return response

    }

    fun login() {

        //账号
//        val username = "caizhangtao99209@163.com"
        val username = "123"

        //密码
        val pwd = "jatv8xc9b8"

        val gson = Gson()

        val gid = guideRandom()

        val dv = generatedDv()

        //打开贴吧首页获取 一些固定cookies数值
        val openTiebaHomeResponse = openTiebaHome()

        //通过gid获取用户信息 如token
        val tokenFromGIDResponse = getTokenFromGID(openTiebaHomeResponse, gid)
        val infoToekn = gson.fromJson(tokenFromGIDResponse.body(), GetInfoToekn::class.java)


        //账号输入框失去焦点的时候触发
        val loginCheckResponse =
            loginCheck(infoToekn, openTiebaHomeResponse.cookies() + tokenFromGIDResponse.cookies(), dv, username)

        println("账号失去焦点:${loginCheckResponse.body()}")

        //获取公钥
        val rsaPublickKeyResponse =
            getRSAPublickKey(token = infoToekn.data.token, gid = gid, cookiesMap = openTiebaHomeResponse.cookies())

        val rsaInfo = gson.fromJson(rsaPublickKeyResponse.body(), RSAInfo::class.java)

        println("获取公钥信息:${rsaInfo.pubkey}")


        viewLogRequest()
    }


    /**
     * 获取RSA公钥的信息
     */
    fun getRSAPublickKey(
        token: String,
        tpl: String = "tb",
        apiver: String = "v3",
        tt: String = System.currentTimeMillis().toString(),
        gid: String,
        loginversion: String = "v4",
        traceid: String = "",
        callback: String = "",
        cookiesMap: Map<String, String>
    ): Connection.Response {

        var parameterMap = mapOf(
            "token" to token,
            "tpl" to tpl,
            "apiver" to apiver,
            "tt" to tt,
            "gid" to gid,
            "loginversion" to loginversion,
            "traceid" to traceid,
            "callback" to callback
        )
        val publicKeyURL = """https://passport.baidu.com/v2/getpublickey"""

        val response = Jsoup
            .connect(publicKeyURL)
            .ignoreContentType(true)
            .data(parameterMap)
            .cookies(cookiesMap)
            .method(Connection.Method.GET)
            .execute()

        return response
    }


    /**
     * ViewLog 加密
     */
    fun viewLogEncrypt(): String {


        //当前时间戳
        var time = System.currentTimeMillis()

        var msg =
            """{"cl":[{"x":883,"y":150,"t":${time}},{"x":918,"y":234,"t":${time + 539}},{"x":898,"y":63,"t":${time + 1508}},{"x":904,"y":30,"t":${time + 1872}}],"mv":[{"fx":1008,"fy":20,"t":${time - 1293}},{"fx":946,"fy":246,"t":${time - 552}},{"fx":962,"fy":194,"t":${time - 395}},{"fx":915,"fy":151,"t":${time - 238}},{"fx":883,"fy":150,"t":${time + 160}},{"fx":916,"fy":225,"t":${time + 318}},{"fx":918,"fy":235,"t":${time + 911}},{"fx":897,"fy":250,"t":${time + 1067}},{"fx":901,"fy":128,"t":${time + 1225}},{"fx":898,"fy":63,"t":${time + 1690}},{"fx":904,"fy":31,"t":${time + 2095}},{"fx":906,"fy":46,"t":${time + 2267}},{"fx":909,"fy":59,"t":${time + 2424}},{"fx":914,"fy":71,"t":${time + 2582}},{"fx":914,"fy":76,"t":${time + 3175}},{"fx":917,"fy":164,"t":${time + 3332}}],"sc":[],"kb":[{"key":"a","t":${time+2285}}],"cr":{"screenTop":23,"screenLeft":0,"clientWidth":1905,"clientHeight":257,"screenWidth":1920,"screenHeight":1080,"availWidth":1920,"availHeight":1008,"outerWidth":1920,"outerHeight":991,"scrollWidth":1905,"scrollHeight":1905},"ac_c":0}"""

        var cx = Context.enter()

        var rhinoScope = cx.initStandardObjects()

        rhinoScope.put("encypteText", rhinoScope, msg)

        var encryptyJsFile = File("/Users/fmy/IdeaProjects/tieba/src/main/resources/baiduEncypt.js")

        var encryptyJs = encryptyJsFile.readText()

        var rhinoRel = cx.evaluateString(rhinoScope, encryptyJs, "<eastEncrypt>", 1, null)

        var stringrel = Context.jsToJava(rhinoRel, java.lang.String::class.java)

        Context.exit()

        return stringrel as String
    }

    /**
     *
     * 登录时发送用户轨迹信息
     *
     */
    fun viewLogRequest(): Connection.Response {

        var parameterMap = mapOf<String, String>(
            "ak" to "1e3f2dd1c81f2075171a547893391274",
            "as" to "6bffae1c",
            "fs" to "f8Xbc3QP44yMDqn6JgcisTksIqj3n7F03pE1Kdr0yY7CwuerumBcUCISR8i2Fq8jVUb6Or3T0um7lrxaYBNCHyONH1o9QqKY9LFmiTtvEBZGviAfhquNj+ZwfPTjoImMDdUrpzzDKeuI0dLtiA6baQ6+cDmjtfizP8cDiopyjFrjV+k1RjPGuQGAqg3RkYLcKI9mQRTWpYnQebY/8a9Q96sEgMD775JMRQnhSKgNTHSQuLRp+AXd1F6rcJnRpT6BK0it8IHJTxcsLq8yIdhz4FacGfOqxW0X/SxUyjoM2aAvQ7N0cbahXZ/nu+XdjtlZ9aTk9xRUyqIYb+HGKf1NWk86csWEUZpoynYUcYUpasW3h5fJ5gEJWUklndS8TcDlCSZq+iXX+QWiS2Y0bEIxm/E6IzjYeosJho5AcCaxCFurwXDfRIPbU1CjarYr0M+6j71MfggMU3tUMoBxq8L946Ymdvu2BXeM1vLDbNKmZPcjvvqxITFggURMVumffHWR5H+XSi6hmyFZWQTmZeLqkABT8VikrmStwl5DSzLWWRMxCoV2d+IZ0jtRfniHnkJ05qSi3hUZRCEiKG15HTxQ41zQOahzrmo9EpgIuk8+ka0IEBozfgAD0r28jcLuDyAN2T4su19VYgoAwkgp+29N7WDkNwFbH39bKKxIwXh4HUPmwcTTo1MpvTELBSI1LfIqIAMHrm/C624aeD+GNiJReEcjHmMz8k3L0l0U8eUXEnJsA3dZkSnHkNc/DE/2dv/aGYMWrQfFCKKenaTd0m31KSlIRMIOV+AQZUTrx2OK80Iynk5GBREdPB7gySLuLkqIh/GJv6JbWk2GpxIJmRwL2XArAlbDWKmNqoIwjczwfzNE0r7BldJbCVH94YYMYKK/MjUEOyVcekJqrHcRgmNC9A==",
            "v" to "2020"
        )
        val logincheckUrl = """https://passport.baidu.com/viewlog"""

        val response = Jsoup
            .connect(logincheckUrl)
            .ignoreContentType(true)
            .data(parameterMap)
            .method(Connection.Method.GET)
            .execute()

        return response
    }


}