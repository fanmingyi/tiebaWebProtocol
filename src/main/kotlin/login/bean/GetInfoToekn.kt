package login.bean

import com.google.gson.annotations.SerializedName


/**
 * @Author fmy
 * @Date 2019-05-24-09:48
 * @Email 635555698@qq.com
 * 使用gid获取tonken返回的信息
 */
data class GetInfoToekn(
    @SerializedName("data")
    var `data`: Data = Data(),
    @SerializedName("errInfo")
    var errInfo: ErrInfo = ErrInfo(),
    @SerializedName("traceid")
    var traceid: String = ""
) {
    data class ErrInfo(
        @SerializedName("no")
        var no: String = ""
    )

    data class Data(
        @SerializedName("codeString")
        var codeString: String = "",
        @SerializedName("cookie")
        var cookie: String = "",
        @SerializedName("disable")
        var disable: String = "",
        @SerializedName("loginrecord")
        var loginrecord: Loginrecord = Loginrecord(),
        @SerializedName("rememberedUserName")
        var rememberedUserName: String = "",
        @SerializedName("spLogin")
        var spLogin: String = "",
        @SerializedName("token")
        var token: String = "",
        @SerializedName("usernametype")
        var usernametype: String = ""
    ) {
        data class Loginrecord(
            @SerializedName("email")
            var email: List<Any> = listOf(),
            @SerializedName("phone")
            var phone: List<Any> = listOf()
        )
    }
}