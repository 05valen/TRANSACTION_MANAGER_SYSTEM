# Sistema de Gestión de Transacciones

## 📋 Descripción del Proyecto

Sistema web completo para el registro, gestión y pago de transacciones financieras. Implementa operaciones CRUD completas, filtros avanzados, y un sistema de pagos por lotes que procesa transacciones en orden cronológico.

### 🎯 Características Principales

- **Backend**: Spring Boot 3.2.6 con Java 17
- **Frontend**: React 18 con Material-UI
- **Base de Datos**: H2 (memoria) para desarrollo
- **Arquitectura**: RESTful API con separación clara de capas
- **Testing**: Tests unitarios y de integración completos
- **Validaciones**: Backend y frontend con manejo de errores robusto

---

## 🛠️ Requisitos Previos

### Software Necesario

1. **Java Development Kit (JDK) 17 o superior**
   ```bash
   java -version
   # Debe mostrar: openjdk version "17.x.x" o superior
   ```

2. **Node.js 18 o superior**
   ```bash
   node --version
   # Debe mostrar: v18.x.x o superior
   ```

3. **npm 9 o superior**
   ```bash
   npm --version
   # Debe mostrar: 9.x.x o superior
   ```

4. **Maven 3.8 o superior**
   ```bash
   mvn --version
   # Debe mostrar: Apache Maven 3.8.x o superior
   ```

### Verificación de Requisitos

```bash
# Verificar que todos los comandos funcionen
java -version && node --version && npm --version && mvn --version
```

---

## 🚀 Instalación y Configuración PASO A PASO

### Paso 1: Clonar el Repositorio

```bash
git clone https://github.com/05valen/TRANSACTION_MANAGER_SYSTEM.git
cd TRANSACTION_MANAGER_SYSTEM
```

### Paso 2: Verificar Estructura del Proyecto

```bash
# Debes ver esta estructura:
ls -la
# Debe mostrar: backend/, frontend/, pom.xml, README.md, etc.
```

### Paso 3: Configurar el Backend

```bash
# Desde la raíz del proyecto (donde está el pom.xml)
mvn clean compile -DskipTests

# Si hay errores de compilación, ejecutar:
mvn clean install -DskipTests
```

### Paso 4: Configurar el Frontend

```bash
# Navegar al directorio frontend
cd frontend

# Instalar dependencias
npm install

# Verificar instalación
npm list --depth=0
```

---

## 🏃‍♂️ EJECUCIÓN DEL PROYECTO - INSTRUCCIONES DETALLADAS

### ⚠️ IMPORTANTE: Liberar Puertos Antes de Ejecutar

```bash
# Matar procesos que puedan estar usando los puertos 8080 y 3000
lsof -ti:8080 | xargs kill -9 2>/dev/null || true
lsof -ti:3000 | xargs kill -9 2>/dev/null || true
```

### Opción 1: Desarrollo (Recomendado)

#### Terminal 1: Backend (Spring Boot)

```bash
# Desde la raíz del proyecto (donde está el pom.xml)
mvn spring-boot:run
```

**✅ Indicadores de éxito del Backend:**
- Puerto 8080 disponible
- Mensaje: `Started Main in X.XXX seconds`
- H2 Console disponible en: http://localhost:8080/h2-console
- No debe haber errores de compilación

**❌ Si hay errores:**
- Verificar que Java 17 esté instalado: `java -version`
- Verificar que Maven esté instalado: `mvn --version`
- Ejecutar: `mvn clean compile -DskipTests`

#### Terminal 2: Frontend (React)

```bash
# Abrir una NUEVA terminal
# Navegar al directorio frontend
cd frontend

# Iniciar el servidor de desarrollo
npm start
```

**✅ Indicadores de éxito del Frontend:**
- Puerto 3000 disponible
- Mensaje: `Compiled successfully!`
- URL: http://localhost:3000
- El navegador se abre automáticamente

**❌ Si hay errores:**
- Verificar que Node.js esté instalado: `node --version`
- Verificar que npm esté instalado: `npm --version`
- Ejecutar: `npm install` en el directorio frontend

### Opción 2: Ejecución con Puertos Diferentes

