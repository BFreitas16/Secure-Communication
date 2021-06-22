# NOTE - the arguments: $1 should be the serverId and $2 should be the number of servers

cd .. && mvn clean package

sudo -u postgres psql -d sec$1 -a -f secure-server/src/main/java/META-INF/drop-tables.sql

java -jar secure-server/target/secure-server-1.0.jar --server.port=920$1 $1 $2 --spring.datasource.url=jdbc:postgresql://localhost:5432/sec$1 | tee deploy/logs/server$1_log.txt &

# Wait for the server to initiate the database before inserting the data
sleep 15
sudo -u postgres psql -d sec$1 -a -f secure-server/src/main/java/META-INF/load-script.sql
echo "SERVERs DEPLOY DONE, START CLIENTS NOW"
read CONT

ps axf | grep secure-server | grep -v grep | awk '{print "kill -9 " $1}' | sh
