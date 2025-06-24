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

## 🚀 Instalación y Configuración

### 1. Clonar el Repositorio

```bash
git clone <repository-url>
cd transaction-management-system
```

### 2. Configurar el Backend

```bash
# Navegar al directorio del proyecto
cd transaction-management-system

# Verificar que Maven puede resolver las dependencias
mvn dependency:resolve

# Compilar el proyecto (sin ejecutar tests)
mvn clean compile -DskipTests
```

### 3. Configurar el Frontend

```bash
# Navegar al directorio frontend
cd frontend

# Instalar dependencias
npm install

# Verificar que las dependencias se instalaron correctamente
npm list --depth=0
```

---

## 🏃‍♂️ Ejecución del Proyecto

### Opción 1: Desarrollo (Recomendado)

#### Terminal 1: Backend
```bash
# Desde la raíz del proyecto
mvn spring-boot:run
```

**Indicadores de éxito:**
- Puerto 8080 disponible
- Mensaje: "Started Main in X.XXX seconds"
- H2 Console disponible en: http://localhost:8080/h2-console

#### Terminal 2: Frontend
```bash
# Desde la carpeta frontend
cd frontend
npm start
```

**Indicadores de éxito:**
- Puerto 3000 disponible
- Mensaje: "Compiled successfully!"
- URL: http://localhost:3000

### Opción 2: Producción

#### Backend
```bash
# Compilar JAR ejecutable
mvn clean package -DskipTests

# Ejecutar JAR
java -jar target/transaction-management-system-1.0-SNAPSHOT.jar
```

#### Frontend
```bash
# Construir para producción
cd frontend
npm run build

# Servir archivos estáticos (requiere servidor web)
# Opción con serve (instalar: npm install -g serve)
serve -s build -l 3000
```

---

## 🧪 Testing

### Tests Automatizados

#### Ejecutar Todos los Tests
```bash
# Desde la raíz del proyecto
mvn clean verify
```

#### Tests Unitarios (Backend)
```bash
mvn test
```

#### Tests de Integración
```bash
mvn test -Dtest=TransaccionControllerIntegrationTest
```

#### Tests Específicos
```bash
# Test de servicio específico
mvn test -Dtest=TransaccionServiceTest

# Test de controlador específico
mvn test -Dtest=TransaccionControllerIntegrationTest
```

### Tests Manuales

#### 1. Verificar Backend
```bash
# Health check
curl -X GET http://localhost:8080/api/transacciones

# Respuesta esperada: [] (array vacío)
```

#### 2. Verificar Frontend
- Abrir http://localhost:3000
- Debe mostrar la interfaz de usuario
- No debe haber errores en la consola del navegador

---

## 📖 Guía de Uso

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
    "nombre": "Compra de suministros",
    "fecha": "2024-01-15",
    "valor": 150.75
  }'
```

### 2. Listar Transacciones

#### Con Filtros
- **Por Nombre**: Búsqueda parcial (case-insensitive)
- **Por Fecha**: Fecha exacta
- **Por Estado**: PENDIENTE, PAGADO, o TODOS

#### Desde la API
```bash
# Todas las transacciones
curl -X GET http://localhost:8080/api/transacciones

# Con filtros
curl -X GET "http://localhost:8080/api/transacciones?nombre=suministros&estado=PENDIENTE"
```

### 3. Editar Transacciones

#### Restricciones
- Solo transacciones con estado **PENDIENTE**
- Transacciones **PAGADAS** no se pueden editar

#### Desde el Frontend
1. Hacer clic en el ícono de editar (lápiz)
2. Modificar los campos
3. Hacer clic en "Actualizar"

#### Desde la API
```bash
curl -X PUT http://localhost:8080/api/transacciones/1 \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Compra de suministros actualizada",
    "fecha": "2024-01-16",
    "valor": 175.25
  }'
```

### 4. Eliminar Transacciones

#### Restricciones
- Solo transacciones con estado **PENDIENTE**
- Transacciones **PAGADAS** no se pueden eliminar

#### Desde el Frontend
1. Hacer clic en el ícono de eliminar (basura)
2. Confirmar eliminación

#### Desde la API
```bash
curl -X DELETE http://localhost:8080/api/transacciones/1
```

### 5. Realizar Pagos

#### Lógica de Pago
- **Orden cronológico**: Se pagan las transacciones más antiguas primero
- **Pago completo**: Solo se paga si el monto cubre completamente la transacción
- **Sin pagos parciales**: Si el monto no alcanza, la transacción queda pendiente

#### Ejemplo de Pago
```bash
# Pago de $200 para transacciones pendientes
curl -X POST "http://localhost:8080/api/transacciones/pagar?monto=200"
```

**Escenarios:**
- **Transacción 1**: $100 (se paga)
- **Transacción 2**: $150 (no se paga, monto restante: $100)
- **Resultado**: "Se pagó 1 transacción exitosamente. Monto restante: $100.00"

---

## 🔧 Configuración Avanzada

### Variables de Entorno

#### Backend (application.properties)
```properties
# Puerto del servidor
server.port=8080

# Configuración de base de datos
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# H2 Console (solo para desarrollo)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA/Hibernate
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
```

#### Frontend (.env)
```env
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_ENVIRONMENT=development
```

### Configuración de CORS

El backend incluye configuración CORS para permitir peticiones desde el frontend:

```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:3000")
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowedHeaders("*");
            }
        };
    }
}
```

---

## 🐛 Troubleshooting

### Problemas Comunes

#### 1. Puerto 8080 Ocupado
```bash
# Verificar qué proceso usa el puerto
lsof -i :8080