Si los puertos 8080 o 3000 están ocupados:

#### Backend en Puerto Diferente
```bash
# Ejecutar en puerto 8081
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=8081"
```

#### Frontend en Puerto Diferente
```bash
# Cuando npm start pregunte por otro puerto, responder 'Y'
cd frontend
npm start
# Responder 'Y' cuando pregunte por puerto alternativo
```

---

## 🧪 Testing

### Ejecutar Tests del Backend

```bash
# Desde la raíz del proyecto
mvn test

# Si hay errores de compilación en tests, ejecutar:
mvn clean compile test-compile
mvn test
```

### Verificar que Todo Funciona

#### 1. Verificar Backend
```bash
# Health check
curl -X GET http://localhost:8080/api/transacciones

# Respuesta esperada: [] (array vacío)
```

#### 2. Verificar Frontend
- Abrir http://localhost:3000
- Debe mostrar la interfaz de usuario
- No debe haber errores en la consola del navegador (F12)

#### 3. Verificar Base de Datos H2
- Abrir http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `SA`
- Password: (dejar vacío)

---

## 📖 Guía de Uso del Sistema

### 1. Crear Transacciones

#### Desde el Frontend
1. Abrir http://localhost:3000
2. En la sección "Nueva Transacción":
   - **Nombre**: Descripción de la transacción
   - **Fecha**: Seleccionar fecha (formato: YYYY-MM-DD)
   - **Valor**: Monto positivo (ej: 100.50)
3. Hacer clic en "Crear Transacción"

#### Desde la API
```bash
curl -X POST http://localhost:8080/api/transacciones \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Pago de servicios",
    "fecha": "2024-01-15",
    "valor": 150.00
  }'
```

### 2. Listar Transacciones

#### Desde el Frontend
- Las transacciones se muestran automáticamente
- Usar filtros para buscar por nombre, fecha o estado

#### Desde la API
```bash
# Obtener todas las transacciones
curl -X GET http://localhost:8080/api/transacciones

# Obtener transacciones con filtros
curl -X GET "http://localhost:8080/api/transacciones?nombre=pago&estado=PENDIENTE"
```

### 3. Realizar Pagos

#### Desde el Frontend
1. En la lista de transacciones, hacer clic en "Pagar"
2. Ingresar el monto exacto del pago
3. Hacer clic en "Confirmar Pago"

**⚠️ Importante:** Solo se aceptan pagos con montos exactos

#### Desde la API
```bash
# Pago exitoso (monto exacto)
curl -X POST http://localhost:8080/api/transacciones/1/pago \
  -H "Content-Type: application/json" \
  -d '{"monto": 150.00}'

# Pago insuficiente (error 400)
curl -X POST http://localhost:8080/api/transacciones/1/pago \
  -H "Content-Type: application/json" \
  -d '{"monto": 100.00}'

# Pago en exceso (error 422)
curl -X POST http://localhost:8080/api/transacciones/1/pago \
  -H "Content-Type: application/json" \
  -d '{"monto": 200.00}'
```

### 4. Pago por Lotes

#### Desde el Frontend
1. Hacer clic en "Pago por Lotes"
2. El sistema procesará automáticamente las transacciones pendientes en orden cronológico

#### Desde la API
```bash
curl -X POST http://localhost:8080/api/transacciones/pago-lotes
```

---

## 🔧 Solución de Problemas Comunes

### Error: "Port 8080 was already in use"
```bash
# Matar proceso en puerto 8080
lsof -ti:8080 | xargs kill -9

# O usar puerto alternativo
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=8081"
```

### Error: "Port 3000 was already in use"
```bash
# Matar proceso en puerto 3000
lsof -ti:3000 | xargs kill -9

# O responder 'Y' cuando npm start pregunte por puerto alternativo
```

### Error: "npm: command not found"
```bash
# Instalar Node.js desde: https://nodejs.org/
# Verificar instalación
node --version
npm --version
```

### Error: "mvn: command not found"
```bash
# Instalar Maven desde: https://maven.apache.org/download.cgi
# Verificar instalación
mvn --version
```

