# 📱 Cómo Probar la App Android con Backend y Frontend

## ⚠️ Estado Actual
La app Android está implementada y hemos corregido muchos errores de compilación, pero aún quedan algunos problemas por resolver antes de poder probarla.

## ✅ Problemas Corregidos
- Creación de clases faltantes (TokenManager, Converters, UserMapper, PreferencesManager)
- Corrección de endpoints de API para coincidir con backend
- Corrección de errores de tipos en LoginUseCase y RegisterUseCase
- Corrección de problemas con MainActivity y temas
- Adición de métodos faltantes en PreferencesManager

## 🔧 Problemas de Compilación Restantes
- Conflictos de métodos duplicados en UserRepositoryImpl
- Errores de tipos en domain models (enums vs strings)
- Métodos faltantes en DAOs (deleteAllTrips, deleteAllUserTrips)
- Errores de importación en UI screens
- Referencias a BuildConfig sin resolver

## 🚀 Pasos para Corregir y Probar

### 1. Corregir Errores de Compilación
```bash
cd /Users/laura/Documents/SWNT/voyager-android
./gradlew assembleDebug
# Ver errores y corregirlos uno por uno
```

### 2. Iniciar Backend (cuando la app compile)
```bash
cd /Users/laura/Documents/SWNT/voyager-backend-core
./mvn spring-boot:run
```

### 3. Instalar y Ejecutar App Android (cuando compile)
```bash
cd /Users/laura/Documents/SWNT/voyager-android
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 4. Probar Funcionalidades

#### 🔐 Registro de Usuario
1. Abrir app → Pantalla de Login
2. Click "¿No tienes cuenta? Regístrate"
3. Ingresar:
   - Email: `testuser123@example.com`
   - Username: `testuser123`
   - Password: `TestPass123!`
   - Nombre: `Test`
   - Apellido: `User`
4. Click "Registrarse"
5. ✅ Debería navegar al dashboard

#### 🔑 Login Normal
1. Cerrar y reabrir app
2. Ingresar credenciales registradas
3. ✅ Debería navegar al dashboard

#### 🌐 Login con Google
1. En pantalla de login, click "Continuar con Google"
2. Simular callback:
```bash
adb shell am start -W -a android.intent.action.VIEW \
  -d "smartrip://auth/callback?code=TEST_CODE&state=TEST_STATE" \
  com.voyager.tourism
```
3. ✅ Debería navegar al dashboard

#### 👤 Completar Perfil
1. Navegar a perfil
2. Completar:
   - Nombre: `Test Updated`
   - Apellido: `User Updated`
   - Biografía: `Viajero apasionado`
   - Intereses: `travel`, `food`
3. Click "Guardar perfil"
4. ✅ Debería mostrar "Perfil actualizado"

#### ✈️ Crear Plan de Viaje
1. Navegar a "Crear viaje"
2. Ingresar:
   - Título: `Viaje a París`
   - Destino: `París, Francia`
   - Fecha inicio: `2024-06-15`
   - Fecha fin: `2024-06-22`
   - Viajeros: `2`
3. Click "Crear viaje"
4. ✅ Debería mostrar éxito

## 🔍 Verificación con Logs

```bash
# Ver logs de la app
adb logcat | grep VoyagerTourism

# Ver logs de red
adb logcat | grep OkHttp
```

## ⚠️ Problemas Comunes

### 404 Not Found
- Backend no está corriendo
- Solución: Iniciar backend primero

### 401 Unauthorized  
- Token no se envía
- Solución: Verificar AuthInterceptor

### Deep Link no funciona
- Intent filter mal configurado
- Solución: Reinstalar app

## ✅ Criterios de Éxito

- [ ] Registro funciona
- [ ] Login normal funciona  
- [ ] Login con Google funciona
- [ ] Perfil se actualiza
- [ ] Plan de viaje se crea
- [ ] No hay errores 404/401

---

**Nota:** La app está configurada para conectar al backend local en `http://10.0.2.2:8080/`
