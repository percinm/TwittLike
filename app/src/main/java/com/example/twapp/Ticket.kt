package com.example.twapp

class Ticket {

    var tweetId:String?=null
    var tweetText:String?=null
    var tweetImageURL:String?=null
    var tweetPersonUID:String?=null
    var tweetDate:String?=null

    constructor(tweetId:String, tweetText:String, tweetImageURL:String, tweetPersonUID:String, tweetDate:String){
        this.tweetId=tweetId
        this.tweetText=tweetText
        this.tweetImageURL=tweetImageURL
        this.tweetPersonUID=tweetPersonUID
        this.tweetDate=tweetDate
    }
}