# Terminar proceso
kill -9 <PID>

# O cambiar puerto en application.properties
server.port=8081
```

#### 2. Puerto 3000 Ocupado
```bash
# Verificar qué proceso usa el puerto
lsof -i :3000

# Terminar proceso
kill -9 <PID>

# O usar puerto diferente
npm start -- --port 3001
```

#### 3. Errores de CORS
- Verificar que el backend esté corriendo en puerto 8080
- Verificar que el frontend esté corriendo en puerto 3000
- Revisar configuración CORS en `CorsConfig.java`

#### 4. Errores de Compilación
```bash
# Limpiar y recompilar
mvn clean compile

# Verificar versión de Java
java -version

# Verificar versión de Maven
mvn --version
```

#### 5. Errores de Dependencias Frontend
```bash
# Limpiar cache de npm
npm cache clean --force

# Eliminar node_modules y reinstalar
rm -rf node_modules package-lock.json
npm install
```

### Logs y Debugging

#### Backend Logs
```bash
# Ver logs detallados
mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.com.transaction=DEBUG"

# Logs específicos de Hibernate
mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.org.hibernate.SQL=DEBUG"
```

#### Frontend Debugging
1. Abrir DevTools (F12)
2. Ir a la pestaña Console
3. Verificar errores de red en Network
4. Revisar logs de la aplicación

---

## 📊 Estructura del Proyecto

```
transaction-management-system/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/transaction/
│   │   │   │   ├── config/
│   │   │   │   │   └── CorsConfig.java
│   │   │   │   ├── controller/
│   │   │   │   │   └── TransaccionController.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── TransaccionCreateDTO.java
│   │   │   │   │   └── TransaccionDTO.java
│   │   │   │   ├── entity/
│   │   │   │   │   ├── EstadoTransaccion.java
│   │   │   │   │   └── Transaccion.java
│   │   │   │   ├── exception/
│   │   │   │   │   └── GlobalExceptionHandler.java
│   │   │   │   ├── repository/
│   │   │   │   │   └── TransaccionRepository.java
│   │   │   │   ├── service/
│   │   │   │   │   ├── PaymentResult.java
│   │   │   │   │   └── TransaccionService.java
│   │   │   │   └── Main.java
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   │       ├── java/com/transaction/
│   │       │   ├── controller/
│   │       │   │   └── TransaccionControllerIntegrationTest.java
│   │       │   └── service/
│   │       │       └── TransaccionServiceTest.java
│   │       └── resources/
│   │           └── application-test.properties
│   └── pom.xml
├── frontend/
│   ├── public/
│   │   └── index.html
│   ├── src/
│   │   ├── components/
│   │   │   ├── FiltrosForm.js
│   │   │   ├── Notification.js
│   │   │   ├── PagoForm.js
│   │   │   ├── TransaccionForm.js
│   │   │   └── TransaccionList.js
│   │   ├── services/
│   │   │   └── api.js
│   │   ├── App.js
│   │   └── index.js
│   ├── package.json
│   └── README.md
└── README.md
```

---

## 🧪 Casos de Prueba

### Escenarios de Pago

#### Caso 1: Pago Completo
```bash
# Crear transacciones
curl -X POST http://localhost:8080/api/transacciones \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Transacción 1", "fecha": "2024-01-01", "valor": 100}'

curl -X POST http://localhost:8080/api/transacciones \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Transacción 2", "fecha": "2024-01-02", "valor": 200}'

# Pagar $150
curl -X POST "http://localhost:8080/api/transacciones/pagar?monto=150"
# Resultado: "Se pagó 1 transacción exitosamente. Monto restante: $50.00"
```

#### Caso 2: Pago Insuficiente
```bash
# Pagar $50 para transacción de $100
curl -X POST "http://localhost:8080/api/transacciones/pagar?monto=50"
# Resultado: "No se pudo pagar ninguna transacción. El monto $50.00 no es suficiente..."
```

### Validaciones

#### Campos Obligatorios
```bash
# Error: nombre vacío
curl -X POST http://localhost:8080/api/transacciones \
  -H "Content-Type: application/json" \
  -d '{"nombre": "", "fecha": "2024-01-01", "valor": 100}'

# Error: valor negativo
curl -X POST http://localhost:8080/api/transacciones \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Test", "fecha": "2024-01-01", "valor": -100}'
```

---

## 📝 Notas de Desarrollo

### Tecnologías Utilizadas

- **Backend**: Spring Boot 3.2.6, Spring Data JPA, H2 Database
- **Frontend**: React 18, Material-UI, Axios
- **Testing**: JUnit 5, Mockito, Spring Boot Test
- **Build Tools**: Maven, npm

### Patrones de Diseño

- **DTO Pattern**: Separación entre entidades y objetos de transferencia
- **Repository Pattern**: Abstracción de acceso a datos
- **Service Layer**: Lógica de negocio centralizada
- **Exception Handling**: Manejo global de excepciones

### Consideraciones de Seguridad

- Validación de entrada en backend y frontend
- Sanitización de datos
- Manejo seguro de errores (no exponer detalles internos)
- Configuración CORS apropiada

---

## 📞 Soporte

Para reportar bugs o solicitar nuevas funcionalidades:

1. Verificar que el problema no esté en la sección de troubleshooting
2. Revisar los logs del backend y frontend
3. Proporcionar pasos específicos para reproducir el problema
4. Incluir información del entorno (SO, versiones de software)

---

## 📄 Licencia

Este proyecto es parte de una evaluación técnica y está destinado únicamente para fines educativos y de demostración. 