package com.example.myapplication

import com.google.gson.annotations.SerializedName

//Генерировал класс с помощью сервиса https://json2kt.com/
data class Artist (

  @SerializedName("id"             ) var id            : Int?     = null,
  @SerializedName("name"           ) var name          : String?  = null,
  @SerializedName("link"           ) var link          : String?  = null,
  @SerializedName("picture"        ) var picture       : String?  = null,
  @SerializedName("picture_small"  ) var pictureSmall  : String?  = null,
  @SerializedName("picture_medium" ) var pictureMedium : String?  = null,
  @SerializedName("picture_big"    ) var pictureBig    : String?  = null,
  @SerializedName("picture_xl"     ) var pictureXl     : String?  = null,
  @SerializedName("radio"          ) var radio         : Boolean? = null,
  @SerializedName("tracklist"      ) var tracklist     : String?  = null,
  @SerializedName("type"           ) var type          : String?  = null

)

