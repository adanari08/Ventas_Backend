# =============================================================
#  Pruebas de los endpoints del microservicio Tienda
#  (mismas pruebas que la coleccion de Postman)
#
#  Uso (con el backend encendido, desde la carpeta del proyecto):
#     powershell -ExecutionPolicy Bypass -File .\postman\pruebas-endpoints.ps1
# =============================================================

$base = "http://localhost:8000"
$resultados = @()

# hace la peticion y regresa el status y el json de respuesta
function Llamar($metodo, $ruta, $body) {
    $params = @{
        Uri             = "$base$ruta"
        Method          = $metodo
        UseBasicParsing = $true
        ContentType     = "application/json; charset=utf-8"
    }
    if ($body) {
        $params.Body = [System.Text.Encoding]::UTF8.GetBytes(($body | ConvertTo-Json))
    }
    $status = 0
    $contenido = ""
    try {
        $r = Invoke-WebRequest @params
        $status = [int]$r.StatusCode
        $contenido = $r.Content
    } catch {
        $resp = $_.Exception.Response
        if (-not $resp) {
            Write-Host "No se pudo conectar con $base. Revisa que el backend este encendido." -ForegroundColor Red
            exit 1
        }
        $status = [int]$resp.StatusCode
        if ($_.ErrorDetails -and $_.ErrorDetails.Message) {
            $contenido = $_.ErrorDetails.Message
        } elseif ($resp.PSObject.Methods.Name -contains "GetResponseStream") {
            $lector = New-Object System.IO.StreamReader($resp.GetResponseStream())
            $contenido = $lector.ReadToEnd()
        }
    }
    $json = $null
    if ($contenido) {
        try { $json = $contenido | ConvertFrom-Json } catch { }
    }
    return [pscustomobject]@{ Status = $status; Json = $json }
}

# guarda el resultado de una prueba
function Probar($id, $nombre, $peticion, $esperado, $respuesta, $condicionExtra) {
    $ok = ($respuesta.Status -eq $esperado)
    if ($ok -and $condicionExtra) { $ok = [bool](& $condicionExtra) }
    $estado = if ($ok) { "PASO" } else { "FALLO" }
    $color = if ($ok) { "Green" } else { "Red" }
    Write-Host ("{0,-4} {1,-6} {2,-32} {3,-38} esperado {4} / obtenido {5}" -f $id, $estado, $nombre, $peticion, $esperado, $respuesta.Status) -ForegroundColor $color
    $script:resultados += [pscustomobject]@{
        ID = $id; Prueba = $nombre; Peticion = $peticion
        Esperado = $esperado; Obtenido = $respuesta.Status; Resultado = $estado
    }
}

Write-Host ""
Write-Host "===== PRUEBAS DE PRODUCTOS =====" -ForegroundColor Cyan

$r = Llamar "GET" "/api/productos"
Probar "P1" "Listar productos" "GET /api/productos" 200 $r { $r.Json.Count -ge 0 }

$r = Llamar "POST" "/api/productos" @{ nombre = "Bocina Bluetooth"; descripcion = "Bocina portatil"; precio = 450.00; stock = 10 }
$productoId = $r.Json.id
Probar "P2" "Crear producto" "POST /api/productos" 201 $r { $productoId -gt 0 }

$r = Llamar "GET" "/api/productos/$productoId"
Probar "P3" "Obtener producto por id" "GET /api/productos/$productoId" 200 $r { $r.Json.nombre -eq "Bocina Bluetooth" }

$r = Llamar "PUT" "/api/productos/$productoId" @{ nombre = "Bocina Bluetooth XL"; descripcion = "Bocina portatil grande"; precio = 520.00; stock = 10 }
Probar "P4" "Actualizar producto" "PUT /api/productos/$productoId" 200 $r { [double]$r.Json.precio -eq 520 }

$r = Llamar "POST" "/api/productos" @{ nombre = ""; precio = -5; stock = -1 }
Probar "P5" "Crear producto invalido" "POST /api/productos" 400 $r { $r.Json.detalles.nombre }

