package com.myapps.pacman.levels

import android.content.Context
import com.myapps.pacman.utils.Position
import org.json.JSONObject

class JsonMapProvider(private val context: Context) : MapProvider {

    override fun getMaps(): Map<Int, LevelStartData> {
        val mapsJson = loadJsonFromAsset("levels_maps.json", context)
        val levelsJson = loadJsonFromAsset("levels_data.json", context)

        if (mapsJson == null || levelsJson == null) return emptyMap()

        val listsOfMaps = parseMapsJson(mapsJson)
        val listOfLevelDefaults = parseLevelsJson(levelsJson)

        val mapOfLevels = mutableMapOf<Int, LevelStartData>()

        for ((index, mapLayouts) in listsOfMaps) {
            val levelData = listOfLevelDefaults.getOrNull(index)

            if (levelData != null) {
                val updatedLevelData = levelData.copy(
                    mapCharData = mapLayouts
                )
                mapOfLevels[index] = updatedLevelData
            }
        }

        return mapOfLevels
    }


    private fun loadJsonFromAsset(fileName: String, context: Context): String? {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ex: Exception) {
            null
        }
    }


    private fun parseMapsJson(jsonString: String?): Map<Int, List<String>> {
        if (jsonString == null) return emptyMap()
        val maps = mutableMapOf<Int, List<String>>()
        val jsonObject = JSONObject(jsonString)
        val mapsJsonArray = jsonObject.getJSONArray("maps")
        for (i in 0 until mapsJsonArray.length()) {
            val mapJsonData = mapsJsonArray.getJSONObject(i)
            val layoutArray = mapJsonData.getJSONArray("layout")
            val layoutList = mutableListOf<String>()
            for (j in 0 until layoutArray.length()) {
                layoutList.add(layoutArray.getString(j))
            }
            maps[i] = layoutList
        }
        return maps
    }

    private fun parseLevelsJson(jsonString: String?): List<LevelStartData> {
        if (jsonString == null) return emptyList()
        val listOfLevelsDefaults = mutableListOf<LevelStartData>()
        val jsonObject = JSONObject(jsonString)
        val levelsJson = jsonObject.getJSONArray("levels")
        for (i in 0 until levelsJson.length()) {
            val levelJson = levelsJson.getJSONObject(i)
            val levelStartData = LevelStartData(
                pacmanDefaultPosition = parsePosition(levelJson.getJSONObject("pacmanDefaultPosition")),
                blinkyDefaultPosition = parsePosition(levelJson.getJSONObject("blinkyDefaultPosition")),
                inkyDefaultPosition = parsePosition(levelJson.getJSONObject("inkyDefaultPosition")),
                pinkyDefaultPosition = parsePosition(levelJson.getJSONObject("pinkyDefaultPosition")),
                clydeDefaultPosition = parsePosition(levelJson.getJSONObject("clydeDefaultPosition")),
                homeTargetPosition = parsePosition(levelJson.getJSONObject("homeTargetPosition")),
                ghostHomeXRange = levelJson.getJSONArray("ghostHomeXRange").let {
                    IntRange(it.getInt(0), it.getInt(it.length() - 1))
                },
                ghostHomeYRange = levelJson.getJSONArray("ghostHomeYRange").let {
                    IntRange(it.getInt(0), it.getInt(it.length() - 1))
                },
                blinkyScatterPosition = parsePosition(levelJson.getJSONObject("blinkyScatterPosition")),
                inkyScatterPosition = parsePosition(levelJson.getJSONObject("inkyScatterPosition")),
                pinkyScatterPosition = parsePosition(levelJson.getJSONObject("pinkyScatterPosition")),
                clydeScatterPosition = parsePosition(levelJson.getJSONObject("clydeScatterPosition")),
                doorTarget = parsePosition(levelJson.getJSONObject("doorTarget")),
                width = levelJson.getInt("width"),
                height = levelJson.getInt("height"),
                amountOfFood = levelJson.getInt("amountOfFood"),
                blinkySpeedDelay = levelJson.getInt("blinkySpeedDelay"),
                isBell = levelJson.getBoolean("isBell")
            )
            listOfLevelsDefaults.add(levelStartData)
        }
        return listOfLevelsDefaults
    }

    private fun parsePosition(jsonObject: JSONObject): Position {
        return Position(
            positionX = jsonObject.getInt("x"),
            positionY = jsonObject.getInt("y")
        )
    }
}