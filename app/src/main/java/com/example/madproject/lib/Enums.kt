package com.example.madproject.lib

enum class Requests(val value: Int)
{
    INTENT_CAPTURE_PHOTO(1),
    INTENT_PHOTO_FROM_GALLERY(2),
    RC_SIGN_IN (3);

    companion object {
        fun from(findValue: Int): Requests = Requests.values().first { it.value == findValue }
    }
}