$r = Llamar "GET" "/api/productos/99999"
Probar "P6" "Producto que no existe" "GET /api/productos/99999" 404 $r

$r = Llamar "GET" "/api/productos/abc"
Probar "P7" "Id que no es numero" "GET /api/productos/abc" 400 $r

Write-Host ""
Write-Host "===== PRUEBAS DE VENTAS =====" -ForegroundColor Cyan

$r = Llamar "POST" "/api/ventas" @{ productoId = $productoId; cantidad = 3 }
$ventaId = $r.Json.id
Probar "V1" "Registrar venta (total 1560)" "POST /api/ventas" 201 $r { [double]$r.Json.total -eq 1560 }

$r = Llamar "GET" "/api/productos/$productoId"
Probar "V2" "Stock descontado (queda 7)" "GET /api/productos/$productoId" 200 $r { $r.Json.stock -eq 7 }

$r = Llamar "GET" "/api/ventas"
Probar "V3" "Listar ventas" "GET /api/ventas" 200 $r { @($r.Json | Where-Object { $_.id -eq $ventaId }).Count -eq 1 }

$r = Llamar "GET" "/api/ventas/$ventaId"
Probar "V4" "Obtener venta por id" "GET /api/ventas/$ventaId" 200 $r { $r.Json.cantidad -eq 3 }

$r = Llamar "GET" "/api/ventas/producto/$productoId"
Probar "V5" "Ventas de un producto" "GET /api/ventas/producto/$productoId" 200 $r { @($r.Json).Count -eq 1 }

$r = Llamar "PUT" "/api/ventas/$ventaId" @{ productoId = $productoId; cantidad = 5 }
$r2 = Llamar "GET" "/api/productos/$productoId"
Probar "V6" "Actualizar venta (stock queda 5)" "PUT /api/ventas/$ventaId" 200 $r { $r.Json.cantidad -eq 5 -and $r2.Json.stock -eq 5 }

$r = Llamar "POST" "/api/ventas" @{ productoId = $productoId; cantidad = 1000 }
Probar "V7" "Venta sin stock suficiente" "POST /api/ventas" 400 $r

$r = Llamar "POST" "/api/ventas" @{ productoId = $productoId; cantidad = 0 }
Probar "V8" "Venta con cantidad 0" "POST /api/ventas" 400 $r

$r = Llamar "POST" "/api/ventas" @{ productoId = 99999; cantidad = 1 }
Probar "V9" "Venta de producto inexistente" "POST /api/ventas" 404 $r

$r = Llamar "DELETE" "/api/productos/$productoId"
Probar "P8" "Borrar producto con ventas" "DELETE /api/productos/$productoId" 409 $r

$r = Llamar "DELETE" "/api/ventas/$ventaId"
Probar "V10" "Eliminar venta" "DELETE /api/ventas/$ventaId" 204 $r

$r = Llamar "GET" "/api/productos/$productoId"
Probar "V11" "Stock regresado (vuelve a 10)" "GET /api/productos/$productoId" 200 $r { $r.Json.stock -eq 10 }

$r = Llamar "DELETE" "/api/productos/$productoId"
Probar "P9" "Borrar producto sin ventas" "DELETE /api/productos/$productoId" 204 $r

$r = Llamar "GET" "/api/productos/$productoId"
Probar "P10" "Producto eliminado" "GET /api/productos/$productoId" 404 $r

# resumen
$pasaron = @($resultados | Where-Object { $_.Resultado -eq "PASO" }).Count
$total = $resultados.Count
Write-Host ""
$colorFinal = if ($pasaron -eq $total) { "Green" } else { "Yellow" }
Write-Host "RESULTADO: $pasaron de $total pruebas pasaron" -ForegroundColor $colorFinal

# se guarda un archivo con los resultados para la evidencia
$archivo = Join-Path $PSScriptRoot "resultados-pruebas.csv"
$resultados | Export-Csv -Path $archivo -NoTypeInformation -Encoding UTF8
Write-Host "Resultados guardados en: $archivo"
