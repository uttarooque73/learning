package com.uttarooque73.netguard.features.adguard

data class AdBlockRule(val domain:String,val category:String,val enabled:Boolean=true)
data class AdBlockStats(val blockedRequests:Long=0,val blockedDomains:Long=0,val lastBlockedDomain:String?=null)

object AdBlockCatalog {
    val defaultRules=listOf(
        AdBlockRule("doubleclick.net","Advertising"),
        AdBlockRule("googlesyndication.com","Advertising"),
        AdBlockRule("googleadservices.com","Advertising"),
        AdBlockRule("adservice.google.com","Advertising"),
        AdBlockRule("ads.yahoo.com","Advertising"),
        AdBlockRule("adnxs.com","Advertising"),
        AdBlockRule("adsrvr.org","Advertising"),
        AdBlockRule("taboola.com","Advertising"),
        AdBlockRule("outbrain.com","Advertising"),
        AdBlockRule("criteo.com","Advertising"),
        AdBlockRule("app-measurement.com","Analytics"),
        AdBlockRule("facebook.net","Tracking"),
        AdBlockRule("connect.facebook.net","Tracking")
    )
    fun matches(host:String,rule:AdBlockRule):Boolean{
        val h=host.trim('.').lowercase()
        val d=rule.domain.trim('.').lowercase()
        return h==d || h.endsWith("."+d)
    }
}
