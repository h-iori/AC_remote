package com.ioristudios.ac.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ioristudios.ac.model.AcMode
import com.ioristudios.ac.model.AcState
import com.ioristudios.ac.model.FanSpeed
import com.ioristudios.ac.model.ProtocolType
import com.ioristudios.ac.model.SwingVMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "iori_ac_preferences")

/**
 * Repository for storing and retrieving AC remote settings persistently.
 */
class AcStateRepository(private val context: Context) {

    private object Keys {
        val POWER = booleanPreferencesKey("power")
        val TEMP = intPreferencesKey("temperature")
        val MODE = stringPreferencesKey("mode")
        val FAN = stringPreferencesKey("fan_speed")
        val SWING_V = stringPreferencesKey("swing_v")
        val SWING_H = booleanPreferencesKey("swing_h")
        val TURBO = booleanPreferencesKey("turbo")
        val LIGHT = booleanPreferencesKey("light")
        val XFAN = booleanPreferencesKey("xfan")
        val ION_FILTER = booleanPreferencesKey("ion_filter")
        val QUIET = booleanPreferencesKey("quiet")
        val PROTOCOL = stringPreferencesKey("protocol")
        val FREQUENCY = intPreferencesKey("carrier_frequency")
    }

    val stateFlow: Flow<AcState> = context.dataStore.data.map { prefs ->
        val modeStr = prefs[Keys.MODE] ?: AcMode.COOL.name
        val fanStr = prefs[Keys.FAN] ?: FanSpeed.AUTO.name
        val swingVStr = prefs[Keys.SWING_V] ?: SwingVMode.AUTO.name
        val protoStr = prefs[Keys.PROTOCOL] ?: ProtocolType.KELVINATOR_16BYTE.name

        AcState(
            power = prefs[Keys.POWER] ?: true,
            targetTemperature = prefs[Keys.TEMP] ?: 24,
            mode = try { AcMode.valueOf(modeStr) } catch (_: Exception) { AcMode.COOL },
            fanSpeed = try { FanSpeed.valueOf(fanStr) } catch (_: Exception) { FanSpeed.AUTO },
            swingVertical = try { SwingVMode.valueOf(swingVStr) } catch (_: Exception) { SwingVMode.AUTO },
            swingHorizontal = prefs[Keys.SWING_H] ?: false,
            turbo = prefs[Keys.TURBO] ?: false,
            light = prefs[Keys.LIGHT] ?: true,
            xFan = prefs[Keys.XFAN] ?: false,
            ionFilter = prefs[Keys.ION_FILTER] ?: false,
            quiet = prefs[Keys.QUIET] ?: false,
            selectedProtocol = try { ProtocolType.valueOf(protoStr) } catch (_: Exception) { ProtocolType.KELVINATOR_16BYTE },
            carrierFrequencyHz = prefs[Keys.FREQUENCY] ?: 38000
        )
    }

    suspend fun saveState(state: AcState) {
        context.dataStore.edit { prefs ->
            prefs[Keys.POWER] = state.power
            prefs[Keys.TEMP] = state.targetTemperature
            prefs[Keys.MODE] = state.mode.name
            prefs[Keys.FAN] = state.fanSpeed.name
            prefs[Keys.SWING_V] = state.swingVertical.name
            prefs[Keys.SWING_H] = state.swingHorizontal
            prefs[Keys.TURBO] = state.turbo
            prefs[Keys.LIGHT] = state.light
            prefs[Keys.XFAN] = state.xFan
            prefs[Keys.ION_FILTER] = state.ionFilter
            prefs[Keys.QUIET] = state.quiet
            prefs[Keys.PROTOCOL] = state.selectedProtocol.name
            prefs[Keys.FREQUENCY] = state.carrierFrequencyHz
        }
    }
}
