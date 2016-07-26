# MQTTclient

IoTの真似事を実験するためのJavaで作ったMQTTクライアント(RequestBroker)のプロジェクト

Keyword:
Raspberry Pi, Mosquitto, MQTT, Cassandra

動作の流れ

1. AWSで稼働するMQTTサーバにRaspberry Pi 3から温度センサのデータをJSONでpublishする

2. このプロジェクトのRequestBrokerがMQTTサーバにsubscribeしている

3. RequestBrokerは、PiからpublishされたJSONデータを受信し中身を解析

4. センサID,日付,時刻,温度を、AWSで稼働するCassandraに登録する


細かいメモは、MyRaspberryPiMemoプロジェクトで。
