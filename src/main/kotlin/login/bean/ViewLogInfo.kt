package login.bean
import com.google.gson.annotations.SerializedName


/**

 * @Author fmy
 * @Date 2019-05-27-09:34
 * @Email 635555698@qq.com
 */

data class ViewLogInfo(
    @SerializedName("data")
    var `data`: Data = Data(),
    @SerializedName("code")
    var code: Int = 0
) {
    data class Data(
        @SerializedName("as")
        var asX: String = "",
        @SerializedName("ds")
        var ds: String = "",
        @SerializedName("tk")
        var tk: String = ""
    )
}