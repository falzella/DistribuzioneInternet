# Definisci il percorso della directory
$directoryPath = "D:\scuola\Progetto_personale\Progetto TPS\DistribuzioneInternet\ProgettoTPScLIENT\ProgettoTPS\src"

# Definisci il numero di volte da eseguire il comando
$numeroDiVolte = 5

# Imposta la directory corrente
Set-Location -Path $directoryPath

# Esegui il comando un numero di volte specificato
for ($i = 1; $i -le $numeroDiVolte; $i++) {
    Write-Host "Esecuzione numero $i"
    Start-Process -FilePath "java" -ArgumentList "progettotps.ProgettoTPS"
    Start-Sleep -Seconds 1  # Aggiungi un ritardo se necessario tra le esecuzioni
}
