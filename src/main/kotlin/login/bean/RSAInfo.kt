package login.bean
import com.google.gson.annotations.SerializedName


/**

 * @Author fmy
 * @Date 2019-05-25-17:30
 * @Email 635555698@qq.com
 */

data class RSAInfo(
    @SerializedName("errno")
    var errno: String = "",
    @SerializedName("key")
    var key: String = "",
    @SerializedName("msg")
    var msg: String = "",
    @SerializedName("pubkey")
    var pubkey: String = "",
    @SerializedName("traceid")
    var traceid: String = ""
)