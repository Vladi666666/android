package com.example.myapplication

import com.google.gson.annotations.SerializedName

//Генерировал класс с помощью сервиса https://json2kt.com/
data class Album (

  @SerializedName("id"           ) var id          : Int?    = null,
  @SerializedName("title"        ) var title       : String? = null,
  @SerializedName("cover"        ) var cover       : String? = null,
  @SerializedName("cover_small"  ) var coverSmall  : String? = null,
  @SerializedName("cover_medium" ) var coverMedium : String? = null,
  @SerializedName("cover_big"    ) var coverBig    : String? = null,
  @SerializedName("cover_xl"     ) var coverXl     : String? = null,
  @SerializedName("md5_image"    ) var md5Image    : String? = null,
  @SerializedName("tracklist"    ) var tracklist   : String? = null,
  @SerializedName("type"         ) var type        : String? = null

)
