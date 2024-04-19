package fr.vinetos.tranquille.presentation.denylist

data class DenylistItem(
    val id: Int,
    val name: String,
    val pattern: String,
    val valid: Boolean
    // TODO: Remove me a replace by the db one
) {
}