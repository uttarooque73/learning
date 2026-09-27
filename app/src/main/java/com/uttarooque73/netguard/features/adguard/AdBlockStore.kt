package com.uttarooque73.netguard.features.adguard

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class AdBlockStore(context:Context){
    private val prefs=context.getSharedPreferences("ad_block_guard",Context.MODE_PRIVATE)
    fun enabled():Boolean=prefs.getBoolean("enabled",false)
    fun setEnabled(value:Boolean){prefs.edit().putBoolean("enabled",value).apply()}
    fun rules():List<AdBlockRule>{
        val raw=prefs.getString("rules",null)?:return AdBlockCatalog.defaultRules
        return runCatching{
            val a=JSONArray(raw)
            (0 until a.length()).map{val o=a.getJSONObject(it);AdBlockRule(o.getString("domain"),o.optString("category","Custom"),o.optBoolean("enabled",true))}
        }.getOrElse{AdBlockCatalog.defaultRules}
    }
    fun saveRules(rules:List<AdBlockRule>){
        val a=JSONArray();rules.forEach{a.put(JSONObject().put("domain",it.domain).put("category",it.category).put("enabled",it.enabled))}
        prefs.edit().putString("rules",a.toString()).apply()
    }
    fun stats():AdBlockStats=AdBlockStats(prefs.getLong("blockedRequests",0),prefs.getLong("blockedDomains",0),prefs.getString("lastBlockedDomain",null))
    fun recordBlocked(domain:String){
        val s=stats()
        prefs.edit().putLong("blockedRequests",s.blockedRequests+1).putLong("blockedDomains",s.blockedDomains+if(s.lastBlockedDomain==domain)0 else 1).putString("lastBlockedDomain",domain).apply()
    }
    fun resetStats(){prefs.edit().remove("blockedRequests").remove("blockedDomains").remove("lastBlockedDomain").apply()}
}
