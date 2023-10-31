package com.example.enhancedclient.model

class User {
    var userId: String? = null
    var password: String? = null

    constructor(
        userId: String?,
        password: String?,
    ) {
        this.userId = userId
        this.password = password
    }

    constructor()
}