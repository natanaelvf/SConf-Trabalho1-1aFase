# SConf-Trabalho1-1aFase
1a Fase do 1o Trabalho de SConf
[Enunciado](https://moodle.ciencias.ulisboa.pt/pluginfile.php/399311/mod_resource/content/12/trab1_Fase1_2022_v2.pdf)

keystorepassword : 123.Asp1rin2.

Os codigos abaixo funcionam em eclipse: Correr nova configuracao com:
Program Arguments: TrokoServer 45678 .\src\KeyStores\server 123.Asp1rin2.
VM Arguments: -Djava.security.policy==server.policy

Correr servidor
java -Djava.security.policy==server.policy TrokoServer 45678 .\src\KeyStores\server 123.Asp1rin2.

Os codigos abaixo funcionam em eclipse: Correr nova configuracao com:
Program Arguments: Trokos localhost:45678 .\src\TrustStores\trustServer .\src\KeyStores\123456789 123.Asp1rin2. 123456789
VM Arguments: -Djava.security.policy==client.policy

Correr cliente
java -Djava.security.policy==client.policy Trokos localhost:45678 .\src\TrustStores\trustServer .\src\KeyStores\123456789 123.Asp1rin2. 123456789

Para criar o certificado do servidor e a truststore:
keytool -genkeypair -alias serverRSA -keyalg RSA -keysize 2048 -storetype JCEKS -keystore KeyStores\server
keytool -exportcert -alias serverRSA -storetype JCEKS -keystore KeyStores\server -file PubKeys\serverRSApub.cer
keytool -importcert -alias serverRSA -file PubKeys\serverRSApub.cer -storetype JCEKS -keystore TrustStores\trustserver

Para criar um cliente novo:
1. keytool -genkeypair -alias <UserID>RSA -keyalg RSA -keysize 2048 -storetype JCEKS -keystore KeyStores\<UserID>
2. keytool -exportcert -alias <UserID>RSA -storetype JCEKS -keystore KeyStores\<UserID> -file PubKeys\<UserID>RSApub.cer
