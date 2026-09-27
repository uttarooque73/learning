package com.uttarooque73.netguard.features.securitycenter

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

data class IntegrityEvent(val timestamp:Long,val title:String,val detail:String,val hash:String,val previousHash:String)

class TamperEvidentSecurityHistory(context:Context) {
    private val prefs=context.getSharedPreferences("netguard_integrity_history",Context.MODE_PRIVATE)
    fun append(title:String,detail:String,timestamp:Long=System.currentTimeMillis()):IntegrityEvent {
        val list=load().toMutableList(); val previous=list.lastOrNull()?.hash.orEmpty()
        val hash=sha256(previous+"|"+timestamp+"|"+title+"|"+detail)
        val e=IntegrityEvent(timestamp,title,detail,hash,previous); list.add(e)
        val a=JSONArray(); list.takeLast(500).forEach{a.put(JSONObject().apply{put("timestamp",it.timestamp);put("title",it.title);put("detail",it.detail);put("hash",it.hash);put("previousHash",it.previousHash)})}
        prefs.edit().putString("events",a.toString()).apply(); return e
    }
    fun load():List<IntegrityEvent> = runCatching {
        val a=JSONArray(prefs.getString("events","[]")); List(a.length()){i->val j=a.getJSONObject(i);IntegrityEvent(j.getLong("timestamp"),j.getString("title"),j.getString("detail"),j.getString("hash"),j.optString("previousHash"))}
    }.getOrDefault(emptyList())
    fun verify():Boolean { val list=load(); var previous=""; return list.all{val expected=sha256(previous+"|"+it.timestamp+"|"+it.title+"|"+it.detail); val ok=expected==it.hash&&it.previousHash==previous; previous=it.hash;ok} }
    private fun sha256(v:String)=MessageDigest.getInstance("SHA-256").digest(v.toByteArray()).joinToString(""){"%02x".format(it)}
}
