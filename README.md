# Aplicação para teste de chaveiros de aplicações Java (JKS)

## Cenário comum de uso

- Empresa A utiliza a aplicação para gerar arquivos de mensagem, certificado e assinatura fazendo menção ao seu chaveiro;

```
java -jar jks-test.jar sign --keystore-file client.jks --keypair-alias client
```

- Empresa A envia os arquivos para Empresa B;
- Empresa B utiliza a aplicação para validar a assinatura:

```
java -jar jks-test.jar validate-signature --keystore-file server.jks --keypair-alias server --use-ca
```

> Em ambientes sensíveis a senha não deve ser informada diretamente nos comandos; a aplicação perguntará as senhas quando verificar que não foram informadas inicialmente.

## Pré-requisitos
- JDK 8 no PATH (caso não esteja, será necessário adicionar o caminho completo do JDK em cada comando que utiliza suas ferramentas);
- Maven 3 no Path (para utilizar o `build.sh`).

Para criar o jar executável:

```
./build.sh
```

> Para *criptografar* uma mensagem, é utilizada a chave pública de quem deve ler (assim, só o destinatário consegue ler a mensagem, utilizando sua chave privada).

> Para assinar uma mensagem, é utilizada a chave privada de quem escreve. O responsável pela assinatura fornece seu certificado para que os destinatários possam validá-lo (utilizando a própria chave pública do remetente ou a chave pública de uma entidade certificadora confiada).

## Testes com apenas um chaveiro

### 1. Criação do chaveiro

```
keytool -genkeypair -alias app -storepass changeit -keypass changeit -keyalg RSA -keystore app.jks -deststoretype PKCS12
```

### 2. Testes de criptografia de mensagem sem assinatura

```
java -jar jks-test.jar write-unsigned --keystore-file app.jks --password changeit --public-key-alias app
java -jar jks-test.jar read-unsigned --keystore-file app.jks --password changeit --keypair-alias app
```

## Testes com dois chaveiros com certificados autoassinados (cada chaveiro possui o certificado exportado do outro)

### 1. Criação dos chaveiros

```
keytool -genkeypair -alias client -storepass changeit -keypass changeit -keyalg RSA -keystore client.jks -deststoretype PKCS12
keytool -genkeypair -alias server -storepass changeit -keypass changeit -keyalg RSA -keystore server.jks -deststoretype PKCS12
```

### 2. Troca dos certificados

Exportação dos certificados dos chaveiros de origem

```
keytool -exportcert -keystore client.jks -alias client -file client.crt -storepass changeit -keypass changeit
keytool -exportcert -keystore server.jks -alias server -file server.crt -storepass changeit -keypass changeit
```

Importação dos certificados nos chaveiros de destino

```
keytool -importcert -keystore client.jks -alias server -file server.crt -storepass changeit -keypass changeit
keytool -importcert -keystore server.jks -alias client -file client.crt -storepass changeit -keypass changeit
```

### 3. Testes de criptografia de mensagem sem assinatura

Cria arquivo com mensagem

```
java -jar jks-test.jar write-unsigned --keystore-file client.jks --password changeit --public-key-alias server
```

Lê mensagem

```
java -jar jks-test.jar read-unsigned --keystore-file server.jks --password changeit --keypair-alias server
```

### 4. Testes de criptografia de mensagem com assinatura

Cria arquivos com mensagem, certificado e assinatura (message.txt, signature.txt e certificate.txt)

```
java -jar jks-test.jar write-signed --keystore-file client.jks --password changeit --keypair-alias client --public-key-alias server
```

Verifica certificado, assinatura e mensagem 

```
java -jar jks-test.jar read-signed --keystore-file server.jks --password changeit --keypair-alias server --public-key-alias client
```


## Testes com dois chaveiros com certificados assinados por CA que ambos confiam
Baseado no fluxo do WS-Security (https://medium.com/@robert.broeckelmann/dsig-part-1-xml-digital-signature-and-ws-security-integrity-225ea3eb894e)

### 1. Criação da CA

```
openssl genrsa -des3 -out ca.key 2048
openssl req -x509 -new -nodes -key ca.key -passin "pass:changeit" -subj "/C=BR/ST=RN/L=Natal/O=Organization/OU=Sample/CN=ca/emailAddress=/challengePassword=changeit" -sha256 -days 1825 -out ca.pem
```

### 2. Criação dos chaveiros

```
keytool -genkeypair -alias client -storepass changeit -keypass changeit -keyalg RSA -keystore client.jks -deststoretype PKCS12
keytool -genkeypair -alias server -storepass changeit -keypass changeit -keyalg RSA -keystore server.jks -deststoretype PKCS12
```

### 3. Criação do CSR (Certificate Signing Request)

```
keytool -keystore client.jks -certreq -alias client -keyalg rsa -file client.csr
keytool -keystore server.jks -certreq -alias server -keyalg rsa -file server.csr
```
### 4. Geração dos certificados pela CA

```
openssl x509 -req -CA ca.pem -CAkey ca.key -in client.csr -out client.crt -days 365 -CAcreateserial
openssl x509 -req -CA ca.pem -CAkey ca.key -in server.csr -out server.crt -days 365 -CAcreateserial
```

### 5. Importação do certificado da CA para os chaveiros

```
keytool -importcert -keystore client.jks -alias ca -file ca.pem -storepass changeit
keytool -importcert -keystore server.jks -alias ca -file ca.pem -storepass changeit
```

### 6. Importação do certificado assinado pela CA para o chaveiro de origem

```
keytool -import -keystore client.jks -file client.crt -alias client
keytool -import -keystore server.jks -file server.crt -alias server
```

### 7. Importação do certificado de quem receberá a mensagem

```
keytool -import -keystore client.jks -file server.crt -alias server
```

### 8. Verificação de assinatura e desencriptação de mensagem

Cria arquivos com mensagem, assinatura e certificado (message.txt, signature.txt e certificate.txt)

```
java -jar jks-test.jar write-signed --keystore-file client.jks --password changeit --keypair-alias client --public-key-alias server
```

Verifica certificado, assinatura e mensagem

```
java -jar jks-test.jar read-signed --keystore-file server.jks --password changeit --keypair-alias server --use-ca
```

### 9. Verificação de assinatura (caso a mensagem não seja encriptada)

Criação da assinatura

```
java -jar jks-test.jar sign --keystore-file client.jks --password changeit --keypair-alias client
```

Verificação da assinatura

```
java -jar jks-test.jar validate-signature --keystore-file server.jks --password changeit --keypair-alias server --use-ca
```

## Referências

- https://docs.oracle.com/cd/E19509-01/820-3503/ggezy/index.html
- https://docs.oracle.com/cd/E19509-01/820-3503/ggeyj/index.html
- https://gist.github.com/nielsutrecht/855f3bef0cf559d8d23e94e2aecd4ede
- https://stackoverflow.com/questions/94445/using-openssl-what-does-unable-to-write-random-state-mean (sobre mensagem "unable to write 'random state' ao gerar certificado com CA)