### Error: "java: command not found"
```bash
# Instalar Java 17 desde: https://adoptium.net/
# Verificar instalación
java -version
```

### Error de Compilación en Tests
```bash
# Limpiar y recompilar
mvn clean compile test-compile
mvn test
```

### Frontend No Se Conecta al Backend
```bash
# Verificar que el backend esté corriendo en puerto 8080
curl http://localhost:8080/api/transacciones

# Verificar configuración CORS en el backend
# El proyecto ya incluye configuración CORS correcta
```

---

## 📚 Endpoints de la API

### Transacciones
- `GET /api/transacciones` - Listar transacciones
- `POST /api/transacciones` - Crear transacción
- `GET /api/transacciones/{id}` - Obtener transacción por ID
- `PUT /api/transacciones/{id}` - Actualizar transacción
- `DELETE /api/transacciones/{id}` - Eliminar transacción

### Pagos
- `POST /api/transacciones/{id}/pago` - Realizar pago individual
- `POST /api/transacciones/pago-lotes` - Realizar pago por lotes

### Filtros
- `GET /api/transacciones?nombre=texto` - Filtrar por nombre
- `GET /api/transacciones?fecha=2024-01-15` - Filtrar por fecha
- `GET /api/transacciones?estado=PENDIENTE` - Filtrar por estado

---

## 🎯 Casos de Prueba Recomendados

### 1. Flujo Básico
1. Crear 3 transacciones con diferentes montos
2. Verificar que aparezcan en la lista
3. Realizar pagos con montos exactos
4. Verificar cambio de estado a "PAGADO"

### 2. Validaciones de Pago
1. Crear transacción de $100
2. Intentar pagar $50 (debe dar error 400)
3. Intentar pagar $150 (debe dar error 422)
4. Pagar $100 (debe ser exitoso)

### 3. Pago por Lotes
1. Crear 5 transacciones con fechas diferentes
2. Realizar pago por lotes
3. Verificar que se procesen en orden cronológico

### 4. Filtros
1. Crear transacciones con diferentes nombres
2. Probar filtros por nombre, fecha y estado
3. Verificar que los filtros funcionen correctamente

---

## 📋 Colección de Postman

El proyecto incluye una colección completa de Postman para probar todos los endpoints de la API.

### 📁 Ubicación
```
documentos/TRANSACTION_MANAGER_SYSTEM.postman_collection.json
```

### 🚀 Cómo usar la colección

1. **Importar en Postman:**
   - Abrir Postman
   - Hacer clic en "Import"
   - Seleccionar el archivo `TRANSACTION_MANAGER_SYSTEM.postman_collection.json`

2. **Configurar variables:**
   - La colección usa la variable `{{base_url}}` configurada por defecto en `http://localhost:8080`
   - Si usas un puerto diferente, actualiza la variable en la colección

3. **Endpoints incluidos:**
   - **Transacciones:**
     - Obtener todas las transacciones
     - Obtener transacciones con filtros
     - Obtener transacción por ID
     - Crear nueva transacción
     - Actualizar transacción
     - Eliminar transacción
   - **Pagos:**
     - Realizar pago
     - Obtener transacciones pendientes

### 🧪 Casos de prueba incluidos
- Ejemplos de JSON para crear y actualizar transacciones
- Filtros de búsqueda con parámetros de ejemplo
- Diferentes escenarios de pago (exitoso, insuficiente, exceso)

---

## 📞 Soporte

Si encuentras problemas:

1. **Verificar requisitos**: Asegúrate de tener Java 17, Node.js 18+ y Maven 3.8+
2. **Revisar logs**: Los errores aparecen en la terminal donde ejecutas los comandos
3. **Liberar puertos**: Usa los comandos para matar procesos en puertos 8080 y 3000
4. **Recompilar**: Ejecuta `mvn clean compile` y `npm install`

---

## 🏆 Estado del Proyecto

✅ **Completado:**
- Backend Spring Boot funcional
- Frontend React funcional
- API REST completa
- Sistema de pagos con validaciones
- Tests unitarios y de integración
- Documentación completa

🚀 **Listo para usar:**
- El proyecto está completamente funcional
- Todos los tests pasan
- Documentación paso a paso incluida 