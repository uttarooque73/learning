package com.uttarooque73.netguard.features.adguard

data class AdBlockRule(val domain:String,val category:String,val enabled:Boolean=true)
data class AdBlockStats(val blockedRequests:Long=0,val blockedDomains:Long=0,val lastBlockedDomain:String?=null)

object AdBlockCatalog {
    val defaultRules=listOf(
        // Advertising
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
        AdBlockRule("pubmatic.com","Advertising"),
        AdBlockRule("rubiconproject.com","Advertising"),
        AdBlockRule("openx.net","Advertising"),
        AdBlockRule("adsafeprotected.com","Advertising"),
        AdBlockRule("amazon-adsystem.com","Advertising"),

        // Analytics
        AdBlockRule("google-analytics.com","Analytics"),
        AdBlockRule("analytics.google.com","Analytics"),
        AdBlockRule("app-measurement.com","Analytics"),
        AdBlockRule("mixpanel.com","Analytics"),
        AdBlockRule("amplitude.com","Analytics"),
        AdBlockRule("segment.io","Analytics"),
        AdBlockRule("segment.com","Analytics"),
        AdBlockRule("heap.io","Analytics"),
        AdBlockRule("hotjar.com","Analytics"),

        // Tracking / attribution
        AdBlockRule("facebook.net","Tracking"),
        AdBlockRule("connect.facebook.net","Tracking"),
        AdBlockRule("facebook.com","Tracking"),
        AdBlockRule("bat.bing.com","Tracking"),
        AdBlockRule("clarity.ms","Tracking"),
        AdBlockRule("branch.io","Tracking"),
        AdBlockRule("appsflyer.com","Tracking"),
        AdBlockRule("adjust.com","Tracking"),
        AdBlockRule("kochava.com","Tracking"),
        AdBlockRule("singular.net","Tracking"),

        // Native advertising / content recommendation
        AdBlockRule("taboola.com","Native Ads"),
        AdBlockRule("outbrain.com","Native Ads"),
        AdBlockRule("revcontent.com","Native Ads"),
        AdBlockRule("mgid.com","Native Ads"),

        // Common telemetry / crash analytics
        AdBlockRule("bugsnag.com","Telemetry"),
        AdBlockRule("sentry.io","Telemetry"),
        AdBlockRule("datadoghq.com","Telemetry")
    )

    fun matches(host:String,rule:AdBlockRule):Boolean{
        val h=host.trim('.').lowercase()
        val d=rule.domain.trim('.').lowercase()
        return h==d || h.endsWith("."+d)
    }
}
