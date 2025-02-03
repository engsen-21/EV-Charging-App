package smartcharge.master.data

import java.io.Serializable

data class Admin(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val password: String? = null
)

data class UserData(
    val id: String? = null,
    val name: String? = null,
    val userName: String? = null,
    val email: String? = null,
    val phoneNo: String? = null,
    val password: String? = null
)

data class Charger(
    val chargerId: String? = null,
    val userId: String? = null,
    val userName: String? = null,
    val phoneNo: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null,
    val chargerType: String? = null,
    val brandModel: String? = null,
    val powerRating: String? = null,
    val connectorType: String? = null,
    val availability: String? = null,
    val pricePerUnit: String? = null,
    val status: String? = null, // "pending", "approved", "rejected"
    val ownershipProofUrl: String? = null,
    val installationCertificateUrl: String? = null,
    val photoUrl: String? = null, // This can be nullable if the URL might be absent
    var comment: String? = null,
    var adminId: String? = null // Field to store the ID of the admin who took action
) : Serializable

data class Report(
    val addressName: String = "",
    val comment: String = "",
    val usage: String = "",
    val chargerType: String = "",
    val photoUrl: String = "",
    val userId: String = ""
)
