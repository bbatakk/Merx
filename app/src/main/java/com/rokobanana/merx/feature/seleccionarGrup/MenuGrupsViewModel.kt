package com.rokobanana.merx.feature.seleccionarGrup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.rokobanana.merx.domain.model.Grup
import com.rokobanana.merx.domain.model.Membre
import com.rokobanana.merx.domain.model.RolMembre
import com.rokobanana.merx.domain.repository.MembresRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class MenuGrupsViewModel @Inject constructor(
    private val membresRepository: MembresRepository,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private val _grups = MutableStateFlow<List<Grup>>(emptyList())
    val grups: StateFlow<List<Grup>> = _grups

    private val _errorText = MutableStateFlow<String?>(null)
    val errorText: StateFlow<String?> = _errorText

    // UI states
    val nouNomGrup = MutableStateFlow("")
    val showDialog = MutableStateFlow(false)
    val grupNoExisteix = MutableStateFlow(false)
    val showKeyDialog = MutableStateFlow(false)
    val groupIdToJoin = MutableStateFlow<String?>(null)
    val inputKey = MutableStateFlow("")
    val errorKey = MutableStateFlow<String?>(null)
    val novaClau = MutableStateFlow("")
    val grupADesvincular = MutableStateFlow<Grup?>(null)
    val grupAEditar = MutableStateFlow<Grup?>(null)
    val editNomGrup = MutableStateFlow("")
    val editClauGrup = MutableStateFlow("")

    val user get() = auth.currentUser

    init {
        carregarGrupsUsuari()
    }

    fun carregarGrupsUsuari() {
        viewModelScope.launch {
            _loading.value = true
            val userId = user?.uid ?: return@launch
            val usuariDoc = db.collection("usuaris").document(userId).get().await()
            val grupIds = (usuariDoc.get("grups") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            val grupsList = mutableListOf<Grup>()
            for (grupId in grupIds) {
                val doc = db.collection("grups").document(grupId).get().await()
                val grup = doc.toObject(Grup::class.java)?.copy(id = doc.id)
                if (grup != null) grupsList.add(grup)
            }
            _grups.value = grupsList
            _loading.value = false
        }
    }

    fun buscarGrupONou() {
        _errorText.value = null
        grupNoExisteix.value = false
        showDialog.value = false

        viewModelScope.launch {
            val result = db.collection("grups")
                .whereEqualTo("nom", nouNomGrup.value.trim())
                .get().await()
            if (!result.isEmpty) {
                groupIdToJoin.value = result.documents.first().id
                showKeyDialog.value = true
            } else {
                grupNoExisteix.value = true
                showDialog.value = true
            }
        }
    }

    fun unirAGrup(onSuccess: (String) -> Unit) {
        val groupId = groupIdToJoin.value ?: return
        val userId = user?.uid ?: return
        viewModelScope.launch {
            val grupDoc = db.collection("grups").document(groupId).get().await()
            val clau = grupDoc.getString("clau") ?: ""
            if (inputKey.value == clau) {
                db.collection("usuaris").document(userId)
                    .update("grups", FieldValue.arrayUnion(groupId)).await()
                membresRepository.afegirMembre(
                    Membre(
                        usuariId = userId,
                        grupId = groupId,
                        rol = RolMembre.MEMBRE
                    )
                )
                showKeyDialog.value = false
                inputKey.value = ""
                groupIdToJoin.value = null
                carregarGrupsUsuari()
                onSuccess(groupId)
            } else {
                errorKey.value = "Clau incorrecta."
            }
        }
    }

    fun crearGrup(onSuccess: (String) -> Unit) {
        val userId = user?.uid ?: return
        viewModelScope.launch {
            val nouGrup = hashMapOf(
                "nom" to nouNomGrup.value.trim(),
                "clau" to novaClau.value
            )
            val grupRef = db.collection("grups").document()
            grupRef.set(nouGrup).await()
            db.collection("usuaris").document(userId)
                .update("grups", FieldValue.arrayUnion(grupRef.id)).await()
            membresRepository.afegirMembre(
                Membre(
                    usuariId = userId,
                    grupId = grupRef.id,
                    rol = RolMembre.ADMIN
                )
            )
            showDialog.value = false
            carregarGrupsUsuari()
            onSuccess(grupRef.id)
        }
    }

    fun desvincularGrup(grup: Grup, onSuccess: () -> Unit) {
        val userId = user?.uid ?: return
        viewModelScope.launch {
            db.collection("usuaris").document(userId)
                .update("grups", FieldValue.arrayRemove(grup.id)).await()
            membresRepository.eliminaMembre(userId, grup.id)
            grupADesvincular.value = null
            carregarGrupsUsuari()
            onSuccess()
        }
    }

    fun editarGrup(grup: Grup, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val updates = mutableMapOf<String, Any>("nom" to editNomGrup.value)
            if (editClauGrup.value.isNotBlank()) updates["clau"] = editClauGrup.value
            db.collection("grups").document(grup.id).update(updates).await()
            grupAEditar.value = null
            carregarGrupsUsuari()
            onSuccess()
        }
    }

    // Helpers per netejar estats
    fun resetDialogs() {
        showDialog.value = false
        showKeyDialog.value = false
        grupNoExisteix.value = false
        groupIdToJoin.value = null
        inputKey.value = ""
        errorKey.value = null
        novaClau.value = ""
        grupADesvincular.value = null
        grupAEditar.value = null
        editNomGrup.value = ""
        editClauGrup.value = ""
    }
}