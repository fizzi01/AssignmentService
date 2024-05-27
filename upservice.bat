@echo off

:: Ferma il container Docker
docker-compose stop

:: Ottieni l'elenco dei container in esecuzione
for /F "tokens=*" %%i in ('docker-compose ps -q') do (
    set running_containers=%%i
)

:: Controlla se il container Docker si è fermato correttamente
if defined running_containers (
    echo Killing containers
    for /F "tokens=*" %%i in ('docker-compose ps -q') do (
        docker kill %%i
    )
)

:: Rimuove il container Docker
docker-compose down

:: Riavvia il container Docker
docker-compose up -d
