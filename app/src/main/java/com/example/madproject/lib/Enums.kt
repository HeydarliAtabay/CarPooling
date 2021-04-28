package com.example.madproject.lib

enum class Requests(val value: Int)
{
    INTENT_CAPTURE_PHOTO(1),
    INTENT_PHOTO_FROM_GALLERY(2);

    companion object {
        fun from(findValue: Int): Requests = Requests.values().first { it.value == findValue }
    }
}

enum class ValueIds(val value: String) {
    FULL_NAME("group11.lab2.FULL_NAME"),
    NICKNAME("group11.lab2.NICKNAME"),
    EMAIL("group11.lab2.EMAIL"),
    LOCATION("group11.lab2.LOCATION"),
    PHONE_NUMBER("group11.lab2.PHONE_NUMBER"),
    DATE_OF_BIRTH("group11.lab2.DATE_OF_BIRTH"),
    CURRENT_PHOTO_PATH("group11.lab2.CURRENT_PHOTO_PATH"),
    JSON_OBJECT("group11.lab2.JSON_OBJECT"),
    JSON_OBJECT_TRIPS("group11.lab2.JSON_OBJECT_TRIPS");
}