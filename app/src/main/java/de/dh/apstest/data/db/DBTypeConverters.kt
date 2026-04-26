package de.dh.apstest.data.db

import androidx.room.TypeConverter
import org.json.JSONArray
import org.json.JSONObject

@TypeConverter
fun fromListOfBlocks(blocks: List<Block>?): String? {
    if (blocks == null) return null
    val jsonArray = JSONArray()
    blocks.forEach {
        val jsonObject = JSONObject()
        jsonObject.put("duration", it.duration)
        jsonObject.put("amount", it.amount)
        jsonArray.put(jsonObject)
    }
    return jsonArray.toString()
}

@TypeConverter
fun toListOfBlocks(jsonString: String?): List<Block>? {
    if (jsonString == null) return null
    val jsonArray = JSONArray(jsonString)
    val list = mutableListOf<Block>()
    for (i in 0 until jsonArray.length()) {
        val jsonObject = jsonArray.getJSONObject(i)
        list.add(Block(jsonObject.getLong("duration"), jsonObject.getDouble("amount")))
    }
    return list
}

@TypeConverter
fun fromListOfTargetBlocks(blocks: List<TargetBlock>?): String? {
    if (blocks == null) return null
    val jsonArray = JSONArray()
    blocks.forEach {
        val jsonObject = JSONObject()
        jsonObject.put("duration", it.duration)
        jsonObject.put("lowTarget", it.lowTarget)
        jsonObject.put("highTarget", it.highTarget)
        jsonArray.put(jsonObject)
    }
    return jsonArray.toString()
}

@TypeConverter
fun toListOfTargetBlocks(jsonString: String?): List<TargetBlock>? {
    if (jsonString == null) return null
    val jsonArray = JSONArray(jsonString)
    val list = mutableListOf<TargetBlock>()
    for (i in 0 until jsonArray.length()) {
        val jsonObject = jsonArray.getJSONObject(i)
        list.add(
            TargetBlock(
                jsonObject.getLong("duration"),
                jsonObject.getDouble("lowTarget"),
                jsonObject.getDouble("highTarget")
            )
        )
    }
    return list
}