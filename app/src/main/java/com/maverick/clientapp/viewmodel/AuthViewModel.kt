package com.maverick.clientapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maverick.clientapp.auth.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    fun registerUser(email: String, password: String, onResult: (Boolean) -> Unit) {
        //Ejecuta la operación de Firebase en una corrutina para no bloquear la UI.
        viewModelScope.launch {
            //Llama a la función de autenticación y devuelve true si el usuario se registró correctamente,
            // false si hubo error.
            val user = authRepository.registerUser(email, password)
            onResult(user != null)
        }
    }
}