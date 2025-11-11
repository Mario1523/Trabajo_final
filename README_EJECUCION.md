# Cómo Ejecutar el Proyecto

## Requisitos
- Java 11 o superior
- Las dependencias de iText ya están incluidas en el directorio `lib/`

## Compilación

```bash
javac -encoding UTF-8 -d target/classes -cp "target/classes;lib\*" src/main/java/com/monitoreo/*.java
```

## Ejecución

### Opción 1: Comando directo (recomendado)
```bash
java -cp "target/classes;lib\*" com.monitoreo.InterfazGrafica
```

### Opción 2: Scripts de inicio rápido
- **Windows**: Doble clic en `iniciar.bat` o ejecutar `.\iniciar.bat`
- **PowerShell**: `.\iniciar.ps1`

### Opción 3: Script completo (compila y ejecuta)
- **Windows**: Doble clic en `ejecutar.bat`
- **PowerShell**: `.\ejecutar.ps1`

## Nota Importante
El classpath debe incluir tanto `target/classes` como `lib\*` para que las dependencias de iText estén disponibles. 

**Formato del classpath:**
- Windows: `"target/classes;lib\*"`
- Linux/Mac: `"target/classes:lib/*"`

