package com.example.twapp

class PostInfo {
    var userUID: String? =null
    var text: String? =null
    var postImage: String
    var date: String? =null

    constructor(userUID: String, text: String, postImage: String, date: String){
        this.userUID = userUID
        this.text = text
        this.postImage = postImage
        this.date = date
    }
}