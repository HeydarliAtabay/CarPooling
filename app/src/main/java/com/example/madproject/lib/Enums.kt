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
    TRIP_DEPARTURE("group11.lab2.TRIP_DEPARTURE"),
    TRIP_ARRIVAL("group11.lab2.TRIP_ARRIVAL"),
    TRIP_DATE("group11.lab2.TRIP_DATE"),
    TRIP_TIME("group11.lab2.TRIP_TIME"),
    TRIP_DURATION("group11.lab2.TRIP_DURATION"),
    TRIP_AVAILABLE_SEATS("group11.lab2.TRIP_AVAILABLE_SEATS"),
    TRIP_PRICE("group11.lab2.TRIP_PRICE"),
    TRIP_ADDITIONAL_INFO("group11.lab2.TRIP_ADDITIONAL_INFO"),
    TRIP_INTERMEDIATE_STOP("group11.lab2.TRIP_INTERMEDIATE_STOP"),
    JSON_OBJECT("group11.lab1.JSON_OBJECT"),
    JSON_OBJECT_TRIP("group11.lab1.JSON_OBJECT_TRIP");

}