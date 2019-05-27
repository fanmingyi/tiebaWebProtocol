package login

import base.MiTM
import com.google.gson.Gson
import login.bean.GetInfoToekn
import login.bean.RSAInfo
import login.bean.ViewLogInfo
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
    fun getTokenFromGID(
        cookies: Map<String, String>,
        gid: String
    ): Connection.Response {
        var time = System.currentTimeMillis()
        val getTokenkUrl =
            """https://passport.baidu.com/v2/api/?getapi&tpl=tb&apiver=v3&tt=$time&class=login&gid=$gid&loginversion=v4&logintype=dialogLogin&traceid=&callback="""
        val getTokenResponse = Jsoup
            .connect(getTokenkUrl)
            .ignoreContentType(true)
            .method(Connection.Method.GET)
            .cookies(cookies)
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


        val cookiesMap = mutableMapOf<String, String>()

        //账号
        val username = "caizhangtao99209@163.com"
//        val username = "123"

        //密码
        val pwd = "jatv8xc9b8"

        val gson = Gson()

        val gid = guideRandom()

        val dv = generatedDv()

        /***
         * ##########################################################
         *
         *
         *  （1）打开百度贴吧首页获取一些固定token
         *
         *
         * ##########################################################
         */

        //请求一：打开贴吧首页获取 一些固定cookies数值
        val openTiebaHomeResponse = openTiebaHome()
        //请求一 cookies 保存
        cookiesMap += openTiebaHomeResponse.cookies()


        /***
         * ##########################################################
         *
         *
         *  （2）使用gid获取用户token信息
         *
         *
         * ##########################################################
         */
        //请求二：通过gid获取用户信息 如token
        val tokenFromGIDResponse = getTokenFromGID(cookiesMap, gid)
        val infoToekn = gson.fromJson(tokenFromGIDResponse.body(), GetInfoToekn::class.java)
        //请求二 cookies保存
        cookiesMap += tokenFromGIDResponse.cookies()

        /***
         * ##########################################################
         *
         *
         *  （3）账号失去焦点触发的协议 ，用于检测账号是否需要输入验证码
         *
         *
         * ##########################################################
         */
        //请求三：账号输入框失去焦点的时候触发
        val loginCheckResponse =
            loginCheck(infoToekn, cookiesMap, dv, username)
        //请求三: cookies保存
        cookiesMap += loginCheckResponse.cookies()
        println("账号失去焦点:${loginCheckResponse.body()}")

        /***
         * ##########################################################
         *
         *
         *  （4）获取RSA公钥信息用户加密密码
         *
         *
         * ##########################################################
         */
        //请求四: 获取公钥
        val rsaPublickKeyResponse =
            getRSAPublickKey(
                token = infoToekn.data.token,
                gid = gid,
                cookiesMap = cookiesMap
            )
        val rsaInfo = gson.fromJson(rsaPublickKeyResponse.body(), RSAInfo::class.java)
        //请求四：保存cookies
        cookiesMap += rsaPublickKeyResponse.cookies()


        /***
         * ##########################################################
         *
         *
         *  （5）登录前发送一些用户信息给百度并接受,然后用返回的信息进行下一步登录
         *
         *
         * ##########################################################
         */
        //请求五:登录前viewLog
        val viewLogResponse = viewLogRequest(cookies = cookiesMap)
        //请求五:cookies保存
        cookiesMap += viewLogResponse.cookies()
        val viewLogInfo = gson.fromJson(viewLogResponse.body(), ViewLogInfo::class.java)


        /***
         * ##########################################################
         *
         *
         *  （6）递交登录信息
         *
         *
         * ##########################################################
         */
        //生成traceId
        val traceid = generatedTraceId()

        //加密密码
        val pwdEncrypt = pwdEncrypt(rsaInfo.pubkey, pwd)

        println("加密后的密码$pwdEncrypt")

        //提交登录
        val loginResponse = loginRequest(
            token = infoToekn.data.token,
            gid = gid,
            username = username,
            password = pwdEncrypt,
            rsakey = rsaInfo.key,
            ds = viewLogInfo.data.ds,
            tk = viewLogInfo.data.tk,
            dv = dv,
            traceid = traceid,
            cookiesMap = cookiesMap
        )


        println("loginResponse ${loginResponse.body()}")


    }


    fun generatedTraceId(): String {

        val cx = Context.enter()
        var rhinoScope = cx.initStandardObjects()
//        rhinoScope.put("encypteText", rhinoScope, msg)
        val encryptyJsFile = File("/Users/fmy/IdeaProjects/tieba/src/main/resources/baiduTraceId.js")
        var encryptyJs = encryptyJsFile.readText()
        val rhinoRel = cx.evaluateString(rhinoScope, encryptyJs, "<baiduGid>", 1, null)
        val stringrel = Context.jsToJava(rhinoRel, java.lang.String::class.java)

        Context.exit()

        return stringrel as String
    }


    fun pwdEncrypt(publicKey: String, pwd: String): String {
        val cx = Context.enter()
        var rhinoScope = cx.initStandardObjects()
        rhinoScope.put("publicKey", rhinoScope, publicKey)
        rhinoScope.put("mypwd", rhinoScope, pwd)
        val encryptyJsFile = File("/Users/fmy/IdeaProjects/tieba/src/main/resources/baiduLoginPwdEncrypt.js")
        var encryptyJs = encryptyJsFile.readText()
        val rhinoRel = cx.evaluateString(rhinoScope, encryptyJs, "<baiduLoginPwdEncrypt>", 1, null)
        val stringrel = Context.jsToJava(rhinoRel, java.lang.String::class.java)

        Context.exit()

        return stringrel as String
    }

    /**
     *
     * 登录请求
     */
    fun loginRequest(
        token: String,
        gid: String,
        username: String,
        password: String,
        rsakey: String,
        ds: String,
        tk: String,
        dv: String,
        traceid: String,
        callback: String = "",
        cookiesMap: Map<String, String>
    ): Connection.Response {
        var parameterMap = mapOf<String, String>(
            "staticpage" to "https://tieba.baidu.com/tb/static-common/html/pass/v3Jump.html",
            "charset" to "UTF-8",
            "token" to token,
            "tpl" to "tb",
            "subpro" to "",
            "apiver" to "v3",
            "tt" to System.currentTimeMillis().toString(),
            "codestring" to "",//验证码
            "safeflg" to "0",
            "u" to "https://tieba.baidu.com/",
            "isPhone" to "",
            "detect" to "1",
            "gid" to gid,
            "quick_user" to "0",
            "logintype" to "dialogLogin",
            "logLoginType" to "pc_loginDialog",
            "idc" to "",
            "loginmerge" to "true",
            "splogin" to "rate",
            "username" to username,
            "password" to password,
            "mem_pass" to "on",
            "rsakey" to rsakey,
            "crypttype" to "12",
            "ppui_logintime" to "429817",
            "countrycode" to "",
            "fp_uid" to "",
            "fp_info" to "",
            "loginversion" to "v4",
            "ds" to ds,
            "tk" to tk,
            "dv" to dv,
            "traceid" to traceid
//            "callback" to ""
        )
        val logincheckUrl = """https://passport.baidu.com/v2/api/?login"""

        val response = Jsoup
            .connect(logincheckUrl)
            .ignoreContentType(true)
            .data(parameterMap)
            .cookies(cookiesMap)
            .method(Connection.Method.POST)
            .execute()

        return response
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
    fun viewLogEncryptFS(): String {


        //当前时间戳
        var time = System.currentTimeMillis()

        var msg =
            """{"cl":[{"x":883,"y":150,"t":${time}},{"x":918,"y":234,"t":${time + 539}},{"x":898,"y":63,"t":${time + 1508}},{"x":904,"y":30,"t":${time + 1872}}],"mv":[{"fx":1008,"fy":20,"t":${time - 1293}},{"fx":946,"fy":246,"t":${time - 552}},{"fx":962,"fy":194,"t":${time - 395}},{"fx":915,"fy":151,"t":${time - 238}},{"fx":883,"fy":150,"t":${time + 160}},{"fx":916,"fy":225,"t":${time + 318}},{"fx":918,"fy":235,"t":${time + 911}},{"fx":897,"fy":250,"t":${time + 1067}},{"fx":901,"fy":128,"t":${time + 1225}},{"fx":898,"fy":63,"t":${time + 1690}},{"fx":904,"fy":31,"t":${time + 2095}},{"fx":906,"fy":46,"t":${time + 2267}},{"fx":909,"fy":59,"t":${time + 2424}},{"fx":914,"fy":71,"t":${time + 2582}},{"fx":914,"fy":76,"t":${time + 3175}},{"fx":917,"fy":164,"t":${time + 3332}}],"sc":[],"kb":[{"key":"a","t":${time + 2285}}],"cr":{"screenTop":23,"screenLeft":0,"clientWidth":1905,"clientHeight":257,"screenWidth":1920,"screenHeight":1080,"availWidth":1920,"availHeight":1008,"outerWidth":1920,"outerHeight":991,"scrollWidth":1905,"scrollHeight":1905},"ac_c":0}"""

        var cx = Context.enter()

        var rhinoScope = cx.initStandardObjects()

        rhinoScope.put("encypteText", rhinoScope, msg)

        var encryptyJsFile = File("/Users/fmy/IdeaProjects/tieba/src/main/resources/baiduEncyptFS.js")

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
    fun viewLogRequest(
        ak: String = "1e3f2dd1c81f2075171a547893391274",
        `as`: String = "6bffae1c",
        cookies: Map<String, String>
    ): Connection.Response {


        var fs = viewLogEncryptFS()

        var parameterMap = mapOf<String, String>(
            "ak" to ak,
            "as" to `as`,
            "fs" to fs,
            "v" to Math.floor(1e4 * Math.random() + 500).toString()
        )
        val logincheckUrl = """https://passport.baidu.com/viewlog"""

        val response = Jsoup
            .connect(logincheckUrl)
            .ignoreContentType(true)
            .data(parameterMap)
            .cookies(cookies)
            .method(Connection.Method.GET)
            .execute()

        return response
    }


}