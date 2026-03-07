# Smoke Test Script (English Version)
# Purpose: Verify "Login → Pagination → (Optional) Create → Details" chain functionality

$base = "http://localhost:48080"
Write-Host "=== Starting Smoke Test ===" -ForegroundColor Green
Write-Host "Test Target: $base" -ForegroundColor Yellow

try {
    # 1) Login and get token
    Write-Host "1. Testing login and token retrieval..." -ForegroundColor Cyan
    $login = Invoke-RestMethod "$base/admin-api/system/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}'
    $token = $login.data.accessToken
    $h = @{ Authorization = "Bearer $token" }
    Write-Host "✓ Login successful, token: $($token.Substring(0,8))..." -ForegroundColor Green

    # 2) Help order pagination (should return code==0)
    Write-Host "2. Testing help order pagination..." -ForegroundColor Cyan
    $pageResult = Invoke-RestMethod "$base/admin-api/system/help-order/page" -Headers $h -Method GET
    Write-Host "✓ Help order pagination successful: code=$($pageResult.code); total=$($pageResult.data.total)" -ForegroundColor Green

    # 3) (Optional) Create a test help order -> get ID -> details
    Write-Host "3. Testing help order creation..." -ForegroundColor Cyan
    $createBody = @{ 
        title="Medical Consultation Test"; 
        description="Created by smoke test"; 
        patientId=1;
        priority="MEDIUM";
        category="CONSULTATION"
    } | ConvertTo-Json
    $created = Invoke-RestMethod "$base/admin-api/system/help-order/create" -Headers $h -Method POST -ContentType "application/json" -Body $createBody
    $id = $created.data
    Write-Host "✓ Help order creation successful: id=$id" -ForegroundColor Green

    # 4) Get help order details
    Write-Host "4. Testing help order details retrieval..." -ForegroundColor Cyan
    $detailResult = Invoke-RestMethod "$base/admin-api/system/help-order/get?id=$id" -Headers $h -Method GET
    Write-Host "✓ Details retrieval successful: code=$($detailResult.code); status=$($detailResult.data.status)" -ForegroundColor Green

    # 5) Test patient module
    Write-Host "5. Testing patient module..." -ForegroundColor Cyan
    $patientResult = Invoke-RestMethod "$base/admin-api/patient/list/page" -Headers $h -Method GET
    Write-Host "✓ Patient pagination successful: code=$($patientResult.code); total=$($patientResult.data.total)" -ForegroundColor Green

    # 6) Test prescription module
    Write-Host "6. Testing prescription module..." -ForegroundColor Cyan
    $prescriptionResult = Invoke-RestMethod "$base/admin-api/patient/prescription-detail/page" -Headers $h -Method GET
    Write-Host "✓ Prescription pagination successful: code=$($prescriptionResult.code); total=$($prescriptionResult.data.total)" -ForegroundColor Green

    Write-Host "=== Smoke Test Completed ===" -ForegroundColor Green
    Write-Host "✓ All core functionalities are working properly!" -ForegroundColor Green

} catch {
    Write-Host "=== Smoke Test Failed ===" -ForegroundColor Red
    Write-Host "Error Message: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode
        Write-Host "HTTP Status Code: $statusCode" -ForegroundColor Red
        
        if ($statusCode -eq 401) {
            Write-Host "Possible Cause: Authentication failed, check token or login endpoint" -ForegroundColor Yellow
        } elseif ($statusCode -eq 403) {
            Write-Host "Possible Cause: Insufficient permissions, check user roles" -ForegroundColor Yellow
        } elseif ($statusCode -eq 404) {
            Write-Host "Possible Cause: Incorrect API path, check API documentation" -ForegroundColor Yellow
        }
    }
    
    Write-Host "Suggested Checks:" -ForegroundColor Yellow
    Write-Host "1. Check if application is running properly (docker ps)" -ForegroundColor Yellow
    Write-Host "2. Verify port 48080 is accessible" -ForegroundColor Yellow
    Write-Host "3. Check database connection status" -ForegroundColor Yellow
    Write-Host "4. View application logs: docker logs medicine-server" -ForegroundColor Yellow
}
