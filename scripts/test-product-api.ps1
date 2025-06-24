# Script PowerShell pour tester la création de produit
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJyb2xlIjoiUk9MRV9BRE1JTiIsInN1YiI6IjRmNmU2MTM2LTM1MjEtNDkzYy1hOTIyLTFmYjcxYjRjZWRlMiIsImlhdCI6MTc1MDQ3MDIwNiwiZXhwIjoxNzUwNDczODA2fQ.Hg0iRgPom1S0t5oWl9GgIKZDqWD8eERT0f_TNvowb_LGiocTnzEjfx66v5jxoxRSp4tA-ZA3gc242L2eFbGxJQ"
}

$body = @{
    name = "Bague en Or 18 Carats"
    description = "Magnifique bague en or 18 carats avec un design élégant et moderne"
    sku = "BAGUE-OR-002"
    price = @{
        amount = 2500.00
        currency = "MAD"
    }
    categoryId = "dee1b441-0e9b-4706-b93d-ed935e9545e4"
    active = $true
} | ConvertTo-Json -Depth 3

Write-Host "Envoi de la requête de création de produit..."
Write-Host "Body: $body"

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8082/api/v1/products" -Method POST -Headers $headers -Body $body
    Write-Host "Succès! Status: $($response.StatusCode)"
    Write-Host "Réponse: $($response.Content)"
} catch {
    Write-Host "Erreur: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Détails de l'erreur: $responseBody"
    }
} 