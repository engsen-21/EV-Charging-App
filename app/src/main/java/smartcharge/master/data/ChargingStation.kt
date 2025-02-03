package smartcharge.master.data

data class ChargingStation(
    val ID: Int, // Unique identifier for the station
    val UUID: String,
    val AddressInfo: AddressInfo, // Nested AddressInfo object containing details
    val Connections: List<Connection>,
    val MediaItems: List<MediaItem>?,
    val UsageCost: String?,
    val StatusType: StatusType?
)

// Data class to represent the address information of a charging station
data class AddressInfo(
    val Title: String, // Title of the charging station
    val AddressLine1: String, // Address line 1
    val Town: String, // Town or city
    val StateOrProvince: String, // State or province
    val CountryID: Int,
    val Postcode: String, // Postal code
    val Latitude: Double, // Latitude coordinate
    val Longitude: Double, // Longitude coordinate
    val AccessComments: String?
)

data class Connection(
    val ID: Int,
    val ConnectionTypeID: Int,
    val StatusTypeID: Int,
    val LevelID: Int,
    val ConnectionType: ConnectionType?,
    val Amps: Int?,
    val Voltage: Int?,
    val PowerKW: Double?,
    val Quantity: Int?
)

data class MediaItem(
    val ItemURL: String,
    val ItemThumbnailURL: String
)

data class StatusType(
    val IsOperational: Boolean
)

data class ConnectionType(
    val Title: String
)