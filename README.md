# 🍔 STREET FOOD PRO
### Sistema Integral de Caja POS, Inventario, Personal y Gestión Operativa para Locales de Comida Rápida

**STREET FOOD PRO** es una solución móvil de punto de venta (POS) y gestión operativa especialmente diseñada para negocios gastronómicos callejeros (food trucks, puestos de perros calientes, hamburgueserías y locales de comida rápida). Desarrollada con arquitectura **Local-First**, funciona al 100% sin conexión a internet y mantiene sincronización instantánea en memoria y almacenamiento persistente local.

---

## 📱 Módulos y Funcionalidades Integradas

1. **Apertura y Cierre de Turno:**
   - Selección de turno: *Turno Mañana (11:00 AM - 6:00 PM)* y *Turno Nocturno (6:00 PM - 2:00 AM)*.
   - Registro del operador/cajero y arqueo base de caja inicial en divisas ($ USD) y moneda nacional (Bs.).
   - Bloqueo y validación de operaciones fuera de turno.

2. **Módulo POS (Punto de Venta Moderno):**
   - Catálogo rápido con precios duales en tiempo real ($ USD y Bolívares `Bs.` calculados mediante tasa configurable).
   - Menú preconfigurado: Perro Caliente Sencillo, Perro Jumbo, Hamburguesa Tradicional, Hamburguesa Doble Especial, Combos Callejeros, Salchipapas, Bebidas (250ml, 500ml, 1L).
   - Carrito de compras reactivo con modificación de cantidades y notas especiales.
   - Registro de cliente (Nombre y WhatsApp).
   - Formas de pago adaptadas: *Punto de Venta (Tarjeta)*, *Pago Móvil*, *Efectivo USD*, *Efectivo Bs.* con validación de código de referencia bancaria.
   - **Comprobante Digital por WhatsApp:** Al confirmar el pago, genera automáticamente el comprobante digital estructurado y abre WhatsApp mediante enlace directo `https://wa.me/...`.

3. **Módulo de Stock e Insumos (`STOCK`):**
   - Control de inventario por ingredientes (Panes, Carnes, Salchichas, Salsas, Papitas ralladas, Queso, Servilletas, Vasos y Bebidas).
   - **Descuento automático de inventario:** Cada venta procesada en el POS descuenta en tiempo real los ingredientes según la receta del producto.
   - Registro de compras de mercancía que incrementa el stock e imputa el egreso financiero al turno activo.
   - Semáforo y alertas visuales para insumos en nivel crítico.

4. **Módulo de Personal (`STAFF`):**
   - Gestión de empleados (Plancheros, Cajeros, Motorizados, Despachadores) y costo por jornada ($/día).
   - Control de asistencia por turno (Presente / Ausente).
   - Registro de pagos/anticipos de nómina asociados al turno.

5. **Módulo de Cuadre y Cierre (`CUADRE`):**
   - Resumen financiero en tiempo real: Ventas Brutas, Egresos (Compras + Nómina) y Ganancia Neta.
   - Desglose por método de pago (Punto de Venta, Pago Móvil, Efectivo USD, Efectivo Bs).
   - Exportación de auditoría formal para imprimir o archivar (formato PDF / Documento de Cierre).
   - Descarga de historial en formato **Excel / CSV**.
   - **Envío de Cierre por WhatsApp:** Reporte estructurado con emojis y métricas financieras enviado directamente al número del dueño/administrador.

6. **Módulo de Ruta y Delivery (`RUTA`):**
   - Radar y mapa Canvas interactivo con simulación GPS en tiempo real.
   - Marcador central del Food Truck / Cocina, punto de entrega del cliente y motorizado animado a lo largo de la trayectoria.
   - Botón *Asignar GPS Cliente* e *Iniciar Ruta Motorizado* (estados: *Pendiente*, *En camino*, *En destino*, *Entregado*).
   - Notificación directa por WhatsApp al cliente informando que su pedido va en camino.

7. **Configuración y Administración (`CONFIG`):**
   - Ajuste rápido de la Tasa de Cambio del día (Bs./USD).
   - Número de WhatsApp del Administrador.
   - Editor de plantillas de recibos y cierres con tags dinámicos (`{cliente}`, `{total_usd}`, `{total_bs}`, `{tasa}`, `{metodo}`).
   - Zona de peligro: Reset y restauración de la base de datos a valores de fábrica.

---

## 🛠️ Guía de Ejecución y Compilación

### 1. Compilación Nativa Android (Jetpack Compose)
Esta aplicación está construida con **Kotlin**, **Jetpack Compose** y **Material Design 3**:

```bash
# Verificar compilación del APK
gradle assembleDebug

# Ejecutar pruebas unitarias y de integración Robolectric
gradle :app:testDebugUnitTest
```

### 2. Configuración y Empaquetado Híbrido (React / Vite + Tailwind + Capacitor)

Si deseas empaquetar una versión PWA / Híbrida en tu entorno local o en **Termux**:

#### Paso 1: Inicialización del Frontend
```bash
# Crear proyecto Vite con React y TypeScript
npm create vite@latest street-food-pro -- --template react-ts
cd street-food-pro

# Instalar dependencias de UI, iconos, mapas y reportes
npm install lucide-react jspdf xlsx leaflet react-leaflet
npm install -D tailwindcss postcss autoprefixer @types/leaflet

# Inicializar configuración de Tailwind CSS
npx tailwindcss init -p

# Ejecutar el servidor de desarrollo local
npm run dev
```

#### Paso 2: Inicialización y Empaquetado con Capacitor
```bash
# Instalar dependencias de Capacitor
npm install @capacitor/core @capacitor/cli @capacitor/android

# Inicializar configuración de Capacitor
npx cap init "Street Food Pro" "com.aistudio.streetfoodpro.app" --web-dir dist

# Construir los assets de producción
npm run build

# Agregar la plataforma Android
npx cap add android

# Sincronizar cambios web hacia el contenedor nativo
npx cap sync android

# Abrir el proyecto en Android Studio para generar el archivo .apk
npx cap open android
```

#### Paso 3: Generación del APK desde línea de comandos (o Termux)
```bash
cd android
./gradlew assembleDebug
# El APK generado se ubica en:
# android/app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔒 Arquitectura Local-First
- Todos los datos (ventas, comanda, stock, empleados y configuración) se conservan localmente en el dispositivo.
- No requiere conexión de base de datos en la nube ni conexión a internet constante para cobrar, facturar y operar el punto de venta.
