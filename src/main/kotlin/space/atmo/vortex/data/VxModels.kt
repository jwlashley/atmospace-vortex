package space.atmo.vortex.data

import kotlinx.serialization.Serializable

@Serializable
data class VxEntry(val title: String, val count: Int)

@Serializable
data class VxCategory(val categoryName: String, val entries: List<VxEntry>)
