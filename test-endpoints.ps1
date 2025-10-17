# Test All Azure REST Endpoints
# This script tests all Service Bus and Event Grid endpoints

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Azure REST API Endpoint Tests" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8080/api"

# Function to make API call and display result
function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Url,
        [string]$Method = "GET",
        [string]$Body = $null
    )
    
    Write-Host "Testing: $Name" -ForegroundColor Yellow
    Write-Host "URL: $Url" -ForegroundColor Gray
    
    try {
        if ($Method -eq "GET") {
            $response = Invoke-WebRequest -Uri $Url -Method $Method -ErrorAction Stop
        } else {
            $response = Invoke-WebRequest -Uri $Url -Method $Method -ContentType "application/json" -Body $Body -ErrorAction Stop
        }
        
        $content = $response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
        Write-Host "✅ SUCCESS" -ForegroundColor Green
        Write-Host $content -ForegroundColor White
    } catch {
        Write-Host "❌ FAILED" -ForegroundColor Red
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
        try {
            if ($_.Exception.Response) {
                $resp = $_.Exception.Response
                $stream = $resp.GetResponseStream()
                if ($null -ne $stream) {
                    $reader = New-Object System.IO.StreamReader($stream)
                    $body = $reader.ReadToEnd()
                    if ($body) {
                        Write-Host "Response body:" -ForegroundColor DarkGray
                        Write-Host $body -ForegroundColor White
                    }
                }
            }
        } catch {
            # ignore nested errors
        }
    }
    Write-Host ""
}

# Check if backend is running
Write-Host "Checking if backend is running..." -ForegroundColor Cyan
$portOpen = Test-NetConnection -ComputerName 'localhost' -Port 8080 -InformationLevel Quiet
if ($portOpen) {
    Write-Host "Backend port 8080 is open. Proceeding with tests..." -ForegroundColor Green
    Write-Host ""
} else {
    Write-Host "❌ Backend is NOT reachable on http://localhost:8080" -ForegroundColor Red
    Write-Host "Please start the backend first with: docker-compose up backend" -ForegroundColor Yellow
    exit 1
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Service Bus Queue Tests" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Service Bus Queue Hello
Test-Endpoint -Name "Service Bus Queue - Hello World" `
    -Url "$baseUrl/azure/servicebus/queue/hello"

# Test 2: Service Bus Queue Custom Message
$customQueueMessage = @{
    message = "Custom test message for queue"
    sender = "PowerShell Test Script"
} | ConvertTo-Json

Test-Endpoint -Name "Service Bus Queue - Custom Message" `
    -Url "$baseUrl/azure/servicebus/queue/send" `
    -Method "POST" `
    -Body $customQueueMessage

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Service Bus Topic Tests" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 3: Service Bus Topic Hello
Test-Endpoint -Name "Service Bus Topic - Hello World" `
    -Url "$baseUrl/azure/servicebus/topic/hello"

# Test 4: Service Bus Topic Custom Message
$customTopicMessage = @{
    message = "Broadcasting to all subscribers"
    sender = "PowerShell Test Script"
} | ConvertTo-Json

Test-Endpoint -Name "Service Bus Topic - Custom Message" `
    -Url "$baseUrl/azure/servicebus/topic/send" `
    -Method "POST" `
    -Body $customTopicMessage

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Event Grid Domain Tests" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 5: Event Grid Publish Hello
Test-Endpoint -Name "Event Grid Domain - Hello World" `
    -Url "$baseUrl/azure/eventgrid/publish/hello"

# Test 6: Event Grid Custom Event
$customEvent = @{
    eventType = "Custom.Test.Event"
    subject = "/testing/powershell"
    data = "This is a custom event from PowerShell"
} | ConvertTo-Json

Test-Endpoint -Name "Event Grid Domain - Custom Event" `
    -Url "$baseUrl/azure/eventgrid/publish" `
    -Method "POST" `
    -Body $customEvent

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Event Grid Topic Tests" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 7: Event Grid Topic Hello
Test-Endpoint -Name "Event Grid Topic - Hello World" `
    -Url "$baseUrl/azure/eventgrid/topic/hello"

# Test 8: Event Grid Topic Custom Event
$customTopicEvent = @{
    eventType = "Topic.Custom.Event"
    subject = "/topic/testing"
    data = "Custom topic event from PowerShell"
} | ConvertTo-Json

Test-Endpoint -Name "Event Grid Topic - Custom Event" `
    -Url "$baseUrl/azure/eventgrid/topic/publish" `
    -Method "POST" `
    -Body $customTopicEvent

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Event Grid Webhook Test" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 9: Event Grid Webhook
$webhookEvents = @(
    @{
        id = "test-event-$(Get-Random)"
        eventType = "HelloWorld.Webhook.Test"
        subject = "/webhook/testing"
        eventTime = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
        data = @{
            message = "Webhook test event"
            source = "PowerShell"
        }
        dataVersion = "1.0"
    }
) | ConvertTo-Json -Depth 10

Test-Endpoint -Name "Event Grid - Webhook Receiver" `
    -Url "$baseUrl/azure/eventgrid/webhook" `
    -Method "POST" `
    -Body $webhookEvents

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   All Tests Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Summary:" -ForegroundColor Cyan
Write-Host "  - Tested 9 different endpoints" -ForegroundColor White
Write-Host "  - Service Bus: Queue and Topic messaging" -ForegroundColor White
Write-Host "  - Event Grid: Domain and Topic events" -ForegroundColor White
Write-Host "  - Webhook: Event receiving" -ForegroundColor White
Write-Host ""
Write-Host "Tip: Check the backend logs to see message processing" -ForegroundColor Yellow
Write-Host '   Run: docker-compose logs -f backend' -ForegroundColor Gray
Write-Host ""
