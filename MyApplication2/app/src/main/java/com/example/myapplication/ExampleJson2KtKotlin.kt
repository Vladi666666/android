package com.example.myapplication

import com.google.gson.annotations.SerializedName

//Генерировал класс с помощью сервиса https://json2kt.com/

data class ExampleJson2KtKotlin (

  @SerializedName("data"  ) var data  : ArrayList<Track> = arrayListOf(),
  @SerializedName("total" ) var total : Int?            = null

)