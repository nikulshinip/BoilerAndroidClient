package io.github.nikulshinip.cottage.obj

import java.net.HttpURLConnection
import java.net.URL

private const val AUTHORIZATION = "Authorization"
private const val AUTHORIZATION_TYPE = "Basic"

class Request(private val url:String,
              private var authorizationString: String = "") {

    private var requestMethod: String = "GET"
    private var parameters: Map<String, String>? = null

    constructor(url: String, authorizationString: String, parameters: Map<String, String>): this(url, authorizationString){
        requestMethod = "POST"
        this.parameters = parameters
    }

    fun connect():String{
        var response = ""
        try {
            val urlConnection = URL(url)
            val connection = urlConnection.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = this@Request.requestMethod
                setRequestProperty(AUTHORIZATION, "$AUTHORIZATION_TYPE $authorizationString")
                useCaches = false
            }

            if (parameters != null){

                var parStr = ""
                parameters?.forEach{ ( key, value) ->
                    parStr += "$key=$value&"
                }
                parStr = parStr.dropLast(1)

                connection.outputStream.apply {
                    write(parStr.toByteArray())
                    flush()
                }

            }

            response = connection.inputStream.bufferedReader().readLine()

        }catch (e:Exception) {
            response = "null"
        }
        return response
    }

}