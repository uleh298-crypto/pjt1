package com.ssafy.ssabree.core.utils.model

class UserInfo(val userId: String) {

    var pw: String = ""
    var nickname: String = ""
    var role: String = ""

    var uId: Int = -1


    constructor(id: String, pw:String):this(id) {
        this.pw = pw
    }

    constructor( id: String, pw:String,nickname:String, role: String, uId: Int): this(id) {
        this.pw = pw
        this.nickname = nickname
        this.role = role
        this.uId = uId
    }
}